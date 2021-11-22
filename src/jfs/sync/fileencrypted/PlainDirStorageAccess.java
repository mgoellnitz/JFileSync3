/*
 * Copyright (C) 2010-2021 Martin Goellnitz
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
package jfs.sync.fileencrypted;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jfs.conf.JFSConfig;
import jfs.sync.encryption.FileInfo;
import jfs.sync.encryption.StorageAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * With this module only the files and not their filesnames get encrypted.
 *
 * It is excluded from the builing path and only serves as a documentation reference.
 */
public class PlainDirStorageAccess implements StorageAccess {

    private static Logger log = LoggerFactory.getLogger(PlainDirStorageAccess.class);

    /**
     * padding for passwords
     */
    private static final String SALT = "aqiowuecqouceienq";


    @Override
    public String getCipherSpec() {
        return JFSConfig.getInstance().getEncryptionCipher();
    } // getCipherSpec()


    @Override
    public byte[] getFileCredentials(String password) {
        byte[] credentials = (password+SALT).substring(0, 16).getBytes();
        return credentials;
    } // getFileCredentials()


    @Override
    public FileInfo getFileInfo(String rootPath, String relativePath) {
        FileInfo result = new FileInfo();
        File file = getFile(rootPath, relativePath);
        result.setName(file.getName());
        result.setPath(file.getPath());
        result.setDirectory(file.isDirectory());
        result.setExists(file.exists());
        result.setCanRead(true);
        result.setCanWrite(true);
        if (log.isDebugEnabled()) {
            log.debug("PlainDirStorageAccess.getFileInfo() "+result.getPath()+" e["+result.isExists()+"] d["+result.isDirectory()
                    +"]");
        } // if
        if (result.isExists()) {
            result.setCanRead(file.canRead());
            result.setCanWrite(file.canWrite());
            if (!result.isDirectory()) {
                result.setModificationDate(file.lastModified());
                result.setSize(-1);
            } else {
                result.setSize(0);
            } // if
        } else {
            if (log.isDebugEnabled()) {
                log.debug("PlainDirStorageAccess.getFileInfo() could not detect file for "+result.getPath());
            } // if
        } // if
        return result;
    } // getFileInfo()


    private File getFile(String rootPath, String relativePath) {
        return new File(rootPath+relativePath);
    }


    @Override
    public String[] list(String rootPath, String relativePath) {
        return getFile(rootPath, relativePath).list();
    }


    @Override
    public boolean createDirectory(String rootPath, String relativePath) {
        return getFile(rootPath, relativePath).mkdir();
    }


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modificationDate) {
        return getFile(rootPath, relativePath).setLastModified(modificationDate);
    }


    @Override
    public boolean setWritable(String rootPath, String relativePath, boolean writable) {
        return getFile(rootPath, relativePath).setWritable(writable);
    }


    @Override
    public boolean delete(String rootPath, String relativePath) {
        return getFile(rootPath, relativePath).delete();
    }


    @Override
    public InputStream getInputStream(String rootPath, String relativePath) throws IOException {
        File file = getFile(rootPath, relativePath);
        if (log.isDebugEnabled()) {
            log.debug("PlainDirStorageAccess.getInputStream() getting input stream for "+file.getPath());
        } // if
        return new FileInputStream(file);
    }


    @Override
    public OutputStream getOutputStream(String rootPath, String relativePath) throws IOException {
        return new FileOutputStream(getFile(rootPath, relativePath));
    }


    @Override
    public String getSeparator() {
        return File.separator;
    }


    @Override
    public void flush(String rootPath, FileInfo info) {
        // Nothing to do in this implementation
    }

} // PlainDirStorageAccess
