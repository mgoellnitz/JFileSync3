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

import java.util.Vector;

import jfs.conf.JFSConfig;
import jfs.conf.JFSDirectoryPair;
import jfs.sync.JFSProgress.ProgressActivity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Compares all JFS directory pairs and adds the results to the table.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSComparison.java,v 1.31 2007/07/18 16:20:49 heidrich Exp $
 */
public class JFSComparison {

    private static Log log = LogFactory.getLog(JFSComparison.class);

    /** Stores the only instance of the class. */
    private static JFSComparison instance = null;


    /**
     * Creates a new comparison object.
     */
    private JFSComparison() {
    }


    /**
     * Returns the reference of the only instance.
     * 
     * @return The only instance.
     */
    public final static JFSComparison getInstance() {
        if (instance==null)
            instance = new JFSComparison();

        return instance;
    }


    /**
     * Inserts an element to the comparison table and starts the comparison algorithm recursively, if and only if the
     * matched files are directories. At least one (source or target) file has to be not equal to null.
     * 
     * @param srcFile
     *            A source file which may be null if no corresponding source file exists, but a target file.
     * @param tgtFile
     *            A target file which may be null if no corresponding target file exists, but a source file.
     * @param parent
     *            The parent element.
     * @param isDirectory
     *            Determines whether the files are directories.
     */
    private final void add(JFSFile srcFile, JFSFile tgtFile, JFSElement parent, boolean isDirectory) {
        assert srcFile!=null||tgtFile!=null;

        // Determine whether the comparison should be performed:
        JFSConfig config = JFSConfig.getInstance();
        if ( !config.getIncludes().isEmpty()) {
            if (srcFile!=null&& !config.matchesIncludes(srcFile))
                return;
            if (tgtFile!=null&& !config.matchesIncludes(tgtFile))
                return;
        }
        if ( !config.getExcludes().isEmpty()) {
            if (srcFile!=null&&config.matchesExcludes(srcFile))
                return;
            if (tgtFile!=null&&config.matchesExcludes(tgtFile))
                return;
        }

        // Add an element to the comparison table:
        if (isDirectory) {
            if (log.isInfoEnabled()) {
                log.info("add() comparisaon table "+srcFile+" "+tgtFile+" "+isDirectory);
            } // if
        } // if
        JFSElement element = new JFSElement(srcFile, tgtFile, parent, isDirectory);
        JFSTable.getInstance().addElement(element);

        // Start algorithm recursively, if the files are directories:
        // (This is the case, if one of them is a directory!)
        if (isDirectory&& !JFSProgress.getInstance().isCanceled()) {
            compareDirectories(srcFile, tgtFile, element);            
        } // if
    }


    /**
     * Compares two lists of files and writes the result to the comparison table. Both lists must be not equal to null.
     * 
     * @param srcFiles
     *            The array of source files.
     * @param tgtFiles
     *            The array of target files.
     * @param parent
     *            The parent element.
     * @param isDirectory
     *            Determines whether the files are directories.
     */
    private final void compareFiles(JFSFile[] srcFiles, JFSFile[] tgtFiles, JFSElement parent, boolean isDirectory) {
        assert srcFiles!=null&&tgtFiles!=null;

        // First, sort the input arrays:
        HeapSort<JFSFile> h = new HeapSort<JFSFile>();
        h.sort(srcFiles);
        h.sort(tgtFiles);

        int srcIndex = 0;
        int tgtIndex = 0;

        while (srcIndex<srcFiles.length&&tgtIndex<tgtFiles.length) {
            int comp = srcFiles[srcIndex].compareTo(tgtFiles[tgtIndex]);

            if (log.isDebugEnabled()) {
                log.debug("compareFiles()   I - srx "+srcIndex+" tgx "+tgtIndex+" comp "+comp+" "+srcFiles[srcIndex]+" "
                        +tgtFiles[tgtIndex]);
            } // if
            if (comp==0) {
                // Case 1: We found two matching files:
                add(srcFiles[srcIndex], tgtFiles[tgtIndex], parent, isDirectory);
                srcIndex++ ;
                tgtIndex++ ;
            } else if (comp>0) {
                // Case 2: No matching file was found and the source file is
                // greater than the target file. In this case we have to write
                // the target file to the comparison table and investigate the
                // next target file in the list:
                add(null, tgtFiles[tgtIndex], parent, isDirectory);
                tgtIndex++ ;
            } else if (comp<0) {
                // Case 3: No matching file was found and the target file is
                // greater than the source file. In this case we have to write
                // the source file to the comparison table and investigate the
                // next source file in the list:
                add(srcFiles[srcIndex], null, parent, isDirectory);
                srcIndex++ ;
            }
        }

        // Case 4: All source files were already handled. In this case we have
        // to write the rest of the target files into the table:
        while (tgtIndex<tgtFiles.length) {
            if (log.isDebugEnabled()) {
                log.debug("compareFiles()  II - srx "+srcIndex+" tgx "+tgtIndex+" "+tgtFiles[tgtIndex]);
            } // if
            add(null, tgtFiles[tgtIndex], parent, isDirectory);
            tgtIndex++ ;
        }

        // Case 5: All target files were already handled. In this case we have
        // to write the rest of the source files into the table:
        while (srcIndex<srcFiles.length) {
            if (log.isDebugEnabled()) {
                log.debug("compareFiles() III - srx "+srcIndex+" tgx "+tgtIndex+" "+srcFiles[srcIndex]);
            } // if
            add(srcFiles[srcIndex], null, parent, isDirectory);
            srcIndex++ ;
        }
    }


    /**
     * Compares two directories and writes the result to the comparison table. At least one (source or target) file has
     * to be not equal to null.
     * 
     * @param srcDir
     *            A source directory which may be null if no corresponding source directory exists, but a target
     *            directory.
     * @param tgtDir
     *            A target directory which may be null if no corresponding target directory exists, but a source
     *            directory.
     * @param parent
     *            The parent element.
     */
    private final void compareDirectories(JFSFile srcDir, JFSFile tgtDir, JFSElement parent) {
        assert srcDir!=null||tgtDir!=null;

        JFSProgress progress = JFSProgress.getInstance();
        JFSComparisonMonitor monitor = JFSComparisonMonitor.getInstance();

        JFSFile[] srcFileList = new JFSFile[0];
        JFSFile[] tgtFileList = new JFSFile[0];
        JFSFile[] srcDirectoryList = new JFSFile[0];
        JFSFile[] tgtDirectoryList = new JFSFile[0];
        int weight = 0;

        // log.debug("compareDirectories() "+parent.getAction());

        if (srcDir!=null) {
            srcFileList = srcDir.getFileList();
            srcDirectoryList = srcDir.getDirectoryList();

            // log.debug("compareDirectories() "+srcDir.getPath());
            // for (JFSFile f : srcFileList) {
            // log.debug("compareDirectories() - "+f.getPath());
            // } // for
            // for (JFSFile f : srcDirectoryList) {
            // log.debug("compareDirectories() - "+f.getPath());
            // } // for

            monitor.setCurrentSrc(srcDir);
            weight += 1;
        }

        if (tgtDir!=null) {
            tgtFileList = tgtDir.getFileList();
            tgtDirectoryList = tgtDir.getDirectoryList();

            // log.debug("compareDirectories() "+tgtDir.getPath());
            // for (JFSFile f : tgtFileList) {
            // log.debug("compareDirectories() - "+f.getPath());
            // } // for
            // for (JFSFile f : tgtDirectoryList) {
            // log.debug("compareDirectories() - "+f.getPath());
            // } // for

            monitor.setCurrentTgt(tgtDir);
            weight += 1;
        }

        monitor.increase(srcDirectoryList.length+tgtDirectoryList.length, weight);
        progress.fireUpdate();

        compareFiles(srcFileList, tgtFileList, parent, false);
        compareFiles(srcDirectoryList, tgtDirectoryList, parent, true);

        monitor.decrease();
    }


    /**
     * Starts comparison for all directory pairs, computes the actions that have to be taken according to the chosen
     * synchronization mode.
     */
    public final void compare() {
        // Get all directory pairs:
        Vector<JFSDirectoryPair> pairs = JFSConfig.getInstance().getDirectoryList();

        // Prepare the progress computation:
        JFSProgress progress = JFSProgress.getInstance();
        progress.prepare(ProgressActivity.COMPARISON);
        JFSComparisonMonitor monitor = JFSComparisonMonitor.getInstance();
        monitor.clean();

        // Start the progress computation:
        progress.start();
        monitor.increase(pairs.size(), 1);

        // Start comparison:
        JFSTable table = JFSTable.getInstance();
        table.clean();
        for (JFSDirectoryPair pair : pairs) {
            monitor.increase(2, 1);
            monitor.setRootUriSrc(pair.getSrc());
            monitor.setRootUriTgt(pair.getTgt());

            // Create root element and add it to the table:
            if ( !progress.isCanceled()) {
                JFSRootElement root = new JFSRootElement(pair);
                JFSTable.getInstance().addRoot(root);

                // Start comparison if root is active:
                if (root.isActive()) {
                    compareDirectories(root.getSrcFile(), root.getTgtFile(), root);
                }
            }

            monitor.decrease();
            progress.fireUpdate();
        }

        // End the progress computation:
        progress.end();
        monitor.decrease();
        monitor.setRootUriSrc("");
        monitor.setRootUriTgt("");
    }
}