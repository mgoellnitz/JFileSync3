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

import java.util.Vector;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConfigObserver;
import jfs.conf.JFSSyncMode;
import jfs.conf.JFSSyncModes;
import jfs.conf.JFSViewMode;
import jfs.conf.JFSViewModes;

/**
 * The JFS table object administrates a flat table of all compared JFS elements and deals with the current view on this
 * table as well corresponding copy and delete statements neccessary in order to synchronize all table elements. During
 * comparison, the table is filled automatically. Before synchronization, the corresponding copy and delete statements
 * are computed. The class observes the configuration object. In case of changes, the object is updated accordingly.
 * 
 * @see jfs.conf.JFSConfig
 * @see jfs.sync.JFSComparison
 * @see jfs.sync.JFSSynchronization
 * @author Jens Heidrich
 * @version $Id: JFSTable.java,v 1.4 2007/07/20 16:35:36 heidrich Exp $
 */
public class JFSTable implements JFSConfigObserver {

    /** Stores the only instance of the class. */
    private static JFSTable instance = null;

    /** The JFS root elements corresponding to the directory pairs. */
    private Vector<JFSRootElement> roots = new Vector<JFSRootElement>();

    /** The table containing all compared elements in the right sequence. */
    private Vector<JFSElement> table = new Vector<JFSElement>();

    /** The current view on the table. */
    private Vector<JFSElement> view = new Vector<JFSElement>();

    /** The list of copy statements. */
    private Vector<JFSCopyStatement> copyStatements = new Vector<JFSCopyStatement>();

    /** The list of delete statements. */
    private Vector<JFSDeleteStatement> deleteStatements = new Vector<JFSDeleteStatement>();


    /**
     * Creates a new table object.
     */
    private JFSTable() {
        // Set default values:
        clean();
    }


    /**
     * Returns the reference of the only instance.
     * 
     * @return The only instance.
     */
    public final static JFSTable getInstance() {
        if (instance==null)
            instance = new JFSTable();

        return instance;
    }


    /**
     * Restores the default values.
     */
    public final void clean() {
        table.clear();
        view.clear();

        copyStatements.clear();
        deleteStatements.clear();
    }


    /**
     * Returns the JFS root element at a given index.
     * 
     * @param index
     *            The index to return.
     * @return The element for the index.
     */
    public final JFSRootElement getRootElement(int index) {
        return roots.get(index);
    }


    /**
     * Returns the JFS element for the overall table at a given index.
     * 
     * @param index
     *            The index to return.
     * @return The element for the index.
     */
    public final JFSElement getTableElement(int index) {
        return table.get(index);
    }


    /**
     * Returns the JFS element for the current view at a given index.
     * 
     * @param index
     *            The index to return.
     * @return The element for the index.
     */
    public final JFSElement getViewElement(int index) {
        return view.get(index);
    }


    /**
     * @return Returns the number of JFS root elements.
     */
    public final int getRootsSize() {
        return roots.size();
    }


    /**
     * @return Returns the size of the overall table.
     */
    public final int getTableSize() {
        return table.size();
    }


    /**
     * @return Returns the size of the current view.
     */
    public final int getViewSize() {
        return view.size();
    }


    /**
     * Adds a certain JFS element and its parent to the current view if it needs to be viewed and is not already viewed.
     * 
     * @param element
     *            The element to add.
     */
    private final void addElementToView(JFSElement element) {
        JFSViewMode mode = JFSViewModes.getInstance().getCurrentMode();
        if ( !mode.isViewed(element.getAction())||element.isViewed())
            return;

        if (view.size()>0&& !element.isDirectory()) {
            JFSElement last = view.lastElement();
            if (last.getParent()!=element.getParent()&&last!=element.getParent()) {
                view.add(element.getParent());
                element.getParent().setViewed(true);
            }
        }

        view.add(element);
        element.setViewed(true);
    }


    /**
     * Adds a certain JFS element and its parent to the flat table, computes the synchronization actions according to
     * the current synchronization mode, and adds it to the current view if it needs to be viewed according to the
     * current view. The table needs to be filled top town; that is, first a folder and then a contained elements (files
     * or more folders).
     * 
     * @param element
     *            The element to add.
     */
    public final void addElement(JFSElement element) {
        JFSSyncModes.getInstance().getCurrentMode().computeAction(element);
        table.add(element);
        addElementToView(element);
    }


    /**
     * Adds a JFS root element.
     * 
     * @param element
     *            The element to add.
     */
    public final void addRoot(JFSRootElement element) {
        roots.add(element);
        addElement(element);
        element.setViewed(true);
    }


    /**
     * Removes a certain JFS element and its parent from the current view.
     * 
     * @param element
     *            The element to remove.
     */
    private final void removeElementFromView(JFSElement element) {
        JFSViewMode mode = JFSViewModes.getInstance().getCurrentMode();

        // Remove only, if no children are viewed any more:
        if (element.isViewed()&& !mode.isViewed(element.getAction())) {
            if (element.getChildren()!=null) {
                for (JFSElement child : element.getChildren()) {
                    if (child.isViewed()&& !child.isDirectory()) {
                        return;
                    }
                }
            }
            view.remove(element);
            element.setViewed(false);
        }

        // Check whether parent can be removed, if no children are viewed:
        JFSElement parent = element.getParent();
        if ( !parent.isRoot()) {
            removeElementFromView(parent);
        }
    }


    /**
     * Removes a certain JFS element and its parent from the table and the current view.
     * 
     * @param element
     *            The element to remove.
     */
    public final void removeElement(JFSElement element) {
        table.remove(element);
        removeElementFromView(element);
    }


    /**
     * Updates a certain JFS element and its parent in the current view. If the element was part of the current view and
     * should not be viewed any more, it is removed from the view. Note, that if it was not viewed before and should be
     * viewed now, the view is not changed. You have to re-compute the whole view again in this case.
     * 
     * @param element
     *            The element to update.
     */
    public final void updateElement(JFSElement element) {
        JFSViewMode mode = JFSViewModes.getInstance().getCurrentMode();
        if ( !mode.isViewed(element.getAction())) {
            removeElementFromView(element);
        }
    }


    /**
     * @return Returns all copy statements.
     */
    public final Vector<JFSCopyStatement> getCopyStatements() {
        return copyStatements;
    }


    /**
     * @return Returns all delete statements.
     */
    public final Vector<JFSDeleteStatement> getDeleteStatements() {
        return deleteStatements;
    }


    /**
     * @return Returns all failed copy statements.
     */
    public final Vector<JFSCopyStatement> getFailedCopyStatements() {
        Vector<JFSCopyStatement> v = new Vector<JFSCopyStatement>();

        for (JFSCopyStatement cs : copyStatements) {
            if ( !cs.getSuccess()&&cs.getCopyFlag())
                v.add(cs);
        }

        return v;
    }


    /**
     * @return Returns all failed delete statements.
     */
    public final Vector<JFSDeleteStatement> getFailedDeleteStatements() {
        Vector<JFSDeleteStatement> v = new Vector<JFSDeleteStatement>();

        for (JFSDeleteStatement ds : deleteStatements) {
            if ( !ds.getSuccess()&&ds.getDeleteFlag())
                v.add(ds);
        }

        return v;
    }


    /**
     * Re-computes the current view for all elements of the comparison table.
     */
    public final void recomputeView() {
        view.clear();
        for (JFSElement element : table) {
            element.setViewed(false);
            addElementToView(element);
        }
    }


    /**
     * Re-computes all actions and the current view for all elements of the comparison table.
     */
    public final void recomputeActionsAndView() {
        view.clear();
        JFSSyncMode mode = JFSSyncModes.getInstance().getCurrentMode();
        for (JFSElement element : table) {
            mode.computeAction(element);
            element.setViewed(false);
            addElementToView(element);
        }
    }


    /**
     * @see JFSConfigObserver#updateConfig(JFSConfig)
     */
    @Override
    public final void updateConfig(JFSConfig config) {
        recomputeActionsAndView();
    }


    /**
     * @see JFSConfigObserver#updateComparison(JFSConfig)
     */
    @Override
    public final void updateComparison(JFSConfig config) {
        // Clean the object and JFS file producers:
        clean();
        JFSFileProducerManager.getInstance().resetProducers();

        // Update config:
        updateConfig(config);
    }


    /**
     * @see JFSConfigObserver#updateServer(JFSConfig)
     */
    @Override
    public final void updateServer(JFSConfig config) {
        updateComparison(config);
    }
}