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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import jfs.conf.JFSConfig;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSSyncModes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents a single pair of corresponding files or directories on the source and target side within the directory
 * structures that have to be compared.
 *
 * @author Jens Heidrich
 * @version $Id: JFSElement.java,v 1.22 2007/03/29 14:11:35 heidrich Exp $
 */
public class JFSElement implements Comparable<JFSElement> {

    private static final Log LOG = LogFactory.getLog(JFSElement.class);

    /** The root element to which the element belongs to. */
    protected JFSRootElement root;

    /** Source file. */
    protected JFSFile srcFile;

    /** Target file. */
    protected JFSFile tgtFile;

    /** The parent of the current element. */
    protected JFSElement parent = null;

    /** The children of the current element. */
    protected List<JFSElement> children = null;

    /** Determines whether the File objects are directories. */
    protected boolean directory;

    /** Determines the state of the element. */
    protected ElementState state = ElementState.NOT_DETERMINED;

    /** Determines whether the action was manually set. */
    protected boolean manuallySetAction = false;

    /**
     * The action that has to be performed for the JFS element.
     *
     * @see JFSSyncModes
     */
    protected SyncAction action = SyncAction.NOP;

    /** Determines whether the action is active or should be skipped. */
    protected boolean active = true;

    /** Determines whether the element is currently viewed. */
    protected boolean viewed = false;


    /** The states of the element. */
    public enum ElementState {

        IS_ROOT, NOT_DETERMINED, EQUAL, SRC_IS_NULL, TGT_IS_NULL, SRC_GT_TGT, TGT_GT_SRC, LENGTH_INCONSISTENT
    }


    /** Constructor for derived classes. */
    protected JFSElement() {
    }


    /**
     * Constructs an element that compares two File objects and determines whether one is newer than the other or
     * whether both files are equal. At least one file has to be not equal to null. If both files are equal, there
     * relative paths have to match. The parent must be a directory and must not be null.
     *
     * @param srcFile Source file.
     * @param tgtFile Target file.
     * @param parent The parent of the current element.
     * @param isDirectory Determines whether the File objects are directories.
     */
    public JFSElement(JFSFile srcFile, JFSFile tgtFile, JFSElement parent, boolean isDirectory) {
        assert (srcFile!=null||tgtFile!=null)&&parent!=null&&parent.isDirectory();

        this.root = parent.getRoot();
        this.srcFile = srcFile;
        this.tgtFile = tgtFile;
        this.parent = parent;
        this.directory = isDirectory;
        parent.addChild(this);

        revalidate();
    }


    /**
     * Revalidates the comparison table element; that is checks which file is newer, etc.
     */
    public final void revalidate() {
        if (state==ElementState.IS_ROOT) {
            return;
        }

        // Reset attributes:
        state = ElementState.NOT_DETERMINED;

        // Get last modified information:
        long srcLastModified = 0;
        long tgtLastModified = 0;

        if (!directory) {
            if (srcFile!=null) {
                srcLastModified = srcFile.getLastModified();
            }
            if (tgtFile!=null) {
                tgtLastModified = tgtFile.getLastModified();
            }
        }

        // Comparison:
        // Under the DOS and Windows FAT file system, the finest granularity on
        // time resolution is two seconds. So we have a maximum tolerance range
        // of 2000ms for each comparison:
        long diffTime = compareToTime(srcLastModified, tgtLastModified);
        if (LOG.isDebugEnabled()) {
            LOG.debug("revalidate() diffTime="+diffTime);
        } // if

        if (srcFile==null) {
            state = ElementState.SRC_IS_NULL;
        } else if (tgtFile==null) {
            state = ElementState.TGT_IS_NULL;
        } else {
            if (!directory) {
                if (diffTime==0) {
                    state = ElementState.EQUAL;
                } else {
                    if (diffTime>0) {
                        state = ElementState.SRC_GT_TGT;
                    } else {
                        state = ElementState.TGT_GT_SRC;
                    }
                }
            } else {
                state = ElementState.EQUAL;
            }
        }

        // Check length:
        if (state==ElementState.EQUAL&&srcFile.getLength()!=tgtFile.getLength()) {
            state = ElementState.LENGTH_INCONSISTENT;
        }

        // Set action to NOP if isEqual is true:
        if (state==ElementState.EQUAL) {
            action = SyncAction.NOP;
        }
    }


    /**
     * @return Returns the root element to which the element belongs to.
     */
    public final JFSRootElement getRoot() {
        return root;
    }


    /**
     * @return Returns the source file.
     */
    public final JFSFile getSrcFile() {
        return srcFile;
    }


    /**
     * Sets the source file. Used when updating the synchronization table during the process.
     *
     * @param file The file to set as source.
     */
    public void setSrcFile(JFSFile file) {
        srcFile = file;
    }


    /**
     * @return Returns the target file.
     */
    public final JFSFile getTgtFile() {
        return tgtFile;
    }


    /**
     * Sets the traget file. Used when updating the synchronization table during the process.
     *
     * @param file The file to set as target.
     */
    public void setTgtFile(JFSFile file) {
        tgtFile = file;
    }


    /**
     * @return Returns the parent element of the current element which reflects the file system structure. The parent
     * element can only be null, if the current element is the root element.
     */
    public final JFSElement getParent() {
        return parent;
    }


    /**
     * @return Returns the children of the current element which reflects the file system structure. The children are
     * null, if the current element is not a directory or the directory it represents is empty.
     */
    public final List<JFSElement> getChildren() {
        return children;
    }


    /**
     * Adds a child to the element.
     *
     * @param child
     * The child to add.
     */
    public final void addChild(JFSElement child) {
        if (children==null) {
            children = new ArrayList<>();
        }

        children.add(child);
    }


    /**
     * @return Computes whether source and target files are directories.
     */
    public final boolean isDirectory() {
        return directory;
    }


    /**
     * @return Returns the state of the element.
     */
    public final ElementState getState() {
        return state;
    }


    /**
     * @return Returns whether the action was manually set.
     */
    public final boolean isManuallySetAction() {
        return manuallySetAction;
    }


    /**
     * Determines whether the action was manually set.
     *
     * @param manuallySetAction True, if it was manually set.
     */
    public void setManuallySetAction(boolean manuallySetAction) {
        this.manuallySetAction = manuallySetAction;
    }


    /**
     * @return Returns the action that has to be performed for the JFS element.
     */
    public final SyncAction getAction() {
        return action;
    }


    /**
     * Sets the action that has to be performed for the JFS element.
     *
     * @see JFSSyncModes
     * @param action The action to set.
     */
    public void setAction(SyncAction action) {
        this.action = action;
    }


    /**
     * @return Returns whether the action of the JFS element is active.
     */
    public final boolean isActive() {
        return active;
    }


    /**
     * Sets whether the action of the JFS element is active.
     *
     * @param isActive True, if the action is active.
     */
    public void setActive(boolean isActive) {
        this.active = isActive;
    }


    /**
     * @return Returns whether the element is viewed.
     */
    public final boolean isViewed() {
        return viewed;
    }


    /**
     * Sets whether the element is viewed.
     *
     * @param isViewed True, if the element is viewed.
     */
    public final void setViewed(boolean isViewed) {
        this.viewed = isViewed;
    }


    /**
     * @return Returns whether the element is a root element.
     */
    public final boolean isRoot() {
        return (parent==this);
    }


    /**
     * @return Returns the name of the element, which has to be the same for source and target if both files are equal.
     * If the source is equal to null the relative path of the target is returned, and vice versa. According to
     * the constructor of this element, it is not allowed that both files are equal to null.
     */
    public String getName() {
        if (srcFile!=null) {
            return srcFile.getName();
        } // if
        return tgtFile.getName();
    }


    /**
     * @return Returns the relative path of the element, which has to be the same for source and target if both files
     * are equal. If the source is equal to null the relative path of the target is returned, and vice versa.
     * According to the constructor of this element, it is not allowed that both files are equal to null.
     */
    public String getRelativePath() {
        if (srcFile!=null) {
            return srcFile.getRelativePath();
        }
        return tgtFile.getRelativePath();
    }


    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(JFSElement e) {
        return getRelativePath().compareTo(e.getRelativePath());
    }


    /**
     * Compares two time stamps taking into account the specified granularity of the configuration object.
     *
     * @param time1 The first time stamp.
     * @param time2 The second time stamp.
     * @return Returns a positive number if the first is newer than the second time stamp, 0 if both are equal, and a
     * negative number if the second time stamp is newer than the first one.
     */
    public static long compareToTime(long time1, long time2) {
        return (time1-time2)/JFSConfig.getInstance().getGranularity();
    }


    /**
     * Returns the valid actions for the JFS element.
     *
     * @return A vector of valid synchronization actions.
     */
    public Set<SyncAction> getValidActions() {
        Set<SyncAction> validActions = new TreeSet<>();

        if (state==ElementState.IS_ROOT) {
            return validActions;
        }

        validActions.add(SyncAction.NOP);

        if (srcFile!=null) {
            validActions.add(SyncAction.COPY_SRC);
        } else {
            validActions.add(SyncAction.DELETE_TGT);
        }

        if (tgtFile!=null) {
            validActions.add(SyncAction.COPY_TGT);
        } else {
            validActions.add(SyncAction.DELETE_SRC);
        }

        if (srcFile!=null&&tgtFile!=null) {
            validActions.add(SyncAction.DELETE_SRC_AND_TGT);
        }

        return validActions;
    }

}
