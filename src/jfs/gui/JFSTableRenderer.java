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

package jfs.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import jfs.conf.JFSConst;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSText;
import jfs.sync.JFSElement;
import jfs.sync.JFSElement.ElementState;
import jfs.sync.JFSFile;
import jfs.sync.JFSTable;

/**
 * This class is responsible for rendering the synchronization table.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSTableRenderer.java,v 1.4 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSTableRenderer implements TableCellRenderer {
    /** Color definition for files that have to be copied from source. */
    private static final Color COPY_SRC = new Color(130, 255, 130);

    /** Color definition for files that have to be copied from target. */
    private static final Color COPY_TGT = new Color(130, 130, 255);

    /** Color definition for files that have to be deleted. */
    private static final Color DELETE = new Color(255, 130, 130);

    /** Color definition for a row that contains a root element. */
    private static final Color ROOT = new Color(200, 200, 255);

    /** Color definition for a row that contains directories. */
    private static final Color DIRECTORY = new Color(255, 255, 200);

    /** Color definition for manually set actions. */
    private static final Color MANUALLY_SET_ACTION = new Color(0, 0, 255);

    /** Color definition for selected manually set actions. */
    private static final Color MANUALLY_SET_ACTION_SELECTED = new Color(220, 220, 255);

    /** Color definition for a warning. */
    private static final Color WARNING = new Color(255, 0, 0);

    /** Color definition for a selected warning. */
    private static final Color WARNING_SELECTED = new Color(255, 220, 220);

    /** An adapted JLabel object for our cell renderer component. */
    protected JLabel cell;

    /** Stores the names for the icons used for displaying actions. */
    private HashMap<SyncAction, String> actionIconNames = new HashMap<SyncAction, String>();

    /** Stores the icons used for displaying the action for a JFS element. */
    private HashMap<SyncAction, JLabel> actionIcons = new HashMap<SyncAction, JLabel>();

    /** Stores the icons used for displaying the action for a JFS element. */
    private HashMap<SyncAction, Color> actionBackgrounds = new HashMap<SyncAction, Color>();


    /**
     * The default constructor just performs some initialization work.
     * 
     * @param table
     *            The table to render.
     */
    public JFSTableRenderer(JTable table) {
        // Set action icons:
        JFSConst jfsConst = JFSConst.getInstance();
        actionIconNames.put(SyncAction.NOP_ROOT, "jfs.icon.root");
        actionIconNames.put(SyncAction.NOP, "jfs.icon.idle");
        actionIconNames.put(SyncAction.COPY_SRC, "jfs.icon.copySrc");
        actionIconNames.put(SyncAction.COPY_TGT, "jfs.icon.copyTgt");
        actionIconNames.put(SyncAction.DELETE_SRC, "jfs.icon.delete");
        actionIconNames.put(SyncAction.DELETE_TGT, "jfs.icon.delete");
        actionIconNames.put(SyncAction.DELETE_SRC_AND_TGT, "jfs.icon.delete");
        actionIconNames.put(SyncAction.ASK_LENGTH_INCONSISTENT, "jfs.icon.question");
        actionIconNames.put(SyncAction.ASK_FILES_GT_HISTORY, "jfs.icon.question");
        actionIconNames.put(SyncAction.ASK_FILES_NOT_IN_HISTORY, "jfs.icon.question");

        for (SyncAction a : actionIconNames.keySet()) {
            JLabel icon = new JLabel(new ImageIcon(jfsConst.getIconUrl(actionIconNames.get(a))));
            icon.setOpaque(true);
            icon.setHorizontalAlignment(JLabel.CENTER);
            actionIcons.put(a, icon);
        }

        // Set action backgrounds:
        Color background = table.getBackground();
        actionBackgrounds.put(SyncAction.NOP_ROOT, background);
        actionBackgrounds.put(SyncAction.NOP, background);
        actionBackgrounds.put(SyncAction.COPY_SRC, COPY_SRC);
        actionBackgrounds.put(SyncAction.COPY_TGT, COPY_TGT);
        actionBackgrounds.put(SyncAction.DELETE_SRC, DELETE);
        actionBackgrounds.put(SyncAction.DELETE_TGT, DELETE);
        actionBackgrounds.put(SyncAction.DELETE_SRC_AND_TGT, DELETE);
        actionBackgrounds.put(SyncAction.ASK_LENGTH_INCONSISTENT, background);
        actionBackgrounds.put(SyncAction.ASK_FILES_GT_HISTORY, background);
        actionBackgrounds.put(SyncAction.ASK_FILES_NOT_IN_HISTORY, background);

        // Create table cell label:
        cell = new JLabel();
        cell.setOpaque(true);
        cell.setFont(table.getFont());
    }


    /**
     * @return Returns the action icon names.
     */
    public HashMap<SyncAction, String> getActionIconNames() {
        return actionIconNames;
    }


    /**
     * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (row>=JFSTable.getInstance().getViewSize()||row<0) {
            return null;
        }

        // Determine corresponding element of the comparison table:
        JFSElement element = JFSTable.getInstance().getViewElement(row);
        JFSFile jfsFile = null;
        if (column==0) {
            jfsFile = element.getSrcFile();
        } else if (column==4) {
            jfsFile = element.getTgtFile();
        }

        // Get the right component first and set alignment:
        JComponent component;
        if (column==3) {
            component = actionIcons.get(element.getAction());
            if (component==null) {
                component = actionIcons.get(SyncAction.NOP);
            }
        } else {
            component = cell;

            cell.setText(String.valueOf(value));
            if (column==2||column==6) {
                cell.setHorizontalAlignment(JLabel.RIGHT);
            } else {
                cell.setHorizontalAlignment(JLabel.LEFT);
            }

            cell.setIcon(null);
            if (column==0||column==4) {
                if (jfsFile!=null&&jfsFile.exists()) {
                    try {
                        Icon icon = UIManager.getIcon(jfsFile.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
                        cell.setIcon(icon);
                    } catch (Exception e) {
                        cell.setIcon(null);
                    }
                }
            }
        }

        // Set tool tip text for component:
        if (column==3) {
            component.setToolTipText(JFSText.getInstance().get(element.getAction().getName()));
        } else {
            component.setToolTipText(null);
        }

        // Set foreground and background colors:
        boolean warning = element.getAction()==SyncAction.ASK_LENGTH_INCONSISTENT
                ||element.getAction()==SyncAction.ASK_FILES_GT_HISTORY
                ||element.getAction()==SyncAction.ASK_FILES_NOT_IN_HISTORY;
        if (isSelected) {
            if (warning) {
                component.setForeground(WARNING_SELECTED);
            } else if (element.isManuallySetAction()) {
                component.setForeground(MANUALLY_SET_ACTION_SELECTED);
            } else {
                component.setForeground(table.getSelectionForeground());
            }
            component.setBackground(table.getSelectionBackground());
        } else {
            if (warning) {
                component.setForeground(WARNING);
            } else if (element.isManuallySetAction()) {
                component.setForeground(MANUALLY_SET_ACTION);
            } else {
                component.setForeground(table.getForeground());
            }

            // Color the action column according to the action performed:
            component.setBackground(table.getBackground());
            if (column==3) {
                component.setBackground(actionBackgrounds.get(element.getAction()));
            }

            // If the background was not changed look for specific rows:
            if (component.getBackground().equals(table.getBackground())) {
                if (element.getState()==ElementState.IS_ROOT) {
                    // Set background for root rows:
                    component.setBackground(ROOT);
                } else if (element.isDirectory()) {
                    // Set background for directory rows:
                    component.setBackground(DIRECTORY);
                }
            }
        }

        if ( !element.isActive()) {
            component.setEnabled(false);
        } else {
            component.setEnabled(true);
        }

        return component;
    }
}