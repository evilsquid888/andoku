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

public class AutomaticInputMethod implements InputMethod {
	private static final String APP_STATE_ACTIVE_INPUT_METHOD = "automaticInputMethod";
	private static final String IM_UNDECIDED = "undecided";
	private static final String IM_CELL_THEN_VALUES = "cellThenValues";
	private static final String IM_VALUES_THEN_CELL = "valuesThenCell";

	private final InputMethodTarget target;
	private final CellThenValuesInputMethod cellThenValues;
	private final ValuesThenCellInputMethod valuesThenCell;
	private InputMethod activeInputMethod = null;
	private Position lastMarkedCell;

	public AutomaticInputMethod(InputMethodTarget target) {
		this.target = target;
		cellThenValues = new CellThenValuesInputMethod(target);
		valuesThenCell = new ValuesThenCellInputMethod(target);
	}

	public void onSaveInstanceState(Bundle outState) {
		if (activeInputMethod == null) {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_UNDECIDED);
		}
		else if (activeInputMethod == cellThenValues) {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_CELL_THEN_VALUES);
			cellThenValues.onSaveInstanceState(outState);
		}
		else {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_VALUES_THEN_CELL);
			valuesThenCell.onSaveInstanceState(outState);
		}
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		String inputMethod = savedInstanceState.getString(APP_STATE_ACTIVE_INPUT_METHOD);
		if (inputMethod == null || inputMethod.equals(IM_UNDECIDED)) {
			setUndecided();
		}
		else if (inputMethod.equals(IM_CELL_THEN_VALUES)) {
			activeInputMethod = cellThenValues;
			cellThenValues.onRestoreInstanceState(savedInstanceState);
		}
		else {
			activeInputMethod = valuesThenCell;
			valuesThenCell.onRestoreInstanceState(savedInstanceState);
		}
	}

	public void reset() {
		cellThenValues.reset();
		valuesThenCell.reset();
		setUndecided();
	}

	public void onMoveMark(int dy, int dx) {
		ifUndecidedUseCellThenValues();

		activeInputMethod.onMoveMark(dy, dx);
	}

	public void onKeypad(int digit) {
		ifUndecidedUseValuesThenCells();

		activeInputMethod.onKeypad(digit);

		if (activeInputMethod == valuesThenCell && valuesThenCell.isValuesEmpty()) {
			setUndecided();
		}
	}

	public void onClear() {
		if (activeInputMethod != null)
			activeInputMethod.onClear();

		if (activeInputMethod == valuesThenCell)
			setUndecided();
	}

	public void onInvert() {
		if (target.getMarkedCell() == null)
			ifUndecidedUseValuesThenCells();
		else
			ifUndecidedUseCellThenValues();

		activeInputMethod.onInvert();

		if (activeInputMethod == valuesThenCell && valuesThenCell.isValuesEmpty()) {
			setUndecided();
		}
	}

	public void onTap(Position cell, boolean editable) {
		if (cell == null && target.getMarkedCell() != null) {
			lastMarkedCell = target.getMarkedCell();
		}

		if (activeInputMethod == cellThenValues && cell != null && cell.equals(lastMarkedCell)) {
			setUndecided();
		}
		else {
			ifUndecidedUseCellThenValues();

			activeInputMethod.onTap(cell, editable);
		}
	}

	private void setUndecided() {
		activeInputMethod = null;
		target.setMarkedCell(null);
		target.highlightDigit(null);
		lastMarkedCell = null;
	}

	private void ifUndecidedUseValuesThenCells() {
		if (activeInputMethod == null) {
			activeInputMethod = valuesThenCell;
		}
	}

	private void ifUndecidedUseCellThenValues() {
		if (activeInputMethod == null) {
			activeInputMethod = cellThenValues;
		}
	}
}
