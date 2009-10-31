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

public class UpdateSaveGameDbTest extends InstrumentationTestCase {
	public void testUpgradeFromVersion1() throws Exception {
		installDb("save_games.db.v1");

		SaveGameDb saveGameDb = new SaveGameDb(getInstrumentation().getTargetContext());
		Cursor cursor = saveGameDb.findAllGames();

		assertTrue(cursor.moveToNext());
		assertEquals(1, cursor.getLong(SaveGameDb.IDX_GAME_ID));
		assertEquals("asset:standard_x_2", cursor.getString(SaveGameDb.IDX_GAME_SOURCE));
		assertEquals(0, cursor.getInt(SaveGameDb.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.STANDARD_X.ordinal(), cursor.getInt(SaveGameDb.IDX_GAME_TYPE));
		assertEquals(691637, cursor.getLong(SaveGameDb.IDX_GAME_TIMER));
		assertEquals(1243208899828l, cursor.getLong(SaveGameDb.IDX_GAME_CREATED_DATE));
		assertEquals(1243209591708l, cursor.getLong(SaveGameDb.IDX_GAME_MODIFIED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals(2, cursor.getLong(SaveGameDb.IDX_GAME_ID));
		assertEquals("asset:standard_x_2", cursor.getString(SaveGameDb.IDX_GAME_SOURCE));
		assertEquals(1, cursor.getInt(SaveGameDb.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.STANDARD_X.ordinal(), cursor.getInt(SaveGameDb.IDX_GAME_TYPE));
		assertEquals(676767, cursor.getLong(SaveGameDb.IDX_GAME_TIMER));
		assertEquals(1243209630317l, cursor.getLong(SaveGameDb.IDX_GAME_CREATED_DATE));
		assertEquals(1243379480292l, cursor.getLong(SaveGameDb.IDX_GAME_MODIFIED_DATE));

		assertTrue(cursor.moveToNext());
		assertEquals(3, cursor.getLong(SaveGameDb.IDX_GAME_ID));
		assertEquals("asset:squiggly_h_2", cursor.getString(SaveGameDb.IDX_GAME_SOURCE));
		assertEquals(2, cursor.getInt(SaveGameDb.IDX_GAME_NUMBER));
		assertEquals(PuzzleType.SQUIGGLY_H.ordinal(), cursor.getInt(SaveGameDb.IDX_GAME_TYPE));
		assertEquals(856112, cursor.getLong(SaveGameDb.IDX_GAME_TIMER));
		assertEquals(1243210307153l, cursor.getLong(SaveGameDb.IDX_GAME_CREATED_DATE));
		assertEquals(1243552984498l, cursor.getLong(SaveGameDb.IDX_GAME_MODIFIED_DATE));

		for (int i = 0; i < 145; i++)
			assertTrue(cursor.moveToNext());

		assertFalse(cursor.moveToNext());
	}

	private void installDb(String fileName) throws IOException, FileNotFoundException {
		AssetManager assets = getInstrumentation().getContext().getAssets();
		InputStream in = assets.open(fileName);

		File dbFile = getInstrumentation().getTargetContext().getDatabasePath(
				SaveGameDb.DATABASE_NAME);
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