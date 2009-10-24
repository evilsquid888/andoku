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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.content.res.AssetManager;

import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Solution;
import com.googlecode.andoku.transfer.PuzzleDecoder;

class AssetsPuzzleSource implements PuzzleSource {
	static final String ASSET_PREFIX = "asset:";

	private static final String PUZZLES_FOLDER = "puzzles/";

	private final AssetManager assets;
	private final String puzzleSet;

	private final int[] index;

	public AssetsPuzzleSource(AssetManager assets, String puzzleSet) throws PuzzleIOException {
		this.assets = assets;
		this.puzzleSet = puzzleSet;

		this.index = loadIndex();
	}

	public String getSourceId() {
		return ASSET_PREFIX + puzzleSet;
	}

	public int numberOfPuzzles() {
		return index.length;
	}

	public PuzzleHolder load(int number) throws PuzzleIOException {
		String[] loaded = loadPuzzle(number);

		Puzzle puzzle;
		Solution solution;
		try {
			puzzle = PuzzleDecoder.decode(loaded[0]);
			solution = PuzzleDecoder.decodeValues(loaded[1]);
		}
		catch (IllegalArgumentException e) {
			throw new PuzzleIOException("Invalid puzzle", e);
		}

		return new PuzzleHolder(this, number, getDifficulty(), puzzle, solution);
	}

	public void close() {
	}

	private int[] loadIndex() throws PuzzleIOException {
		try {
			String indexFile = PUZZLES_FOLDER + puzzleSet + ".idx";

			InputStream in = assets.open(indexFile);
			try {
				DataInputStream din = new DataInputStream(in);
				int size = din.readInt();
				int[] offsets = new int[size];
				for (int idx = 0; idx < size; idx++) {
					offsets[idx] = din.readInt();
				}
				return offsets;
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new PuzzleIOException(e);
		}
	}

	private String[] loadPuzzle(int number) throws PuzzleIOException {
		try {
			String puzzleFile = PUZZLES_FOLDER + puzzleSet + ".adk";
			int offset = index[number];

			InputStream in = assets.open(puzzleFile);
			try {
				skipFully(in, offset);
				Reader reader = new InputStreamReader(in, "US-ASCII");
				BufferedReader br = new BufferedReader(reader, 512);
				String puzzleRep = br.readLine();
				String solutionRep = br.readLine();

				return new String[] { puzzleRep, solutionRep };
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new PuzzleIOException(e);
		}
	}

	private Difficulty getDifficulty() {
		final int difficulty = puzzleSet.charAt(puzzleSet.length() - 1) - '0' - 1;
		if (difficulty < 0 || difficulty > 4)
			throw new IllegalStateException();

		return Difficulty.values()[difficulty];
	}

	private void skipFully(InputStream in, int bytes) throws IOException {
		while (bytes > 0) {
			bytes -= skip(in, bytes);
		}
	}

	private long skip(InputStream in, int bytes) throws IOException {
		long skipped = in.skip(bytes);
		if (skipped > 0)
			return skipped;

		if (in.read() != -1)
			return 1;

		throw new EOFException();
	}
}
