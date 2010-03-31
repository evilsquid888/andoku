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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.model.PuzzleType;
import com.googlecode.andoku.source.PuzzleSourceIds;

class Util {
	private Util() {
	}

	public static void setFullscreenMode(Activity activity) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean fullscreenMode = settings.getBoolean(Settings.KEY_FULLSCREEN_MODE, true);
		if (fullscreenMode) {
			// FEATURE_PROGRESS seems to suppress the tiny drop shadow at the top of the screen
			activity.requestWindowFeature(Window.FEATURE_PROGRESS);

			final Window window = activity.getWindow();

			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			// Workaround for issue #1
			// FLAG_LAYOUT_NO_LIMITS: allow window to extend outside of the screen.
			window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	public static String getFolderName(AndokuDatabase db, String sourceId) {
		return db.getFolderName(PuzzleSourceIds.getDbFolderId(sourceId));
	}

	public static String getPuzzleName(Resources resources, PuzzleType puzzleType) {
		return resources.getString(getNameResourceId(puzzleType));
	}

	public static Drawable getPuzzleIcon(Resources resources, PuzzleType puzzleType) {
		return resources.getDrawable(getIconResourceId(puzzleType));
	}

	private static int getNameResourceId(PuzzleType puzzleType) {
		switch (puzzleType) {
			case STANDARD:
				return R.string.name_sudoku_standard;
			case STANDARD_X:
				return R.string.name_sudoku_standard_x;
			case STANDARD_HYPER:
				return R.string.name_sudoku_standard_hyper;
			case STANDARD_PERCENT:
				return R.string.name_sudoku_standard_percent;
			case SQUIGGLY:
				return R.string.name_sudoku_squiggly;
			case SQUIGGLY_X:
				return R.string.name_sudoku_squiggly_x;
			case SQUIGGLY_HYPER:
				return R.string.name_sudoku_squiggly_hyper;
			case SQUIGGLY_PERCENT:
				return R.string.name_sudoku_squiggly_percent;
		}
		throw new IllegalStateException();
	}

	private static int getIconResourceId(PuzzleType puzzleType) {
		switch (puzzleType) {
			case STANDARD:
				return R.drawable.standard_n;
			case STANDARD_X:
				return R.drawable.standard_x;
			case STANDARD_HYPER:
				return R.drawable.standard_h;
			case STANDARD_PERCENT:
				return R.drawable.standard_p;
			case SQUIGGLY:
				return R.drawable.squiggly_n;
			case SQUIGGLY_X:
				return R.drawable.squiggly_x;
			case SQUIGGLY_HYPER:
				return R.drawable.squiggly_h;
			case SQUIGGLY_PERCENT:
				return R.drawable.squiggly_p;
		}
		throw new IllegalStateException();
	}
}
