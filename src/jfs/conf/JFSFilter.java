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

package jfs.conf;

import java.io.File;
import java.util.regex.PatternSyntaxException;

import jfs.sync.JFSFile;

/**
 * This class specifies a single filter used to identify files that should be excluded from synchronization. Files
 * matching the specified filter will not be added to the synchronization list.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSFilter.java,v 1.8 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSFilter implements Cloneable {

    /** The filter type determines which part of the file should be checked. */
    public static enum FilterType {
        NAME("profile.filter.type.name"), RELATIVE_PATH("profile.filter.type.relativePath"), PATH(
                "profile.filter.type.path");
        private String name;


        FilterType(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }

    /** The range determines for which files the filter should be applied. */
    public static enum FilterRange {
        ALL("profile.filter.range.all"), DIRECTORIES("profile.filter.range.directories"), FILES(
                "profile.filter.range.files");
        private String name;


        FilterRange(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }

    /** The filter type. */
    private FilterType type = FilterType.NAME;

    /** The filter range. */
    private FilterRange range = FilterRange.ALL;

    /** Determines whether the filter should be applied or not. */
    private boolean isActive = true;

    /** The regular expression representing the filter. */
    private String filter;


    /**
     * Creates a new filter.
     * 
     * @param filter
     *            The filter to use.
     */
    public JFSFilter(String filter) {
        this.filter = filter;
    }


    /**
     * @return Returns the range.
     */
    public FilterRange getRange() {
        return range;
    }


    /**
     * @param range
     *            The range to set.
     */
    public void setRange(FilterRange range) {
        this.range = range;
    }


    /**
     * @param range
     *            The range to set.
     */
    public void setRange(String range) {
        boolean foundRange = false;
        for (FilterRange fRange : FilterRange.values()) {
            if (range.equals(String.valueOf(fRange).toLowerCase())) {
                setRange(fRange);
                foundRange = true;
            }
        }
        if ( !foundRange) {
            JFSLog.getErr().getStream().println(JFSText.getInstance().get("error.numberFormat"));
        }
    }


    /**
     * @return Returns the type.
     */
    public FilterType getType() {
        return type;
    }


    /**
     * @param type
     *            The type to set.
     */
    public void setType(FilterType type) {
        this.type = type;
    }


    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        boolean foundType = false;
        for (FilterType fType : FilterType.values()) {
            if (type.equals(String.valueOf(fType).toLowerCase())) {
                setType(fType);
                foundType = true;
            }
        }
        if ( !foundType) {
            JFSLog.getErr().getStream().println(JFSText.getInstance().get("error.numberFormat"));
        }
    }


    /**
     * @return Returns the regular expression of the filter.
     */
    public String getFilter() {
        return filter;
    }


    /**
     * @param filter
     *            Sets the regular expression of the filter.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }


    /**
     * @return Returns the whether to filter is active.
     */
    public boolean isActive() {
        return isActive;
    }


    /**
     * @param isActive
     *            Activates or deactivates the filter.
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }


    /**
     * Tests whether a given file machtes the filter. If the filter is not active false is returned. If the filter is
     * active, the regular expression of the filter is tested against a given file.
     * 
     * @param file
     *            The file to test.
     * @return Returns true if the filter is active and the given file matches the regular expression of the filter.
     */
    public boolean matches(JFSFile file) {
        try {
            return isActive
                    &&(range==FilterRange.ALL||file.isDirectory()&&range==FilterRange.DIRECTORIES|| !file.isDirectory()
                            &&range==FilterRange.FILES)
                    &&(type==FilterType.NAME&&file.getName().matches(filter)||type==FilterType.RELATIVE_PATH
                            &&file.getRelativePath().matches("\\"+File.separator+"?"+filter)||type==FilterType.PATH
                            &&file.getPath().matches(filter));
        } catch (PatternSyntaxException e) {
            return false;
        }
    }


    /**
     * @see Object#clone()
     */
    @Override
    public JFSFilter clone() {
        JFSFilter clone = new JFSFilter(filter);
        clone.setActive(isActive);
        clone.setType(type);
        clone.setRange(range);
        return clone;
    }


    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof JFSFilter) {
            JFSFilter f = (JFSFilter)o;
            return filter.equals(f.getFilter())&&isActive==f.isActive()&&type==f.getType()&&range==f.getRange();
        }
        return false;
    }
}