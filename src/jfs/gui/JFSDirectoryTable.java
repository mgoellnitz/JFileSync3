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
import java.io.File;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jfs.conf.JFSConfig;
import jfs.conf.JFSText;
import jfs.sync.JFSFileProducerManager;


/**
 * This class is responsible for displaying the directory pairs of a configuration object.
 *
 * @author Jens Heidrich
 * @version $Id: JFSDirectoryTable.java,v 1.15 2007/06/06 19:51:33 heidrich Exp $
 */
public class JFSDirectoryTable extends AbstractTableModel implements TableCellRenderer {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 48L;

    /**
     * Color definition for an invalid directory file.
     */
    private static final Color INVALID_DIR = new Color(255, 0, 0);

    /**
     * Color definition for a selected invalid directory file.
     */
    private static final Color INVALID_DIR_SELECTED = new Color(255, 220, 220);

    /**
     * Color definition for a external files.
     */
    private static final Color EXTERNAL = new Color(0, 0, 255);

    /**
     * Color definition for a selected external files.
     */
    private static final Color EXTERNAL_SELECTED = new Color(220, 220, 255);

    /**
     * The object with information that should be displayed by the table.
     */
    private JFSConfig config;

    /**
     * The corresponding JTable.
     */
    private final JTable localTable;

    /**
     * An adapted JLabel object for our cell renderer component.
     */
    protected JLabel cell;


    /**
     * The default constructor just performs some initialization work.
     *
     * @param config
     * The configuration object to display.
     */
    public JFSDirectoryTable(final JFSConfig config) {
        this.config = config;

        // Create column model:
        JFSText t = JFSText.getInstance();
        DefaultTableColumnModel cm = new DefaultTableColumnModel();

        JTextField editor = new JTextField();
        editor.setBorder(new EmptyBorder(0, 0, 0, 0));

        TableColumn source = new TableColumn(0, 100);
        source.setHeaderValue(t.get("profile.dir.table.src"));
        source.setCellEditor(new DefaultCellEditor(editor));
        cm.addColumn(source);

        TableColumn target = new TableColumn(1, 100);
        target.setHeaderValue(t.get("profile.dir.table.tgt"));
        target.setCellEditor(new DefaultCellEditor(editor));
        cm.addColumn(target);

        // Create table and cell label:
        localTable = new JTable(this, cm);
        localTable.setDefaultRenderer(String.class, this);
        localTable.getTableHeader().setReorderingAllowed(false);
        cell = new JLabel();
        cell.setOpaque(true);
        cell.setFont(localTable.getFont());
        cell.setHorizontalAlignment(JLabel.LEFT);
    }


    /**
     * @see TableModel#getRowCount()
     */
    @Override
    public final int getRowCount() {
        return config.getDirectoryList().size();
    }


    /**
     * @see TableModel#getColumnCount()
     */
    @Override
    public final int getColumnCount() {
        return 2;
    }


    /**
     * @see TableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }


    /**
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public final Object getValueAt(int row, int column) {
        if (row<0||row>=getRowCount()||column<0||column>=getColumnCount()) {
            return null;
        }

        switch (column) {
            case 0:
                return config.getDirectoryList().get(row).getSrc();
            default:
                return config.getDirectoryList().get(row).getTgt();
        }
    }


    /**
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public final Class<?> getColumnClass(int column) {
        return String.class;
    }


    /**
     * @return Returns the JTable object.
     */
    public final JTable getJTable() {
        return localTable;
    }


    /**
     * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
        JFSText t = JFSText.getInstance();

        String path = (String) value;

        // Adapt cell label individualy:
        int idx = path.indexOf(':');
        String prefix = (idx>1) ? path.substring(0, idx) : "xyz";
        String dir = (idx>1) ? path.substring(idx+3) : path;

        File file = new File(dir);
        cell.setText(path);
        cell.setToolTipText(null);

        if (isSelected) {
            cell.setForeground(table.getSelectionForeground());
            cell.setBackground(table.getSelectionBackground());
        } else {
            cell.setForeground(table.getForeground());
            cell.setBackground(table.getBackground());
        }

        if (JFSFileProducerManager.getInstance().getSchemes().contains(prefix)&&dir.matches("[a-z][a-z][a-z]*://.*")) {
            if (isSelected) {
                cell.setForeground(EXTERNAL_SELECTED);
            } else {
                cell.setForeground(EXTERNAL);
            } // if
            cell.setText(path.substring(prefix.length()+3));
            cell.setToolTipText(t.get("profile.dir.message.external"));
        } else if (!file.exists()||!file.isDirectory()) {
            if (isSelected) {
                cell.setForeground(INVALID_DIR_SELECTED);
            } else {
                cell.setForeground(INVALID_DIR);
            }
            cell.setToolTipText(t.get("profile.dir.message.invalid"));
        }

        return cell;
    }

}
