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

package com.googlecode.andoku.solver;

import com.googlecode.andoku.model.Puzzle;

/**
 * Gets notified of puzzle solutions and decides if the search should continue.
 */
public interface PuzzleReporter {
	/**
	 * Report a solution.
	 * 
	 * @param solution a solution to the original puzzle. The reporter implementation has to copy
	 *           this solution if it wants to keep it.
	 * @return <code>true</code> if the solver should continue to look for solutions,
	 *         <code>false</code> otherwise.
	 */
	boolean report(Puzzle solution);
}
