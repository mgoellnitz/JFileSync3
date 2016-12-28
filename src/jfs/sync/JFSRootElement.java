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

import java.io.PrintStream;
import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSHistory;
import jfs.conf.JFSHistoryManager;
import jfs.conf.JFSLog;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.conf.JFSText;


/**
 * Represents a JFS root element. File factories are created for the source and target side, which may be accessed (and
 * shut down) via this object.
 *
 * @author Jens Heidrich
 * @version $Id: JFSRootElement.java,v 1.4 2007/07/20 16:35:36 heidrich Exp $
 */
public class JFSRootElement extends JFSElement {

    /**
     * The used producer to create source JFS file objects.
     */
    private final JFSFileProducer srcProducer;

    /**
     * The used producer to create target JFS file objects.
     */
    private final JFSFileProducer tgtProducer;

    /**
     * The synchronization history.
     */
    private final JFSHistory history;


    /**
     * Constructs a root element.
     *
     * @param pair
     * The directory pair to construct a root element for.
     */
    public JFSRootElement(JFSDirectoryPair pair) {
        // Create file producers to use:
        JFSFileProducerManager pm = JFSFileProducerManager.getInstance();
        srcProducer = pm.createProducer(pair.getSrc());
        tgtProducer = pm.createProducer(pair.getTgt());

        // Extract root source and target:
        srcFile = srcProducer.getRootJfsFile();
        tgtFile = tgtProducer.getRootJfsFile();

        // Assert root characteristics:
        assert srcFile!=null&&tgtFile!=null;

        // Test whether the file objects exists and are directories:
        JFSText t = JFSText.getInstance();
        if (!srcFile.exists()||!tgtFile.exists()||!srcFile.isDirectory()||!tgtFile.isDirectory()) {
            PrintStream p = JFSLog.getErr().getStream();
            p.println(t.get("error.validDirectoryPair"));
            p.println("  '"+srcProducer.getRootPath()+"', ");
            p.println("  '"+tgtProducer.getRootPath()+"'");
            active = false;
        } else {
            active = true;
        }

        // Set root standard characteristics:
        root = this;
        parent = this;
        directory = true;
        state = ElementState.IS_ROOT;
        action = SyncAction.NOP_ROOT;

        // Load history
        history = JFSHistoryManager.getInstance().getHistory(pair);
        history.load();
    }


    /**
     * Returns the assigned source JFS file producer.
     *
     * @return The source producer.
     */
    public JFSFileProducer getSrcProducer() {
        return srcProducer;
    }


    /**
     * Returns the assigned target JFS file producer.
     *
     * @return The target producer.
     */
    public JFSFileProducer getTgtProducer() {
        return tgtProducer;
    }


    /**
     * Returns the history.
     *
     * @return The history.
     */
    public JFSHistory getHistory() {
        return history;
    }


    /**
     * @see JFSElement#setSrcFile(JFSFile)
     */
    @Override
    public final void setSrcFile(JFSFile file) {
        // empty default
    }


    /**
     * @see JFSElement#setTgtFile(JFSFile)
     */
    @Override
    public final void setTgtFile(JFSFile file) {
        // empty default
    }


    /**
     * @see JFSElement#setAction(JFSSyncMode.SyncAction)
     */
    @Override
    public final void setAction(SyncAction action) {
        // empty default
    }


    /**
     * @see JFSElement#setActive(boolean)
     */
    @Override
    public void setActive(boolean isActive) {
        // empty default
    }

}
