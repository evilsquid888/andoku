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

public class Solution {
	private int[][] solution;

	public Solution(int[][] solution) {
		this.solution = solution;
	}

	public Solution(Puzzle puzzle) {
		if (!puzzle.isSolved())
			throw new IllegalArgumentException();

		final int size = puzzle.getSize();
		this.solution = new int[size][size];

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				int value = puzzle.getValue(row, col);
				solution[row][col] = value;
			}
		}
	}

	public int getValue(int row, int col) {
		return solution[row][col];
	}
}
