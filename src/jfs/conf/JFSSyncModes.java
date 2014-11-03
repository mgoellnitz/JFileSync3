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

import java.util.Collection;
import java.util.TreeMap;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.sync.JFSElement.ElementState;

/**
 * This class specifies all synchronization modes.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSSyncModes.java,v 1.10 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSSyncModes {

	/** Stores the only instance of the class. */
	private static JFSSyncModes instance = null;

	/** The default mode. */
	private int defaultMode;

	/** The map containing all available modes. */
	private TreeMap<Integer, JFSSyncMode> modes = new TreeMap<Integer, JFSSyncMode>();

	/**
	 * Creates a new table of sync modes.
	 */
	private JFSSyncModes() {
		JFSSyncMode m;

		// Synchronization Mode AUTOMATIC. The history is used to determine
		// whether to delete or copy files. If the history is empty, a merge
		// is performed:
		m = new JFSSyncMode(9, "syncMode.automatic");
		m.setAutomatic(true);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_ALL. All files that don't exist on the
		// source side are copied from the target side and vice versa:
		m = new JFSSyncMode(10, "syncMode.copyAll");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.COPY_TGT);
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.COPY_SRC);
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_SRC);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_TGT);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_FROM_SRC. Only files that don't exist on
		// the target side are copied from the source side:
		m = new JFSSyncMode(11, "syncMode.copyFromSrc");
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.COPY_SRC);
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_SRC);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_FROM_TGT. Only files that don't exist on
		// the source side are copied from the target side:
		m = new JFSSyncMode(12, "syncMode.copyFromTgt");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.COPY_TGT);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_TGT);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_FROM_SRC_DELETE_FROM_TGT. Files that don't
		// exist on the target side are copied from the source side and files
		// that don't exist on the source side are deleted from the target side:
		m = new JFSSyncMode(13, "syncMode.copyFromSrcDeleteFromTgt");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.DELETE_TGT);
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.COPY_SRC);
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_SRC);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_FROM_TGT_DELETE_FROM_SRC. Files that don't
		// exist on the source side are copied from the target side and files
		// that don't exist on the target side are deleted from the source side:
		m = new JFSSyncMode(14, "syncMode.copyFromTgtDeleteFromSrc");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.COPY_TGT);
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.DELETE_SRC);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_TGT);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode COPY_COMMON_ONLY. Only files that exist on both
		// sides are synchronized, that is a file ic copied, if it is newer than
		// it's counterpart on the other side:
		m = new JFSSyncMode(15, "syncMode.copyCommonOnly");
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_SRC);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_TGT);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode FORCE_SRC_COPY. All newer and even older files
		// from the source side are copied to the target. All files not existing
		// on source side are deleted from target. That means, if a file on
		// target side is newer than the file on source side, it will be
		// overwritten with the older file.
		m = new JFSSyncMode(16, "syncMode.forceSrcCopy");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.DELETE_TGT);
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.COPY_SRC);
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_SRC);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_SRC);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Synchronization Mode FORCE_TGT_COPY. All newer and even older files
		// from the target side are copied to the source. All files not existing
		// on target side are deleted from source. That means, if a file on
		// source side is newer than the file on target side, it will be
		// overwritten with the older file.
		m = new JFSSyncMode(17, "syncMode.forceTgtCopy");
		m.setAction(ElementState.SRC_IS_NULL, SyncAction.COPY_TGT);
		m.setAction(ElementState.TGT_IS_NULL, SyncAction.DELETE_SRC);
		m.setAction(ElementState.SRC_GT_TGT, SyncAction.COPY_TGT);
		m.setAction(ElementState.TGT_GT_SRC, SyncAction.COPY_TGT);
		m.setAction(ElementState.LENGTH_INCONSISTENT,
				SyncAction.ASK_LENGTH_INCONSISTENT);
		modes.put(m.getId(), m);

		// Set default mode:
		defaultMode = 9;
	}

	/**
	 * Returns the reference of the only object of the class.
	 * 
	 * @return The only instance.
	 */
	public static JFSSyncModes getInstance() {
		if (instance == null) {
			instance = new JFSSyncModes();
                }

		return instance;
	}

	/**
	 * Tests whether a mode of the given identifier was specified.
	 * 
	 * @param id
	 *            The mode's identifier.
	 * @return True if the mode exists.
	 */
	public boolean contains(int id) {
		return modes.containsKey(id);
	}

	/**
	 * @return Returns all modes; that is, a collection of all mode objects.
	 */
	public Collection<JFSSyncMode> getModes() {
		return modes.values();
	}

	/**
	 * @return Returns the current sync mode.
	 */
	public JFSSyncMode getCurrentMode() {
		JFSSyncMode mode = modes.get((int) JFSConfig.getInstance()
				.getSyncMode());
		if (mode == null) {
			mode = modes.get(getDefaultMode());
                }

		assert mode != null;

		return mode;
	}

	/**
	 * @return Returns the default mode.
	 */
	public int getDefaultMode() {
		return defaultMode;
	}
}