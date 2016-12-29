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
package jfs.sync;


/**
 * Represents a copy statement; i.e., two files that have to be copied. The
 * object includes a source and a target file object and a flag which determines
 * whether the copy statement has to be performed or not.
 *
 * @author Jens Heidrich
 * @version $Id: JFSCopyStatement.java,v 1.10 2007/02/26 18:49:09 heidrich Exp $
 */
public class JFSCopyStatement {

    /**
     * The associated element of the comparison table.
     */
    private final JFSElement element;

    /**
     * The source file.
     */
    private final JFSFile srcFile;

    /**
     * The target file.
     */
    private final JFSFile tgtFile;

    /**
     * Determines whether the source file in the copy statement equals to the
     * source file of the comparison table element (true) or the target file
     * (false).
     */
    private boolean copyFromSource;

    /**
     * Flag. If this flag is set, the copy statement has to be performed.
     */
    private boolean copyFlag = true;

    /**
     * This flag informs whether the copy of the file was successful or not. It
     * is false by default.
     */
    private boolean success = false;


    /**
     * Default constructor.
     *
     * @param element
     * The associated element of the comparison table.
     * @param srcFile
     * The source file.
     * @param tgtFile
     * The target file.
     */
    public JFSCopyStatement(JFSElement element, JFSFile srcFile, JFSFile tgtFile) {
        this.element = element;
        this.srcFile = srcFile;
        this.tgtFile = tgtFile;

        if (srcFile==element.getSrcFile()) {
            copyFromSource = true;
        } else {
            copyFromSource = false;
        }
    }


    /**
     * Returns the source file.
     *
     * @return Source file.
     */
    public final JFSFile getSrc() {
        return srcFile;
    }


    /**
     * Returns the target file.
     *
     * @return Target file.
     */
    public final JFSFile getTgt() {
        return tgtFile;
    }


    /**
     * Returns the state of the copy flag.
     *
     * @return The copy flag.
     */
    public final boolean getCopyFlag() {
        return copyFlag;
    }


    /**
     * Sets the state of the copy flag. If the boolean value is true the source
     * file is enabled to be copied to the target. If it is false, the copy
     * statement is skiped in the algorithm which uses the JFSCopyStatement
     * object.
     *
     * @param copyFlag
     * The copy flag.
     */
    public final void setCopyFlag(boolean copyFlag) {
        this.copyFlag = copyFlag;
    }


    /**
     * Returns the state of the success flag.
     *
     * @return The success flag.
     */
    public final boolean getSuccess() {
        return success;
    }


    /**
     * Sets the state of the success flag. If the boolean value is true the
     * source file was successfully copied to the target.
     *
     * @param success
     * The success flag.
     */
    public final void setSuccess(boolean success) {
        this.success = success;
    }


    /**
     * Returns the associated element of the comparison table.
     *
     * @return Element of the comparison table.
     */
    public JFSElement getElement() {
        return element;
    }


    /**
     * Returns whether the source file in the copy statement equals to the
     * source file of the comparison table element (true) or the target file
     * (false).
     *
     * @return True if the copy source matches the source file of the comparison
     * table.
     */
    public boolean isCopyFromSource() {
        return copyFromSource;
    }

}
