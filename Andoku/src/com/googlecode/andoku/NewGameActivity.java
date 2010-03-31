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
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.source.PuzzleSourceIds;

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

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

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

		String puzzleSourceId = getSelectedPuzzleSource();

		new GameLauncher(this, db).startNewGame(puzzleSourceId);
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
			case 3:
				sb.append("p_");
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
}
