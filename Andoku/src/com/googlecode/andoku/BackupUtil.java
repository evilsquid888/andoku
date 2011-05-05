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

package com.googlecode.andoku;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andokusquid.R;

class BackupUtil extends Activity {
	private static final String TAG = BackupUtil.class.getName();

	private static final String SD_CARD_BACKUP_DIR = "Andoku";
	private static final String DATABASE_BACKUP_FILE = "database.bak";
	private static final String DATABASE_BACKUP_FILE_SIGNATURE = "database.bak.sig";
	private static final String DATABASE_UPDATE_FILE = "database.update";

	private static final String MAC_NAME = "HmacSHA1";

	private BackupUtil() {
	}

	public static void restoreOrBackupDatabase(Context context) {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.w(TAG, "Cannot restore or back up database; external storage not mounted");
			return;
		}

		File dbFile = context.getDatabasePath(AndokuDatabase.DATABASE_NAME);
		File sdcard = Environment.getExternalStorageDirectory();
		File andokuDir = new File(sdcard, SD_CARD_BACKUP_DIR);
		if (!andokuDir.isDirectory() && !andokuDir.mkdirs()) {
			Log.e(TAG, "Could not create root directory \"" + SD_CARD_BACKUP_DIR
					+ "\" on external storage");
			return;
		}

		// If this line does not compile it is because you have to specify a secret app key in the
		// resources first! Create res/values/secret.xml and add a random string named secret_key.
		// Please also adjust the value of SD_CARD_BACKUP_DIR so that the backup files of your mod
		// and the backup files of the original Andoku do not conflict.
		String secretKey = "secretandoku";

		File backupFile = new File(andokuDir, DATABASE_BACKUP_FILE);
		File backupFileSignature = new File(andokuDir, DATABASE_BACKUP_FILE_SIGNATURE);
		File updateFile = new File(andokuDir, DATABASE_UPDATE_FILE);

		// database can be overwritten by manually placing an update-file on the sd card
		if (updateFile.isFile()) {
			if (createDir(dbFile.getParentFile())) {
				Log.i(TAG, "Updating database from " + updateFile.getAbsolutePath());

				if (dbFile.isFile())
					Log.i(TAG, "Overwriting existing database!");

				if (copyFile(updateFile, dbFile))
					updateFile.delete();
			}
		}
		// restore from backup in case installation was wiped (i.e. andoku was uninstalled and reinstalled)
		else if (backupFile.isFile() && !dbFile.isFile()) {
			if (createDir(dbFile.getParentFile())) {
				Log.i(TAG, "Verifying database signature " + backupFileSignature.getAbsolutePath());

				if (verifySignature(secretKey, backupFile, backupFileSignature)) {
					Log.i(TAG, "Restoring database from backup " + backupFile.getAbsolutePath());

					copyFile(backupFile, dbFile);
				}
				else {
					Log.w(TAG, "Signature broken; ignoring database backup!");
				}
			}

			return; // no need to back up
		}

		// copy current database to backup file
		if (dbFile.isFile()) {
			Log.i(TAG, "Writing database signature to " + backupFileSignature.getAbsolutePath());

			if (createSignature(secretKey, dbFile, backupFileSignature)) {
				Log.i(TAG, "Backing up database to " + backupFile.getAbsolutePath());

				copyFile(dbFile, backupFile);
			}
		}
	}

	private static boolean createDir(File dir) {
		if (dir.isDirectory())
			return true;

		final boolean created = dir.mkdirs();
		if (!created)
			Log.e(TAG, "Could not create directory " + dir);

		return created;
	}

	private static boolean copyFile(File source, File target) {
		try {
			copy(new FileInputStream(source), new FileOutputStream(target));
			return true;
		}
		catch (IOException e) {
			Log.e(TAG, "Could not copy " + source + " to " + target, e);
			return false;
		}
	}

	private static boolean createSignature(String keyString, File input, File signatureFile) {
		try {
			byte[] message = load(input);
			byte[] signature = sign(keyString, message);

			OutputStream out = new FileOutputStream(signatureFile);
			out.write(signature);
			out.close();

			return true;
		}
		catch (IOException e) {
			Log.e(TAG, "Could not sign database", e);
			return false;
		}
		catch (GeneralSecurityException e) {
			Log.e(TAG, "Could not sign database", e);
			return false;
		}
	}

	private static boolean verifySignature(String keyString, File input, File signatureFile) {
		if (!signatureFile.isFile()) {
			// for now just ignore if not signed; future versions may behave differently
			Log.w(TAG, "Database backup not signed!");
			return true;
		}

		try {
			byte[] message = load(input);
			byte[] expectedSignature = sign(keyString, message);
			byte[] actualSignature = load(signatureFile);

			return Arrays.equals(expectedSignature, actualSignature);
		}
		catch (IOException e) {
			Log.e(TAG, "Could not verify database signature", e);
			return false;
		}
		catch (GeneralSecurityException e) {
			Log.e(TAG, "Could not verify database signature", e);
			return false;
		}
	}

	private static byte[] sign(String keyString, byte[] message) throws GeneralSecurityException {
		byte[] keyBytes = utf8(keyString);

		SecretKey key = new SecretKeySpec(keyBytes, MAC_NAME);
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(key);

		return mac.doFinal(message);
	}

	private static byte[] utf8(String keyString) {
		try {
			return keyString.getBytes("utf-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // utf-8 is supported!
		}
	}

	private static byte[] load(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(new FileInputStream(file), baos);
		return baos.toByteArray();
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];

		try {
			while (true) {
				int bytes = in.read(buffer);
				if (bytes == -1)
					break;

				out.write(buffer, 0, bytes);
			}

			out.flush();
		}
		finally {
			try {
				in.close();
			}
			finally {
				out.close();
			}
		}
	}
}
