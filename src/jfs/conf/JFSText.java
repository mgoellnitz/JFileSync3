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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Translates the text of JFileSync windows. The class implements the singleton
 * design pattern.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSText.java,v 1.11 2007/02/26 18:49:11 heidrich Exp $
 */
public class JFSText {

	/** The system's line separator. */
	public final static String LINE_SEPARATOR = System.getProperty(
			"line.separator", "\n");

	/** Stores the only instance of the class. */
	private static JFSText instance = null;

	/** Stores the system locale. */
	private Locale locale = null;

	/** Stores the resource bundle. */
	private ResourceBundle bundle = null;

	/**
	 * Sets some default values for the object.
	 */
	private JFSText() {
		locale = Locale.getDefault();
		bundle = ResourceBundle.getBundle(JFSConst.TRANSLATION_CLASS);
	}

	/**
	 * Returns the reference of the only JFSText object.
	 * 
	 * @return The only JFSText instance.
	 */
	public final static JFSText getInstance() {
		if (instance == null)
			instance = new JFSText();

		return instance;
	}

	/**
	 * Returns the translated string for a certain key.
	 * 
	 * @param key
	 *            The key.
	 * @return Translated string.
	 */
	public final String get(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Gets the current locale.
	 * 
	 * @return The current locale.
	 */
	public final Locale getLocale() {
		return locale;
	}
}