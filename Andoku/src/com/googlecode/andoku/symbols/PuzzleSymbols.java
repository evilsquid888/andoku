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

import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.ValueSet;

public final class PuzzleSymbols {
	public static char UNDEFINED_SYMBOL = '?';
	public static int UNDEFINED_VALUE = Puzzle.UNDEFINED;

	private final String symbolSet;
	private final int size;

	public PuzzleSymbols(String symbolSet) {
		this.symbolSet = symbolSet;
		this.size = symbolSet.length();
	}

	public String getSymbols(ValueSet values) {
		StringBuilder sb = new StringBuilder();

		for (int value = values.nextValue(0); value != -1; value = values.nextValue(value + 1)) {
			char symbol = getSymbol(value);
			if (symbol == UNDEFINED_SYMBOL)
				throw new IllegalArgumentException();

			sb.append(symbol);
		}

		return sb.toString();
	}

	public ValueSet getValues(String symbols) {
		ValueSet values = new ValueSet();

		final int length = symbols.length();
		for (int idx = 0; idx < length; idx++) {
			char symbol = symbols.charAt(idx);
			int value = getValue(symbol);
			if (value == UNDEFINED_VALUE)
				throw new IllegalArgumentException();

			values.add(value);
		}

		return values;
	}

	public char getSymbol(int value) {
		if (value < 0 || value >= size)
			return UNDEFINED_SYMBOL;

		return symbolSet.charAt(value);
	}

	public int getValue(char symbol) {
		int value = symbolSet.indexOf(symbol);
		if (value < 0 || value >= size)
			return UNDEFINED_VALUE;

		return value;
	}

	public String getSymbolSet() {
		return symbolSet;
	}
}
