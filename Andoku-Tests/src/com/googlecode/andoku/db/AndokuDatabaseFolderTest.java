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

package com.googlecode.andoku.db;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.SQLException;
import android.test.AndroidTestCase;

public class AndokuDatabaseFolderTest extends AndroidTestCase {
	private AndokuDatabase db;

	@Override
	protected void setUp() throws Exception {
		db = new AndokuDatabase(getContext());
		db.resetAll();
	}

	@Override
	protected void tearDown() throws Exception {
		db.close();
	}

	public void testDatabaseIsEmpty() throws Exception {
		assertTrue(loadFolders().isEmpty());
	}

	public void testCreateFolder() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);

		Map<Long, String> folders = loadFolders();
		assertEquals(1, folders.size());
		assertEquals("f1", folders.get(folderId1));

		long folderId2 = db.createFolder("f2");
		assertTrue(folderId2 >= 0);

		folders = loadFolders();
		assertEquals(2, folders.size());
		assertEquals("f1", folders.get(folderId1));
		assertEquals("f2", folders.get(folderId2));
	}

	public void testCreateSubFolder() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "f2");
		assertTrue(folderId2 >= 0);

		Map<Long, String> folders = loadFolders();
		assertEquals(1, folders.size());
		assertEquals("f1", folders.get(folderId1));

		folders = loadFolders(folderId1);
		assertEquals(1, folders.size());
		assertEquals("f2", folders.get(folderId2));
	}

	public void testFolderNameIsUnique() throws Exception {
		long folderId1 = db.createFolder("folder");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "folder");
		assertTrue(folderId2 >= 0);

		try {
			db.createFolder("folder");
			fail();
		}
		catch (SQLException expected) {
		}

		try {
			db.createFolder(folderId1, "folder");
			fail();
		}
		catch (SQLException expected) {
		}
	}

	public void testFolderExistsByName() throws Exception {
		assertFalse(db.folderExists("folder"));

		long folderId1 = db.createFolder("folder");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "folder");
		assertTrue(folderId2 >= 0);

		assertTrue(db.folderExists("folder"));

		assertTrue(db.folderExists(folderId1, "folder"));

		assertFalse(db.folderExists("nosuchfolder"));
	}

	public void testGetFolderId() throws Exception {
		assertNull(db.getFolderId("folder"));

		long folderId1 = db.createFolder("folder");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "folder");
		assertTrue(folderId2 >= 0);

		assertEquals(new Long(folderId1), db.getFolderId("folder"));

		assertEquals(new Long(folderId2), db.getFolderId(folderId1, "folder"));

		assertNull(db.getFolderId("nosuchfolder"));
	}

	public void testGetOrCreateFolder() throws Exception {
		long folderId1 = db.getOrCreateFolder("f1");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.getOrCreateFolder("f1");
		assertEquals(folderId1, folderId2);

		long folderId3 = db.getOrCreateFolder(folderId1, "f2");
		assertTrue(folderId3 >= 0);

		long folderId4 = db.getOrCreateFolder(folderId1, "f2");
		assertEquals(folderId3, folderId4);
	}

	public void testGetFolderName() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "f2");
		assertTrue(folderId2 >= 0);

		assertEquals("f1", db.getFolderName(folderId1));
		assertEquals("f2", db.getFolderName(folderId2));
		assertNull(db.getFolderName(folderId2 + 1));
	}

	public void testGetParentFolderId() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "f2");
		assertTrue(folderId2 >= 0);

		assertEquals(new Long(AndokuDatabase.ROOT_FOLDER_ID), db.getParentFolderId(folderId1));
		assertEquals(new Long(folderId1), db.getParentFolderId(folderId2));
		assertNull(db.getParentFolderId(folderId2 + 1));
	}

	public void testRenameFolder() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);
		assertEquals("f1", db.getFolderName(folderId1));

		db.renameFolder(folderId1, "newf1");
		assertEquals("newf1", db.getFolderName(folderId1));

		long folderId2 = db.createFolder(folderId1, "f2");
		assertTrue(folderId2 >= 0);
		assertEquals("f2", db.getFolderName(folderId2));

		db.renameFolder(folderId2, "newf2");
		assertEquals("newf2", db.getFolderName(folderId2));
	}

	public void testCannotRenameAsExistingFolder() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder("f2");
		assertTrue(folderId2 >= 0);

		try {
			db.renameFolder(folderId1, "f2");
			fail();
		}
		catch (SQLException e) {
		}
	}

	public void testDeleteFolder() throws Exception {
		long folderId1 = db.createFolder("f1");
		long folderId2 = db.createFolder(folderId1, "f2");
		long folderId3 = db.createFolder(folderId1, "f3");
		long folderId4 = db.createFolder(folderId2, "f4");

		assertTrue(db.folderExists(folderId1));
		assertTrue(db.folderExists(folderId2));
		assertTrue(db.folderExists(folderId3));
		assertTrue(db.folderExists(folderId4));

		db.deleteFolder(folderId1);

		assertFalse(db.folderExists(folderId1));
		assertFalse(db.folderExists(folderId2));
		assertFalse(db.folderExists(folderId3));
		assertFalse(db.folderExists(folderId4));
	}

	public void testHasSubFolders() throws Exception {
		long folderId1 = db.createFolder("f1");
		assertFalse(db.hasSubFolders(folderId1));

		long folderId2 = db.createFolder(folderId1, "f2");
		assertTrue(db.hasSubFolders(folderId1));

		long folderId3 = db.createFolder(folderId1, "f3");
		assertTrue(db.hasSubFolders(folderId1));

		db.deleteFolder(folderId2);
		assertTrue(db.hasSubFolders(folderId1));

		db.deleteFolder(folderId3);
		assertFalse(db.hasSubFolders(folderId1));
	}

	public void testHasPuzzles() throws Exception {
		long folderId = db.createFolder("folder");
		assertFalse(db.hasPuzzles(folderId));

		String clues1 = ".8.4.96536428...7.......8....7..5.42...7.1...85.6..1....6.......1...47362735.8.1.";
		PuzzleInfo puzzle1 = new PuzzleInfo.Builder(clues1).build();
		long puzzleId1 = db.insertPuzzle(folderId, puzzle1);
		assertTrue(db.hasPuzzles(folderId));

		String clues2 = "63.2.8.1.2...5..891.9.6..3...8..6.5....187....6.5..9...9..7.1.681..2...5.2.4.3.97";
		PuzzleInfo puzzle2 = new PuzzleInfo.Builder(clues2).build();
		long puzzleId2 = db.insertPuzzle(folderId, puzzle2);
		assertTrue(db.hasPuzzles(folderId));

		db.deletePuzzle(puzzleId1);
		assertTrue(db.hasPuzzles(folderId));

		db.deletePuzzle(puzzleId2);
		assertFalse(db.hasPuzzles(folderId));
	}

	public void testFolderIsEmpty() throws Exception {
		long folderId1 = db.createFolder("folder");
		assertTrue(db.isEmpty(folderId1));

		String clues = ".8.4.96536428...7.......8....7..5.42...7.1...85.6..1....6.......1...47362735.8.1.";
		PuzzleInfo puzzle = new PuzzleInfo.Builder(clues).build();
		long puzzleId = db.insertPuzzle(folderId1, puzzle);
		assertFalse(db.isEmpty(folderId1));

		long folderId2 = db.createFolder(folderId1, "f2");
		assertFalse(db.isEmpty(folderId1));

		db.deletePuzzle(puzzleId);
		assertFalse(db.isEmpty(folderId1));

		db.deleteFolder(folderId2);
		assertTrue(db.isEmpty(folderId1));
	}

	private Map<Long, String> loadFolders() {
		return loadFolders(AndokuDatabase.ROOT_FOLDER_ID);
	}

	private Map<Long, String> loadFolders(long parent) {
		Map<Long, String> folders = new HashMap<Long, String>();

		Cursor cursor = db.getFolders(parent);
		while (cursor.moveToNext())
			folders.put(cursor.getLong(0), cursor.getString(1));
		cursor.close();

		return folders;
	}
}
