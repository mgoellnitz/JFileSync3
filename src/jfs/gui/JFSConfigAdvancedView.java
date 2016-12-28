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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import jfs.conf.JFSConfig;
import jfs.conf.JFSText;

/**
 * This dialog manages advanced settings.
 *
 * @author Jens Heidrich
 * @version $Id: JFSConfigAdvancedView.java,v 1.1 2005/05/17 07:37:51 heidrich Exp $
 */
public class JFSConfigAdvancedView extends JDialog implements ActionListener {
    /** The UID. */
    private static final long serialVersionUID = 545435345L;

    /** The configuration object to modify. */
    private final JFSConfig config;

    /** The granularity spinner. */
    private SpinnerNumberModel granularity;

    /** The buffer size spinner. */
    private SpinnerNumberModel bufferSize;

    /** The keep user actions check box. */
    private final JCheckBox keepUserActions;

    /** The history checkbox. */
    private final JCheckBox history;

    /** The set can write checkbox. */
    private final JCheckBox setCanWrite;


    /**
     * Initializes the config view.
     *
     * @param dialog
     *            The main frame.
     * @param config
     *            The configuration to change.
     */
    public JFSConfigAdvancedView(JDialog dialog, JFSConfig config) {
        super(dialog, true);
        this.config = config;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("profile.advanced.title"));
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create options panel:
        JLabel granularityLabel = new JLabel(t.get("profile.granularity"));
        granularity = new SpinnerNumberModel(config.getGranularity(), 0, 60000, 500);
        JSpinner granularitySpinner = new JSpinner(granularity);

        JLabel bufferSizeLabel = new JLabel(t.get("profile.bufferSize"));
        bufferSize = new SpinnerNumberModel(config.getBufferSize(), 0, 1048576, 256);
        JSpinner bufferSizeSpinner = new JSpinner(bufferSize);

        keepUserActions = new JCheckBox(t.get("profile.keepUserActions"), config.isKeepUserActions());

        history = new JCheckBox(t.get("profile.storeHistory"), config.isStoreHistory());

        setCanWrite = new JCheckBox(t.get("profile.setCanWrite"), config.isStoreHistory());

        JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1Panel.add(granularityLabel);
        row1Panel.add(granularitySpinner);

        JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2Panel.add(bufferSizeLabel);
        row2Panel.add(bufferSizeSpinner);

        JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3Panel.add(keepUserActions);

        JPanel row4Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row4Panel.add(history);

        JPanel row5Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row5Panel.add(setCanWrite);

        JPanel optionsPanel = new JPanel(new GridLayout(5, 1));
        optionsPanel.setBorder(new TitledBorder(t.get("profile.option.heading")));
        optionsPanel.add(row1Panel);
        optionsPanel.add(row2Panel);
        optionsPanel.add(row3Panel);
        optionsPanel.add(row4Panel);
        optionsPanel.add(row5Panel);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
        buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

        // Add all panels:
        cp.add(optionsPanel, BorderLayout.NORTH);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        pack();
        JFSSupport.center(dialog, this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if (cmd.equals("button.cancel")||cmd.equals("button.ok")) {
            setVisible(false);
            dispose();
        }

        if (cmd.equals("button.ok")) {
            config.setGranularity(granularity.getNumber().intValue());
            config.setBufferSize(bufferSize.getNumber().intValue());
            config.setKeepUserActions(keepUserActions.isSelected());
            config.setStoreHistory(history.isSelected());
            config.setCanWrite(setCanWrite.isSelected());
        }
    }
}