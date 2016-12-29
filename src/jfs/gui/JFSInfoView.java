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
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jfs.conf.JFSConst;
import jfs.conf.JFSText;

/**
 * This dialog displays information about the currently used JFS version.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSInfoView.java,v 1.5 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSInfoView extends JDialog implements ActionListener {
    /** The UID. */
    private static final long serialVersionUID = 2023123123L;


    /**
     * Initializes the dialog.
     * 
     * @param mainView
     *            The main view.
     */
    public JFSInfoView(JFSMainView mainView) {
        super(mainView.getFrame(), true);

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("menu.info"));
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create info panel:
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setForeground(Color.BLACK);
        infoPanel.add(new JLabel(t.get("general.appName")+" "+JFSConst.getInstance().getString("jfs.version")));
        infoPanel.add(new JLabel(t.get("info.copyright")));
        infoPanel.add(new JLabel(t.get("info.author")));
        infoPanel.add(new JLabel(t.get("info.copyright.addon")));
        infoPanel.add(new JLabel(t.get("info.author.addon")));

        // Create icon panel:
        JPanel iconPanel = new JPanel();
        iconPanel.setBackground(Color.WHITE);
        iconPanel.setForeground(Color.BLACK);
        JLabel jfsIcon = new JLabel(new ImageIcon(JFSConst.getInstance().getIconUrl("jfs.icon.info")));
        iconPanel.add(jfsIcon);

        // Create icon and info panel:
        JPanel iconInfoPanel = new JPanel(new BorderLayout());
        iconInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        iconInfoPanel.add(iconPanel, BorderLayout.WEST);
        iconInfoPanel.add(infoPanel, BorderLayout.CENTER);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.close", "button.close", this));

        // Add all panels:
        cp.add(iconInfoPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        pack();
        JFSSupport.center(mainView.getFrame(), this);
        setVisible(true);
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