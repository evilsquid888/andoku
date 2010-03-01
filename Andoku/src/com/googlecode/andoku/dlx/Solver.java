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

import java.util.Comparator;

public class Solver {
	private static final Comparator<Header> SIZE_COLUMN_COMPARATOR = new Comparator<Header>() {
		public int compare(Header h1, Header h2) {
			return h1.size - h2.size;
		}
	};

	private final Matrix<?> m;
	private final DlxListener listener;
	private final Comparator<Header> columnComparator;
	private final RowSorter rowSorter;

	public Solver(Matrix<?> m, DlxListener listener, Comparator<Header> columnComparator,
			RowSorter rowSorter) {
		if (m == null)
			throw new NullPointerException();
		if (listener == null)
			throw new NullPointerException();
		if (columnComparator == null)
			columnComparator = SIZE_COLUMN_COMPARATOR;

		this.m = m;
		this.listener = listener;
		this.columnComparator = columnComparator;
		this.rowSorter = rowSorter;
	}

	public void search() {
		search0();
	}

	private boolean search0() {
		Header root = m.getRoot();
		if (m.getColumnCount() == 0)
			return listener.solutionFound();

		boolean proceed = true;

		Header c = chooseColumn(root);
		m.cover(c);

		for (Data row : getRows(c)) {
			proceed = listener.select(row);

			if (!proceed)
				break;

			for (Data j = row.right; j != row; j = j.right)
				m.cover(j.column);

			proceed = search0();

			for (Data j = row.left; j != row; j = j.left)
				m.uncover(j.column);

			listener.deselect(row);

			if (!proceed)
				break;
		}

		m.uncover(c);

		return proceed;
	}

	private Header chooseColumn(Header root) {
		Header best = null;

		for (Header c = (Header) root.right; c != root; c = (Header) c.right)
			if (best == null || columnComparator.compare(c, best) < 0)
				best = c;

		return best;
	}

	private Data[] getRows(Header c) {
		Data[] rows = new Data[c.size];

		int idx = 0;
		for (Data row = c.down; row != c; row = row.down)
			rows[idx++] = row;

		if (rowSorter != null)
			rowSorter.sort(rows);

		return rows;
	}
}
