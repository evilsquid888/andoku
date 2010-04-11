/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009, 2010  Markus Wiederkehr
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

// corresponds to res/xml/settings.xml
public class Settings {
	public static final String KEY_COLORED_REGIONS = "colored_regions";
	public static final String KEY_HIGHLIGHT_DIGITS = "highlight_digits_2";
	public static final String KEY_COLOR_THEME = "color_theme";
	public static final String KEY_SHOW_TIMER = "show_timer";
	public static final String KEY_FULLSCREEN_MODE = "fullscreen_mode";
	public static final String KEY_INPUT_METHOD = "input_method";
	public static final String KEY_CHECK_AGAINST_SOLUTION = "check_against_solution";
	public static final String KEY_ENABLE_ELIMINATE_VALUES = "enable_eliminate_values";

	private Settings() {
	}
}
