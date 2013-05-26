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

import jfs.sync.JFSQuestion.QuestionAnswer;

/**
 * An interface for a JFS questions oracle which answers questions the algorithm
 * has during its performance.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSQuestionOracle.java,v 1.5 2007/02/26 18:49:09 heidrich Exp $
 */
public interface JFSQuestionOracle {

	/**
	 * This method is called every time the question needs to be asked and
	 * visualized.
	 * 
	 * @param question
	 *            The question to ask.
	 */
	public QuestionAnswer ask(JFSQuestion question);
}