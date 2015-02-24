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

import java.io.PrintStream;
import java.util.List;
import jfs.conf.JFSConfig;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSFilter;
import jfs.conf.JFSLog;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSText;
import jfs.sync.JFSCopyStatement;
import jfs.sync.JFSDeleteStatement;
import jfs.sync.JFSElement;
import jfs.sync.JFSFile;
import jfs.sync.JFSFormatter;
import jfs.sync.JFSTable;

/**
 * This class performs all necessary formatting operations for printing the
 * configuration (profile), the synchronization table, and the copy and delete
 * statements.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSPrint.java,v 1.9 2007/02/26 18:49:11 heidrich Exp $
 */
public final class JFSPrint {


    private JFSPrint() {
    }
    
    
	/**
	 * Prints the whole configuration to standard out.
	 */
	public static void simplePrint() {
		JFSConfig config = JFSConfig.getInstance();

		// Get translation object:
		JFSText t = JFSText.getInstance();
		PrintStream p = JFSLog.getOut().getStream();

		// Print basic settings:
		p.println(t.get("profile.print.heading"));
		p
				.println("  " + t.get("profile.print.title") + " "
						+ config.getTitle());
		p.println("  " + t.get("profile.print.syncMode") + " #"
				+ config.getSyncMode());
		p.println("  " + t.get("profile.print.view") + " #" + config.getView());
		if (config.getDirectoryList().size() > 0) {
			p.println("  " + t.get("profile.print.dir.heading"));

			for (JFSDirectoryPair pair : config.getDirectoryList()) {
				p.println("    '" + pair.getSrc() + "' "
						+ t.get("profile.print.dir.connector") + " '"
						+ pair.getTgt() + "'");
			}
		}

		// Print advanced settings:
		p.println(t.get("profile.print.advanced.title"));
		p.println("  " + t.get("profile.print.granularity") + " "
				+ config.getGranularity());
		p.println("  " + t.get("profile.print.bufferSize") + " "
				+ config.getBufferSize());
		p.println("  " + t.get("profile.print.keepUserActions") + " "
				+ config.isKeepUserActions());
		p.println("  " + t.get("profile.print.storeHistory") + " "
				+ config.isStoreHistory());

		// Print includes and excludes:
		if (config.getIncludes().size() > 0) {
			p.println("  " + t.get("profile.print.includes.heading"));

			for (JFSFilter f : config.getIncludes()) {
				p.print("    [");
				if (f.isActive()) {
					p.print("X");
                                } else {
					p.print(" ");
                                }
				p.println("] '" + f.getFilter());
			}
		}
		if (config.getExcludes().size() > 0) {
			p.println("  " + t.get("profile.print.excludes.heading"));

			for (JFSFilter f : config.getExcludes()) {
				p.print("    [");
				if (f.isActive()) {
					p.print("X");
                                } else {
					p.print(" ");
                                }
				p.println("] '" + f.getFilter());
			}
		}

		// Print server settings:
		p.println(t.get("profile.print.server.title"));
		p.println("  " + t.get("profile.print.server.user.name") + " "
				+ config.getServerUserName());
		p.println("  " + t.get("profile.print.server.pass.phrase") + " "
				+ config.getServerPassPhrase());
		p.println("  " + t.get("profile.print.server.timeout") + " "
				+ config.getServerTimeout());
		p.println();
	}

	/**
	 * Prints the comparison table.
	 */
	public static void printComparisonTable() {
		JFSText t = JFSText.getInstance();
		JFSTable table = JFSTable.getInstance();

		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.print.comparison.title"));

		for (int i = 0; i < table.getViewSize(); i++) {
			JFSElement element = table.getViewElement(i);
			JFSFile src = element.getSrcFile();
			JFSFile tgt = element.getTgtFile();

			// Compute source string:
			String srcString = null;

			if (src != null) {
				if (element.isDirectory()) {
					srcString = src.getPath();
                                } else {
					srcString = src.getName();
                                }
                        }

			if (element.isDirectory()) {
				srcString = JFSFormatter.adapt(srcString, 32);
                        } else {
				srcString = "  " + JFSFormatter.adapt(srcString, 30);
                        }

			// Compute action string:
			String actionString;

			if (element.getAction() == SyncAction.NOP_ROOT || element.getAction() == SyncAction.NOP)
				actionString = "   ";
			else if (element.getAction() == SyncAction.COPY_SRC)
				actionString = " > ";
			else if (element.getAction() == SyncAction.COPY_TGT)
				actionString = " < ";
			else if (element.getAction() == SyncAction.DELETE_SRC
					|| element.getAction() == SyncAction.DELETE_TGT
					|| element.getAction() == SyncAction.DELETE_SRC_AND_TGT)
				actionString = " - ";
                        else {
				actionString = " ? ";
                        }

			// Compute target string:
			String tgtString = null;

			if (tgt != null) {
				if (element.isDirectory()) {
					tgtString = tgt.getPath();
                                } else {
					tgtString = tgt.getName();
                                }
                        }

			if (element.isDirectory()) {
				tgtString = JFSFormatter.adapt(tgtString, 32);
                        } else {
				tgtString = "  " + JFSFormatter.adapt(tgtString, 30);
                        }

			// Print row:
			out.println(srcString + actionString + tgtString);
		}

		out.println(t.get("cmd.print.comparison.help"));
		out.println(t.get("cmd.print.count") + " " + table.getViewSize());
		out.println();
	}

	/**
	 * Prints all copy statements.
	 * 
	 * @param copyStatements
	 *            The copy statements to be printed.
	 */
	public static void printCopyStatements(List<JFSCopyStatement> copyStatements) {
		// Get translation object:
		JFSText t = JFSText.getInstance();

		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.print.copyFile.title"));

		// Print information about the files to copy:
		int i = 0;
		for (JFSCopyStatement cs : copyStatements) {
			String srcString = JFSFormatter.adapt(cs.getSrc().getPath(), 27);
			String tgtString = JFSFormatter.adapt(cs.getTgt().getPath(), 27);
			String copyFlag;

			if (cs.getCopyFlag()) {
				copyFlag = "X";
                        } else {
				copyFlag = " ";
                        }

			out.println("[" + copyFlag + "] "
					+ t.get("cmd.print.copyFile.command") + " " + srcString
					+ " " + t.get("cmd.print.copyFile.connector") + " "
					+ tgtString + " (" + t.get("cmd.print.number") + " "
					+ (i + 1) + ")");
			i++;
		}

		out.println(t.get("cmd.print.count") + " " + copyStatements.size());
		out.println();
	}

	/**
	 * Prints all delete statements.
	 * 
	 * @param deleteStatements
	 *            The delete statements to be printed.
	 */
	public static void printDeleteStatements(List<JFSDeleteStatement> deleteStatements) {
		// Get translation object:
		JFSText t = JFSText.getInstance();

		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.print.deleteFile.title"));

		// Print information about the files to delete:
		int i = 0;
		for (JFSDeleteStatement ds : deleteStatements) {
			String fileString = JFSFormatter.adapt(ds.getFile().getPath(), 56);
			String deleteFlag;

			if (ds.getDeleteFlag()) {
				deleteFlag = "X";
                        } else {
				deleteFlag = " ";
                        }

			out.println("[" + deleteFlag + "] "
					+ t.get("cmd.print.deleteFile.command") + " " + fileString
					+ " (" + t.get("cmd.print.number") + " " + (i + 1) + ")");
			i++;
		}

		out.println(t.get("cmd.print.count") + " " + deleteStatements.size());
		out.println();
	}

	/**
	 * Prints information about files that have not been copied; i.e., the copy
	 * statement was not performed successfully. (Only files with an active copy
	 * flag are shown.) If the vector is empty nothing is printed.
	 * 
	 * @param copyStatements
	 *            The vector to be printed.
	 */
	public static void printFailedCopyStatements(List<JFSCopyStatement> copyStatements) {
		if (copyStatements.isEmpty()) {
			return;
                }

		// Get translation object:
		JFSText t = JFSText.getInstance();

		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.print.notCopiedFiles.title"));

		// Print information about not copied files:
		for (JFSCopyStatement cs : copyStatements) {
			String srcString = JFSFormatter.adapt(cs.getSrc().getPath(), 27);
			String tgtString = JFSFormatter.adapt(cs.getTgt().getPath(), 27);
			out.println(t.get("cmd.print.copyFile.command") + " " + srcString
					+ " " + t.get("cmd.print.copyFile.connector") + " "
					+ tgtString);
		}

		out.println(t.get("cmd.print.count") + " " + copyStatements.size());
		out.println();
	}

	/**
	 * Prints information about files that have not been deleted; i.e., the
	 * delete statement was not performed successfully. (Only files with active
	 * delete flag are shown.) If the vector is empty nothing is printed.
	 * 
	 * @param deleteStatements
	 *            The vector to be printed.
	 */
	public static void printFailedDeleteStatements(List<JFSDeleteStatement> deleteStatements) {
		if (deleteStatements.isEmpty()) {
			return;
                }

		// Get translation object:
		JFSText t = JFSText.getInstance();

		PrintStream out = JFSLog.getOut().getStream();
		out.println(t.get("cmd.print.notDeletedFiles.title"));

		// Print information about not deleted files:
		for (JFSDeleteStatement ds : deleteStatements) {
			String fileString = JFSFormatter.adapt(ds.getFile().getPath(), 56);
			out.println(t.get("cmd.print.deleteFile.command") + " " + fileString);
		}

		out.println(t.get("cmd.print.count") + " " + deleteStatements.size());
		out.println();
	}
        
}