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

package com.googlecode.andoku.db;

public class PuzzleId {
	public final String puzzleSourceId;
	public final int number;

	public PuzzleId(String puzzleSourceId, int number) {
		this.puzzleSourceId = puzzleSourceId;
		this.number = number;
	}

	@Override
	public int hashCode() {
		return puzzleSourceId.hashCode() ^ number;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof PuzzleId))
			return false;

		PuzzleId other = (PuzzleId) obj;
		return puzzleSourceId.equals(other.puzzleSourceId) && number == other.number;
	}

	@Override
	public String toString() {
		return puzzleSourceId + ':' + number;
	}
}
