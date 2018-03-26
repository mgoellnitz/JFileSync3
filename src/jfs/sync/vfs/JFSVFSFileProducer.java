/*
 * JFileSync
 * Copyright (C) 2002-2015, Jens Heidrich
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
package jfs.sync.vfs;

import java.util.ArrayList;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class produces VFS files to be handled by the algorithm.
 *
 * @author Jens Heidrich
 */
public class JFSVFSFileProducer extends JFSFileProducer {

    private static final String SCHEME_SFTP = "sftp";

    private static final Logger LOG = LoggerFactory.getLogger(JFSVFSFileProducer.class);

    private JFSVFSFile rootFile = null;


    /**
     * @see JFSFileProducer#JFSFileProducer(String, String)
     */
    public JFSVFSFileProducer(String uri) {
        super(uri.substring(0, uri.indexOf(':')), uri);
    }


    /**
     * Resets the file system manager.
     */
    public void reset() {
        try {
            ((DefaultFileSystemManager) VFS.getManager()).close();
            ((DefaultFileSystemManager) VFS.getManager()).init();
        } catch (FileSystemException e) {
            LOG.error("reset()", e);
        }
    }


    /**
     * @return Returns the base file.
     */
    public FileObject getBaseFile() {
        return ((JFSVFSFile) getRootJfsFile()).getFileObject();
    }


    /**
     * @see JFSFileProducer#getRootJfsFile()
     */
    public JFSFile getRootJfsFile() {
        if (rootFile==null) {
            rootFile = new JFSVFSFile(this);
        }
        return rootFile;
    }


    /**
     * @see JFSFileProducer#getJfsFile(String)
     */
    public JFSFile getJfsFile(String path, boolean asFolder) {
        return new JFSVFSFile(this, path);
    }


    /**
     * @return Returns the available schemes.
     */
    static public String[] getSchemes() {
        ArrayList<String> schemes = new ArrayList<>();
        String[] schemesArray = new String[0];
        try {
            schemes.add(SCHEME_SFTP);
            for (String s : VFS.getManager().getSchemes()) {
                if (!("file:".equals(s)||s.equals(SCHEME_SFTP))) {
                    schemes.add(s);
                }
            }
        } catch (FileSystemException e) {
            LOG.error("getSchemes()", e);
        }
        return schemes.toArray(schemesArray);
    }
}
