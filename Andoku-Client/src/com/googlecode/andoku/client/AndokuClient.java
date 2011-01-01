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

package com.googlecode.andoku.client;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Client code that allows external apps to import puzzles into Andoku.
 */
public class AndokuClient {
	public static final String PACKAGE_NAME = "com.googlecode.andoku";

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

	private static final String COL_ID = BaseColumns._ID;
	private static final String COL_FOLDER_NAME = "name";

	private static final String OPEN_PUZZLE_CLASS = PACKAGE_NAME + ".OpenImportedPuzzleActivity";
	private static final String EXTRA_PUZZLE_URI = PACKAGE_NAME + ".puzzleUri";
	private static final String EXTRA_START_PUZZLE = PACKAGE_NAME + ".start";

	private final ContentResolver contentResolver;
	private final PackageManager packageManager;

	public AndokuClient(Context context) {
		contentResolver = context.getContentResolver();
		packageManager = context.getPackageManager();
	}

	/**
	 * Checks if a compatible version of Andoku is installed.
	 */
	public boolean isCompatibleAndokuVersionInstalled() {
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(PACKAGE_NAME, 0);
			return packageInfo.versionCode >= 4;
		}
		catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns an intent that can be used to open the specified puzzle in Andoku.
	 * 
	 * @param puzzleUri puzzle URI as returned by insertPuzzle().
	 * @param startPuzzle immediately start the game (true) or merely open its folder (false).
	 */
	public Intent getOpenPuzzleIntent(Uri puzzleUri, boolean startPuzzle) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName(PACKAGE_NAME, OPEN_PUZZLE_CLASS);
		intent.putExtra(EXTRA_PUZZLE_URI, puzzleUri);
		intent.putExtra(EXTRA_START_PUZZLE, startPuzzle);
		return intent;
	}

	/**
	 * Returns the URI of the specified folder if it already exists or the URI of a newly created
	 * folder otherwise.
	 * <p>
	 * Forward slashes in the path can be used to address sub-folders.
	 */
	public Uri getOrCreateFolder(String path) {
		Uri folderUri = getFolder(path);
		if (folderUri != null)
			return folderUri;
		else
			return createFolder(path);
	}

	/**
	 * Returns the URI for the specified folder or <code>null</code> if it does not exist.
	 */
	public Uri getFolder(String path) {
		Long parentId = null;

		String[] segments = path.split("/");
		if (segments.length == 0)
			throw new IllegalArgumentException();

		for (String segment : segments) {
			Uri uri = parentId == null ? CONTENT_URI : ContentUris.withAppendedId(CONTENT_URI,
					parentId);
			String[] projection = { COL_ID };
			String selection = COL_FOLDER_NAME + "=?";
			String[] selectionArgs = { segment };
			String sortOrder = null;

			Cursor query = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

			if (query.moveToFirst())
				parentId = query.getLong(0);
			else
				return null;
		}

		return ContentUris.withAppendedId(CONTENT_URI, parentId);
	}

	/**
	 * Creates a new folder with a unique name. Uniqueness is ensured by appending " (1)", " (2)",
	 * and so on, to the specified path until a new folder with that path can be created.
	 */
	public UriAndPath createUniqueFolder(String path) {
		for (int i = 1;; i++) {
			String uniquePath = i == 1 ? path : path + " (" + i + ")";

			Uri folderUri = createFolder(uniquePath);
			if (folderUri != null)
				return new UriAndPath(folderUri, uniquePath);
		}
	}

	/**
	 * Creates a folder for the specified path and returns its URI. Returns <code>null</code> if the
	 * specified folder already exists.
	 */
	public Uri createFolder(String path) {
		ContentValues values = new ContentValues();
		values.put(KEY_PATH, path);

		return contentResolver.insert(CONTENT_URI, values);
	}

	/**
	 * Inserts a single puzzle into the specified folder.
	 */
	public Uri insertPuzzle(Uri folderUri, ContentValues values) {
		Uri puzzlesUri = Uri.withAppendedPath(folderUri, PATH_PUZZLES);

		return contentResolver.insert(puzzlesUri, values);
	}

	/**
	 * Bulk insert method to insert a list of puzzles into the specified folder.
	 */
	public Uri insertPuzzles(Uri folderUri, List<ContentValues> valuesList) {
		return insertPuzzles0(folderUri, valuesList);
	}

	/**
	 * Bulk insert method that also registers a ContentObserver.
	 */
	public Uri insertPuzzles(Uri folderUri, List<ContentValues> valuesList, ContentObserver observer) {
		if (observer != null)
			contentResolver.registerContentObserver(folderUri, true, observer);

		try {
			return insertPuzzles0(folderUri, valuesList);
		}
		finally {
			if (observer != null)
				contentResolver.unregisterContentObserver(observer);
		}
	}

	private Uri insertPuzzles0(Uri folderUri, List<ContentValues> valuesList) {
		if (valuesList.isEmpty())
			throw new IllegalArgumentException();

		Uri puzzlesUri = Uri.withAppendedPath(folderUri, PATH_PUZZLES);

		ContentValues first = valuesList.get(0);
		Uri puzzleUri = contentResolver.insert(puzzlesUri, first);

		final int size = valuesList.size();
		final int puzzlesPerBulkInsert = 100;
		for (int idx = 1; idx < size; idx += puzzlesPerBulkInsert) {
			int end = Math.min(idx + puzzlesPerBulkInsert, size);

			List<ContentValues> subList = valuesList.subList(idx, end);
			ContentValues[] valuesArray = subList.toArray(new ContentValues[subList.size()]);
			contentResolver.bulkInsert(puzzlesUri, valuesArray);
		}

		return puzzleUri;
	}
}
