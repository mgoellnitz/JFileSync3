/*
 * Copyright (C) 2010-2012 Martin Goellnitz
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
package jfs.sync.encfs;

import java.io.File;
import jfs.conf.JFSConfig;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import org.mrpdaemon.sec.encfs.EncFSVolume;
import org.mrpdaemon.sec.encfs.EncFSVolumeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class produces webdav JFS files to be handled by the algorithm.
 *
 * @author Martin Goellnitz
 *
 */
public class JFSEncfsProducer extends JFSFileProducer {

    private static final Logger LOG = LoggerFactory.getLogger(JFSEncfsProducer.class);

    private EncFSVolume volume;


    /**
     * @see JFSFileProducer#JFSFileProducer(String, String)
     */
    public JFSEncfsProducer(String uri) {
        super(JFSEncfsProducerFactory.SCHEME_NAME, uri);

        if (volume==null) {
            String passphrase = JFSConfig.getInstance().getEncryptionPassPhrase();
            try {
                LOG.info("({}) opening volume", uri);
                volume = new EncFSVolumeBuilder().withRootPath(uri).withPassword(passphrase).buildVolume();
            } catch (Exception e) {
                LOG.error("()", e);
            } // try/catch
        } // if
    } // JFSEncfsProducer()


    /**
     * @see JFSFileProducer#getRootJfsFile()
     */
    @Override
    public JFSFile getRootJfsFile() {
        return new JFSEncfsFile(volume, this);
    }


    /**
     * @see JFSFileProducer#getJfsFile(String)
     */
    @Override
    public JFSFile getJfsFile(String path, boolean asFolder) {
        return new JFSEncfsFile(volume, this, path, asFolder);
    }


    @Override
    public boolean hasExecutableFlag() {
        return File.separatorChar == '/';
    }

}
