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

package com.googlecode.andoku.transfer;

public class StandardAreas {
	private static final int[][] STD_5 = { { 0, 0, 0, 1, 1 }, { 0, 0, 4, 1, 1 }, { 2, 4, 4, 4, 1 },
			{ 2, 2, 4, 3, 3 }, { 2, 2, 3, 3, 3 } };

	private static final int[][] STD_6 = { { 0, 0, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1 },
			{ 2, 2, 2, 3, 3, 3 }, { 2, 2, 2, 3, 3, 3 }, { 4, 4, 4, 5, 5, 5 }, { 4, 4, 4, 5, 5, 5 } };

	private static final int[][] STD_7 = { { 0, 0, 0, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1 },
			{ 2, 2, 2, 3, 4, 4, 4 }, { 2, 3, 3, 3, 3, 3, 4 }, { 2, 2, 2, 3, 4, 4, 4 },
			{ 5, 5, 5, 5, 6, 6, 6 }, { 5, 5, 5, 6, 6, 6, 6 } };

	private static final int[][] STD_8 = { { 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 1, 1, 1 },
			{ 2, 2, 2, 2, 3, 3, 3, 3 }, { 2, 2, 2, 2, 3, 3, 3, 3 }, { 4, 4, 4, 4, 5, 5, 5, 5 },
			{ 4, 4, 4, 4, 5, 5, 5, 5 }, { 6, 6, 6, 6, 7, 7, 7, 7 }, { 6, 6, 6, 6, 7, 7, 7, 7 } };

	private static final int[][] STD_9 = { { 0, 0, 0, 1, 1, 1, 2, 2, 2 },
			{ 0, 0, 0, 1, 1, 1, 2, 2, 2 }, { 0, 0, 0, 1, 1, 1, 2, 2, 2 },
			{ 3, 3, 3, 4, 4, 4, 5, 5, 5 }, { 3, 3, 3, 4, 4, 4, 5, 5, 5 },
			{ 3, 3, 3, 4, 4, 4, 5, 5, 5 }, { 6, 6, 6, 7, 7, 7, 8, 8, 8 },
			{ 6, 6, 6, 7, 7, 7, 8, 8, 8 }, { 6, 6, 6, 7, 7, 7, 8, 8, 8 } };

	private StandardAreas() {
	}

	public static int[][] getAreas(int size) {
		switch (size) {
			case 5:
				return STD_5;
			case 6:
				return STD_6;
			case 7:
				return STD_7;
			case 8:
				return STD_8;
			case 9:
				return STD_9;
			default:
				throw new IllegalArgumentException();
		}
	}
}
