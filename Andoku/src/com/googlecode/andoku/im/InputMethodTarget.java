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

import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;

public interface InputMethodTarget {
	int getPuzzleSize();

	Position getMarkedCell();
	void setMarkedCell(Position cell);

	boolean isClue(Position cell);

	ValueSet getCellValues(Position cell);
	void setCellValues(Position cell, ValueSet values);

	int getNumberOfDigitButtons();
	void checkButton(int digit, boolean checked);
}
