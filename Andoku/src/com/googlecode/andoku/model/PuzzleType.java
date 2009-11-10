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

package com.googlecode.andoku.model;

public enum PuzzleType {
	STANDARD(false, false, false),
	STANDARD_X(false, false, true),
	STANDARD_HYPER(false, true, false),
	SQUIGGLY(true, false, false),
	SQUIGGLY_X(true, false, true),
	SQUIGGLY_H(true, true, false);

	private final boolean squiggly;
	private final boolean hyper;
	private final boolean x;

	PuzzleType(boolean squiggly, boolean hyper, boolean x) {
		this.squiggly = squiggly;
		this.hyper = hyper;
		this.x = x;
	}

	public boolean isSquiggly() {
		return squiggly;
	}

	public boolean isHyper() {
		return hyper;
	}

	public boolean isX() {
		return x;
	}

	public static PuzzleType forOrdinal(int ordinal) {
		return PuzzleType.values()[ordinal];
	}
}
