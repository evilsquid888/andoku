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

import com.googlecode.andoku.model.Difficulty;

public class PuzzleInfo {
	public static final String AREAS_NONE = "";

	public static final String EXTRA_HYPER = "H";
	public static final String EXTRA_X = "X";
	public static final String EXTRA_NONE = "";

	public static final String SOLUTION_NONE = "";

	private final String name;
	private final Difficulty difficulty;
	private final int size;
	private final String clues; //        "...6.12........3......"
	private final String areas; //        "11122223311122222341.."|""
	private final String extraRegions; // "H"|"X"|""
	private final String solution; //     "35869127496158734217.."

	public static final class Builder {
		private String name = "";
		private Difficulty difficulty = Difficulty.UNKNOWN;
		private final int size;
		private final String clues;
		private String areas = AREAS_NONE;
		private String extraRegions = EXTRA_NONE;
		private String solution = SOLUTION_NONE;

		public Builder(String clues) {
			int size = (int) Math.sqrt(clues.length());
			if (clues.length() != size * size)
				throw new IllegalArgumentException();

			this.size = size;
			this.clues = clues;
		}

		public Builder setName(String name) {
			if (name == null)
				throw new IllegalArgumentException();

			this.name = name;
			return this;
		}

		public Builder setDifficulty(Difficulty difficulty) {
			if (difficulty == null)
				throw new IllegalArgumentException();

			this.difficulty = difficulty;
			return this;
		}

		public Builder setAreas(String areas) {
			if (!areas.equals(AREAS_NONE) && areas.length() != size * size)
				throw new IllegalArgumentException();

			this.areas = areas;
			return this;
		}

		public Builder setExtraRegions(String extraRegions) {
			if (!extraRegions.equals(EXTRA_HYPER) && !extraRegions.equals(EXTRA_X)
					&& !extraRegions.equals(EXTRA_NONE))
				throw new IllegalArgumentException();

			this.extraRegions = extraRegions;
			return this;
		}

		public Builder setSolution(String solution) {
			if (!solution.equals(SOLUTION_NONE) && solution.length() != size * size)
				throw new IllegalArgumentException();

			this.solution = solution;
			return this;
		}

		public PuzzleInfo build() {
			return new PuzzleInfo(this);
		}
	}

	private PuzzleInfo(Builder builder) {
		name = builder.name;
		difficulty = builder.difficulty;
		size = builder.size;
		clues = builder.clues;
		areas = builder.areas;
		extraRegions = builder.extraRegions;
		solution = builder.solution;
	}

	public String getName() {
		return name;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public int getSize() {
		return size;
	}

	public String getClues() {
		return clues;
	}

	public String getAreas() {
		return areas;
	}

	public String getExtraRegions() {
		return extraRegions;
	}

	public String getSolution() {
		return solution;
	}

	@Override
	public String toString() {
		return name + "|" + clues + "|" + areas + "|" + extraRegions + "|" + solution + "|"
				+ difficulty;
	}
}
