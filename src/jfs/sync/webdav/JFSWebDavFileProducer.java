/*
 * Copyright (C) 2010-2015, Martin Goellnitz
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

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import jfs.conf.JFSConfig;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.util.WindowsProxySelector;
import static jfs.sync.webdav.JFSWebDavFile.CUSTOM_PROPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class produces webdav JFS files to be handled by the algorithm.
 *
 * @author Martin Goellnitz
 *
 */
public class JFSWebDavFileProducer extends JFSFileProducer {

    public static final String PROP_LAST_MODIFIED_TIME_WIN = "Win32LastModifiedTime";

    public static final QName QNAME_LAST_MODIFIED_TIME_WIN = new QName("urn:schemas-microsoft-com:", PROP_LAST_MODIFIED_TIME_WIN, "ns1");

    public static final String PROP_LAST_MODIFIED_TIME = "getlastmodified";

    public static final String PROP_CUSTOM_MODIFIED = "JFileSync";

    public static final QName QNAME_LAST_MODIFIED_TIME = new QName("DAV:", PROP_LAST_MODIFIED_TIME, "d");

    public static final QName QNAME_CUSTOM_MODIFIED = new QName("http://www.provocon.de/sync", PROP_CUSTOM_MODIFIED, "sync");


    static {
        CUSTOM_PROPS.add(QNAME_CUSTOM_MODIFIED);
        CUSTOM_PROPS.add(QNAME_LAST_MODIFIED_TIME_WIN);
    }

    private static final Logger LOG = LoggerFactory.getLogger(JFSWebDavFileProducer.class);

    private Sardine sardine;

    private Map<String, List<DavResource>> directoryCache = new HashMap<>(256);


    List<DavResource> getListing(String url) throws IOException {
        if (directoryCache.containsKey(url)) {
            return directoryCache.get(url);
        } // if
        LOG.debug("getListing() listing {}", url);
        List<DavResource> listing = sardine.list(url, 1, CUSTOM_PROPS);
        LOG.info("getListing({}) listing {}", listing.size(), url);
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
            LOG.debug("getSardine() webdav client {}", sardine);
        } // if
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            LOG.error("getSardine()", ie);
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

} // JFSWebDavFileProducer
