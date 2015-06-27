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

import java.awt.Frame;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jfs.conf.JFSText;
import jfs.sync.JFSElement;
import jfs.sync.JFSFile;
import jfs.sync.JFSFormatter;
import jfs.sync.JFSTable;


/**
 * This class is responsible for displaying the synchronization table.
 *
 * @author Jens Heidrich
 * @version $Id: JFSTableView.java,v 1.3 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSTableView extends AbstractTableModel {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 45L;

    /**
     * The corresponding JTable.
     */
    private final JTable table;


    /**
     * The default constructor just performs some initialization work.
     *
     * @param parent
     * The parent frame.
     */
    public JFSTableView(Frame parent) {
        // Create column model:
        JFSText t = JFSText.getInstance();
        DefaultTableColumnModel cm = new DefaultTableColumnModel();

        TableColumn srcFile = new TableColumn(0, 205);
        srcFile.setHeaderValue(t.get("compTable.src"));
        cm.addColumn(srcFile);

        TableColumn srcDate = new TableColumn(1, 120);
        srcDate.setHeaderValue(t.get("compTable.date"));
        cm.addColumn(srcDate);

        TableColumn srcSize = new TableColumn(2, 60);
        srcSize.setHeaderValue(t.get("compTable.size"));
        cm.addColumn(srcSize);

        TableColumn comp = new TableColumn(3, 30);
        comp.setHeaderValue(t.get("compTable.action"));
        cm.addColumn(comp);

        TableColumn tgtFile = new TableColumn(4, 205);
        tgtFile.setHeaderValue(t.get("compTable.tgt"));
        cm.addColumn(tgtFile);

        TableColumn tgtDate = new TableColumn(5, 120);
        tgtDate.setHeaderValue(t.get("compTable.date"));
        cm.addColumn(tgtDate);

        TableColumn tgtSize = new TableColumn(6, 60);
        tgtSize.setHeaderValue(t.get("compTable.size"));
        cm.addColumn(tgtSize);

        // Create table:
        table = new JTable(this, cm);
        JFSTableRenderer renderer = new JFSTableRenderer(table);
        table.setDefaultRenderer(String.class, renderer);
        table.getTableHeader().setReorderingAllowed(false);

        // Register mouse listener:
        table.addMouseListener(new JFSTableListener(parent, table, renderer.getActionIconNames()));

        // Set row height:
        table.setRowHeight(table.getRowHeight()+2);
    }


    /**
     * @see TableModel#getRowCount()
     */
    @Override
    public final int getRowCount() {
        return JFSTable.getInstance().getViewSize();
    }


    /**
     * @see TableModel#getColumnCount()
     */
    @Override
    public final int getColumnCount() {
        return 7;
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

        String entry = "";
        JFSElement element = JFSTable.getInstance().getViewElement(row);
        JFSFile file = null;

        if (column>=0&&column<=2) {
            file = element.getSrcFile();
        } else {
            if (column>=4&&column<=6) {
                file = element.getTgtFile();
            }
        }

        if (file!=null) {
            switch (column) {
                case 0:
                case 4:
                    if (element.isDirectory()) {
                        entry = file.getPath();
                    } else {
                        entry = file.getName();
                    }

                    break;
                case 1:
                case 5:
                    entry = JFSFormatter.getLastModified(file);
                    break;
                case 2:
                case 6:
                    entry = JFSFormatter.getLength(file);
                    break;
            }
        }

        if (column==3) {
            entry = String.valueOf(element.getAction());
        }

        return entry;
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
        return table;
    }

}
