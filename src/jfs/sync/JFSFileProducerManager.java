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
package jfs.sync;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import jfs.sync.local.JFSLocalFileProducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class manages all JFS file producer factories that exist for the program. It is able to detect the right file
 * producer for a certain scheme (like "ext" or "file") and advises the corresponding producer factory to create a new
 * file produces or to destroy an already existing file producer.
 *
 * @author Jens Heidrich
 * @version $Id: JFSFileProducerManager.java,v 1.1 2005/05/06 11:06:57 heidrich Exp $
 */
public final class JFSFileProducerManager {

    private static final Logger LOG = LoggerFactory.getLogger(JFSFileProducerManager.class);

    /**
     * Stores the only instance of the class.
     */
    private static JFSFileProducerManager instance = null;

    /**
     * All registered factories for a certain URI scheme.
     */
    private final Map<String, JFSFileProducerFactory> factories;

    /**
     * The default factory.
     */
    private final JFSFileProducerFactory defaultFactory;


    /**
     * Registers all factories and sets the default factory.
     */
    @SuppressWarnings("unchecked")
    private JFSFileProducerManager() {
        factories = new HashMap<>();
        Properties p = new Properties();
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            p.load(classLoader.getResourceAsStream("producers.properties"));
        } catch (Exception e) {
            LOG.error("JFSFileProducerManager()", e);
        } // try/catch
        for (Object property : p.keySet()) {
            String className = ""+property;
            if (className.startsWith("jfs.sync.")) {
                if ("on".equals(p.getProperty(className))) {
                    try {
                        Class<JFSFileProducerFactory> c = (Class<JFSFileProducerFactory>) classLoader
                                .loadClass(className);
                        Constructor<JFSFileProducerFactory> constructor = c.getConstructor(new Class<?>[0]);
                        JFSFileProducerFactory factory = constructor.newInstance(new Object[0]);
                        String name = factory.getName();
                        factories.put(name, factory);
                    } catch (Exception e) {
                        LOG.error("JFSFileProducerManager()", e);
                    } // try/catch
                } // if
            } // if
        } // for
        defaultFactory = factories.get(JFSLocalFileProducerFactory.SCHEME_NAME);
    }


    /**
     * Returns the reference of the only object of the class.
     *
     * @return The only instance.
     */
    public static JFSFileProducerManager getInstance() {
        if (instance==null) {
            instance = new JFSFileProducerManager();
        }

        return instance;
    }


    /**
     * Resets all producers.
     */
    public void resetProducers() {
        for (JFSFileProducerFactory f : factories.values()) {
            f.resetProducers();
        }
    }


    /**
     * Returns a new procucer for a special URI.
     *
     * @param uri
     * The URI to create the producer for.
     * @return The created producer.
     */
    public JFSFileProducer createProducer(String uri) {
        return getFactory(uri).createProducer(uri);
    }


    /**
     * Shuts down an existing producer for a special URI.
     *
     * @param uri
     * The URI to distroy the producer for.
     */
    public void shutDownProducer(String uri) {
        getFactory(uri).shutDownProducer(uri);
    }


    /**
     * Cancels an existing producer for a special URI.
     *
     * @param uri
     * The URI to distroy the producer for.
     */
    public void cancelProducer(String uri) {
        getFactory(uri).cancelProducer(uri);
    }


    /**
     * Returns the factory for a special URI.
     *
     * @param uri
     * The URI to create the factory for.
     * @return The created factory.
     */
    private JFSFileProducerFactory getFactory(String uri) {
        for (String scheme : factories.keySet()) {
            if (uri.startsWith(scheme+":")) {
                return factories.get(scheme);
            }
        } // if

        return defaultFactory;
    }


    public Set<String> getSchemes() {
        return factories.keySet();
    } // getSchemes()

}
