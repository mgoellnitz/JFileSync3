/*
 * Copyright (C) 2010-2015, Martin Goellnitz
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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import jfs.sync.encrypted.EncryptedFileStorageAccess;
import jfs.sync.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract storage implementation dealing with storag using separate file mata data elements.
 *
 * Plain text meta data for a directory is stored in a separate file which is ignored for all other actions.
 */
public abstract class AbstractMetaStorageAccess extends EncryptedFileStorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetaStorageAccess.class);

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);

    private final Map<String, Map<String, FileInfo>> directoryCache = new HashMap<>();


    public AbstractMetaStorageAccess(String cipher, boolean shortenPaths) {
        super(cipher, shortenPaths);
    } // AbstractMetaStorageAccess()


    protected Map<String, FileInfo> getMetaData(String rootPath, String relativePath) {
        if (directoryCache.containsKey(relativePath)) {
            return directoryCache.get(relativePath);
        } // if
        Map<String, FileInfo> result = new HashMap<>();
        ObjectInputStream ois = null;
        try {
            InputStream inputStream = getInputStream(rootPath, getMetaDataPath(relativePath));
            byte[] credentials = getCredentials(relativePath);
            Cipher cipher = SecurityUtils.getCipher(getCipherSpec(), Cipher.DECRYPT_MODE, credentials);
            inputStream = new CipherInputStream(inputStream, cipher);
            LOG.debug("getMetaData() reading infos for {}", relativePath);
            ois = new ObjectInputStream(inputStream);
            Object o;
            while ((o = ois.readObject())!=null) {
                if (o instanceof FileInfo) {
                    FileInfo fi = (FileInfo) o;
                    if (fi.isDirectory()) {
                        String date;
                        synchronized (FORMATTER) {
                            date = FORMATTER.format(new Date(fi.getModificationDate()));
                        }
                        LOG.debug("getMetaData() {}{}: {}", relativePath+getSeparator(), fi.getName(), date);
                    } // if
                    result.put(fi.getName(), fi);
                } // if
            } // while
            ois.close();
        } catch (FileNotFoundException|EOFException e) {
            // empty directory or - who cares?
        } catch (Exception e) {
            LOG.info("getMetaData() possible issue while reading infos {}", e, e);
        } finally {
            try {
                if (ois!=null) {
                    ois.close();
                } // if
            } catch (Exception ex) {
                // who cares?
            } // try/catch
        } // try/catch
        directoryCache.put(relativePath, result);
        return result;
    } // getMetaData()


    protected abstract OutputStream getOutputStream(String rootPath, String relativePath, boolean forPayload) throws IOException;


    @Override
    public OutputStream getOutputStream(String rootPath, String relativePath) throws IOException {
        return getOutputStream(rootPath, relativePath, true);
    } // getOutputStream()


    /**
     * flushing listing as meta data info for pathAndName[0] in rootPath
     *
     * @param rootPath
     * @param pathAndName
     * path and name for the file and path for which this update takes place
     * @param listing
     */
    public void flushMetaData(String rootPath, String[] pathAndName, Map<String, FileInfo> listing) {
        try {
            LOG.debug("flushMetaData() flushing {}", listing);
            OutputStream os = getOutputStream(rootPath, getMetaDataPath(pathAndName[0]), false);

            try {
                byte[] credentials = getCredentials(pathAndName[0]);
                Cipher cipher = SecurityUtils.getCipher(getCipherSpec(), Cipher.ENCRYPT_MODE, credentials);
                os = new CipherOutputStream(os, cipher);
            } catch (InvalidKeyException e) {
                LOG.error("flushMetaData()", e);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("flushMetaData()", e);
            } catch (NoSuchPaddingException e) {
                LOG.error("flushMetaData()", e);
            } // try/catch

            ObjectOutputStream oos = new ObjectOutputStream(os);

            for (FileInfo info : listing.values()) {
                LOG.debug("flushMetaData() writing {}", info.getName());
                oos.writeObject(info);
            } // for
            oos.flush();
            os.close();
            if (LOG.isDebugEnabled()) {
                Map<String, FileInfo> backtest = getMetaData(rootPath, pathAndName[0]);
                for (FileInfo info : backtest.values()) {
                    LOG.debug("flushMetaData() reading {}", info.getName());
                } // for
            } // if
        } catch (IOException ioe) {
            LOG.error("flushMetaData() error writing meta data ", ioe);
        } // try/catch
    } // flushMetaData()


    public Map<String, FileInfo> getParentListing(String rootPath, String[] pathAndName) {
        Map<String, FileInfo> listing = getMetaData(rootPath, pathAndName[0]);
        LOG.debug("getParentListing({}) {}", pathAndName[0], listing);
        return listing;
    } // getParentListing()


    @Override
    public String[] list(String rootPath, String relativePath) {
        Map<String, FileInfo> listing = getMetaData(rootPath, relativePath);
        String[] result = new String[listing.size()];
        int i = 0;
        for (String name : listing.keySet()) {
            result[i++] = name;
        } // for
        return result;
    } // list()


    @Override
    public void flush(String rootPath, FileInfo info) {
        String[] pathAndName = new String[2];
        pathAndName[0] = info.getPath();
        pathAndName[1] = info.getName();
        Map<String, FileInfo> listing = getParentListing(rootPath, pathAndName);
        if (listing.containsKey(info.getName())) {
            listing.remove(info.getName());
        } // if
        listing.put(info.getName(), info);
        LOG.info("flush() flushing {}/{}", pathAndName[0], pathAndName[1]);
        flushMetaData(rootPath, pathAndName, listing);
    } // flush()

} // AbstractMetaStorageAccess
