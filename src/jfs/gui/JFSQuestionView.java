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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jfs.conf.JFSText;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.sync.JFSElement;
import jfs.sync.JFSFormatter;
import jfs.sync.JFSQuestion;
import jfs.sync.JFSQuestionOracle;
import jfs.sync.JFSQuestion.QuestionAnswer;

/**
 * A JFS questions oracle which asks the user on the command line to determine an action for a JFS element.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSQuestionView.java,v 1.10 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSQuestionView implements JFSQuestionOracle, ActionListener {
    /** The main view. */
    private JFSMainView mainView;

    /** The progress dialog. */
    private JDialog dialog;

    /** The question label. */
    private JLabel questionText = new JLabel();

    /** The source file. */
    private JTextField srcFile = new JTextField();

    /** The target file. */
    private JTextField tgtFile = new JTextField();

    /** The source last modified date label. */
    private JLabel srcLastModified = new JLabel();

    /** The target last modified date label. */
    private JLabel tgtLastModified = new JLabel();

    /** The source size label. */
    private JLabel srcSize = new JLabel();

    /** The target size label. */
    private JLabel tgtSize = new JLabel();

    /** The question to answer. */
    private JFSQuestion jfsQuestion;

    /** The given answer. */
    private QuestionAnswer answer;


    /**
     * Initializes the question view.
     * 
     * @param mainView
     *            The main frame.
     */
    public JFSQuestionView(JFSMainView mainView) {
        this.mainView = mainView;

        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Create the modal dialog:
        dialog = new JDialog(mainView.getFrame(), true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setTitle(t.get("syncQuestion.title"));

        Container cp = dialog.getContentPane();
        cp.setLayout(new BorderLayout());

        // Adapt labels and assign to panels:
        srcFile.setColumns(40);
        srcFile.setEditable(false);
        srcLastModified.setHorizontalAlignment(JLabel.RIGHT);
        srcSize.setHorizontalAlignment(JLabel.RIGHT);
        tgtFile.setColumns(40);
        tgtFile.setEditable(false);
        tgtLastModified.setHorizontalAlignment(JLabel.RIGHT);
        tgtSize.setHorizontalAlignment(JLabel.RIGHT);

        JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        questionPanel.add(questionText);

        JPanel srcInfoPanel = new JPanel(new GridLayout(2, 1));
        srcInfoPanel.add(srcLastModified);
        srcInfoPanel.add(srcSize);
        JPanel srcPanel = new JPanel();
        srcPanel.setBorder(new TitledBorder(t.get("syncQuestion.table.src")));
        srcPanel.add(srcFile);
        srcPanel.add(srcInfoPanel);

        JPanel tgtInfoPanel = new JPanel(new GridLayout(2, 1));
        tgtInfoPanel.add(tgtLastModified);
        tgtInfoPanel.add(tgtSize);
        JPanel tgtPanel = new JPanel();
        tgtPanel.setBorder(new TitledBorder(t.get("syncQuestion.table.tgt")));
        tgtPanel.add(tgtFile);
        tgtPanel.add(tgtInfoPanel);

        JPanel filesPanel = new JPanel(new GridLayout(2, 1));
        filesPanel.add(srcPanel);
        filesPanel.add(tgtPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(JFSSupport.getButton("syncQuestion.action.copySrc", "syncAction.copySrc", this));
        buttonPanel.add(JFSSupport.getButton("syncQuestion.action.copyTgt", "syncAction.copyTgt", this));
        buttonPanel.add(JFSSupport.getButton("syncQuestion.action.skip", "SKIP", this));
        buttonPanel.add(JFSSupport.getButton("syncQuestion.action.skipAll", "SKIP_ALL", this));

        cp.add(questionPanel, BorderLayout.NORTH);
        cp.add(filesPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);
    }


    /**
     * @see JFSQuestionOracle#ask(JFSQuestion)
     */
    @Override
    public final QuestionAnswer ask(JFSQuestion question) {
        jfsQuestion = question;
        answer = QuestionAnswer.SKIP;

        // Adapt fields and pack dialog:
        JFSElement e = question.getElement();
        questionText.setText(question.getQuestionText());
        srcFile.setText(e.getSrcFile().getPath());
        srcLastModified.setText(JFSFormatter.getLastModified(e.getSrcFile()));
        srcSize.setText(JFSFormatter.getLength(e.getSrcFile()));
        tgtFile.setText(e.getTgtFile().getPath());
        tgtLastModified.setText(JFSFormatter.getLastModified(e.getTgtFile()));
        tgtSize.setText(JFSFormatter.getLength(e.getTgtFile()));

        // Make dialog window visible:
        dialog.pack();
        JFSSupport.center(mainView.getFrame(), dialog);
        dialog.setVisible(true);

        // Wait until dialog is not visible any more and return:
        return answer;
    }


    /**
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if (cmd.equals("syncAction.copySrc")) {
            jfsQuestion.setAction(SyncAction.COPY_SRC);
            answer = QuestionAnswer.DO;
        }

        if (cmd.equals("syncAction.copyTgt")) {
            jfsQuestion.setAction(SyncAction.COPY_TGT);
            answer = QuestionAnswer.DO;
        }

        if (cmd.equals("SKIP")) {
            answer = QuestionAnswer.SKIP;
        }

        if (cmd.equals("SKIP_ALL")) {
            answer = QuestionAnswer.SKIP_ALL;
        }

        if ( !cmd.equals("")) {
            dialog.setVisible(false);
            mainView.update();
        }
    }
}