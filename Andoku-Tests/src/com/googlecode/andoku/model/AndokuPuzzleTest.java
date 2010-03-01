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

import java.io.Serializable;

import junit.framework.TestCase;

import com.googlecode.andoku.util.MockPuzzleSource;
import com.googlecode.andoku.util.SerializableUtil;

public class AndokuPuzzleTest extends TestCase {
	public void testSaveAndRestoreMemento() throws Exception {
		// "9..1........9.23.612.....89.............6.............71.....252.96.7........5..7|112222334112222334111552334116553334666654444677755488677955888677999988677999988|H";
		// "987153264574982316123576489461829753395468172642731598718394625259647831836215947";

		AndokuPuzzle p1 = MockPuzzleSource.createPuzzle(0);
		// set correct values
		p1.setValues(0, 1, ValueSet.of(7));
		p1.setValues(0, 2, ValueSet.of(6));
		p1.setValues(0, 4, ValueSet.of(3, 4));
		// set incorrect values
		p1.setValues(0, 5, ValueSet.of(8));
		p1.checkForErrors();

		assertEquals(2, p1.getRegionErrors().size());
		assertEquals(3, p1.getErrorPositions().size());

		Serializable memento = p1.saveToMemento();

		memento = SerializableUtil.roundTrip(memento);

		AndokuPuzzle p2 = MockPuzzleSource.createPuzzle(0);

		assertEquals(ValueSet.none(), p2.getValues(0, 1));
		assertEquals(ValueSet.none(), p2.getValues(0, 2));
		assertEquals(ValueSet.none(), p2.getValues(0, 4));
		assertEquals(ValueSet.none(), p2.getValues(0, 5));

		assertTrue(p2.getRegionErrors().isEmpty());
		assertTrue(p2.getErrorPositions().isEmpty());

		p2.restoreFromMemento(memento);

		assertEquals(ValueSet.of(7), p2.getValues(0, 1));
		assertEquals(ValueSet.of(6), p2.getValues(0, 2));
		assertEquals(ValueSet.of(3, 4), p2.getValues(0, 4));
		assertEquals(ValueSet.of(8), p2.getValues(0, 5));

		assertEquals(2, p2.getRegionErrors().size());
		assertEquals(3, p2.getErrorPositions().size());

		assertEquals(p1.getRegionErrors(), p2.getRegionErrors());
		assertEquals(p1.getErrorPositions(), p2.getErrorPositions());
	}
}
