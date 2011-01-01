/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009, 2011  Markus Wiederkehr
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

package com.googlecode.andoku.clientdemo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.googlecode.andoku.client.AndokuClient;

/**
 * Simple demo class that illustrates the client code. Imports a single sudoku puzzle into Andoku
 * and then starts the game.
 */
public class ClientDemoActivity extends Activity {
	private static final String TAG = ClientDemoActivity.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndokuClient client = new AndokuClient(this);
		if (!client.isCompatibleAndokuVersionInstalled()) {
			Log.e(TAG, "Incompatible or no version of Andoku available");
			finish();
			return;
		}

		// path should not contain forwards slashes because Andoku cannot open sub-folders yet
		Uri folderUri = client.getOrCreateFolder("Sudoku Client Demo");

		ContentValues values = createPuzzle();
		Uri puzzleUri = client.insertPuzzle(folderUri, values);

		Intent intent = client.getOpenPuzzleIntent(puzzleUri, true);
		startActivity(intent);
		finish();
	}

	private ContentValues createPuzzle() {
		ContentValues values = new ContentValues();

		// puzzle name (optional)
		values.put(AndokuClient.KEY_NAME, "Squiggly Hyper");

		// the clues of the puzzle (required)
		values.put(AndokuClient.KEY_CLUES,
				"070000580000008070150600000900005000010906030000800006000009053040500000085000040");

		// the area codes of a squiggly puzzle (omit if not squiggly)
		values.put(AndokuClient.KEY_AREAS,
				"112233334122222334111122334115555344666654444667555588677998888677999998677779988");

		// the extra regions code ("X" = X, "H" = Hyper, "P" = Percent, "C" = Color, omit if none)
		values.put(AndokuClient.KEY_EXTRA_REGIONS, "H");

		// difficulty (optional) ranges from 0 (easy) to 4 (fiendish)
		values.put(AndokuClient.KEY_DIFFICULTY, 2);

		return values;
	}
}
