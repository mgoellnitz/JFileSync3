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
package jfs.sync.encryption;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jfs.sync.base.AbstractJFSFileProducerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractEncryptedFileProducerFactory extends AbstractJFSFileProducerFactory {

    private static Log log = LogFactory.getLog(AbstractEncryptedFileProducerFactory.class);

    /*
     * Map mapping extensions of file types which are usually compressed to the maximum number of megabytes to which
     * files with this extension should be compressed. Larger files with these extensions should not be tried to
     * compress again (even if it may help in many cases - it's far too slow)
     */
    private Map<String, Long> compressionLevels;


    public AbstractEncryptedFileProducerFactory() {
        compressionLevels = new HashMap<String, Long>();
        Properties p = new Properties();
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            p.load(classLoader.getResourceAsStream("compression-levels.properties"));
        } catch (Exception e) {
            log.error("()", e);
        } // try/catch
        for (Object property : p.keySet()) {
            String extension = ""+property;
            if ( !extension.equals("null")) {
                String limitString = p.getProperty(extension);
                try {
                    long limit = Long.parseLong(limitString);
                    limit = limit*1024l*1024l; // MB
                    compressionLevels.put(extension, limit);
                } catch (Exception e) {
                    log.error("()", e);
                } // try/catch
            } // if
        } // for
    } // AbstractEncryptedFileProducerFactory()


    public Map<String, Long> getCompressionsLevels() {
        return compressionLevels;
    } // getCompressionsLevels()

} // AbstractEncryptedFileProducerFactory
