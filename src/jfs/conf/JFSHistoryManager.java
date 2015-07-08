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
package jfs.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jfs.sync.JFSElement;
import jfs.sync.JFSElement.ElementState;
import jfs.sync.JFSRootElement;
import jfs.sync.JFSTable;


/**
 * Manager all histories of synchronized directory pairs.
 *
 * @author Jens Heidrich
 * @version $Id: JFSHistoryManager.java,v 1.8 2007/07/20 15:59:30 heidrich Exp $
 */
public final class JFSHistoryManager {

    /**
     * Stores the only instance of the class.
     *
     * SingletonHolder is loaded on the first execution of JFSHistoryManager.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {

        public static final JFSHistoryManager INSTANCE = new JFSHistoryManager();

    }

    /**
     * The stored histories.
     */
    private final List<JFSHistory> histories = new ArrayList<>();


    /**
     * Constructs a the only history manager.
     */
    protected JFSHistoryManager() {
        // Avoid instanciation from outside
    }


    /**
     * Returns the reference of the only instance.
     *
     * @return The only instance.
     */
    public static JFSHistoryManager getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * Sorts all histories stored.
     */
    public void sortHistories() {
        Collections.sort(histories);
    }


    /**
     * Returns the set of histories.
     *
     * @return The histories.
     */
    public List<JFSHistory> getHistories() {
        return histories;
    }


    /**
     * Returns the history for a special directory pair. If no history is found, a new one is created and added to the
     * list of histories.
     *
     * @param pair
     * The directory pair to search the history for.
     * @return The history.
     */
    public JFSHistory getHistory(JFSDirectoryPair pair) {
        for (JFSHistory h : histories) {
            if (h.getPair().equals(pair)) {
                return h;
            }
        }
        JFSHistory h = new JFSHistoryXML();
        h.setPair(pair);
        histories.add(h);
        return h;
    }


    /**
     * Adds a special history.
     *
     * @param history
     * The history to add.
     */
    public void addHistory(JFSHistory history) {
        histories.add(history);
    }


    /**
     * Deletes the history for a special history; that is, deletes the file, the history is stored to and removes the
     * history from the list of histories.
     *
     * @param history
     * The history to delete.
     */
    public void deleteHistory(JFSHistory history) {
        deleteHistoryFile(history);
        history.clear();
        histories.remove(history);
    }


    /**
     * Deletes all histories.
     *
     * @see #deleteHistory(JFSHistory)
     */
    public void deleteAll() {
        for (JFSHistory h : histories) {
            deleteHistoryFile(h);
            h.clear();
        }
        histories.clear();
    }


    /**
     * Deletes the history file for a special history.
     *
     * @param history
     * The history to delete the corresponding file for.
     */
    private void deleteHistoryFile(JFSHistory history) {
        File file = new File(JFSConst.HOME_DIR+File.separatorChar+history.getFileName());
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * Cleans the JFS configuration directory by cleaning all history files that are not referenced in the history
     * manager.
     */
    public void cleanHistories() {
        List<String> historyFiles = new ArrayList<>();
        for (JFSHistory h : getHistories()) {
            historyFiles.add(h.getFileName());
        }

        File jfsDir = new File(JFSConst.HOME_DIR);
        for (File f : jfsDir.listFiles()) {
            String name = f.getName();
            if (name.startsWith(JFSConst.HISTORY_FILE_PREFIX)&&name.endsWith(".xml")&&!historyFiles.contains(name)) {
                f.delete();
            }
        }
    }


    /**
     * Updates and stores the currently managed histories with the synchronization table. If the element's files are
     * equal (identified by their equal time stamp), an existing history item has to be updated or a new one has to be
     * created. If a history item was found (and updated) or a new one was created, it is added to the new history. The
     * relative path is stored as well as the last modified date and the length. That means, if the JFS element's files
     * are not equal, the previous history item is kept as is. The latter can be caused by an interrupted or failed
     * synchronization process or if JFS elements are deactivated before synchronization.
     */
    public void updateHistories() {
        JFSTable table = JFSTable.getInstance();

        JFSHistory h = null;
        JFSRootElement root = null;
        List<JFSHistoryItem> newHistory = null;
        Map<String, JFSHistoryItem> newDirectories = null;
        Map<String, JFSHistoryItem> newFiles = null;

        for (int i = 0; i<table.getTableSize(); i++) {
            JFSElement element = table.getTableElement(i);

            // If root is found, update previous history and get new one:
            if (element.getState()==ElementState.IS_ROOT) {
                if (root!=null&&h!=null) {
                    h.update(root, newHistory, newDirectories, newFiles);
                }

                root = (JFSRootElement) element;
                h = root.getHistory();
                h.setDate(System.currentTimeMillis());
                newHistory = new ArrayList<>();
                newDirectories = new HashMap<>();
                newFiles = new HashMap<>();
            }

            JFSHistoryItem item = h.getHistory(element);

            // If the element's files are equal, an existing item has to be
            // updated or a new one has to be created:
            if (element.getState()==ElementState.EQUAL) {
                // Use the source file (which exists if both files are equal)
                // to construct the new history item:
                if (item==null) {
                    item = new JFSHistoryItem(element.getRelativePath());
                    item.setDirectory(element.isDirectory());
                }
                item.setLastModified(element.getSrcFile().getLastModified());
                item.setLength(element.getSrcFile().getLength());
            }

            // If a history item was found or a new one was created, add this
            // to the new history:
            if (item!=null) {
                newHistory.add(item);
                if (item.isDirectory()) {
                    newDirectories.put(item.getRelativePath(), item);
                } else {
                    newFiles.put(item.getRelativePath(), item);
                }
            }
        }

        // Update last history read:
        if (root!=null&&h!=null) {
            h.update(root, newHistory, newDirectories, newFiles);
        }
    }

}
