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

import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerFactory;
import jfs.sync.base.AbstractJFSFileProducerFactory;

/**
 * This class produces factories for webdav JFS file producers.
 * 
 * @author Martin Goellnitz
 * 
 */
public class JFSWebDavFileProducerFactory extends AbstractJFSFileProducerFactory {

    /** The prefix used to indicate an external file. */
    public static final String SCHEME_NAME = "webdav";


    public JFSWebDavFileProducerFactory() {
        System.setProperty("java.net.useSystemProxies", "true");
    } // JFSWebDavFileProducerFactory()


    @Override
    public String getName() {
        return SCHEME_NAME;
    } // getName()


    /**
     * @see JFSFileProducerFactory#createProducer(String)
     */
    @Override
    public final JFSFileProducer createProducer(String uri) {
        JFSWebDavFileProducer p = new JFSWebDavFileProducer(uri.substring(SCHEME_NAME.length()+3));
        return p;
    }

}