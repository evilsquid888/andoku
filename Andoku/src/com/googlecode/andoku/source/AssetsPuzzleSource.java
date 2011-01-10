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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;

import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.transfer.PuzzleDecoder;

class AssetsPuzzleSource implements PuzzleSource {
	private static final String PUZZLES_FOLDER = "puzzles/";

	private final AssetManager assets;
	private final String folderName;

	private final List<String> entries;

	public AssetsPuzzleSource(AssetManager assets, String folderName) {
		this.assets = assets;
		this.folderName = folderName;

		this.entries = loadEntries();
	}

	private List<String> loadEntries() {
		List<String> entries = new ArrayList<String>(100);

		try {
			String puzzleFile = PUZZLES_FOLDER + folderName + ".adk";

			InputStream in = assets.open(puzzleFile);
			try {
				Reader reader = new InputStreamReader(in, "US-ASCII");
				BufferedReader br = new BufferedReader(reader, 512);
				while (true) {
					String line = br.readLine();
					if (line == null)
						break;

					if (line.length() == 0 || line.startsWith("#"))
						continue;
						
						entries.add(line);
				}

				return entries;
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new AssetsPuzzleSourceException(e);
		}
	}

	public String getSourceId() {
		return PuzzleSourceIds.forAssetFolder(folderName);
	}

	public int numberOfPuzzles() {
		return entries.size();
	}

	public PuzzleHolder load(int number) {
		String puzzleStr = entries.get(number);

		Puzzle puzzle = PuzzleDecoder.decode(puzzleStr);

		return new PuzzleHolder(this, number, null, puzzle, getDifficulty());
	}

	private Difficulty getDifficulty() {
		final int difficulty = folderName.charAt(folderName.length() - 1) - '0' - 1;
		if (difficulty < 0 || difficulty > 4)
			throw new IllegalStateException();

		return Difficulty.values()[difficulty];
	}

	public void close() {
	}
}
