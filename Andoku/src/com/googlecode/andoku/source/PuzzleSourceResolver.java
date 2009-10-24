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

package com.googlecode.andoku.source;

import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;

public class PuzzleSourceResolver {
	private static final HashMap<String, AssetsPuzzleSource> ASSET_SOURCES = new HashMap<String, AssetsPuzzleSource>();

	private PuzzleSourceResolver() {
	}

	public static PuzzleSource resolveSource(Context context, String puzzleSourceId)
			throws PuzzleIOException {
		if (puzzleSourceId.startsWith(AssetsPuzzleSource.ASSET_PREFIX))
			return restoreAssetSource(context, puzzleSourceId
					.substring(AssetsPuzzleSource.ASSET_PREFIX.length()));

		throw new IllegalArgumentException(puzzleSourceId);
	}

	private static PuzzleSource restoreAssetSource(Context context, String puzzleSet)
			throws PuzzleIOException {
		return getAssetSource(context.getAssets(), puzzleSet);
	}

	private static AssetsPuzzleSource getAssetSource(AssetManager assets, String puzzleSet)
			throws PuzzleIOException {
		synchronized (ASSET_SOURCES) {
			AssetsPuzzleSource source = ASSET_SOURCES.get(puzzleSet);
			if (source == null) {
				source = new AssetsPuzzleSource(assets, puzzleSet);
				ASSET_SOURCES.put(puzzleSet, source);
			}

			return source;
		}
	}
}
