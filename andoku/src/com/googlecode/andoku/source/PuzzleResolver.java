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

public class PuzzleResolver {
	private static final HashMap<String, AssetsPuzzleSource> ASSET_SOURCES = new HashMap<String, AssetsPuzzleSource>();

	private PuzzleResolver() {
	}

	public static boolean exists(Context context, String puzzleSource, int puzzleNumber)
			throws PuzzleIOException {
		if (puzzleSource.startsWith(AssetsPuzzleSource.ASSET_PREFIX))
			return existsAsset(context, puzzleSource.substring(AssetsPuzzleSource.ASSET_PREFIX
					.length()), puzzleNumber);

		throw new IllegalArgumentException();
	}

	private static boolean existsAsset(Context context, String puzzleSet, int puzzleNumber)
			throws PuzzleIOException {
		AssetsPuzzleSource source = getAssetSource(context.getAssets(), puzzleSet);
		return puzzleNumber >= 0 && puzzleNumber < source.numberOfPuzzles();
	}

	public static PuzzleHolder resolve(Context context, String puzzleId) throws PuzzleIOException {
		if (puzzleId.startsWith(AssetsPuzzleSource.ASSET_PREFIX))
			return restoreAsset(context, puzzleId.substring(AssetsPuzzleSource.ASSET_PREFIX.length()));

		throw new IllegalArgumentException();
	}

	private static PuzzleHolder restoreAsset(Context context, String id) throws PuzzleIOException {
		int idx = id.lastIndexOf(PuzzleHolder.NUMBER_SEPARATOR);
		String puzzleSet = id.substring(0, idx);
		int puzzleNumber = Integer.parseInt(id.substring(idx + 1));

		AssetsPuzzleSource source = getAssetSource(context.getAssets(), puzzleSet);
		return source.load(puzzleNumber);
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
