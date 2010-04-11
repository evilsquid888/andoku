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

package com.googlecode.andoku.im;

import android.os.Bundle;

import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;

public class CellThenValuesInputMethod implements InputMethod {
	private static final String APP_STATE_MARKED_CELL = "markedCell";

	private final InputMethodTarget target;

	public CellThenValuesInputMethod(InputMethodTarget target) {
		this.target = target;
	}

	public void onSaveInstanceState(Bundle outState) {
		Position markedCell = target.getMarkedCell();
		if (markedCell != null)
			outState.putIntArray(APP_STATE_MARKED_CELL, new int[] { markedCell.row, markedCell.col });
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		int[] markedCell = savedInstanceState.getIntArray(APP_STATE_MARKED_CELL);
		if (markedCell != null)
			setMark(new Position(markedCell[0], markedCell[1]));
	}

	public void reset() {
		target.highlightDigit(null);
		setMark(null);
	}

	public void onMoveMark(int dy, int dx) {
		final int size = target.getPuzzleSize();

		Position mark = target.getMarkedCell();
		int row = mark == null ? size / 2 : mark.row;
		int col = mark == null ? size / 2 : mark.col;

		row += dy;
		if (row == -1)
			row = size - 1;
		if (row == size)
			row = 0;

		col += dx;
		if (col == -1)
			col = size - 1;
		if (col == size)
			col = 0;

		setMark(new Position(row, col));
	}

	public void onKeypad(int digit) {
		Position mark = target.getMarkedCell();
		if (mark == null || target.isClue(mark))
			return;

		ValueSet values = target.getCellValues(mark);

		if (values.contains(digit)) {
			values.remove(digit);
			target.checkButton(digit, false);
		}
		else {
			values.add(digit);
			target.checkButton(digit, true);
		}

		target.setCellValues(mark, values);

		if (values.isEmpty())
			target.highlightDigit(null);
		else
			target.highlightDigit(digit);
	}

	public void onClear() {
		Position mark = target.getMarkedCell();
		if (mark == null || target.isClue(mark))
			return;

		final int size = target.getPuzzleSize();
		for (int digit = 0; digit < size; digit++)
			target.checkButton(digit, false);

		target.setCellValues(mark, new ValueSet());

		target.highlightDigit(null);
	}

	public void onInvert() {
		Position mark = target.getMarkedCell();
		if (mark == null || target.isClue(mark))
			return;

		ValueSet values = target.getCellValues(mark);
		final int size = target.getPuzzleSize();
		for (int digit = 0; digit < size; digit++) {
			if (values.contains(digit)) {
				values.remove(digit);
				target.checkButton(digit, false);
			}
			else {
				values.add(digit);
				target.checkButton(digit, true);
			}
		}

		target.setCellValues(mark, values);
	}

	public void onSweep() {
		setMark(null);
	}

	public void onTap(Position cell, boolean editable) {
		setMark(cell);
	}

	public void onValuesChanged() {
		checkButtons(target.getMarkedCell());
	}

	private void setMark(Position cell) {
		target.setMarkedCell(cell);

		if (cell == null) {
			target.highlightDigit(null);
		}
		else {
			ValueSet values = target.getCellValues(cell);
			if (values.size() == 1) {
				target.highlightDigit(values.nextValue(0));
			}
		}

		checkButtons(cell);
	}

	private void checkButtons(Position cell) {
		final int size = target.getPuzzleSize();

		if (cell == null) {
			for (int v = 0; v < size; v++) {
				target.checkButton(v, false);
			}
		}
		else {
			ValueSet values = target.getCellValues(cell);
			for (int v = 0; v < size; v++) {
				target.checkButton(v, values.contains(v));
			}
		}
	}
}
