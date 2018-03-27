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
public final class JFSCopyMonitor {

    /**
     * Stores the only instance of the class.
     *
     * SingletonHolder is loaded on the first execution of JFSCopyMonitor.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {

        public static final JFSCopyMonitor INSTANCE = new JFSCopyMonitor();

    }

    /**
     * The number of files to copy.
     */
    private int filesToCopy = 0;

    /**
     * The number of files copied.
     */
    private int filesCopied = 0;

    /**
     * The number of bytes to transfer for all file.
     */
    private long bytesToTransfer = 0;

    /**
     * The number of bytes transfered for all file.
     */
    private long bytesTransfered = 0;

    /**
     * The number of bytes to transfer for the current file.
     */
    private long bytesToTransferCurrentFile = 0;

    /**
     * The number of bytes transfered for the current file.
     */
    private long bytesTransferedCurrentFile = 0;

    /**
     * The currently copied source file.
     */
    private JFSFile currentSrc = null;

    /**
     * The currently copied target file.
     */
    private JFSFile currentTgt = null;


    /**
     * Creates a new synchronization object.
     */
    protected JFSCopyMonitor() {
        // avoid external instanciation.
    }


    /**
     * Returns the reference of the only instance.
     *
     * @return The only instance.
     */
    public static JFSCopyMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * Restores the default values.
     */
    public void clean() {
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
    public int getRatio() {
        return getRatio(bytesTransfered+bytesTransferedCurrentFile, bytesToTransfer);
    }


    /**
     * @return Returns the ratio of bytes already transfered for the current file transfer in percent.
     */
    public int getCurrentFileRatio() {
        return getRatio(bytesTransferedCurrentFile, bytesToTransferCurrentFile);
    }


    /**
     * @return Returns the ratio of files already copied in percent.
     */
    public int getCopiedFileRatio() {
        return getRatio(filesCopied, filesToCopy);
    }


    /**
     * Computes the ratio of two numbers and returns null, if the second one is zero.
     *
     * @param a
     * The file to devide.
     * @param b
     * The file to use as divisor.
     * @return The ratio of two numbers.
     */
    private int getRatio(float a, float b) {
        if (b>0) {
            return Math.round(a/b*100);
        }
        return 100;
    }


    /**
     * @return Returns the bytes to transfer.
     */
    public long getBytesToTransfer() {
        return bytesToTransfer;
    }


    /**
     * Sets the bytes to transfer.
     *
     * @param bytesToTransfer
     * The bytes to transfer to set.
     */
    void setBytesToTransfer(long bytesToTransfer) {
        this.bytesToTransfer = bytesToTransfer;
    }


    /**
     * @return Returns the bytes to transfer of the current file transfer.
     */
    public long getBytesToTransferCurrentFile() {
        return bytesToTransferCurrentFile;
    }


    /**
     * Sets the bytes to transfer of the current file transfer.
     *
     * @param bytesToTransferCurrentFile
     * The bytes to transfer to set.
     */
    void setBytesToTransferCurrentFile(long bytesToTransferCurrentFile) {
        this.bytesToTransferCurrentFile = bytesToTransferCurrentFile;
    }


    /**
     * @return Returns the bytes transfered.
     */
    public long getBytesTransfered() {
        return bytesTransfered;
    }


    /**
     * Sets the bytes transfered.
     *
     * @param bytesTransfered
     * The bytes transfered to set.
     */
    void setBytesTransfered(long bytesTransfered) {
        this.bytesTransfered = bytesTransfered;
    }


    /**
     * @return Returns the bytes transfered of the current file transfer.
     */
    public long getBytesTransferedCurrentFile() {
        return bytesTransferedCurrentFile;
    }


    /**
     * Sets the bytes transfered of the current file transfer.
     *
     * @param bytesTransferedCurrentFile
     * The bytes transfered to set.
     */
    void setBytesTransferedCurrentFile(long bytesTransferedCurrentFile) {
        this.bytesTransferedCurrentFile = bytesTransferedCurrentFile;
    }


    /**
     * @return Returns the current source.
     */
    public JFSFile getCurrentSrc() {
        return currentSrc;
    }


    /**
     * Sets the current source.
     *
     * @param currentSrc
     * The current source to set.
     */
    void setCurrentSrc(JFSFile currentSrc) {
        this.currentSrc = currentSrc;
    }


    /**
     * @return Returns the current target.
     */
    public JFSFile getCurrentTgt() {
        return currentTgt;
    }


    /**
     * Sets the current target.
     *
     * @param currentTgt
     * The current target to set.
     */
    void setCurrentTgt(JFSFile currentTgt) {
        this.currentTgt = currentTgt;
    }


    /**
     * @return Returns the currently handeled file (which equals the source file, if the source directory is not null
     * and the target file otherwise).
     */
    public JFSFile getCurrentFile() {
        assert currentSrc!=null||currentTgt!=null;
        if (currentSrc!=null) {
            return currentSrc;
        }
        return currentTgt;
    }


    /**
     * @return Returns the files copied.
     */
    public int getFilesCopied() {
        return filesCopied;
    }


    /**
     * Sets the files copied.
     *
     * @param filesCopied
     * The files copied to set.
     */
    void setFilesCopied(int filesCopied) {
        this.filesCopied = filesCopied;
    }


    /**
     * @return Returns the files to copy.
     */
    public int getFilesToCopy() {
        return filesToCopy;
    }


    /**
     * Sets the files to copy.
     *
     * @param filesToCopy
     * The files to copy to set.
     */
    void setFilesToCopy(int filesToCopy) {
        this.filesToCopy = filesToCopy;
    }


    /**
     * Sets the number of bytes to copy depending on a list of copy statements.
     *
     * @param copyStatements
     * The copy statements to consider.
     * @return The numer of bytes to transfer.
     */
    public static long getBytesToTransfer(List<JFSCopyStatement> copyStatements) {
        long bytes = 0;
        for (JFSCopyStatement cs : copyStatements) {
            if (cs.getCopyFlag()&&!cs.getSuccess()) {
                bytes += cs.getSrc().getLength();
            }
        }
        return bytes;
    }

}
