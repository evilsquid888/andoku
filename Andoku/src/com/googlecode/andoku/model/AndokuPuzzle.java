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

package com.googlecode.andoku.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.util.Log;

import com.googlecode.andoku.solver.DlxPuzzleSolver;
import com.googlecode.andoku.solver.PuzzleSolver;
import com.googlecode.andoku.solver.SingleSolutionReporter;
import com.googlecode.andoku.transfer.StandardAreas;

public class AndokuPuzzle {
	private static final String TAG = AndokuPuzzle.class.getName();

	private static final short MAGIC_BYTE_ARRAY = (short) 0xaa2b;
	private static final short MAGIC_SERIALIZABLE = (short) 0xaced;

	private final String name;
	private final int size;
	private final Puzzle problem;
	private final PuzzleType puzzleType;
	private final Difficulty difficulty;
	private final int[][] extra;

	private ValueSet[][] values;

	private final int[] areaColors;
	private final int numberOfAreaColors;

	private Solution solution;
	private boolean computeSolutionFailed = false;

	private Integer numValuesSet;

	private Boolean solved;

	// multiple identical values within a single region
	private HashSet<RegionError> regionErrors;

	// errors compared to actual solution; correct value has been eliminated
	private HashSet<Position> cellErrors;

	private boolean restored = false;

	public AndokuPuzzle(String name, Puzzle puzzle, Difficulty difficulty) {
		if (puzzle == null)
			throw new IllegalArgumentException();
		if (difficulty == null)
			throw new IllegalArgumentException();

		this.name = name;
		this.size = puzzle.getSize();
		this.problem = puzzle;
		this.puzzleType = determinePuzzleType(puzzle);
		this.difficulty = difficulty;
		this.extra = obtainExtra(puzzle);
		this.values = obtainValues(puzzle);
		this.areaColors = new AreaColorGenerator().generate(puzzle);
		this.numberOfAreaColors = countNumberOfAreaColors();
		this.regionErrors = new HashSet<RegionError>();
		this.cellErrors = new HashSet<Position>();
	}

	private int countNumberOfAreaColors() {
		int maxColor = -1;
		for (int color : areaColors) {
			maxColor = Math.max(maxColor, color);
		}
		return maxColor + 1;
	}

	public byte[] saveToMemento() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);

		try {
			out.writeShort(MAGIC_BYTE_ARRAY);
			saveVersion5(out);
			out.close();
		}
		catch (IOException e) {
			// should not happen when writing to byte array
			throw new IllegalStateException(e);
		}

		return baos.toByteArray();
	}

	private void saveVersion5(DataOutputStream out) throws IOException {
		out.writeShort(5); // version

		writeValues(out, values);
		writeRegionErrors(out, regionErrors);
		writeCellErrors(out, cellErrors);
	}

	public boolean restoreFromMemento(byte[] b) {
		boolean success = restoreFromMemento0(b);
		if (success)
			restored = true;

		return success;
	}

	private boolean restoreFromMemento0(byte[] b) {
		DataInput in = new DataInputStream(new ByteArrayInputStream(b));
		try {
			short magic = in.readShort();

			switch (magic) {
				case MAGIC_BYTE_ARRAY:
					return restoreFromByteArray(in);
				case MAGIC_SERIALIZABLE:
					return restoreFromSerializable(b);
				default:
					Log.e(TAG, "Unrecognized memento magic: " + magic);
					return false;
			}
		}
		catch (IOException e) {
			Log.e(TAG, "Error restoring memento", e);
			return false;
		}
	}

	private boolean restoreFromByteArray(DataInput in) throws IOException {
		Log.d(TAG, "Restoring memento from byte array (Andoku 1.2.2 and later)");

		short version = in.readShort();

		switch (version) {
			case 5:
				return restoreFromVersion5(in); // Andoku 1.2.2 and later
			default:
				Log.e(TAG, "Invalid memento version: " + version);
				return false;
		}
	}

	private boolean restoreFromVersion5(DataInput in) throws IOException {
		final ValueSet[][] values = readValues(in);
		final HashSet<RegionError> regionErrors = readRegionErrors(in);
		final HashSet<Position> cellErrors = readCellErrors(in);

		return restoreFrom(values, regionErrors, cellErrors);
	}

	private boolean restoreFromSerializable(byte[] b) {
		Log.d(TAG, "Restoring memento from Serializable (Andoku 1.0.0 - 1.2.1)");

		Object object = deserialize(b);

		if (!(object instanceof PuzzleMemento)) {
			Log.e(TAG, "Not a PuzzleMemento");
			return false;
		}

		PuzzleMemento memento = (PuzzleMemento) object;
		return restoreFrom(memento.values, memento.regionErrors, memento.cellErrors);
	}

	private Object deserialize(byte[] blob) {
		blob = Issue30.workaround(blob);

		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			ObjectInputStream oin = new ObjectInputStream(bais);
			Object object = oin.readObject();
			oin.close();
			return object;
		}
		catch (IOException e) {
			Log.e(TAG, "Error deserializing memento", e);
			return null;
		}
		catch (ClassNotFoundException e) {
			Log.e(TAG, "Error deserializing memento", e);
			return null;
		}
	}

	private boolean restoreFrom(final ValueSet[][] values, final HashSet<RegionError> regionErrors,
			final HashSet<Position> cellErrors) {
		if (values.length != this.values.length) {
			Log.e(TAG, "Memento values length incorrect");
			return false;
		}

		this.values = values;
		this.regionErrors = regionErrors;
		this.cellErrors = cellErrors;

		invalidateSolved();

		return true;
	}

	public boolean isRestored() {
		return restored;
	}

	public String getName() {
		return name;
	}

	public PuzzleType getPuzzleType() {
		return puzzleType;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public boolean hasSolution() {
		return solution != null;
	}

	public boolean computeSolution() {
		if (this.solution != null)
			throw new IllegalStateException();

		if (computeSolutionFailed)
			return false;

		SingleSolutionReporter reporter = new SingleSolutionReporter();
		PuzzleSolver solver = new DlxPuzzleSolver();
		solver.solve(problem, reporter);

		Puzzle solution = reporter.getSolution();
		if (solution == null) {
			computeSolutionFailed = true;
			return false;
		}

		this.solution = new Solution(solution);
		return true;
	}

	public boolean isSolved() {
		if (solved == null) {
			solved = checkSolved();
		}

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
		return getMissingValuesCount() == 0;
	}

	public int getMissingValuesCount() {
		if (numValuesSet == null) {
			numValuesSet = countValuesSet();
		}

		return size * size - numValuesSet;
	}

	public boolean isClue(int row, int col) {
		return problem.getValue(row, col) != Puzzle.UNDEFINED;
	}

	public boolean isExtraRegion(int row, int col) {
		return extra[row][col] != -1;
	}

	public int getExtraRegionCode(int row, int col) {
		return extra[row][col];
	}

	public int getAreaCode(int row, int col) {
		return problem.getAreaCode(row, col);
	}

	public int getAreaColor(int row, int col) {
		return areaColors[problem.getAreaCode(row, col)];
	}

	public int getNumberOfAreaColors() {
		return numberOfAreaColors;
	}

	public ValueSet getValues(int row, int col) {
		return new ValueSet(values[row][col]);
	}

	public boolean setValues(int row, int col, ValueSet valueSet) {
		if (values[row][col].equals(valueSet))
			return false;

		values[row][col].setFromInt(valueSet.toInt());

		invalidateSolved();

		Position p = new Position(row, col);
		if (removeError(p))
			return true;

		return false;
	}

	private boolean checkSolved() {
		if (getMissingValuesCount() != 0)
			return false;

		Position[] positionOfValue = new Position[size];

		for (Region region : problem.getRegions()) {
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

	private void invalidateSolved() {
		solved = null;
		numValuesSet = null;
	}

	public boolean checkForErrors(boolean checkAgainstSolution) {
		clearErrors();

		// check for duplicate values within a region

		Position[] positionOf = new Position[size];

		for (Region region : problem.getRegions()) {
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

		// compare values to actual solution (if we have a solution)

		if (checkAgainstSolution && solution != null) {
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					ValueSet values = this.values[row][col];
					if (!values.isEmpty() && !values.contains(solution.getValue(row, col))) {
						cellErrors.add(new Position(row, col));
					}
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

	public HashSet<Position> getCellErrors() {
		return cellErrors;
	}

	public boolean canEliminateValues() {
		Set<Position> positions = getAllPositionsWithASingleValue();

		return canEliminateValues(positions);
	}

	private Set<Position> getAllPositionsWithASingleValue() {
		Set<Position> positions = new HashSet<Position>();

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				if (values[row][col].size() == 1)
					positions.add(new Position(row, col));

		return positions;
	}

	private boolean canEliminateValues(Set<Position> positions) {
		for (Position position : positions) {
			int value = values[position.row][position.col].nextValue(0);

			Region[] regions = problem.getRegionsAt(position.row, position.col);
			for (Region region : regions) {
				for (Position p : region.positions) {
					if (!positions.contains(p) && !isClue(p.row, p.col)) {
						if (canEliminateValue(p, value))
							return true;
					}
				}
			}
		}

		return false;
	}

	private boolean canEliminateValue(Position position, int value) {
		ValueSet currentValues = values[position.row][position.col];
		return currentValues.isEmpty() || currentValues.contains(value);
	}

	public int eliminateValues() {
		setAllValuesOnEmptyPositions();

		Set<Position> positions = getAllPositionsWithASingleValue();

		return eliminateValues(positions);
	}

	private void setAllValuesOnEmptyPositions() {
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				if (values[row][col].isEmpty())
					setValues(row, col, ValueSet.all(size));
	}

	private int eliminateValues(Set<Position> positions) {
		int numberValuesEliminated = 0;

		for (Position position : positions) {
			int value = values[position.row][position.col].nextValue(0);

			Region[] regions = problem.getRegionsAt(position.row, position.col);
			for (Region region : regions) {
				for (Position p : region.positions) {
					if (!positions.contains(p) && !isClue(p.row, p.col)) {
						if (eliminate(p, value))
							numberValuesEliminated++;
					}
				}
			}
		}

		return numberValuesEliminated;
	}

	private boolean eliminate(Position position, int value) {
		ValueSet currentValues = values[position.row][position.col];
		if (currentValues.contains(value)) {
			ValueSet newValues = new ValueSet(currentValues);
			newValues.remove(value);

			setValues(position.row, position.col, newValues);
			return true;
		}

		return false;
	}

	private static PuzzleType determinePuzzleType(Puzzle puzzle) {
		boolean squiggly = isSquiggly(puzzle);
		boolean x = puzzle.getExtraRegions().length == 2;
		boolean hyper = puzzle.getExtraRegions().length == 4;
		boolean percent = puzzle.getExtraRegions().length == 3;
		boolean color = puzzle.getExtraRegions().length == 9;

		if (squiggly) {
			if (x)
				return PuzzleType.SQUIGGLY_X;
			else if (hyper)
				return PuzzleType.SQUIGGLY_HYPER;
			else if (percent)
				return PuzzleType.SQUIGGLY_PERCENT;
			else if (color)
				return PuzzleType.SQUIGGLY_COLOR;
			else
				return PuzzleType.SQUIGGLY;
		}
		else {
			if (x)
				return PuzzleType.STANDARD_X;
			else if (hyper)
				return PuzzleType.STANDARD_HYPER;
			else if (percent)
				return PuzzleType.STANDARD_PERCENT;
			else if (color)
				return PuzzleType.STANDARD_COLOR;
			else
				return PuzzleType.STANDARD;
		}
	}

	private static boolean isSquiggly(Puzzle puzzle) {
		final int size = puzzle.getSize();
		int[][] stdAreas = StandardAreas.getAreas(size);

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				if (puzzle.getAreaCode(row, col) != stdAreas[row][col])
					return true;

		return false;
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

	private static int[][] obtainExtra(Puzzle puzzle) {
		final int size = puzzle.getSize();

		int[][] extra = new int[size][size];
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				extra[row][col] = -1;

		int regionNumber = 0;
		for (ExtraRegion extraRegion : puzzle.getExtraRegions()) {
			for (Position position : extraRegion.positions) {
				extra[position.row][position.col] = regionNumber;
			}

			regionNumber++;
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

	private int countValuesSet() {
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

	/**
	 * Only maintained for backward compatibility. Andoku 1.0.0 - 1.2.1 used this class to save the
	 * state of a puzzle. Andoku 1.2.2 and later directly write to a byte array instead.
	 */
	private static final class PuzzleMemento implements Externalizable {
		private static final long serialVersionUID = -7495554868028722997L;

		public ValueSet[][] values;
		public HashSet<RegionError> regionErrors;
		public HashSet<Position> cellErrors;

		@SuppressWarnings("unused")
		public PuzzleMemento() {
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			throw new UnsupportedOperationException();
		}

		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			byte version = in.readByte();

			switch (version) {
				case 3:
					readVersion3(in); // Andoku 1.0.0 - 1.0.2
					break;
				case 4:
					readVersion4(in); // Andoku 1.1.0 - 1.2.1
					break;
				default:
					throw new IOException("invalid version: " + version);
			}
		}

		private void readVersion3(ObjectInput in) throws IOException {
			in.readInt(); // skip hash value
			values = readValues(in);
			regionErrors = readRegionErrors(in);
			cellErrors = readCellErrors(in);
		}

		private void readVersion4(ObjectInput in) throws IOException {
			values = readValues(in);
			regionErrors = readRegionErrors(in);
			cellErrors = readCellErrors(in);
		}
	}

	private static void writeValues(DataOutput out, ValueSet[][] values) throws IOException {
		final int size = values.length;
		out.writeChar(size);

		for (int row = 0; row < size; row++) {
			final ValueSet[] v = values[row];
			for (int col = 0; col < size; col++) {
				out.writeChar(v[col].toInt());
			}
		}
	}

	private static ValueSet[][] readValues(DataInput in) throws IOException {
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

	private static void writeRegionErrors(DataOutput out, HashSet<RegionError> errors)
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

	private static HashSet<RegionError> readRegionErrors(DataInput in) throws IOException {
		final int numErrors = in.readChar();
		HashSet<RegionError> errors = new HashSet<RegionError>(numErrors);

		for (int i = 0; i < numErrors; i++) {
			Position p1 = new Position(in.readChar(), in.readChar());
			Position p2 = new Position(in.readChar(), in.readChar());
			errors.add(new RegionError(p1, p2));
		}

		return errors;
	}

	private static void writeCellErrors(DataOutput out, HashSet<Position> errors) throws IOException {
		final int numErrors = errors.size();
		out.writeChar(numErrors);

		for (Position p : errors) {
			out.writeChar(p.row);
			out.writeChar(p.col);
		}
	}

	private static HashSet<Position> readCellErrors(DataInput in) throws IOException {
		final int numErrors = in.readChar();
		HashSet<Position> errors = new HashSet<Position>(numErrors);

		for (int i = 0; i < numErrors; i++) {
			Position p = new Position(in.readChar(), in.readChar());
			errors.add(p);
		}

		return errors;
	}
}
