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

import java.text.DateFormat;
import java.util.Date;

/**
 * Handles an item stored in a history of synchronized files.
 * 
 * @see JFSHistory
 * @author Jens Heidrich
 * @version $Id: JFSHistoryItem.java,v 1.8 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSHistoryItem {

    /** The relative path of the synchronized files. */
    private String relativePath;

    /** The last modified date of the synchronized files. */
    private long lastModified = -1;

    /** The length of the synchronized files. */
    private long length = -1;

    /** Determines whether the item is a directory. */
    private boolean directory = false;


    /**
     * Constructs a history item.
     */
    public JFSHistoryItem(String relativePath) {
        this.relativePath = relativePath;
    }


    /**
     * Returns the last modified date.
     * 
     * @return The date as long value.
     */
    public long getLastModified() {
        return lastModified;
    }


    /**
     * Returns a date string for the last modification of the file.
     * 
     * @return Time of last modification of the file as a date string.
     */
    public final String getLastModifiedDate() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(new Date(getLastModified()));
    }


    /**
     * Sets the last modified date.
     * 
     * @param lastModified
     *            The date as long value.
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }


    /**
     * Returns the file length.
     * 
     * @return The length.
     */
    public long getLength() {
        return length;
    }


    /**
     * Sets the file length.
     * 
     * @param length
     *            The length to set.
     */
    public void setLength(long length) {
        this.length = length;
    }


    /**
     * Returns whether the item is a directory.
     * 
     * @return True if and only if the item is a directory.
     */
    public boolean isDirectory() {
        return directory;
    }


    /**
     * Sets whether the item is a directory.
     * 
     * @param directory
     *            True if and only if the item is a directory.
     */
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }


    /**
     * Returns the relative path of the file (from the JFS root files).
     * 
     * @return Returns the relative path.
     */
    public String getRelativePath() {
        return relativePath;
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "'"+getRelativePath()+"' ("+getLastModifiedDate()+")";
    }
}