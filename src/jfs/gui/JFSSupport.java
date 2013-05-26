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

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import jfs.conf.JFSConst;
import jfs.conf.JFSText;

/**
 * This class provides some useful methods for creating buttons, menus, and so
 * on.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSSupport.java,v 1.14 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSSupport {

	/**
	 * Creates a radio button menu item.
	 * 
	 * @param group
	 *            Group to add button.
	 * @param nameKey
	 *            Name of the button.
	 * @param alias
	 *            Alias name.
	 * @param isActive
	 *            Determines if the button is currently activated.
	 * @param listener
	 *            The action listener.
	 * @return The generated element.
	 */
	static JRadioButtonMenuItem getRadioButtonMenuItem(ButtonGroup group,
			String nameKey, String alias, boolean isActive,
			ActionListener listener) {
		JRadioButtonMenuItem mi;
		mi = new JRadioButtonMenuItem(JFSText.getInstance().get(nameKey),
				isActive);
		mi.setActionCommand(alias);
		mi.addActionListener(listener);
		group.add(mi);
		return mi;
	}

	/**
	 * Creates a check box menu item.
	 * 
	 * @param nameKey
	 *            Name of the button.
	 * @param alias
	 *            Alias name.
	 * @param isActive
	 *            Determines if button is currently activated.
	 * @param listener
	 *            The action listener.
	 * @return The generated element.
	 */
	static JCheckBoxMenuItem getCheckBoxMenuItem(String nameKey, String alias,
			boolean isActive, ActionListener listener) {
		JCheckBoxMenuItem mi;
		mi = new JCheckBoxMenuItem(JFSText.getInstance().get(nameKey), isActive);
		mi.setActionCommand(alias);
		mi.addActionListener(listener);
		return mi;
	}

	/**
	 * Creates a menu item.
	 * 
	 * @param nameKey
	 *            Name of the item.
	 * @param alias
	 *            Alias name.
	 * @param listener
	 *            The action listener.
	 * @return The generated element.
	 */
	static JMenuItem getMenuItem(String nameKey, String alias,
			ActionListener listener) {
		JMenuItem mi = new JMenuItem(JFSText.getInstance().get(nameKey));
		mi.setActionCommand(alias);
		mi.addActionListener(listener);
		return mi;
	}

	/**
	 * Creates a menu item.
	 * 
	 * @param nameKey
	 *            Name of the item.
	 * @param alias
	 *            Alias name.
	 * @param listener
	 *            The action listener.
	 * @param iconKey
	 *            A valid icon key.
	 * @return The generated element.
	 */
	static JMenuItem getMenuItem(String nameKey, String alias,
			ActionListener listener, String iconKey) {
		JMenuItem mi = new JMenuItem(JFSText.getInstance().get(nameKey));
		mi.setActionCommand(alias);
		mi.addActionListener(listener);
		ImageIcon icon = new ImageIcon(JFSConst.getInstance().getIconUrl(
				iconKey));
		mi.setIcon(icon);
		return mi;
	}

	/**
	 * Creates a button containing text and a tool tip.
	 * 
	 * @param nameKey
	 *            Name of the button.
	 * @param alias
	 *            Alias name.
	 * @param listener
	 *            The action listener.
	 * @return The generated element.
	 */
	static JButton getButton(String nameKey, String alias,
			ActionListener listener) {
		JButton button = new JButton(JFSText.getInstance().get(nameKey));
		button.setActionCommand(alias);
		button.addActionListener(listener);
		return button;
	}

	/**
	 * Creates a button containing an icon and a tool tip.
	 * 
	 * @param iconKey
	 *            A valid icon key.
	 * @param alias
	 *            Alias name.
	 * @param listener
	 *            The action listener.
	 * @param toolTipKey
	 *            The tool tip text.
	 * @return The generated element.
	 */
	static JButton getButton(String iconKey, String alias,
			ActionListener listener, String toolTipKey) {
		JButton button = new JButton(new ImageIcon(JFSConst.getInstance()
				.getIconUrl(iconKey)));
		button.setActionCommand(alias);
		button.addActionListener(listener);
		button.setToolTipText(JFSText.getInstance().get(toolTipKey));
		button.setMargin(new Insets(1, 1, 1, 1));
		return button;
	}

	/**
	 * Creates a toggle containing an icon and a tool tip.
	 * 
	 * @param iconKey
	 *            A valid icon key.
	 * @param alias
	 *            Alias name.
	 * @param isActive
	 *            Determines if button is currently activated.
	 * @param listener
	 *            The action listener.
	 * @param toolTipKey
	 *            The tool tip text.
	 * @return The generated element.
	 */
	static JToggleButton getToggleButton(String iconKey, String alias,
			boolean isActive, ActionListener listener, String toolTipKey) {
		JToggleButton button = new JToggleButton(new ImageIcon(JFSConst
				.getInstance().getIconUrl(iconKey)));
		button.setActionCommand(alias);
		button.addActionListener(listener);
		button.setToolTipText(JFSText.getInstance().get(toolTipKey));
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setSelected(isActive);
		return button;
	}

	/**
	 * Centers a child component in relation to its father component.
	 * 
	 * @param father
	 *            The father component.
	 * @param child
	 *            The child component.
	 */
	static void center(Component father, Component child) {
		child.setLocation(father.getX()
				+ ((father.getWidth() - child.getWidth()) / 2), father.getY()
				+ ((father.getHeight() - child.getHeight()) / 2));
	}
}