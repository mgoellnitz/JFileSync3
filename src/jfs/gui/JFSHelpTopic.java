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

package jfs.gui;

import java.net.URL;

import jfs.conf.JFSConst;
import jfs.conf.JFSText;

/**
 * Represents a help topic.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHelpTopic.java,v 1.13 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSHelpTopic implements Comparable<JFSHelpTopic> {

    /** The ID of the help topic. */
    private String id;

    /** The title of the topic id. */
    private String title;


    /**
     * Creates a help topic object.
     * 
     * @param id
     *            The ID of the help topic.
     */
    public JFSHelpTopic(String id) {
        this.id = id;

        // Get the title from the translation object:
        // (The content usually has many lines, so we
        // just read it, if we need it.)
        JFSText t = JFSText.getInstance();
        title = t.get(id);
    }


    /**
     * Returns the topic ID.
     * 
     * @return Topic ID.
     */
    public final String getId() {
        return id;
    }


    /**
     * Returns the topic title.
     * 
     * @return Topic title.
     */
    public final String getTitle() {
        return title;
    }


    /**
     * Returns the URL of the help topic.
     * 
     * @return The URL.
     */
    public final URL getUrl() {
        return JFSConst.getInstance().getResourceUrl(id);
    }


    /**
     * Returns the result of the comparison of the titles of two help topic objects.
     * 
     * @param topic
     *            The help topic object to compare the current object with.
     * @return Result of the comparison.
     */
    @Override
    public final int compareTo(JFSHelpTopic topic) {
        String startTopic = JFSConst.getInstance().getString("jfs.help.startTopic");
        boolean isThisStart = this.getId().equals(startTopic);
        boolean isTopicStart = topic.getId().equals(startTopic);

        if ( !isThisStart&& !isTopicStart)
            return this.getTitle().compareTo(topic.getTitle());
        else if (isThisStart)
            return -1;
        else if (isTopicStart)
            return 1;

        return 0;
    }


    /**
     * Returns the string representation of the object.
     * 
     * @return The string representation.
     */
    @Override
    public final String toString() {
        return title;
    }
}