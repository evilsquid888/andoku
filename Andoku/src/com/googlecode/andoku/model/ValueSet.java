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

public final class ValueSet {
	public static final int MAX_SIZE = 16;

	private int values;

	public ValueSet() {
	}

	public ValueSet(int values) {
		this.values = values;
	}

	public ValueSet(ValueSet other) {
		values = other.values;
	}

	public static ValueSet all(int size) {
		if (size <= 0 || size > MAX_SIZE)
			throw new IllegalArgumentException();

		int values = size < 32 ? (1 << size) - 1 : -1;
		return new ValueSet(values);
	}

	public static ValueSet none() {
		return new ValueSet(0);
	}

	public static ValueSet of(int value) {
		ValueSet values = new ValueSet();
		values.add(value);
		return values;
	}

	public static ValueSet of(int... values) {
		ValueSet valueSet = new ValueSet();
		for (int value : values)
			valueSet.add(value);
		return valueSet;
	}

	public int toInt() {
		return values;
	}

	public void setFromInt(int values) {
		this.values = values;
	}

	public void add(int value) {
		values |= 1 << value;
	}

	public void addAll(ValueSet values) {
		this.values |= values.values;
	}

	public void remove(int value) {
		values &= ~(1 << value);
	}

	public void removeAll(ValueSet values) {
		this.values &= ~values.values;
	}

	public void clear() {
		values = 0;
	}

	public boolean contains(int value) {
		return (values & 1 << value) != 0;
	}

	public boolean containsAny(ValueSet values) {
		return (this.values & values.values) != 0;
	}

	public void retainAll(ValueSet values) {
		this.values &= values.values;
	}

	public int size() {
		int count = 0;
		for (int bit = 0; bit < MAX_SIZE; bit++) {
			if ((values & 1 << bit) != 0)
				count++;
		}
		return count;
	}

	public boolean isEmpty() {
		return values == 0;
	}

	public int nextValue(int value) {
		for (int bit = value; bit < MAX_SIZE; bit++) {
			if ((values & 1 << bit) != 0)
				return bit;
		}
		return -1;
	}

	public int getValues(int[] array) {
		int idx = 0;
		for (int bit = 0; bit < MAX_SIZE; bit++) {
			if ((values & 1 << bit) != 0)
				array[idx++] = bit;
		}
		return idx;
	}

	@Override
	public int hashCode() {
		return values;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ValueSet))
			return false;

		ValueSet other = (ValueSet) obj;
		return values == other.values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');

		for (int bit = 0; bit < MAX_SIZE; bit++) {
			if ((values & 1 << bit) != 0) {
				if (sb.length() > 1)
					sb.append(", ");
				sb.append(bit);
			}
		}

		sb.append(']');
		return sb.toString();
	}

//	public static void main(String[] args) {
//		ValueSet v = all(32);
//		ValueSet x = new ValueSet(9);
//		x.add(5);
//		x.add(2);
//		v.removeAll(x);
//		System.out.println(v);
//	}
}
