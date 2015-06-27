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
import java.io.IOException;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Provides some methods to ease XML handling.
 *
 * @author Jens Heidrich
 * @version $Id: XMLSupport.java,v 1.5 2007/02/26 18:49:11 heidrich Exp $
 */
public final class XMLSupport {

    private XMLSupport() {
    }


    /**
     * Returns the document element (root tag) of a given XML file. If no file
     * exists null is returned.
     *
     * @param file
     * The file to parse.
     * @return The document element of the parsed file.
     */
    public static Element getDocumentElement(File file) {
        if (!file.exists()) {
            return null;
        }

        // Get translation object:
        JFSText t = JFSText.getInstance();

        try {
            // Get root:
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(file);
            Element root = xmlDoc.getDocumentElement();

            // Normalize text representation:
            root.normalize();

            return root;
        } catch (SAXParseException err) {
            PrintStream p = JFSLog.getErr().getStream();
            p.println(t.get("error.xml.parser"));
            p.println("  "+t.get("error.xml.parserLine")+" "
                    +err.getLineNumber());
            p.println("  "+t.get("error.xml.parserUri")+" "
                    +err.getSystemId());
        } catch (SAXException e) {
            JFSLog.getErr().getStream().println(t.get("error.sax"));
        } catch (IOException ioe) {
            JFSLog.getErr().getStream().println(
                    t.get("error.io")+" '"+file+"'.");
        } catch (Throwable th) {
            JFSLog.getErr().getStream().println(t.get("error.xml.load"));
        }

        return null;
    }


    /**
     * Creates a new document.
     *
     * @return The new document.
     */
    public static Document newDocument() {
        JFSText t = JFSText.getInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException pce) {
            JFSLog.getErr().getStream().println(
                    t.get("error.xml.configuration"));
        }

        return null;
    }


    /**
     * Stores an DOM to a file.
     *
     * @param file
     * The file to write to.
     * @param root
     * The document element to write.
     * @return True if and only if writing was successful.
     */
    public static boolean storeElement(File file, Element root) {
        JFSText t = JFSText.getInstance();
        try {
			// Create a File object from the path name and prove for valid path
            // name:
            File parent = file.getParentFile();

            if ((parent==null)||!parent.exists()) {
                JFSLog.getErr().getStream().println(
                        t.get("error.validPath")+" '"+file+"'.");
                return false;
            }

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(root);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            return true;
        } catch (TransformerConfigurationException tce) {
            JFSLog.getErr().getStream().println(
                    t.get("error.xml.configuration"));
        } catch (TransformerException te) {
            JFSLog.getErr().getStream().println(t.get("error.xml.transformer"));
        } catch (Throwable th) {
            JFSLog.getErr().getStream().println(t.get("error.xml.save"));
        }

        return false;
    }

}
