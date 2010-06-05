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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import com.googlecode.andoku.Constants;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.source.PuzzleSourceIds;

public class AndokuDatabase {
	private static final String TAG = AndokuDatabase.class.getName();

	public static final String DATABASE_NAME = "save_games.db";
	private static final int DATABASE_VERSION = 2;

	public static final int ROOT_FOLDER_ID = -1;

	private static final char PATH_SEPARATOR_CHAR = '/';

	public static final String COL_ID = BaseColumns._ID;

	public static final String TABLE_FOLDERS = "folders";
	public static final String COL_FOLDER_NAME = "name";
	public static final String COL_FOLDER_PARENT = "parent";

	// indexes for getFolders() 
	public static final int IDX_FOLDERS_ID = 0;
	public static final int IDX_FOLDERS_NAME = 1;
	public static final int IDX_FOLDERS_PARENT = 2;

	public static final String TABLE_PUZZLES = "puzzles";
	public static final String COL_FOLDER = "folder";
	public static final String COL_NAME = "name";
	public static final String COL_DIFFICULTY = "difficulty"; // 0-4|-1
	public static final String COL_SIZE = "size"; //             9
	public static final String COL_CLUES = "clues"; //           "...6.12........3......"
	public static final String COL_AREAS = "areas"; //           "11122223311122222341.."|""
	public static final String COL_EXTRA_REGIONS = "extra"; //   "H"|"X"|""

	private SQLiteStatement insertPuzzleStatement;

	private static final String TABLE_GAMES = "games";
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
	public static final int IDX_GAME_SOURCE = 1;
	public static final int IDX_GAME_NUMBER = 2;
	public static final int IDX_GAME_TYPE = 3;
	public static final int IDX_GAME_TIMER = 4;
	public static final int IDX_GAME_CREATED_DATE = 5;
	public static final int IDX_GAME_MODIFIED_DATE = 6;

	// indexes for findGamesBySource()
	public static final int IDX_GAME_BY_SOURCE_NUMBER = 0;
	public static final int IDX_GAME_BY_SOURCE_SOLVED = 1;

	private DatabaseHelper openHelper;

	public AndokuDatabase(Context context) {
		if (Constants.LOG_V)
			Log.v(TAG, "AndokuDatabase()");

		openHelper = new DatabaseHelper(context);
	}

	public void resetAll() {
		if (Constants.LOG_V)
			Log.v(TAG, "resetAll()");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_FOLDERS, null, null);
		db.delete(TABLE_PUZZLES, null, null);
		db.delete(TABLE_GAMES, null, null);
	}

	public long createFolder(String name) {
		return createFolder(ROOT_FOLDER_ID, name);
	}

	public long createFolder(long parentId, String name) {
		if (Constants.LOG_V)
			Log.v(TAG, "createFolder(" + parentId + "," + name + ")");

		checkValidFolderName(name);

		SQLiteDatabase db = openHelper.getWritableDatabase();

		return createFolder(db, parentId, name);
	}

	public boolean folderExists(String name) {
		return getFolderId(ROOT_FOLDER_ID, name) != null;
	}

	public boolean folderExists(long parentId, String name) {
		return getFolderId(parentId, name) != null;
	}

	public Long getFolderId(String name) {
		return getFolderId(ROOT_FOLDER_ID, name);
	}

	public Long getFolderId(long parentId, String name) {
		if (Constants.LOG_V)
			Log.v(TAG, "getFolderId(" + parentId + "," + name + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return getFolderId(db, parentId, name);
	}

	public long getOrCreateFolder(String name) {
		return getOrCreateFolder(ROOT_FOLDER_ID, name);
	}

	public long getOrCreateFolder(long parentId, String name) {
		if (Constants.LOG_V)
			Log.v(TAG, "getOrCreateFolder(" + parentId + "," + name + ")");

		checkValidFolderName(name);

		SQLiteDatabase db = openHelper.getWritableDatabase();

		Long folderId = getFolderId(db, parentId, name);
		if (folderId != null)
			return folderId;
		else
			return createFolder(db, parentId, name);
	}

	public Cursor getFolders() {
		return getFolders(ROOT_FOLDER_ID);
	}

	public Cursor getFolders(long parentId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getFolders(" + parentId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return getFolders(db, parentId);
	}

	public boolean folderExists(long folderId) {
		return getFolderName(folderId) != null;
	}

	public String getFolderName(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getFolderName(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_FOLDER_NAME };
		String selection = COL_ID + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };
		Cursor cursor = db.query(TABLE_FOLDERS, columns, selection, selectionArgs, null, null, null);

		try {
			if (cursor.moveToFirst())
				return cursor.getString(0);
			else
				return null;
		}
		finally {
			cursor.close();
		}
	}

	public Long getParentFolderId(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getParentFolderId(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_FOLDER_PARENT };
		String selection = COL_ID + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };
		Cursor cursor = db.query(TABLE_FOLDERS, columns, selection, selectionArgs, null, null, null);

		try {
			if (cursor.moveToFirst())
				return cursor.getLong(0);
			else
				return null;
		}
		finally {
			cursor.close();
		}
	}

	public void renameFolder(long folderId, String newName) {
		if (Constants.LOG_V)
			Log.v(TAG, "renameFolder(" + folderId + "," + newName + ")");

		checkValidFolderName(newName);

		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_FOLDER_NAME, newName);

		String whereClause = COL_ID + "=?";
		String[] whereArgs = { String.valueOf(folderId) };
		int rows = db.update(TABLE_FOLDERS, values, whereClause, whereArgs);
		if (rows != 1)
			throw new SQLException("Could not rename folder " + folderId + " in " + newName);
	}

	public void deleteFolder(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "deleteFolder(" + folderId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		deleteFolder(db, folderId);
	}

	public boolean hasSubFolders(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "hasSubFolders(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return hasSubFolders(db, folderId);
	}

	public boolean hasPuzzles(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "hasPuzzles(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return hasPuzzles(db, folderId);
	}

	public boolean isEmpty(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "isEmpty(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		return !hasSubFolders(db, folderId) && !hasPuzzles(db, folderId);
	}

	public long insertPuzzle(long folderId, PuzzleInfo puzzleInfo) {
		if (Constants.LOG_V)
			Log.v(TAG, "insertPuzzle(" + folderId + "," + puzzleInfo + ")");

		// if (!folderExists(folderId))
		//	  throw new IllegalArgumentException("No such folder: " + folderId);

		if (insertPuzzleStatement == null) {
			SQLiteDatabase db = openHelper.getWritableDatabase();
			insertPuzzleStatement = db.compileStatement("INSERT INTO " + TABLE_PUZZLES + "("
					+ COL_FOLDER + ", " + COL_NAME + ", " + COL_DIFFICULTY + ", " + COL_SIZE + ", "
					+ COL_CLUES + ", " + COL_AREAS + ", " + COL_EXTRA_REGIONS
					+ ") VALUES (?, ?, ?, ?, ?, ?, ?)");
		}

		insertPuzzleStatement.bindLong(1, folderId);
		insertPuzzleStatement.bindString(2, puzzleInfo.getName());
		insertPuzzleStatement.bindLong(3, puzzleInfo.getDifficulty().ordinal());
		insertPuzzleStatement.bindLong(4, puzzleInfo.getSize());
		insertPuzzleStatement.bindString(5, puzzleInfo.getClues());
		insertPuzzleStatement.bindString(6, puzzleInfo.getAreas());
		insertPuzzleStatement.bindString(7, puzzleInfo.getExtraRegions());

		long insertedRowId = insertPuzzleStatement.executeInsert();
		if (insertedRowId == -1)
			throw new SQLException("Could not create puzzle " + puzzleInfo);

		return insertedRowId;
	}

	public void deletePuzzle(long puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "deletePuzzle(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		String whereClause = COL_ID + "=?";
		String[] whereArgs = { String.valueOf(puzzleId) };
		db.delete(TABLE_PUZZLES, whereClause, whereArgs);
	}

	public int getNumberOfPuzzles(long folderId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getNumberOfPuzzles(" + folderId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = db.query(TABLE_PUZZLES, new String[] { "COUNT(*)" }, COL_FOLDER + "=?",
				new String[] { String.valueOf(folderId) }, null, null, null);
		try {
			cursor.moveToFirst();

			return cursor.getInt(0);
		}
		finally {
			cursor.close();
		}
	}

	public int getPuzzleNumber(long folderId, long puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getPuzzleNumber(" + folderId + "," + puzzleId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID };
		String selection = COL_FOLDER + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };

		Cursor cursor = db.query(TABLE_PUZZLES, columns, selection, selectionArgs, null, null, null,
				null);

		try {
			int total = cursor.getCount();
			return getPuzzleNumber(cursor, puzzleId, 0, total - 1);
		}
		finally {
			cursor.close();
		}
	}

	private int getPuzzleNumber(Cursor cursor, long puzzleId, int fromNumber, int toNumber) {
		if (fromNumber > toNumber)
			return -1;

		int candidate = (fromNumber + toNumber) / 2;
		if (!cursor.moveToPosition(candidate))
			throw new IllegalStateException();

		int id = cursor.getInt(0);
		if (id == puzzleId)
			return candidate;

		if (id < puzzleId)
			return getPuzzleNumber(cursor, puzzleId, candidate + 1, toNumber);
		else
			return getPuzzleNumber(cursor, puzzleId, fromNumber, candidate - 1);
	}

	public PuzzleInfo loadPuzzle(long folderId, int number) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadPuzzle(" + folderId + "," + number + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_NAME, COL_DIFFICULTY, COL_SIZE, COL_CLUES, COL_AREAS,
				COL_EXTRA_REGIONS };
		String selection = COL_FOLDER + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };
		String limit = number + ",1";

		Cursor cursor = db.query(TABLE_PUZZLES, columns, selection, selectionArgs, null, null, null,
				limit);
		try {
			if (cursor.moveToNext()) {
				PuzzleInfo.Builder builder = new PuzzleInfo.Builder(cursor.getString(3));
				builder.setName(cursor.getString(0));
				builder.setDifficulty(Difficulty.values()[cursor.getInt(1)]);
				builder.setAreas(cursor.getString(4));
				builder.setExtraRegions(cursor.getString(5));
				return builder.build();
			}
			else {
				return null;
			}
		}
		finally {
			cursor.close();
		}
	}

	public void saveGame(PuzzleId puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "saveGame(" + puzzleId + ")");

		long now = System.currentTimeMillis();

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			String[] columns = { COL_ID };
			String selection = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
			String[] selectionArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
			Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);

			long rowId = -1;
			if (cursor.moveToFirst()) {
				rowId = cursor.getLong(0);
			}

			cursor.close();

			ContentValues values = new ContentValues();
			values.put(COL_PUZZLE, puzzle.saveToMemento());
			values.put(COL_TIMER, timer.getTime());
			values.put(COL_SOLVED, puzzle.isSolved());
			values.put(COL_MODIFIED_DATE, now);

			if (rowId == -1) {
				values.put(COL_SOURCE, puzzleId.puzzleSourceId);
				values.put(COL_NUMBER, puzzleId.number);
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

	public boolean loadGame(PuzzleId puzzleId, AndokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadGame(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_PUZZLE, COL_TIMER };
		String selection = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
		String[] selectionArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			byte[] memento = cursor.getBlob(0);
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

	public void delete(PuzzleId puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "delete(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		String whereClause = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
		String[] whereArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
		db.delete(TABLE_GAMES, whereClause, whereArgs);
	}

	public Cursor findAllGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findAllGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_SOURCE, COL_NUMBER, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
				COL_MODIFIED_DATE };
		return db.query(TABLE_GAMES, columns, null, null, null, null, null);
	}

	public boolean hasUnfinishedGames() {
		Cursor cursor = findUnfinishedGames();
		try {
			return cursor.moveToNext();
		}
		finally {
			cursor.close();
		}
	}

	public Cursor findUnfinishedGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findUnfinishedGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_SOURCE, COL_NUMBER, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
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

	public PuzzleId puzzleIdByRowId(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "puzzleIdByRowId(" + rowId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_SOURCE, COL_NUMBER };
		String selection = COL_ID + "=?";
		String[] selectionArgs = { Long.toString(rowId) };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return null;
			}

			String puzzleSourceId = cursor.getString(0);
			int number = cursor.getInt(1);
			return new PuzzleId(puzzleSourceId, number);
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

	public void beginTransaction() {
		openHelper.getWritableDatabase().beginTransaction();
	}

	public void endTransaction() {
		openHelper.getWritableDatabase().endTransaction();
	}

	public void setTransactionSuccessful() {
		openHelper.getWritableDatabase().setTransactionSuccessful();
	}

	public Cursor query(SQLiteQueryBuilder qb, String[] projection, String selection,
			String[] selectionArgs, String orderBy) {
		SQLiteDatabase db = openHelper.getReadableDatabase();

		return qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
	}

	private long createFolder(SQLiteDatabase db, long parentId, String name) {
		ContentValues values = new ContentValues();
		values.put(COL_FOLDER_NAME, name);
		values.put(COL_FOLDER_PARENT, parentId);

		long insertedRowId = db.insert(TABLE_FOLDERS, null, values);
		if (insertedRowId == -1)
			throw new SQLException("Could not create folder " + name);

		return insertedRowId;
	}

	private Long getFolderId(SQLiteDatabase db, long parentId, String name) {
		String[] columns = { COL_ID };
		String selection = COL_FOLDER_NAME + "=? AND " + COL_FOLDER_PARENT + "=?";
		String[] selectionArgs = { name, String.valueOf(parentId) };
		Cursor cursor = db.query(TABLE_FOLDERS, columns, selection, selectionArgs, null, null, null);
		try {
			if (cursor.moveToNext())
				return cursor.getLong(0);
			else
				return null;
		}
		finally {
			cursor.close();
		}
	}

	private void deleteFolder(SQLiteDatabase db, long folderId) {
		recursivelyDeleteSubFolders(db, folderId);

		deletePuzzles(db, folderId);

		deleteSavedGames(db, folderId);

		deleteFolder0(db, folderId);
	}

	private void recursivelyDeleteSubFolders(SQLiteDatabase db, long folderId) {
		Cursor cursor = getFolders(db, folderId);
		try {
			while (cursor.moveToNext()) {
				long subFolderId = cursor.getLong(0);
				deleteFolder(db, subFolderId);
			}
		}
		finally {
			cursor.close();
		}
	}

	private void deletePuzzles(SQLiteDatabase db, long folderId) {
		String whereClause = COL_FOLDER + "=?";
		String[] whereArgs = { String.valueOf(folderId) };
		db.delete(TABLE_PUZZLES, whereClause, whereArgs);
	}

	private void deleteSavedGames(SQLiteDatabase db, long folderId) {
		String whereClause = COL_SOURCE + "=?";
		String[] whereArgs = { PuzzleSourceIds.forDbFolder(folderId) };
		db.delete(TABLE_GAMES, whereClause, whereArgs);
	}

	private void deleteFolder0(SQLiteDatabase db, long folderId) {
		String whereClause = COL_ID + "=?";
		String[] whereArgs = { String.valueOf(folderId) };
		int rows = db.delete(TABLE_FOLDERS, whereClause, whereArgs);
		if (rows != 1)
			throw new SQLException("Could not delete folder " + folderId);
	}

	private Cursor getFolders(SQLiteDatabase db, long parentId) {
		String selection = COL_FOLDER_PARENT + "=?";
		String[] selectionArgs = { String.valueOf(parentId) };
		final String orderBy = COL_FOLDER_NAME + " asc";
		return db.query(TABLE_FOLDERS, null, selection, selectionArgs, null, null, orderBy);
	}

	private void checkValidFolderName(String folderName) {
		if (folderName == null || folderName.length() == 0)
			throw new IllegalArgumentException();
		if (folderName.indexOf(PATH_SEPARATOR_CHAR) != -1)
			throw new IllegalArgumentException();
	}

	private boolean hasSubFolders(SQLiteDatabase db, long folderId) {
		String[] columns = { COL_ID };
		String selection = COL_FOLDER_PARENT + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };
		String limit = "1";
		Cursor cursor = db.query(TABLE_FOLDERS, columns, selection, selectionArgs, null, null, null,
				limit);
		try {
			return cursor.moveToNext();
		}
		finally {
			cursor.close();
		}
	}

	private boolean hasPuzzles(SQLiteDatabase db, long folderId) {
		String[] columns = { COL_ID };
		String selection = COL_FOLDER + "=?";
		String[] selectionArgs = { String.valueOf(folderId) };
		String limit = "1";
		Cursor cursor = db.query(TABLE_PUZZLES, columns, selection, selectionArgs, null, null, null,
				limit);
		try {
			return cursor.moveToNext();
		}
		finally {
			cursor.close();
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
			db.execSQL("CREATE TABLE " + TABLE_FOLDERS + " (" + COL_ID + " INTEGER PRIMARY KEY,"
					+ COL_FOLDER_NAME + " TEXT, " + COL_FOLDER_PARENT + " INTEGER, UNIQUE ("
					+ COL_FOLDER_NAME + ", " + COL_FOLDER_PARENT + "));");

			db.execSQL("CREATE TABLE " + TABLE_PUZZLES + " (" + COL_ID + " INTEGER PRIMARY KEY,"
					+ COL_FOLDER + " INTEGER," + COL_NAME + " TEXT, " + COL_DIFFICULTY + " INTEGER, "
					+ COL_SIZE + " INTEGER, " + COL_CLUES + " TEXT, " + COL_AREAS + " TEXT, "
					+ COL_EXTRA_REGIONS + " TEXT);");

			db.execSQL("CREATE TABLE " + TABLE_GAMES + " (" + COL_ID + " INTEGER PRIMARY KEY,"
					+ COL_SOURCE + " TEXT," + COL_NUMBER + " INTEGER," + COL_TYPE + " INTEGER,"
					+ COL_PUZZLE + " BLOB," + COL_TIMER + " INTEGER," + COL_SOLVED + " BOOLEAN,"
					+ COL_CREATED_DATE + " INTEGER," + COL_MODIFIED_DATE + " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ".");

			db.beginTransaction();
			try {
				if (oldVersion < 2)
					upgradeV1ToV2(db);

				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}

		private void upgradeV1ToV2(SQLiteDatabase db) {
			Log.d(TAG, "Upgrading from version 1 to 2.");

			// Equivalent to "ALTER TABLE games DROP COLUMN pid;" which is not supported by sqlite.
			// Loses the ID column but that does not matter.

			db.execSQL("ALTER TABLE " + TABLE_GAMES + " RENAME TO tmp;");
			onCreate(db);

			Cursor cursor = db.query("tmp", null, null, null, null, null, null);
			while (cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				// idx 0 is ID
				// idx 1 is old puzzleId ("pid") to be removed
				values.put(COL_SOURCE, cursor.getString(2));
				values.put(COL_NUMBER, cursor.getInt(3));
				values.put(COL_TYPE, cursor.getInt(4));
				values.put(COL_PUZZLE, cursor.getBlob(5));
				values.put(COL_TIMER, cursor.getLong(6));
				values.put(COL_SOLVED, cursor.getInt(7));
				values.put(COL_CREATED_DATE, cursor.getLong(8));
				values.put(COL_MODIFIED_DATE, cursor.getLong(9));

				db.insert(TABLE_GAMES, null, values);
			}
			cursor.close();

			db.execSQL("DROP TABLE tmp;");

			Log.d(TAG, "Upgraded from version 1 to 2.");
		}
	}
}
