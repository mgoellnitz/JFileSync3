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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;


/**
 * Represents a directory or a simple file. This object encapsulates the Java File object.
 *
 * @author Jens Heidrich
 * @version $Id: JFSFile.java,v 1.33 2007/07/20 12:27:52 heidrich Exp $
 */
public abstract class JFSFile implements Comparable<JFSFile> {

    /**
     * The assigned file producer.
     */
    protected JFSFileProducer fileProducer;

    /**
     * The relative path of the JFS file starting from the root JFS file.
     */
    protected String relativePath;


    /**
     * Creates a new JFS file from a relative path.
     *
     * @param fileProducer
     * The assigned file producer.
     * @param relativePath
     * The relative path of the JFS file starting from the root JFS file.
     */
    protected JFSFile(JFSFileProducer fileProducer, String relativePath) {
        this.fileProducer = fileProducer;
        this.relativePath = JFSFormatter.replaceSeparatorChar(relativePath);
    }


    /**
     * Returns the corresponding file object if possible, null otherwise.
     *
     * @return File object.
     */
    // public abstract File getFile();

    /**
     * Returns the name of the file.
     *
     * @return Name of the file.
     */
    public abstract String getName();


    /**
     * Returns the path of the file starting from the root JFS file.
     *
     * @return Path of the file.
     */
    public abstract String getPath();


    /**
     * Returns the assigned file producer.
     *
     * @return The file producer.
     */
    public final JFSFileProducer getFileProducer() {
        return fileProducer;
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
     * Returns whether the file is a directory.
     *
     * @return True if and only if the file is a directory.
     */
    public abstract boolean isDirectory();


    /**
     * Returns whether we can read the file.
     *
     * @return True if and only if we can read the file.
     */
    public abstract boolean canRead();


    /**
     * Returns whether we can write to the file.
     *
     * @return True if and only if we can write to the file.
     */
    public abstract boolean canWrite();


    /**
     * Returns the length of the file.
     *
     * @return Length of the file.
     */
    public abstract long getLength();


    /**
     * Returns the time of the last modification of the file.
     *
     * @return Time of last modification of the file.
     */
    public abstract long getLastModified();


    /**
     * Returns the included files. The returned list must be not equal to null. If no children exist, an array of size
     * zero is returned.
     *
     * @return An array of JFSFile objects included in the directory.
     */
    public abstract JFSFile[] getList();


    /**
     * Returns the included directories.
     *
     * @return An array of JFSFile objects included in the directory.
     */
    public final JFSFile[] getDirectoryList() {
        JFSFile[] list = getList();

        assert list!=null;

        int length = 0;

        for (JFSFile f : list) {
            if (f.isDirectory()) {
                length++;
            }
        }

        JFSFile[] directoryList = new JFSFile[length];
        int j = 0;

        for (JFSFile f : list) {
            if (f.isDirectory()) {
                directoryList[j] = f;
                j++;
            }
        }

        return directoryList;
    }


    /**
     * Returns the included files (not directories).
     *
     * @return An array of JFSFile objects included in the directory.
     */
    public final JFSFile[] getFileList() {
        JFSFile[] list = getList();

        assert list!=null;

        int length = 0;

        for (JFSFile f : list) {
            if (!f.isDirectory()) {
                length++;
            } // if
        } // for

        JFSFile[] fileList = new JFSFile[length];
        int j = 0;

        for (JFSFile f : list) {
            if (!f.isDirectory()) {
                fileList[j] = f;
                j++;
            }
        } // for

        return fileList;
    }


    /**
     * Tests whether the file denoted by this abstract pathname exists.
     *
     * @return True if and only if the file denoted by this abstract pathname exists; false otherwise.
     */
    public abstract boolean exists();


    /**
     * Creates the directory named by this abstract pathname.
     *
     * @return True if and only if the directory was created; false otherwise.
     */
    public abstract boolean mkdir();


    /**
     * Sets the last-modified time of the file or directory named by this abstract pathname.
     *
     * @param time
     * The new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * @return True if and only if the operation succeeded; false otherwise.
     */
    public abstract boolean setLastModified(long time);


    /**
     * Marks the file or directory named by this abstract pathname so that only read operations are allowed.
     *
     * @return True if and only if the operation succeeded; false otherwise.
     */
    public abstract boolean setReadOnly();


    /**
     * Deletes the file or directory denoted by this abstract pathname. If this pathname denotes a directory, then the
     * directory must be empty in order to be deleted.
     *
     * @return True if and only if the file or directory is successfully deleted; false otherwise.
     */
    public abstract boolean delete();


    /**
     * Returns the input stream if the file is not a directory and null if it is a directory or nor stream could be
     * created.
     *
     * @return The input stream.
     */
    protected abstract InputStream getInputStream();


    /**
     * Returns the output stream if the file is not a directory and null if it is a directory or nor stream could be
     * created.
     *
     * @return The output stream.
     */
    protected abstract OutputStream getOutputStream();


    /**
     * Performs operations for closing the created input stream for this JFS file.
     */
    protected abstract void closeInputStream();


    /**
     * Performs operations for closing the created output stream for this JFS file.
     */
    protected abstract void closeOutputStream();


    /**
     * Performs operation before the copy statement on target side, for instance, preparing setting of file attributes
     * like last modified and can write property.
     *
     * @param srcFile
     * The file to copy from.
     * @return True if and only if the operation was successful.
     */
    protected abstract boolean preCopyTgt(JFSFile srcFile);


    /**
     * Performs operation before the copy statement on source side.
     *
     * @param tgtFile
     * The file to copy to.
     * @return True if and only if the operation was successful.
     */
    protected abstract boolean preCopySrc(JFSFile tgtFile);


    /**
     * Performs operation after the copy statement on target side, for instance, preparing setting of file attributes
     * like last modified and can write property.
     *
     * @param srcFile
     * The file to copy from.
     * @return True if and only if the operation was successful.
     */
    protected abstract boolean postCopyTgt(JFSFile srcFile);


    /**
     * Performs operation after the copy statement on source side.
     *
     * @param tgtFile
     * The file to copy to.
     * @return True if and only if the operation was successful.
     */
    protected abstract boolean postCopySrc(JFSFile tgtFile);


    /**
     * Flushes all changes to the file object if not performed yet. This is done recursively. So, a flush on the root
     * file performs a flush on all childs.
     *
     * @return True if and only if the operation was successful.
     */
    public abstract boolean flush();


    /**
     * Writes the content of the JFS file to a new target file. If this file is a directory false is returned. This
     * method just copies the contents from this file to the target file, no attributes, like the last modified date or
     * the can attribute are adopted.
     *
     * @param in
     * The input stream of the source file.
     * @param out
     * The output stream of the target file.
     * @return True if and only if the file is not a directory and was successfully copied; false otherwise.
     */
    private final boolean copy(InputStream in, OutputStream out) {
        JFSText t = JFSText.getInstance();
        JFSProgress progress = JFSProgress.getInstance();
        JFSCopyMonitor monitor = JFSCopyMonitor.getInstance();

        if (isDirectory()) {
            return false;
        }

        try {
            if ((in==null)||(out==null)) {
                // System.out.println("copy("+this.getPath()+this.getName()+") 2 "+in+" "+out);
                return false;
            }

            byte[] buf = new byte[JFSConfig.getInstance().getBufferSize()];
            long length = getLength();
            long transferedBytes = 0;
            int len;
            int maxLen = JFSConfig.getInstance().getBufferSize();

            if (length<maxLen) {
                maxLen = (int) length;
            }

            while (transferedBytes<length&&(len = in.read(buf, 0, maxLen))>0&&!progress.isCanceled()) {
                out.write(buf, 0, len);
                transferedBytes += len;

                long r = length-transferedBytes;
                if (r<maxLen) {
                    maxLen = (int) r;
                }

                monitor.setBytesTransferedCurrentFile(transferedBytes);
                progress.fireUpdate();
            }

            if (transferedBytes==length) {
                return true;
            }
            // System.out.println("copy() 3");
            return false;
        } catch (IOException e) {
            e.printStackTrace(System.out);
            PrintStream p = JFSLog.getErr().getStream();
            p.println(t.get("error.io"));
            p.println("  '"+this.getPath()+"'");

            // System.out.println("copy("+this.getPath()+this.getName()+") 4");
            return false;
        }
    }


    /**
     * Writes the content of the JFSFile to a new target file. If this JFSFile is a directory the target directory is
     * made.
     *
     * @param tgtFile
     * The Target File.
     * @return True if and only if the file is successfully copied; false otherwise.
     */
    public final boolean copy(JFSFile tgtFile) {
        // Test whether the source file (this) can be read by the application
        // and the target file (tgtFile) can be written to. If not, false is
        // returned:
        if (!canRead()||!tgtFile.canWrite()) {
            return false;
        }

        boolean success = tgtFile.preCopyTgt(this);
        success = success&&preCopySrc(tgtFile);

        if (isDirectory()) {
            // System.out.println("copy() "+tgtFile.getName());
            success = success&&tgtFile.mkdir();
        } else {
            success = success&&copy(getInputStream(), tgtFile.getOutputStream());
            closeInputStream();
            tgtFile.closeOutputStream();
        }

        success = success&&tgtFile.postCopyTgt(this);
        success = success&&postCopySrc(tgtFile);

        if (!success||(JFSProgress.getInstance().isCanceled())) {
            tgtFile.delete();
            success = false;
        }

        return success;
    }


    /**
     * Returns the result of the comparison of the names of two JFSFile objects.
     *
     * @param jfsFile
     * The file object to compare the current object with.
     * @return Result of the comparison.
     */
    @Override
    public final int compareTo(JFSFile jfsFile) {
        return this.getName().compareTo(jfsFile.getName());
    }


    @Override
    public String toString() {
        return getName();
    } // toString()

}
