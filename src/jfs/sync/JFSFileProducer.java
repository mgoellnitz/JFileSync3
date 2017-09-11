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


/**
 * This class produces JFS files to be handled by the algorithm. Each comparison object has exactly two file producers,
 * which produce all needed JFS files for the source and the target side specified in the comparison object.
 *
 * @author Jens Heidrich
 * @version $Id: JFSFileProducer.java,v 1.8 2007/06/06 19:51:33 heidrich Exp $
 */
public abstract class JFSFileProducer {

    /** The URI scheme. */
    private final String scheme;

    /** The URI assigned. */
    private final String uri;

    /** The original unmodifief directory path extracted from the URI. */
    private final String originalRootPath;

    /** The directory path extracted from the URI. */
    private final String rootPath;


    /**
     * Creates a new file producer for a specific URI.
     *
     * @param uri
     * The URI.
     */
    protected JFSFileProducer(String scheme, String uri) {
        this.scheme = scheme;
        this.uri = uri;
        originalRootPath = getPath();
        rootPath = originalRootPath;
    }


    /**
     * Returns the JFS file representation for the root path of the URI assigned to the factory.
     *
     * @return A JFS file object.
     */
    public abstract JFSFile getRootJfsFile();


    /**
     * Creates a JFS file object out of an corresponding string that represents a path to a file or directory. The path
     * has to be relative to the root path of the URI assigned to the factory.
     *
     * @param path The path to a file or directory.
     * @return A JFS file object.
     */
    public abstract JFSFile getJfsFile(String path, boolean asFolder);


    /**
     * Returns the assigned URI scheme.
     *
     * @return The URI scheme.
     */
    public final String getScheme() {
        return scheme;
    }


    /**
     * Returns the assigned URI.
     *
     * @return The URI.
     */
    public final String getUri() {
        return uri;
    }


    /**
     * Returns the root path used to create the root JFS file.
     *
     * @return The path.
     */
    public final String getOriginalRootPath() {
        return originalRootPath;
    }


    /**
     * Returns the root path used to create the root JFS file.
     *
     * @return The path.
     */
    public final String getRootPath() {
        return rootPath;
    }


    /**
     * Extracts the path form the given URI string, like 'ext://host:port/directory'. If an abstract path name is given
     * instead of an URI the abstract path name is returned.
     *
     * @return The directory part of the URI.
     */
    private final String getPath() {
        // Return URI as path if no scheme is specified:
        if (!uri.startsWith(scheme+":")) {
            return uri;
        }

        int schemeIndex = scheme.length();

        // Search for authority (host and port) otherwise:
        int authorityIndex = uri.indexOf("//", schemeIndex+1);
        int pathIndex = uri.indexOf("/", Math.max(schemeIndex+1, authorityIndex+2));
        if (pathIndex!=-1) {
            return uri.substring(pathIndex+1);
        }
        return ".";
    }
}
