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
package jfs.sync.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jfs.sync.JFSFileProducer;


/**
 * Abstract file producer base class for file producers dealing with story access implementations.
 */
public abstract class AbstractFileProducer extends JFSFileProducer {

    protected StorageAccess storageAccess;


    public AbstractFileProducer(StorageAccess storageAccess, String scheme, String uri) {
        super(scheme, uri);
        this.storageAccess = storageAccess;
    }


    public ExtendedFileInfo getFileInfo(String relativePath) {
        return storageAccess.getFileInfo(getRootPath(), relativePath);
    }


    public String[] list(String relativePath) {
        return storageAccess.list(getRootPath(), relativePath);
    }


    public boolean createDirectory(String relativePath) {
        return storageAccess.createDirectory(getRootPath(), relativePath);
    }


    public boolean setLastModified(String relativePath, long modificationTime) {
        return storageAccess.setLastModified(getRootPath(), relativePath, modificationTime);
    }


    public boolean setWritable(String relativePath, boolean writable) {
        return storageAccess.setWritable(getRootPath(), relativePath, writable);
    }


    public boolean setExecutable(String relativePath, boolean executable) {
        return storageAccess.setExecutable(getRootPath(), relativePath, executable);
    }


    public boolean delete(String relativePath) {
        return storageAccess.delete(getRootPath(), relativePath);
    }


    public long getStorageSize(String relativePath) {
        return storageAccess.getStorageSize(getRootPath(), relativePath);
    }


    public InputStream getInputStream(String relativePath) throws IOException {
        return storageAccess.getInputStream(getRootPath(), relativePath);
    }


    public OutputStream getOutputStream(String relativePath) throws IOException {
        return storageAccess.getOutputStream(getRootPath(), relativePath);
    }


    public String getSeparator() {
        return storageAccess.getSeparator();
    }


    public void flush(ExtendedFileInfo info) {
        storageAccess.flush(getRootPath(), info);
    }

}
