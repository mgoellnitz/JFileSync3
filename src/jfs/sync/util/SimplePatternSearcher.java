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
package jfs.sync.util;


/**
 * Simple pattern searcher to find start end end marker occurrences in a string.
 */
public class SimplePatternSearcher {

    private String text;

    private int currentIndex;


    public SimplePatternSearcher(String text) {
        this.text = text;
        this.currentIndex = 0;
    } // SimplePatternSearcher()


    public String findText(String startMarker, String endMarker) {
        String result = null;
        int markerLength = startMarker.length();
        int idx = text.indexOf(startMarker, currentIndex);
        if (idx>=0) {
            currentIndex = idx;
            currentIndex += markerLength;
            idx = text.indexOf(endMarker, currentIndex);

            if (idx>=0) {
                result = text.substring(currentIndex, idx);
                currentIndex = idx;
            } // if
        } // if

        return result;
    } // findText()


    public void reset() {
        currentIndex = 0;
    } // reset()

} // SimplePatternSearcher
