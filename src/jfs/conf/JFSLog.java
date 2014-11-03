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

package jfs.conf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles log messages (including ERRor messages). Log messages can
 * be send to a log file (e.g., when JFS is started with its GUI) or to any
 * print stream.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLog.java,v 1.7 2007/02/26 18:50:12 heidrich Exp $
 */
public class JFSLog {
    
	/** The ERRor message log. */
	private static final JFSLog ERR = new JFSLog(System.err, JFSConst.ERR_LOG_FILE);

	/** The standard OUTput message log. */
	private static final JFSLog OUT = new JFSLog(System.out, JFSConst.OUT_LOG_FILE);

	/** The log message stream to use. */
	private final PrintStream printStream;

	/** The file containing all log messages. */
	private final File logFile;

	/** The print stream to use for OUTputting log messages. */
	private PrintStream currentStream;

	/** Determines whether unread log messages are available. */
	private boolean unreadLogMessages = false;

	/** Vector with all oberservers of the configuration object. */
	private final List<JFSLogObserver> observers = new ArrayList<>();

	/**
	 * Creates a new log. Per default the log messages are OUTputed to the log stream.
	 * 
	 * @param printStream
	 *            The print stream to use.
	 * @param logFileName
	 *            The log file name to use. The log file is stored in the JFS
	 *            settings directory (usually '~/.jfs').
	 */
	public JFSLog(PrintStream printStream, String logFileName) {
		this.printStream = printStream;
		logFile = new File(JFSConst.HOME_DIR + File.separator + logFileName);
		currentStream = printStream;
	}

	/**
	 * Determines whether the log file should be used for OUTputting log messages or the print stream. 
         * The log file is located in the JFS settings directory of the users home directory (usually '~/.jfs'). 
         * The log file and the settings directory are created if they don't exist .
	 * 
	 * @param useLogFile
	 *            True if and only if the log file should be used.
	 */
	public void useLogFile(boolean useLogFile) {
		if (useLogFile) {
			// Redirect error stream to file:
			File home = new File(JFSConst.HOME_DIR);

			if (!home.exists()) {
				home.mkdir();
                        }

			try {
				if (!logFile.exists()) {
					logFile.createNewFile();
                                }
				currentStream = new PrintStream(new FileOutputStream(logFile));
			} catch (IOException e) {
				System.err.println(e);
			}
		} else {
			currentStream = printStream;
		}
	}

	/**
	 * Resets the log file in the JFS configuration directory to which log
	 * messages are sent to. It is deleted and created from scratch if it
	 * already exists. The log stream is redirected to the newly created log
	 * file afterwards.
	 */
	public void resetLogFile() {
		unreadLogMessages = false;

		// Delete previous log file and create new one:
		if (logFile.exists()) {
			logFile.delete();
                }

		useLogFile(true);
	}

	/**
	 * Returns the the log file URL.
	 * 
	 * @return The log file URL.
	 * @throws MalformedURLException
	 *             Thrown in case of a malformed URL.
	 */
	public URL getLogURL() throws MalformedURLException {
		return logFile.toURI().toURL();
	}

	/**
	 * Returns the current log stream to OUTput log messages to. If the stream
	 * is requested, it is assumed that new log messages are written to the
	 * stream, so that the log object has unread log messages.
	 * 
	 * @return The log stream.
	 */
	public synchronized PrintStream getStream() {
		unreadLogMessages = true;
		fireUpdate();
		return currentStream;
	}

	/**
	 * Returns whether unread log messages are available.
	 * 
	 * @return True if and only if new messages are available.
	 */
	public boolean hasUnreadLogMessages() {
		return unreadLogMessages;
	}

	/**
	 * Determines that the log has no unread log messages.
	 */
	public void setLogMessagesRead() {
		unreadLogMessages = false;
		fireUpdate();
	}

	/**
	 * Attaches an additional observer.
	 * 
	 * @param observer
	 *            The new observer.
	 */
	public final void attach(JFSLogObserver observer) {
		observers.add(observer);
		observer.update(this);
	}

	/**
	 * Detaches an existing observer.
	 * 
	 * @param observer
	 *            An old observer.
	 */
	public final void detach(JFSLogObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Updates the observers.
	 */
	private final void fireUpdate() {
		for (JFSLogObserver observer : observers) {
			observer.update(this);
		}
	}

	/**
	 * Returns the ERRor message log.
	 * 
	 * @return The log object.
	 */
	public static JFSLog getErr() {
		return ERR;
	}

	/**
	 * Returns the standard OUTput message log.
	 * 
	 * @return The log object.
	 */
	public static JFSLog getOut() {
		return OUT;
	}
}