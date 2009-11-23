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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.source.PuzzleSourceIds;
import com.googlecode.andoku.source.PuzzleSourceResolver;

public class NewGameActivity extends Activity {
	private static final String TAG = NewGameActivity.class.getName();

	private static final String PREF_KEY_PUZZLE_GRID = "puzzleGrid";
	private static final String PREF_KEY_PUZZLE_EXTRA_REGION = "puzzleExtraRegions";
	private static final String PREF_KEY_PUZZLE_DIFFICULTY = "puzzleDifficulty";

	private AndokuDatabase db;

	private Spinner gridSpinner;
	private Spinner extraRegionsSpinner;
	private Spinner difficultySpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		super.onCreate(savedInstanceState);

		Util.setFullscreenWorkaround(this);

		setContentView(R.layout.new_game);

		db = new AndokuDatabase(this);

		Button startNewGameButton = (Button) findViewById(R.id.startNewGameButton);
		startNewGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onStartNewGameButton();
			}
		});

		gridSpinner = (Spinner) findViewById(R.id.gridSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.grid_styles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridSpinner.setAdapter(adapter);

		extraRegionsSpinner = (Spinner) findViewById(R.id.extraRegionsSpinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.extra_regions,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extraRegionsSpinner.setAdapter(adapter);

		difficultySpinner = (Spinner) findViewById(R.id.difficultySpinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.difficulties,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		difficultySpinner.setAdapter(adapter);

		loadPuzzlePreferences();
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (db != null) {
			db.close();
		}
	}

	void onStartNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onStartNewGameButton()");

		savePuzzlePreferences();

		try {
			String puzzleSourceId = getSelectedPuzzleSource();
			int number = findAvailableGame(puzzleSourceId);

			startGame(puzzleSourceId, number);
		}
		catch (PuzzleIOException e) {
			handlePuzzleIOException(e);
		}
	}

	private void loadPuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "loadPuzzlePreferences()");

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		gridSpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_GRID, 0));
		extraRegionsSpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_EXTRA_REGION, 0));
		difficultySpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_DIFFICULTY, 0));
	}

	private void savePuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "savePuzzlePreferences()");

		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt(PREF_KEY_PUZZLE_GRID, gridSpinner.getSelectedItemPosition());
		editor.putInt(PREF_KEY_PUZZLE_EXTRA_REGION, extraRegionsSpinner.getSelectedItemPosition());
		editor.putInt(PREF_KEY_PUZZLE_DIFFICULTY, difficultySpinner.getSelectedItemPosition());
		editor.commit();
	}

	private String getSelectedPuzzleSource() {
		String folderName = getSelectedAssetFolderName();
		return PuzzleSourceIds.forAssetFolder(folderName);
	}

	private String getSelectedAssetFolderName() {
		StringBuilder sb = new StringBuilder();

		switch (gridSpinner.getSelectedItemPosition()) {
			case 0:
				sb.append("standard_");
				break;
			case 1:
				sb.append("squiggly_");
				break;
			default:
				throw new IllegalStateException();
		}

		switch (extraRegionsSpinner.getSelectedItemPosition()) {
			case 0:
				sb.append("n_");
				break;
			case 1:
				sb.append("x_");
				break;
			case 2:
				sb.append("h_");
				break;
			default:
				throw new IllegalStateException();
		}

		int difficulty = difficultySpinner.getSelectedItemPosition() + 1;
		if (difficulty < 1 || difficulty > 5)
			throw new IllegalStateException();

		sb.append(difficulty);

		return sb.toString();
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
		PuzzleSource puzzleSource = PuzzleSourceResolver.resolveSource(this, puzzleSourceId);
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
		Intent intent = new Intent(this, AndokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
		startActivity(intent);
	}

	private void handlePuzzleIOException(PuzzleIOException e) {
		Log.e(TAG, "Error finding available games", e);

		Resources resources = getResources();
		String title = resources.getString(R.string.error_title_io_error);
		String message = getResources().getString(R.string.error_message_finding_available);

		Intent intent = new Intent(this, DisplayErrorActivity.class);
		intent.putExtra(Constants.EXTRA_ERROR_TITLE, title);
		intent.putExtra(Constants.EXTRA_ERROR_MESSAGE, message);
		intent.putExtra(Constants.EXTRA_ERROR_THROWABLE, e);
		startActivity(intent);

		finish();
	}
}
