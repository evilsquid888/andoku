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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.googlecode.andoku.db.AndokuDatabase;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getName();

	private Button foldersButton;
	private Button resumeGameButton;

	private AndokuDatabase db;
	private long importedPuzzlesFolderId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		super.onCreate(savedInstanceState);

		BackupUtil.restoreOrBackupDatabase(this);

		Util.setFullscreenWorkaround(this);

		setContentView(R.layout.main);

		db = new AndokuDatabase(this);

		importedPuzzlesFolderId = db.getOrCreateFolder(Constants.IMPORTED_PUZZLES_FOLDER);

		resumeGameButton = (Button) findViewById(R.id.resumeGameButton);
		resumeGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onResumeGameButton();
			}
		});

		Button newGameButton = (Button) findViewById(R.id.selectNewGameButton);
		newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectNewGameButton();
			}
		});

		foldersButton = (Button) findViewById(R.id.selectFoldersButton);
		foldersButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectFoldersButton();
			}
		});

		Button settingsButton = (Button) findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSettingsButton();
			}
		});

		Button helpButton = (Button) findViewById(R.id.helpButton);
		helpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onHelpButton();
			}
		});

		Button aboutButton = (Button) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onAboutButton();
			}
		});
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume()");

		super.onResume();

		final boolean hasPuzzleFolders = db.hasSubFolders(importedPuzzlesFolderId);
		foldersButton.setVisibility(hasPuzzleFolders ? View.VISIBLE : View.GONE);

		final boolean hasSavedGames = db.hasUnfinishedGames();
		resumeGameButton.setEnabled(hasSavedGames);
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

	void onResumeGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameButton()");

		Intent intent = new Intent(this, ResumeGameActivity.class);
		startActivity(intent);
	}

	void onSelectNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectNewGameButton()");

		Intent intent = new Intent(this, NewGameActivity.class);
		startActivity(intent);
	}

	void onSelectFoldersButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectFoldersButton()");

		Intent intent = new Intent(this, FolderListActivity.class);
		intent.putExtra(Constants.EXTRA_FOLDER_ID, importedPuzzlesFolderId);
		startActivity(intent);
	}

	void onSettingsButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSettingsButton()");

		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	void onHelpButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onHelpButton()");

		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

	void onAboutButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onAboutButton()");

		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
}
