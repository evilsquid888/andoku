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

package com.googlecode.andoku.model;

public enum PuzzleType {
	// do not change order of constants - used in db
	STANDARD,
	STANDARD_X,
	STANDARD_HYPER,
	SQUIGGLY,
	SQUIGGLY_X,
	SQUIGGLY_HYPER,
	STANDARD_PERCENT,
	SQUIGGLY_PERCENT,
	STANDARD_COLOR,
	SQUIGGLY_COLOR;

	public static PuzzleType forOrdinal(int ordinal) {
		return PuzzleType.values()[ordinal];
	}
}
