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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import jfs.conf.JFSConfig;
import jfs.conf.JFSConst;
import jfs.conf.JFSLog;
import jfs.conf.JFSSettings;
import jfs.conf.JFSSyncModes;
import jfs.conf.JFSText;
import jfs.conf.JFSViewModes;
import jfs.sync.JFSComparison;
import jfs.sync.JFSCopyStatement;
import jfs.sync.JFSDeleteStatement;
import jfs.sync.JFSProgress;
import jfs.sync.JFSSynchronization;
import jfs.sync.JFSTable;

/**
 * The JFS shell performs a command line comparison and synchronization. First
 * of all the specified directory pair is compared. After that, you may enter
 * different options via a shell-like environment or skip the shell and directly
 * perform the synchronization depeding on the command line options.
 *
 * @author Jens Heidrich
 * @version $Id: JFSShell.java,v 1.11 2007/02/26 18:49:11 heidrich Exp $
 */
public final class JFSShell {


    private JFSShell() {
    }

	/**
	 * Reads a positive number out of a string of the following form: "[command]
	 * [number]"
	 *
	 * @param input
	 *            The input string.
	 * @param min
	 *            The minimum value of the parsed number.
	 * @param max
	 *            The maximum value of the parsed number.
	 * @return The parsed number or '-1' if the parsing failed.
	 */
	public static int parseInt(String input, int min, int max) {
		// Get translation object:
		JFSText t = JFSText.getInstance();

		String[] cmd = input.split(" ");
		int number = -1;

		if (cmd.length == 2) {
			try {
				number = Integer.parseInt(cmd[1]);

				if ((number < min) || (number > max)) {
					JFSLog.getErr().getStream().println(
							t.get("error.validIndex") + " '" + number + "'.");
					number = -1;
				}
			} catch (NumberFormatException e) {
				JFSLog.getErr().getStream()
						.println(t.get("error.numberFormat"));
			}
		} else {
			JFSLog.getErr().getStream().println(t.get("error.inputNumber") + " '" + cmd.length + "'.");
                }

		return number;
	}

	/**
	 * Prints the content of URL to standard out.
	 *
	 * @param url
	 *            The input url.
	 */
	public static void printURL(URL url) {
		// Get translation object:
		JFSText t = JFSText.getInstance();

		InputStream stream;
		int c;

		try {
			stream = url.openStream();

			while ((c = stream.read()) != -1) {
				JFSLog.getOut().getStream().print((char) c);
			}

			stream.close();
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(
					t.get("error.io") + " '" + url + "'.");
		}
	}

	/**
	 * Starts the JFS command line shell in order to perform comparisons and
	 * synchronizations.
	 *
	 * @param quiet
	 *            This option has to be true, if comparison and synchronization
	 *            should run in background. It has to be false, if a shell
	 *            prompt should appear.
	 */
	public static void startShell(boolean quiet) {
		// Get translation object, configuration, output stream, and task:
		JFSText t = JFSText.getInstance();
		JFSConfig config = JFSConfig.getInstance();
		PrintStream p = JFSLog.getOut().getStream();
		JFSTable table = JFSTable.getInstance();
		JFSComparison comparison = JFSComparison.getInstance();
		JFSSynchronization synchronization = JFSSynchronization.getInstance();

		// Start Command Line Processing:
		JFSPrint.simplePrint();

		p.println(t.get("cmd.init"));
		p.println();

		JFSProgress.getInstance().attach(new JFSProgressPrint());

		// Set question oracle if not quiet:
		if (!quiet) {
			synchronization.getQuestion().setOracle(new JFSQuestionPrint());
                }

		p.println(t.get("cmd.startComp"));
		p.println();
		comparison.compare();
		synchronization.computeSynchronizationLists();

		if (!quiet) {
			// Start the JFileSync Shell:
			BufferedReader din = new BufferedReader(new InputStreamReader(
					System.in));
			p.println();
			p.println(t.get("cmd.shell"));
			p.println();

			String input = "";

			while (!"exit".equals(input) && !"sync".equals(input)) {
				p.print("jfs>");

				try {
					input = din.readLine().toLowerCase();

					if ("t".equals(input)) {
						JFSPrint.printComparisonTable();
					} else if ("c".equals(input)) {
						JFSPrint.printCopyStatements(table.getCopyStatements());
					} else if ("d".equals(input)) {
						JFSPrint.printDeleteStatements(table
								.getDeleteStatements());
					} else if (input.startsWith("c ")) {
						List<JFSCopyStatement> list = table.getCopyStatements();
						int number = JFSShell.parseInt(input, 1, list.size());

						if (number != -1) {
							JFSCopyStatement cs = list.get(number - 1);
							cs.setCopyFlag(!cs.getCopyFlag());
						}
					} else if (input.startsWith("d ")) {
						List<JFSDeleteStatement> list = table.getDeleteStatements();
						int number = JFSShell.parseInt(input, 1, list.size());

						if (number != -1) {
							JFSDeleteStatement ds = list.get(number - 1);
							ds.setDeleteFlag(!ds.getDeleteFlag());
						}
					} else if (input.startsWith("view ")) {
						int number = JFSShell.parseInt(input, 0,
								Integer.MAX_VALUE);

						// If the argument is a valid number then set a
						// new view in the configuration object:
						if (JFSViewModes.getInstance().contains(number)) {
							config.setView((byte) number);
						} else {
							JFSLog.getErr().getStream().println(
									t.get("error.numberFormat"));
						}
					} else if (input.startsWith("sync ")) {
						int number = JFSShell.parseInt(input, 0,
								Integer.MAX_VALUE);

						// If the argument is a valid number then set the
						// new mode in the configuration object and re-init
						// the tables of files to copy and to delete
						// appropriately:
						JFSSyncModes modes = JFSSyncModes.getInstance();
						if (modes.contains(number)) {
							config.setSyncMode((byte) number);
							JFSTable.getInstance().recomputeActionsAndView();
							synchronization.computeSynchronizationLists();
						} else {
							JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
						}
					} else if ("help".equals(input)) {
						JFSShell.printURL(JFSConst.getInstance().getResourceUrl("jfs.help.topic.shell"));
					} else if ("sync".equals(input)) {
						synchronize();
					} else if (input.length() > 0) {
						JFSLog.getErr().getStream().println(t.get("error.validCommand"));
					}
				} catch (IOException e) {
					// Thrown by readLine(). Continue in this case.
					JFSLog.getErr().getStream().println(
							t.get("error.inputRead"));
				}
			}

		} else {
			synchronize();
		}

		p.println();
		p.println(t.get("cmd.exit"));
		p.println();

		// Store settings:
		JFSSettings.getInstance().store();
	}

	/**
	 * Performs a command line synchronization.
	 */
	private static void synchronize() {
		// Start synchronization:
		PrintStream p = JFSLog.getOut().getStream();
		JFSTable table = JFSTable.getInstance();
		JFSSynchronization synchronization = JFSSynchronization.getInstance();

		p.println();
		p.println(JFSText.getInstance().get("cmd.startSync"));
		p.println();
		synchronization.synchronize();

		// Print all failed copy and delete statements:
		p.println();
		JFSPrint.printFailedCopyStatements(table.getFailedCopyStatements());
		JFSPrint.printFailedDeleteStatements(table.getFailedDeleteStatements());
	}
}