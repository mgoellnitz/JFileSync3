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
 * Another wrapper class for int values. The original wrapper class doesn't
 * allow to change the value of an Integer which is necessary for an efficient
 * handling of int values in the JFSProgress object.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSInteger.java,v 1.9 2007/02/26 18:49:08 heidrich Exp $
 */
public class JFSInteger {

	/** The int value. */
	private int value;

	/**
	 * Default constructor. Initializes the int value.
	 * 
	 * @param value
	 *            The int value.
	 */
	public JFSInteger(int value) {
		this.value = value;
	}

	/**
	 * This method gets the currently stored value.
	 * 
	 * @return The int value.
	 */
	public final int getValue() {
		return value;
	}

	/**
	 * This method sets a new int value.
	 * 
	 * @param value
	 *            The new int value.
	 */
	public final void setValue(int value) {
		this.value = value;
	}
}