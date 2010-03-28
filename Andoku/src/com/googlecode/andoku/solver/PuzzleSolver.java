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

import com.googlecode.andoku.model.Puzzle;

/**
 * Can solve sudoku puzzles and notify a puzzle reporter of its solution(s).
 */
public interface PuzzleSolver {
	/**
	 * Solves the specified puzzle and passes solutions to the specified puzzle reporter.
	 * 
	 * @param puzzle puzzle to solve.
	 * @param reporter will be notified of solutions and decides if more solutions should be searched
	 *           for.
	 */
	void solve(Puzzle puzzle, PuzzleReporter reporter);
}
