/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
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

package jfs.sync.local;

import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerFactory;
import jfs.sync.base.AbstractJFSFileProducerFactory;

/**
 * This class produces factories for local JFS files.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSLocalFileProducerFactory.java,v 1.1 2005/04/25 09:26:18 heidrich Exp $
 */
public class JFSLocalFileProducerFactory extends AbstractJFSFileProducerFactory {

    /** The prefix used to indicate a local file. */
    public static final String SCHEME_NAME = "file";


    @Override
    public String getName() {
        return SCHEME_NAME;
    } // getName()


    /**
     * @see JFSFileProducerFactory#createProducer(String)
     */
    @Override
    public final JFSFileProducer createProducer(String uri) {
        return new JFSLocalFileProducer(uri);
    }

}