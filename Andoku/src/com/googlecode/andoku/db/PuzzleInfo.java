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

package com.googlecode.andoku.db;

import java.util.Locale;

import com.googlecode.andoku.model.Difficulty;

public class PuzzleInfo {
	public static final String AREAS_NONE = "";

	public static final String EXTRA_X = "X";
	public static final String EXTRA_HYPER = "H";
	public static final String EXTRA_PERCENT = "P";
	public static final String EXTRA_COLOR = "C";
	public static final String EXTRA_NONE = "";

	private final String name;
	private final Difficulty difficulty;
	private final int size;
	private final String clues; //        "...6.12........3......"
	private final String areas; //        "11122223311122222341.."|""
	private final String extraRegions; // "X"|"H"|"P"|"C"|""

	public static final class Builder {
		private String name = "";
		private Difficulty difficulty = Difficulty.UNKNOWN;
		private final int size;
		private final String clues;
		private String areas = AREAS_NONE;
		private String extraRegions = EXTRA_NONE;

		public Builder(String clues) {
			int size = (int) Math.sqrt(clues.length());
			if (clues.length() != size * size)
				throw new IllegalArgumentException();

			this.size = size;
			this.clues = clues;
		}

		public Builder setName(String name) {
			if (!isValidName(name))
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
			if (!isValidAreas(areas))
				throw new IllegalArgumentException();

			this.areas = areas;
			return this;
		}

		public Builder setExtraRegions(String extraRegions) {
			if (!isValidExtraRegions(extraRegions))
				throw new IllegalArgumentException();

			this.extraRegions = extraRegions.toUpperCase(Locale.US);
			return this;
		}

		public PuzzleInfo build() {
			return new PuzzleInfo(this);
		}

		public static boolean isValidClues(String clues) {
			final int length = clues.length();

			final int size = (int) Math.sqrt(length);
			if (length != size * size)
				return false;

			if (size != 9)
				return false;

			for (int i = 0; i < length; i++) {
				char c = clues.charAt(i);
				if (c != '.' && (c < '1' || c > '9'))
					return false;
			}

			return true;
		}

		public boolean isValidName(String name) {
			return name != null;
		}

		public boolean isValidDifficulty(int difficulty) {
			return difficulty >= 0 && difficulty < Difficulty.values().length;
		}

		public boolean isValidAreas(String areas) {
			final int length = areas.length();

			if (length == 0)
				return true;

			if (length != size * size)
				return false;

			for (int i = 0; i < length; i++) {
				char c = areas.charAt(i);
				if (c < '1' || c > '9')
					return false;
			}

			return true;
		}

		public boolean isValidExtraRegions(String extraRegions) {
			return extraRegions.equalsIgnoreCase(EXTRA_X)
					|| extraRegions.equalsIgnoreCase(EXTRA_HYPER)
					|| extraRegions.equalsIgnoreCase(EXTRA_PERCENT)
					|| extraRegions.equalsIgnoreCase(EXTRA_COLOR)
					|| extraRegions.equalsIgnoreCase(EXTRA_NONE);
		}
	}

	private PuzzleInfo(Builder builder) {
		name = builder.name;
		difficulty = builder.difficulty;
		size = builder.size;
		clues = builder.clues;
		areas = builder.areas;
		extraRegions = builder.extraRegions;
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

	@Override
	public String toString() {
		return name + "|" + clues + "|" + areas + "|" + extraRegions + "|" + difficulty;
	}
}
