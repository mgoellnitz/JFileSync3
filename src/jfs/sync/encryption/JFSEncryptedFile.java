/*
 * Copyright (C) 2010-2016 Martin Goellnitz
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
package jfs.sync.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;
import jfs.sync.JFSFile;
import jfs.sync.JFSProgress;
import jfs.sync.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JFSFile imlementation to deal with files where the content is encrypted.
 */
public class JFSEncryptedFile extends JFSFile {

    private static final Logger LOG = LoggerFactory.getLogger(JFSEncryptedFile.class);

    /**
     * duplication to avoid constant casts
     */
    private final AbstractFileProducer fileProducer;

    private FileInfo fileInfo;

    /**
     * If the file is a directory this points to all files (and directories) within the directory. Null for all non
     * directories.
     */
    private JFSFile[] list = null;

    /** The last input stream opened for this file. */
    private InputStream in = null;

    /** The last output stream opened for this file. */
    private OutputStream out = null;


    /**
     * Creates a new local JFS file object.
     *
     * @param fileProducer The assigned file producer.
     * @param cipherSpec JCE cipher specification.
     * @param relativePath The relative path of the JFS file starting from the root JFS file.
     */
    JFSEncryptedFile(AbstractFileProducer fileProducer, String relativePath) {
        super(fileProducer, relativePath);
        // super has a somewhat buggy normalization of filename only dealing with local file separator definitions
        this.relativePath = this.relativePath.replace('\\', '/');
        this.relativePath = this.relativePath.replace("/", fileProducer.getSeparator());
        this.fileProducer = fileProducer;

        fileInfo = fileProducer.getFileInfo(getRelativePath());
        LOG.debug("('{}') name={}", getRelativePath(), fileInfo.getPath());
    }// JFSEncryptedFile()


    private Cipher getCipher(int cipherMode) {
        try {
            String cipherSpec = ((AbstractFileProducer) getFileProducer()).storageAccess.getCipherSpec();
            byte[] credentials = ((AbstractFileProducer) getFileProducer()).storageAccess.getFileCredentials(getRelativePath());
            return SecurityUtils.getCipher(cipherSpec, cipherMode, credentials);
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("getCipher() No Such Algorhithm");
        } catch (NoSuchPaddingException nspe) {
            LOG.error("getCipher() No Such Padding");
        } catch (InvalidKeyException ike) {
            LOG.error("getCipher() Invalid Key {}", ike.getLocalizedMessage());
        } // try/catch
        return null;
    } // getCipher()


    /**
     * @see JFSFile#getName()
     */
    @Override
    public final String getName() {
        LOG.debug("getName('{}') {}", getRelativePath(), fileInfo.getName());
        return fileInfo.getName();
    }


    /**
     * @see JFSFile#getPath()
     */
    @Override
    public final String getPath() {
        return fileProducer.getRootPath()+getRelativePath();
    }


    /**
     * @see JFSFile#isDirectory()
     */
    @Override
    public final boolean isDirectory() {
        return fileInfo.isDirectory();
    }


    /**
     * @see JFSFile#canRead()
     */
    @Override
    public final boolean canRead() {
        return fileInfo.isCanRead();
    }


    /**
     * @see JFSFile#canWrite()
     */
    @Override
    public final boolean canWrite() {
        LOG.debug("canWrite() {}", fileInfo.isCanWrite());
        return fileInfo.isCanWrite();
        // return true;
    }


    /**
     * @see JFSFile#getLength()
     */
    @Override
    public final long getLength() {
        if (fileInfo.getSize()<0) {
            try {
                // TODO: move this to storage layer?
                InputStream fis = fileProducer.getInputStream(getRelativePath());
                ObjectInputStream ois = new ObjectInputStream(fis);
                JFSEncryptedStream.readMarker(ois);
                fileInfo.setSize(JFSEncryptedStream.readLength(ois));
                LOG.debug("getLength({}) detected plain text length {}", getRelativePath(), fileInfo.getSize());
                ois.close();
            } catch (Exception e) {
                // TODO: what to do now?!?!?!
                LOG.error("getLength() could not detect plain text length for "+getPath(), e);
            } // try/catch
        } // if
        return fileInfo.getSize();
    } // getLength()


    /**
     * @see JFSFile#getLastModified()
     */
    @Override
    public final long getLastModified() {
        LOG.debug("lastModified('{}') {}", getRelativePath(), fileInfo.getModificationDate());
        // strange enough, directories need to be 0 in all cases
        return isDirectory() ? 0 : fileInfo.getModificationDate();
    }


    /**
     * @see JFSFile#getList()
     */
    @Override
    public final JFSFile[] getList() {
        if (list==null) {
            String[] files = fileProducer.list(getRelativePath());

            if (files!=null) {
                list = new JFSFile[files.length];

                for (int i = 0; i<files.length; i++) {
                    // asFolder parameter doesn't to anything
                    LOG.debug("getList({}) {} {}", getName(), i, files[i]);
                    list[i] = fileProducer.getJfsFile(getRelativePath()+fileProducer.getSeparator()+files[i], false);
                } // for
            } else {
                list = new JFSFile[0];
            }
        }

        LOG.debug("getList() {}", list.length);
        return list;
    }


    /**
     * @see JFSFile#exists()
     */
    @Override
    public final boolean exists() {
        LOG.debug("exists('{}') {}", getRelativePath(), fileInfo.isExists());
        return fileInfo.isExists();
    }


    /**
     * @see JFSFile#mkdir()
     */
    @Override
    public final boolean mkdir() {
        boolean success = fileProducer.createDirectory(getRelativePath());

        if (success) {
            fileInfo.setDirectory(true);
        } // if

        return success;
    } // mkdir()


    /**
     * @see JFSFile#setLastModified(long)
     */
    @Override
    public final boolean setLastModified(long time) {
        boolean success = fileProducer.setLastModified(getRelativePath(), time);
        LOG.debug("setLastModified('{}') setting modification date: {}", getRelativePath(), success);

        if (success) {
            fileInfo.setModificationDate(time);
        } // if

        return success;
    }


    /**
     * @see JFSFile#setReadOnly()
     */
    @Override
    public final boolean setReadOnly() {
        boolean success = true;
        if (JFSConfig.getInstance().isSetCanWrite()) {

            success = fileProducer.setReadOnly(getRelativePath());
            if (success) {
                fileInfo.setCanWrite(false);
            } // if
        } // if
        return success;
    } // setReadOnly()


    /**
     * @see JFSFile#delete()
     */
    @Override
    public final boolean delete() {
        return fileProducer.delete(getRelativePath());
    }


    /**
     * @see JFSFile#getInputStream()
     */
    @Override
    protected InputStream getInputStream() {
        try {
            InputStream stream = fileProducer.getInputStream(getRelativePath());
            return JFSEncryptedStream.createInputStream(stream, getLength(), getCipher(Cipher.DECRYPT_MODE));
        } catch (IOException ioe) {
            LOG.error("getInputStream() I/O Exception "+ioe.getLocalizedMessage());
            return null;
        } // try/catch
    } // getInputStream()


    /**
     * @see JFSFile#getOutputStream()
     */
    @Override
    protected OutputStream getOutputStream() {
        String p = getRelativePath();
        out = null;
        long l = getLength();
        LOG.debug("getOutputStream() opening '{}' with length {}", p, l);
        try {
            int idx = p.lastIndexOf('.');
            long compressionLimit = Long.MAX_VALUE;
            if (idx>0) {
                idx++;
                String extension = p.substring(idx).toLowerCase();
                Map<String, Long> compressedExtensions = ((JFSEncryptedFileProducer) fileProducer).getCompressionLevels();
                if (compressedExtensions.containsKey(extension)) {
                    compressionLimit = compressedExtensions.get(extension);
                    LOG.info("getOutputStream() compression limit {} set for {}", compressionLimit, getName());
                } // if
            } // if
            out = JFSEncryptedStream.createOutputStream(compressionLimit, fileProducer.getOutputStream(p), l,
                    getCipher(Cipher.ENCRYPT_MODE));
        } catch (IOException e) {
            LOG.error("getOutputStream()", e);
        } // try/catch
        return out;
    } // getOutputStream()


    /**
     * @see JFSFile#closeInputStream()
     */
    @Override
    protected void closeInputStream() {
        JFSText t = JFSText.getInstance();
        try {
            if (in!=null) {
                in.close();
                in = null;
            } // if
        } catch (IOException e) {
            JFSLog.getErr().getStream().println(t.get("error.io")+" "+e);
        } // try/catch
    } // closeInputStream()


    /**
     * @see JFSFile#closeOutputStream()
     */
    @Override
    protected void closeOutputStream() {
        JFSText t = JFSText.getInstance();
        try {
            if (out!=null) {
                LOG.debug("closeOutputStream() closing {}", getPath());
                out.flush();
                out.close();
                out = null;
            } // if
        } catch (IOException e) {
            JFSLog.getErr().getStream().println(t.get("error.io")+" "+e);
        } // try/catch
    } // closeOutputStream()


    /**
     * @see JFSFile#preCopyTgt(JFSFile)
     */
    @Override
    protected boolean preCopyTgt(JFSFile srcFile) {
        fileInfo.setSize(srcFile.getLength());
        return true;
    }


    /**
     * @see JFSFile#preCopySrc(JFSFile)
     */
    @Override
    protected boolean preCopySrc(JFSFile tgtFile) {
        return true;
    }


    /**
     * @see JFSFile#postCopyTgt(JFSFile)
     */
    @Override
    protected boolean postCopyTgt(JFSFile srcFile) {
        boolean success = true;

        // Set last modified and read-only only when file is no directory:
        if (!JFSProgress.getInstance().isCanceled()&&!srcFile.isDirectory()) {
            // Just to work on the same file info
            fileInfo = fileProducer.getFileInfo(relativePath);
            fileInfo.setExists(true);
            fileInfo.setSize(srcFile.getLength());
            success = success&&setLastModified(srcFile.getLastModified());
            // set last modified has to implicitly
            if (!success) {
                fileProducer.flush(fileInfo);
            } // if
            if (!srcFile.canWrite()) {
                success = success&&setReadOnly();
            } // if
        } // if

        return success;
    }


    /**
     * @see JFSFile#postCopySrc(JFSFile)
     */
    @Override
    protected boolean postCopySrc(JFSFile tgtFile) {
        LOG.info("postCopySrc() free memory {}", Runtime.getRuntime().freeMemory());
        return true;
    } // postCopySrc()


    /**
     * @see JFSFile#flush()
     */
    @Override
    public boolean flush() {
        return true;
    }


    @Override
    protected void finalize() throws Throwable {
        LOG.info("finalize() free memory {}", Runtime.getRuntime().freeMemory());
        super.finalize();
    } // finalize()

} // JFSEncryptedFile()
