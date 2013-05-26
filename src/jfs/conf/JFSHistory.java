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
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import jfs.sync.JFSElement;
import jfs.sync.JFSRootElement;

/**
 * Handles a history of synchronized files for a certain directory pair.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHistory.java,v 1.12 2007/07/20 15:59:30 heidrich Exp $
 */
public abstract class JFSHistory implements Comparable<JFSHistory> {
    /** The directory pair the history is created for. */
    private JFSDirectoryPair pair = null;

    /** The date when the history was created or updated. */
    private long date = -1;

    /** The items of the history. */
    protected Vector<JFSHistoryItem> history = new Vector<JFSHistoryItem>();

    /** The directory items of the history for fast access. */
    protected HashMap<String, JFSHistoryItem> directories = new HashMap<String, JFSHistoryItem>();

    /** The file items of the history for fast access. */
    protected HashMap<String, JFSHistoryItem> files = new HashMap<String, JFSHistoryItem>();

    /** The assigned file name the history is stored in and loaded from. */
    private String fileName = null;

    /** Determines whether the history was already loaded from file. */
    private boolean isLoaded = false;


    /**
     * Clears all history elements apart from the assigned directory pair.
     */
    public void clear() {
        date = -1;
        history.clear();
        directories.clear();
        files.clear();
        fileName = null;
        isLoaded = false;
    }


    /**
     * Loads a history. If the history is already loaded true is returned.
     * 
     * @return True if and only if loading was successful.
     */
    public boolean load() {
        if (isLoaded)
            return true;

        // Return false, if no pair or no file was assigned:
        if (pair==null||fileName==null)
            return false;

        File file = new File(JFSConst.HOME_DIR+File.separatorChar+fileName);
        isLoaded = load(file);

        return isLoaded;
    }


    /**
     * Loads a history.
     * 
     * @param file
     *            The history to load.
     * @return True if and only if loading did not fail.
     */
    protected abstract boolean load(File file);


    /**
     * Stores a history. If no pair is assigned, false is returned. If no file is assigned a new file is created in the
     * JFS configuration directory. If the history is not part if the history manager, it is added to the manager.
     * 
     * @return True if and only if storing was successful.
     */
    public boolean store() {
        // Return false, if no pair is assigned:
        if (pair==null)
            return false;

        // Create new unique file name, if no file is assigned:
        File file = null;
        if (fileName==null) {
            long time = System.currentTimeMillis();
            do {
                fileName = JFSConst.HISTORY_FILE_PREFIX+time+".xml";
                file = new File(JFSConst.HOME_DIR+File.separatorChar+fileName);
                time += 1;
            } while (file.exists());
        } else {
            file = new File(JFSConst.HOME_DIR+File.separatorChar+fileName);
        }

        // Check that history is in manager:
        JFSHistoryManager hm = JFSHistoryManager.getInstance();
        if ( !hm.getHistories().contains(this))
            hm.addHistory(this);
        hm.sortHistories();

        boolean success = store(file);

        // Store settings when history is saved successfully:
        if (success)
            JFSSettings.getInstance().store();

        return success;
    }


    /**
     * Stores a history.
     * 
     * @param file
     *            The history to store.
     * @return True if and only if storing did not fail.
     */
    protected abstract boolean store(File file);


    /**
     * Returns the history item for a certain JFS element or null if no item was found.
     * 
     * @param element
     *            The JFS element to search a history item for.
     * @return The corresponding history item.
     */
    public final JFSHistoryItem getHistory(JFSElement element) {
        if (element.isDirectory()) {
            return directories.get(element.getRelativePath());
        }
        return files.get(element.getRelativePath());
    }


    /**
     * Returns the date when the history was created/updated.
     * 
     * @return The date.
     */
    public long getDate() {
        return date;
    }


    /**
     * Returns a date string for the last modification of the file.
     * 
     * @return Time of last modification of the file as a date string.
     */
    public final String getDateAsString() {
        if (getDate()!= -1) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            return df.format(new Date(getDate()));
        }
        return "-";
    }


    /**
     * Sets the date when the history was created/updated.
     * 
     * @param date
     *            The date to set.
     */
    public void setDate(long date) {
        this.date = date;
    }


    /**
     * Returns the assigned directory pair.
     * 
     * @return The directory pair.
     */
    public JFSDirectoryPair getPair() {
        return pair;
    }


    /**
     * Sets the assigned directory pair.
     * 
     * @param pair
     *            The directory pair to set.
     */
    public void setPair(JFSDirectoryPair pair) {
        this.pair = pair;
    }


    /**
     * Returns the file to load the history from and to store the history to.
     * 
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }


    /**
     * Sets the file to load the history from and to store the history to.
     * 
     * @param fileName
     *            The file name to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(JFSHistory h) {
        if (getDate()>h.getDate()) {
            return 1;
        } else if (getDate()<h.getDate()) {
            return -1;
        } else {
            return 0;
        }
    }


    /**
     * Updates and stores the history.
     * 
     * @param root
     *            The root element for the history.
     * @param newHistory
     *            The new history vector.
     * @param newDirectories
     *            The new directories hash map.
     * @param newFiles
     *            The new files hash map.
     */
    public void update(JFSRootElement root, Vector<JFSHistoryItem> newHistory,
            HashMap<String, JFSHistoryItem> newDirectories, HashMap<String, JFSHistoryItem> newFiles) {
        if (root.isActive()) {
            // Replace old history:
            history = newHistory;
            directories = newDirectories;
            files = newFiles;

            // Store history:
            store();
        }
    }
}