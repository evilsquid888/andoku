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

package com.googlecode.andoku.source;

public class PuzzleSourceIds {
	private static final char SEPARATOR = ':';
	private static final String ASSET_PREFIX = "asset" + SEPARATOR;
	private static final String DB_PREFIX = "db" + SEPARATOR;

	private PuzzleSourceIds() {
	}

	public static String forAssetFolder(String folderName) {
		return ASSET_PREFIX + folderName;
	}

	public static String forDbFolder(long folderId) {
		return DB_PREFIX + folderId;
	}

	public static boolean isAssetSource(String puzzleSourceId) {
		return puzzleSourceId.startsWith(ASSET_PREFIX);
	}

	public static String getAssetFolderName(String puzzleSourceId) {
		return puzzleSourceId.substring(ASSET_PREFIX.length());
	}

	public static boolean isDbSource(String puzzleSourceId) {
		return puzzleSourceId.startsWith(DB_PREFIX);
	}

	public static long getDbFolderId(String puzzleSourceId) {
		return Long.parseLong(puzzleSourceId.substring(DB_PREFIX.length()));
	}
}
