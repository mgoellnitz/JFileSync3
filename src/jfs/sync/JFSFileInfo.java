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

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import jfs.conf.JFSLog;


/**
 * Stores information about a certain file to be transmitted between JFS server and client.
 *
 * @author Jens Heidrich
 * @version $Id: JFSFileInfo.java,v 1.22 2007/07/18 16:20:49 heidrich Exp $
 */
public class JFSFileInfo implements Serializable {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 42L;

    /**
     * The name of the file.
     */
    private String name = "";

    /**
     * The real path of the file on server side.
     */
    private String path = "";

    /**
     * The path of the assigned root JFS file (relative from the server's base directory).
     */
    protected String rootPath = "";

    /**
     * The relative path of the JFS file starting from the root JFS file.
     */
    protected String relativePath = "";

    /**
     * Determines whether the file is a directory.
     */
    private boolean isDirectory = false;

    /**
     * Determines whether we can read the file.
     */
    private boolean canRead = true;

    /**
     * Determines whether we can write to the file.
     */
    private boolean canWrite = true;

    /**
     * Determines whether the file exists.
     */
    private boolean exists = false;

    /**
     * The length of the file. Zero for directories.
     */
    private long length = 0;

    /**
     * The time of last modification of the file. Zero for directories.
     */
    private long lastModified = 0;

    /**
     * If the file is a directory this points to all files (and directories) within the directory. Null for all non
     * directories.
     */
    private JFSFileInfo[] list = null;


    /**
     * Creates a new file information object.
     *
     * @param rootPath
     * The path of the root JFS file (relative from the server's base directory).
     * @param relativePath
     * The relative path of the JFS file starting from the root JFS file.
     */
    public JFSFileInfo(String rootPath, String relativePath) {
        this.rootPath = rootPath;
        this.relativePath = relativePath;
    }


    /**
     * Completes the information of the JFS file information object. This method is called on the server side in order
     * to set information about the path on the server.
     *
     * @return The file object used to extract the path information.
     */
    public final File complete() {
        rootPath = JFSFormatter.replaceSeparatorChar(rootPath);
        relativePath = JFSFormatter.replaceSeparatorChar(relativePath);

        File file = new File(rootPath+relativePath);

        name = file.getName();
        path = file.getPath();
        exists = file.exists();

        if (exists) {
            canRead = file.canRead();
            canWrite = file.canWrite();
        }

        return file;
    }


    /**
     * Updates the object on the basis of the current file system. This method is called on the server side in order to
     * fill the object with real information about the file. The whole structure for the server's file systen is read
     * in.
     */
    public final void update() {
        File file = complete();

        if (exists) {
            isDirectory = file.isDirectory();
            if (!isDirectory) {
                length = file.length();
                lastModified = file.lastModified();
            }

            if (list==null) {
                String[] fileList = file.list();

                if (isDirectory&&(fileList!=null)) {
                    list = new JFSFileInfo[fileList.length];

                    for (int i = 0; i<fileList.length; i++) {
                        list[i] = new JFSFileInfo(rootPath, relativePath+File.separator+fileList[i]);
                        list[i].update();
                    }
                }
            }
        }
    }


    /**
     * Updates the current file on the basis of this object.
     *
     * @return True if the update could be performed successfully.
     */
    public final boolean updateFileSystem() {
        boolean success = true;

        File file = new File(path);

        if (!isDirectory) {
            success = success&&file.setLastModified(lastModified);
        }

        if (!canWrite) {
            success = success&&file.setReadOnly();
        }

        if (isDirectory&&list!=null) {
            for (JFSFileInfo fi : list) {
                success = success&&fi.updateFileSystem();
            }
        }

        return success;
    }


    /**
     * Returns the name of the file.
     *
     * @return Name of the file.
     */
    public final String getName() {
        return name;
    }


    /**
     * Sets the name of the file.
     *
     * @param name
     * Name of the file.
     */
    public final void setName(String name) {
        this.name = name;
    }


    /**
     * Returns the path of the file.
     *
     * @return Path of the file.
     */
    public final String getPath() {
        return path;
    }


    /**
     * Sets the path of the file.
     *
     * @param path
     * Path of the file.
     */
    public final void setPath(String path) {
        this.path = path;
    }


    /**
     * Returns the root JFS path of the file.
     *
     * @return Path of the root JFS file.
     */
    public final String getRootPath() {
        return rootPath;
    }


    /**
     * Sets the root path of the file.
     *
     * @param rootPath
     * Path of the file.
     */
    public final void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }


    /**
     * Returns the relative path of the file.
     *
     * @return Path of the file.
     */
    public final String getRelativePath() {
        return relativePath;
    }


    /**
     * Sets the relative path of the file.
     *
     * @param relativePath
     * Path of the file.
     */
    public final void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }


    /**
     * Returns the virtual path of the file (the root path concattenated with the relative path of the file).
     *
     * @return Virtual path of the file.
     */
    public final String getVirtualPath() {
        return rootPath+relativePath;
    }


    /**
     * Returns whether the file is a directory.
     *
     * @return True if and only if the file is a directory.
     */
    public final boolean isDirectory() {
        return isDirectory;
    }


    /**
     * Sets whether the file on server side is a directory or not.
     *
     * @param b
     * True if and only if the file is a directory.
     */
    public void setDirectory(boolean b) {
        isDirectory = b;
    }


    /**
     * Returns whether we can read the file.
     *
     * @return True if and only if we can read the file.
     */
    public final boolean canRead() {
        return canRead;
    }


    /**
     * Returns whether we can write to the file.
     *
     * @return True if and only if we can write to the file.
     */
    public final boolean canWrite() {
        return canWrite;
    }


    /**
     * Marks the file or directory named by this abstract pathname so that only read operations are allowed.
     */
    public final void setReadOnly() {
        canWrite = false;
    }


    /**
     * Returns the length of the file.
     *
     * @return Length of the file.
     */
    public final long getLength() {
        return length;
    }


    /**
     * Sets the length of the file.
     *
     * @param l
     * The length to set.
     */
    public void setLength(long l) {
        length = l;
    }


    /**
     * Returns the time of the last modification of the file.
     *
     * @return Time of last modification of the file.
     */
    public final long getLastModified() {
        return lastModified;
    }


    /**
     * Sets the last-modified time of the file or directory named by this abstract pathname.
     *
     * @param time
     * The new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    public final void setLastModified(long time) {
        lastModified = time;
    }


    /**
     * Returns the included files.
     *
     * @return An array of JFSFile objects included in the directory.
     */
    public final JFSFileInfo[] getList() {
        return list;
    }


    /**
     * Tests whether the file denoted by this abstract pathname exists.
     *
     * @return True if and only if the file denoted by this abstract pathname exists; false otherwise.
     */
    public final boolean exists() {
        return exists;
    }


    /**
     * Sets whether the file exists or not on server side.
     *
     * @param b
     * True if and only if the file exists.
     */
    public void setExists(boolean b) {
        exists = b;
    }


    /**
     * Prints information about the specified file to the JFS standard out.
     */
    public void print() {
        PrintStream out = JFSLog.getOut().getStream();
        out.println("Name: "+getPath());

        if (isDirectory()) {
            out.println("  List: "+Arrays.asList(getList()));
        } else {
            out.println("  Length: "+getLength());
            out.println("  Last Modified: "+getLastModified());
        }
        out.println();
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return getRelativePath()+":"+getName()+" ("+getRootPath()+")";
    }

}
