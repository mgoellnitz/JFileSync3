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

/**
 * Sorts a list of objects using the heap sort algorithm. The class is used to
 * sort a list of file names. ATTENTION: We have to compare the strings of the
 * file names instead of using the compareTo method of the file objects itself.
 * This is necessary because the predefined order differs between the File
 * object und the String object. The new JFSFile object compares the Strings of
 * the file names and implements the Comparable Interface.
 * 
 * @author Jens Heidrich
 * @version $Id: HeapSort.java,v 1.13 2007/02/26 18:49:09 heidrich Exp $
 */
public class HeapSort<E extends Comparable<E>> {

	/** Array of comparables. */
	private E[] c;

	/**
	 * Switches two elements of the array.
	 * 
	 * @param a
	 *            Index of the first object.
	 * @param b
	 *            Index of the second object.
	 */
	private void switchElements(int a, int b) {
		E temp = c[a];
		c[a] = c[b];
		c[b] = temp;
	}

	/**
	 * Heapfies the array within a certain region.
	 * 
	 * @param left
	 *            The left side of the region.
	 * @param right
	 *            The right side of the region.
	 */
	private void heapify(int left, int right) {
		int k = 2 * left;

		if (k > right)
			return;

		if ((k + 1) > right) {
			if (c[k - 1].compareTo(c[left - 1]) > 0)
				switchElements(left - 1, k - 1);

			return;
		}

		if (c[k - 1].compareTo(c[k]) < 0)
			k++;

		if (c[left - 1].compareTo(c[k - 1]) < 0) {
			switchElements(left - 1, k - 1);
			heapify(k, right);
		}
	}

	/**
	 * Sorts an array of files.
	 * 
	 * @param array
	 *            Array of comparables.
	 */
	public void sort(E[] array) {
		if (array == null)
			return;

		c = array;

		int left = (c.length / 2) + 1;
		int right = c.length;

		while (left > 1) {
			left--;
			heapify(left, right);
		}

		while (right > 1) {
			switchElements(right - 1, left - 1);
			right--;
			heapify(left, right);
		}
	}
}