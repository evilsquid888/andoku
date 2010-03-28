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

package com.googlecode.andoku.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class Puzzle {
	public static final String REGION_TYPE_ROW = "row";
	public static final String REGION_TYPE_COLUMN = "col";
	public static final String REGION_TYPE_AREA = "area";
	public static final String REGION_TYPE_EXTRA = "extra";

	public static final int UNDEFINED = -1;

	private final int[][] areaCodes;
	private final int size;
	private final ExtraRegion[] extraRegions;

	private final Region[] regions;
	private final Region[][][] regionsAt;
	private final int[][] values;
	private final ValueSet[][] eliminated;

	private int valuesCount;

	public Puzzle(Puzzle other) {
		this(other.areaCodes, other.extraRegions, false);

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				int value = other.values[row][col];
				if (value != UNDEFINED)
					set(row, col, value);
			}
	}

	public Puzzle(int[][] areaCodes, ExtraRegion[] extraRegions) {
		this(areaCodes, extraRegions, true);
	}

	private Puzzle(int[][] areaCodes, ExtraRegion[] extraRegions, boolean check) {
		if (check)
			checkParameters(areaCodes, extraRegions);

		size = areaCodes.length;
		this.areaCodes = areaCodes;
		this.extraRegions = extraRegions;

		regions = createRegions();
		regionsAt = initRegionsAt();

		values = new int[size][size];
		eliminated = new ValueSet[size][size];
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				values[row][col] = UNDEFINED;
				eliminated[row][col] = new ValueSet();
			}

		valuesCount = 0;
	}

	public int getSize() {
		return size;
	}

	public int getAreaCode(int row, int col) {
		return areaCodes[row][col];
	}

	public ExtraRegion[] getExtraRegions() {
		return extraRegions;
	}

	public Region[] getRegions() {
		return regions;
	}

	public Region[] getRegionsAt(int row, int col) {
		return regionsAt[row][col];
	}

	public void set(int row, int col, int value) {
		assert values[row][col] == UNDEFINED;

		for (Region r : regionsAt[row][col])
			r.values.add(value);

		values[row][col] = value;

		valuesCount++;
	}

	public void force(int row, int col, int value) {
		if (values[row][col] != UNDEFINED)
			clear(row, col);

		for (Region region : regionsAt[row][col]) {
			for (Position position : region.positions) {
				if (values[position.row][position.col] == value)
					clear(position.row, position.col);
			}
		}

		set(row, col, value);
	}

	public void clear(int row, int col) {
		int value = values[row][col];
		assert value != UNDEFINED;

		for (Region r : regionsAt[row][col])
			r.values.remove(value);

		values[row][col] = UNDEFINED;

		valuesCount--;
	}

	public void eliminateValue(int row, int col, int value) {
		eliminated[row][col].add(value);
	}

	public void eliminateValues(int row, int col, ValueSet values) {
		eliminated[row][col].addAll(values);
	}

	public int getValue(int row, int col) {
		return values[row][col];
	}

	public ValueSet getPossibleValues(int row, int col) {
		if (values[row][col] != UNDEFINED)
			return new ValueSet();

		ValueSet values = ValueSet.all(size);

		for (Region r : regionsAt[row][col])
			values.removeAll(r.values);

		values.removeAll(eliminated[row][col]);

		return values;
	}

	public int getValuesCount() {
		return valuesCount;
	}

	public boolean isSolved() {
		return valuesCount == size * size;
	}

	@Override
	public String toString() {
		char offset = size <= 9 ? '1' : (size == 10 ? '0' : 'A');

		StringBuilder sb = new StringBuilder();

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (values[row][col] == UNDEFINED)
					sb.append('.');
				else
					sb.append((char) (values[row][col] + offset));
			}
			if (row < size - 1)
				sb.append(' ');
		}

		return sb.toString();
	}

	private Region[] createRegions() {
		List<Region> regions = new ArrayList<Region>();

		int id = 0;

		for (int row = 0; row < size; row++) {
			List<Position> positions = new ArrayList<Position>();
			for (int col = 0; col < size; col++)
				positions.add(new Position(row, col));

			regions.add(new Region(id++, REGION_TYPE_ROW, row, positions));
		}

		for (int col = 0; col < size; col++) {
			List<Position> positions = new ArrayList<Position>();
			for (int row = 0; row < size; row++)
				positions.add(new Position(row, col));

			regions.add(new Region(id++, REGION_TYPE_COLUMN, col, positions));
		}

		for (int areaCode = 0; areaCode < size; areaCode++) {
			List<Position> positions = new ArrayList<Position>();
			for (int row = 0; row < size; row++)
				for (int col = 0; col < size; col++)
					if (areaCodes[row][col] == areaCode)
						positions.add(new Position(row, col));

			regions.add(new Region(id++, REGION_TYPE_AREA, areaCode, positions));
		}

		for (int extraNumber = 0; extraNumber < extraRegions.length; extraNumber++) {
			ExtraRegion extraRegion = extraRegions[extraNumber];
			regions.add(new Region(id++, REGION_TYPE_EXTRA, extraNumber, extraRegion.positions));
		}

		return regions.toArray(new Region[regions.size()]);
	}

	private Region[][][] initRegionsAt() {
		Map<Position, List<Region>> regionsAtMap = new HashMap<Position, List<Region>>();
		for (Region region : regions) {
			for (Position position : region.positions) {
				List<Region> regionList = regionsAtMap.get(position);
				if (regionList == null) {
					regionList = new ArrayList<Region>();
					regionsAtMap.put(position, regionList);
				}
				regionList.add(region);
			}
		}

		Region[][][] regionsAt = new Region[size][size][];
		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++) {
				List<Region> regionsList = regionsAtMap.get(new Position(row, col));
				regionsAt[row][col] = regionsList.toArray(new Region[regionsList.size()]);
			}
		return regionsAt;
	}

	private void checkParameters(int[][] areaCodes, ExtraRegion[] extraRegions) {
		int size = areaCodes.length;

		if (size < 3 || size > ValueSet.MAX_SIZE)
			throw new IllegalArgumentException("Invalid size: " + size);

		int[] counters = new int[size];
		for (int[] areaCodesRow : areaCodes) {
			if (areaCodesRow.length != size)
				throw new IllegalArgumentException("Invalid number of area code columns");

			for (int areaCode : areaCodesRow) {
				if (areaCode < 0 || areaCode >= size)
					throw new IllegalArgumentException("Invalid area code: " + areaCode);

				counters[areaCode]++;
			}
		}

		for (int i = 0; i < counters.length; i++)
			if (counters[i] != size)
				throw new IllegalArgumentException("Invalid number of " + i + "'s: " + counters[i]);

		for (ExtraRegion extraRegion : extraRegions) {
			if (extraRegion.positions.length != size)
				throw new IllegalArgumentException("Invalid extra region size: "
						+ extraRegion.positions.length);

			if (new HashSet<Position>(Arrays.asList(extraRegion.positions)).size() != size)
				throw new IllegalArgumentException("Invalid number of unique positions in extra region");

			for (Position position : extraRegion.positions)
				if (position.row < 0 || position.col < 0 || position.row >= size
						|| position.col >= size)
					throw new IllegalArgumentException("Extra region position outside grid");
		}
	}
}
