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

package com.googlecode.andoku.commands;

import com.googlecode.andoku.history.Command;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.ValueSet;

public abstract class AbstractCommand implements Command<AndokuContext> {
	protected AbstractCommand() {
	}

	public boolean isEffective() {
		return true;
	}

	public Command<AndokuContext> mergeDown(Command<AndokuContext> last) {
		return null;
	}

	public int describeContents() {
		return 0;
	}

	protected ValueSet[][] saveValues(AndokuPuzzle puzzle) {
		final int size = puzzle.getSize();
		ValueSet[][] result = new ValueSet[size][size];

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				result[row][col] = puzzle.getValues(row, col);

		return result;
	}

	protected void restoreValues(AndokuPuzzle puzzle, ValueSet[][] originalValues) {
		final int size = puzzle.getSize();

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				puzzle.setValues(row, col, originalValues[row][col]);
	}
}
