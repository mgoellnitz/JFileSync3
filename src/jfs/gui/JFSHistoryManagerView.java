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
import jfs.conf.JFSConfig;
import jfs.conf.JFSHistoryManager;
import jfs.conf.JFSText;


/**
 * This dialog manages all histories.
 *
 * @author Jens Heidrich
 * @version $Id: JFSHistoryManagerView.java,v 1.1 2005/05/17 07:37:51 heidrich Exp $
 */
public class JFSHistoryManagerView extends JDialog implements ActionListener, ListSelectionListener {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 3467768L;

    /**
     * The table of histories.
     */
    private JFSHistoryTable historyTable;

    /**
     * Number of histories.
     */
    private JLabel historyLabel;

    /**
     * The clear button.
     */
    private JButton clearButton;

    /**
     * The clear all button.
     */
    private JButton clearAllButton;


    /**
     * Initializes the history manager view.
     *
     * @param frame
     * The main frame.
     */
    public JFSHistoryManagerView(JFrame frame) {
        super(frame, true);

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("history.heading"));

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create table:
        historyTable = new JFSHistoryTable();
        historyTable.getJTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getJTable().getSelectionModel().addListSelectionListener(this);

        // Create filter panel:
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new TitledBorder(t.get("history.table.heading")));
        historyPanel.add(new JScrollPane(historyTable.getJTable()), BorderLayout.CENTER);

        JPanel historyStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyLabel = new JLabel();
        historyStatePanel.add(historyLabel);

        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButton = JFSSupport.getButton("history.clear", "CLEAR", this);
        clearAllButton = JFSSupport.getButton("history.clearAll", "CLEAR_ALL", this);
        historyButtonPanel.add(clearButton);
        historyButtonPanel.add(clearAllButton);

        JPanel historyStateAndButtonPanel = new JPanel(new BorderLayout());
        historyStateAndButtonPanel.add(historyStatePanel, BorderLayout.WEST);
        historyStateAndButtonPanel.add(historyButtonPanel, BorderLayout.EAST);
        historyPanel.add(historyStateAndButtonPanel, BorderLayout.SOUTH);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.close", "button.close", this));

        // Add all panels:
        cp.add(historyPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        update();
        checkButtons();
        historyPanel.setPreferredSize(new Dimension(450, 350));
        pack();
        JFSSupport.center(frame, this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        JFSText t = JFSText.getInstance();
        String cmd = event.getActionCommand();

        if (cmd.equals("CLEAR_ALL")) {
            int result = JOptionPane.showConfirmDialog(this, t.get("history.clearAll.question"),
                    t.get("general.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result==JOptionPane.OK_OPTION) {
                ListSelectionModel model = historyTable.getJTable().getSelectionModel();
                JFSHistoryManager hm = JFSHistoryManager.getInstance();
                hm.deleteAll();
                model.clearSelection();
                update();
                checkButtons();
                JFSConfig.getInstance().fireComparisonUpdate();
            }
        }

        if (cmd.equals("CLEAR")) {
            ListSelectionModel model = historyTable.getJTable().getSelectionModel();

            // If a row is selected remove it from the table:
            if (!model.isSelectionEmpty()) {
                int row = model.getLeadSelectionIndex();

                JFSHistoryManager hm = JFSHistoryManager.getInstance();
                hm.deleteHistory(hm.getHistories().get(row));

                if (row>0) {
                    model.setLeadSelectionIndex(row-1);
                } else if (row==0&&historyTable.getJTable().getRowCount()>0) {
                    model.setLeadSelectionIndex(0);
                } else {
                    model.clearSelection();
                }

                update();
                checkButtons();
                JFSConfig.getInstance().fireComparisonUpdate();
            }
        }

        if (cmd.equals("button.close")) {
            setVisible(false);
            dispose();
        }
    }


    /**
     * Called whenever values of the table change.
     */
    public void update() {
        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Update filter label:
        historyLabel.setText(JFSHistoryManager.getInstance().getHistories().size()+" "+t.get("general.objectNo"));

        // Update table:
        historyTable.getJTable().revalidate();
        historyTable.getJTable().repaint();
    }


    /**
     * Checks buttons whether it makes sense to activate or deactivate them.
     */
    private void checkButtons() {
        JTable table = historyTable.getJTable();
        if (table.getModel().getRowCount()==0) {
            clearAllButton.setEnabled(false);
        } else {
            clearAllButton.setEnabled(true);
        }
        if (table.getSelectionModel().isSelectionEmpty()) {
            clearButton.setEnabled(false);
        } else {
            clearButton.setEnabled(true);
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
