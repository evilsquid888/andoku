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

package com.googlecode.andoku;

public class Constants {
	private static final String PREFIX = Constants.class.getPackage().getName() + ".";

	public static final String EXTRA_PUZZLE_SOURCE_ID = PREFIX + "puzzleSourceId";
	public static final String EXTRA_PUZZLE_NUMBER = PREFIX + "puzzleNumber";
	public static final String EXTRA_START_PUZZLE = PREFIX + "start";

	public static final String EXTRA_ERROR_TITLE = PREFIX + "errorTitle";
	public static final String EXTRA_ERROR_MESSAGE = PREFIX + "errorMessage";
	public static final String EXTRA_ERROR_THROWABLE = PREFIX + "errorException";

	public static final String EXTRA_FOLDER_ID = PREFIX + "folderId";

	public static final String EXTRA_PUZZLE_URI = PREFIX + "puzzleUri";

	public static final String IMPORTED_PUZZLES_FOLDER = "Imported Puzzles";

	public static final boolean LOG_V = false;

	private Constants() {
	}
}
