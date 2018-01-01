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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import jfs.conf.JFSConst;
import jfs.conf.JFSText;

/**
 * This dialog assists a new user in performing a synchronization.
 *
 * @author Jens Heidrich
 * @version $Id: JFSAssistantView.java,v 1.9 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSAssistantView extends JDialog implements ActionListener {

    /** The UID. */
    private static final long serialVersionUID = 200L;

    /** The main view. */
    private final JFSMainView mainView;

    /** The button for step 2. */
    private final JButton step2Button;

    /** The button for step 3. */
    private final JButton step3Button;


    /**
     * Initializes the assistant.
     *
     * @param mainView
     *            The main view.
     */
    public JFSAssistantView(JFSMainView mainView) {
        super(mainView.getFrame(), false);
        this.mainView = mainView;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(t.get("assistant.title"));
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create panels for each step:
        JPanel step1 = new JPanel(new BorderLayout());
        step1.setBorder(new TitledBorder(t.get("assistant.editProfile")));
        step1.add(new JLabel(t.get("assistant.step1")), BorderLayout.NORTH);
        JPanel step1ButtonPanel = new JPanel();
        JButton step1Button = JFSSupport.getButton("jfs.icon.profile", "OPTIONS", this, "menu.options");
        step1Button.setText(t.get("menu.options"));
        step1ButtonPanel.add(step1Button);
        step1.add(step1ButtonPanel, BorderLayout.SOUTH);

        JPanel step2 = new JPanel(new BorderLayout());
        step2.setBorder(new TitledBorder(t.get("assistant.compare")));
        step2.add(new JLabel(t.get("assistant.step2")), BorderLayout.NORTH);
        JPanel step2ButtonPanel = new JPanel();
        step2Button = JFSSupport.getButton("jfs.icon.compare", "COMPARE", this, "menu.compare");
        step2Button.setText(t.get("menu.compare"));
        step2ButtonPanel.add(step2Button);
        step2.add(step2ButtonPanel, BorderLayout.SOUTH);

        JPanel step3 = new JPanel(new BorderLayout());
        step3.setBorder(new TitledBorder(t.get("assistant.synchronize")));
        step3.add(new JLabel(t.get("assistant.step3")), BorderLayout.NORTH);
        JPanel step3ButtonPanel = new JPanel();
        step3Button = JFSSupport.getButton("jfs.icon.synchronize", "SYNCHRONIZE", this, "menu.synchronize");
        step3Button.setText(t.get("menu.synchronize"));
        step3ButtonPanel.add(step3Button);
        step3.add(step3ButtonPanel, BorderLayout.SOUTH);

        // Create icon panel:
        JPanel iconPanel = new JPanel();
        JLabel jfsIcon = new JLabel(new ImageIcon(JFSConst.getInstance().getIconUrl("jfs.icon.info")));
        iconPanel.add(jfsIcon);

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.close", "button.close", this));

        // Add all panels:
        JPanel steps = new JPanel(new GridLayout(3, 1));
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        cp.add(iconPanel, BorderLayout.WEST);
        cp.add(steps, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Pack and activate dialog:
        step2Button.setEnabled(false);
        step3Button.setEnabled(false);
        pack();
        JFSSupport.center(mainView.getFrame(), this);
        this.setVisible(true);
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if ("OPTIONS".equals(cmd)) {
            mainView.actionPerformed("OPTIONS");
            step2Button.setEnabled(true);
        }

        if ("COMPARE".equals(cmd)) {
            mainView.actionPerformed("COMPARE");
            step3Button.setEnabled(true);
        }

        if ("SYNCHRONIZE".equals(cmd)) {
            mainView.actionPerformed("SYNCHRONIZE");
        }

        if ("button.close".equals(cmd)) {
            setVisible(false);
        }
    }

}