/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
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
package jfs.conf;

import java.io.File;


/**
 * Represents two directories that have to be compared against each other.
 *
 * @author Jens Heidrich
 * @version $Id: JFSDirectoryPair.java,v 1.11 2007/02/26 18:49:11 heidrich Exp $
 */
public final class JFSDirectoryPair implements Cloneable {

    /**
     * The source directory.
     */
    private String srcDir;

    /**
     * The target directory.
     */
    private String tgtDir;


    /**
     * Creates objects out of corresponding strings.
     *
     * @param srcDir
     * String of the source directory.
     * @param tgtDir
     * String of the target directory.
     */
    public JFSDirectoryPair(String srcDir, String tgtDir) {
        this.srcDir = srcDir;
        this.tgtDir = tgtDir;
    }


    /**
     * Returns the source directory.
     *
     * @return Source Directory.
     */
    public String getSrc() {
        return srcDir;
    }


    /**
     * Sets the source directory.
     *
     * @param srcDir
     * Source Directory.
     */
    public void setSrc(String srcDir) {
        this.srcDir = srcDir;
    }


    /**
     * Sets the source directory.
     *
     * @param srcDir
     * Source Directory.
     */
    public void setSrc(File srcDir) {
        this.srcDir = srcDir.getPath();
    }


    /**
     * Returns the target directory.
     *
     * @return Target Directory.
     */
    public String getTgt() {
        return tgtDir;
    }


    /**
     * Sets the target directory.
     *
     * @param tgtDir
     * Target Directory.
     */
    public void setTgt(String tgtDir) {
        this.tgtDir = tgtDir;
    }


    /**
     * Sets the target directory.
     *
     * @param tgtDir
     * Target Directory.
     */
    public void setTgt(File tgtDir) {
        this.tgtDir = tgtDir.getPath();
    }


    /**
     * @see Object#clone()
     */
    @Override
    public JFSDirectoryPair clone() {
        return new JFSDirectoryPair(srcDir, tgtDir);
    }


    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object.getClass()!=getClass()) {
            return false;
        }
        JFSDirectoryPair pair = (JFSDirectoryPair) object;

        return getSrc().equals(pair.getSrc())&&getTgt().equals(pair.getTgt());
    }

}
