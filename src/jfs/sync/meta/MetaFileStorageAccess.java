/*
 * Copyright (C) 2010-2025 Martin Goellnitz
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
package jfs.sync.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.crypto.Cipher;
import jfs.sync.encryption.AbstractMetaStorageAccess;
import jfs.sync.encryption.ExtendedFileInfo;
import jfs.sync.encryption.JFSEncryptedStream;
import jfs.sync.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetaFileStorageAccess extends AbstractMetaStorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(MetaFileStorageAccess.class);

    private static long outputStreams;

    private static long inputStreams;


    public MetaFileStorageAccess(String cipher, boolean sixBits) {
        super(cipher, sixBits);
    } // MetaFileStorageAccess()


    /**
     * create non existent files
     *
     * @param file
     * @param pathAndName
     * @return
     */
    private ExtendedFileInfo createFileInfo(File file, String[] pathAndName) {
        ExtendedFileInfo result = new ExtendedFileInfo();

        result.setCanRead(true);
        result.setCanWrite(true);
        result.setCanExecute(false);
        result.setDirectory(file.isDirectory());
        result.setExists(file.exists());
        result.setModificationDate(file.lastModified());
        result.setName(pathAndName[1]);
        result.setPath(pathAndName[0]);
        result.setSize(result.isDirectory() ? 0 : file.length());

        if (LOG.isDebugEnabled()) {
            LOG.debug("createFileInfo("+pathAndName[0]+"/"+pathAndName[1]+") "+result);
        } // if

        return result;
    } // createFileInfo()


    @Override
    public ExtendedFileInfo getFileInfo(String rootPath, String relativePath) {
        String[] pathAndName = getPathAndName(relativePath);
        ExtendedFileInfo result = getParentListing(rootPath, pathAndName).get(pathAndName[1]);
        if (result==null) {
            result = createFileInfo(getFile(rootPath, relativePath), pathAndName);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFileInfo() {} / {}.", result.getPath(), result.getName());
            } // if
        } // if
        return result;
    } // getFileInfo()


    @Override
    public boolean createDirectory(String rootPath, String relativePath) {
        File file = getFile(rootPath, relativePath);
        file.mkdir();
        boolean success = file.exists();
        if (success) {
            String[] pathAndName = getPathAndName(relativePath);
            Map<String, ExtendedFileInfo> listing = getParentListing(rootPath, pathAndName);
            LOG.debug("createDirectory() {}", relativePath);
            LOG.debug("createDirectory() listing={}", listing);
            ExtendedFileInfo info = createFileInfo(file, pathAndName);
            listing.put(pathAndName[1], info);
            LOG.debug("createDirectory() listing={}", listing);
            LOG.info("createDirectory() flushing {}/{}", pathAndName[0], pathAndName[1]);
            flushMetaData(rootPath, pathAndName, listing);
        } // if
        LOG.debug("createDirectory() {}", success);
        return success;
    } // createDirectory()


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modificationDate) {
        boolean success = getFile(rootPath, relativePath).setLastModified(modificationDate);
        if (success) {
            String[] pathAndName = getPathAndName(relativePath);
            Map<String, ExtendedFileInfo> listing = getParentListing(rootPath, pathAndName);
            ExtendedFileInfo info = listing.get(pathAndName[1]);
            LOG.info("setLastModified() flushing {}/{}", pathAndName[0], pathAndName[1]);
            info.setModificationDate(modificationDate);
            flushMetaData(rootPath, pathAndName, listing);
        } // if
        return success;
    } // setLastModified()


    @Override
    public boolean setWritable(String rootPath, String relativePath, boolean writable) {
        LOG.info("setWritable() set writable {}: {}", relativePath, writable);
        boolean result = true;
        File file = getFile(rootPath, relativePath);
        if (file.exists()) {
            result = file.setWritable(writable);
        }
        return result;
    } // setWritable()


    @Override
    public boolean delete(String rootPath, String relativePath) {
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, ExtendedFileInfo> listing = getParentListing(rootPath, pathAndName);
        LOG.debug("delete() {}", relativePath);
        LOG.debug("delete() listing={}", listing);
        // remove named item
        File file = getFile(rootPath, relativePath);
        if (listing.containsKey(pathAndName[1])) {
            listing.remove(pathAndName[1]);
            LOG.info("delete() flushing {}/{}", pathAndName[0], pathAndName[1]);
            flushMetaData(rootPath, pathAndName, listing);
            LOG.info("delete() listing={}", listing);
            if (file.isDirectory()) {
                String metaDataPath = getMetaDataPath(relativePath);
                File metaDataFile = getFile(rootPath, metaDataPath);
                if (metaDataFile.exists()) {
                    metaDataFile.delete();
                } // if
            } // if
            file.delete();
        } // if
        return !file.exists();
    } // delete()


    @Override
    public InputStream getInputStream(String rootPath, String relativePath) throws IOException {
        File file = getFile(rootPath, relativePath);
        LOG.debug("getInputStream() getting input stream for {}", file.getPath());
        inputStreams++;
        LOG.debug("getInputStream({}) inputStreams={}", relativePath, inputStreams);
        return new FileInputStream(file);
    } // getInputStream()


    @Override
    protected OutputStream getOutputStream(String rootPath, String relativePath, boolean forPayload) throws IOException {
        File file = getFile(rootPath, relativePath);
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, ExtendedFileInfo> listing = getParentListing(rootPath, pathAndName);
        ExtendedFileInfo info = null;
        if (forPayload&&(listing.get(pathAndName[1])==null||!file.exists())) {
            info = createFileInfo(file, pathAndName);
            listing.put(info.getName(), info);
            LOG.info("getOutputStream() flushing {}/{}", pathAndName[0], pathAndName[1]);
            flushMetaData(rootPath, pathAndName, listing);
            LOG.debug("getOutputStream() getting output stream for {} {}", file.getPath(), info);
        } // if
        LOG.debug("getOutputStream() getting output stream for {}", file.getPath());
        outputStreams++;
        if (LOG.isInfoEnabled()) {
            LOG.info("getOutputStream({}) outputStreams={}", relativePath, outputStreams);
        } // if
        OutputStream result = new FileOutputStream(file);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOutputStream() have output stream for "+file.getPath()+" "+info+" "+result);
        } // if
        return result;
    } // getOutputStream()


    /**
     *
     * Extract one file from encrypted repository.
     *
     * TODO: list directories
     *
     */
    public static void main(String[] args) throws Exception {
        final String password = args[0];
        MetaFileStorageAccess storage = new MetaFileStorageAccess("Twofish", false) {

            protected String getPassword(String relativePath) {
                String result = "";

                String pwd = password;

                int i = 0;
                int j = relativePath.length()-1;
                while ((i<pwd.length())||(j>=0)) {
                    if (i<pwd.length()) {
                        result += pwd.charAt(i++);
                    } // if
                    if (j>=0) {
                        result += relativePath.charAt(j--);
                    } // if
                } // while

                return result;
            } // getPassword()

        };
        String encryptedPath = args[2];
        String[] elements = encryptedPath.split("\\\\");
        String path = "";
        for (int i = 0; i<elements.length; i++) {
            String encryptedName = elements[i];
            String plain = storage.getDecryptedFileName(path, encryptedName);
            path += storage.getSeparator();
            path += plain;
            System.out.println(path);
        } // for

        String cipherSpec = storage.getCipherSpec();
        byte[] credentials = storage.getFileCredentials(path);
        Cipher cipher = SecurityUtils.getCipher(cipherSpec, true, credentials);

        InputStream is = storage.getInputStream(args[1], path);
        is = JFSEncryptedStream.createInputStream(is, JFSEncryptedStream.DONT_CHECK_LENGTH, cipher);
        int b = 0;
        while (b>=0) {
            b = is.read();
        } // while
        is.close();
    } // main()

} // MetaFileStorageAccess
