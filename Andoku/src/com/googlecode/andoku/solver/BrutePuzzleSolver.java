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

package com.googlecode.andoku.solver;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.Puzzle;

/**
 * Brute force puzzle solver.
 */
public class BrutePuzzleSolver implements PuzzleSolver {
	private final Random random;

	protected int size;
	protected Puzzle puzzle;
	private PuzzleReporter reporter;

	private Set<Position> undefinedPositions;

	public BrutePuzzleSolver() {
		this(null);
	}

	public BrutePuzzleSolver(Random random) {
		this.random = random;
	}

	public void solve(Puzzle puzzle, PuzzleReporter reporter) {
		this.size = puzzle.getSize();
		this.puzzle = new Puzzle(puzzle);
		this.reporter = reporter;

		solve();
	}

	private void solve() {
		undefinedPositions = findUndefinedPositions();
		if (undefinedPositions.isEmpty()) {
			reportSolution(); // already solved
		}
		else {
			Position startPosition = removeNextPosition();

			solve0(startPosition);
		}
	}

	private Set<Position> findUndefinedPositions() {
		Set<Position> undefined = new HashSet<Position>();
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (puzzle.getValue(row, col) == Puzzle.UNDEFINED) {
					undefined.add(new Position(row, col));
				}
			}
		}
		return undefined;
	}

	/**
	 * Recursively solves the puzzle.
	 * 
	 * @return <code>true</code> if the calling recursion level should continue to find solutions,
	 *         <code>false</code> otherwise.
	 */
	private boolean solve0(Position position) {
		final int[] values = new int[puzzle.getSize()];
		final int count = puzzle.getPossibleValues(position.row, position.col).getValues(values);

		if (random != null)
			shuffle(values, count);

		for (int i = 0; i < count; i++) {
			int value = values[i];

			puzzle.set(position.row, position.col, value);

			if (puzzle.isSolved()) {
				if (!reportSolution())
					return false;
			}
			else {
				Position nextPosition = removeNextPosition();
				assert nextPosition != null; // because not solved

				if (!solve0(nextPosition))
					return false;

				undefinedPositions.add(nextPosition);
			}

			puzzle.clear(position.row, position.col);
		}

		return true;
	}

	private void shuffle(int[] values, int count) {
		for (int i = count; i > 1; i--) {
			int idx1 = i - 1;
			int idx2 = random.nextInt(i);
			int tmp = values[idx1];
			values[idx1] = values[idx2];
			values[idx2] = tmp;
		}
	}

	private boolean reportSolution() {
		return reporter.report(puzzle);
	}

	private Position removeNextPosition() {
		Position nextPosition = findNextPosition();

		undefinedPositions.remove(nextPosition);

		return nextPosition;
	}

	/**
	 * Returns the next position with an undefined value that should be used to find a solution.
	 * 
	 * This implementation assumes that only one or very few solutions to the puzzle exist and
	 * returns the position that has the lowest number of possible values left.
	 */
	private Position findNextPosition() {
		int minPossible = Integer.MAX_VALUE;
		Position minPosition = null;

		for (Position position : undefinedPositions) {
			int numPossible = puzzle.getPossibleValues(position.row, position.col).size();
			if (numPossible <= 1)
				return position;

			if (numPossible < minPossible) {
				minPossible = numPossible;
				minPosition = position;
			}
		}

		return minPossible == Integer.MAX_VALUE ? null : minPosition;
	}
}
