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

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.db.PuzzleInfo;
import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Solution;
import com.googlecode.andoku.transfer.PuzzleDecoder;

class DbPuzzleSource implements PuzzleSource {
	private final AndokuDatabase db;
	private final long folderId;

	public DbPuzzleSource(AndokuDatabase db, long folderId) {
		this.db = db;
		this.folderId = folderId;
	}

	public String getSourceId() {
		return PuzzleSourceIds.forDbFolder(folderId);
	}

	public PuzzleHolder load(int number) throws PuzzleIOException {
		PuzzleInfo puzzleInfo = db.loadPuzzle(folderId, number);
		if (puzzleInfo == null)
			throw new PuzzleIOException("Puzzle " + number + " not found in folder " + folderId);

		Difficulty difficulty = puzzleInfo.getDifficulty();
		Puzzle puzzle = createPuzzle(puzzleInfo);
		Solution solution = createSolution(puzzleInfo);

		return new PuzzleHolder(this, number, difficulty, puzzle, solution);
	}

	public int numberOfPuzzles() {
		return db.getNumberOfPuzzles(folderId);
	}

	public void close() {
		db.close();
	}

	private Puzzle createPuzzle(PuzzleInfo puzzleInfo) {
		return PuzzleDecoder.decode(puzzleInfo.getClues() + "|" + puzzleInfo.getAreas() + "|"
				+ puzzleInfo.getExtraRegions());
	}

	private Solution createSolution(PuzzleInfo puzzleInfo) {
		return PuzzleDecoder.decodeValues(puzzleInfo.getSolution());
	}
}
