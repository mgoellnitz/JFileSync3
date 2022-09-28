/*
 * Copyright (C) 2010-2022 Martin Goellnitz
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
package jfs.sync.encfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jfs.conf.JFSConfig;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.base.AbstractJFSFileProducerFactory;
import jfs.sync.encryption.ExtendedFileInfo;
import org.mrpdaemon.sec.encfs.EncFSFile;
import org.mrpdaemon.sec.encfs.EncFSVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a file that is stored as EncFS file locally.
 *
 * @author Martin Goellnitz
 *
 */
public class JFSEncfsFile extends JFSFile {

    private static final Logger LOG = LoggerFactory.getLogger(JFSEncfsFile.class);

    /**
     * The retrieved file information object from the server.
     */
    private final ExtendedFileInfo info;

    private EncFSFile file = null;

    /**
     * The list of included files.
     */
    private JFSFile[] list = null;

    private EncFSVolume access;

    private OutputStream output = null;

    InputStream input = null;


    /**
     * Creates a new external file for a certain path using a specific file producer.
     *
     * @param access       The server access object to use.
     * @param fileProducer The assigned file producer.
     * @param path         The path to create the external file for.
     */
    public JFSEncfsFile(EncFSVolume access, JFSFileProducer fileProducer, String path, boolean isDirectory) {
        super(fileProducer, path);
        path = path.replace(File.separatorChar, '/');

        this.access = access;

        info = new ExtendedFileInfo();
        try {
            String[] pathAndName = AbstractJFSFileProducerFactory.getPathAndName(path, "/");
            info.setPath(pathAndName[0]);
            info.setName(pathAndName[1]);
            info.setDirectory(isDirectory);
            try {
                file = access.getFile(path.length()==0 ? "/" : path);
                info.setCanRead(file.isReadable());
                info.setCanWrite(file.isWritable());
                info.setCanExecute(file.isExecutable());
                info.setPath(file.getParentPath());
                info.setName(path.length()==0 ? "" : file.getName());
                info.setDirectory(file.isDirectory());
                info.setExists(file.isReadable());
                info.setModificationDate(file.getLastModified());
                info.setSize(file.getLength());
            } catch (IllegalArgumentException iae) {
                LOG.warn("()", iae);
                info.setExists(false);
            } // try/catch

        } catch (Exception e) {
            LOG.error("()", e);
        } // try/catch

        if (LOG.isInfoEnabled()) {
            LOG.info("() "+(info.isDirectory() ? "d" : "-")+(info.isExists() ? "e" : "-")+" | "+info.getPath()+"/"+info.getName());
        } // if
    } // JFSEncfsFile()


    /**
     * Creates a new external root file and reads the structure from server.
     *
     * @param access       The server access object to use.
     * @param fileProducer The assigned file producer.
     */
    public JFSEncfsFile(EncFSVolume access, JFSFileProducer fileProducer) {
        this(access, fileProducer, "", true);
    } // JFSWebDavFile()


    /**
     * @see JFSFile#canRead()
     */
    @Override
    public boolean canRead() {
        return info.isCanRead();
    }


    /**
     * @see JFSFile#canWrite()
     */
    @Override
    public boolean canWrite() {
        return info.isCanWrite();
    }


    /**
     * @see JFSFile#canExecute()
     */
    @Override
    public boolean canExecute() {
        return info.isCanExecute();
    }


    /**
     * @see JFSFile#getInputStream()
     */
    @Override
    protected InputStream getInputStream() {
        LOG.debug("getInputStream() file {}", file.getPath());
        try {
            input = file.openInputStream();
        } catch (Exception e) {
            LOG.error("getInputStream()", e);
        } // try/catch
        return input;
    } // getInputStream()


    /**
     * @see JFSFile#getOutputStream()
     */
    @Override
    protected OutputStream getOutputStream() {
        LOG.debug("getOutputStream()");
        try {
            if (!info.isExists()) {
                file = access.createFile(getPath());
                info.setCanRead(file.isReadable());
                info.setCanWrite(file.isWritable());
                info.setExists(true);
                info.setPath(file.getParentPath());
                info.setName(file.getName());
                info.setDirectory(file.isDirectory());
                info.setSize(file.isDirectory() ? 0 : file.getLength());
                info.setModificationDate(file.getLastModified());
            } // if
            output = file.openOutputStream(-1);
        } catch (Exception e) {
            LOG.error("getOutputStream()", e);
        } // try/catch
        return output;
    } // getOutputStream()


    /**
     * @see JFSFile#closeInputStream()
     */
    @Override
    protected void closeInputStream() {
        if (input!=null) {
            try {
                input.close();
            } catch (IOException e) {
                LOG.error("closeInputStream()", e);
            } // try/catch
            input = null;
        } // if
    } // closeInputStream()


    /**
     * @see JFSFile#closeOutputStream()
     */
    @Override
    protected void closeOutputStream() {
        LOG.debug("getOutputStream()");
        if (output!=null) {
            try {
                output.close();
            } catch (IOException e) {
                LOG.error("closeOutputStream()", e);
            } // try/catch
            output = null;
        } // if
    } // closeOutputStream()


    /**
     * @see JFSFile#removeWriteLock()
     */
    @Override
    public boolean removeWriteLock() {
        LOG.debug("removeWriteLock() {}", file.getPath());
        boolean result = file.isWritable();
        return result;
    } // removeWriteLock()


    /**
     * @see JFSFile#delete()
     */
    @Override
    public boolean delete() {
        LOG.debug("delete() deleting {}", file.getPath());
        boolean result = false;
        try {
            result = file.delete();
        } catch (IOException e) {
            LOG.error("delete()", e);
        } // try/catch
        return result;
    } // delete()


    /**
     * @see JFSFile#exists()
     */
    @Override
    public boolean exists() {
        return info.isExists();
    }


    /**
     * @see JFSFile#getLastModified()
     */
    @Override
    public long getLastModified() {
        return info.getModificationDate();
    }


    /**
     * @see JFSFile#getLength()
     */
    @Override
    public long getLength() {
        return info.getSize();
    }


    /**
     * @see JFSFile#getList()
     */
    @Override
    public JFSFile[] getList() {
        LOG.debug("getList() listing {}", file.getPath());
        if (list==null) {
            list = new JFSEncfsFile[0];
            if (isDirectory()) {
                try {
                    EncFSFile[] listing = file.listFiles();
                    list = new JFSEncfsFile[listing.length];
                    int i = 0;
                    for (EncFSFile f : listing) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getList("+i+") listing "+f.getPath());
                        } // if
                        list[i] = new JFSEncfsFile(access, fileProducer, f.getPath(), f.isDirectory());
                        i++;
                    } // for
                } catch (Exception e) {
                    LOG.error("getList()", e);
                } // try/catch
            } // if
        } // getList()

        return list;
    } // getList()


    /**
     * @see JFSFile#getName()
     */
    @Override
    public String getName() {
        return info.getName();
    }


    /**
     * @see JFSFile#getPath()
     */
    @Override
    public String getPath() {
        return (info.getPath().length()>1 ? info.getPath() : "")+"/"+info.getName();
    }


    /**
     * @see JFSFile#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return info.isDirectory();
    }


    /**
     * @see JFSFile#mkdir()
     */
    @Override
    public boolean mkdir() {
        boolean result = false;
        try {
            String path = info.getPath()+"/"+info.getName();
            LOG.debug("mkdir() creating {}", path);
            result = access.makeDir(path);
            if (result) {
                file = access.getFile(path);
            } // if
        } catch (Exception e) {
            LOG.error("mkdir()", e);
        } // try/catch
        return result;
    } // mkdir()


    /**
     * @see JFSFile#setLastModified(long)
     */
    @Override
    public boolean setLastModified(long time) {
        LOG.debug("setLastModified() {}/{}", info.getPath(), info.getName());
        boolean success = false;

        info.setModificationDate(time);

        if (LOG.isDebugEnabled()) {
            LOG.debug("setLastModified() "+file.getParentPath()+":"+file.getName()+" - "+file.getPath());
        } // if
        // TODO: Maybe fix this somewhere else...
        String encryptedPath = file.getEncryptedPath();
        int idx = encryptedPath.startsWith("//") ? 1 : 0;
        String encPath = getFileProducer().getRootPath()+encryptedPath.substring(idx);
        File encFile = new File(encPath);
        LOG.debug("setLastModified({}) {}", encPath, encFile.exists());
        if (encFile.exists()) {
            encFile.setLastModified(info.getModificationDate());
        } // if

        return success;
    } // setLastModified()


    /**
     * @see JFSFile#setReadOnly()
     */
    @Override
    public boolean setReadOnly() {
        if (!JFSConfig.getInstance().isSetCanWrite()) {
            return true;
        }

        info.setCanWrite(false);

        return true;
    }


    /**
     * @see JFSFile#setExecutable()
     */
    @Override
    public boolean setExecutable() {
        info.setCanExecute(true);
        return true;
    } // setExecutable()


    /**
     * @see JFSFile#preCopyTgt(JFSFile)
     */
    @Override
    protected boolean preCopyTgt(JFSFile srcFile) {
        LOG.debug("preCopyTgt() {}/{}", info.getPath(), info.getName());
        info.setModificationDate(srcFile.getLastModified());
        // Set last modified and read-only only when file is no directory:
        if (!srcFile.isDirectory()) {
            info.setSize(srcFile.getLength());
            if (!srcFile.canWrite()) {
                info.setCanWrite(false);
            }
            if (!srcFile.canExecute()) {
                info.setCanExecute(true);
            }
        } // if

        return true;
    } // preCopyTgt()


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
        LOG.debug("postCopyTgt() {}", srcFile.getPath());
        // Update information object after copy. This method is only
        // called if all operations were performed successfully:
        info.setDirectory(srcFile.isDirectory());
        info.setExists(srcFile.exists());
        info.setSize(srcFile.getLength());
        setLastModified(srcFile.getLastModified());

        return true;
    } // postCopyTgt()


    /**
     * @see JFSFile#postCopySrc(JFSFile)
     */
    @Override
    protected boolean postCopySrc(JFSFile tgtFile) {
        return true;
    }

} // JFSEncfsFile
