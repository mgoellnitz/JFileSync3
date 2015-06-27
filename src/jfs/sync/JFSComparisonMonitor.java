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


/**
 * Monitors the detailed state of the currently performed comparison.
 *
 * @author Jens Heidrich
 * @version $Id: JFSComparisonMonitor.java,v 1.1 2006/08/25 15:54:37 heidrich Exp $
 */
public final class JFSComparisonMonitor {

    /**
     * Stores the only instance of the class.
     */
    private static JFSComparisonMonitor instance = null;

    /**
     * The number items to handle on every level of the file hierarchy.
     */
    private Vector<Integer> itemsStarted = new Vector<Integer>();

    /**
     * The number items already handled on every level.
     */
    private Vector<Integer> itemsHandled = new Vector<Integer>();

    /**
     * The weigth of all currently handled items on every level.
     */
    private Vector<Integer> itemsWeight = new Vector<Integer>();

    /**
     * The current root URI for source files handled.
     */
    private String rootUriSrc = "";

    /**
     * The current root URI for target files handled.
     */
    private String rootUriTgt = "";

    /**
     * The current source file handled.
     */
    private JFSFile currentSrc = null;

    /**
     * The current target file handled.
     */
    private JFSFile currentTgt = null;


    /**
     * Creates a new comparison object.
     */
    private JFSComparisonMonitor() {
    }


    /**
     * Returns the reference of the only instance.
     *
     * @return The only instance.
     */
    public  static JFSComparisonMonitor getInstance() {
        if (instance==null) {
            instance = new JFSComparisonMonitor();
        }

        return instance;
    }


    /**
     * Restores the default values.
     */
    public void clean() {
        currentSrc = null;
        currentTgt = null;
        itemsStarted.clear();
        itemsHandled.clear();
        itemsWeight.clear();
    }


    /**
     * Increases the progress depth by adding new elements for (1) number of started items, (2) number of handled items,
     * and finally (3) the weight for the currently handeled items. Always used in combination with decrease().
     *
     * @param containedItems
     * The number of contained sub items in the currently handled item.
     * @param weight
     * The weigth of the currently handled item; i.e., the delta by which the number of handled items is
     * increased if this item is completely handled.
     */
    void increase(int containedItems, int weight) {
        itemsStarted.add(containedItems);
        itemsHandled.add(0);
        itemsWeight.add(weight);
    }


    /**
     * Decreases the depth for the started and handled items. Always used in combination with increase().
     */
    void decrease() {
        // All vector (should ;-) have the same size, so we just
        // use the size of itemsStarted to remove the last elements:
        int size = itemsStarted.size();

        if (size>1) {
            // Increase the number of handled directories by the weight of
            // the currently finished directory:
            int handled = itemsHandled.get(size-2);
            int weight = itemsWeight.get(size-1);
            itemsHandled.set(size-2, handled+weight);

            // Because the directory (pair) is handled, we can remove
            // the number of started and handled sub directories and the
            // weight:
            itemsStarted.remove(size-1);
            itemsHandled.remove(size-1);
            itemsWeight.remove(size-1);
        } else {
            itemsStarted.clear();
            itemsHandled.clear();
            itemsWeight.clear();
        }
    }


    /**
     * @return Returns the ratio of the items already handled in percent.
     */
     int getRatio() {
        float ratio = 0;
        for (int i = itemsStarted.size()-1; i>=0; i--) {
            int started = itemsStarted.get(i);
            int handled = itemsHandled.get(i);
            int weight = itemsWeight.get(i);

            if (started!=0) {
                ratio = (weight*(handled+ratio))/started;
            }
        }

        return Math.round(ratio*100);
    }


    /**
     * @return Returns the root URI for all source files handeled.
     */
    public String getRootUriSrc() {
        return rootUriSrc;
    }


    /**
     * Sets the root URI for all source files handeled.
     *
     * @param rootUriSrc
     * The URI to set.
     */
    public void setRootUriSrc(String rootUriSrc) {
        this.rootUriSrc = rootUriSrc;
    }


    /**
     * @return Returns the root URI for all target files handeled.
     */
    public String getRootUriTgt() {
        return rootUriTgt;
    }


    /**
     * Sets the root URI for all target files handeled.
     *
     * @param rootUriTgt
     * The URI to set.
     */
    public void setRootUriTgt(String rootUriTgt) {
        this.rootUriTgt = rootUriTgt;
    }


    /**
     * @return Returns the currently handeled source directory.
     */
    public JFSFile getCurrentSrc() {
        return currentSrc;
    }


    /**
     * Sets the currently handeled source directory.
     *
     * @param currentSrc
     * The current source to set.
     */
    void setCurrentSrc(JFSFile currentSrc) {
        this.currentSrc = currentSrc;
    }


    /**
     * @return Returns the currently handeled target directory.
     */
    public  JFSFile getCurrentTgt() {
        return currentTgt;
    }


    /**
     * Sets the currently handeled target directory.
     *
     * @param currentTgt
     * The current target to set.
     */
    void setCurrentTgt(JFSFile currentTgt) {
        this.currentTgt = currentTgt;
    }


    /**
     * @return Returns the currently handeled directory (which equals the source directory, if the source directory is
     * not null and the target directory otherwise). If the monitor was not initialized null is returned.
     */
    public JFSFile getCurrentDir() {
        if (currentSrc!=null) {
            return currentSrc;
        }
        return currentTgt;
    }

}
