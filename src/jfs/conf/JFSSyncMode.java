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

import java.util.HashMap;
import java.util.Map;
import jfs.sync.JFSElement;
import jfs.sync.JFSElement.ElementState;
import jfs.sync.JFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class specifies a single synchronization mode.
 *
 * @author Jens Heidrich
 * @version $Id: JFSSyncMode.java,v 1.12 2007/03/29 13:20:54 heidrich Exp $
 */
public class JFSSyncMode {

    private static final Logger LOG = LoggerFactory.getLogger(JFSSyncMode.class);


    /**
     * Actions for used for all modes.
     */
    public enum SyncAction {

        NOP_ROOT("syncAction.nopRoot"), NOP("syncAction.nop"), COPY_SRC("syncAction.copySrc"), COPY_TGT(
                "syncAction.copyTgt"), DELETE_SRC("syncAction.deleteSrc"), DELETE_TGT("syncAction.deleteTgt"), DELETE_SRC_AND_TGT(
                        "syncAction.deleteSrcAndTgt"), ASK_LENGTH_INCONSISTENT("syncAction.askLengthInconsistent"), ASK_FILES_GT_HISTORY(
                        "syncAction.askFilesGtHistory"), ASK_FILES_NOT_IN_HISTORY("syncAction.askFilesNotInHistory");

        private String name;


        SyncAction(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }

    }

    /**
     * The identifier of the mode.
     */
    private final int id;

    /**
     * The string alias of the mode.
     */
    private final String alias;

    /**
     * Assigns actions to states of an element of the comparison table.
     */
    private final Map<ElementState, SyncAction> stateActions = new HashMap<>();

    /**
     * Determines whether the actions of the mode should be automatically set based on the synchronization history.
     */
    private boolean automatic = false;


    /**
     * Creates a new mode.
     *
     * @param id
     * The identifier to use.
     * @param alias
     * The alias to use.
     */
    public JFSSyncMode(int id, String alias) {
        this.id = id;
        this.alias = alias;
    }


    /**
     * Returns the action for a specific state of an element of the comparison table. If nothing is specified regarding
     * the given state, the default action (no operation) is returned.
     *
     * @param state
     * The state of the element of the comparisn table.
     * @return The corresponding action for the state.
     */
    public SyncAction getAction(ElementState state) {
        if (stateActions.containsKey(state)) {
            return stateActions.get(state);
        }
        return SyncAction.NOP;
    }


    /**
     * Sets the action for a specific state of an element of the comparison table.
     *
     * @param state
     * The state of the element of the comparisn table.
     * @param action
     * The corresponding action for the state.
     */
    public void setAction(ElementState state, SyncAction action) {
        stateActions.put(state, action);
    }


    /**
     * @return Returns whether the automatic option is set.
     */
    public boolean isAutomatic() {
        return automatic;
    }


    /**
     * @param automatic
     * Sets the automatic option. If this option is set, all other actions are ignorned and determined
     * automatically based on the synchronization history.
     */
    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
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


    /**
     * Computes the action that has to performed for an entry of the comparison table according to this synchronization
     * mode and a file history.
     *
     * @param element
     * The element to compute the action for.
     */
    public final void computeAction(JFSElement element) {
        if (element.isManuallySetAction()&&JFSConfig.getInstance().isKeepUserActions()) {
            return;
        }

        if (!isAutomatic()) {
            // Add actions to comparison tables according to the chosen
            // mode:
            element.setAction(getAction(element.getState()));
        } else {
            // Add basic actions based on the stored history:
            computeBasicAction(element);
            if (LOG.isInfoEnabled()) {
                LOG.info("computeAction() isAutomatic "+element.getAction());
            } // if

            // Check whether the parent is also copied if the children are
            // copied:
            JFSElement parent = element.getParent();
            if (element.getAction()==SyncAction.COPY_SRC&&parent.getAction()==SyncAction.DELETE_SRC) {
                parent.setAction(SyncAction.COPY_SRC);
            } else if (element.getAction()==SyncAction.COPY_TGT&&parent.getAction()==SyncAction.DELETE_TGT) {
                parent.setAction(SyncAction.COPY_TGT);
            } else if ((element.getAction()==SyncAction.COPY_SRC||element.getAction()==SyncAction.COPY_TGT)
                    &&parent.getAction()==SyncAction.DELETE_SRC_AND_TGT) {
                parent.setAction(SyncAction.NOP);
            }
        }
    }


    /**
     * Computes the action that has to performed for an entry of the comparison table according to this synchronization
     * mode and a history of former synchronized files and directories.
     *
     * @param element
     * The element to compute the basic action for.
     */
    private final void computeBasicAction(JFSElement element) {
        // Analyze comparison table:
        element.setAction(SyncAction.NOP);
        JFSElement.ElementState s = element.getState();

        // Ask user, when length is inconsistent and continue:
        if (s==ElementState.LENGTH_INCONSISTENT) {
            element.setAction(SyncAction.ASK_LENGTH_INCONSISTENT);
            return;
        }

        // Use history, if a corresponding history item is available, and
        // else, merge the structures:
        JFSHistory history = element.getRoot().getHistory();
        if (LOG.isInfoEnabled()) {
            LOG.info("computeBasicAction() history="+history);
        } // fi
        if (history==null) {
            merge(element);
        } else {
            JFSHistoryItem h = history.getHistory(element);
            if (LOG.isInfoEnabled()) {
                LOG.info("computeBasicAction("+element.getRelativePath()+") historyItem="+h);
            } // if
            if (h!=null) {
                useHistory(element, h);
            } else {
                merge(element);
            }
        }
    }


    /**
     * The current analyzed element is part of the history and matches a history item. In this case, the history can be
     * used in order to determine the correct actions.
     *
     * @param current
     * The element to analyze.
     * @param h
     * The corresponding history element.
     */
    private void useHistory(JFSElement current, JFSHistoryItem h) {
        JFSFile src = current.getSrcFile();
        JFSFile tgt = current.getTgtFile();
        JFSElement.ElementState s = current.getState();
        if (s==ElementState.SRC_GT_TGT) {
            if (JFSElement.compareToTime(tgt.getLastModified(), h.getLastModified())>0) {
                // Both files are newer than the history:
                current.setAction(SyncAction.ASK_FILES_GT_HISTORY);
            } else {
                // The source file is newer:
                current.setAction(SyncAction.COPY_SRC);
            }
        } else if (s==ElementState.TGT_GT_SRC) {
            if (JFSElement.compareToTime(src.getLastModified(), h.getLastModified())>0) {
                // Both files are newer than the history:
                current.setAction(SyncAction.ASK_FILES_GT_HISTORY);
            } else {
                // The source file is newer:
                current.setAction(SyncAction.COPY_TGT);
            }
        } else if (s==ElementState.SRC_IS_NULL) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("useHistory() "+s+" "+h.getLastModified()+" / "+tgt.getLastModified());
            } // if
            if (JFSElement.compareToTime(tgt.getLastModified(), h.getLastModified())>0) {
                // The target file is newer than the history:
                current.setAction(SyncAction.COPY_TGT);
            } else {
                // The file on source side was removed once:
                current.setAction(SyncAction.DELETE_TGT);
            }
        } else if (s==ElementState.TGT_IS_NULL) {
            if (JFSElement.compareToTime(src.getLastModified(), h.getLastModified())>0) {
                // The source file is newer than the history:
                current.setAction(SyncAction.COPY_SRC);
            } else {
                // The file on target side was removed once:
                current.setAction(SyncAction.DELETE_SRC);
            }
        }
    }


    /**
     * A new element is found, which was not in the history before. In this case, we have no history information at all
     * and therefore perform a simple merge.
     *
     * @param current
     * The element to analyze.
     */
    private void merge(JFSElement current) {
        JFSElement.ElementState s = current.getState();
        if (s==ElementState.SRC_GT_TGT) {
            // The source file is newer:
            current.setAction(SyncAction.ASK_FILES_NOT_IN_HISTORY);
        } else if (s==ElementState.TGT_GT_SRC) {
            // The target file is newer:
            current.setAction(SyncAction.ASK_FILES_NOT_IN_HISTORY);
        } else if (s==ElementState.SRC_IS_NULL) {
            // A new file was added to the target side:
            current.setAction(SyncAction.COPY_TGT);
        } else if (s==ElementState.TGT_IS_NULL) {
            // A new file was added to the source side:
            current.setAction(SyncAction.COPY_SRC);
        }
    }

}
