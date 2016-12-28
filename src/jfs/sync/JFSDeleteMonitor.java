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


/**
 * Monitors the detailed state of the currently performed delete operations.
 *
 * @author Jens Heidrich
 * @version $Id: JFSDeleteMonitor.java,v 1.2 2007/02/26 18:49:09 heidrich Exp $
 */
public final class JFSDeleteMonitor {

    /**
     * Stores the only instance of the class.
     *
     * SingletonHolder is loaded on the first execution of JFSDeleteMonitor.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {

        public static final JFSDeleteMonitor INSTANCE = new JFSDeleteMonitor();

    }

    /**
     * The number of files to delete.
     */
    private int filesToDelete = 0;

    /**
     * The number of files deleted.
     */
    private int filesDeleted = 0;

    /**
     * The currently deleted file.
     */
    private JFSFile currentFile = null;


    /**
     * Creates a new synchronization object.
     */
    protected JFSDeleteMonitor() {
        // Avoid external instanciation.
    }


    /**
     * Returns the reference of the only instance.
     *
     * @return The only instance.
     */
    public static JFSDeleteMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * Restores the default values.
     */
    public void clean() {
        filesToDelete = 0;
        filesDeleted = 0;
        currentFile = null;
    }


    /**
     * @return Returns the ratio of files already deleted in percent.
     */
    public int getRatio() {
        if (filesToDelete>0) {
            return Math.round((float) filesDeleted/(float) filesToDelete*100);
        }
        return 100;
    }


    /**
     * @return Returns the current file.
     */
    public JFSFile getCurrentFile() {
        return currentFile;
    }


    /**
     * Sets the current file.
     *
     * @param currentFile
     * The current file to set.
     */
    public void setCurrentFile(JFSFile currentFile) {
        this.currentFile = currentFile;
    }


    /**
     * @return Returns the number of files to delete.
     */
    public int getFilesToDelete() {
        return filesToDelete;
    }


    /**
     * Sets the number of files to delete.
     *
     * @param filesToDelete
     * The number of files to delete to set.
     */
    public void setFilesToDelete(int filesToDelete) {
        this.filesToDelete = filesToDelete;
    }


    /**
     * @return Returns the number of files deleted.
     */
    public int getFilesDeleted() {
        return filesDeleted;
    }


    /**
     * Sets the number of files deleted.
     *
     * @param filesDeleted
     * The number of files deleted to set.
     */
    public void setFilesDeleted(int filesDeleted) {
        this.filesDeleted = filesDeleted;
    }

}
