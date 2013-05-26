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

import java.io.File;

import javax.swing.filechooser.FileFilter;

import jfs.conf.JFSText;

/**
 * This class is used in order to filter the list of files shown in the JFileChoose object.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSConfigFileFilter.java,v 1.10 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSConfigFileFilter extends FileFilter {

    /** All allowed extensions: */
    private static final String[] ALLOWED_EXTENSIONS = { "xml", "conf", "jfs" };


    /**
     * Determines whether the given file is accepted by the filter.
     * 
     * @param file
     *            A file in the list of the file chooser.
     * @return True if and only if the file is accepted.
     */
    @Override
    public boolean accept(File file) {
        if (file!=null) {
            // Show file if it is a directory:
            if (file.isDirectory())
                return true;

            // Determine extension of the file:
            String filename = file.getName();
            String extension = "";
            int index = filename.lastIndexOf('.');

            if ((index>0)&&(index<(filename.length()-1)))
                extension = filename.substring(index+1).toLowerCase();

            for (String s : ALLOWED_EXTENSIONS)
                if (extension.equals(s))
                    return true;
        }

        return false;
    }


    /**
     * The description of this filter.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return JFSText.getInstance().get("profile.configFiles");
    }


    /**
     * Returns the allowed extensions.
     * 
     * @return The allowed extensions.
     */
    public String[] getFilterExtensions() {
        String[] extensions = new String[ALLOWED_EXTENSIONS.length];
        for (int i = 0; i<extensions.length; i++ ) {
            extensions[i] = "*."+ALLOWED_EXTENSIONS[i];
        }

        return extensions;
    }
}