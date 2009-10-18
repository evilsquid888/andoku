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

package com.googlecode.andoku.db;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.SQLException;
import android.test.AndroidTestCase;

import com.googlecode.andoku.db.PuzzleDb;

public class PuzzleDbTest extends AndroidTestCase {
	private PuzzleDb db;

	@Override
	protected void setUp() throws Exception {
		db = new PuzzleDb(getContext());
		db.resetAll();
	}

	@Override
	protected void tearDown() throws Exception {
		db.close();
	}

	public void testIsEmpty() throws Exception {
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
		} catch (SQLException expected) {
		}

		try {
			db.createFolder(folderId1, "folder");
			fail();
		} catch (SQLException expected) {
		}
	}

	public void testFolderExistsByName() throws Exception {
		assertFalse(db.folderExists("folder"));

		long folderId1 = db.createFolder("folder");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "folder");
		assertTrue(folderId2 >= 0);

		assertTrue(db.folderExists("folder"));

		assertTrue(db.folderExists(folderId1,
				"folder"));

		assertFalse(db.folderExists("nosuchfolder"));
	}

	public void testGetFolderId() throws Exception {
		assertNull(db.getFolderId("folder"));

		long folderId1 = db.createFolder("folder");
		assertTrue(folderId1 >= 0);

		long folderId2 = db.createFolder(folderId1, "folder");
		assertTrue(folderId2 >= 0);

		assertEquals(new Long(folderId1), db.getFolderId("folder"));

		assertEquals(new Long(folderId2), db.getFolderId(folderId1,
				"folder"));

		assertNull(db.getFolderId("nosuchfolder"));
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

		assertEquals(new Long(PuzzleDb.ROOT_FOLDER_ID), db
				.getParentFolderId(folderId1));
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
		} catch (SQLException e) {
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

	private Map<Long, String> loadFolders() {
		return loadFolders(PuzzleDb.ROOT_FOLDER_ID);
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
