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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jfs.conf.JFSText;
import jfs.sync.JFSCopyStatement;
import jfs.sync.JFSDeleteStatement;
import jfs.sync.JFSTable;

/**
 * A dialog that is shown before the synchronization which allows to deselect (and reselect) files that should have been
 * copied or deleted.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSConfirmationView.java,v 1.10 2005/05/13 15:03:20 heidrich Exp $
 */
public class JFSConfirmationView extends JDialog implements ActionListener, ListSelectionListener {
    /** The UID. */
    private static final long serialVersionUID = 51L;

    /** The table of files that have to be copied. */
    private JTable copyTable;

    /** The table of files that have to be deleted. */
    private JTable deleteTable;

    /** Number of files that have to be copied. */
    private JLabel copyLabel;

    /** Number of files that have to be deleted. */
    private JLabel deleteLabel;

    /** Result of the dialog. */
    private int result = JOptionPane.CANCEL_OPTION;

    /** The detailed view on copy and delete statements. */
    private JPanel statementsPanel;

    /** Shows and hides synchronization details. */
    private JButton toggleDetailsButton;


    /**
     * Initializes the confirmation view.
     * 
     * @param frame
     *            The main frame.
     */
    public JFSConfirmationView(JFrame frame) {
        super(frame, true);

        // Get the translation object and table:
        JFSText t = JFSText.getInstance();
        JFSTable table = JFSTable.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("report.title.ok"));

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Ask confirmation question:
        JPanel questionPanel = new JPanel();
        questionPanel.setBorder(new TitledBorder(t.get("report.question.title")));
        questionPanel.add(new JLabel(t.get("report.question")));
        toggleDetailsButton = JFSSupport.getButton("report.details.show", "TOGGLE_DETAILS", this);
        questionPanel.add(toggleDetailsButton);

        // Initialize copy table:
        JFSCopyTable jfsCopyTable = new JFSCopyTable(table.getCopyStatements());
        copyTable = jfsCopyTable.getJTable();
        ListSelectionModel copySelectionModel = copyTable.getSelectionModel();
        copySelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        copyTable.selectAll();
        copySelectionModel.addListSelectionListener(this);

        // Initialize delete table:
        JFSDeleteTable jfsDeleteTable = new JFSDeleteTable(table.getDeleteStatements());
        deleteTable = jfsDeleteTable.getJTable();
        ListSelectionModel deleteSelectionModel = deleteTable.getSelectionModel();
        deleteSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deleteTable.selectAll();
        deleteSelectionModel.addListSelectionListener(this);

        // Create copy panel:
        JPanel copyPanel = new JPanel(new BorderLayout());
        copyPanel.setBorder(new TitledBorder(t.get("report.copy.ok")));
        copyPanel.add(new JScrollPane(copyTable), BorderLayout.CENTER);

        JPanel copyStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        copyLabel = new JLabel(table.getCopyStatements().size()+" "+t.get("general.objectNo"));
        copyStatePanel.add(copyLabel);

        JPanel copyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copySelectAllButton = JFSSupport.getButton("button.selectAll", "SELECT_ALL_COPY", this);
        JButton copyDeselectAllButton = JFSSupport.getButton("button.deselectAll", "DESELECT_ALL_COPY", this);
        copyButtonPanel.add(copySelectAllButton);
        copyButtonPanel.add(copyDeselectAllButton);
        if (copyTable.getModel().getRowCount()==0) {
            copySelectAllButton.setEnabled(false);
            copyDeselectAllButton.setEnabled(false);
        }

        JPanel copyStateAndButtonPanel = new JPanel(new BorderLayout());
        copyStateAndButtonPanel.add(copyStatePanel, BorderLayout.WEST);
        copyStateAndButtonPanel.add(copyButtonPanel, BorderLayout.EAST);
        copyPanel.add(copyStateAndButtonPanel, BorderLayout.SOUTH);

        // Create delete panel:
        JPanel deletePanel = new JPanel(new BorderLayout());
        deletePanel.setBorder(new TitledBorder(t.get("report.delete.ok")));
        deletePanel.add(new JScrollPane(deleteTable), BorderLayout.CENTER);

        JPanel deleteStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deleteLabel = new JLabel(table.getDeleteStatements().size()+" "+t.get("general.objectNo"));
        deleteStatePanel.add(deleteLabel);

        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteSelectAllButton = JFSSupport.getButton("button.selectAll", "SELECT_ALL_DELETE", this);
        JButton deleteDeselectAllButton = JFSSupport.getButton("button.deselectAll", "DESELECT_ALL_DELETE", this);
        deleteButtonPanel.add(deleteSelectAllButton);
        deleteButtonPanel.add(deleteDeselectAllButton);
        if (deleteTable.getModel().getRowCount()==0) {
            deleteSelectAllButton.setEnabled(false);
            deleteDeselectAllButton.setEnabled(false);
        }

        JPanel deleteStateAndButtonPanel = new JPanel(new BorderLayout());
        deleteStateAndButtonPanel.add(deleteStatePanel, BorderLayout.WEST);
        deleteStateAndButtonPanel.add(deleteButtonPanel, BorderLayout.EAST);
        deletePanel.add(deleteStateAndButtonPanel, BorderLayout.SOUTH);

        // Create center panel:
        statementsPanel = new JPanel(new GridLayout(2, 1));
        statementsPanel.add(copyPanel);
        statementsPanel.add(deletePanel);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
        buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

        // Add all panels:
        cp.add(questionPanel, BorderLayout.NORTH);
        cp.add(statementsPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Activate dialog:
        statementsPanel.setVisible(false);
        statementsPanel.setPreferredSize(new Dimension(400, 450));
        this.pack();
        JFSSupport.center(frame, this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        JFSTable table = JFSTable.getInstance();

        if (cmd.equals("TOGGLE_DETAILS")) {
            JFSText t = JFSText.getInstance();

            if (statementsPanel.isVisible()) {
                toggleDetailsButton.setText(t.get("report.details.show"));
                statementsPanel.setVisible(false);
            } else {
                toggleDetailsButton.setText(t.get("report.details.hide"));
                statementsPanel.setVisible(true);
            }

            this.pack();
            JFSSupport.center(this.getParent(), this);
        }

        if (cmd.equals("SELECT_ALL_COPY"))
            copyTable.selectAll();

        if (cmd.equals("DESELECT_ALL_COPY"))
            copyTable.clearSelection();

        if (cmd.equals("SELECT_ALL_DELETE"))
            deleteTable.selectAll();

        if (cmd.equals("DESELECT_ALL_DELETE"))
            deleteTable.clearSelection();

        if (cmd.equals("button.ok")) {
            // Set result:
            result = JOptionPane.OK_OPTION;

            // Update copy file list:
            int i = 0;
            for (JFSCopyStatement cs : table.getCopyStatements()) {
                if (copyTable.isRowSelected(i))
                    cs.setCopyFlag(true);
                else
                    cs.setCopyFlag(false);
                i++ ;
            }

            // Update delete file list:
            int j = 0;
            for (JFSDeleteStatement ds : table.getDeleteStatements()) {
                if (deleteTable.isRowSelected(j))
                    ds.setDeleteFlag(true);
                else
                    ds.setDeleteFlag(false);
                j++ ;
            }
        }

        if (cmd.equals("button.cancel"))
            result = JOptionPane.CANCEL_OPTION;

        if (cmd.equals("button.cancel")||cmd.equals("button.ok")) {
            setVisible(false);
            dispose();
        }
    }


    /**
     * Returns the result of the dialog.
     * 
     * @return JOptionPane.CANCEL_OPTION or JOptionPane.OK_OPTION.
     */
    public int getResult() {
        return result;
    }


    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            The event that characterizes the change.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // Just check the table after the last change event
        // (out of a series of change events):
        if ( !e.getValueIsAdjusting()) {
            // Get the translation object:
            JFSText t = JFSText.getInstance();

            // Update copy label:
            copyLabel.setText(copyTable.getSelectedRowCount()+" "+t.get("general.objectNo"));

            // Update delete label:
            deleteLabel.setText(deleteTable.getSelectedRowCount()+" "+t.get("general.objectNo"));
        }
    }
}