/*
 * JFileSync
 * Copyright (C) 2002-2015, Jens Heidrich
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
package jfs.sync.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSUserAuthentication;
import jfs.sync.JFSUserAuthenticationInterface;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents an FTP file.
 *
 * @author Jens Heidrich
 */
public class JFSVFSFile extends JFSFile {

    private static final Logger LOG = LoggerFactory.getLogger(JFSVFSFileProducer.class);

    /** The abstract file. */
    private FileObject file = null;

    private long lastModified = -1;

    private long length = -1;

    private Boolean directory = null;

    private String name = null;

    private String path = null;


    /**
     * Creates a new external root file and reads the structure from server.
     *
     * @param fileProducer
     * The assigned file producer.
     */
    public JFSVFSFile(JFSVFSFileProducer fileProducer) {
        super(fileProducer, "");
        try {
            FileSystemOptions opts = new FileSystemOptions();

            // Avoid using known hosts file if SFTP is used:
            if (fileProducer.getScheme().equals("sftp")) {
                SftpFileSystemConfigBuilder.getInstance()
                        .setStrictHostKeyChecking(opts, "no");
            }

            // Get user name and password, if not specified:
            try {
                URI uriObject = new URI(fileProducer.getUri());
                String userInfo = uriObject.getUserInfo();
                if (userInfo==null||!userInfo.contains(":")) {
                    JFSUserAuthentication userAuth = JFSUserAuthentication.getInstance();
                    userAuth.setResource(fileProducer.getUri());
                    JFSUserAuthenticationInterface userInterface = userAuth.getUserInterface();
                    StaticUserAuthenticator auth = new StaticUserAuthenticator(
                            null, userInterface.getUserName(),
                            userInterface.getPassword());
                    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
                }
            } catch (URISyntaxException e) {
                LOG.error("()", e);
            }

            file = VFS.getManager().resolveFile(fileProducer.getUri(), opts);
        } catch (FileSystemException e) {
            LOG.error("()", e);
        }
    }


    /**
     * Creates a new external file for a certain path using a specific file
     * producer.
     *
     * @param fileProducer
     * The assigned file producer.
     * @param path
     * The path to create the external file for.
     */
    public JFSVFSFile(JFSVFSFileProducer fileProducer, String path) {
        super(fileProducer, path);
        try {
            file = VFS.getManager().resolveFile(fileProducer.getBaseFile(), fileProducer.getBaseFile().getURL()+path);
        } catch (FileSystemException e) {
            LOG.error("()", e);
        }
    }


    /**
     * Creates an external file based on a previously read-in structure.
     *
     * @param fileProducer
     * The assigned file producer.
     * @param file
     * The previously read-in file information object.
     * @param relativePath
     * The relative path of the file.
     */
    private JFSVFSFile(JFSFileProducer fileProducer, FileObject file,
            String relativePath) {
        super(fileProducer, relativePath);
        this.file = file;
    }


    /**
     * @return Returns the file object.
     */
    public FileObject getFileObject() {
        return file;
    }


    /**
     * @see JFSFile#getInputStream()
     */
    @Override
    protected InputStream getInputStream() {
        if (file==null) {
            return null;
        }
        try {
            return file.getContent().getInputStream();
        } catch (FileSystemException e) {
            LOG.error("getInputStream()", e);
            return null;
        }

    }


    /**
     * @see JFSFile#getOutputStream()
     */
    @Override
    protected OutputStream getOutputStream() {
        if (file==null) {
            return null;
        }
        try {
            return file.getContent().getOutputStream();
        } catch (FileSystemException e) {
            LOG.error("getOutputStream()", e);
            return null;
        }
    }


    /**
     * @see JFSFile#closeInputStream()
     */
    @Override
    protected void closeInputStream() {
        if (file!=null) {
            try {
                file.getContent().close();
            } catch (IOException e) {
                LOG.error("closeInputStream()", e);
            }
        }
    }


    /**
     * @see JFSFile#closeOutputStream()
     */
    @Override
    protected void closeOutputStream() {
        if (file!=null) {
            try {
                file.getContent().close();
            } catch (IOException e) {
                LOG.error("closeOutputStream()", e);
            }
        }
    }


    /**
     * @see JFSFile#removeWriteLock()
     */
    public boolean removeWriteLock() {
        if (file==null) {
            return false;
        }
        try {
            return file.setWritable(true, false);
        } catch (FileSystemException e) {
            LOG.error("removeWriteLock()", e);
            return false;
        }
    }


    /**
     * @see JFSFile#delete()
     */
    public boolean delete() {
        if (file==null) {
            return false;
        }
        try {
            return file.delete();
        } catch (FileSystemException e) {
            LOG.error("delete()", e);
            return false;
        }
    }


    /**
     * @see JFSFile#exists()
     */
    @Override
    public boolean exists() {
        if (file==null) {
            return false;
        }
        try {
            return file.exists();
        } catch (FileSystemException e) {
            LOG.error("exists()", e);
            return false;
        }
    }


    /**
     * @see JFSFile#getLastModified()
     */
    @Override
    public long getLastModified() {
        if (lastModified==-1) {
            if (file==null||isDirectory()) {
                lastModified = 0;
            } else {
                try {
                    lastModified = file.getContent().getLastModifiedTime();
                } catch (FileSystemException e) {
                    lastModified = 0;
                    LOG.error("getLastModified()", e);
                }
            }
        }
        return lastModified;
    }


    /**
     * @see JFSFile#getLength()
     */
    @Override
    public long getLength() {
        if (length==-1) {
            if (file==null||isDirectory()) {
                length = 0;
            } else {
                try {
                    length = file.getContent().getSize();
                } catch (FileSystemException e) {
                    length = 0;
                    LOG.error("getLength()", e);
                }
            }
        }
        return length;
    }


    /**
     * @see JFSFile#getName()
     */
    @Override
    public String getName() {
        if (name==null) {
            name = (file==null) ? "" : file.getName().getBaseName();
        }
        return name;
    }


    /**
     * @see JFSFile#getPath()
     */
    public String getPath() {
        if (path==null) {
            path = (file==null) ? "" : file.getName().getPath();
        }
        return path;
    }


    /**
     * @see JFSFile#isDirectory()
     */
    public boolean isDirectory() {
        if (file==null) {
            return false;
        }
        if (directory==null) {
            try {
                directory = file.getType().equals(FileType.FOLDER);
            } catch (FileSystemException e) {
                LOG.error("isDirectory()", e);
                directory = false;
            }
        }
        return directory;
    }


    /**
     * @see JFSFile#mkdir()
     */
    public boolean mkdir() {
        if (file==null) {
            return false;
        }
        try {
            file.createFolder();
            file.refresh();
            return true;
        } catch (FileSystemException e) {
            LOG.error("mkdir()", e);
            return false;
        }
    }


    /**
     * @see JFSFile#setLastModified(long)
     */
    public boolean setLastModified(long time) {
        if (file==null) {
            return false;
        }
        try {
            file.getContent().setLastModifiedTime(time);
            lastModified = time;
            return true;
        } catch (FileSystemException e) {
            LOG.error("setLastModified()", e);
            return false;
        }
    }


    @Override
    protected boolean preCopyTgt(JFSFile srcFile) {
        try {
            file.getContent().close();
            return true;
        } catch (FileSystemException e) {
            LOG.error("preCopyTgt()", e);
            return false;
        }
    }


    @Override
    protected boolean preCopySrc(JFSFile tgtFile) {
        return true;
    }


    @Override
    protected boolean postCopyTgt(JFSFile srcFile) {
        try {
            if (!srcFile.isDirectory()) {
                file.getContent().setLastModifiedTime(srcFile.getLastModified());
            }
            file.refresh();
            return true;
        } catch (FileSystemException e) {
            LOG.error("preCopyTgt()", e);
            return false;
        }
    }


    @Override
    protected boolean postCopySrc(JFSFile tgtFile) {
        return true;
    }


    /**
     * @see JFSFile#canRead()
     */
    @Override
    public boolean canRead() {
        if (file==null) {
            return false;
        }
        try {
            return file.isReadable();
        } catch (FileSystemException e) {
            LOG.error("canRead()", e);
            return false;
        }
    }


    /**
     * @see JFSFile#canWrite()
     */
    @Override
    public boolean canWrite() {
        if (file==null) {
            return false;
        }
        try {
            return file.isWriteable();
        } catch (FileSystemException e) {
            LOG.error("canWrite()", e);
            return false;
        }
    }


    @Override
    public boolean canExecute() {
        return false;
    }


    @Override
    public boolean setReadOnly() {
        try {
            file.setWritable(false, true);
            file.setWritable(false, false);
        } catch (FileSystemException e) {
            LOG.error("setReadOnly()", e);
            return false;
        }
        return true;
    }


    @Override
    public boolean setExecutable() {
        try {
            file.setExecutable(false, true);
            file.setExecutable(false, false);
        } catch (FileSystemException e) {
            LOG.error("setExecutable()", e);
            return false;
        }
        return true;
    }


    private void fillFileList(List<JFSFile> list) {
        if (isDirectory()&&list!=null&&file!=null) {
            try {
                FileObject[] files = file.getChildren();
                if (files!=null) {
                    LOG.debug("fillFileList({}):", file.getName());
                    String dummyName = file.getName()+"/"+file.getName().getBaseName();
                    LOG.debug("fillFileList() dummy: {}", dummyName);
                    for (FileObject fo : files) {
                        if (dummyName.equals(""+fo.getName())) {
                            LOG.info("fillFileList() leaving out {} in {}", fo, file);
                        } else {
                            LOG.debug("fillFileList({})   {}", getRelativePath(), fo.getName());
                            String p = getRelativePath()+"/"+fo.getName().getBaseName();
                            list.add(new JFSVFSFile(fileProducer, fo, p));
                        }
                    }
                    LOG.debug("fillFileList({}).", file.getName());
                }
            } catch (FileSystemException e) {
                LOG.error("fillFileList()", e);
            }
        }
    }


    @Override
    public JFSFile[] getList() {
        JFSFile[] template = new JFSFile[0];
        List<JFSFile> fileList = new ArrayList<>();
        fillFileList(fileList);
        return fileList.toArray(template);
    }

}
