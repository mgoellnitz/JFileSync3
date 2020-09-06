/*
 * JFileSync
 * Copyright (C) 2002-2015, Jens Heidrich
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
package jfs.sync.vfs;

import java.util.HashMap;
import java.util.Map;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerFactory;


/**
 * This class produces factories for FTP files.
 *
 * @author Jens Heidrich
 */
public class JFSVFSFileProducerFactory implements JFSFileProducerFactory {

    /**
     * The map of file producers.
     */
    private Map<String, JFSVFSFileProducer> producers = new HashMap<>();


    @Override
    public String getName() {
        return "VFS";
    }


    /**
     * @see JFSFileProducerFactory#resetProducers()
     */
    public final void resetProducers() {
        // Reset previous producers before clearing the list:
        for (JFSVFSFileProducer p : producers.values()) {
            p.reset();
        }
        producers.clear();
    }


    /**
     * @see JFSFileProducerFactory#createProducer(String)
     */
    public final JFSFileProducer createProducer(String uri) {
        JFSVFSFileProducer p = new JFSVFSFileProducer(uri);
        producers.put(uri, p);
        return p;
    }

}
