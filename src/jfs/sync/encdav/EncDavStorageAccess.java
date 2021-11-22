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
package jfs.sync.encdav;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import jfs.conf.JFSConfig;
import jfs.sync.base.AbstractJFSFileProducerFactory;
import jfs.sync.encryption.AbstractEncryptedStorageAccess;
import jfs.sync.encryption.FileInfo;
import jfs.sync.encryption.StorageAccess;
import jfs.sync.util.DavUtils;
import jfs.sync.util.WindowsProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Storage access with encrypted files, leaving outmeta data file for each directory but still a direct WebDAV backend.
 */
public class EncDavStorageAccess extends AbstractEncryptedStorageAccess implements StorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(EncDavStorageAccess.class);

    private final Map<String, List<DavResource>> folderCache = new HashMap<>();

    private Sardine sardine = null;

    private final String cipherspec;


    public EncDavStorageAccess(String cipher, boolean shortenPaths) {
        super(shortenPaths);
        cipherspec = cipher;
    } // EncDavStorageAccess()


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
    }


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


    @Override
    public String getCipherSpec() {
        return cipherspec;
    } // getCipherSpec()


    protected List<DavResource> getListing(String rootPath, String relativePath) {
        String url = getUrl(rootPath, relativePath);
        List<DavResource> listing = Collections.emptyList();
        if (folderCache.containsKey(url)) {
            listing = folderCache.get(url);
        } else {
            try {
                listing = getSardine().list(url, 1, DavUtils.getCustomDavProperties());
            } catch (Exception e) {
                LOG.error("getListing()", e);
            } // try/catch
        } // if
        LOG.debug("getListing() {} elements in {}", listing.size(), url);
        return listing;
    } // getListing()


    private String getEncryptedPathElement(DavResource item, int prefixLength) {
        String rPath = item.getPath().substring(prefixLength);
        if (rPath.endsWith("/")) {
            rPath = rPath.substring(0, rPath.length()-1);
        } // if
        int x = rPath.lastIndexOf('/');
        if (x>0) {
            rPath = rPath.substring(x+1);
        } // if
        LOG.debug("getEncryptedPathElement({})  {}", item, rPath);
        return rPath;
    } // getEncryptedPathElement()


    protected DavResource getFile(String rootPath, String relativePath) {
        int uriStartIndex = rootPath.indexOf('/', 9);
        int prefixLength = rootPath.substring(uriStartIndex).length()+1;
        String[] pathAndName = AbstractJFSFileProducerFactory.getPathAndName(relativePath, "/");
        String pathElement = pathAndName[1].length()>0 ? getEncryptedFileName(pathAndName[0], pathAndName[1]) : pathAndName[1];
        LOG.debug("getFile() {} + {} {}", relativePath, pathAndName[1], pathElement);
        List<DavResource> listing = getListing(rootPath, pathAndName[0]);
        DavResource result = null;
        for (DavResource item : listing) {
            String rPath = getEncryptedPathElement(item, prefixLength);
            // LOG.debug("getFile() {} '{}'=='{}' ? {}", item, rPath, pathElement, rPath.equals(pathElement));
            if (rPath.equals(pathElement)) {
                result = item;
            } // if
        }  // for
        LOG.debug("getFile() {} - {} / {}: {}", relativePath, pathAndName[0], pathAndName[1], result);
        if (result==null) {
            LOG.error("getFile() {}", relativePath, new Exception(""));
        } // if
        return result;
    } // getFile()


    @Override
    public String[] list(String rootPath, String relativePath) {
        List<DavResource> items = getListing(rootPath, relativePath);

        int uriStartIndex = rootPath.indexOf('/', 9);
        int prefixLength = rootPath.substring(uriStartIndex).length()+1;
        LOG.debug("list() {} - {} [{}:{}]", rootPath, relativePath, uriStartIndex, prefixLength);
        // decrypt
        String[] result = new String[items.size()-1];
        int i = 1;
        while (i<items.size()) {
            DavResource item = items.get(i);
            LOG.debug("list() {}: {}", relativePath, item);
            String rPath = getEncryptedPathElement(item, prefixLength);
            String decryptedItem = getDecryptedFileName(relativePath, rPath);
            result[(i++)-1] = decryptedItem;
            LOG.debug("list() {} -> {}", rPath, decryptedItem);
        } // for

        // sort out meta data
        Collection<String> itemCollection = new HashSet<>();
        for (String item : result) {
            if (!getMetaDataFileName(relativePath).equals(item)) {
                itemCollection.add(item);
            } // if
        } // for

        // repackage as array
        result = new String[itemCollection.size()];
        i = 0;
        for (String item : itemCollection) {
            result[i++] = item;
        } // for
        return result;
    } // list()


    @Override
    public FileInfo getFileInfo(String rootPath, String relativePath) {
        FileInfo result = new FileInfo();
        String name = getLastPathElement(relativePath, relativePath);
        result.setName(name);
        result.setPath(rootPath+relativePath);

        DavResource resource = getFile(rootPath, relativePath);
        result.setCanRead(false);
        result.setCanWrite(false);
        result.setDirectory(false);
        if (resource!=null) {
            result.setDirectory(resource.isDirectory());
            result.setExists(true);
        } else {
            result.setExists(false);
        } // if
        LOG.debug("getFileInfo() {} e[{}] d[{}]", result.getPath(), result.isExists(), result.isDirectory());
        if (result.isExists()) {
            result.setCanRead(true);
            result.setCanWrite(true);
            if (!result.isDirectory()) {
                result.setModificationDate(DavUtils.getModificationDate(resource));
                result.setSize(-1);
            } else {
                result.setSize(0);
            } // if
        } else {
            LOG.debug("getFileInfo() could not detect file for {}", result.getPath());
        } // if
        return result;
    } // getFileInfo()

    /* TODO: above this line needs modification */

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
        return true;
    }


    @Override
    public boolean setLastModified(String rootPath, String relativePath, long modificationDate) {
        boolean success = false;
        try {
            DavResource resource = getFile(rootPath, relativePath);
            String url = getUrl(rootPath, relativePath)+(resource.isDirectory() ? "/" : "");
            success = DavUtils.setLastModified(sardine, url, modificationDate);
        } catch (Exception e) {
            LOG.error("setLastModified()", e);
        } // try/catch

        return success;
    }


    @Override
    public boolean setWritable(String rootPath, String relativePath, boolean writable) {
        return true;
    }


    @Override
    public boolean delete(String rootPath, String relativePath) {
        try {
            DavResource resource = getFile(rootPath, relativePath);
            getSardine().delete(getUrl(rootPath, relativePath)+(resource.isDirectory() ? "/" : ""));
        } catch (Exception e) {
            LOG.warn("delete()", e);
            return false;
        } // try/catch
        return true;
    } // delete()


    @Override
    public InputStream getInputStream(String rootpath, String relativePath) throws IOException {
        String url = getUrl(rootpath, relativePath);
        return getSardine().get(url);
    } // getInputStream()


    @Override
    public OutputStream getOutputStream(String rootPath, final String relativePath) throws IOException {
        LOG.debug("getOutputStream() {}", relativePath);
        final String url = getUrl(rootPath, relativePath);
        OutputStream result = new com.gc.iotools.stream.os.OutputStreamToInputStream<String>() {

            @Override
            protected String doRead(InputStream input) throws Exception {
                getSardine().put(url, input);
                return "";
            }

        };
        return result;
    } // getOutputStream()


    @Override
    public void flush(String rootPath, FileInfo info) {
        // Nothing to do in this implementation
    } // flush()

} // EnvDavStorageAccess
