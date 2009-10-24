/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku.source;

import com.googlecode.andoku.db.PuzzleId;
import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Solution;

public class PuzzleHolder {
	private final PuzzleSource source;
	private final int number;

	private final Difficulty difficulty;
	private final Puzzle puzzle;
	private final Solution solution;

	public PuzzleHolder(PuzzleSource source, int number, Difficulty difficulty, Puzzle puzzle,
			Solution solution) throws PuzzleIOException {
		this.source = source;
		this.number = number;
		this.difficulty = difficulty;
		this.puzzle = puzzle;
		this.solution = solution;
	}

	public PuzzleSource getSource() {
		return source;
	}

	public int getNumber() {
		return number;
	}

	public PuzzleId getPuzzleId() {
		return new PuzzleId(source.getSourceId(), number);
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	public Solution getSolution() {
		return solution;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}
}
