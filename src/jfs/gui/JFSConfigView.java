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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSSyncMode;
import jfs.conf.JFSSyncModes;
import jfs.conf.JFSText;

/**
 * This dialog is responsible for changing options within the configuration object (aka user's profile) currently used.
 *
 * @author Jens Heidrich
 * @version $Id: JFSConfigView.java,v 1.26 2007/06/06 19:51:33 heidrich Exp $
 */
public class JFSConfigView extends JDialog implements ActionListener, ListSelectionListener {

    /** The UID. */
    private static final long serialVersionUID = 50L;

    /** The new configuration object. */
    private final JFSConfig configNew;

    /** The title text field. */
    private final JTextField title;

    /** The synchronization mode box. */
    @SuppressWarnings("rawtypes")
    private final JComboBox syncMode;

    /** The synchronization modes. */
    private final List<JFSSyncMode> syncModeList;

    /** The table of directory pairs. */
    private final JTable directoryTable;

    /** Number of directory pairs. */
    private final JLabel directoryLabel;

    /** The up button. */
    private final JButton upButton;

    /** The down button. */
    private final JButton downButton;

    /** The change button. */
    private final JButton changeButton;

    /** The remove button. */
    private final JButton removeButton;


    /**
     * Initializes the config view.
     *
     * @param frame
     *            The main frame.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JFSConfigView(JFrame frame) {
        super(frame, true);

        JFSConfig config = JFSConfig.getInstance();

        // Clone the existing configuration object:
        configNew = (JFSConfig)config.clone();

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("profile.title")+config.getTitle());
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // A reference to the corresponding selection models of the table:
        ListSelectionModel selection;

        // Initialize directory pair table:
        JFSDirectoryTable jfsDirectoryTable = new JFSDirectoryTable(configNew);
        directoryTable = jfsDirectoryTable.getJTable();
        selection = directoryTable.getSelectionModel();
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection.addListSelectionListener(this);

        // Call dialog if row is double-clicked:
        final JFSConfigView configView = this;
        directoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) {
                    int row = directoryTable.rowAtPoint(e.getPoint());
                    JFSDirectoryPair pair = configNew.getDirectoryList().get(row);
                    new JFSConfigDirectoryView(configView, configNew, pair);
                }
            }
        });

        // Create directory panel:
        JPanel directoryPanel = new JPanel(new BorderLayout());
        directoryPanel.setBorder(new TitledBorder(t.get("profile.dir.table.title")));
        directoryPanel.add(new JScrollPane(directoryTable), BorderLayout.CENTER);

        JPanel directoryStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        directoryLabel = new JLabel();
        directoryStatePanel.add(directoryLabel);

        JPanel directoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        upButton = JFSSupport.getButton("button.up", "button.up", this);
        downButton = JFSSupport.getButton("button.down", "button.down", this);
        JButton addButton = JFSSupport.getButton("button.add", "button.add", this);
        changeButton = JFSSupport.getButton("button.change", "button.change", this);
        removeButton = JFSSupport.getButton("button.remove", "button.remove", this);
        directoryButtonPanel.add(upButton);
        directoryButtonPanel.add(downButton);
        directoryButtonPanel.add(addButton);
        directoryButtonPanel.add(changeButton);
        directoryButtonPanel.add(removeButton);

        JPanel directoryStateAndButtonPanel = new JPanel(new BorderLayout());
        directoryStateAndButtonPanel.add(directoryStatePanel, BorderLayout.WEST);
        directoryStateAndButtonPanel.add(directoryButtonPanel, BorderLayout.EAST);
        directoryPanel.add(directoryStateAndButtonPanel, BorderLayout.SOUTH);

        // Create options panel:
        JLabel titleLabel = new JLabel(t.get("profile.title"));
        title = new JTextField(configNew.getTitle(), 40);

        // Set-up modes:
        JLabel syncModeLabel = new JLabel(t.get("profile.syncMode"));
        JFSSyncModes modes = JFSSyncModes.getInstance();
        syncMode = new JComboBox();
        syncModeList = new Vector<JFSSyncMode>(modes.getModes());
        for (JFSSyncMode mode : syncModeList) {
            syncMode.addItem(t.get(mode.getAlias()));
        }

        // Determine and select current mode:
        JFSSyncMode currentMode = modes.getCurrentMode();
        int currentIndex = syncModeList.indexOf(currentMode);

        if (currentIndex!= -1)
            syncMode.setSelectedIndex(currentIndex);

        // Set-up panels:
        JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1Panel.add(titleLabel);
        row1Panel.add(title);

        JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2Panel.add(syncModeLabel);
        row2Panel.add(syncMode);

        JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3Panel.add(JFSSupport.getButton("profile.advanced.button", "ADVANCED", this));
        row3Panel.add(JFSSupport.getButton("profile.filter.includes", "INCLUDES", this));
        row3Panel.add(JFSSupport.getButton("profile.filter.excludes", "EXCLUDES", this));
        row3Panel.add(JFSSupport.getButton("server.title", "server.title", this));

        JPanel optionsPanel = new JPanel(new GridLayout(3, 1));
        optionsPanel.setBorder(new TitledBorder(t.get("profile.option.heading")));
        optionsPanel.add(row1Panel);
        optionsPanel.add(row2Panel);
        optionsPanel.add(row3Panel);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
        buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

        // Add all panels:
        cp.add(optionsPanel, BorderLayout.NORTH);
        cp.add(directoryPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // First update and button check:
        update();
        checkButtons();

        // Pack and activate dialog:
        directoryPanel.setPreferredSize(new Dimension(500, 300));
        pack();
        JFSSupport.center(frame, this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if ("button.up".equals(cmd)) {
            int row = directoryTable.getSelectedRow();

            // If a row is selected and it is not the first one then
            // move it one position upwards:
            if (row>0) {
                JFSDirectoryPair pair = configNew.removeDirectoryPair(row);
                configNew.insertDirectoryPair(pair, row-1);
                directoryTable.setRowSelectionInterval(row-1, row-1);
                update();
            }
        }

        if ("button.down".equals(cmd)) {
            int row = directoryTable.getSelectedRow();

            // If a row is selected and it is not the last one then
            // move it one position upwards:
            if ((row> -1)&&(row<(directoryTable.getRowCount()-1))) {
                JFSDirectoryPair pair = configNew.removeDirectoryPair(row);

                // If 'row' is the last element just add a new
                // last element, otherwise insert 'pair' at 'row+1':
                if (row==(directoryTable.getRowCount()-1)) {
                    configNew.addDirectoryPair(pair);
                } else {
                    configNew.insertDirectoryPair(pair, row+1);
                }

                directoryTable.setRowSelectionInterval(row+1, row+1);
                update();
            }
        }

        if ("button.add".equals(cmd)) {
            new JFSConfigDirectoryView(this, configNew, new JFSDirectoryPair("", ""));
            update();
            checkButtons();
        }

        if ("button.change".equals(cmd)&& !directoryTable.getSelectionModel().isSelectionEmpty()) {
            JFSDirectoryPair pair = configNew.getDirectoryList().get(directoryTable.getSelectedRow());
            new JFSConfigDirectoryView(this, configNew, pair);
            update();
        }

        if ("button.remove".equals(cmd)) {
            ListSelectionModel model = directoryTable.getSelectionModel();

            // If a row is selected remove it from the table:
            if ( !model.isSelectionEmpty()) {
                int row = model.getLeadSelectionIndex();

                configNew.removeDirectoryPair(row);

                if (row>0)
                    model.setLeadSelectionIndex(row-1);
                else if ((row==0)&&(directoryTable.getRowCount()>0))
                    model.setLeadSelectionIndex(0);
                else
                    model.clearSelection();

                update();
            }
        }

        if ("button.cancel".equals(cmd)||"button.ok".equals(cmd)) {
            setVisible(false);
            dispose();
        }

        if ("button.ok".equals(cmd)) {
            // Update the new configuration object:
            configNew.setTitle(title.getText());
            JFSSyncMode mode = syncModeList.get(syncMode.getSelectedIndex());
            configNew.setSyncMode((byte)mode.getId());

            // Transfer the data to the current object and update all
            // configuration object observers:
            JFSConfig config = JFSConfig.getInstance();
            configNew.transferContentTo(config);
        }

        if ("ADVANCED".equals(cmd)) {
            new JFSConfigAdvancedView(this, configNew);
        }

        if ("INCLUDES".equals(cmd)) {
            new JFSConfigFilterView(this, configNew, true);
        }

        if ("EXCLUDES".equals(cmd)) {
            new JFSConfigFilterView(this, configNew, false);
        }

        if ("server.title".equals(cmd)) {
            new JFSConfigServerView(this, configNew);
        }
    }


    /**
     * Launches a dialog to create a new directory if the specified one doesn't exist.
     *
     * @param component
     *            The frame to attach the dialog.
     * @param dir
     *            The directory to create.
     */
    public static void createDirectoryDialog(Component component, String dir) {
        // Test for existence:
        for (String schema : JFSConfigDirectoryView.OTHER_PRODUCER_CODES) {
            if (dir.startsWith(schema)) {
                dir = dir.substring((schema+"://").length());
            } // if
        } // for

        // No URL given
        if ( !dir.matches("[a-z][a-z][a-z]*://.*")) {
            File file = new File(dir);

            if ( !file.exists()) {
                // Create dialog:
                JFSText t = JFSText.getInstance();
                JPanel panel = new JPanel(new GridLayout(3, 1));
                JLabel msg = new JLabel(t.get("profile.dir.create.message"));
                JLabel question = new JLabel(t.get("profile.dir.create.question"));
                JTextField directory = new JTextField(dir);
                panel.add(msg);
                panel.add(question);
                panel.add(directory);

                int result = JOptionPane.showConfirmDialog(component, panel, t.get("profile.dir.create.title"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (result==JOptionPane.OK_OPTION) {
                    if ( !file.mkdirs()) {
                        JLabel failed = new JLabel(t.get("profile.dir.message.failed"));
                        JOptionPane.showMessageDialog(component, failed, t.get("profile.dir.create.title"),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }


    /**
     * Called whenever values of the table change.
     */
    private void update() {
        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Update directory label:
        directoryLabel.setText(configNew.getDirectoryList().size()+" "+t.get("general.objectNo"));

        // Update table:
        directoryTable.revalidate();
        directoryTable.repaint();
    }


    /**
     * Checks buttons whether it makes sense to activate or deactivate them.
     */
    private void checkButtons() {
        if (directoryTable.getSelectionModel().isSelectionEmpty()) {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            changeButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else {
            if (directoryTable.getSelectedRow()==0) {
                upButton.setEnabled(false);
            } else {
                upButton.setEnabled(true);
            }
            if (directoryTable.getSelectedRow()==directoryTable.getModel().getRowCount()-1) {
                downButton.setEnabled(false);
            } else {
                downButton.setEnabled(true);
            }
            changeButton.setEnabled(true);
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