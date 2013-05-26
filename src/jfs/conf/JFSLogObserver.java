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

/**
 * An Interface for all observers of a log object.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLogObserver.java,v 1.4 2007/02/26 18:49:11 heidrich Exp $
 */
public interface JFSLogObserver {

	/**
	 * Called when the log status changed.
	 * 
	 * @param log
	 *            The log object.
	 */
	public void update(JFSLog log);
}