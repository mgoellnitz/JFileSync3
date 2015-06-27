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
package jfs.sync.dav;

import java.util.Map;
import jfs.conf.JFSConfig;
import jfs.sync.JFSFileProducer;
import jfs.sync.encryption.AbstractEncryptedFileProducerFactory;
import jfs.sync.encryption.JFSEncryptedFileProducer;


/**
 * File producer factory creating instances of WebDAV accessing file producsers.
 */
public class JFSDavFileProducerFactory extends AbstractEncryptedFileProducerFactory {

    public static final String SCHEME_NAME = "dav";


    @Override
    public String getName() {
        return SCHEME_NAME;
    } // getName()


    @Override
    public JFSFileProducer createProducer(String uri) {
        DavStorageAccess storageAccess = new DavStorageAccess(JFSConfig.getInstance().getEncryptionCipher());
        Map<String, Long> levels = getCompressionsLevels();
        String shortenedUri = uri.substring(SCHEME_NAME.length()+3);
        return new JFSEncryptedFileProducer(storageAccess, levels, SCHEME_NAME, shortenedUri);
    } // createProducer()

} // JFSDavdFileProducserFactory
