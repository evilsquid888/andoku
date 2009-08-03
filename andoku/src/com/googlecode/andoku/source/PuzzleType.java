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

package com.googlecode.andoku.source;

import com.googlecode.andoku.R;

public enum PuzzleType {
	STANDARD {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_standard;
		}

		@Override
		public int getIconResId() {
			return R.drawable.standard_n;
		}
	},

	STANDARD_X {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_standard_x;
		}

		@Override
		public int getIconResId() {
			return R.drawable.standard_x;
		}
	},

	STANDARD_HYPER {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_standard_hyper;
		}

		@Override
		public int getIconResId() {
			return R.drawable.standard_h;
		}
	},

	SQUIGGLY {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_squiggly;
		}

		@Override
		public int getIconResId() {
			return R.drawable.squiggly_n;
		}
	},

	SQUIGGLY_X {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_squiggly_x;
		}

		@Override
		public int getIconResId() {
			return R.drawable.squiggly_x;
		}
	},

	SQUIGGLY_H {
		@Override
		public int getNameResId() {
			return R.string.name_sudoku_squiggly_hyper;
		}

		@Override
		public int getIconResId() {
			return R.drawable.squiggly_h;
		}
	};

	public abstract int getNameResId();
	public abstract int getIconResId();

	public static PuzzleType forOrdinal(int ordinal) {
		return PuzzleType.values()[ordinal];
	}
}
