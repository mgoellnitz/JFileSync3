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
package jfs.sync.dav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import jfs.conf.JFSConfig;
import jfs.sync.encryption.FileInfo;
import jfs.sync.encryption.StorageAccess;
import jfs.sync.meta.AbstractMetaStorageAccess;
import jfs.sync.util.WindowsProxySelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DavStorageAccess extends AbstractMetaStorageAccess implements StorageAccess {

    private static final String PROP_LAST_MODIFIED_TIME = "Win32LastModifiedTime";

    // See commented area below
    // private static final QName QNAME_LAST_MODIFIED_TIME = new QName("urn:schemas-microsoft-com:", PROP_LAST_MODIFIED_TIME, "ns1");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ROOT);


    {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final Log LOG = LogFactory.getLog(DavStorageAccess.class);

    private Sardine sardine = null;

    /*
     * To speed things up we have a second DavResource based directory cache
     */
    private final Map<String, List<DavResource>> directoryCache = new HashMap<String, List<DavResource>>();


    public DavStorageAccess(String cipher) {
        super(cipher, false);
    } // DavStorageAccess()


    private Sardine getSardine() {
        if (sardine==null) {
            String username = JFSConfig.getInstance().getServerUserName();
            String passphrase = JFSConfig.getInstance().getServerPassPhrase();
            sardine = SardineFactory.begin(username, passphrase, WindowsProxySelector.getInstance());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getSardine() webdav client "+sardine);
            } // if
        } // if
        return sardine;
    } // getSardine()


    @Override
    public String getSeparator() {
        return "/";
    } // getSeparator()


    private String getUrl(String rootPath, String relativePath) {
        String urlSegment = getFileName(relativePath);
        try {
            urlSegment = URLEncoder.encode(urlSegment, "UTF-8").replace("%2F", getSeparator());
        } catch (UnsupportedEncodingException e) {
            LOG.error("getUrl() System doesn't know UTF8 ?!?!");
        } // try/catch
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUrl("+relativePath+") "+rootPath+urlSegment);
        } // if
        return rootPath+urlSegment;
    } // getUrl()


    List<DavResource> getListing(String rootPath, String url) throws IOException {
        if (directoryCache.containsKey(url)) {
            return directoryCache.get(url);
        } // if
        boolean available = true;
        if (url.length()>rootPath.length()) {
            String[] pathAndName = getPathAndName(url);
            available = getListing(rootPath, pathAndName[0]).contains(pathAndName[1]);
        } // if
        if (LOG.isInfoEnabled()) {
            LOG.info("getListing() listing: "+url+" - "+available);
        } // if
        List<DavResource> listing = null;
        try {
            if (available) {
                listing = getSardine().list(url+getSeparator());
                if (LOG.isInfoEnabled()) {
                    LOG.info("getListing("+listing.size()+") listing "+url);
                } // if
            } else {
                listing = Collections.emptyList();
            } // if
        } catch (Exception e) {
            listing = Collections.emptyList();
            LOG.error("getListing()", e);
        } // try/catch
        directoryCache.put(url, listing);
        return listing;
    } // getListing()


    /**
     * create file info for optionally non existing files
     *
     * @param file
     * @param pathAndName
     * @return
     */
    private FileInfo createFileInfo(String rootPath, String relativePath, String[] pathAndName) {
        String url = getUrl(rootPath, relativePath);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createFileInfo() url="+url);
        } // if
        FileInfo result = new FileInfo();
        result.setExists(false);

        result.setCanRead(true);
        result.setCanWrite(true);
        result.setPath(pathAndName[0]);
        result.setName(pathAndName[1]);
        Collection<DavResource> resources = Collections.emptyList();
        try {
            String[] urlPathAndName = getPathAndName(url);
            if (LOG.isDebugEnabled()) {
                LOG.debug("createFileInfo() - "+urlPathAndName[0]+" / "+urlPathAndName[1]);
            } // if
            // resources = getSardine().list(urlPathAndName[0]+getSeparator());
            resources = getListing(rootPath, urlPathAndName[0]);
            for (DavResource resource : resources) {
                if (urlPathAndName[1].equals(resource.getName())) {
                    result.setDirectory(resource.isDirectory());
                    result.setExists(true);
                    Date modificationDate = resource.getModified();
                    String modifiedDateString = resource.getCustomProps().get(PROP_LAST_MODIFIED_TIME);
                    if (modifiedDateString!=null) {
                        try {
                            synchronized (DATE_FORMAT) {
                                modificationDate = DATE_FORMAT.parse(modifiedDateString);
                            }
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("createFileInfo() "+modificationDate+" ["+modificationDate.getTime()+";"
                                        +resource.getModified().getTime()+"]");
                            } // if
                        } catch (Exception e) {
                            LOG.error("createFileInfo()", e);
                        } // try/catch
                    } // if
                    result.setModificationDate(modificationDate.getTime());
                    result.setSize(resource.getContentLength());
                } // if
            } // for
        } catch (Exception e) {
            LOG.error("createFileInfo()", e);
        } // try/catch

        if (LOG.isDebugEnabled()) {
            LOG.debug("createFileInfo("+pathAndName[0]+"/"+pathAndName[1]+") "+result);
        } // if

        return result;
    } // createFileInfo()


    // TODO: very similar to local file case
    public FileInfo getFileInfo(String rootPath, String relativePath) {
        String[] pathAndName = getPathAndName(relativePath);
        FileInfo result = getParentListing(rootPath, pathAndName).get(pathAndName[1]);
        if (result==null) {
            result = createFileInfo(rootPath, relativePath, pathAndName);
        } // if
        return result;
    } // getFileInfo()


    @Override
    public boolean createDirectory(String rootPath, String relativePath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectory() "+relativePath);
        } // if
        String url = getUrl(rootPath, relativePath);
        try {
            getSardine().createDirectory(url);
        } catch (Exception e) {
            if (e instanceof SardineException) {
                SardineException se = (SardineException) e;
                if (LOG.isWarnEnabled()) {
                    LOG.warn("createDirectory("+url+") status code: "+se.getStatusCode()+" "+se.getResponsePhrase());
                } // if
            } // if
            if (LOG.isWarnEnabled()) {
                LOG.warn("createDirectory()", e);
            } // if
            return false;
        } // try/catch
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectory("+relativePath+") pre-listing="+listing);
        } // if

        FileInfo info = new FileInfo();
        info.setCanRead(true);
        info.setCanWrite(true);
        info.setPath(pathAndName[0]);
        info.setName(pathAndName[1]);
        info.setDirectory(true);
        info.setExists(true);
        info.setModificationDate(0);
        info.setSize(0);

        listing.put(pathAndName[1], info);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectory() post-listing="+listing);
        } // if
        if (LOG.isInfoEnabled()) {
            LOG.info("createDirectory() flushing "+pathAndName[0]+"/: "+listing);
        } // if
        flushMetaData(rootPath, pathAndName, listing);
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectory() flushing empty path "+pathAndName[0]+"/"+pathAndName[1]);
        } // if
        listing = Collections.emptyMap();
        pathAndName[0] += getSeparator();
        pathAndName[0] += pathAndName[1];
        flushMetaData(rootPath, pathAndName, listing);
        return true;
    } // createDirectory()


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modificationDate) {
        boolean success = false;
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        FileInfo info = listing.get(pathAndName[1]);
        // TODO: Still not working from time to time...
        // try {
        // String url = getUrl(rootPath, relativePath)+(info.isDirectory() ? "/" : "");
        // String modificationDateString = DATE_FORMAT.format(new Date(modificationDate));
        // if (log.isInfoEnabled()) {
        // log.info("setLastModified() setting time for "+url+" to "+modificationDateString);
        // } // if
        // Map<QName, String> addProps = new HashMap<QName, String>();
        // addProps.put(QNAME_LAST_MODIFIED_TIME, modificationDateString);
        // try {
        // List<DavResource> result = sardine.patch(url, addProps);
        // if (log.isInfoEnabled()) {
        // log.info("setLastModified() result list size "+result.size());
        // } // if
        // success = (result.size()==1);
        // } catch (IOException e) {
        // log.error("setLastModified()", e);
        // } // try/catch
        // } catch (Exception e) {
        // log.error("setLastModified()", e);
        // } // try/catch
        success = true;
        // TODO: starting from here it's the same as with local files
        if (success) {
            if (LOG.isInfoEnabled()) {
                LOG.info("setLastModified() flushing "+pathAndName[0]+"/"+pathAndName[1]);
            } // if
            info.setModificationDate(modificationDate);
            flushMetaData(rootPath, pathAndName, listing);
        } // if
        return success;
    } // setLastModified()


    @Override
    public boolean setReadOnly(String rootpath, String path) {
        return false;
    } // setReadOnly()


    @Override
    public boolean delete(String rootPath, String relativePath) {
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete() "+relativePath);
            LOG.debug("delete() listing="+listing);
        } // if
        // remove named item
        if (listing.containsKey(pathAndName[1])) {
            FileInfo info = listing.get(pathAndName[1]);
            listing.remove(pathAndName[1]);
            if (LOG.isInfoEnabled()) {
                LOG.info("delete() flushing "+pathAndName[0]+"/"+pathAndName[1]);
            } // if
            flushMetaData(rootPath, pathAndName, listing);
            if (LOG.isInfoEnabled()) {
                LOG.info("delete() listing="+listing);
            } // if
            if (info.isDirectory()) {
                String metaDataPath = getMetaDataPath(relativePath);
                String metaDataUrl = getUrl(rootPath, metaDataPath);
                try {
                    getSardine().delete(metaDataUrl);
                } catch (Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("delete()", e);
                    } // if
                    return false;
                } // try/catch
            } // if
            try {
                getSardine().delete(getUrl(rootPath, relativePath)+(info.isDirectory() ? "/" : ""));
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("delete()", e);
                } // if
                return false;
            } // try/catch
        } // if
        return true;
    } // delete()


    @Override
    public InputStream getInputStream(String rootpath, String path) throws IOException {
        String url = getUrl(rootpath, path);
        return getSardine().get(url);
    } // getInputStream()


    protected OutputStream getOutputStream(String rootPath, final String relativePath, final boolean forPayload) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOutputStream() "+relativePath);
        } // if
        final String url = getUrl(rootPath, relativePath);
        String[] pathAndName = getPathAndName(relativePath);
        if (forPayload&&(!getSardine().exists(url))) {
            FileInfo info = createFileInfo(rootPath, relativePath, pathAndName);
            Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
            listing.put(info.getName(), info);
            if (LOG.isInfoEnabled()) {
                LOG.info("getOutputStream() flushing "+pathAndName[0]+"/"+pathAndName[1]+": "+listing);
            } // if
            flushMetaData(rootPath, pathAndName, listing);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOutputStream() getting output stream for "+url+" "+info);
            } // if
        } // if
        OutputStream result = new com.gc.iotools.stream.os.OutputStreamToInputStream<String>() {

            @Override
            protected String doRead(InputStream input) throws Exception {
                System.out.println("doRead("+forPayload+") "+url);
                try {
                    getSardine().put(url, input);
                } catch (SardineException se) {
                    if ((!forPayload)&&(se.getStatusCode()==403)) {
                        getSardine().put(url, input);
                    } else {
                        throw se;
                    } // if
                } // try/catch
                return "";
            }

        };
        return result;
    } // getOutputStream()

} // DavStorageAccess
