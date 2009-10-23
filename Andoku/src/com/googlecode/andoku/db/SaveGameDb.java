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

package com.googlecode.andoku.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.googlecode.andoku.Constants;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;

public class SaveGameDb {
	private static final String TAG = SaveGameDb.class.getName();

	public static final String DATABASE_NAME = "save_games.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_GAMES = "games";
	public static final String COL_ID = BaseColumns._ID;
	public static final String COL_PUZZLE_ID = "pid";
	public static final String COL_SOURCE = "source";
	public static final String COL_NUMBER = "number";
	public static final String COL_TYPE = "type";
	public static final String COL_PUZZLE = "puzzle";
	public static final String COL_TIMER = "timer";
	public static final String COL_SOLVED = "solved";
	public static final String COL_CREATED_DATE = "created";
	public static final String COL_MODIFIED_DATE = "modified";

	// indexes for findAllGames() and findUnfinishedGames();
	public static final int IDX_GAME_ID = 0;
	public static final int IDX_GAME_PUZZLE_ID = 1;
	public static final int IDX_GAME_TYPE = 2;
	public static final int IDX_GAME_TIMER = 3;
	public static final int IDX_GAME_CREATED_DATE = 4;
	public static final int IDX_GAME_MODIFIED_DATE = 5;

	// indexes for findGamesBySource()
	public static final int IDX_GAME_BY_SOURCE_NUMBER = 0;
	public static final int IDX_GAME_BY_SOURCE_SOLVED = 1;

	private DatabaseHelper openHelper;

	public SaveGameDb(Context context) {
		if (Constants.LOG_V)
			Log.v(TAG, "SaveGameDb()");

		openHelper = new DatabaseHelper(context);
	}

	public void resetAll() {
		if (Constants.LOG_V)
			Log.v(TAG, "resetAll()");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_GAMES, null, null);
	}

	public void saveGame(String puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "saveGame(" + puzzleId + ")");

		long now = System.currentTimeMillis();

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			String[] columns = { COL_ID };
			String selection = COL_PUZZLE_ID + "=?";
			String[] selectionArgs = { puzzleId };
			Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);

			long rowId = -1;
			if (cursor.moveToFirst()) {
				rowId = cursor.getLong(0);
			}

			cursor.close();

			ContentValues values = new ContentValues();
			values.put(COL_PUZZLE, serialize(puzzle.saveToMemento()));
			values.put(COL_TIMER, timer.getTime());
			values.put(COL_SOLVED, puzzle.isSolved());
			values.put(COL_MODIFIED_DATE, now);

			if (rowId == -1) {
				int idx = puzzleId.lastIndexOf(':');
				String source = puzzleId.substring(0, idx);
				int number = Integer.parseInt(puzzleId.substring(idx + 1));

				values.put(COL_PUZZLE_ID, puzzleId);
				values.put(COL_SOURCE, source);
				values.put(COL_NUMBER, number);
				values.put(COL_TYPE, puzzle.getPuzzleType().ordinal());
				values.put(COL_CREATED_DATE, now);
				long insertedRowId = db.insert(TABLE_GAMES, null, values);
				if (insertedRowId == -1)
					return;
			}
			else {
				int updated = db.update(TABLE_GAMES, values, COL_ID + "=?", new String[] { String
						.valueOf(rowId) });
				if (updated == 0)
					return;
			}

			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	public boolean loadGame(String puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadGame(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_PUZZLE, COL_TIMER };
		String selection = COL_PUZZLE_ID + "=?";
		String[] selectionArgs = { puzzleId };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			Object memento = deserialize(cursor.getBlob(0));
			long time = cursor.getLong(1);

			if (!puzzle.restoreFromMemento(memento)) {
				Log.w(TAG, "Could not restore puzzle memento for " + puzzleId);
				return false;
			}

			timer.setTime(time);
			return true;
		}
		finally {
			cursor.close();
		}
	}

	public void delete(String puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "delete(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_GAMES, COL_PUZZLE_ID + "=?", new String[] { puzzleId });
	}

	public Cursor findAllGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findAllGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_PUZZLE_ID, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
				COL_MODIFIED_DATE };
		return db.query(TABLE_GAMES, columns, null, null, null, null, null);
	}

	public Cursor findUnfinishedGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findUnfinishedGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_PUZZLE_ID, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
				COL_MODIFIED_DATE };
		String selection = COL_SOLVED + "=0";
		String orderBy = COL_MODIFIED_DATE + " DESC";
		return db.query(TABLE_GAMES, columns, selection, null, null, null, orderBy);
	}

	public Cursor findGamesBySource(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "findGamesBySource(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_NUMBER, COL_SOLVED };
		String selection = COL_SOURCE + "=?";
		String[] selectionArgs = new String[] { puzzleSourceId };
		String orderBy = COL_NUMBER;
		return db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, orderBy);
	}

	public GameStatistics getStatistics(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getStatistics(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { "COUNT(*)", "SUM(timer)", "MIN(timer)", "MAX(timer)" };
		String selection = COL_SOURCE + "=? AND " + COL_SOLVED + "=1";
		String[] selectionArgs = new String[] { puzzleSourceId };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			cursor.moveToFirst();

			return new GameStatistics(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2));
		}
		finally {
			cursor.close();
		}
	}

	public String puzzleIdByRowId(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "puzzleIdByRowId(" + rowId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_PUZZLE_ID };
		String selection = COL_ID + "=?";
		String[] selectionArgs = { Long.toString(rowId) };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return null;
			}

			return cursor.getString(0);
		}
		finally {
			cursor.close();
		}
	}

	public void close() {
		if (Constants.LOG_V)
			Log.v(TAG, "close()");

		openHelper.close();
	}

	private byte[] serialize(Serializable memento) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(baos);
			oout.writeObject(memento);
			oout.close();
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Object deserialize(byte[] blob) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			ObjectInputStream oin = new ObjectInputStream(bais);
			Object object = oin.readObject();
			oin.close();
			return object;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_GAMES + " (" + COL_ID + " INTEGER PRIMARY KEY,"
					+ COL_PUZZLE_ID + " TEXT," + COL_SOURCE + " TEXT," + COL_NUMBER + " INTEGER,"
					+ COL_TYPE + " INTEGER," + COL_PUZZLE + " BLOB," + COL_TIMER + " INTEGER,"
					+ COL_SOLVED + " BOOLEAN," + COL_CREATED_DATE + " INTEGER," + COL_MODIFIED_DATE
					+ " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ "; which will destroy all old data!");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
			onCreate(db);
		}
	}
}
