/*
 * Copyright (C) 2010-2013, Martin Goellnitz
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

import java.io.Serializable;

public class FileInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 261020090742L;

    private String name = "";

    private String path = "";

    private long modificationDate = 0;

    private long size;

    private boolean canRead = true;

    private boolean canWrite = true;

    private boolean exists = false;

    private boolean isDirectory = false;


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public long getModificationDate() {
        return modificationDate;
    }


    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }


    public long getSize() {
        return size;
    }


    public void setSize(long size) {
        this.size = size;
    }


    public boolean isCanRead() {
        return canRead;
    }


    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }


    public boolean isCanWrite() {
        return canWrite;
    }


    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }


    public boolean isExists() {
        return exists;
    }


    public void setExists(boolean exists) {
        this.exists = exists;
    }


    public boolean isDirectory() {
        return isDirectory;
    }


    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }


    @Override
    public String toString() {
        return (isExists() ? "e" : "-")+(isDirectory() ? "d" : "-")+(isCanRead() ? "r" : "-")+(isCanWrite() ? "w" : "-")+"["+getSize()
                +"]";
    } // toString()

} // FileInfo
