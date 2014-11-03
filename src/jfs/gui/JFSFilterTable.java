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

import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jfs.conf.JFSFilter;
import jfs.conf.JFSFilter.FilterRange;
import jfs.conf.JFSFilter.FilterType;
import jfs.conf.JFSText;

/**
 * A table displaying filters (to include/exclude files).
 * 
 * @author Jens Heidrich
 * @version $Id: JFSFilterTable.java,v 1.6 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSFilterTable extends AbstractTableModel {
    
    /** The UID. */
    private static final long serialVersionUID = 100L;

    /** The object with information that should be displayed by the table. */
    private final List<JFSFilter> filters;

    /** The corresponding JTable. */
    private final JTable table;


    /**
     * The default constructor.
     * 
     * @param filters
     *            The filters to display.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public JFSFilterTable(List<JFSFilter> filters) {
        this.filters = filters;

        // Create column model:
        JFSText t = JFSText.getInstance();
        DefaultTableColumnModel cm = new DefaultTableColumnModel();

        JCheckBox cb = new JCheckBox();
        JComboBox typeCombo = new JComboBox();
        for (FilterType fType : FilterType.values()) {
            typeCombo.addItem(t.get(fType.getName()));
        }
        JComboBox rangeCombo = new JComboBox();
        for (FilterRange fRange : FilterRange.values()) {
            rangeCombo.addItem(t.get(fRange.getName()));
        }
        cb.setHorizontalAlignment(JCheckBox.CENTER);
        JTextField editor = new JTextField();
        editor.setBorder(new EmptyBorder(0, 0, 0, 0));

        TableColumn active = new TableColumn(0, 5);
        active.setHeaderValue(t.get("profile.filter.active"));
        active.setCellEditor(new DefaultCellEditor(cb));
        cm.addColumn(active);

        TableColumn type = new TableColumn(1, 10);
        type.setHeaderValue(t.get("profile.filter.type"));
        type.setCellEditor(new DefaultCellEditor(typeCombo));
        cm.addColumn(type);

        TableColumn range = new TableColumn(2, 10);
        range.setHeaderValue(t.get("profile.filter.range"));
        range.setCellEditor(new DefaultCellEditor(rangeCombo));
        cm.addColumn(range);

        TableColumn filter = new TableColumn(3, 75);
        filter.setHeaderValue(t.get("profile.filter.regexp"));
        filter.setCellEditor(new DefaultCellEditor(editor));
        cm.addColumn(filter);

        // Create table:
        table = new JTable(this, cm);
        table.getTableHeader().setReorderingAllowed(false);
    }


    /**
     * @see TableModel#getRowCount()
     */
    @Override
    public final int getRowCount() {
        return filters.size();
    }


    /**
     * @see TableModel#getColumnCount()
     */
    @Override
    public final int getColumnCount() {
        return 4;
    }


    /**
     * @see TableModel#isCellEditable(int, int)
     */
    @Override
    public final boolean isCellEditable(int row, int column) {
        return true;
    }


    /**
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public final Object getValueAt(int row, int column) {
        if (row<0||row>=getRowCount()||column<0||column>=getColumnCount()) {
            return null;
        }

        JFSText t = JFSText.getInstance();
        switch (column) {
        case 0:
            return filters.get(row).isActive();
        case 1:
            return t.get(filters.get(row).getType().getName());
        case 2:
            return t.get(filters.get(row).getRange().getName());
        default:
            return filters.get(row).getFilter();
        }
    }


    /**
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public final Class<?> getColumnClass(int column) {
        switch (column) {
        case 0:
            return Boolean.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        default:
            return String.class;
        }
    }


    /**
     * @see TableModel#setValueAt(Object, int, int)
     */
    @Override
    public final void setValueAt(Object value, int row, int column) {
        if (row<0||row>=getRowCount()||column<0||column>=getColumnCount()) {
            return;
        }

        JFSText t = JFSText.getInstance();
        switch (column) {
        case 0:
            filters.get(row).setActive((Boolean)value);
            break;
        case 1:
            String type = String.valueOf(value);
            for (FilterType fType : FilterType.values()) {
                if (type.equals(t.get(fType.getName()))) {
                    filters.get(row).setType(fType);
                }
            }
            break;
        case 2:
            String range = String.valueOf(value);
            for (FilterRange fRange : FilterRange.values()) {
                if (range.equals(t.get(fRange.getName()))) {
                    filters.get(row).setRange(fRange);
                }
            }
            break;
        default:
            filters.get(row).setFilter((String)value);
            break;
        }
    }


    /**
     * @return Returns the filters.
     */
    public final List<JFSFilter> getFilters() {
        return filters;
    }


    /**
     * @return Returns the JTable object.
     */
    public final JTable getJTable() {
        return table;
    }
}