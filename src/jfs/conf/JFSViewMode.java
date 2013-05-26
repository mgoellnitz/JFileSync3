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

import java.util.Vector;

import jfs.conf.JFSSyncMode.SyncAction;

/**
 * This class specifies a single view mode.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSViewMode.java,v 1.6 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSViewMode {

    /** The identifier of the mode. */
    private int id;

    /** The string alias of the mode. */
    private String alias;

    /** Stores the actions for which the mode allows viewing. */
    private Vector<SyncAction> allowedActions = new Vector<SyncAction>();


    /**
     * Creates a new mode.
     * 
     * @param id
     *            The identifier to use.
     * @param alias
     *            The alias to use.
     */
    public JFSViewMode(int id, String alias) {
        this.id = id;
        this.alias = alias;
    }


    /**
     * Returns whether this mode allows viewing for a certain action. If nothing is specified regarding the given
     * action, no viewing is allowed.
     * 
     * @param action
     *            The action.
     * @return True if and only if the mode allows viewing.
     */
    public boolean isViewed(SyncAction action) {
        return allowedActions.contains(action);
    }


    /**
     * Sets whether this mode allows viewing for a certain action. If nothing is specified regarding the given action,
     * no viewing is allowed.
     * 
     * @param action
     *            The action.
     */
    public void setViewed(SyncAction action) {
        allowedActions.add(action);
    }


    /**
     * @return Returns the identifier of the mode.
     */
    public int getId() {
        return id;
    }


    /**
     * @return Returns the alias of the mode.
     */
    public String getAlias() {
        return alias;
    }


    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return alias+" ["+id+"]";
    }
}