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

package com.googlecode.andoku.source;

import android.content.Context;
import android.content.res.AssetManager;

import com.googlecode.andoku.db.AndokuDatabase;

public class PuzzleSourceResolver {
	private PuzzleSourceResolver() {
	}

	public static PuzzleSource resolveSource(Context context, String puzzleSourceId)
			throws PuzzleIOException {
		if (PuzzleSourceIds.isAssetSource(puzzleSourceId))
			return resolveAssetSource(context, PuzzleSourceIds.getAssetFolderName(puzzleSourceId));

		if (PuzzleSourceIds.isDbSource(puzzleSourceId))
			return resolveDbSource(context, PuzzleSourceIds.getDbFolderId(puzzleSourceId));

		throw new IllegalArgumentException(puzzleSourceId);
	}

	private static PuzzleSource resolveAssetSource(Context context, String folderName)
			throws PuzzleIOException {
		AssetManager assets = context.getAssets();
		return new AssetsPuzzleSource(assets, folderName);
	}

	private static PuzzleSource resolveDbSource(Context context, long folderId)
			throws PuzzleIOException {
		AndokuDatabase db = new AndokuDatabase(context);
		return new DbPuzzleSource(db, folderId);
	}
}
