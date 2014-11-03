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
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Loads and saves the configuration entries from or to an XML configuration file.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHistoryXML.java,v 1.7 2007/02/26 18:49:11 heidrich Exp $
 * @see jfs.conf.JFSConfig
 */
class JFSHistoryXML extends JFSHistory {

    /**
     * @see JFSHistory#load(File)
     */
    @Override
    protected boolean load(File file) {
        // Load the contents of the XML file:
        JFSText t = JFSText.getInstance();
        try {
            // Compute root:
            Element root = XMLSupport.getDocumentElement(file);
            if (root==null) {
                return false;
            }

            // Test root element:
            if ( !root.getNodeName().equals("history")) {
                JFSLog.getErr().getStream().println(t.get("error.xml"));

                return false;
            }

            // Read attributes of root element:
            try {
                Attr src = root.getAttributeNode("src");
                Attr tgt = root.getAttributeNode("tgt");
                Attr date = root.getAttributeNode("date");

                if (src==null||tgt==null||date==null) {
                    return false;
                }

                // Check consistency:
                JFSDirectoryPair pair = getPair();
                assert pair!=null&&pair.getSrc().equals(src.getValue())&&pair.getTgt().equals(tgt.getValue())
                        &&getDate()==Long.parseLong(date.getValue());
            } catch (AssertionError e) {
                JFSLog.getErr().getStream().println(t.get("error.xml.load")+":"+e);
                return false;
            } catch (NumberFormatException e) {
                JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
                return false;
            }

            // Read all specified history items:
            Node child = root.getFirstChild();
            history.clear();
            directories.clear();
            files.clear();
            while (child!=null) {
                String nodeName = child.getNodeName();
                if (nodeName.equals("item")) {
                    Element item = (Element)child;
                    try {
                        Attr path = item.getAttributeNode("path");
                        Attr modified = item.getAttributeNode("modified");
                        Attr length = item.getAttributeNode("length");
                        Attr directory = item.getAttributeNode("directory");
                        JFSHistoryItem i = new JFSHistoryItem(path.getValue());
                        i.setLastModified(Long.parseLong(modified.getValue()));
                        i.setLength(Long.parseLong(length.getValue()));
                        i.setDirectory(Boolean.valueOf(directory.getValue()));
                        history.add(i);
                        if (i.isDirectory()) {
                            directories.put(i.getRelativePath(), i);
                        } else {
                            files.put(i.getRelativePath(), i);
                        }
                    } catch (Exception e) {
                        // Write to error log, but continue:
                        JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
                    }
                }
                child = child.getNextSibling();
            }

            return true;
        } catch (Exception e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.load"));
        }

        return false;
    }


    /**
     * @see JFSHistory#store(File)
     */
    @Override
    protected boolean store(File file) {
        // Create the DOM and store the contents:
        JFSText t = JFSText.getInstance();
        try {
            Document doc = XMLSupport.newDocument();
            if (doc==null) {
                return false;
            }

            Element root = doc.createElement("history");

            root.setAttribute("src", getPair().getSrc());
            root.setAttribute("tgt", getPair().getTgt());
            root.setAttribute("date", String.valueOf(getDate()));

            // Create and add history items:
            for (JFSHistoryItem i : history) {
                Element item = doc.createElement("item");
                item.setAttribute("path", i.getRelativePath());
                item.setAttribute("modified", String.valueOf(i.getLastModified()));
                item.setAttribute("length", String.valueOf(i.getLength()));
                item.setAttribute("directory", String.valueOf(i.isDirectory()));
                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(item);
            }

            root.appendChild(doc.createTextNode("\n"));

            return XMLSupport.storeElement(file, root);
        } catch (DOMException e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.configuration"));
        }

        return false;
    }
}