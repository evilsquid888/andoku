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
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.source.PuzzleSourceIds;

public class OpenImportedPuzzleActivity extends Activity {
	private static final String TAG = OpenImportedPuzzleActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras == null) {
			Log.e(TAG, "Extras missing from intent");
			finish();
			return;
		}

		Parcelable parcelable = extras.getParcelable(Constants.EXTRA_PUZZLE_URI);
		if (!(parcelable instanceof Uri)) {
			Log.e(TAG, "Not an Uri: " + parcelable);
			finish();
			return;
		}

		Uri puzzleUri = (Uri) parcelable;
		long[] folderAndPuzzleIds = AndokuContentProvider.getFolderAndPuzzleIds(puzzleUri);
		if (folderAndPuzzleIds == null) {
			Log.e(TAG, "Not a valid puzzle URI: " + puzzleUri);
			finish();
			return;
		}

		long folderId = folderAndPuzzleIds[0];
		long puzzleId = folderAndPuzzleIds[1];

		String puzzleSourceId = PuzzleSourceIds.forDbFolder(folderId);
		int number = getPuzzleNumber(folderId, puzzleId);
		if (number == -1) {
			Log.e(TAG, "No such puzzle: " + puzzleId + " in folder: " + folderId);
			finish();
			return;
		}

		boolean start = intent.getBooleanExtra(Constants.EXTRA_START_PUZZLE, false);

		intent = new Intent(this, AndokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
		intent.putExtra(Constants.EXTRA_START_PUZZLE, start);
		startActivity(intent);

		finish();
	}

	private int getPuzzleNumber(long folderId, long puzzleId) {
		AndokuDatabase db = new AndokuDatabase(this);
		try {
			return db.getPuzzleNumber(folderId, puzzleId);
		}
		finally {
			db.close();
		}
	}
}
