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

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jfs.conf.JFSHistory;
import jfs.conf.JFSHistoryManager;
import jfs.conf.JFSText;

/**
 * This class is responsible for displaying the stored histories.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHistoryTable.java,v 1.6 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSHistoryTable extends AbstractTableModel {
    /** The UID. */
    private static final long serialVersionUID = 567343L;

    /** The corresponding JTable. */
    private final JTable table;

    /** The vector of history items to display. */
    private final Vector<JFSHistory> v = JFSHistoryManager.getInstance().getHistories();


    /**
     * The default constructor just performs some initialization work.
     */
    public JFSHistoryTable() {
        // Create column model:
        JFSText t = JFSText.getInstance();
        DefaultTableColumnModel cm = new DefaultTableColumnModel();

        TableColumn source = new TableColumn(0, 110);
        source.setHeaderValue(t.get("history.table.src"));
        cm.addColumn(source);

        TableColumn target = new TableColumn(1, 110);
        target.setHeaderValue(t.get("history.table.tgt"));
        cm.addColumn(target);

        TableColumn date = new TableColumn(2, 60);
        date.setHeaderValue(t.get("history.table.date"));
        cm.addColumn(date);

        TableColumn stored = new TableColumn(3, 10);
        stored.setHeaderValue(t.get("history.table.stored"));
        cm.addColumn(stored);

        // Create table:
        table = new JTable(this, cm);
        table.getTableHeader().setReorderingAllowed(false);
    }


    /**
     * @see TableModel#getRowCount()
     */
    @Override
    public final int getRowCount() {
        return v.size();
    }


    /**
     * @see TableModel#getColumnCount()
     */
    @Override
    public final int getColumnCount() {
        return 4;
    }


    /**
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public final Object getValueAt(int row, int column) {
        if (row<0||row>=getRowCount()||column<0||column>=getColumnCount())
            return null;

        JFSHistory h = v.get(row);

        switch (column) {
        case 0:
            return h.getPair().getSrc();
        case 1:
            return h.getPair().getTgt();
        case 2:
            return h.getDateAsString();
        case 3:
            return h.getFileName()!=null;
        }

        return "";
    }


    /**
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public final Class<?> getColumnClass(int column) {
        if (column==3) {
            return Boolean.class;
        }
        return String.class;
    }


    /**
     * @return Returns the JTable object.
     */
    public final JTable getJTable() {
        return table;
    }
}