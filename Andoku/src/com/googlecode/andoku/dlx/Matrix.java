/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009, 2010  Markus Wiederkehr
 *
 * This file is part of Andoku.
 *
 * Andoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Andoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku.dlx;

import java.util.HashMap;
import java.util.Map;

public class Matrix<P> {
	private final Header root;
	private final Map<P, Data> rowByPayload = new HashMap<P, Data>();
	private int columnCount;

	public Matrix() {
		this.root = new Header("_ROOT_");
	}

	public void addColumn(Header column) {
		appendToRow(this.root, column);

		this.columnCount++;
	}

	public Data addRow(P payload, boolean[] values) {
		Data row = null;

		Header column = this.root;
		for (int i = 0; i < values.length; i++) {
			column = (Header) column.right;
			if (column == this.root)
				throw new IllegalArgumentException("Too many columns");

			if (!values[i])
				continue;

			Data data = new Data(payload);
			appendToColumn(column, data);

			if (row != null)
				appendToRow(row, data);
			else
				row = data;
		}

		if (column.right != this.root)
			throw new IllegalArgumentException("Not enough columns");

		if (payload != null)
			this.rowByPayload.put(payload, row);

		return row;
	}

	public Data getRow(P payload) {
		return this.rowByPayload.get(payload);
	}

	public void cover(Header column) {
		column.left.right = column.right;
		column.right.left = column.left;

		for (Data i = column.down; i != column; i = i.down)
			for (Data j = i.right; j != i; j = j.right) {
				j.up.down = j.down;
				j.down.up = j.up;
				j.column.size--;
			}

		this.columnCount--;
	}

	public void uncover(Header column) {
		for (Data i = column.up; i != column; i = i.up)
			for (Data j = i.left; j != i; j = j.left) {
				j.column.size++;
				j.up.down = j;
				j.down.up = j;
			}

		column.left.right = column;
		column.right.left = column;

		this.columnCount++;
	}

	public void eliminateRow(Data row) {
		assert !(row instanceof Header);

		cover(row.column);

		for (Data j = row.right; j != row; j = j.right)
			cover(j.column);
	}

	public Header getRoot() {
		return this.root;
	}

	public int getColumnCount() {
		return this.columnCount;
	}

	private static void appendToRow(Data row, Data data) {
		Data last = row.left;

		last.right = data;
		data.left = last;
		row.left = data;
		data.right = row;
	}

	private static void appendToColumn(Header column, Data data) {
		Data last = column.up;

		last.down = data;
		data.up = last;
		column.up = data;
		data.down = column;

		data.column = column;
		column.size++;
	}
}
