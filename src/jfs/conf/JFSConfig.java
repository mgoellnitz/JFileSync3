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
import java.util.Vector;
import jfs.sync.JFSFile;

/**
 * Manages all configuration options of JFileSync user profile.
 *
 * @author Jens Heidrich
 * @version $Id: JFSConfig.java,v 1.31 2007/06/06 19:51:33 heidrich Exp $
 */
public abstract class JFSConfig implements Cloneable {

    /** Stores the only instance of the class. */
    private static JFSConfig instance = null;

    /** Stores the default configuration file. */
    protected static File defaultFile = new File(JFSConst.HOME_DIR+File.separator+JFSConst.DEFAULT_PROFILE_FILE);

    /** Stores the title of the configuration. */
    protected String title;

    /** Stores the current synchronization mode. */
    protected byte syncMode;

    /** Stores the current view onto the comparison table. */
    protected byte view;

    /** Vector with all directory pairs that have to be compared. */
    protected Vector<JFSDirectoryPair> directoryList = new Vector<JFSDirectoryPair>();

    /** Stores the granularity in ms for file comparisons. */
    protected int granularity;

    /** The used buffer size for file operations. */
    protected int bufferSize;

    /** Determines whether the system should keep user-defined actions. */
    protected boolean keepUserActions;

    /** Determines whether the history of the directory pairs is stored. */
    protected boolean storeHistory;

    /** Determines whether the set can write property of a file is set. */
    protected boolean setCanWrite;

    /** The files to include in comparison. */
    protected Vector<JFSFilter> includes = new Vector<JFSFilter>();

    /** The files to exclude from comparison. */
    protected Vector<JFSFilter> excludes = new Vector<JFSFilter>();

    /** The used server name to log in. */
    protected String serverUserName;

    /** The used server pass phrase for authentication. */
    protected String serverPassPhrase;

    /** The used default server timeout for all used sockets. */
    protected int serverTimeout;

    /** The used pass phrase for encryption. */
    protected String encryptionPassPhrase;

    /** The used cipher type for encryption. */
    protected String encryptionCipher;

    /** shorten paths by use of seven bit file name character tables */
    protected boolean shortenPaths = false;

    /** Determines whether the current profile was stored to a file. */
    protected boolean isCurrentProfileStored;

    /** Vector with all oberservers of the configuration object. */
    protected Vector<JFSConfigObserver> observers = new Vector<JFSConfigObserver>();


    /**
     * Sets some default values for the configuration object.
     */
    protected JFSConfig() {
        clean();
    }


    /**
     * Returns the reference of the only object of the class.
     *
     * @return The only instance.
     */
    public static JFSConfig getInstance() {
        if (instance==null) {
            instance = new JFSConfigXML();
        }
        return instance;
    }


    /**
     * Restores the default values, but keeps all registered observers.
     */
    public final void clean() {
        // Basic settings:
        title = JFSText.getInstance().get("profile.defaultTitle");
        syncMode = (byte)JFSSyncModes.getInstance().getDefaultMode();
        view = (byte)JFSViewModes.getInstance().getDefaultMode();
        directoryList.clear();

        // Advanced settings:
        granularity = JFSConst.GRANULARITY;
        bufferSize = JFSConst.BUFFER_SIZE;
        keepUserActions = JFSConst.KEEP_USER_ACTIONS;
        storeHistory = JFSConst.STORE_HISTORY;
        setCanWrite = JFSConst.SET_CAN_WRITE;

        // Includes and excludes:
        includes.clear();
        excludes.clear();

        // Server settings:
        serverUserName = JFSConst.SERVER_USER_NAME;
        serverPassPhrase = JFSConst.SERVER_PASS_PHRASE;
        serverTimeout = JFSConst.SERVER_TIMEOUT;

        encryptionPassPhrase = "";
        encryptionCipher = "AES";
        shortenPaths = false;

        // When cleaned, the profile is stored by definition:
        isCurrentProfileStored = true;
    }


    /**
     * Loads the default configuration file in the user's home directory after program start if no configuration file
     * was specified (GUI only).
     */
    public final void loadDefaultFile() {
        if (defaultFile.exists()) {
            // Loading the default file should not change whether the profile
            // was changed:
            boolean isStored = isCurrentProfileStored();
            loadProfile(defaultFile);
            setCurrentProfileStored(isStored);
        }
    }


    /**
     * Stores the default configuration file to the user's home directory after program termination (GUI only). If the
     * directory doesn't exist, it is created from scratch.
     */
    public final void storeDefaultFile() {
        File home = new File(JFSConst.HOME_DIR);

        if ( !home.exists()) {
            home.mkdir();
        }

        storeProfile(defaultFile);
    }


    /**
     * Loads a profile.
     *
     * @param profile
     *            The profile to load.
     * @return True if and only if loading did not fail.
     */
    protected abstract boolean loadProfile(File profile);


    /**
     * Stores a profile.
     *
     * @param profile
     *            The profile to store.
     * @return True if and only if storing did not fail.
     */
    protected abstract boolean storeProfile(File profile);


    /**
     * Loads a profile. If loading the profile did not fail, it sets the profile as the current one in the JFS settings
     * object, and adds the profile to the list of last opened profiles. Else, the current profile is set to null and
     * the profile is removed from the list of opened profiles.
     *
     * @param profile
     *            The profile to load.
     * @return True if and only if loading did not fail.
     */
    public final boolean load(File profile) {
        if (profile==null) {
            return false;
        }

        JFSSettings s = JFSSettings.getInstance();
        if (loadProfile(profile)) {
            s.setCurrentProfile(profile);
            s.addLastOpenedProfile(profile);
            setCurrentProfileStored(true);
            return true;
        }
        s.setCurrentProfile(null);
        s.getLastOpenedProfiles().remove(profile);
        return false;
    }


    /**
     * Stores a profile. If storing the profile did not fail, it sets the profile as the current one in the JFS settings
     * object, and adds the profile to the list of last opened profiles.
     *
     * @param profile
     *            The profile to load.
     * @return True if and only if storing did not fail.
     */
    public final boolean store(File profile) {
        if (profile==null) {
            return false;
        }

        JFSSettings s = JFSSettings.getInstance();
        if (storeProfile(profile)) {
            s.setCurrentProfile(profile);
            s.addLastOpenedProfile(profile);
            setCurrentProfileStored(true);
            return true;
        }
        return false;
    }


    /**
     * Returns the name of the configuration.
     *
     * @return Title.
     */
    public final String getTitle() {
        return title;
    }


    /**
     * Sets the title of the configuration.
     *
     * @param title
     *            The title.
     */
    public final void setTitle(String title) {
        if ( !title.equals(this.title)) {
            this.title = title;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the chosen synchronization mode.
     *
     * @return Number of the choosen mode.
     */
    public final byte getSyncMode() {
        return syncMode;
    }


    /**
     * Sets the synchronization mode.
     *
     * @param syncMode
     *            Number of the choosen mode.
     */
    public void setSyncMode(byte syncMode) {
        // Set only, if mode exists:
        if (JFSSyncModes.getInstance().contains(syncMode)&&syncMode!=this.syncMode) {
            this.syncMode = syncMode;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the chosen view on the comparison table.
     *
     * @return Number of the chosen view.
     */
    public final byte getView() {
        return view;
    }


    /**
     * Sets the view.
     *
     * @param view
     *            Number of the chosen view.
     */
    public void setView(byte view) {
        // Set only, if mode exists:
        if (JFSViewModes.getInstance().contains(view)&&view!=this.view) {
            this.view = view;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the vector of all directory pairs that have to be compared.
     *
     * @return Vector of directory pairs.
     */
    public final Vector<JFSDirectoryPair> getDirectoryList() {
        return directoryList;
    }


    /**
     * Adds a directory pair.
     *
     * @param pair
     *            The pair to add.
     */
    public final void addDirectoryPair(JFSDirectoryPair pair) {
        directoryList.add(pair);
        setCurrentProfileStored(false);
    }


    /**
     * Determines whether the configuration contains a special directory pair.
     *
     * @param pair
     *            The pair to check.
     * @return Returns true, if the pair is part of the configuration.
     */
    public final boolean hasDirectoryPair(JFSDirectoryPair pair) {
        return directoryList.contains(pair);
    }


    /**
     * Removes a directory pair.
     *
     * @param index
     *            The index of the element to remove.
     * @return The removed element.
     */
    public final JFSDirectoryPair removeDirectoryPair(int index) {
        setCurrentProfileStored(false);
        return directoryList.remove(index);
    }


    /**
     * Inserts a directory pair.
     *
     * @param pair
     *            The pair to insert.
     * @param index
     *            The index of the element to insert.
     */
    public final void insertDirectoryPair(JFSDirectoryPair pair, int index) {
        directoryList.insertElementAt(pair, index);
        setCurrentProfileStored(false);
    }


    /**
     * Returns the chosen granularity of the comparison in milliseconds that is used in order to comapare the last
     * modified time of two files. Under the DOS and Windows FAT filesystem, the finest granularity on time resolution
     * is two seconds. So we define the default maximum tollerance range for each comparison as 2000ms.
     *
     * @return Granularity in ms.
     */
    public final int getGranularity() {
        return granularity;
    }


    /**
     * Sets the granularity if it is greater than zero.
     *
     * @param granularity
     *            Granularity in ms.
     */
    public void setGranularity(int granularity) {
        if (granularity>0&&granularity!=this.granularity) {
            this.granularity = granularity;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the chosen buffer size.
     *
     * @return Size in byte.
     */
    public final int getBufferSize() {
        return bufferSize;
    }


    /**
     * Sets the buffer size.
     *
     * @param bufferSize
     *            Size in byte.
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSize!=this.bufferSize) {
            this.bufferSize = bufferSize;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns whether the system should keep user-defined actions.
     *
     * @return True, if the system should do so.
     */
    public final boolean isKeepUserActions() {
        return keepUserActions;
    }


    /**
     * Determines whether the system should keep user-defined actions.
     *
     * @param keepUserActions
     *            True, if the system should do so.
     */
    public void setKeepUserActions(boolean keepUserActions) {
        if (keepUserActions!=this.keepUserActions) {
            this.keepUserActions = keepUserActions;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns whether the program should store the history of a synchronized files. This is needed when the program
     * should automatically use the information of its previous run in order to determine which files to copy and
     * delete.
     *
     * @return True if and only if the program should store the history.
     */
    public boolean isStoreHistory() {
        return storeHistory;
    }


    /**
     * Sets whether the program should store the history of a synchronized files. This is needed when the program should
     * automatically use the information of its previous run in order to determine which files to copy and delete.
     *
     * @param storeHistory
     *            True if and only if the program should store the history.
     */
    public void setStoreHistory(boolean storeHistory) {
        if (storeHistory!=this.storeHistory) {
            this.storeHistory = storeHistory;
            setCurrentProfileStored(false);
        }
    }


    /**
     * @return Determines whether the set can write property of a file is set.
     */
    public boolean isSetCanWrite() {
        return setCanWrite;
    }


    /**
     * Determines whether the set can write property of a file is set.
     *
     * @param setCanWrite
     *            True if and only if the set can write property of a file is set.
     */
    public void setCanWrite(boolean setCanWrite) {
        if (setCanWrite!=this.setCanWrite) {
            this.setCanWrite = setCanWrite;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the vector of filters used to determine whether a file should be included in the comparison.
     *
     * @return Vector of JFSFilter objects.
     */
    public final Vector<JFSFilter> getIncludes() {
        return includes;
    }


    /**
     * Adds an include filter.
     *
     * @param filter
     *            The include filter to add.
     */
    public final void addInclude(JFSFilter filter) {
        includes.add(filter);
        setCurrentProfileStored(false);
    }


    /**
     * Replaces all include filters.
     *
     * @param filters
     *            The include filters to use.
     */
    public final void replaceIncludes(Vector<JFSFilter> filters) {
        includes.clear();
        includes.addAll(filters);
        setCurrentProfileStored(false);
    }


    /**
     * Determines whether a given file matches an include expression.
     *
     * @param file
     *            The file to test.
     * @return True, if and only if the file matches at least one include expression.
     */
    public final boolean matchesIncludes(JFSFile file) {
        for (JFSFilter f : includes) {
            if (f.matches(file)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Returns the vector of filters used to determine whether a file should be excluded in the comparison.
     *
     * @return Vector of JFSFilter objects.
     */
    public final Vector<JFSFilter> getExcludes() {
        return excludes;
    }


    /**
     * Adds an exclude filter.
     *
     * @param filter
     *            The exclude filter to add.
     */
    public final void addExclude(JFSFilter filter) {
        excludes.add(filter);
        setCurrentProfileStored(false);
    }


    /**
     * Replaces all exclude filters.
     *
     * @param filters
     *            The exclude filters to use.
     */
    public final void replaceExcludes(Vector<JFSFilter> filters) {
        excludes.clear();
        excludes.addAll(filters);
        setCurrentProfileStored(false);
    }


    /**
     * Determines whether a given file matches an exclude expression.
     *
     * @param file
     *            The file to test.
     * @return True, if and only if the file matches at least one exclude expression.
     */
    public final boolean matchesExcludes(JFSFile file) {
        for (JFSFilter f : excludes) {
            if (f.matches(file)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Returns the server base directory for remote connections.
     *
     * @return The server base directory.
     */
    public String getServerUserName() {
        return serverUserName;
    }


    /**
     * Sets the server base directory for remote connections.
     *
     * @param serverBase
     *            The server base directory.
     */
    public void setServerUserName(String serverUserName) {
        if ( !serverUserName.equals(this.serverUserName)) {
            this.serverUserName = serverUserName;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the server pass phrase for remote connections.
     *
     * @return The server pass phrase.
     */
    public String getServerPassPhrase() {
        return serverPassPhrase;
    }


    /**
     * Sets the server pass phrase for remote connections.
     *
     * @param serverPassPhrase
     *            The server pass phrase.
     */
    public void setServerPassPhrase(String serverPassPhrase) {
        if ( !serverPassPhrase.equals(this.serverPassPhrase)) {
            this.serverPassPhrase = serverPassPhrase;
            setCurrentProfileStored(false);
        }
    }


    /**
     * Returns the server timeout.
     *
     * @return The server timeout.
     */
    public int getServerTimeout() {
        return serverTimeout;
    }


    /**
     * Sets the server timeout.
     *
     * @param serverTimeout
     *            The server timeout.
     */
    public void setServerTimeout(int serverTimeout) {
        if (serverTimeout!=this.serverTimeout) {
            this.serverTimeout = serverTimeout;
            setCurrentProfileStored(false);
        }
    }


    public String getEncryptionPassPhrase() {
        return encryptionPassPhrase;
    }


    public void setEncryptionPassPhrase(String encryptionPassPhrase) {
        if ( !encryptionPassPhrase.equals(this.encryptionPassPhrase)) {
            this.encryptionPassPhrase = encryptionPassPhrase;
            setCurrentProfileStored(false);
        }
    }


    public String getEncryptionCipher() {
        return encryptionCipher;
    }


    public void setEncryptionCipher(String encryptionCipher) {
        if ( !encryptionCipher.equals(this.encryptionCipher)) {
            this.encryptionCipher = encryptionCipher;
            setCurrentProfileStored(false);
        }
    }


    public boolean isShortenPaths() {
        return shortenPaths;
    }


    public void setShortenPaths(boolean shortenPaths) {
        if ( shortenPaths!=this.shortenPaths) {
            this.shortenPaths = shortenPaths;
            setCurrentProfileStored(false);
        }
        this.shortenPaths = shortenPaths;
    }


    /**
     * Determines whether the current profile was stored to a file. If the profile was changed and is not stored yet,
     * this method will return false.
     */
    public final boolean isCurrentProfileStored() {
        return isCurrentProfileStored;
    }


    /**
     * Determines whether the current profile was stored to a file.
     *
     * @param isCurrentProfileStored
     *            True, if the profile was stored.
     */
    public final void setCurrentProfileStored(boolean isCurrentProfileStored) {
        this.isCurrentProfileStored = isCurrentProfileStored;
    }


    /**
     * Send to all observers when the configuration has to be updated.
     */
    public final void fireConfigUpdate() {
        for (JFSConfigObserver co : observers) {
            co.updateConfig(this);
        }
    }


    /**
     * Send to all observers when the computed comparison has to be updated.
     */
    public final void fireComparisonUpdate() {
        for (JFSConfigObserver co : observers) {
            co.updateComparison(this);
        }
    }


    /**
     * Send to all observers when the server has to be updated.
     */
    public final void fireServerUpdate() {
        for (JFSConfigObserver co : observers) {
            co.updateServer(this);
        }
    }


    /**
     * Attaches an additional observer.
     *
     * @param observer
     *            The new observer.
     */
    public final void attach(JFSConfigObserver observer) {
        observers.add(observer);
        updateObserver(observer);
    }


    /**
     * Detaches an existing observer.
     *
     * @param observer
     *            An old observer.
     */
    public final void detach(JFSConfigObserver observer) {
        observers.remove(observer);
    }


    /**
     * Updates the current state of the configuration for a special observer.
     *
     * @param observer
     *            The observer to update.
     */
    private final void updateObserver(JFSConfigObserver observer) {
        observer.updateConfig(this);
        observer.updateComparison(this);
        observer.updateServer(this);
    }


    /**
     * Updates the current state of the configuration for all existing observers.
     */
    public final void fireUpdate() {
        for (JFSConfigObserver co : observers) {
            updateObserver(co);
        }
    }


    /**
     * Transfers the content of the configuration object to an other configuration object (without the registered
     * observers).
     *
     * @param config
     *            The transfer target.
     */
    public final void transferContentTo(JFSConfig config) {
        boolean configUpdate = false;
        boolean serverUpdate = false;
        boolean comparisonUpdate = false;

        // Transfer basic settings:
        if ( !title.equals(config.title)) {
            config.title = title;
            configUpdate = true;
        }

        if (syncMode!=config.syncMode) {
            config.syncMode = syncMode;
            configUpdate = true;
        }

        if (view!=config.view) {
            config.view = view;
            configUpdate = true;
        }

        if ( !directoryList.equals(config.directoryList)) {
            config.directoryList.clear();
            for (JFSDirectoryPair pair : directoryList)
                config.directoryList.add(pair.clone());
            comparisonUpdate = true;
        }

        // Transfer advanced settings:
        if (granularity!=config.granularity) {
            config.granularity = granularity;
            configUpdate = true;
        }

        if (bufferSize!=config.bufferSize) {
            config.bufferSize = bufferSize;
            serverUpdate = true;
        }

        if (keepUserActions!=config.keepUserActions) {
            config.keepUserActions = keepUserActions;
            configUpdate = true;
        }

        if (storeHistory!=config.storeHistory) {
            config.storeHistory = storeHistory;
            configUpdate = true;
        }

        if (setCanWrite!=config.setCanWrite) {
            config.setCanWrite = setCanWrite;
            configUpdate = true;
        }

        // Transfer includes and excludes:
        if ( !includes.equals(config.includes)) {
            config.includes.clear();
            for (JFSFilter f : includes)
                config.includes.add(f.clone());
            comparisonUpdate = true;
        }

        if ( !excludes.equals(config.excludes)) {
            config.excludes.clear();
            for (JFSFilter f : excludes)
                config.excludes.add(f.clone());
            comparisonUpdate = true;
        }

         if ( !serverUserName.equals(config.serverUserName)) {
            config.serverUserName = serverUserName;
            serverUpdate = true;
        }

        if ( !serverPassPhrase.equals(config.serverPassPhrase)) {
            config.serverPassPhrase = serverPassPhrase;
            serverUpdate = true;
        }

        if (serverTimeout!=config.serverTimeout) {
            config.serverTimeout = serverTimeout;
            serverUpdate = true;
        }

        if ( !encryptionPassPhrase.equals(config.encryptionPassPhrase)) {
            config.encryptionPassPhrase = encryptionPassPhrase;
            configUpdate = true;
        } // if

        if ( !encryptionCipher.equals(config.encryptionCipher)) {
            config.encryptionCipher = encryptionCipher;
            configUpdate = true;
        } // if

        // Transfer whether profile was stored:
        if (isCurrentProfileStored!=config.isCurrentProfileStored) {
            config.isCurrentProfileStored = isCurrentProfileStored;
        }

        // Fire updates accordingly:
        if (configUpdate) {
            config.fireConfigUpdate();
        }

        if (comparisonUpdate) {
            config.fireComparisonUpdate();
        }

        if (serverUpdate) {
            config.fireServerUpdate();
        }
    }


    /**
     * @see Object#clone()
     */
    @Override
    public final Object clone() {
        JFSConfig clone = new JFSConfigXML();
        this.transferContentTo(clone);

        return clone;
    }
}