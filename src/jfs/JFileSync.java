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
package jfs;

import java.awt.Image;
import java.awt.Taskbar;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import jfs.conf.JFSConfig;
import jfs.conf.JFSConst;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSFilter;
import jfs.conf.JFSLog;
import jfs.conf.JFSSettings;
import jfs.conf.JFSText;
import jfs.gui.JFSMainView;
import jfs.shell.JFSShell;
import jfs.sync.JFSTable;


/**
 * JFileSync is an application for synchronizing pairs of directories. This class is the main class of the application.
 * It processes all command line options and starts (1) a JFS server, (2) the Java Swing-based graphical user interface,
 * or (3) a command line shell.
 *
 * @see JFSServerFactory#startCmdLineServer()
 * @see jfs.gui.JFSMainView
 * @see JFSShell#startShell(boolean)
 * @author Jens Heidrich
 * @version $Id: JFileSync.java,v 1.40 2007/07/20 15:24:22 heidrich Exp $
 */
public final class JFileSync {

    private JFileSync() {
    }


    /**
     * Determines the home directory, where the JAR file respectively the class files are located.
     */
    public static String getHome() {
        try {
            URL packageUrl = ClassLoader.getSystemResource("jfs");
            File jfsLibDir = null;

            // JFS started from JAR:
            if (packageUrl.getProtocol().equals("jar")) {
                URL jarUrl = new URL(packageUrl.getFile());
                File jfsFile = new File(jarUrl.toURI());
                jfsLibDir = jfsFile.getParentFile().getParentFile();
            }

            // JFS started from classes directory:
            if (packageUrl.getProtocol().equals("file")) {
                File jfsFile = new File(packageUrl.toURI());
                jfsLibDir = jfsFile.getParentFile();
            }

            return jfsLibDir.getPath();
        } catch (Exception e) {
            return ".";
        }
    }


    /**
     * Start of the application.
     *
     * @param args
     * Command line arguments.
     */
    public static void main(String[] args) {
        PrintStream p = JFSLog.getOut().getStream();
        JFSConst constants = JFSConst.getInstance();
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            ImageIcon dockImage = new ImageIcon(constants.getIconUrl("jfs.icon.dock"));
            taskbar.setIconImage(dockImage.getImage());
        } catch (UnsupportedOperationException e) {
            ; // ignored for non macOS machines
        }

        // Get settings for the first time in order to load stored
        // settings before doing any actions:
        JFSSettings s = JFSSettings.getInstance();

        // Get translation and configuration object:
        JFSText t = JFSText.getInstance();
        JFSConfig config = JFSConfig.getInstance();

        // Clean config before starting (if main method is used as service):
        config.clean();

        boolean quiet = false;
        boolean loadDefaultFile = true;
        boolean nogui = false;
        int i = 0;

        // Handle command line arguments:
        try {
            while (i<args.length) {
                if (args[i].equals("-debug")) {
                    s.setDebug(true);
                } else if (args[i].equals("-config")) {
                    i++;
                    config.load(new File(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-sync")) {
                    i++;
                    config.setSyncMode(Byte.parseByte(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-view")) {
                    i++;
                    config.setView(Byte.parseByte(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-dir")) {
                    i++;
                    config.addDirectoryPair(new JFSDirectoryPair(args[i], args[i+1]));
                    i++;
                    loadDefaultFile = false;
                } else if (args[i].equals("-granularity")) {
                    i++;
                    config.setGranularity(Integer.parseInt(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-buffersize")) {
                    i++;
                    config.setBufferSize(Integer.parseInt(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-overwriteuseractions")) {
                    config.setKeepUserActions(false);
                    loadDefaultFile = false;
                } else if (args[i].equals("-nohistory")) {
                    config.setStoreHistory(false);
                    loadDefaultFile = false;
                } else if (args[i].equals("-nowriteprotection")) {
                    config.setCanWrite(false);
                    loadDefaultFile = false;
                } else if (args[i].equals("-include")||args[i].equals("-exclude")) {
                    i++;
                    JFSFilter filter = new JFSFilter(args[i]);
                    if (args[i-1].equals("-include")) {
                        config.addInclude(filter);
                    } else {
                        config.addExclude(filter);
                    }
                    if (((i+1)<args.length)&&!args[i+1].startsWith("-")) {
                        i++;
                        filter.setType(args[i]);
                        if (((i+1)<args.length)&&!args[i+1].startsWith("-")) {
                            i++;
                            filter.setRange(args[i]);
                        }
                    }
                    loadDefaultFile = false;
                } else if (args[i].equals("-username")) {
                    i++;
                    config.setServerUserName(args[i]);
                    loadDefaultFile = false;
                } else if (args[i].equals("-passphrase")) {
                    i++;
                    config.setServerPassPhrase(args[i]);
                    loadDefaultFile = false;
                } else if (args[i].equals("-timeout")) {
                    i++;
                    config.setServerTimeout(Integer.parseInt(args[i]));
                    loadDefaultFile = false;
                } else if (args[i].equals("-nogui")) {
                    nogui = true;
                } else if (args[i].equals("-quiet")) {
                    quiet = true;
                } else if (args[i].equals("-laf")) {
                    if (((i+1)>=args.length)||args[i+1].startsWith("-")) {
                        p.println(t.get("cmd.laf"));

                        UIManager.LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();

                        for (int j = 0; j<laf.length; j++) {
                            p.println(j+1+". "+laf[j].getName()+" <"+laf[j].getClassName()+">");
                        }

                        System.exit(0);
                    } else {
                        i++;
                        s.setLaf(args[i]);
                    }
                } else if (args[i].equals("-help")||args[i].equals("-?")) {
                    printCmdLineHelpAndExit();
                } else if (args[i].equals("-version")) {
                    p.println(t.get("general.appName")+" "+JFSConst.getInstance().getString("jfs.version"));
                    p.println(t.get("info.copyright"));
                    p.println(t.get("info.author"));
                    p.println(t.get("info.copyright.addon"));
                    p.println(t.get("info.author.addon"));
                    p.println(t.get("info.home")+" "+getHome());
                    System.exit(0);
                } else if (args.length==1) {
                    config.load(new File(args[i]));
                    loadDefaultFile = false;
                } else {
                    printCmdLineHelpAndExit();
                }

                i++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            printCmdLineHelpAndExit();
        } catch (NumberFormatException e) {
            JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
            System.exit(0);
        }

        // Initialize new synchronization table:
        JFSTable table = JFSTable.getInstance();
        config.attach(table);

        if (!nogui) {
            // Start GUI:
            p.println(t.get("cmd.startGui"));

            // Determine whether the last configuration when (stored when
            // the program was exited should be loaded at GUI startup:
            s.setNoGui(false);
            ImageIcon jfsIcon = new ImageIcon(constants.getIconUrl("jfs.icon.logo"));
            new JFSMainView(loadDefaultFile, jfsIcon.getImage());
        } else {
            s.setNoGui(true);
            JFSShell.startShell(quiet);
        }
    }


    /**
     * Performs a busy wait.
     *
     * @param duration
     * The number of milli-seconds to wait.
     */
    public static void busyWait(long duration) {
        long time = System.currentTimeMillis();
        long stop = time+duration;
        while (time<=stop) {
            time = System.currentTimeMillis();
        }
    }


    /**
     * Prints the command line help file and exits.
     */
    private static void printCmdLineHelpAndExit() {
        JFSShell.printURL(JFSConst.getInstance().getResourceUrl("jfs.help.topic.cmdLine"));
        System.exit(0);
    }

}
