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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.googlecode.andoku.Constants;
import com.googlecode.andoku.source.Difficulty;

public class PuzzleDb {
	private static final String TAG = PuzzleDb.class.getName();

	public static final String DATABASE_NAME = "puzzles.db";
	private static final int DATABASE_VERSION = 3;

	private static final char PATH_SEPARATOR_CHAR = '/';

	private static final String COL_ID = BaseColumns._ID;

	private static final String TABLE_FOLDERS = "folders";
	public static final String COL_FOLDER_NAME = "name";
	public static final String COL_FOLDER_PARENT = "parent";

	private static final String TABLE_PUZZLES = "puzzles";
	public static final String COL_FOLDER = "folder";
	public static final String COL_NAME = "name";
	public static final String COL_DIFFICULTY = "difficulty"; // 0-4|-1
	public static final String COL_SIZE = "size"; //             9
	public static final String COL_CLUES = "clues"; //           "...6.12........3......"
	public static final String COL_AREAS = "areas"; //           "11122223311122222341.."|""
	public static final String COL_EXTRA_REGIONS = "extra"; //   "H"|"X"|""
	public static final String COL_SOLUTION = "solution"; //     "35869127496158734217.."

	public static final int ROOT_FOLDER_ID = -1;

	private DatabaseHelper openHelper;

	public PuzzleDb(Context context) {
		if (Constants.LOG_V)
			Log.v(TAG, "PuzzleDb()");

		openHelper = new DatabaseHelper(context);
	}

	public void resetAll() {
		if (Constants.LOG_V)
			Log.v(TAG, "resetAll()");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_FOLDERS, null, null);
		db.delete(TABLE_PUZZLES, null, null);
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

	public long insertPuzzle(long folderId, PuzzleInfo puzzleInfo) {
		if (Constants.LOG_V)
			Log.v(TAG, "insertPuzzle(" + folderId + "," + puzzleInfo + ")");

		if (!folderExists(folderId))
			throw new IllegalArgumentException("No such folder: " + folderId);

		SQLiteDatabase db = openHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_FOLDER, folderId);
		values.put(COL_NAME, puzzleInfo.getName());
		values.put(COL_DIFFICULTY, puzzleInfo.getDifficulty().ordinal());
		values.put(COL_SIZE, puzzleInfo.getSize());
		values.put(COL_CLUES, puzzleInfo.getClues());
		values.put(COL_AREAS, puzzleInfo.getAreas());
		values.put(COL_EXTRA_REGIONS, puzzleInfo.getExtraRegions());
		values.put(COL_SOLUTION, puzzleInfo.getSolution());

		long insertedRowId = db.insert(TABLE_PUZZLES, null, values);
		if (insertedRowId == -1)
			throw new SQLException("Could not create puzzle " + puzzleInfo);

		return insertedRowId;
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

	public PuzzleInfo loadPuzzle(long folderId, int number) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadPuzzle(" + folderId + "," + number + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_NAME, COL_DIFFICULTY, COL_SIZE, COL_CLUES, COL_AREAS,
				COL_EXTRA_REGIONS, COL_SOLUTION };
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
				builder.setSolution(cursor.getString(6));
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

	public void close() {
		if (Constants.LOG_V)
			Log.v(TAG, "close()");

		openHelper.close();
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
					+ COL_EXTRA_REGIONS + " TEXT, " + COL_SOLUTION + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ "; which will destroy all old data!");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUZZLES);
			onCreate(db);
		}
	}
}
