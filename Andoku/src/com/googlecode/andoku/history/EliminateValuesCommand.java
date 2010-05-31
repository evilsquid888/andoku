/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2010  Markus Wiederkehr
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

package com.googlecode.andoku.history;

import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.ValueSet;

public class EliminateValuesCommand extends AbstractCommand {
	private static final long TIME_PENALTY_PER_ELIMINATED_VALUE = 1000;

	private final AndokuPuzzle puzzle;
	private final TickTimer timer;
	private final ValueSet[][] originalValues;

	public EliminateValuesCommand(AndokuPuzzle puzzle, TickTimer timer) {
		this.puzzle = puzzle;
		this.timer = timer;
		this.originalValues = saveValues(puzzle);
	}

	public void execute() {
		int numberValuesEliminated = puzzle.eliminateValues();

		long penalty = TIME_PENALTY_PER_ELIMINATED_VALUE * numberValuesEliminated;
		timer.setTime(timer.getTime() + penalty);
	}

	public void undo() {
		restoreValues(puzzle, originalValues);
	}

	@Override
	public void redo() {
		puzzle.eliminateValues();
	}
}
