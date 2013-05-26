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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jfs.conf.JFSText;
import jfs.sync.JFSElement;
import jfs.sync.JFSFormatter;

/**
 * This dialog views some file properties.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSPropertiesView.java,v 1.5 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSPropertiesView extends JDialog implements ActionListener {
    /** The UID. */
    private static final long serialVersionUID = 64564564L;

    /** Determines the I18N keys for boolean values. */
    public enum BooleanText {
        TRUE("general.true"), FALSE("general.false");
        private String name;


        BooleanText(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }


        public static BooleanText getBooleanText(Boolean b) {
            if (b) {
                return TRUE;
            }
            return FALSE;
        }
    }


    /**
     * Shows properties of a JFS element.
     * 
     * @param parent
     *            The parent frame.
     * @param element
     *            The element to view.
     */
    public JFSPropertiesView(Frame parent, JFSElement element) {
        super(parent, true);

        JFSText t = JFSText.getInstance();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("fileProps.title"));
        setResizable(true);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        Vector<String> header = new Vector<String>();
        header.add(t.get("fileProps.table.property"));
        header.add(t.get("fileProps.table.value"));

        Vector<Vector<String>> data = new Vector<Vector<String>>();
        addProperty(data, "fileProps.name", element.getName());
        addProperty(data, "fileProps.relativePath", element.getRelativePath());
        if (element.getSrcFile()!=null) {
            addProperty(data, "fileProps.src.path", element.getSrcFile().getPath());
            if ( !element.isDirectory()) {
                addProperty(data, "fileProps.src.lastModified", JFSFormatter.getLastModified(element.getSrcFile()));
                addProperty(data, "fileProps.src.length", JFSFormatter.getLength(element.getSrcFile()));
                addProperty(data, "fileProps.src.canRead", t.get(BooleanText.getBooleanText(
                        element.getSrcFile().canRead()).getName()));
                addProperty(data, "fileProps.src.canWrite", t.get(BooleanText.getBooleanText(
                        element.getSrcFile().canWrite()).getName()));
            }
        }
        if (element.getTgtFile()!=null) {
            addProperty(data, "fileProps.tgt.path", element.getTgtFile().getPath());
            if ( !element.isDirectory()) {
                addProperty(data, "fileProps.tgt.lastModified", JFSFormatter.getLastModified(element.getTgtFile()));
                addProperty(data, "fileProps.tgt.length", JFSFormatter.getLength(element.getTgtFile()));
                addProperty(data, "fileProps.tgt.canRead", t.get(BooleanText.getBooleanText(
                        element.getTgtFile().canRead()).getName()));
                addProperty(data, "fileProps.tgt.canWrite", t.get(BooleanText.getBooleanText(
                        element.getTgtFile().canWrite()).getName()));
            }
        }
        addProperty(data, "fileProps.action", t.get(element.getAction().getName()));

        JTable table = new JTable(data, header);
        table.setEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new JScrollPane(table));
        panel.setPreferredSize(new Dimension(350, 200));

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.close", "button.close", this));

        // Add all panels:
        cp.add(panel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        pack();
        JFSSupport.center(parent, this);
        this.setVisible(true);
    }


    /**
     * Adds a new property to the list of property data.
     * 
     * @param data
     *            The list to add to.
     * @param propertyId
     *            The indetifier of the property.
     * @param value
     *            The value to add.
     */
    private final void addProperty(Vector<Vector<String>> data, String propertyId, String value) {
        JFSText t = JFSText.getInstance();
        Vector<String> row = new Vector<String>();
        data.add(row);
        row.add(t.get(propertyId));
        row.add(value);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if (cmd.equals("button.close")) {
            setVisible(false);
            dispose();
        }
    }
}