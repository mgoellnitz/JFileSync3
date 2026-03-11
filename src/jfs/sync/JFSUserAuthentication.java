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

import java.net.URI;
import java.net.URISyntaxException;


/**
 * May be used by file producers in order to ask for the user name and password.
 *
 * @author Jens Heidrich
 * @version $Id: JFSUserAuthentication.java,v 1.1 2008/06/11 12:10:58 heidrich Exp $
 */
public class JFSUserAuthentication {

    /** The only instance of this class. */
    static private JFSUserAuthentication userAuthentication = null;

    /** The interface to answer the question. */
    private JFSUserAuthenticationInterface userInterface = null;

    /** The resource identifier to get the authentication data for. */
    private String resource = "";


    /**
     * Private constructor.
     */
    private JFSUserAuthentication() {
    }


    /**
     * @return Returns the only instance of the class.
     */
    static public JFSUserAuthentication getInstance() {
        if (userAuthentication==null) {
            userAuthentication = new JFSUserAuthentication();
        }
        return userAuthentication;
    }


    /**
     * Sets the class implementing the user interface.
     *
     * @param userInterface
     * The user interface class to set.
     */
    public void setUserInterface(JFSUserAuthenticationInterface userInterface) {
        this.userInterface = userInterface;
    }


    /**
     * @return Retrieves user name and password and returns the user interface
     * delivering both.
     */
    public JFSUserAuthenticationInterface getUserInterface() {
        userInterface.ask(userAuthentication);
        return userInterface;
    }


    /**
     * @return Returns the resource identifier to get the authentication data
     * for.
     */
    public String getResource() {
        return resource;
    }


    /**
     * Sets the resource identifier to get the authentication data for.
     *
     * @param resource
     * The resource to set.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }


    /**
     * @return Returns the specified user name of the resource, if any.
     */
    public String getUriUserName() {
        String userName = "anonymous";
        try {
            URI uriObject = new URI(resource);
            String userInfo = uriObject.getUserInfo();
            if (userInfo!=null) {
                String[] components = userInfo.split(":");
                if (components.length>0) {
                    userName = components[0];
                }
            }
        } catch (URISyntaxException e) {
        }
        return userName;
    }


    /**
     * @return Returns the specified password of the resource, if any.
     */
    public String getUriPassword() {
        String password = "";
        try {
            URI uriObject = new URI(resource);
            String userInfo = uriObject.getUserInfo();
            if (userInfo!=null) {
                String[] components = userInfo.split(":");
                if (components.length>1) {
                    password = components[1];
                }
            }
        } catch (URISyntaxException e) {
        }
        return password;
    }

}
