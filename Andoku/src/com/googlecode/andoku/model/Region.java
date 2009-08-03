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

import java.util.Arrays;
import java.util.List;

public final class Region {
	public final int id;
	public final String type;
	public final int number;
	public final Position[] positions;
	public final ValueSet values;

	public Region(int id, String type, int number, List<Position> positions) {
		this.id = id;
		this.type = type;
		this.number = number;
		this.positions = positions.toArray(new Position[positions.size()]);
		values = new ValueSet();
	}

	public Region(int id, String type, int number, Position[] positions) {
		this.id = id;
		this.type = type;
		this.number = number;
		this.positions = positions;
		values = new ValueSet();
	}

	public String getName() {
		return type + " " + number;
	}

	@Override
	public String toString() {
		return "Region " + type + " " + number + ": " + Arrays.asList(positions);
	}
}
