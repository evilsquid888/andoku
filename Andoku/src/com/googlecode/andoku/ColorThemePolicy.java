/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C)   Markus Wiederkehr
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

package com.googlecode.andoku;

import com.googlecode.andoku.ColorTheme.Builder;
import com.googlecode.andokusquid.R;

public enum ColorThemePolicy {
	CLASSIC {
		@Override
		public void customize(Builder builder) {
		}
	},

	DARK {
		@Override
		public void customize(Builder builder) {
			builder.backgroudColors = new int[] { 0xff000000, 0xff000000, 0xff6b7881 };
			builder.puzzleBackgroundColor = 0xff000000;
			builder.nameTextColor = 0xffffffdd;
			builder.difficultyTextColor = 0xffffffdd;
			builder.sourceTextColor = 0xffffffdd;
			builder.timerTextColor = 0xffffffdd;
			builder.gridColor = 0x66bfbfbf;
			builder.borderColor = 0xffffffff;
			builder.extraRegionColor = 0xcd7b89cd;
			builder.colorSudokuExtraRegionColors = new int[] { 0x77ff0000, 0x77000000, 0x7700ff00,
					0x77808080, 0x7700ffff, 0x770000ff, 0x77ffff00, 0x77ff00ff, 0x77ffffff };
			builder.valueColor = 0xffddffdd;
			builder.clueColor = 0xffffffdd;
			builder.errorColor = 0xffe60000;
			builder.markedPositionColor = 0xb300ff00;
			builder.markedPositionClueColor = 0xb3ff0000;
			builder.areaColors2 = new int[] { 0xff000000, 0xff333333 };
			builder.areaColors3 = Util.colorRing(0xff000033, 3);
			builder.areaColors4 = Util.colorRing(0xff000033, 4);
			builder.highlightedCellColorSingleDigit = 0xe6e6e600;
			builder.highlightedCellColorMultipleDigits = 0xe6a6a600;
		}
	};

	public abstract void customize(Builder builder);
}
