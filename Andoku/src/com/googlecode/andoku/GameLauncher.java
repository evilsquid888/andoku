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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.source.PuzzleSourceResolver;

class GameLauncher {
	private static final String TAG = GameLauncher.class.getName();

	private final Activity activity;
	private final AndokuDatabase db;

	public GameLauncher(Activity activity, AndokuDatabase db) {
		this.activity = activity;
		this.db = db;
	}

	public void startNewGame(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "startNewGame(" + puzzleSourceId + ")");

		try {
			int number = findAvailableGame(puzzleSourceId);

			startGame(puzzleSourceId, number);
		}
		catch (PuzzleIOException e) {
			handlePuzzleIOException(e);
		}
	}

	private int findAvailableGame(String puzzleSourceId) throws PuzzleIOException {
		if (Constants.LOG_V)
			Log.v(TAG, "findAvailableGame(" + puzzleSourceId + ")");

		int numberOfPuzzles = getNumberOfPuzzles(puzzleSourceId);

		int candidate = 0;
		int fallback = -1; // use an unsolved game if no unplayed game found

		Cursor c = db.findGamesBySource(puzzleSourceId);

		try {
			while (c.moveToNext()) {
				int number = c.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_NUMBER);

				// is there a gap and candidate number is available?
				if (number > candidate) {
					if (Constants.LOG_V)
						Log.v(TAG, "found gap before save game " + number + "; returning " + candidate);

					return candidate;
				}

				boolean solved = c.getInt(AndokuDatabase.IDX_GAME_BY_SOURCE_SOLVED) != 0;
				if (!solved && fallback == -1)
					fallback = number;

				candidate++;
			}

			if (puzzleExists(candidate, numberOfPuzzles)) {
				if (Constants.LOG_V)
					Log.v(TAG, "returning next game after save games: " + candidate);

				return candidate;
			}

			if (fallback != -1 && puzzleExists(fallback, numberOfPuzzles)) {
				if (Constants.LOG_V)
					Log.v(TAG, "all games played; returning first uncomplete: " + fallback);

				return fallback;
			}

			if (Constants.LOG_V)
				Log.v(TAG, "all games solved; returning 0");

			return 0;
		}
		finally {
			c.close();
		}
	}

	private int getNumberOfPuzzles(String puzzleSourceId) throws PuzzleIOException {
		PuzzleSource puzzleSource = PuzzleSourceResolver.resolveSource(activity, puzzleSourceId);
		try {
			return puzzleSource.numberOfPuzzles();
		}
		finally {
			puzzleSource.close();
		}
	}

	private boolean puzzleExists(int number, int total) {
		return number >= 0 && number < total;
	}

	private void startGame(String puzzleSourceId, int number) {
		Intent intent = new Intent(activity, AndokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
		activity.startActivity(intent);
	}

	private void handlePuzzleIOException(PuzzleIOException e) {
		Log.e(TAG, "Error finding available games", e);

		Resources resources = activity.getResources();
		String title = resources.getString(R.string.error_title_io_error);
		String message = resources.getString(R.string.error_message_finding_available);

		Intent intent = new Intent(activity, DisplayErrorActivity.class);
		intent.putExtra(Constants.EXTRA_ERROR_TITLE, title);
		intent.putExtra(Constants.EXTRA_ERROR_MESSAGE, message);
		intent.putExtra(Constants.EXTRA_ERROR_THROWABLE, e);
		activity.startActivity(intent);

		activity.finish();
	}
}
