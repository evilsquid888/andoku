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

package com.googlecode.andoku.dlx;

public class Header extends Data {
	private String name;

	int size;

	public Header(String name) {
		super(null);

		this.name = name;
		size = 0;
	}

	public Header(String name, Object payload) {
		super(payload);

		this.name = name;
		size = 0;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return name + " (" + size + ")";
	}
}
