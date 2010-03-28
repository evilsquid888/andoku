/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
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

package com.googlecode.andoku.solver;

import java.util.Comparator;
import java.util.Random;

import com.googlecode.andoku.dlx.Data;
import com.googlecode.andoku.dlx.Header;
import com.googlecode.andoku.dlx.DlxListener;
import com.googlecode.andoku.dlx.Matrix;
import com.googlecode.andoku.dlx.RowSorter;
import com.googlecode.andoku.dlx.Solver;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Region;

/**
 * Puzzle solver that uses the Dancing Links (DLX) algorithm.
 */
public class DlxPuzzleSolver implements PuzzleSolver {
	protected final Random random;
	private final long maxUpdates;

	protected int size;
	protected Puzzle puzzle;
	private PuzzleReporter reporter;

	private long updates;

	public DlxPuzzleSolver() {
		this(null, 0);
	}

	public DlxPuzzleSolver(Random random) {
		this(random, 0);
	}

	public DlxPuzzleSolver(Random random, long maxUpdates) {
		this.random = random;
		this.maxUpdates = maxUpdates;
	}

	public long getNumberOfUpdates() {
		return updates;
	}

	public void solve(Puzzle puzzle, PuzzleReporter reporter) {
		this.size = puzzle.getSize();
		this.puzzle = new Puzzle(puzzle);
		this.reporter = reporter;

		solve();
	}

	protected void solve() {
		Matrix<RCV> m = createMatrix();
		eliminateGivenClues(m);

		updates = 0;
		Strategy strategy = new Strategy();
		new Solver(m, strategy, strategy, strategy).search();
	}

	private Matrix<RCV> createMatrix() {
		Matrix<RCV> m = new Matrix<RCV>();

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				String name = row + "x" + col;
				m.addColumn(new Header(name));
			}

		final int regionOffset = m.getColumnCount();
		for (Region region : puzzle.getRegions())
			for (int v = 0; v < size; v++) {
				String name = region.getName() + " value " + v;
				m.addColumn(new Header(name, region));
			}

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				Region[] regions = puzzle.getRegionsAt(row, col);

				for (int v = 0; v < size; v++) {
					boolean[] values = new boolean[m.getColumnCount()];

					values[row * size + col] = true;

					for (Region region : regions)
						values[regionOffset + region.id * size + v] = true;

					m.addRow(new RCV(row, col, v), values);
				}
			}

		return m;
	}

	private void eliminateGivenClues(Matrix<RCV> m) {
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				int value = puzzle.getValue(row, col);
				if (value != Puzzle.UNDEFINED) {
					Data data = m.getRow(new RCV(row, col, value));
					m.eliminateRow(data);
				}
			}
	}

	private final class Strategy implements DlxListener, Comparator<Header>, RowSorter {
		public Strategy() {
		}

		public boolean select(Data row) {
			RCV r = (RCV) row.getPayload();
			puzzle.set(r.row, r.col, r.value);
			return maxUpdates == 0 || ++updates < maxUpdates;
		}

		public void deselect(Data row) {
			RCV r = (RCV) row.getPayload();
			puzzle.clear(r.row, r.col);
		}

		public boolean solutionFound() {
			return reporter.report(puzzle);
		}

		public int compare(Header c1, Header c2) {
			final int diff = c1.getSize() - c2.getSize();
			if (diff != 0 || random == null)
				return diff;

			return random.nextBoolean() ? -1 : 1;
		}

		public void sort(Data[] rows) {
			if (random == null)
				return;

			for (int i = rows.length; i > 1; i--) {
				int x = i - 1;
				int y = random.nextInt(i);

				Data tmp = rows[x];
				rows[x] = rows[y];
				rows[y] = tmp;
			}
		}
	}

	/**
	 * A triple of Row, Column and Value.
	 */
	private static final class RCV {
		private final int row;
		private final int col;
		private final int value;

		public RCV(int row, int col, int value) {
			this.row = row;
			this.col = col;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return 9901 * row + 1009 * col + value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof RCV))
				return false;

			RCV other = (RCV) obj;
			return row == other.row && col == other.col && value == other.value;
		}

		@Override
		public String toString() {
			return value + "@" + row + "x" + col;
		}
	}
}
