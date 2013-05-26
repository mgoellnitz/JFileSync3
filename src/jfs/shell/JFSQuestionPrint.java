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

package jfs.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import jfs.conf.JFSLog;
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
 * @version $Id: JFSQuestionPrint.java,v 1.6 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSQuestionPrint implements JFSQuestionOracle {

    /**
     * @see JFSQuestionOracle#ask(JFSQuestion)
     */
    @Override
    public final QuestionAnswer ask(JFSQuestion question) {
        PrintStream p = JFSLog.getOut().getStream();
        JFSText t = JFSText.getInstance();
        JFSElement e = question.getElement();

        p.println();
        p.println(question.getQuestionText());
        p.println("  "+t.get("syncQuestion.print.src")+" "+JFSFormatter.adapt(e.getSrcFile().getPath(), 40)+" ("
                +JFSFormatter.getLastModified(e.getSrcFile())+")");
        p.println("  "+t.get("syncQuestion.print.tgt")+" "+JFSFormatter.adapt(e.getTgtFile().getPath(), 40)+" ("
                +JFSFormatter.getLastModified(e.getTgtFile())+")");
        p.println();
        p.println(t.get("syncQuestion.print.actions.help"));
        p.println("  "+t.get("syncQuestion.print.actions"));

        BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = din.readLine().toLowerCase();
            if (input.equals("1")) {
                p.println("  "+t.get("syncQuestion.print.action.copySrc"));
                p.println();
                question.setAction(SyncAction.COPY_SRC);
                return QuestionAnswer.DO;
            } else if (input.equals("2")) {
                p.println("  "+t.get("syncQuestion.print.action.copyTgt"));
                p.println();
                question.setAction(SyncAction.COPY_TGT);
                return QuestionAnswer.DO;
            } else if (input.equals("4")) {
                p.println("  "+t.get("syncQuestion.print.action.skipAll"));
                p.println();
                return QuestionAnswer.SKIP_ALL;
            }
        } catch (IOException exeption) {
            JFSLog.getErr().getStream().println(t.get("error.inputRead"));
        }

        // Skip question by default:
        p.println("  "+t.get("syncQuestion.print.action.skip"));
        p.println();
        return QuestionAnswer.SKIP;
    }
}