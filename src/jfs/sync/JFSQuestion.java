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
package jfs.sync;

import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSText;


/**
 * Represents a question the synchronization algorithm must ask the user.
 *
 * @author Jens Heidrich
 * @version $Id: JFSQuestion.java,v 1.7 2007/02/26 18:49:09 heidrich Exp $
 */
public class JFSQuestion {

    /**
     * The possible answers a oracle may give.
     */
    public enum QuestionAnswer {

        DO, SKIP, SKIP_ALL

    }

    /**
     * The element the question is asked for.
     */
    private JFSElement jfsElement;

    /**
     * The action for the element the question is asked for.
     */
    private SyncAction action = SyncAction.NOP;

    /**
     * The assigned JFS question oracle.
     */
    private JFSQuestionOracle oracle = null;


    /**
     * Returns the element the question is asked for.
     *
     * @return The JFS element of the comparison table.
     */
    public JFSElement getElement() {
        return jfsElement;
    }


    /**
     * Returns the action to perform for the element the question is asked for.
     *
     * @return The synchronization action.
     */
    public SyncAction getAction() {
        return action;
    }


    /**
     * Returns the text for the question.
     *
     * @return The text as a string.
     */
    public String getQuestionText() {
        if (jfsElement==null) {
            return "";
        }

        JFSText t = JFSText.getInstance();
        if (jfsElement.getAction()==SyncAction.ASK_LENGTH_INCONSISTENT) {
            return t.get("syncQuestion.lengthInconsistent");
        } else if (jfsElement.getAction()==SyncAction.ASK_FILES_GT_HISTORY) {
            return t.get("syncQuestion.filesGtHistory");
        } else if (jfsElement.getAction()==SyncAction.ASK_FILES_NOT_IN_HISTORY) {
            return t.get("syncQuestion.filesNotInHistory");
        }

        return "";
    }


    /**
     * Sets the action to perform for the element the question is asked for.
     *
     * @param action
     * The action to set.
     */
    public void setAction(SyncAction action) {
        this.action = action;
    }


    /**
     * Sets the oracle to use.
     *
     * @param oracle
     * The new oracle.
     */
    public final void setOracle(JFSQuestionOracle oracle) {
        this.oracle = oracle;
    }


    /**
     * Answers a question by asking the oracle to request the user to decide
     * what to do with a specific JFS element of the comparison table. If the
     * user makes a decision, the action assigned to the element is updated
     * accordingly. If no oracle is assigned the question is skipped.
     *
     * @param element
     * The element to request an answer for.
     * @return The given answer by the oracle.
     */
    public final QuestionAnswer answer(JFSElement element) {
        if (oracle==null) {
            return QuestionAnswer.SKIP;
        }

        jfsElement = element;
        action = element.getAction();
        QuestionAnswer answer = oracle.ask(this);

        if (answer==QuestionAnswer.DO) {
            element.setAction(getAction());
            element.setManuallySetAction(true);
        }

        return answer;
    }

}
