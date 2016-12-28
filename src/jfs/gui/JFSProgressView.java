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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import jfs.conf.JFSText;
import jfs.sync.JFSComparison;
import jfs.sync.JFSComparisonMonitor;
import jfs.sync.JFSCopyMonitor;
import jfs.sync.JFSDeleteMonitor;
import jfs.sync.JFSFormatter;
import jfs.sync.JFSProgress;
import jfs.sync.JFSProgress.ProgressActivity;
import jfs.sync.JFSProgress.ProgressState;
import jfs.sync.JFSProgressObserver;
import jfs.sync.JFSSynchronization;


/**
 * A dialog that is shown during the comparison of a set of directory pairs und during the synchronization.
 *
 * @author Jens Heidrich
 * @version $Id: JFSProgressView.java,v 1.29 2007/07/20 16:35:36 heidrich Exp $
 */
public class JFSProgressView extends JDialog implements JFSProgressObserver, ActionListener {

    /**
     * The UID.
     */
    private static final long serialVersionUID = 52L;

    /**
     * The main frame ;-).
     */
    private final JFSMainView mainView;

    /**
     * The completion bar for the algorithm.
     */
    private final JProgressBar completionBar;

    /**
     * The estimated time until the current activity is done.
     */
    private final JLabel remainingTime;

    /**
     * The panel containing details about comparison.
     */
    private final JPanel comparisonPanel;

    /**
     * The panel containing details about delete statements.
     */
    private final JPanel deletePanel;

    /**
     * The panel containing details about copy statements.
     */
    private final JPanel copyPanel;

    /**
     * The relative path of the currently compared directory.
     */
    private final JLabel comparisonCurrentDir;

    /**
     * The number of performed versus overall delete statements.
     */
    private final JLabel deleteStmtsNo;

    /**
     * The relative path of the currently deleted file.
     */
    private final JLabel deleteCurrentFile;

    /**
     * The number of performed versus overall copy statements.
     */
    private final JLabel copyStmtsNo;

    /**
     * Bytes already transfered versus overall number of bytes.
     */
    private final JLabel copyBytes;

    /**
     * The relative path of the currently copied file.
     */
    private final JLabel copyCurrentFile;

    /**
     * Bytes transfered versus overall number of bytes for the current file.
     */
    private final JLabel copyBytesCurrentFile;

    /**
     * Shows and hides synchronization details.
     */
    private final JButton toggleDetailsButton;

    /**
     * Determines whether details should be shown.
     */
    private boolean doShowDetails = false;


    /**
     * Initializes the progress view.
     *
     * @param mainView
     * The main frame.
     */
    public JFSProgressView(JFSMainView mainView) {
        // Create the modal dialog:
        super(mainView.getFrame(), true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.mainView = mainView;
        int width = 75*(int) (mainView.getFrame().getBounds().getWidth())/100;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create state panel:
        JPanel statePanel = new JPanel(new BorderLayout());
        statePanel.setBorder(new TitledBorder(t.get("progress.state")));
        completionBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        completionBar.setStringPainted(false);
        statePanel.add(completionBar, BorderLayout.NORTH);
        toggleDetailsButton = JFSSupport.getButton("progress.details.show", "TOGGLE_DETAILS", this);
        statePanel.add(toggleDetailsButton, BorderLayout.EAST);
        remainingTime = new JLabel();
        statePanel.add(remainingTime, BorderLayout.WEST);
        Dimension d = statePanel.getPreferredSize();
        statePanel.setPreferredSize(new Dimension(width, d.height));

        // Create comparison panel:
        comparisonPanel = new JPanel(new GridLayout(1, 1));
        comparisonPanel.setBorder(new TitledBorder(t.get("progress.details.title")));
        JPanel comparisonRow1 = new JPanel();
        BoxLayout comparisonBL1 = new BoxLayout(comparisonRow1, BoxLayout.X_AXIS);
        comparisonRow1.setLayout(comparisonBL1);
        comparisonPanel.add(comparisonRow1);
        comparisonCurrentDir = new JLabel();
        comparisonRow1.add(new JLabel(t.get("progress.details.current")+" "));
        comparisonRow1.add(comparisonCurrentDir);
        d = comparisonPanel.getPreferredSize();
        comparisonPanel.setPreferredSize(new Dimension(width, d.height));

        // Create delete panel:
        deletePanel = new JPanel(new GridLayout(2, 1));
        deletePanel.setBorder(new TitledBorder(t.get("progress.details.title")));
        JPanel deleteRow1 = new JPanel();
        BoxLayout deleteBL1 = new BoxLayout(deleteRow1, BoxLayout.X_AXIS);
        deleteRow1.setLayout(deleteBL1);
        deletePanel.add(deleteRow1);
        deleteStmtsNo = new JLabel();
        deleteRow1.add(new JLabel(t.get("progress.details.statementsNo")+" "));
        deleteRow1.add(deleteStmtsNo);
        JPanel deleteRow2 = new JPanel();
        BoxLayout deleteBL2 = new BoxLayout(deleteRow2, BoxLayout.X_AXIS);
        deleteRow2.setLayout(deleteBL2);
        deletePanel.add(deleteRow2);
        deleteCurrentFile = new JLabel();
        deleteRow2.add(new JLabel(t.get("progress.details.current")+" "));
        deleteRow2.add(deleteCurrentFile);
        d = deletePanel.getPreferredSize();
        deletePanel.setPreferredSize(new Dimension(width, d.height));

        // Create copy panel:
        copyPanel = new JPanel(new GridLayout(4, 1));
        copyPanel.setBorder(new TitledBorder(t.get("progress.details.title")));
        JPanel copyRow1 = new JPanel();
        BoxLayout copyBL1 = new BoxLayout(copyRow1, BoxLayout.X_AXIS);
        copyRow1.setLayout(copyBL1);
        copyPanel.add(copyRow1);
        copyStmtsNo = new JLabel();
        copyRow1.add(new JLabel(t.get("progress.details.statementsNo")+" "));
        copyRow1.add(copyStmtsNo);
        JPanel copyRow2 = new JPanel();
        BoxLayout copyBL2 = new BoxLayout(copyRow2, BoxLayout.X_AXIS);
        copyRow2.setLayout(copyBL2);
        copyPanel.add(copyRow2);
        copyBytes = new JLabel();
        copyRow2.add(new JLabel(t.get("progress.details.bytes")+" "));
        copyRow2.add(copyBytes);
        JPanel copyRow3 = new JPanel();
        BoxLayout copyBL3 = new BoxLayout(copyRow3, BoxLayout.X_AXIS);
        copyRow3.setLayout(copyBL3);
        copyPanel.add(copyRow3);
        copyCurrentFile = new JLabel();
        copyRow3.add(new JLabel(t.get("progress.details.current")+" "));
        copyRow3.add(copyCurrentFile);
        JPanel copyRow4 = new JPanel();
        BoxLayout copyBL4 = new BoxLayout(copyRow4, BoxLayout.X_AXIS);
        copyRow4.setLayout(copyBL4);
        copyPanel.add(copyRow4);
        copyBytesCurrentFile = new JLabel();
        copyRow4.add(new JLabel(t.get("progress.details.bytesCurrent")+" "));
        copyRow4.add(copyBytesCurrentFile);
        d = copyPanel.getPreferredSize();
        copyPanel.setPreferredSize(new Dimension(width, d.height));

        // Create panel containing all three details panels:
        JPanel detailsPanel = new JPanel();
        BoxLayout detailsBL = new BoxLayout(detailsPanel, BoxLayout.X_AXIS);
        detailsPanel.setLayout(detailsBL);
        detailsPanel.add(comparisonPanel);
        detailsPanel.add(deletePanel);
        detailsPanel.add(copyPanel);

        // Create cancel button in a separate panel:
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel", this));

        // Add all panels:
        cp.add(statePanel, BorderLayout.NORTH);
        cp.add(detailsPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        // Hide details and reset dialog:
        // hideDetails();
        pack();
        reset();
    }


    /**
     * Resets the dialog.
     */
    public final void reset() {
        synchronized (this) {
            // Center the dialog before each computation to reflect changes of
            // the main frame:
            JFSSupport.center(mainView.getFrame(), this);
            completionBar.setValue(0);
            setTitle(JFSText.getInstance().get("progress.processing"));
        }
    }


    /**
     * Shows all details.
     */
    private void showDetails() {
        synchronized (this) {
            JFSText t = JFSText.getInstance();
            JFSProgress progress = JFSProgress.getInstance();
            doShowDetails = true;
            toggleDetailsButton.setText(t.get("progress.details.hide"));
            ProgressActivity a = progress.getActivity();
            if (a==ProgressActivity.COMPARISON) {
                comparisonPanel.setVisible(true);
                deletePanel.setVisible(false);
                copyPanel.setVisible(false);
            } else {
                if (a==ProgressActivity.SYNCHRONIZATION_DELETE) {
                    comparisonPanel.setVisible(false);
                    deletePanel.setVisible(true);
                    copyPanel.setVisible(false);
                } else {
                    if (a==ProgressActivity.SYNCHRONIZATION_COPY) {
                        comparisonPanel.setVisible(false);
                        deletePanel.setVisible(false);
                        copyPanel.setVisible(true);
                    }
                }
            }
        }
    }


    /**
     * Hides all details.
     */
    private void hideDetails() {
        synchronized (this) {
            JFSText t = JFSText.getInstance();
            doShowDetails = false;
            toggleDetailsButton.setText(t.get("progress.details.show"));
            comparisonPanel.setVisible(false);
            deletePanel.setVisible(false);
            copyPanel.setVisible(false);
        }
    }


    /**
     * Sets details about the current operation.
     */
    public final void setDetails() {
        if (!doShowDetails) {
            return;
        }

        synchronized (this) {
            JFSProgress progress = JFSProgress.getInstance();
            ProgressActivity a = progress.getActivity();

            if (a==ProgressActivity.COMPARISON&&comparisonPanel.isVisible()) {
                JFSComparisonMonitor cm = JFSComparisonMonitor.getInstance();
                if (cm.getCurrentDir()!=null) {
                    comparisonCurrentDir.setText(cm.getCurrentDir().getRelativePath());
                }
            } else {
                if (a==ProgressActivity.SYNCHRONIZATION_DELETE&&deletePanel.isVisible()) {
                    JFSDeleteMonitor dm = JFSDeleteMonitor.getInstance();
                    deleteStmtsNo.setText(dm.getFilesDeleted()+"/"+dm.getFilesToDelete());
                    if (dm.getCurrentFile()!=null) {
                        deleteCurrentFile.setText(dm.getCurrentFile().getRelativePath());
                    }
                } else {
                    if (a==ProgressActivity.SYNCHRONIZATION_COPY&&copyPanel.isVisible()) {
                        JFSCopyMonitor cm = JFSCopyMonitor.getInstance();
                        copyStmtsNo.setText(cm.getFilesCopied()+"/"+cm.getFilesToCopy());
                        copyBytes.setText(JFSFormatter.getLength(cm.getBytesTransfered())+"/"
                                +JFSFormatter.getLength(cm.getBytesToTransfer()));
                        if (cm.getCurrentFile()!=null) {
                            copyCurrentFile.setText(cm.getCurrentFile().getRelativePath());
                            copyBytesCurrentFile.setText(JFSFormatter.getLength(cm.getBytesTransferedCurrentFile())+"/"
                                    +JFSFormatter.getLength(cm.getBytesToTransferCurrentFile()));
                        }
                    }
                }
            }
        }
    }


    /**
     * @see JFSProgressObserver#update(JFSProgress)
     */
    @Override
    public final void update(JFSProgress progress) {
        synchronized (this) {
            JFSText t = JFSText.getInstance();
            ProgressActivity a = progress.getActivity();
            ProgressState s = progress.getState();
            remainingTime.setText(progress.getRemainingTime());

            if (s==ProgressState.PREPARATION) {
                reset();
                setTitle(t.get(a.getName()));
                if (doShowDetails) {
                    showDetails();
                } else {
                    hideDetails();
                }
                pack();
                JFSSupport.center(getParent(), this);
                repaint();
                mainView.updateComparisonTable();
            } else {
                if (s==ProgressState.DONE) {
                    completionBar.setValue(100);
                    repaint();
                    reset();
                    mainView.update();
                } else {
                    if (s==ProgressState.ACTIVE) {
                        completionBar.setValue(progress.getCompletionRatio());
                        setDetails();
                        repaint();
                        mainView.updateComparisonTable();
                    }
                }
            }
        }
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if ("TOGGLE_DETAILS".equals(cmd)) {
            if (doShowDetails) {
                hideDetails();
            } else {
                showDetails();
            }
            pack();
            JFSSupport.center(getParent(), this);
        }

        if ("button.cancel".equals(cmd)) {
            JFSProgress.getInstance().cancel();
        }
    }


    /**
     * Performs the comparison as a separate thread.
     */
    public final void compareInThread() {
        final JDialog dialog = this;

        // Create new thread:
        final Thread thread = new Thread() {
            @Override
            public void run() {
                // Wait for dialog to appear:
                while (!dialog.isVisible()) {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException ie) {

                    }
                }

                // Compare:
                JFSComparison.getInstance().compare();

                // Hide dialog:
                dialog.setVisible(false);
            }

        };

        // Start thread:
        thread.start();

        // Make dialog window visible:
        dialog.setVisible(true);
    }


    /**
     * Performs the synchronization as a separate thread.
     */
    public final void synchronizeInThread() {
        final JDialog dialog = this;

        // Create new thread:
        final Thread thread = new Thread() {
            @Override
            public void run() {
                // Wait for dialog to appear:
                while (!dialog.isVisible()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ie) {
                        ;
                    }
                }

                // Synchronize:
                JFSSynchronization.getInstance().synchronize();

                // Hide dialog:
                dialog.setVisible(false);

            }

        };

        // Start thread:
        thread.start();

        // Make dialog window visible:
        dialog.setVisible(true);
    }

}
