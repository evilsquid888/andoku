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

package com.googlecode.andoku.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.test.InstrumentationTestCase;

import com.googlecode.andoku.model.PuzzleType;

public class UpdateAndokuDatabaseTest extends InstrumentationTestCase {
	public void testUpgradeFromVersion1() throws Exception {
		installDb("save_games.db.v1");

		AndokuDatabase db = new AndokuDatabase(getInstrumentation().getTargetContext());
		Cursor cursor = db.findAllGames();

		assertTrue(cursor.moveToNext());
		assertEquals(1, cursor.getLong(AndokuDatabase.IDX_GAME_ID));
		assertEquals("asset:standard_x_2", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(0, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.STANDARD_X.ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(691637, cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertEquals(1243208899828l, cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));
		assertEquals(1243209591708l, cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals(2, cursor.getLong(AndokuDatabase.IDX_GAME_ID));
		assertEquals("asset:standard_x_2", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(1, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.STANDARD_X.ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(676767, cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertEquals(1243209630317l, cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));
		assertEquals(1243379480292l, cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals(3, cursor.getLong(AndokuDatabase.IDX_GAME_ID));
		assertEquals("asset:squiggly_h_2", cursor.getString(AndokuDatabase.IDX_GAME_SOURCE));
		assertEquals(2, cursor.getInt(AndokuDatabase.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.SQUIGGLY_HYPER.ordinal(), cursor.getInt(AndokuDatabase.IDX_GAME_TYPE));
		assertEquals(856112, cursor.getLong(AndokuDatabase.IDX_GAME_TIMER));
		assertEquals(1243210307153l, cursor.getLong(AndokuDatabase.IDX_GAME_CREATED_DATE));
		assertEquals(1243552984498l, cursor.getLong(AndokuDatabase.IDX_GAME_MODIFIED_DATE));

		for (int i = 0; i < 145; i++)
			assertTrue(cursor.moveToNext());

		assertFalse(cursor.moveToNext());
	}

	private void installDb(String fileName) throws IOException, FileNotFoundException {
		AssetManager assets = getInstrumentation().getContext().getAssets();
		InputStream in = assets.open(fileName);

		File dbFile = getInstrumentation().getTargetContext().getDatabasePath(
				AndokuDatabase.DATABASE_NAME);
		OutputStream out = new FileOutputStream(dbFile);
		copy(in, out);
	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[512];

			while (true) {
				int bytes = in.read(buffer);
				if (bytes == -1)
					break;

				out.write(buffer, 0, bytes);
			}
		}
		finally {
			in.close();
			out.close();
		}
	}
}
