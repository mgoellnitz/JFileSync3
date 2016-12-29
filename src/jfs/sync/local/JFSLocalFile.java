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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jfs.conf.JFSConfig;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a directory or a simple file. This object encapsulates the Java File object.
 *
 * @author Jens Heidrich
 * @version $Id: JFSLocalFile.java,v 1.13 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSLocalFile extends JFSFile {

    private static final Logger LOG = LoggerFactory.getLogger(JFSLocalFile.class);

    /**
     * The corresponding file object.
     */
    private final File file;

    /**
     * The name of the file.
     */
    private final String name;

    /**
     * The path of the file.
     */
    private final String path;

    /**
     * Tells whether the file is a directory.
     */
    private boolean directory = false;

    /**
     * Tells whether we can read the file.
     */
    private boolean readable = true;

    /**
     * Tells whether we can write to the file.
     */
    private boolean writable = true;

    /**
     * Determines whether the file exists.
     */
    private boolean existing = false;

    /**
     * The length of the file. Zero for directories.
     */
    private long length = 0;

    /**
     * The time of last modification of the file. Zero for directories.
     */
    private long lastModified = 0;

    /**
     * If the file is a directory this points to all files (and directories) within the directory. Null for all non
     * directories.
     */
    private JFSFile[] list = null;

    /**
     * The last input stream opened for this file.
     */
    private InputStream in = null;

    /**
     * The last output stream opened for this file.
     */
    private OutputStream out = null;


    /**
     * Creates a new local JFS file object.
     *
     * @param fileProducer
     * The assigned file producer.
     * @param relativePath
     * The relative path of the JFS file starting from the root JFS file.
     */
    public JFSLocalFile(JFSFileProducer fileProducer, String relativePath) {
        super(fileProducer, relativePath);
        file = new File(fileProducer.getRootPath()+getRelativePath());
        name = file.getName();
        path = file.getPath();
        directory = file.isDirectory();
        existing = file.exists();
        if (LOG.isDebugEnabled()) {
            LOG.debug("() "+getPath()+" e["+existing+"] d["+directory+"]");
        }
        if (existing) {
            readable = file.canRead();
            writable = file.canWrite();
        }
        if (!directory) {
            lastModified = file.lastModified();
            length = file.length();
        }
    }


    /**
     * @see JFSFile#getName()
     */
    @Override
    public final String getName() {
        LOG.debug("getName() {}", name);
        return name;
    }


    /**
     * @see JFSFile#getPath()
     */
    @Override
    public final String getPath() {
        LOG.debug("getPath() {}", path);
        return path;
    }


    /**
     * @see JFSFile#isDirectory()
     */
    @Override
    public final boolean isDirectory() {
        LOG.debug("isDirectory() {}", directory);
        return directory;
    }


    /**
     * @see JFSFile#canRead()
     */
    @Override
    public final boolean canRead() {
        LOG.debug("canRead() {}", readable);
        return readable;
    }


    /**
     * @see JFSFile#canWrite()
     */
    @Override
    public final boolean canWrite() {
        LOG.debug("canWrite() {}", writable);
        return writable;
    }


    /**
     * @see JFSFile#getLength()
     */
    @Override
    public final long getLength() {
        LOG.debug("getLength() {}", length);
        return length;
    }


    /**
     * @see JFSFile#getLastModified()
     */
    @Override
    public final long getLastModified() {
        LOG.debug("getLastModified() {}", lastModified);
        return lastModified;
    }


    /**
     * @see JFSFile#getList()
     */
    @Override
    public final JFSFile[] getList() {
        if (list==null) {
            String[] files = file.list();

            if (files!=null) {
                list = new JFSFile[files.length];

                for (int i = 0; i<files.length; i++) {
                    list[i] = new JFSLocalFile(fileProducer, getRelativePath()+File.separatorChar+files[i]);
                }
            } else {
                list = new JFSLocalFile[0];
            }
        }

        LOG.debug("getList() {}", list, ""); // Last string just to avoid a compiler warning
        return list;
    }


    /**
     * @see JFSFile#exists()
     */
    @Override
    public final boolean exists() {
        LOG.debug("JFSLocalFile.exists() {}", existing);
        return existing;
    }


    /**
     * @see JFSFile#mkdir()
     */
    @Override
    public final boolean mkdir() {
        boolean success = file.mkdir();

        if (success) {
            directory = true;
        }

        return success;
    }


    /**
     * @see JFSFile#setLastModified(long)
     */
    @Override
    public final boolean setLastModified(long time) {
        boolean success = file.setLastModified(time);

        if (success) {
            lastModified = time;
        }

        return success;
    }


    /**
     * @see JFSFile#setReadOnly()
     */
    @Override
    public final boolean setReadOnly() {
        if (!JFSConfig.getInstance().isSetCanWrite()) {
            return true;
        }

        boolean success = file.setReadOnly();

        if (success) {
            writable = false;
        }

        return success;
    }


    /**
     * @see JFSFile#delete()
     */
    @Override
    public final boolean delete() {
        return file.delete();
    }


    /**
     * @see JFSFile#getInputStream()
     */
    @Override
    protected InputStream getInputStream() {
        try {
            in = new FileInputStream(getPath());
            return in;
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    /**
     * @see JFSFile#getOutputStream()
     */
    @Override
    protected OutputStream getOutputStream() {
        try {
            out = new FileOutputStream(getPath());
            return out;
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    /**
     * @see JFSFile#closeInputStream()
     */
    @Override
    protected void closeInputStream() {
        JFSText t = JFSText.getInstance();
        try {
            if (in!=null) {
                in.close();
                in = null;
            }
        } catch (IOException e) {
            JFSLog.getErr().getStream().println(t.get("error.io")+" "+e);
        }
    }


    /**
     * @see JFSFile#closeOutputStream()
     */
    @Override
    protected void closeOutputStream() {
        JFSText t = JFSText.getInstance();
        try {
            if (out!=null) {
                out.close();
                out = null;
            }
        } catch (IOException e) {
            JFSLog.getErr().getStream().println(t.get("error.io")+" "+e);
        }
    }


    /**
     * @see JFSFile#preCopyTgt(JFSFile)
     */
    @Override
    protected boolean preCopyTgt(JFSFile srcFile) {
        return true;
    }


    /**
     * @see JFSFile#preCopySrc(JFSFile)
     */
    @Override
    protected boolean preCopySrc(JFSFile tgtFile) {
        return true;
    }


    /**
     * @see JFSFile#postCopyTgt(JFSFile)
     */
    @Override
    protected boolean postCopyTgt(JFSFile srcFile) {
        boolean success = true;

        // Set last modified and read-only only when file is no directory:
        if (!JFSProgress.getInstance().isCanceled()&&!srcFile.isDirectory()) {
            existing = true;
            length = srcFile.getLength();
            success = success&&setLastModified(srcFile.getLastModified());
            if (!srcFile.canWrite()) {
                success = success&&setReadOnly();
            }
        }

        return success;
    }


    /**
     * @see JFSFile#postCopySrc(JFSFile)
     */
    @Override
    protected boolean postCopySrc(JFSFile tgtFile) {
        return true;
    }


    /**
     * @see JFSFile#flush()
     */
    @Override
    public boolean flush() {
        return true;
    }

}
