/*
 * Copyright (C) 2021 Martin Goellnitz
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


/**
 * New file information holder class including executable flag.
 */
public class ExtendedFileInfo extends FileInfo implements Serializable {

    private boolean executable = false;


    public ExtendedFileInfo() {
    }


    public ExtendedFileInfo(FileInfo fi) {
        this.setCanRead(fi.isCanRead());
        this.setCanWrite(fi.isCanWrite());
        this.setDirectory(fi.isDirectory());
        this.setExists(fi.isExists());
        this.setModificationDate(fi.getModificationDate());
        this.setName(fi.getName());
        this.setPath(fi.getPath());
        this.setSize(fi.getSize());
    }


    public boolean isCanExecute() {
        return executable;
    }


    public void setCanExecute(boolean exec) {
        this.executable = exec;
    }


    @Override
    public String toString() {
        return (isCanExecute() ? "x" : "-") + super.toString();
    } // toString()

} // ExtendedFileInfo
