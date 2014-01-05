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
package jfs.sync.encrypted;

import jfs.conf.JFSConfig;
import jfs.sync.JFSFileProducer;
import jfs.sync.encryption.AbstractEncryptedFileProducerFactory;
import jfs.sync.encryption.JFSEncryptedFileProducer;


public class JFSEncryptedProducerFactory extends AbstractEncryptedFileProducerFactory {

    public static final String SCHEME_NAME = "encrypted";


    @Override
    public String getName() {
        return SCHEME_NAME;
    } // getName()


    @Override
    public JFSFileProducer createProducer(String uri) {
        final JFSConfig config = JFSConfig.getInstance();
        EncryptedFileStorageAccess storageAccess = new EncryptedFileStorageAccess(config.getEncryptionCipher(), config.isShortenPaths());
        return new JFSEncryptedFileProducer(storageAccess, getCompressionsLevels(), SCHEME_NAME, uri.substring(SCHEME_NAME.length()+3));
    } // createProducer()

} // JFSEncryptedProducerFactory
