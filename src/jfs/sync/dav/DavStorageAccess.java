/*
 * Copyright (C) 2010-2021 Martin Goellnitz
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

import com.gc.iotools.stream.os.OutputStreamToInputStream;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jfs.conf.JFSConfig;
import jfs.sync.encryption.AbstractMetaStorageAccess;
import jfs.sync.encryption.FileInfo;
import jfs.sync.encryption.StorageAccess;
import jfs.sync.util.DavUtils;
import jfs.sync.util.WindowsProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Storage access with encrypted files, separate meta data file for each directory and a WebDAV backend.
 */
public class DavStorageAccess extends AbstractMetaStorageAccess implements StorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(DavStorageAccess.class);

    private Sardine sardine = null;

    /*
     * To speed things up we have a second DavResource based directory cache
     */
    private final Map<String, List<DavResource>> directoryCache = new HashMap<>();


    public DavStorageAccess(String cipher) {
        super(cipher, false);
    } // DavStorageAccess()


    private Sardine getSardine() {
        if (sardine==null) {
            String username = JFSConfig.getInstance().getServerUserName();
            String passphrase = JFSConfig.getInstance().getServerPassPhrase();
            sardine = SardineFactory.begin(username, passphrase, WindowsProxySelector.getInstance());
            LOG.debug("getSardine() webdav client {}", sardine);
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
        LOG.debug("getUrl({}) {}{}", relativePath, rootPath, urlSegment);
        return rootPath+urlSegment;
    } // getUrl()


    private List<DavResource> getListing(String rootPath, String url) throws IOException {
        if (directoryCache.containsKey(url)) {
            return directoryCache.get(url);
        } // if
        boolean available = true;
        if (url.length()>rootPath.length()) {
            String[] pathAndName = getPathAndName(url);
            available = getListing(rootPath, pathAndName[0]).contains(pathAndName[1]);
        } // if
        LOG.info("getListing() listing: {} - {}", url, available);
        List<DavResource> listing = null;
        try {
            if (available) {
                listing = getSardine().list(url+getSeparator());
                LOG.info("getListing({}) listing {}", listing.size(), url);
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
        LOG.debug("createFileInfo() url={}", url);
        FileInfo result = new FileInfo();
        result.setExists(false);
        result.setCanRead(true);
        result.setCanWrite(true);
        result.setPath(pathAndName[0]);
        result.setName(pathAndName[1]);
        Collection<DavResource> resources = Collections.emptyList();
        try {
            String[] urlPathAndName = getPathAndName(url);
            LOG.debug("createFileInfo() - {} / {}", urlPathAndName[0], urlPathAndName[1]);
            resources = getListing(rootPath, urlPathAndName[0]);
            for (DavResource resource : resources) {
                if (urlPathAndName[1].equals(resource.getName())) {
                    result.setDirectory(resource.isDirectory());
                    result.setExists(true);
                    long modificationTime = DavUtils.getModificationDate(resource);
                    result.setModificationDate(modificationTime);
                    result.setSize(resource.isDirectory() ? 0 : resource.getContentLength());
                } // if
            } // for
        } catch (Exception e) {
            LOG.error("createFileInfo()", e);
        } // try/catch

        LOG.debug("createFileInfo({}/{}) {}", pathAndName[0], pathAndName[1], result);
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
        LOG.debug("createDirectory() {}", relativePath);
        String url = getUrl(rootPath, relativePath);
        try {
            getSardine().createDirectory(url);
        } catch (Exception e) {
            if (e instanceof SardineException) {
                SardineException se = (SardineException) e;
                LOG.warn("createDirectory({}) status code: {} {}", url, se.getStatusCode(), se.getResponsePhrase());
            } // if
            LOG.warn("createDirectory()", e);
            return false;
        } // try/catch
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        LOG.debug("createDirectory({}) pre-listing={}", relativePath, listing);

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
        LOG.debug("createDirectory() post-listing={}", listing);
        LOG.info("createDirectory() flushing {}/: {}", pathAndName[0], listing);
        flushMetaData(rootPath, pathAndName, listing);
        LOG.debug("createDirectory() flushing empty path {}/{}", pathAndName[0], pathAndName[1]);
        listing = Collections.emptyMap();
        pathAndName[0] += getSeparator();
        pathAndName[0] += pathAndName[1];
        flushMetaData(rootPath, pathAndName, listing);
        return true;
    } // createDirectory()


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modified) {
        boolean success = false;
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        FileInfo info = listing.get(pathAndName[1]);
        try {
            String url = getUrl(rootPath, relativePath)+(info.isDirectory() ? "/" : "");
            success = DavUtils.setLastModified(sardine, url, modified);
        } catch (Exception e) {
            LOG.error("setLastModified()", e);
        } // try/catch

        // TODO: starting from here it's the same as with local files
        if (success) {
            LOG.info("setLastModified() flushing {}/{}", pathAndName[0], pathAndName[1]);
            info.setModificationDate(modified);
            flushMetaData(rootPath, pathAndName, listing);
        } // if
        return success;
    } // setLastModified()


    @Override
    public boolean setWritable(String rootpath, String path, boolean writable) {
        return false;
    } // setWritable()


    @Override
    public boolean delete(String rootPath, String relativePath) {
        String[] pathAndName = getPathAndName(relativePath);
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        LOG.debug("delete() {}", relativePath);
        LOG.debug("delete() listing={}", listing);
        // remove named item
        if (listing.containsKey(pathAndName[1])) {
            FileInfo info = listing.get(pathAndName[1]);
            listing.remove(pathAndName[1]);
            LOG.info("delete() flushing {}/{}", pathAndName[0], pathAndName[1]);
            flushMetaData(rootPath, pathAndName, listing);
            LOG.info("delete() listing={}", listing);
            if (info.isDirectory()) {
                String metaDataPath = getMetaDataPath(relativePath);
                String metaDataUrl = getUrl(rootPath, metaDataPath);
                try {
                    getSardine().delete(metaDataUrl);
                } catch (Exception e) {
                    LOG.warn("delete()", e);
                    return false;
                } // try/catch
            } // if
            try {
                getSardine().delete(getUrl(rootPath, relativePath)+(info.isDirectory() ? "/" : ""));
            } catch (Exception e) {
                LOG.warn("delete()", e);
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
        LOG.debug("getOutputStream() {}", relativePath);
        final String url = getUrl(rootPath, relativePath);
        String[] pathAndName = getPathAndName(relativePath);
        if (forPayload&&(!getSardine().exists(url))) {
            FileInfo info = createFileInfo(rootPath, relativePath, pathAndName);
            Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
            listing.put(info.getName(), info);
            LOG.info("getOutputStream() flushing {}/{}: {}", pathAndName[0], pathAndName[1], listing);
            flushMetaData(rootPath, pathAndName, listing);
            LOG.debug("getOutputStream() getting output stream for {} {}", url, info);
        } // if
        OutputStreamToInputStream<String> result = new OutputStreamToInputStream<String>() {

            @Override
            protected String doRead(InputStream input) throws Exception {
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
        result.setDefaultPipeSize(256000);
        return result;
    } // getOutputStream()

} // DavStorageAccess
