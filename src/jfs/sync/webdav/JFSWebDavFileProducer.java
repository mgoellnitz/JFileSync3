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
package jfs.sync.webdav;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jfs.conf.JFSConfig;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.util.WindowsProxySelector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * This class produces webdav JFS files to be handled by the algorithm.
 * 
 * @author Martin Goellnitz
 * 
 */
public class JFSWebDavFileProducer extends JFSFileProducer {

    private static Log log = LogFactory.getLog(JFSWebDavFileProducer.class);

    private Sardine sardine;

    private Map<String, List<DavResource>> directoryCache = new HashMap<String, List<DavResource>>();


    List<DavResource> getListing(String url) throws IOException {
        if (directoryCache.containsKey(url)) {
            return directoryCache.get(url);
        } // if
        if (log.isDebugEnabled()) {
            log.debug("getListing() listing "+url);
        } // if
        List<DavResource> listing = sardine.list(url);
        if (log.isInfoEnabled()) {
            log.info("getListing("+listing.size()+") listing "+url);
        } // if
        directoryCache.put(url, listing);
        return listing;
    } // getListing()


    /**
     * @see JFSFileProducer#JFSFileProducer(String, String)
     */
    public JFSWebDavFileProducer(String uri) {
        super(JFSWebDavFileProducerFactory.SCHEME_NAME, uri);

        if (sardine==null) {
            String username = JFSConfig.getInstance().getServerUserName();
            String passphrase = JFSConfig.getInstance().getServerPassPhrase();
            sardine = SardineFactory.begin(username, passphrase, WindowsProxySelector.getInstance());
            if (log.isDebugEnabled()) {
                log.debug("getSardine() webdav client "+sardine);
            } // if
        } // if
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            log.error("getSardine()", ie);
        } // try/catch
    } // JFSWebDavFileProducer()


    /**
     * @see JFSFileProducer#getRootJfsFile()
     */
    @Override
    public JFSFile getRootJfsFile() {
        return new JFSWebDavFile(sardine, this);
    }


    /**
     * @see JFSFileProducer#getJfsFile(String)
     */
    @Override
    public JFSFile getJfsFile(String path, boolean asFolder) {
        return new JFSWebDavFile(sardine, this, path, asFolder);
    }

}