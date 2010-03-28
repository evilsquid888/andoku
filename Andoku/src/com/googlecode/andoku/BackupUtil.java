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

package com.googlecode.andoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.googlecode.andoku.db.AndokuDatabase;

class BackupUtil extends Activity {
	private static final String TAG = BackupUtil.class.getName();

	private static final String ANDOKU_DIR = "Andoku";
	private static final String DATABASE_BACKUP_FILE = "database.bak";
	private static final String DATABASE_UPDATE_FILE = "database.update";

	private BackupUtil() {
	}

	public static void restoreOrBackupDatabase(Context context) {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.w(TAG, "Cannot restore or back up database; external storage not mounted");
			return;
		}

		File dbFile = context.getDatabasePath(AndokuDatabase.DATABASE_NAME);
		File sdcard = Environment.getExternalStorageDirectory();
		File andokuDir = new File(sdcard, ANDOKU_DIR);
		if (!andokuDir.isDirectory() && !andokuDir.mkdirs()) {
			Log.w(TAG, "Could not create root directory \"" + ANDOKU_DIR + "\" on external storage");
			return;
		}

		File backupFile = new File(andokuDir, DATABASE_BACKUP_FILE);
		File updateFile = new File(andokuDir, DATABASE_UPDATE_FILE);

		// database can be overwritten by manually placing an update-file on the sd card
		if (updateFile.isFile()) {
			if (Constants.LOG_V) {
				Log.v(TAG, "Updating database from " + updateFile.getAbsolutePath());

				if (dbFile.isFile())
					Log.v(TAG, "Overwriting existing database!");
			}

			if (createDir(dbFile.getParentFile()))
				if (copyFile(updateFile, dbFile))
					updateFile.delete();
		}
		// restore from backup in case installation was wiped (i.e. andoku was uninstalled and reinstalled)
		else if (backupFile.isFile() && !dbFile.isFile()) {
			if (Constants.LOG_V)
				Log.v(TAG, "Restoring database from backup " + backupFile.getAbsolutePath());

			if (createDir(dbFile.getParentFile()))
				copyFile(backupFile, dbFile);

			return; // no need to back up
		}

		// copy current database to backup file
		if (dbFile.isFile()) {
			if (Constants.LOG_V)
				Log.v(TAG, "Backing up database to " + backupFile.getAbsolutePath());

			copyFile(dbFile, backupFile);
		}
	}

	private static boolean createDir(File dir) {
		if (dir.isDirectory())
			return true;

		final boolean created = dir.mkdirs();
		if (!created)
			Log.w(TAG, "Could not create directory " + dir);

		return created;
	}

	private static boolean copyFile(File source, File target) {
		try {
			FileChannel in = null;
			FileChannel out = null;

			try {
				in = new FileInputStream(source).getChannel();
				out = new FileOutputStream(target).getChannel();

				long written = 0;
				long total = in.size();

				while (written < total) {
					long bytes = out.transferFrom(in, written, total - written);

					written += bytes;
				}
			}
			finally {
				try {
					if (in != null)
						in.close();
				}
				finally {
					if (out != null)
						out.close();
				}
			}

			return true;
		}
		catch (IOException e) {
			Log.w(TAG, "Could not copy " + source + " to " + target, e);

			return false;
		}
	}
}
