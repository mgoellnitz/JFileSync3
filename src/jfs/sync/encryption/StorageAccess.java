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


/**
 * Abstraction interface for story access.
 */
public interface StorageAccess {

    ExtendedFileInfo getFileInfo(String rootpath, String path);


    String[] list(String rootpath, String path);


    boolean createDirectory(String rootpath, String path);


    boolean setLastModified(String rootpath, String path, long modificationDate);


    boolean setWritable(String rootpath, String path, boolean writable);


    boolean setExecutable(String rootpath, String path, boolean executable);


    boolean delete(String rootpath, String path);


    InputStream getInputStream(String rootpath, String path) throws IOException;


    OutputStream getOutputStream(String rootpath, String path) throws IOException;


    String getSeparator();


    void flush(String rootPath, ExtendedFileInfo info);


    String getCipherSpec();


    byte[] getFileCredentials(String password);

} // StorageAccess
