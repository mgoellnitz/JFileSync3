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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This dialog shows logging information.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLogView.java,v 1.12 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSLogView extends JDialog implements ActionListener {
    /** The UID. */
    private static final long serialVersionUID = 53L;

    /** The indentifier for showing the error log. */
    public final static byte ERR = 0;

    /** The indentifier for showing the output log. */
    public final static byte OUT = 1;

    /** The scroll pane containing the log messages. */
    private JScrollPane logPanel;

    /** The type of the log view. */
    private byte type;


    /**
     * Initializes the report view.
     * 
     * @param frame
     *            The main frame.
     */
    public JFSLogView(JFrame frame, byte type) {
        super(frame, true);
        this.type = type;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 500);
        if (type==ERR) {
            setTitle(t.get("log.err.title"));
            JFSLog.getErr().setLogMessagesRead();
        } else if (type==OUT) {
            setTitle(t.get("log.out.title"));
            JFSLog.getOut().setLogMessagesRead();
        }
        JFSSupport.center(frame, this);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Initialize error text area:
        logPanel = new JScrollPane();
        setContents();

        // Create buttons in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.close", "button.close", this));
        buttonPanel.add(JFSSupport.getButton("log.update", "UPDATE", this));
        buttonPanel.add(JFSSupport.getButton("log.clear", "CLEAR", this));

        // Add all panels:
        cp.add(logPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Activate dialog:
        setVisible(true);
    }


    /**
     * Sets the content of the text area.
     */
    private void setContents() {
        JFSText t = JFSText.getInstance();
        URL url = null;
        try {
            if (type==ERR)
                url = JFSLog.getErr().getLogURL();
            else if (type==OUT)
                url = JFSLog.getOut().getLogURL();

            try {
                JEditorPane log = new JEditorPane();
                log.setEditable(false);
                log.setPage(url);
                logPanel.setViewportView(log);
            } catch (IOException e) {
                JFSLog.getErr().getStream().println(t.get("error.io")+" "+url);
            }
        } catch (MalformedURLException e) {
            JFSLog.getErr().getStream().println(t.get("error.io")+" "+e);
        }
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

        if (cmd.equals("UPDATE")) {
            setContents();
        }

        if (cmd.equals("CLEAR")) {
            // Clean log text area and reset log file:
            if (type==ERR)
                JFSLog.getErr().resetLogFile();
            else if (type==OUT)
                JFSLog.getOut().resetLogFile();
            setContents();
        }
    }
}