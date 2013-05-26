/*
 * Copyright (C) 2010-2013, Martin Goellnitz
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
package jfs.sync.encryption;

import java.util.Map;

import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

public class JFSEncryptedFileProducer extends AbstractFileProducer {

    /*
     * Collection with extensions of file types which are usually compressed. large files with these extensions should
     * not be tried to compress again (even if it may help in many cases - it's far too slow)
     */
    private Map<String, Long> compressionLevels;


    public JFSEncryptedFileProducer(StorageAccess storageAccess, Map<String, Long> levels, String scheme, String uri) {
        super(storageAccess, scheme, uri);
        this.compressionLevels = levels;
    } // JFSEncryptedFileProducer()


    /**
     * @see JFSFileProducer#getRootJfsFile()
     */
    @Override
    public final JFSFile getRootJfsFile() {
        // System.out.println("JFSEncryptedFileProducer.getRootJfsFile()");
        return new JFSEncryptedFile(this, "");
    }


    /**
     * @see JFSFileProducer#getJfsFile(String)
     */
    @Override
    public final JFSFile getJfsFile(String path, boolean asFolder) {
        // System.out.println("JFSEncryptedFileProducer.getJfsFile()");
        return new JFSEncryptedFile(this, path);
    }


    /**
     * returns a map object mapping file name extensions without leading '.' to the amount in megabytes to which files
     * of the type should be compressed.
     * 
     * @return
     */
    public Map<String, Long> getCompressionLevels() {
        return compressionLevels;
    } // getCompressionLevels()

} // JFSEncryptedFileProducer
