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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.db.PuzzleInfo;
import com.googlecode.andoku.model.Difficulty;

public class AndokuContentProvider extends ContentProvider {
	private static final String TAG = AndokuContentProvider.class.getName();

	public static final String AUTHORITY = "com.googlecode.andoku.puzzlesprovider";
	public static final String PATH_FOLDERS = "folders";
	public static final String PATH_PUZZLES = "puzzles";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_FOLDERS);

	public static final String KEY_PATH = "path";
	public static final String KEY_NAME = "name";
	public static final String KEY_CLUES = "clues";
	public static final String KEY_DIFFICULTY = "difficulty";
	public static final String KEY_AREAS = "areas";
	public static final String KEY_EXTRA_REGIONS = "extraRegions";

	private static final int CODE_FOLDERS = 1;
	private static final int CODE_FOLDERS_ID = 2;
	private static final int CODE_PUZZLES = 3;
	private static final int CODE_PUZZLES_ID = 4;

	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		MATCHER.addURI(AUTHORITY, PATH_FOLDERS, CODE_FOLDERS);
		MATCHER.addURI(AUTHORITY, PATH_FOLDERS + "/#", CODE_FOLDERS_ID);
		MATCHER.addURI(AUTHORITY, PATH_FOLDERS + "/#/" + PATH_PUZZLES, CODE_PUZZLES);
		MATCHER.addURI(AUTHORITY, PATH_FOLDERS + "/#/" + PATH_PUZZLES + "/#", CODE_PUZZLES_ID);
	}

	// projection map for queryFolder()
	private static final Map<String, String> FOLDERS_PROJECTION_MAP;

	static {
		FOLDERS_PROJECTION_MAP = new HashMap<String, String>();
		FOLDERS_PROJECTION_MAP.put(AndokuDatabase.COL_ID, AndokuDatabase.COL_ID);
		FOLDERS_PROJECTION_MAP.put(AndokuDatabase.COL_FOLDER_NAME, AndokuDatabase.COL_FOLDER_NAME);
	}

	private AndokuDatabase db;

	public static long[] getFolderAndPuzzleIds(Uri puzzleUri) {
		int match = MATCHER.match(puzzleUri);
		switch (match) {
			case CODE_FOLDERS:
				return null;
			case CODE_FOLDERS_ID:
				return null;
			case CODE_PUZZLES:
				return null;
			case CODE_PUZZLES_ID:
				long folderId = Long.parseLong(puzzleUri.getPathSegments().get(1));
				long puzzleId = Long.parseLong(puzzleUri.getPathSegments().get(3));
				return new long[] { folderId, puzzleId };
			default:
				return null;
		}
	}

	@Override
	public boolean onCreate() {
		db = new AndokuDatabase(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri) {
		int match = MATCHER.match(uri);
		switch (match) {
			case CODE_FOLDERS:
				return "vnd.android.cursor.dir/vnd.com.googlecode.andoku.folder";
			case CODE_FOLDERS_ID:
				return "vnd.android.cursor.item/vnd.com.googlecode.andoku.folder";
			case CODE_PUZZLES:
				return "vnd.android.cursor.dir/vnd.com.googlecode.andoku.puzzle";
			case CODE_PUZZLES_ID:
				return "vnd.android.cursor.item/vnd.com.googlecode.andoku.puzzle";
			default:
				return null;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		if (Constants.LOG_V)
			Log.v(TAG, "query(" + uri + ", " + Arrays.toString(projection) + ", " + selection + ", "
					+ Arrays.toString(selectionArgs) + ", " + sortOrder + ")");

		int match = MATCHER.match(uri);
		switch (match) {
			case CODE_FOLDERS: {
				long parentId = db.getOrCreateFolder(Constants.IMPORTED_PUZZLES_FOLDER);
				return queryFolder(parentId, projection, selection, selectionArgs, sortOrder);
			}
			case CODE_FOLDERS_ID: {
				long parentId = Long.parseLong(uri.getPathSegments().get(1));
				return queryFolder(parentId, projection, selection, selectionArgs, sortOrder);
			}
			case CODE_PUZZLES:
				throw new UnsupportedOperationException();
			case CODE_PUZZLES_ID:
				throw new UnsupportedOperationException();
			default:
				throw new UnsupportedOperationException();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
		if (Constants.LOG_V)
			Log.v(TAG, "bulkInsert(" + uri + ", " + valuesArray.length + ")");

		db.beginTransaction();
		try {
			bulkInsert0(uri, valuesArray);

			db.setTransactionSuccessful();

			return valuesArray.length;
		}
		finally {
			db.endTransaction();
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (Constants.LOG_V)
			Log.v(TAG, "insert(" + uri + ", " + values + ")");

		int match = MATCHER.match(uri);
		switch (match) {
			case CODE_FOLDERS:
				return insertFolder(uri, values);
			case CODE_PUZZLES:
				return insertPuzzle(uri, values);
			default:
				throw new InvalidParameterException("Invalid URI: " + uri);
		}
	}

	private Cursor queryFolder(long parentId, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(AndokuDatabase.TABLE_FOLDERS);
		qb.setProjectionMap(FOLDERS_PROJECTION_MAP);
		qb.appendWhere(AndokuDatabase.COL_FOLDER_PARENT + "=" + parentId);

		String orderBy = TextUtils.isEmpty(sortOrder)
				? AndokuDatabase.COL_FOLDER_NAME + " asc"
				: sortOrder;

		return db.query(qb, projection, selection, selectionArgs, orderBy);
	}

	private void bulkInsert0(Uri uri, ContentValues[] valuesArray) {
		int match = MATCHER.match(uri);
		switch (match) {
			case CODE_FOLDERS:
				bulkInsertFolder(uri, valuesArray);
				break;
			case CODE_PUZZLES:
				bulkInsertPuzzle(uri, valuesArray);
				break;
			default:
				throw new InvalidParameterException("Invalid URI: " + uri);
		}
	}

	private void bulkInsertFolder(Uri uri, ContentValues[] valuesArray) {
		for (ContentValues values : valuesArray) {
			insertFolder(uri, values);
		}
	}

	private void bulkInsertPuzzle(Uri uri, ContentValues[] valuesArray) {
		long folderId = parseFolderId(uri);

		for (ContentValues values : valuesArray) {
			insertPuzzle(uri, values, folderId);
		}
	}

	private Uri insertFolder(Uri uri, ContentValues values) {
		String path = values.getAsString(KEY_PATH);
		if (path == null)
			throw new InvalidParameterException("Missing path");

		String[] segments = path.split("/");
		if (!isValidPath(segments))
			throw new InvalidParameterException("Invalid path: " + path);

		long parentId = db.getOrCreateFolder(Constants.IMPORTED_PUZZLES_FOLDER);

		for (int i = 0; i < segments.length - 1; i++) {
			String segment = segments[i];
			parentId = db.getOrCreateFolder(parentId, segment);
		}

		String lastSegment = segments[segments.length - 1];
		if (db.folderExists(parentId, lastSegment)) {
			return null;
		}
		else {
			parentId = db.createFolder(parentId, lastSegment);

			Uri folderUri = ContentUris.withAppendedId(uri, parentId);
			getContext().getContentResolver().notifyChange(folderUri, null);
			return folderUri;
		}
	}

	private boolean isValidPath(String[] segments) {
		if (segments.length == 0)
			return false;

		for (String segment : segments) {
			if (segment.length() == 0)
				return false;
		}

		return true;
	}

	private Uri insertPuzzle(Uri uri, ContentValues values) {
		long folderId = parseFolderId(uri);

		return insertPuzzle(uri, values, folderId);
	}

	private long parseFolderId(Uri uri) {
		long folderId = Long.parseLong(uri.getPathSegments().get(1));

		if (!db.folderExists(folderId))
			throw new InvalidParameterException("No such folder: " + folderId);

		return folderId;
	}

	private Uri insertPuzzle(Uri uri, ContentValues values, long folderId) {
		PuzzleInfo puzzleInfo = createPuzzleInfo(values);
		long puzzleId = db.insertPuzzle(folderId, puzzleInfo);

		Uri puzzleUri = ContentUris.withAppendedId(uri, puzzleId);
		getContext().getContentResolver().notifyChange(puzzleUri, null);
		return puzzleUri;
	}

	// TODO: support sizes != 9x9
	private PuzzleInfo createPuzzleInfo(ContentValues values) {
		String clues = values.getAsString(KEY_CLUES);
		if (clues == null)
			throw new InvalidParameterException("Missing clues");

		clues = clues.trim().replace('0', '.');
		if (!isValidClues(clues))
			throw new InvalidParameterException("Invalid clues: " + clues);

		PuzzleInfo.Builder builder = new PuzzleInfo.Builder(clues);

		String name = values.getAsString(KEY_NAME);
		if (name != null) {
			name = name.trim();
			if (isValidName(name))
				builder.setName(name);
			else
				throw new InvalidParameterException("Invalid name: " + name);
		}

		String areas = values.getAsString(KEY_AREAS);
		if (areas != null) {
			areas = areas.trim();
			if (isValidAreas(clues, areas))
				builder.setAreas(areas);
			else
				throw new InvalidParameterException("Invalid areas: " + areas);
		}

		String extraRegions = values.getAsString(KEY_EXTRA_REGIONS);
		if (extraRegions != null) {
			extraRegions = extraRegions.trim();
			if (isValidExtraRegions(extraRegions))
				builder.setExtraRegions(extraRegions);
			else
				throw new InvalidParameterException("Invalid extra regions: " + extraRegions);
		}

		Integer difficulty = values.getAsInteger(KEY_DIFFICULTY);
		if (difficulty != null) {
			if (isValidDifficulty(difficulty))
				builder.setDifficulty(Difficulty.values()[difficulty]);
			else
				throw new InvalidParameterException("Invalid difficulty: " + difficulty);
		}

		return builder.build();
	}

	private boolean isValidClues(String clues) {
		final int length = clues.length();

		final int size = (int) Math.sqrt(length);
		if (length != size * size)
			return false;

		if (size != 9)
			return false;

		for (int i = 0; i < length; i++) {
			char c = clues.charAt(i);
			if (c != '.' && (c < '1' || c > '9'))
				return false;
		}

		return true;
	}

	private boolean isValidName(String name) {
		return true;
	}

	private boolean isValidAreas(String clues, String areas) {
		final int length = areas.length();

		if (length == 0)
			return true;

		if (length != clues.length())
			return false;

		for (int i = 0; i < length; i++) {
			char c = areas.charAt(i);
			if (c < '1' || c > '9')
				return false;
		}

		return true;
	}

	private boolean isValidExtraRegions(String extraRegions) {
		return extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_HYPER)
				|| extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_X)
				|| extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_NONE);
	}

	private boolean isValidDifficulty(Integer difficulty) {
		return difficulty >= 0 && difficulty < Difficulty.values().length;
	}
}
