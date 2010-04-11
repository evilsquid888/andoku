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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.googlecode.andoku.solver.DlxPuzzleSolver;
import com.googlecode.andoku.solver.PuzzleSolver;
import com.googlecode.andoku.solver.SingleSolutionReporter;
import com.googlecode.andoku.transfer.StandardAreas;

public class AndokuPuzzle {
	private final String name;
	private final int size;
	private final Puzzle problem;
	private final PuzzleType puzzleType;
	private final Difficulty difficulty;
	private final boolean[][] extra;
	private boolean solved;

	private ValueSet[][] values;
	private int numValues;

	private final int[] areaColors;
	private final int numberOfAreaColors;

	private Solution solution;
	private boolean computeSolutionFailed = false;

	// multiple identical values within a single region
	private HashSet<RegionError> regionErrors;

	// errors compared to actual solution; correct value has been eliminated
	private HashSet<Position> cellErrors;

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
		this.numValues = countValues(this.values);
		this.solved = checkSolved();
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

	public Serializable saveToMemento() {
		return new PuzzleMemento(copyValues(values), copyRegionErrors(regionErrors),
				copyCellErrors(cellErrors));
	}

	public boolean restoreFromMemento(Object object) {
		if (!(object instanceof PuzzleMemento))
			return false;

		PuzzleMemento memento = (PuzzleMemento) object;
		if (memento.values.length != values.length)
			return false;

		this.values = copyValues(memento.values);
		this.numValues = countValues(this.values);
		this.solved = checkSolved();
		this.regionErrors = copyRegionErrors(memento.regionErrors);
		this.cellErrors = copyCellErrors(memento.cellErrors);

		return true;
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
		return problem.getValue(row, col) != Puzzle.UNDEFINED;
	}

	public boolean isExtraRegion(int row, int col) {
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
		Set<Position> cells = getAllCellsWithASingleValue();

		return canEliminateValues(cells);
	}

	private Set<Position> getAllCellsWithASingleValue() {
		Set<Position> cells = new HashSet<Position>();

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				if (values[row][col].size() == 1)
					cells.add(new Position(row, col));

		return cells;
	}

	private boolean canEliminateValues(Set<Position> cells) {
		for (Position cell : cells) {
			int value = values[cell.row][cell.col].nextValue(0);

			Region[] regions = problem.getRegionsAt(cell.row, cell.col);
			for (Region region : regions) {
				for (Position p : region.positions) {
					if (!cells.contains(p) && !isClue(p.row, p.col)) {
						if (canEliminateValue(p, value))
							return true;
					}
				}
			}
		}

		return false;
	}

	private boolean canEliminateValue(Position cell, int value) {
		ValueSet currentValues = values[cell.row][cell.col];
		return currentValues.isEmpty() || currentValues.contains(value);
	}

	public int eliminateValues() {
		setAllValuesOnEmptyCells();

		Set<Position> cells = getAllCellsWithASingleValue();

		return eliminateValues(cells);
	}

	private void setAllValuesOnEmptyCells() {
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				if (values[row][col].isEmpty())
					setValues(row, col, ValueSet.all(size));
	}

	private int eliminateValues(Set<Position> cells) {
		int numberValuesEliminated = 0;

		for (Position cell : cells) {
			int value = values[cell.row][cell.col].nextValue(0);

			Region[] regions = problem.getRegionsAt(cell.row, cell.col);
			for (Region region : regions) {
				for (Position p : region.positions) {
					if (!cells.contains(p) && !isClue(p.row, p.col)) {
						if (eliminate(p, value))
							numberValuesEliminated++;
					}
				}
			}
		}

		return numberValuesEliminated;
	}

	private boolean eliminate(Position cell, int value) {
		ValueSet currentValues = values[cell.row][cell.col];
		if (currentValues.contains(value)) {
			ValueSet newValues = new ValueSet(currentValues);
			newValues.remove(value);

			setValues(cell.row, cell.col, newValues);
			return true;
		}

		return false;
	}

	private static PuzzleType determinePuzzleType(Puzzle puzzle) {
		boolean squiggly = isSquiggly(puzzle);
		boolean x = puzzle.getExtraRegions().length == 2;
		boolean hyper = puzzle.getExtraRegions().length == 4;
		boolean percent = puzzle.getExtraRegions().length == 3;

		if (squiggly) {
			if (x)
				return PuzzleType.SQUIGGLY_X;
			else if (hyper)
				return PuzzleType.SQUIGGLY_HYPER;
			else if (percent)
				return PuzzleType.SQUIGGLY_PERCENT;
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

		public ValueSet[][] values;
		public HashSet<RegionError> regionErrors;
		public HashSet<Position> cellErrors;

		public PuzzleMemento() {
		}

		public PuzzleMemento(ValueSet[][] values, HashSet<RegionError> regionErrors,
				HashSet<Position> cellErrors) {
			this.values = values;
			this.regionErrors = regionErrors;
			this.cellErrors = cellErrors;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeByte(4); // version

			writeValues(out, values);
			writeRegionErrors(out, regionErrors);
			writeCellErrors(out, cellErrors);
		}

		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			byte version = in.readByte();

			switch (version) {
				case 3:
					readVersion3(in); // Andoku 1.0.0 - 1.0.2
					break;
				case 4:
					readVersion4(in);
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
