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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jfs.conf.JFSConfig;
import jfs.conf.JFSFilter;
import jfs.conf.JFSFilter.FilterRange;
import jfs.conf.JFSFilter.FilterType;
import jfs.conf.JFSText;

/**
 * This dialog manages filter settings.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSConfigFilterView.java,v 1.10 2006/08/28 11:31:54 heidrich Exp $
 */
public class JFSConfigFilterView extends JDialog implements ActionListener, ListSelectionListener {
    /** The UID. */
    private static final long serialVersionUID = 49L;

    /** The configuration object to modify. */
    private final JFSConfig config;

    /** Determines whether to handle include or exclude filters. */
    private boolean isIncludeFilter;

    /** The table of filters to change. */
    private JFSFilterTable filterTable;

    /** Number of filters. */
    private JLabel filterLabel;

    /** The up button. */
    private JButton upButton;

    /** The down button. */
    private JButton downButton;

    /** The remove button. */
    private JButton removeButton;


    /**
     * Initializes the config view.
     * 
     * @param dialog
     *            The main frame.
     * @param config
     *            The configuration to change.
     * @param isIncludeFilter
     *            Determines the type of the dialog.
     */
    public JFSConfigFilterView(JDialog dialog, JFSConfig config, boolean isIncludeFilter) {
        super(dialog, true);
        this.config = config;
        this.isIncludeFilter = isIncludeFilter;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        List<JFSFilter> filters;
        if (isIncludeFilter) {
            setTitle(t.get("profile.filter.includes.title"));
            filters = config.getIncludes();
        } else {
            setTitle(t.get("profile.filter.excludes.title"));
            filters = config.getExcludes();
        }
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create table:
        Vector<JFSFilter> filtersClone = new Vector<JFSFilter>();
        for (JFSFilter f : filters)
            filtersClone.add(f.clone());
        filterTable = new JFSFilterTable(filtersClone);
        filterTable.getJTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterTable.getJTable().getSelectionModel().addListSelectionListener(this);

        // Create help message:
        JLabel help = new JLabel(t.get("profile.filter.regexp.help"));

        // Create filter panel:
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(new TitledBorder(t.get("profile.filter.list")));
        filterPanel.add(help, BorderLayout.NORTH);
        filterPanel.add(new JScrollPane(filterTable.getJTable()), BorderLayout.CENTER);

        JPanel filterStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterLabel = new JLabel();
        filterStatePanel.add(filterLabel);

        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        upButton = JFSSupport.getButton("button.up", "button.up", this);
        downButton = JFSSupport.getButton("button.down", "button.down", this);
        JButton addButton = JFSSupport.getButton("button.add", "button.add", this);
        removeButton = JFSSupport.getButton("button.remove", "button.remove", this);
        filterButtonPanel.add(upButton);
        filterButtonPanel.add(downButton);
        filterButtonPanel.add(addButton);
        filterButtonPanel.add(removeButton);

        JPanel filterStateAndButtonPanel = new JPanel(new BorderLayout());
        filterStateAndButtonPanel.add(filterStatePanel, BorderLayout.WEST);
        filterStateAndButtonPanel.add(filterButtonPanel, BorderLayout.EAST);
        filterPanel.add(filterStateAndButtonPanel, BorderLayout.SOUTH);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
        buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

        // Add all panels:
        cp.add(filterPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        update();
        checkButtons();
        filterPanel.setPreferredSize(new Dimension(450, 250));
        pack();
        JFSSupport.center(dialog, this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void actionPerformed(ActionEvent event) {
        JFSText t = JFSText.getInstance();
        String cmd = event.getActionCommand();

        if (cmd.equals("button.up")) {
            int row = filterTable.getJTable().getSelectedRow();

            // If a row is selected and it is not the first one then
            // move it one position upwards:
            if (row>0) {
                JFSFilter f = filterTable.getFilters().remove(row);
                filterTable.getFilters().insertElementAt(f, row-1);
                filterTable.getJTable().setRowSelectionInterval(row-1, row-1);
                update();
            }
        }

        if (cmd.equals("button.down")) {
            int row = filterTable.getJTable().getSelectedRow();

            // If a row is selected and it is not the last one then
            // move it one position upwards:
            if ((row> -1)&&(row<(filterTable.getJTable().getRowCount()-1))) {
                JFSFilter f = filterTable.getFilters().remove(row);

                // If 'row' is the last element just add a new
                // last element, otherwise insert 'pair' at 'row+1':
                if (row==(filterTable.getJTable().getRowCount()-1))
                    filterTable.getFilters().add(f);
                else
                    filterTable.getFilters().insertElementAt(f, row+1);

                filterTable.getJTable().setRowSelectionInterval(row+1, row+1);
                update();
            }
        }

        if (cmd.equals("button.add")) {
            // Create dialog:
            JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JTextField filterText = new JTextField();
            filterText.setColumns(20);
            row1Panel.add(new JLabel(t.get("profile.filter.add.regexp")));
            row1Panel.add(filterText);

            JComboBox typeCombo = new JComboBox();
            for (FilterType fType : FilterType.values()) {
                typeCombo.addItem(t.get(fType.getName()));
            }
            row2Panel.add(new JLabel(t.get("profile.filter.add.type")));
            row2Panel.add(typeCombo);

            JComboBox rangeCombo = new JComboBox();
            for (FilterRange fRange : FilterRange.values()) {
                rangeCombo.addItem(t.get(fRange.getName()));
            }
            row3Panel.add(new JLabel(t.get("profile.filter.add.range")));
            row3Panel.add(rangeCombo);

            JPanel panel = new JPanel(new GridLayout(3, 1));
            panel.add(row1Panel);
            panel.add(row2Panel);
            panel.add(row3Panel);

            int result = JOptionPane.showConfirmDialog(this, panel, t.get("profile.filter.add.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

            // If not canceled, add filter:
            if (result==JOptionPane.OK_OPTION) {
                JFSFilter f = new JFSFilter(filterText.getText());
                for (FilterType fType : FilterType.values()) {
                    if (typeCombo.getSelectedItem().equals(t.get(fType.getName()))) {
                        f.setType(fType);
                    }
                }
                for (FilterRange fRange : FilterRange.values()) {
                    if (rangeCombo.getSelectedItem().equals(t.get(fRange.getName()))) {
                        f.setRange(fRange);
                    }
                }

                filterTable.getFilters().add(f);
                update();
                checkButtons();
            }
        }

        if (cmd.equals("button.remove")) {
            ListSelectionModel model = filterTable.getJTable().getSelectionModel();

            // If a row is selected remove it from the table:
            if ( !model.isSelectionEmpty()) {
                int row = model.getLeadSelectionIndex();

                filterTable.getFilters().removeElementAt(row);

                if (row>0)
                    model.setLeadSelectionIndex(row-1);
                else if ((row==0)&&(filterTable.getJTable().getRowCount()>0))
                    model.setLeadSelectionIndex(0);
                else
                    model.clearSelection();

                update();
            }
        }

        if (cmd.equals("button.cancel")||cmd.equals("button.ok")) {
            setVisible(false);
            dispose();
        }

        if (cmd.equals("button.ok")) {
            if (isIncludeFilter) {
                if ( !filterTable.getFilters().equals(config.getIncludes()))
                    config.replaceIncludes(filterTable.getFilters());
            } else {
                if ( !filterTable.getFilters().equals(config.getExcludes()))
                    config.replaceExcludes(filterTable.getFilters());
            }
        }

    }


    /**
     * Called whenever values of the table change.
     */
    public void update() {
        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Update filter label:
        filterLabel.setText(filterTable.getFilters().size()+" "+t.get("general.objectNo"));

        // Update table:
        filterTable.getJTable().revalidate();
        filterTable.getJTable().repaint();
    }


    /**
     * Checks buttons whether it makes sense to activate or deactivate them.
     */
    private void checkButtons() {
        JTable table = filterTable.getJTable();
        if (table.getSelectionModel().isSelectionEmpty()) {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else {
            if (table.getSelectedRow()==0) {
                upButton.setEnabled(false);
            } else {
                upButton.setEnabled(true);
            }
            if (table.getSelectedRow()==table.getModel().getRowCount()-1) {
                downButton.setEnabled(false);
            } else {
                downButton.setEnabled(true);
            }
            removeButton.setEnabled(true);
        }
    }


    /**
     * @see ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        checkButtons();
    }
}