/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */
package jfs.sync;

import java.util.List;
import jfs.conf.JFSConfig;
import jfs.conf.JFSHistoryManager;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSSyncModes;
import jfs.sync.JFSProgress.ProgressActivity;
import jfs.sync.JFSQuestion.QuestionAnswer;


/**
 * This class is responsible for synchronizing a table of JFS elements. In order to perform a complete synchronization
 * cycle, the following actions have to be taken: (1) perform a comparison for each directory pair, (2) compute the
 * actions applied to the comparison table according to the chosen synchronization mode, (3) compute the lists of copy
 * and delete statements from the actions, and finally (4) perform a synchronization.
 *
 * @author Jens Heidrich
 * @version $Id: JFSSynchronization.java,v 1.33 2007/06/05 16:09:41 heidrich Exp $
 */
public final class JFSSynchronization {

    /**
     * Stores the only instance of the class.
     *
     * SingletonHolder is loaded on the first execution of JFSSynchronization.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {

        public static final JFSSynchronization INSTANCE = new JFSSynchronization();

    }

    /**
     * Answers questions during before the algorithm is performed.
     */
    private JFSQuestion question = new JFSQuestion();


    /**
     * Creates a new synchronization object.
     */
    protected JFSSynchronization() {
        // Avoid external instanciation
    }


    /**
     * Returns the reference of the only instance.
     *
     * @return The only instance.
     */
    public static JFSSynchronization getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * Returns the JFS question object (e.g., in order to set the oracle).
     *
     * @return The used JFS question object.
     */
    public JFSQuestion getQuestion() {
        return question;
    }


    /**
     * Computes two lists of files according to the actions specified in the comparison table mode. The first list
     * contains copy statements and represents all files that have to copied from source to target or the other way
     * around. The second list contains delete statements and represents all files that have to be deleted. When the
     * lists are computed, the user is asked some questions regarding JFS elements for which no action could be computed
     * automatically.
     */
    public void computeSynchronizationLists() {
        // Get table and reset copy and delete statements:
        JFSTable table = JFSTable.getInstance();
        List<JFSCopyStatement> copyStatements = table.getCopyStatements();
        List<JFSDeleteStatement> deleteStatements = table.getDeleteStatements();
        copyStatements.clear();
        deleteStatements.clear();
        boolean skipAll = false;

        // Go through table and compute actions:
        JFSFileProducer srcProducer = null;
        JFSFileProducer tgtProducer = null;
        for (int i = 0; i<table.getTableSize(); i++) {
            JFSElement element = table.getTableElement(i);

            // Get producers for the current element:
            srcProducer = element.getRoot().getSrcProducer();
            tgtProducer = element.getRoot().getTgtProducer();

            // Check whether element and corresponding action is active:
            if (!element.isActive()) {
                continue;
            }

            JFSFile srcFile = element.getSrcFile();
            JFSFile tgtFile = element.getTgtFile();

            // Ask the user first:
            if (!skipAll
                    &&(element.getAction()==SyncAction.ASK_LENGTH_INCONSISTENT
                    ||element.getAction()==SyncAction.ASK_FILES_GT_HISTORY||element.getAction()==SyncAction.ASK_FILES_NOT_IN_HISTORY)) {
                QuestionAnswer a = question.answer(element);
                if (a==QuestionAnswer.SKIP_ALL) {
                    skipAll = true;
                }
            }

            // Case 1: Copy the file on source side to the target side:
            if (element.getAction()==SyncAction.COPY_SRC) {
                JFSFile newFile = tgtProducer.getJfsFile(srcFile.getRelativePath(), srcFile.isDirectory());
                copyStatements.add(new JFSCopyStatement(element, srcFile, newFile));
            }

            // Case 2: Copy the file on target side to the source side:
            if (element.getAction()==SyncAction.COPY_TGT) {
                JFSFile newFile = srcProducer.getJfsFile(tgtFile.getRelativePath(), tgtFile.isDirectory());
                copyStatements.add(new JFSCopyStatement(element, tgtFile, newFile));
            }

            // Case 3: Delete the file on target side:
            if (element.getAction()==SyncAction.DELETE_TGT||element.getAction()==SyncAction.DELETE_SRC_AND_TGT) {
                deleteStatements.add(new JFSDeleteStatement(element, tgtFile));
            }

            // Case 4: Delete the file on source side:
            if (element.getAction()==SyncAction.DELETE_SRC||element.getAction()==SyncAction.DELETE_SRC_AND_TGT) {
                deleteStatements.add(new JFSDeleteStatement(element, srcFile));
            }
        }

        // The list of delete statements has to be inverted, because the
        // directories must be empty before they can be deleted:
        invert(deleteStatements);
    }


    /**
     * Synchronizes the directory structures specified in the directory pair if and only if the test run flag is not
     * set.
     */
    public void synchronize() {
        JFSProgress progress = JFSProgress.getInstance();
        JFSConfig config = JFSConfig.getInstance();
        JFSTable table = JFSTable.getInstance();
        List<JFSCopyStatement> copyStatements = table.getCopyStatements();
        List<JFSDeleteStatement> deleteStatements = table.getDeleteStatements();

        // Handle all files to delete first:
        // (This is important for DOS and Windows Operating Systems.
        // Let us assume a file on the source side named "TEST.txt"
        // and on the target side "test.txt" and we want to copy files
        // from source and delete from target.
        // Because JFileSync works with a case-sensitive algorithm,
        // the file "TEST.txt" would be copied first and the file
        // "test.txt" would be deleted afterwards if we would
        // perform the copy statements first. Because DOS and Windows
        // don't distinguish between both file names, there wouldn't exist
        // a file with one of names on the target side after the
        // synchronization.)
        JFSDeleteMonitor dm = JFSDeleteMonitor.getInstance();
        progress.prepare(ProgressActivity.SYNCHRONIZATION_DELETE);
        dm.clean();
        dm.setFilesToDelete(deleteStatements.size());
        progress.start();
        boolean success;
        int i = 0;

        while (i<deleteStatements.size()&&!progress.isCanceled()) {
            JFSDeleteStatement ds = deleteStatements.get(i);
            dm.setCurrentFile(ds.getFile());

            // Delete only if the delete flag is set and the success flag is
            // false:
            if (ds.getDeleteFlag()&&!ds.getSuccess()) {
                success = ds.getFile().delete();
                ds.setSuccess(success);

                // Remove from table if action was successfully performed:
                if (success) {
                    JFSElement element = ds.getElement();
                    element.setAction(SyncAction.NOP);
                    table.removeElement(element);
                }
            }

            i++;
            dm.setFilesDeleted(i);
            progress.fireUpdate();
        }
        progress.end();

        // Handle all files to copy:
        JFSCopyMonitor cm = JFSCopyMonitor.getInstance();
        progress.prepare(ProgressActivity.SYNCHRONIZATION_COPY);
        cm.clean();
        cm.setFilesToCopy(copyStatements.size());
        cm.setBytesToTransfer(JFSCopyMonitor.getBytesToTransfer(copyStatements));
        progress.start();
        i = 0;

        while (i<copyStatements.size()&&!progress.isCanceled()) {
            JFSCopyStatement cs = copyStatements.get(i);
            cm.setCurrentSrc(cs.getSrc());
            cm.setCurrentTgt(cs.getTgt());
            cm.setBytesTransferedCurrentFile(0);

            // Copy only if the copy flag is set and the success flag is false:
            if (cs.getCopyFlag()&&!cs.getSuccess()) {
                cm.setBytesToTransferCurrentFile(cs.getSrc().getLength());
                success = cs.getSrc().copy(cs.getTgt());
                cs.setSuccess(success);

                // Update table element if action was successfully performed:
                if (success) {
                    JFSElement element = cs.getElement();
                    if (cs.isCopyFromSource()) {
                        element.setTgtFile(cs.getTgt());
                    } else {
                        element.setSrcFile(cs.getTgt());
                    }

                    // Revalidate element, compute action and update view:
                    element.revalidate();
                    JFSSyncModes.getInstance().getCurrentMode().computeAction(element);
                    table.updateElement(cs.getElement());
                }
                cm.setBytesTransfered(cm.getBytesTransfered()+cm.getBytesToTransferCurrentFile());
            }
            i++;
            cm.setFilesCopied(i);
            progress.fireUpdate();
        }
        progress.end();

        // Shuts down file producers of previously created comparison
        // objects:
        for (int j = 0; j<table.getRootsSize(); j++) {
            JFSRootElement root = table.getRootElement(j);
            root.shutDownProducers();
        }

        // Store the history, even if the synchronization process was
        // cancelled:
        if (config.isStoreHistory()) {
            JFSHistoryManager.getInstance().updateHistories();
        }

        // Update the current view. This has to be done, if a synchronization
        // action fails and new elements would have to be added to the current
        // view:
        table.recomputeView();
    }


    /**
     * Inverts all elements within a vector; i.e., the first element will become the last one and so on.
     *
     * @param <T>
     * The elements contained in the vector.
     * @param v
     * The vector to be inverted.
     */
    private static <T> void invert(List<T> v) {
        T temp;
        int size = v.size();

        for (int i = 0; i<size/2; i++) {
            temp = v.get(i);
            v.set(i, v.get(size-1-i));
            v.set(size-1-i, temp);
        }
    }

}
