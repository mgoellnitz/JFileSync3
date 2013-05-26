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
 * Loads and saves the program settings from or to an XML file.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSSettingsXML.java,v 1.18 2007/02/26 18:49:11 heidrich Exp $
 * @see jfs.conf.JFSSettings
 */
class JFSSettingsXML extends JFSSettings {

    /**
     * @see JFSSettings#load()
     */
    @Override
    public final void load() {
        if ( !file.exists())
            return;

        clean();

        // Load the contents of the XML file:
        JFSText t = JFSText.getInstance();
        JFSHistoryManager hm = JFSHistoryManager.getInstance();
        try {
            // Compute root:
            Element root = XMLSupport.getDocumentElement(file);
            if (root==null)
                return;

            // Test root element:
            if ( !root.getNodeName().equals("jFileSync")) {
                JFSLog.getErr().getStream().println(t.get("error.xml"));

                return;
            }

            // Read all child nodes:
            Node child = root.getFirstChild();
            Attr attr;

            while (child!=null) {
                if (child.getNodeName().equals("window")) {
                    try {
                        attr = ((Element)child).getAttributeNode("state");
                        windowState = Integer.parseInt(attr.getValue());
                        attr = ((Element)child).getAttributeNode("x");
                        windowX = Integer.parseInt(attr.getValue());
                        attr = ((Element)child).getAttributeNode("y");
                        windowY = Integer.parseInt(attr.getValue());
                        attr = ((Element)child).getAttributeNode("width");
                        windowWidth = Integer.parseInt(attr.getValue());
                        attr = ((Element)child).getAttributeNode("height");
                        windowHeight = Integer.parseInt(attr.getValue());
                    } catch (NumberFormatException e) {
                        // Thrown by parseInt() and parseByte(). Continue in
                        // this case.
                        JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
                    }
                }

                if (child.getNodeName().equals("directories")) {
                    attr = ((Element)child).getAttributeNode("profile");
                    if (attr!=null)
                        lastProfileDir = new File(attr.getValue());
                    attr = ((Element)child).getAttributeNode("srcPair");
                    if (attr!=null)
                        lastSrcPairDir = new File(attr.getValue());
                    attr = ((Element)child).getAttributeNode("tgtPair");
                    if (attr!=null)
                        lastTgtPairDir = new File(attr.getValue());
                }

                if (child.getNodeName().equals("laf")) {
                    attr = ((Element)child).getAttributeNode("class");
                    setLaf(attr.getValue());
                }

                if (child.getNodeName().equals("currentProfile")) {
                    attr = ((Element)child).getAttributeNode("path");
                    if (attr!=null)
                        currentProfile = new File(attr.getValue());
                    attr = ((Element)child).getAttributeNode("isStored");
                    if (attr!=null)
                        JFSConfig.getInstance().setCurrentProfileStored(Boolean.valueOf(attr.getValue()));
                }

                if (child.getNodeName().equals("profile")) {
                    attr = ((Element)child).getAttributeNode("path");

                    if (lastOpenedProfiles.size()<=JFSConst.LAST_OPENED_PROFILES_SIZE)
                        lastOpenedProfiles.add(new File(attr.getValue()));
                }

                if (child.getNodeName().equals("history")) {
                    Element element = (Element)child;
                    Attr src = element.getAttributeNode("src");
                    Attr tgt = element.getAttributeNode("tgt");
                    Attr date = element.getAttributeNode("date");
                    Attr historyFile = element.getAttributeNode("file");

                    if (src!=null&&tgt!=null&&date!=null&&historyFile!=null) {
                        JFSHistory h = new JFSHistoryXML();
                        h.setPair(new JFSDirectoryPair(src.getValue(), tgt.getValue()));
                        h.setDate(Long.parseLong(date.getValue()));
                        h.setFileName(historyFile.getValue());
                        hm.addHistory(h);
                    }
                }

                child = child.getNextSibling();
            }

            // Sort histories after reading all from file:
            hm.sortHistories();
        } catch (Exception e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.load"));
        }
    }


    /**
     * @see JFSSettings#store()
     */
    @Override
    public final void store() {
        // Create home if it does not exists:
        File home = new File(JFSConst.HOME_DIR);
        if ( !home.exists())
            home.mkdir();

        // Clean history files:
        JFSHistoryManager.getInstance().cleanHistories();

        // Create the DOM and store the contents:
        JFSText t = JFSText.getInstance();
        try {
            Document doc = XMLSupport.newDocument();
            if (doc==null)
                return;

            Element root = doc.createElement("jFileSync");
            root.setAttribute("version", JFSConst.getInstance().getString("jfs.version"));

            // Create child elements:
            Element element;

            element = doc.createElement("window");
            element.setAttribute("state", String.valueOf(windowState));
            element.setAttribute("x", String.valueOf(windowX));
            element.setAttribute("y", String.valueOf(windowY));
            element.setAttribute("width", String.valueOf(windowWidth));
            element.setAttribute("height", String.valueOf(windowHeight));
            root.appendChild(doc.createTextNode("\n  "));
            root.appendChild(element);

            element = doc.createElement("directories");
            element.setAttribute("profile", lastProfileDir.getPath());
            element.setAttribute("srcPair", lastSrcPairDir.getPath());
            element.setAttribute("tgtPair", lastTgtPairDir.getPath());
            root.appendChild(doc.createTextNode("\n  "));
            root.appendChild(element);

            element = doc.createElement("laf");
            element.setAttribute("class", laf);
            root.appendChild(doc.createTextNode("\n  "));
            root.appendChild(element);

            element = doc.createElement("currentProfile");
            if (currentProfile!=null)
                element.setAttribute("path", currentProfile.getPath());
            element.setAttribute("isStored", String.valueOf(JFSConfig.getInstance().isCurrentProfileStored()));
            root.appendChild(doc.createTextNode("\n  "));
            root.appendChild(element);

            for (File f : lastOpenedProfiles) {
                element = doc.createElement("profile");
                element.setAttribute("path", f.getPath());
                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(element);
            }

            for (JFSHistory h : JFSHistoryManager.getInstance().getHistories()) {
                if (h.getPair()!=null&&h.getFileName()!=null&&h.getDate()!= -1) {
                    element = doc.createElement("history");
                    element.setAttribute("src", h.getPair().getSrc());
                    element.setAttribute("tgt", h.getPair().getTgt());
                    element.setAttribute("date", String.valueOf(h.getDate()));
                    element.setAttribute("file", h.getFileName());
                    root.appendChild(doc.createTextNode("\n  "));
                    root.appendChild(element);
                }
            }

            root.appendChild(doc.createTextNode("\n"));

            XMLSupport.storeElement(file, root);
        } catch (DOMException e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.configuration"));
        }
    }
}