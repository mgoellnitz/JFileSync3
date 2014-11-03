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
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.JFrame;


/**
 * Class JFSConst stores all constant variables including the possible
 * synchronization modes and the possible views onto the files. Moreover it is
 * possible to retrieve constants from a configuration properties file. The
 * class implements the singleton design pattern.
 *
 * @author Jens Heidrich
 * @version $Id: JFSConst.java,v 1.28 2007/07/20 14:19:20 heidrich Exp $
 */
public final class JFSConst {

    /**
     * Stores the only instance of the class.
     */
    private static JFSConst instance = null;

    /**
     * Stores the class name of the used properties file.
     */
    private static final String CONFIG_CLASS = "jfs.resources.conf.JFSConfig";

    /**
     * Stores the class name of the used bundle.
     */
    public static final String TRANSLATION_CLASS = "jfs.resources.conf.JFSTranslation";

    /**
     * The number of help page references stored forward and backward.
     */
    public static final int HELP_HISTORY_SIZE = 15;

    /**
     * The default location for JFS configuration files.
     */
    public final static String HOME_DIR = System.getProperty("user.home", ".")+File.separator+".jfs3";

    /**
     * The current working directory.
     */
    public final static String WORKING_DIR = System.getProperty("user.dir", ".");

    /**
     * The file name of the default user profile file.
     */
    public final static String DEFAULT_PROFILE_FILE = "Profile.xml";

    /**
     * The file name of the settings file.
     */
    public final static String SETTINGS_FILE = "Settings.xml";

    /**
     * The file name of the default error log file.
     */
    public final static String ERR_LOG_FILE = "Error-Log.txt";

    /**
     * The file name of the default log file.
     */
    public final static String OUT_LOG_FILE = "Log.txt";

    /**
     * The default value for the debug mode.
     */
    public final static boolean DEBUG = false;

    /**
     * The default state of the JFileSync main window (maximized, iconified,
     * etc.).
     */
    public static final int WINDOW_STATE = JFrame.NORMAL;

    /**
     * The default JFileSync main window position along the x axis.
     */
    public static final int WINDOW_X = 0;

    /**
     * The default JFileSync main window position along the y axis.
     */
    public static final int WINDOW_Y = 0;

    /**
     * The default JFileSync main window's width.
     */
    public static final int WINDOW_WIDTH = 800;

    /**
     * The default JFileSync main window's height.
     */
    public static final int WINDOW_HEIGHT = 600;

    /**
     * The default granularity in ms for a comparison of two files.
     */
    public static final int GRANULARITY = 2000;

    /**
     * The used buffer size for file operations.
     */
    public static final int BUFFER_SIZE = 262144;

    /**
     * Determines whether the system should keep user-defined actions.
     */
    public static final boolean KEEP_USER_ACTIONS = true;

    /**
     * Determines whether the system stores the synchronization histories.
     */
    public static final boolean STORE_HISTORY = true;

    /**
     * Determines whether the set can write property of a file is set.
     */
    public static final boolean SET_CAN_WRITE = true;

    /**
     * The used default server user name.
     */
    public static final String SERVER_USER_NAME = "";

    /**
     * The used default server base directory.
     */
    public static final String SERVER_PASS_PHRASE = "";

    /**
     * The used default server timeout for all used sockets.
     */
    public static final int SERVER_TIMEOUT = 5000;

    /**
     * The time interval between an update of the progress observers in
     * milliseconds.
     */
    public static final int PROGRESS_UPDATE = 300;

    /**
     * The minimal number of bytes before a graphical progress view shows the
     * transfer rate.
     */
    public static final long VIEW_MIN_BYTES = 500000;

    /**
     * The number of last visited opened profiles to store persistently.
     */
    public static final int LAST_OPENED_PROFILES_SIZE = 5;

    /**
     * The prefix of a history file.
     */
    public static final String HISTORY_FILE_PREFIX = "History-";

    /**
     * The associated bundle to access the properties file.
     */
    private ResourceBundle bundle;


    /**
     * Sets some default values for the object.
     */
    private JFSConst() {
        bundle = ResourceBundle.getBundle(JFSConst.CONFIG_CLASS);
    }


    /**
     * Returns the reference of the only JFSConst object.
     *
     * @return The only JFSConst instance.
     */
    public static JFSConst getInstance() {
        if (instance==null) {
            instance = new JFSConst();
        }

        return instance;
    }


    /**
     * Returns the string value for a certain key.
     *
     * @param key
     * The key.
     * @return The assigned string.
     */
    public String getString(String key) {
        return bundle.getString(key);
    }


    /**
     * Returns the string array for a certain key.
     *
     * @param key
     * The key.
     * @return The assigned string array.
     */
    public String[] getStringArray(String key) {
        String array = bundle.getString(key);

        return array.split(",\\s*");
    }


    /**
     * Tries to create a valid URL corresponding to a given key identifying a
     * system resource file.
     *
     * @param key
     * The key.
     * @return A valid URL.
     */
    public URL getResourceUrl(String key) {
        return getUrl(JFSConst.getInstance().getString("jfs.resource.base")+"/"+bundle.getString(key));
    }


    /**
     * Tries to create a valid URL corresponding to a given key identifying a
     * icon file.
     *
     * @param key
     * The key.
     * @return A valid URL.
     */
    public URL getIconUrl(String key) {
        return getUrl(JFSConst.getInstance().getString("jfs.icon.base")+"/"+bundle.getString(key));
    }


    /**
     * Tries to create a valid URL corresponding to a given file name.
     *
     * @param fileName
     * The file name.
     * @return A valid URL.
     */
    public URL getUrl(String fileName) {
        URL fileUrl = null;

        try {
            fileUrl = ClassLoader.getSystemResource(fileName);
        } catch (Exception e) {
            JFSLog.getErr().getStream().println("Couldn't create URL: "+fileName);
        }

        return fileUrl;
    }
    
}
