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
package jfs.sync.encrypted;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import jfs.sync.encryption.AbstractEncryptedStorageAccess;
import jfs.sync.encryption.ExtendedFileInfo;
import jfs.sync.encryption.StorageAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a storage layer access which encryptes the filenames, compresses and encrytes contents and is aware of the
 * meta solution in the other package. To be able to deal with the original file length, every file has a header
 * containing information about compression and original length.
 *
 * It has been refactored from an older version to meet higher security standards concerning known plain text attacks
 * and re-use of encryption keys
 */
public class EncryptedFileStorageAccess extends AbstractEncryptedStorageAccess implements StorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptedFileStorageAccess.class);

    private final String cipherspec;


    public EncryptedFileStorageAccess(String cipher, boolean shortenPaths) {
        super(shortenPaths);
        cipherspec = cipher;
    } // EncryptedFileStorageAccess()


    @Override
    public String getSeparator() {
        return File.separator;
    }


    @Override
    public String getCipherSpec() {
        return cipherspec;
    } // getCipherSpec()


    protected File getFile(String rootPath, String relativePath) {
        String path = getFileName(relativePath);
        return new File(rootPath+path);
    } // getFile()


    @Override
    public String[] list(String rootPath, String relativePath) {
        String[] items = getFile(rootPath, relativePath).list();

        // decrypt
        String[] result = new String[items.length];
        int i = 0;
        for (String item : items) {
            String decryptedItem = getDecryptedFileName(relativePath, item);
            result[i++] = decryptedItem;
            LOG.info("list() {} -> {}", item, decryptedItem);
        } // for

        // sort out meta data
        Collection<String> itemCollection = new HashSet<>();
        for (String item : result) {
            if (!getMetaDataFileName(relativePath).equals(item)) {
                itemCollection.add(item);
            } // if
        } // for

        // repackage as array
        result = new String[itemCollection.size()];
        i = 0;
        for (String item : itemCollection) {
            result[i++] = item;
        } // for
        return result;
    } // list()


    @Override
    public ExtendedFileInfo getFileInfo(String rootPath, String relativePath) {
        ExtendedFileInfo result = new ExtendedFileInfo();
        String name = getLastPathElement(relativePath, relativePath);
        result.setName(name);
        result.setPath(rootPath+relativePath);
        File file = getFile(rootPath, relativePath);
        result.setDirectory(file.isDirectory());
        result.setExists(file.exists());
        result.setCanRead(true);
        result.setCanWrite(true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFileInfo() "+result.getPath()+" e["+result.isExists()+"] d["+result.isDirectory()+"]");
        } // if
        if (result.isExists()) {
            result.setCanRead(file.canRead());
            result.setCanWrite(file.canWrite());
            result.setCanExecute(file.canExecute());
            if (!result.isDirectory()) {
                result.setModificationDate(file.lastModified());
                result.setSize(-1);
            } else {
                result.setSize(0);
            } // if
        } else {
            LOG.debug("getFileInfo() could not detect file for {}", result.getPath());
        } // if
        return result;
    } // getFileInfo()


    @Override
    public boolean createDirectory(String rootPath, String relativePath) {
        LOG.debug("createDirectory() {}", relativePath);
        return getFile(rootPath, relativePath).mkdir();
    }


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modificationDate) {
        return getFile(rootPath, relativePath).setLastModified(modificationDate);
    }


    @Override
    public boolean setWritable(String rootPath, String relativePath, boolean writable) {
        LOG.info("setWritable() set writable {}: {}", relativePath, writable);
        boolean result = true;
        File file = getFile(rootPath, relativePath);
        if (file.exists()) {
            result = file.setWritable(writable);
        }
        return result;
    }


    @Override
    public boolean setExecutable(String rootPath, String relativePath, boolean executable) {
        return getFile(rootPath, relativePath).setExecutable(executable);
    }


    protected String filePermissionsString(File file) {
        return (file.canRead() ? "r" : "-")+(file.canWrite() ? "w" : "-")+(file.canExecute() ? "x" : "-");
    } // filePermissionsString()


    @Override
    public boolean delete(String rootPath, String relativePath) {
        File file = getFile(rootPath, relativePath);
        LOG.warn("delete({}) {}", relativePath, file.getAbsolutePath());
        LOG.warn("delete({}) file.exists(): {} {}", relativePath, file.exists(), filePermissionsString(file));
        if (file.isDirectory()) {
            String metaDataPath = getMetaDataPath(relativePath);
            File metaDataFile = getFile(rootPath, metaDataPath);
            if (metaDataFile.exists()) {
                metaDataFile.delete();
            } // if
        } // if
        file.delete();
        LOG.warn("delete({}) file.exists(): {}", relativePath, file.exists());
        return !file.exists();
    } // delete()


    @Override
    public InputStream getInputStream(String rootPath, String relativePath) throws IOException {
        File file = getFile(rootPath, relativePath);
        LOG.info("getInputStream() getting input stream for {}", file.getPath());
        return new FileInputStream(file);
    }


    @Override
    public OutputStream getOutputStream(String rootPath, String relativePath) throws IOException {
        File file = getFile(rootPath, relativePath);
        LOG.debug("getOutputStream() getting output stream for {}", file.getPath());
        return new FileOutputStream(file);
    }


    @Override
    public void flush(String rootPath, ExtendedFileInfo info) {
        // Nothing to do in this implementation
    } // flush()


    /**
     * Test
     */
    public static void main(String[] args) throws Exception {
        EncryptedFileStorageAccess d = new EncryptedFileStorageAccess("AES", true);

        String relativePath = "a/path/for/me";

        String enc = d.getEncryptedFileName(relativePath, "src");
        System.out.println("enc= "+enc);
        String plain = d.getDecryptedFileName(relativePath, enc);
        System.out.println("plain= "+plain);
        d.getPassword(relativePath);
    } // main()

} // EncryptedFileStorageAccess
