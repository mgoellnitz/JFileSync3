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
package jfs.sync.base;

import jfs.sync.JFSFileProducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic abstract file producer factory base class.
 */
public abstract class AbstractJFSFileProducerFactory implements JFSFileProducerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJFSFileProducerFactory.class);


    public static final String[] getPathAndName(String relativePath, String separator) {
        String[] result = new String[2];
        String name = relativePath;
        int idx = name.lastIndexOf(separator);
        String parentPath = "";
        if (idx>=0) {
            parentPath = name.substring(0, idx);
            idx++;
            name = name.substring(idx);
        } // if
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPathAndName("+relativePath+") "+parentPath+";"+name);
        } // if
        result[0] = parentPath;
        result[1] = name;
        return result;
    } // getPathAndName()


    @Override
    public void cancelProducer(String uri) {
        // empty default
    }


    @Override
    public void resetProducers() {
        // empty default
    }


    @Override
    public void shutDownProducer(String uri) {
        // empty default
    }

} // AbstractJFSFileProducerFactory
