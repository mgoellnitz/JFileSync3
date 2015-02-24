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

/**
 * This class specifies all view modes.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSViewModes.java,v 1.11 2007/02/26 18:49:11 heidrich Exp $
 */
public final class JFSViewModes {

	/** Stores the only instance of the class. */
	private static JFSViewModes instance = null;

	/** The default mode. */
	private int defaultMode;

	/** The map containing all available modes. */
	private TreeMap<Integer, JFSViewMode> modes = new TreeMap<Integer, JFSViewMode>();

	/**
	 * Creates a new table of sync modes.
	 */
	private JFSViewModes() {
		JFSViewMode m;

		// View Mode VIEW_ALL. View all files of the comparison table:
		m = new JFSViewMode(20, "view.all");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.NOP);
		m.setViewed(SyncAction.COPY_SRC);
		m.setViewed(SyncAction.COPY_TGT);
		m.setViewed(SyncAction.DELETE_SRC);
		m.setViewed(SyncAction.DELETE_TGT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_EXCHANGE. View only files that must be copied from
		// source to target and vice versa:
		m = new JFSViewMode(21, "view.exchange");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.COPY_SRC);
		m.setViewed(SyncAction.COPY_TGT);
		m.setViewed(SyncAction.DELETE_SRC);
		m.setViewed(SyncAction.DELETE_TGT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_SRC_TO_TGT. View only files that must be copied from
		// source to target:
		m = new JFSViewMode(22, "view.srcToTgt");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.COPY_SRC);
		m.setViewed(SyncAction.DELETE_SRC);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_TGT_TO_SRC. View only files that must be copied from
		// target to source:
		m = new JFSViewMode(23, "view.tgtToSrc");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.COPY_TGT);
		m.setViewed(SyncAction.DELETE_TGT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_COPY. View only files that must be copied:
		m = new JFSViewMode(24, "view.copy");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.COPY_SRC);
		m.setViewed(SyncAction.COPY_TGT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_DELETE. View only files that must be deleted:
		m = new JFSViewMode(25, "view.delete");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.DELETE_SRC);
		m.setViewed(SyncAction.DELETE_TGT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// View Mode VIEW_ASK. View only ambiguous files:
		m = new JFSViewMode(26, "view.ask");
		m.setViewed(SyncAction.NOP_ROOT);
		m.setViewed(SyncAction.ASK_LENGTH_INCONSISTENT);
		m.setViewed(SyncAction.ASK_FILES_GT_HISTORY);
		m.setViewed(SyncAction.ASK_FILES_NOT_IN_HISTORY);
		modes.put(m.getId(), m);

		// Set default mode:
		defaultMode = 21;
	}

	/**
	 * Returns the reference of the only object of the class.
	 * 
	 * @return The only instance.
	 */
	public static JFSViewModes getInstance() {
		if (instance == null)
			instance = new JFSViewModes();

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
	public Collection<JFSViewMode> getModes() {
		return modes.values();
	}

	/**
	 * @return Returns the current view mode.
	 */
	public JFSViewMode getCurrentMode() {
		JFSViewMode mode = modes.get((int) JFSConfig.getInstance().getView());
		if (mode == null)
			mode = modes.get(getDefaultMode());

		assert mode != null;

		return mode;
	}

	/**
	 * Returns a mode of the given identifier. If no such exists null is
	 * returned.
	 * 
	 * @param id
	 *            The mode's identifier.
	 * @return The mode of the given identifier.
	 */
	public JFSViewMode get(int id) {
		return modes.get(id);
	}

	/**
	 * @return Returns the default mode.
	 */
	public int getDefaultMode() {
		return defaultMode;
	}
}