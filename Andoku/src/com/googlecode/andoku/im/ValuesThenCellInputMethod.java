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

package com.googlecode.andoku.im;

import android.os.Bundle;

import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;

public class ValuesThenCellInputMethod implements InputMethod {
	private static final String APP_STATE_KEYPAD_VALUES = "keypadValues";

	private final InputMethodTarget target;

	private final ValueSet values = new ValueSet();

	public ValuesThenCellInputMethod(InputMethodTarget target) {
		this.target = target;
	}

	public boolean isValuesEmpty() {
		return values.isEmpty();
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(APP_STATE_KEYPAD_VALUES, values.toInt());
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		int v = savedInstanceState.getInt(APP_STATE_KEYPAD_VALUES, 0);
		setValues(v);
	}

	public void reset() {
		target.setMarkedCell(null);
		target.highlightDigit(null);
		setValues(0);
	}

	public void onMoveMark(int dy, int dx) {
	}

	public void onKeypad(int digit) {
		if (values.contains(digit)) {
			values.remove(digit);
			target.checkButton(digit, false);
		}
		else {
			values.add(digit);
			target.checkButton(digit, true);
		}

		if (values.isEmpty())
			target.highlightDigit(null);
		else
			target.highlightDigit(digit);
	}

	public void onClear() {
		setValues(0);

		target.highlightDigit(null);
	}

	public void onInvert() {
		final int nButtons = target.getNumberOfDigitButtons();
		for (int digit = 0; digit < nButtons; digit++) {
			if (values.contains(digit))
				values.remove(digit);
			else
				values.add(digit);
		}

		checkButtons();
	}

	public void onTap(Position cell, boolean editable) {
		if (!editable)
			return;

		ValueSet cellValues = target.getCellValues(cell);
		if (cellValues.containsAny(values)) {
			cellValues.removeAll(values);
			target.setCellValues(cell, cellValues);
		}
		else {
			cellValues.addAll(values);
			target.setCellValues(cell, cellValues);
		}
	}

	private void setValues(int v) {
		values.setFromInt(v);

		checkButtons();
	}

	private void checkButtons() {
		final int nButtons = target.getNumberOfDigitButtons();
		for (int digit = 0; digit < nButtons; digit++)
			target.checkButton(digit, values.contains(digit));
	}
}
