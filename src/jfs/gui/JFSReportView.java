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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import jfs.conf.JFSText;
import jfs.sync.JFSTable;


/**
 * A dialog that is shown after the synchronization presenting all failed copy and delete statements deleted.
 *
 * @author Jens Heidrich
 * @version $Id: JFSReportView.java,v 1.16 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSReportView extends JDialog implements ActionListener {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 54L;

    /**
     * The main JFileSync frame.
     */
    private final JFrame frame;


    /**
     * Initializes the report view.
     *
     * @param frame
     * The main frame.
     */
    public JFSReportView(JFrame frame) {
        super(frame, true);
        this.frame = frame;

        // View detailed report, if there exists at least one failed statement,
        // view success message otherwise:
        JFSTable table = JFSTable.getInstance();
        if ((table.getFailedCopyStatements().size()>0)||(table.getFailedDeleteStatements().size()>0)) {
            // Get the translation object:
            JFSText t = JFSText.getInstance();

            // Create the modal dialog:
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(600, 550);
            setTitle(t.get("report.title.failed"));
            JFSSupport.center(frame, this);

            Container cp = getContentPane();
            cp.setLayout(new BorderLayout());

            // Initialize copy and delete table:
            JFSCopyTable jfsCopyTable = new JFSCopyTable(table.getFailedCopyStatements());
            JTable copyTable = jfsCopyTable.getJTable();
            JFSDeleteTable jfsDeleteTable = new JFSDeleteTable(table.getFailedDeleteStatements());
            JTable deleteTable = jfsDeleteTable.getJTable();

            // Create copy panel:
            JPanel copyPanel = new JPanel(new BorderLayout());
            copyPanel.setBorder(new TitledBorder(t.get("report.copy.failed")));
            copyPanel.add(new JScrollPane(copyTable), BorderLayout.CENTER);

            JPanel copyStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel copyLabel = new JLabel(table.getFailedCopyStatements().size()+" "+t.get("general.objectNo"));
            copyStatePanel.add(copyLabel);
            copyPanel.add(copyStatePanel, BorderLayout.SOUTH);

            // Create delete panel:
            JPanel deletePanel = new JPanel(new BorderLayout());
            deletePanel.setBorder(new TitledBorder(t.get("report.delete.failed")));
            deletePanel.add(new JScrollPane(deleteTable), BorderLayout.CENTER);

            JPanel deleteStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel deleteLabel = new JLabel(table.getFailedDeleteStatements().size()+" "+t.get("general.objectNo"));
            deleteStatePanel.add(deleteLabel);
            deletePanel.add(deleteStatePanel, BorderLayout.SOUTH);

            // Create center panel:
            JPanel centerPanel = new JPanel(new GridLayout(2, 1));
            centerPanel.add(copyPanel);
            centerPanel.add(deletePanel);

            // Create buttons in a separate panel:
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
            buttonPanel.add(JFSSupport.getButton("menu.errLog", "error.log", this));

            // Add all panels:
            cp.add(centerPanel, BorderLayout.CENTER);
            cp.add(buttonPanel, BorderLayout.SOUTH);

            // Activate dialog:
            setVisible(true);
        } else {
            dispose();
            JOptionPane.showMessageDialog(frame, JFSText.getInstance().get("message.success"), JFSText.getInstance()
                    .get("message.success.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if ("button.ok".equals(cmd)) {
            setVisible(false);
            dispose();
        }

        if ("error.log".equals(cmd)) {
            new JFSLogView(frame, JFSLogView.ERR);
        }
    }

}
