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
 * @version $Id: JFSConfigXML.java,v 1.24 2007/02/26 18:49:11 heidrich Exp $
 * @see jfs.conf.JFSConfig
 */
class JFSConfigXML extends JFSConfig {

    private static final String ATTR_PASSPHRASE = "passphrase";

    private static final String ATTR_CIPHER = "cipher";

    private static final String ATTR_SHORTEN = "shorten";


    /**
     * @see JFSConfig#loadProfile(File)
     */
    @Override
    protected boolean loadProfile(File confFile) {
        // Reset configuration:
        clean();

        // Load the contents of the XML file:
        JFSText t = JFSText.getInstance();
        try {
            // Compute root:
            Element root = XMLSupport.getDocumentElement(confFile);
            if (root==null) {
                return false;
            }

            // Test root element:
            if ( !root.getNodeName().equals("jFileSync")) {
                JFSLog.getErr().getStream().println(t.get("error.xml"));

                return false;
            }

            // Read attributes of root element:
            try {
                Attr attr;
                attr = root.getAttributeNode("title");

                if (attr!=null) {
                    setTitle(attr.getValue());
                }

                attr = root.getAttributeNode("sync");

                if (attr!=null) {
                    setSyncMode(Byte.parseByte(attr.getValue()));
                }

                attr = root.getAttributeNode("view");

                if (attr!=null) {
                    setView(Byte.parseByte(attr.getValue()));
                }

                attr = root.getAttributeNode("granularity");

                if (attr!=null) {
                    setGranularity(Integer.parseInt(attr.getValue()));
                }

                attr = root.getAttributeNode("buffersize");

                if (attr!=null) {
                    setBufferSize(Integer.parseInt(attr.getValue()));
                }

                attr = root.getAttributeNode("keepuseractions");

                if (attr!=null) {
                    setKeepUserActions(Boolean.valueOf(attr.getValue()).booleanValue());
                }

                attr = root.getAttributeNode("storehistory");

                if (attr!=null) {
                    setStoreHistory(Boolean.valueOf(attr.getValue()).booleanValue());
                }

                attr = root.getAttributeNode("setcanwrite");

                if (attr!=null) {
                    setCanWrite(Boolean.valueOf(attr.getValue()).booleanValue());
                }
            } catch (NumberFormatException e) {
                // Thrown by parseInt() and parseByte(). Continue in this case.
                JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
            }

            // Read all specified directories:
            Node child = root.getFirstChild();
            directoryList.clear();

            while (child!=null) {
                String nodeName = child.getNodeName();
                Attr attr;

                if (nodeName.equals("server")) {
                    try {
                        attr = ((Element)child).getAttributeNode("timeout");

                        if (attr!=null) {
                            serverTimeout = Integer.parseInt(attr.getValue());
                        }
                    } catch (Exception e) {
                        // Thrown by parseInt() and parseByte(). Continue in
                        // this case.
                        JFSLog.getErr().getStream().println(t.get("error.numberFormat"));
                    }

                    attr = ((Element)child).getAttributeNode("user");

                    if (attr!=null) {
                        serverUserName = attr.getValue();
                    }

                    attr = ((Element)child).getAttributeNode(ATTR_PASSPHRASE);

                    if (attr!=null) {
                        serverPassPhrase = attr.getValue();
                    } // if
                }

                if (nodeName.equals("encryption")) {
                    attr = ((Element)child).getAttributeNode(ATTR_PASSPHRASE);
                    if (attr!=null) {
                        encryptionPassPhrase = attr.getValue();
                    } // if
                    attr = ((Element)child).getAttributeNode(ATTR_CIPHER);
                    if (attr!=null) {
                        encryptionCipher = attr.getValue();
                    } // if
                    attr = ((Element)child).getAttributeNode(ATTR_SHORTEN);
                    if (attr!=null) {
                        shortenPaths = Boolean.valueOf(attr.getValue()).booleanValue();
                    } // if
                } // if

                if (nodeName.equals("directory")) {
                    Attr src = ((Element)child).getAttributeNode("src");
                    Attr tgt = ((Element)child).getAttributeNode("tgt");

                    if (src!=null&&tgt!=null) {
                        directoryList.add(new JFSDirectoryPair(src.getValue(), tgt.getValue()));
                    }
                }

                if (nodeName.equals("include")||nodeName.equals("exclude")) {
                    Attr active = ((Element)child).getAttributeNode("active");
                    Attr filter = ((Element)child).getAttributeNode("filter");
                    Attr type = ((Element)child).getAttributeNode("type");
                    Attr range = ((Element)child).getAttributeNode("range");

                    if (filter!=null) {
                        JFSFilter f = new JFSFilter(filter.getValue());

                        if (active!=null) {
                            boolean b = Boolean.parseBoolean(active.getValue());
                            f.setActive(b);
                        }
                        if (type!=null) {
                            f.setType(type.getValue());
                        }
                        if (range!=null) {
                            f.setRange(range.getValue());
                        }
                        if (nodeName.equals("include")) {
                            includes.add(f);
                        } else {
                            excludes.add(f);
                        }
                    }
                }

                child = child.getNextSibling();
            }

            // Update all observers:
            fireUpdate();

            return true;
        } catch (Exception e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.load"));
        }

        return false;
    }


    /**
     * @see JFSConfig#storeProfile(File)
     */
    @Override
    protected boolean storeProfile(File confFile) {
        // Create the DOM and store the contents:
        JFSText t = JFSText.getInstance();
        try {
            Document doc = XMLSupport.newDocument();
            if (doc==null) {
                return false;
            }

            Element root = doc.createElement("jFileSync");
            root.setAttribute("version", JFSConst.getInstance().getString("jfs.version"));

            // Create and add attributes to root element if the value differs
            // from the default values:
            if ( !getTitle().equals(JFSText.getInstance().get("profile.defaultTitle"))) {
                root.setAttribute("title", getTitle());
            }

            if (getSyncMode()!=JFSSyncModes.getInstance().getDefaultMode()) {
                root.setAttribute("sync", String.valueOf(getSyncMode()));
            }

            if (getView()!=JFSViewModes.getInstance().getDefaultMode()) {
                root.setAttribute("view", String.valueOf(getView()));
            }

            if (getGranularity()!=JFSConst.GRANULARITY) {
                root.setAttribute("granularity", String.valueOf(getGranularity()));
            }

            if (getBufferSize()!=JFSConst.BUFFER_SIZE) {
                root.setAttribute("buffersize", String.valueOf(getBufferSize()));
            }

            if (isKeepUserActions()!=JFSConst.KEEP_USER_ACTIONS) {
                root.setAttribute("keepuseractions", String.valueOf(isKeepUserActions()));
            }

            if (isStoreHistory()!=JFSConst.STORE_HISTORY) {
                root.setAttribute("storehistory", String.valueOf(isStoreHistory()));
            }

            if (isSetCanWrite()!=JFSConst.SET_CAN_WRITE) {
                root.setAttribute("setcanwrite", String.valueOf(isSetCanWrite()));
            }

            // Add server settings if not equal to default:
            if (!serverUserName.equals(JFSConst.SERVER_USER_NAME)
                    || !serverPassPhrase.equals(JFSConst.SERVER_PASS_PHRASE)||serverTimeout!=JFSConst.SERVER_TIMEOUT
                    ) {
                Element element = doc.createElement("server");

                if ( !serverUserName.equals(JFSConst.SERVER_USER_NAME)) {
                    element.setAttribute("user", serverUserName);
                }

                if ( !serverPassPhrase.equals(JFSConst.SERVER_PASS_PHRASE)) {
                    element.setAttribute(ATTR_PASSPHRASE, serverPassPhrase);
                }

                if (serverTimeout!=JFSConst.SERVER_TIMEOUT) {
                    element.setAttribute("timeout", String.valueOf(serverTimeout));
                }

                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(element);
            }

            // Add server settings if not equal to default:
            if ( !encryptionPassPhrase.equals("")) {
                Element element = doc.createElement("encryption");

                if ( !encryptionPassPhrase.equals("")) {
                    element.setAttribute(ATTR_PASSPHRASE, encryptionPassPhrase);
                }
                if ( !encryptionCipher.equals("AES")) {
                    element.setAttribute(ATTR_CIPHER, encryptionCipher);
                }
               if ( shortenPaths) {
                    element.setAttribute(ATTR_SHORTEN, "true");
                }

                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(element);
            }

            // Create and add directory tags:
            for (JFSDirectoryPair pair : getDirectoryList()) {
                Element dir = doc.createElement("directory");
                dir.setAttribute("src", pair.getSrc());
                dir.setAttribute("tgt", pair.getTgt());
                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(dir);
            }

            // Create and add filter tags:
            for (JFSFilter f : getIncludes()) {
                Element filter = doc.createElement("include");
                filter.setAttribute("filter", f.getFilter());
                filter.setAttribute("active", String.valueOf(f.isActive()));
                filter.setAttribute("type", String.valueOf(f.getType()).toLowerCase());
                filter.setAttribute("range", String.valueOf(f.getRange()).toLowerCase());
                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(filter);
            }
            for (JFSFilter f : getExcludes()) {
                Element filter = doc.createElement("exclude");
                filter.setAttribute("filter", f.getFilter());
                filter.setAttribute("active", String.valueOf(f.isActive()));
                filter.setAttribute("type", String.valueOf(f.getType()).toLowerCase());
                filter.setAttribute("range", String.valueOf(f.getRange()).toLowerCase());
                root.appendChild(doc.createTextNode("\n  "));
                root.appendChild(filter);
            }

            root.appendChild(doc.createTextNode("\n"));

            return XMLSupport.storeElement(confFile, root);
        } catch (DOMException e) {
            JFSLog.getErr().getStream().println(t.get("error.xml.configuration"));
        }

        return false;
    }
}