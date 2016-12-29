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
 * Represents a delete statement; i.e., a files that has to be deleted. The
 * object includes the file object and a flag which determines whether the
 * delete statement has to be performed or not.
 *
 * @author Jens Heidrich
 * @version $Id: JFSDeleteStatement.java,v 1.10 2007/02/26 18:49:09 heidrich Exp $
 */
public class JFSDeleteStatement {

	/** The associated element of the comparison table. */
	private final JFSElement element;

	/** The file to be deleted. */
	private final JFSFile delFile;

	/** Flag. If this flag is set, the delete statement has to be performed. */
	private boolean deleteFlag = true;

	/**
	 * This flag informs whether deleting the file was successful or not. It is
	 * false by default.
	 */
	private boolean success = false;

	/**
	 * Default constructor.
	 *
	 * @param element
	 *            The associated element of the comparison table.
	 * @param delFile
	 *            The source file.
	 */
	public JFSDeleteStatement(JFSElement element, JFSFile delFile) {
		this.element = element;
		this.delFile = delFile;
	}

	/**
	 * Returns the file that has to be deleted.
	 *
	 * @return A JFSFile object.
	 */
	public final JFSFile getFile() {
		return delFile;
	}

	/**
	 * Returns the state of the delete flag.
	 *
	 * @return The delete flag.
	 */
	public final boolean getDeleteFlag() {
		return deleteFlag;
	}

	/**
	 * Sets the state of the delete flag. If the boolean value is true the file
	 * is enabled to be deleted. If it is false, the delete statement is skiped
	 * in the algorithm which uses the JFSDeleteStatement object.
	 *
	 * @param deleteFlag
	 *            The delete flag.
	 */
	public final void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
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
	 * Sets the state of the success flag. If the boolean value is true the file
	 * was successfully deleted.
	 *
	 * @param success
	 *            The success flag.
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
}