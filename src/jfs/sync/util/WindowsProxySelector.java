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
package jfs.sync.util;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WindowsProxySelector extends ProxySelector {

    private static Log log = LogFactory.getLog(WindowsProxySelector.class);

    private ProxySelector root;

    private static ProxySelector instance = null;


    private WindowsProxySelector() {
        root = ProxySelector.getDefault();
        ProxySelector.setDefault(null);
        if (log.isDebugEnabled()) {
            log.debug("select() root "+root);
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
        if (log.isDebugEnabled()) {
            log.debug("select() uri "+uri);
            log.debug("select() url "+url);
            log.debug("select() proxies "+result);
        } // if
        return result;
    }// select()


    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        root.connectFailed(uri, sa, ioe);
    } // connectFailed()

} // WindowsProxySelector()
