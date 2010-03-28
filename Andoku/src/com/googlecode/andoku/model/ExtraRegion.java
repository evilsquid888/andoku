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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ExtraRegion {
	public final Position[] positions;

	public ExtraRegion(List<Position> positions) {
		this.positions = positions.toArray(new Position[positions.size()]);
	}

	public ExtraRegion(Position[] positions) {
		this.positions = positions;
	}

	@Override
	public int hashCode() {
		// not very fast but probably only needed by PuzzleEncoder/Decoder
		return new HashSet<Position>(Arrays.asList(positions)).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ExtraRegion))
			return false;

		// not very fast but probably only needed by PuzzleEncoder
		ExtraRegion other = (ExtraRegion) obj;
		return new HashSet<Position>(Arrays.asList(positions)).equals(new HashSet<Position>(Arrays
				.asList(other.positions)));
	}
}
