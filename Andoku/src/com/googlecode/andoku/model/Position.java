/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
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

public final class Position implements Comparable<Position> {
	public final int row;
	public final int col;

	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public int hashCode() {
		return row * 9901 + col;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Position))
			return false;

		Position other = (Position) obj;
		return row == other.row && col == other.col;
	}

	public int compareTo(Position o) {
		int diff = row - o.row;
		if (diff != 0)
			return diff;
		return col - o.col;
	}

	@Override
	public String toString() {
		return row + "x" + col;
	}
}
