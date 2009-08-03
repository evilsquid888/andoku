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

package com.googlecode.andoku.symbols;

public class StandardPuzzleSymbols {
	private static final String SYMBOL_SET = "123456789";

	private StandardPuzzleSymbols() {
	}

	public static PuzzleSymbols getStandardSymbols(int size) {
		if (size < 1 || size > SYMBOL_SET.length())
			throw new IllegalArgumentException();

		return new PuzzleSymbols(SYMBOL_SET.substring(0, size));
	}
}
