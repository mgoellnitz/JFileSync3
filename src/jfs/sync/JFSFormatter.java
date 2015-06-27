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

import java.io.File;
import java.text.DateFormat;
import java.util.Date;


/**
 * Provides static methods in order to format numbers.
 *
 * @author Jens Heidrich
 * @version $Id: JFSFormatter.java,v 1.2 2007/02/26 18:49:09 heidrich Exp $
 */
public final class JFSFormatter {

    private JFSFormatter() {
    }


    /**
     * Returns the length of the file as a string if the JFSFile is not a directory and empty string otherwise.
     *
     * @param file
     * The file to get the formatted length from.
     * @return Length of the file as a string.
     */
    public static String getLength(JFSFile file) {
        if (file.isDirectory()) {
            return "";
        }

        return getLength(file.getLength());
    }


    /**
     * Returns the formatted length.
     *
     * @param length
     * The length in number of bytes.
     * @return Formatted length.
     */
    public static String getLength(long length) {
        if (length<1000) {
            return length+" b";
        } else if (length/1024<1000) {
            return ((double) (length*10/1024)/10)+" KB";
        } else {
            return ((double) (length*100/1048576)/100)+" MB";
        }
    }


    /**
     * Returns a date string for the last modification of the file.
     *
     * @return Time of last modification of the file as a date string.
     */
    public static String getLastModified(JFSFile file) {
        if (file.isDirectory()) {
            return "";
        }

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(new Date(file.getLastModified()));
    }


    /**
     * Replaces all separator chars by the system specific ones.
     *
     * @param path
     * The original path.
     * @return The modified path.
     */
    public static String replaceSeparatorChar(String path) {
        path = path.replace('\\', File.separatorChar);
        path = path.replace('/', File.separatorChar);

        return path;
    }


    /**
     * Cuts the head of a string if its length exceeds the specified maximum length. If the input string has be cut,
     * "..." is added to its head. However, its length never exceeds the maximum length.
     *
     * @param input
     * The input string. If the is is equal to null, an empty string is returned instead.
     * @param max
     * The maximum length.
     * @return The modified string.
     */
    public static String cutHead(String input, int max) {
        if (input==null||max==0) {
            return "";
        }
        if (max==1) {
            return ".";
        }
        if (max==2) {
            return "..";
        }

        // At this point max is >= 3:
        int start = input.length()-(max-3);

        if (start>0) {
            input = "..."+input.substring(start);
        }

        return input;
    }


    /**
     * Adapts the path of a JFSFile according to the maximum number of displayable characters. If the file is null, a
     * string containing spaces is returned.
     *
     * @see #adapt(String, int)
     * @param input
     * The input JFS file.
     * @param max
     * The maximum length.
     * @return The modified string of length max.
     */
    public static String adaptPath(JFSFile input, int max) {
        if (input!=null) {
            return adapt(input.getPath(), max);
        }
        return adapt(null, max);
    }


    /**
     * Adapts a string according to the maximum number of displayable characters. If the original string has to many
     * chars, the trailing chars are cut, if it has to few chars, spaces are added. Finally the output string is set in
     * quotations. If the input string is null, a string of spaces is returned.
     *
     * @param input
     * The input string.
     * @param max
     * The maximum length.
     * @return The modified string of length max.
     */
    public static String adapt(String input, int max) {
        if (input==null) {
            char[] spaces = new char[max];
            for (int i = 0; i<max; i++) {
                spaces[i] = ' ';
            }
            return new String(spaces);
        }

        int length = input.length();
        String out;

        if (length<max-2) {
            char[] spaces = new char[max-2-length];

            for (int i = 0; i<max-2-length; i++) {
                spaces[i] = ' ';
            }

            out = "'"+input+"'"+new String(spaces);
        } else if (length>max-2) {
            out = "'"+input.substring(length-max+2)+"'";
        } else {
            out = "'"+input+"'";
        }

        return out;
    }

}
