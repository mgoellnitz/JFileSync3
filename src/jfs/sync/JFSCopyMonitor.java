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

/**
 * Monitors the detailed state of the currently performed copy operations.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSCopyMonitor.java,v 1.2 2007/02/26 18:49:09 heidrich Exp $
 */
public class JFSCopyMonitor {

    /** Stores the only instance of the class. */
    private static JFSCopyMonitor instance = null;

    /** The number of files to copy. */
    private int filesToCopy = 0;

    /** The number of files copied. */
    private int filesCopied = 0;

    /** The number of bytes to transfer for all file. */
    private long bytesToTransfer = 0;

    /** The number of bytes transfered for all file. */
    private long bytesTransfered = 0;

    /** The number of bytes to transfer for the current file. */
    private long bytesToTransferCurrentFile = 0;

    /** The number of bytes transfered for the current file. */
    private long bytesTransferedCurrentFile = 0;

    /** The currently copied source file. */
    private JFSFile currentSrc = null;

    /** The currently copied target file. */
    private JFSFile currentTgt = null;


    /**
     * Creates a new synchronization object.
     */
    private JFSCopyMonitor() {
    }


    /**
     * Returns the reference of the only instance.
     * 
     * @return The only instance.
     */
    public final static JFSCopyMonitor getInstance() {
        if (instance==null) {
            instance = new JFSCopyMonitor();
        }

        return instance;
    }


    /**
     * Restores the default values.
     */
    public final void clean() {
        filesToCopy = 0;
        filesCopied = 0;
        bytesToTransfer = 0;
        bytesTransfered = 0;
        bytesToTransferCurrentFile = 0;
        bytesTransferedCurrentFile = 0;
        currentSrc = null;
        currentTgt = null;
    }


    /**
     * @return Returns the ratio of bytes already transfered in percent.
     */
    public final int getRatio() {
        return getRatio(bytesTransfered, bytesToTransfer);
    }


    /**
     * @return Returns the ratio of bytes already transfered for the current file transfer in percent.
     */
    public final int getCurrentFileRatio() {
        return getRatio(bytesTransferedCurrentFile, bytesToTransferCurrentFile);
    }


    /**
     * @return Returns the ratio of files already copied in percent.
     */
    public final int getCopiedFileRatio() {
        return getRatio(filesCopied, filesToCopy);
    }


    /**
     * Computes the ratio of two numbers and returns null, if the second one is zero.
     * 
     * @param a
     *            The file to devide.
     * @param b
     *            The file to use as divisor.
     * @return The ratio of two numbers.
     */
    private final int getRatio(float a, float b) {
        if (b>0) {
            return Math.round(a/b*100);
        }
        return 100;
    }


    /**
     * @return Returns the bytes to transfer.
     */
    public final long getBytesToTransfer() {
        return bytesToTransfer;
    }


    /**
     * Sets the bytes to transfer.
     * 
     * @param bytesToTransfer
     *            The bytes to transfer to set.
     */
    final void setBytesToTransfer(long bytesToTransfer) {
        this.bytesToTransfer = bytesToTransfer;
    }


    /**
     * @return Returns the bytes to transfer of the current file transfer.
     */
    public final long getBytesToTransferCurrentFile() {
        return bytesToTransferCurrentFile;
    }


    /**
     * Sets the bytes to transfer of the current file transfer.
     * 
     * @param bytesToTransferCurrentFile
     *            The bytes to transfer to set.
     */
    final void setBytesToTransferCurrentFile(long bytesToTransferCurrentFile) {
        this.bytesToTransferCurrentFile = bytesToTransferCurrentFile;
    }


    /**
     * @return Returns the bytes transfered.
     */
    public final long getBytesTransfered() {
        return bytesTransfered;
    }


    /**
     * Sets the bytes transfered.
     * 
     * @param bytesTransfered
     *            The bytes transfered to set.
     */
    final void setBytesTransfered(long bytesTransfered) {
        this.bytesTransfered = bytesTransfered;
    }


    /**
     * @return Returns the bytes transfered of the current file transfer.
     */
    public final long getBytesTransferedCurrentFile() {
        return bytesTransferedCurrentFile;
    }


    /**
     * Sets the bytes transfered of the current file transfer.
     * 
     * @param bytesTransferedCurrentFile
     *            The bytes transfered to set.
     */
    final void setBytesTransferedCurrentFile(long bytesTransferedCurrentFile) {
        this.bytesTransferedCurrentFile = bytesTransferedCurrentFile;
    }


    /**
     * @return Returns the current source.
     */
    public final JFSFile getCurrentSrc() {
        return currentSrc;
    }


    /**
     * Sets the current source.
     * 
     * @param currentSrc
     *            The current source to set.
     */
    final void setCurrentSrc(JFSFile currentSrc) {
        this.currentSrc = currentSrc;
    }


    /**
     * @return Returns the current target.
     */
    public final JFSFile getCurrentTgt() {
        return currentTgt;
    }


    /**
     * Sets the current target.
     * 
     * @param currentTgt
     *            The current target to set.
     */
    final void setCurrentTgt(JFSFile currentTgt) {
        this.currentTgt = currentTgt;
    }


    /**
     * @return Returns the currently handeled file (which equals the source file, if the source directory is not null
     *         and the target file otherwise).
     */
    public final JFSFile getCurrentFile() {
        assert currentSrc!=null||currentTgt!=null;
        if (currentSrc!=null) {
            return currentSrc;
        }
        return currentTgt;
    }


    /**
     * @return Returns the files copied.
     */
    public final int getFilesCopied() {
        return filesCopied;
    }


    /**
     * Sets the files copied.
     * 
     * @param filesCopied
     *            The files copied to set.
     */
    final void setFilesCopied(int filesCopied) {
        this.filesCopied = filesCopied;
    }


    /**
     * @return Returns the files to copy.
     */
    public final int getFilesToCopy() {
        return filesToCopy;
    }


    /**
     * Sets the files to copy.
     * 
     * @param filesToCopy
     *            The files to copy to set.
     */
    final void setFilesToCopy(int filesToCopy) {
        this.filesToCopy = filesToCopy;
    }


    /**
     * Sets the number of bytes to copy depending on a list of copy statements.
     * 
     * @param copyStatements
     *            The copy statements to consider.
     * @return The numer of bytes to transfer.
     */
    public final static long getBytesToTransfer(List<JFSCopyStatement> copyStatements) {
        long bytes = 0;
        for (JFSCopyStatement cs : copyStatements) {
            if (cs.getCopyFlag()&& !cs.getSuccess()) {
                bytes += cs.getSrc().getLength();
            }
        }
        return bytes;
    }
}