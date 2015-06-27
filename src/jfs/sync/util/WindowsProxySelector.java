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
package jfs.sync.util;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to automatically detect windows http proxy settings.
 */
public final class WindowsProxySelector extends ProxySelector {

    private static final Logger LOG = LoggerFactory.getLogger(WindowsProxySelector.class);

    private final ProxySelector root;

    private static ProxySelector instance = null;


    private WindowsProxySelector() {
        root = ProxySelector.getDefault();
        ProxySelector.setDefault(null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("select() root "+root);
        } // if
    } // WindowsProxySelector()


    public static ProxySelector getInstance() {
        if (instance==null) {
            instance = new WindowsProxySelector();
        } // if
        return instance;
    } // getInstance()


    @Override
    public List<Proxy> select(URI uri) {
        List<Proxy> result = null;

        URI url = URI.create(uri.toString().replace("https://", "http://"));
        result = root.select(url);
        if (LOG.isDebugEnabled()) {
            LOG.debug("select() uri "+uri);
            LOG.debug("select() url "+url);
            LOG.debug("select() proxies "+result);
        } // if
        return result;
    }// select()


    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        root.connectFailed(uri, sa, ioe);
    } // connectFailed()

} // WindowsProxySelector()
