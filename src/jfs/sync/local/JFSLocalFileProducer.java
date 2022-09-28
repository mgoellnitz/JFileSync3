/*
 * JFileSync
 * Copyright (C) 2002-2022 Jens Heidrich, Martin Goellnitz
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

package jfs.sync.local;

import java.io.File;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

/**
 * This class produces local JFS files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducer.java,v 1.1 2005/04/25 09:26:18 heidrich Exp $
 */
public class JFSLocalFileProducer extends JFSFileProducer {
    /**
     * @see JFSFileProducer#JFSFileProducer(String, String)
     */
    public JFSLocalFileProducer(String uri) {
        super(JFSLocalFileProducerFactory.SCHEME_NAME, uri);
    }


    /**
     * @see JFSFileProducer#getRootJfsFile()
     */
    @Override
    public final JFSFile getRootJfsFile() {
        return new JFSLocalFile(this, "");
    }


    /**
     * @see JFSFileProducer#getJfsFile(String)
     */
    @Override
    public final JFSFile getJfsFile(String path, boolean asFolder) {
        return new JFSLocalFile(this, path);
    }


    @Override
    public boolean hasExecutableFlag() {
        return File.separatorChar == '/';
    }

}