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

package com.googlecode.andoku.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleType;

public class AndokuPuzzle {
	private final int size;
	private final Puzzle puzzle;
	private final PuzzleType puzzleType;
	private final boolean[][] extra;
	private final int[][] solution;
	private boolean solved;

	private int[][] areaColors = null;

	private ValueSet[][] values;
	private int numValues;

	// multiple identical values within a single region
	private HashSet<RegionError> regionErrors;

	// errors compared to actual solution; correct value has been eliminated
	private HashSet<Position> cellErrors;

	private AndokuPuzzle(Puzzle puzzle, PuzzleType puzzleType, int[][] solution) {
		this.size = puzzle.getSize();
		this.puzzle = puzzle;
		this.puzzleType = puzzleType;
		this.extra = obtainExtra(puzzle);
		this.solution = solution;
		this.values = obtainValues(puzzle);
		this.numValues = countValues(this.values);
		this.solved = checkSolved();
		this.regionErrors = new HashSet<RegionError>();
		this.cellErrors = new HashSet<Position>();
	}

	public static AndokuPuzzle create(PuzzleHolder puzzleHolder) {
		return new AndokuPuzzle(puzzleHolder.getPuzzle(), puzzleHolder.getPuzzleType(), puzzleHolder
				.getSolution());
	}

	public Serializable saveToMemento() {
		PuzzleMemento memento = new PuzzleMemento(hash(puzzle, solution), copyValues(values),
				copyRegionErrors(regionErrors), copyCellErrors(cellErrors));

		// memento = SerializableUtil.roundTrip(memento);

		return memento;
	}

	public boolean restoreFromMemento(Object object) {
		if (!(object instanceof PuzzleMemento))
			return false;

		PuzzleMemento memento = (PuzzleMemento) object;
		if (memento.values.length != values.length)
			return false;

		if (memento.hash != hash(puzzle, solution))
			return false;

		this.values = copyValues(memento.values);
		this.numValues = countValues(this.values);
		this.solved = checkSolved();
		this.regionErrors = copyRegionErrors(memento.regionErrors);
		this.cellErrors = copyCellErrors(memento.cellErrors);

		return true;
	}

	public PuzzleType getPuzzleType() {
		return puzzleType;
	}

	public boolean isSolved() {
		return solved;
	}

	public int getSize() {
		return size;
	}

	public boolean isModified() {
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (!isClue(row, col) && !values[row][col].isEmpty())
					return true;
			}
		}

		return false;
	}

	public boolean isCompletelyFilled() {
		return numValues == size * size;
	}

	public int getMissingValuesCount() {
		return size * size - numValues;
	}

	public boolean isClue(int row, int col) {
		return puzzle.getValue(row, col) != Puzzle.UNDEFINED;
	}

	public boolean isExtraRegion(int row, int col) {
		return extra[row][col];
	}

	public int getAreaCode(int row, int col) {
		return puzzle.getAreaCode(row, col);
	}

	public int getAreaColor(int row, int col) {
		if (areaColors == null) {
			areaColors = new AreaColorGenerator(puzzle).generate();
		}

		return areaColors[row][col];
	}

	public ValueSet getValues(int row, int col) {
		return new ValueSet(values[row][col]);
	}

	public boolean setValues(int row, int col, ValueSet valueSet) {
		if (values[row][col].equals(valueSet))
			return false;

		boolean singleValueBefore = values[row][col].size() == 1;
		values[row][col].setFromInt(valueSet.toInt());
		boolean singleValueAfter = values[row][col].size() == 1;

		if (singleValueBefore && !singleValueAfter)
			numValues--;
		if (!singleValueBefore && singleValueAfter)
			numValues++;

		solved = checkSolved();

		Position p = new Position(row, col);
		if (removeError(p))
			return true;

		return false;
	}

	private boolean checkSolved() {
		if (!isCompletelyFilled())
			return false;

		Position[] positionOfValue = new Position[size];

		for (Region region : puzzle.getRegions()) {
			for (int value = 0; value < size; value++)
				positionOfValue[value] = null;

			for (Position p : region.positions) {
				int value = values[p.row][p.col].nextValue(0);
				if (positionOfValue[value] != null)
					return false;

				positionOfValue[value] = p;
			}
		}

		return true;
	}

	public boolean checkForErrors() {
		clearErrors();

		// check for duplicate values within a region

		Position[] positionOf = new Position[size];

		for (Region region : puzzle.getRegions()) {
			for (int value = 0; value < size; value++)
				positionOf[value] = null;

			for (Position p : region.positions) {
				ValueSet values = this.values[p.row][p.col];
				if (values.size() == 1) {
					int value = values.nextValue(0);
					if (positionOf[value] != null) {
						addErrorLink(positionOf[value], p);
						continue;
					}

					positionOf[value] = p;
				}
			}
		}

		// compare values to actual solution

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				ValueSet values = this.values[row][col];
				if (!values.isEmpty() && !values.contains(solution[row][col])) {
					cellErrors.add(new Position(row, col));
				}
			}
		}

		return hasErrors();
	}

	public boolean hasErrors() {
		return !regionErrors.isEmpty() || !cellErrors.isEmpty();
	}

	public HashSet<RegionError> getRegionErrors() {
		return regionErrors;
	}

	public boolean isErrorPosition(int row, int col) {
		for (RegionError error : regionErrors) {
			if (error.p1.row == row && error.p1.col == col)
				return true;
			if (error.p2.row == row && error.p2.col == col)
				return true;
		}

		for (Position p : cellErrors) {
			if (p.row == row && p.col == col)
				return true;
		}

		return false;
	}

	public HashSet<Position> getErrorPositions() {
		HashSet<Position> positions = new HashSet<Position>();

		for (RegionError error : regionErrors) {
			positions.add(error.p1);
			positions.add(error.p2);
		}

		positions.addAll(cellErrors);

		return positions;
	}

	private void addErrorLink(Position from, Position to) {
		regionErrors.add(new RegionError(from, to));
	}

	private boolean removeError(Position position) {
		cellErrors.remove(position);

		if (regionErrors.isEmpty())
			return false;

		boolean removed = false;

		for (Iterator<RegionError> iterator = regionErrors.iterator(); iterator.hasNext();) {
			RegionError error = iterator.next();
			if (position.equals(error.p1) || position.equals(error.p2)) {
				iterator.remove();
				removed = true;
			}
		}

		return removed;
	}

	private void clearErrors() {
		regionErrors.clear();
		cellErrors.clear();
	}

	private static int hash(Puzzle puzzle, int[][] solution) {
		int hash = puzzle.getSize();

		final int size = puzzle.getSize();
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				hash = 37 * hash + solution[row][col];

				hash = 37 * hash + puzzle.getAreaCode(row, col);

				if (puzzle.getValue(row, col) != Puzzle.UNDEFINED)
					hash += 17;
			}
		}

		return hash;
	}

	private static boolean[][] obtainExtra(Puzzle puzzle) {
		final int size = puzzle.getSize();

		boolean[][] extra = new boolean[size][size];
		for (ExtraRegion extraRegion : puzzle.getExtraRegions()) {
			for (Position position : extraRegion.positions) {
				extra[position.row][position.col] = true;
			}
		}

		return extra;
	}

	private static ValueSet[][] obtainValues(Puzzle puzzle) {
		final int size = puzzle.getSize();

		ValueSet[][] values = new ValueSet[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				values[row][col] = new ValueSet();
				int value = puzzle.getValue(row, col);
				if (value != Puzzle.UNDEFINED)
					values[row][col].add(value);
			}
		}

		return values;
	}

	private static int countValues(ValueSet[][] values) {
		final int size = values.length;

		int count = 0;
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (values[row][col].size() == 1)
					count++;
			}
		}
		return count;
	}

	private static ValueSet[][] copyValues(ValueSet[][] orig) {
		int size = orig.length;

		ValueSet[][] copy = new ValueSet[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				copy[row][col] = new ValueSet(orig[row][col]);
			}
		}

		return copy;
	}

	private static HashSet<RegionError> copyRegionErrors(HashSet<RegionError> orig) {
		return new HashSet<RegionError>(orig);
	}

	private HashSet<Position> copyCellErrors(HashSet<Position> orig) {
		return new HashSet<Position>(orig);
	}

	private static final class PuzzleMemento implements Externalizable {
		private static final long serialVersionUID = -7495554868028722997L;

		public int hash;
		public ValueSet[][] values;
		public HashSet<RegionError> regionErrors;
		public HashSet<Position> cellErrors;

		public PuzzleMemento() {
		}

		public PuzzleMemento(int hash, ValueSet[][] values, HashSet<RegionError> regionErrors,
				HashSet<Position> cellErrors) {
			this.hash = hash;
			this.values = values;
			this.regionErrors = regionErrors;
			this.cellErrors = cellErrors;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeByte(3); // version

			out.writeInt(hash);
			writeValues(out, values);
			writeRegionErrors(out, regionErrors);
			writeCellErrors(out, cellErrors);
		}

		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			byte version = in.readByte();
			if (version != 2 && version != 3)
				throw new IOException("invalid version");

			hash = in.readInt();
			values = readValues(in);
			regionErrors = readRegionErrors(in);
			cellErrors = version >= 3 ? readCellErrors(in) : new HashSet<Position>();
		}

		private void writeValues(ObjectOutput out, ValueSet[][] values) throws IOException {
			final int size = values.length;
			out.writeChar(size);

			for (int row = 0; row < size; row++) {
				final ValueSet[] v = values[row];
				for (int col = 0; col < size; col++) {
					out.writeChar(v[col].toInt());
				}
			}
		}

		private ValueSet[][] readValues(ObjectInput in) throws IOException {
			final int size = in.readChar();
			ValueSet[][] values = new ValueSet[size][size];

			for (int row = 0; row < size; row++) {
				final ValueSet[] v = values[row];
				for (int col = 0; col < size; col++) {
					v[col] = new ValueSet(in.readChar());
				}
			}

			return values;
		}

		private void writeRegionErrors(ObjectOutput out, HashSet<RegionError> errors)
				throws IOException {
			final int numErrors = errors.size();
			out.writeChar(numErrors);

			for (RegionError error : errors) {
				out.writeChar(error.p1.row);
				out.writeChar(error.p1.col);
				out.writeChar(error.p2.row);
				out.writeChar(error.p2.col);
			}
		}

		private HashSet<RegionError> readRegionErrors(ObjectInput in) throws IOException {
			final int numErrors = in.readChar();
			HashSet<RegionError> errors = new HashSet<RegionError>(numErrors);

			for (int i = 0; i < numErrors; i++) {
				Position p1 = new Position(in.readChar(), in.readChar());
				Position p2 = new Position(in.readChar(), in.readChar());
				errors.add(new RegionError(p1, p2));
			}

			return errors;
		}

		private void writeCellErrors(ObjectOutput out, HashSet<Position> errors) throws IOException {
			final int numErrors = errors.size();
			out.writeChar(numErrors);

			for (Position p : errors) {
				out.writeChar(p.row);
				out.writeChar(p.col);
			}
		}

		private HashSet<Position> readCellErrors(ObjectInput in) throws IOException {
			final int numErrors = in.readChar();
			HashSet<Position> errors = new HashSet<Position>(numErrors);

			for (int i = 0; i < numErrors; i++) {
				Position p = new Position(in.readChar(), in.readChar());
				errors.add(p);
			}

			return errors;
		}
	}
}
