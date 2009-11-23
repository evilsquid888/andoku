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

package com.googlecode.andoku;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.model.PuzzleType;
import com.googlecode.andoku.source.PuzzleSourceIds;

class Util {
	private Util() {
	}

	public static void setFullscreenWorkaround(Activity activity) {
		// Workaround for issue #1
		// FLAG_LAYOUT_NO_LIMITS: allow window to extend outside of the screen.
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
			case SQUIGGLY:
				return R.string.name_sudoku_squiggly;
			case SQUIGGLY_X:
				return R.string.name_sudoku_squiggly_x;
			case SQUIGGLY_H:
				return R.string.name_sudoku_squiggly_hyper;
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
			case SQUIGGLY:
				return R.drawable.squiggly_n;
			case SQUIGGLY_X:
				return R.drawable.squiggly_x;
			case SQUIGGLY_H:
				return R.drawable.squiggly_h;
		}
		throw new IllegalStateException();
	}
}
