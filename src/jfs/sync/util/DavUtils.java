/*
 * Copyright (C) 2015, Martin Goellnitz
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

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Some common utility functions for the access of WebDAV resources through Sardine.
 */
public final class DavUtils {

    public static final String PROP_LAST_MODIFIED_TIME_WIN = "Win32LastModifiedTime";

    public static final QName QNAME_LAST_MODIFIED_TIME_WIN = new QName("urn:schemas-microsoft-com:", PROP_LAST_MODIFIED_TIME_WIN, "ns1");

    public static final String PROP_LAST_MODIFIED_TIME = "getlastmodified";

    public static final String PROP_CUSTOM_MODIFIED = "JFileSync";

    public static final QName QNAME_LAST_MODIFIED_TIME = new QName("DAV:", PROP_LAST_MODIFIED_TIME, "d");

    public static final QName QNAME_CUSTOM_MODIFIED = new QName("http://www.provocon.de/sync", PROP_CUSTOM_MODIFIED, "sync");

    private static final DateFormat DATE_FORMAT;

    private static final Logger LOG = LoggerFactory.getLogger(DavUtils.class);

    private static final Set<QName> CUSTOM_PROPS = new HashSet<>();


    static {
        CUSTOM_PROPS.add(DavUtils.QNAME_CUSTOM_MODIFIED);
        CUSTOM_PROPS.add(DavUtils.QNAME_LAST_MODIFIED_TIME_WIN);
        DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ROOT);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    public static Set<QName> getCustomDavProperties() {
        return CUSTOM_PROPS;
    } // getCustomDavProperties()


    public static long getModificationDate(DavResource resource) {
        Date modificationDate = resource.getModified();
        String modifiedDateString = resource.getCustomProps().get(DavUtils.PROP_LAST_MODIFIED_TIME_WIN);
        LOG.info("getModificationDate() custom properties for {}: {}", resource.getName(), resource.getCustomPropsNS());
        if (StringUtils.isNotEmpty(modifiedDateString)) {
            try {
                synchronized (DATE_FORMAT) {
                    modificationDate = DATE_FORMAT.parse(modifiedDateString);
                }
            } catch (ParseException pe) {
                LOG.error("createFileInfo() {}", pe.getMessage());
            } catch (Exception e) {
                LOG.error("createFileInfo()", e);
            } // try/catch
        } // if
        LOG.debug("getModificationDate() {} [{};{}]", modificationDate.getTime(), modificationDate, resource.getModified().getTime());
        return modificationDate.getTime();
    } // getModificationDate()


    /**
     * Set or modify a property.
     *
     * @param sardine Sardine instance to use
     * @param url Specification of the resource to set the value for
     * @param property qualified property name with namespace
     * @param value new property value
     * @return true if setting of property was succesful
     */
    private static boolean setProperty(Sardine sardine, String url, QName property, String value) {
        boolean success = false;
//        List<QName> removeProps = new ArrayList<>(1);
//        removeProps.add(property);
        Set<QName> props = new HashSet<>(1);
        props.add(property);
        Map<QName, String> addProps = new HashMap<>();
        addProps.put(property, value);
        try {
            List<DavResource> result = sardine.patch(url, addProps);
            LOG.info("setProperty() result list size {}", result.size());
            success = (result.size()==1);
            if (success) {
                result = sardine.list(url, 1, props);
                LOG.info("setProperty() result custom props {}", result.get(0).getCustomPropsNS());
            } // if
        } catch (IOException e) {
            LOG.error("setProperty() failed for "+url, e);
        } // try/catch
        return success;
    } // setProperty()


    /**
     * Set modification time for a given resource.
     * This method tries to set the modification date and a second custom flag like it's used
     * by the windows explorer.
     *
     * @param access Sardine instance to use.
     * @param url url of the resource to set the modification time for.
     * @param time modification time in seconds since the epoche
     * @return return if operation was successul
     */
    public static boolean setLastModified(Sardine access, String url, long time) {
        String modificationDate;
        synchronized (DATE_FORMAT) {
            modificationDate = DATE_FORMAT.format(new Date(time));
        }
        LOG.debug("setLastModified() setting time for {} to {}", url, modificationDate);
        boolean success = DavUtils.setProperty(access, url, DavUtils.QNAME_LAST_MODIFIED_TIME_WIN, modificationDate);
        // success = success&DavUtils.setProperty(access, url, DavUtils.QNAME_LAST_MODIFIED_TIME, modificationDate);
        // success = success&DavUtils.setProperty(access, url, DavUtils.QNAME_CUSTOM_MODIFIED, modificationDate);

        return success;
    } // setLastModified()

} // DavUtils
