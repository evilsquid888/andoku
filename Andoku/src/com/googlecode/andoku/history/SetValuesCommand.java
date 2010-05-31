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

import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;

public class SetValuesCommand extends AbstractCommand {
	private final AndokuPuzzle puzzle;
	private final Position cell;
	private final ValueSet values;
	private final ValueSet originalValues;

	private SetValuesCommand(AndokuPuzzle puzzle, Position cell, ValueSet values,
			ValueSet originalValues) {
		this.puzzle = puzzle;
		this.cell = cell;
		this.values = values;
		this.originalValues = originalValues;
	}

	public SetValuesCommand(AndokuPuzzle puzzle, Position cell, ValueSet values) {
		this.puzzle = puzzle;
		this.cell = cell;
		this.values = values;
		originalValues = puzzle.getValues(cell.row, cell.col);
	}

	public void execute() {
		puzzle.setValues(cell.row, cell.col, values);
	}

	public void undo() {
		puzzle.setValues(cell.row, cell.col, originalValues);
	}

	@Override
	public Command mergeWith(Command last) {
		if (!(last instanceof SetValuesCommand))
			return null;

		SetValuesCommand other = (SetValuesCommand) last;
		if (puzzle != other.puzzle || !cell.equals(other.cell))
			return null;

		return new SetValuesCommand(puzzle, cell, values, other.originalValues);
	}

	@Override
	public boolean isEffective() {
		return !values.equals(originalValues);
	}
}
