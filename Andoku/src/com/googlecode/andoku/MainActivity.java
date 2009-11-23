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

package com.googlecode.andoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.googlecode.andoku.db.AndokuDatabase;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getName();

	private static final String ANDOKU_DIR = "Andoku";
	private static final String DATABASE_BACKUP_FILE = "database.bak";
	private static final String DATABASE_UPDATE_FILE = "database.update";

	private static final int FLIP_IDX_MENU = 0;
	private static final int FLIP_IDX_ABOUT = 1;

	private static final String APP_STATE_FLIPPER = "flipper";

	private ViewFlipper flipper;

	private Button foldersButton;
	private Button resumeGameButton;

	private AndokuDatabase db;
	private long importedPuzzlesFolderId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		super.onCreate(savedInstanceState);

		restoreOrBackupDatabase();

		Util.setFullscreenWorkaround(this);

		setContentView(R.layout.main);

		flipper = (ViewFlipper) findViewById(R.id.flipper);

		if (savedInstanceState != null) {
			flipper.setDisplayedChild(savedInstanceState.getInt(APP_STATE_FLIPPER, 0));
		}

		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_out));

		db = new AndokuDatabase(this);

		importedPuzzlesFolderId = db.getOrCreateFolder(Constants.IMPORTED_PUZZLES_FOLDER);

		resumeGameButton = (Button) findViewById(R.id.resumeGameButton);
		resumeGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onResumeGameButton();
			}
		});

		Button newGameButton = (Button) findViewById(R.id.selectNewGameButton);
		newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectNewGameButton();
			}
		});

		foldersButton = (Button) findViewById(R.id.selectFoldersButton);
		foldersButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectFoldersButton();
			}
		});

		Button settingsButton = (Button) findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSettingsButton();
			}
		});

		Button helpButton = (Button) findViewById(R.id.helpButton);
		helpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onHelpButton();
			}
		});

		Button aboutButton = (Button) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onAboutButton();
			}
		});

		OnClickListener backListener = new OnClickListener() {
			public void onClick(View v) {
				onBackButton();
			}
		};
		Util.saveSetOnClickListener(findViewById(R.id.backButton1), backListener);
		Util.saveSetOnClickListener(findViewById(R.id.backButton3), backListener);

		WebView aboutWebView = (WebView) findViewById(R.id.aboutWebView);
		aboutWebView.loadUrl("file:///android_asset/"
				+ getResources().getString(R.string.html_page_about));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onSaveInstanceState()");

		super.onSaveInstanceState(outState);

		outState.putInt(APP_STATE_FLIPPER, flipper.getDisplayedChild());
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume()");

		super.onResume();

		final boolean hasPuzzleFolders = db.hasSubFolders(importedPuzzlesFolderId);
		foldersButton.setEnabled(hasPuzzleFolders);

		final boolean hasSavedGames = db.hasUnfinishedGames();
		resumeGameButton.setEnabled(hasSavedGames);
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (db != null) {
			db.close();
		}
	}

	void onResumeGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameButton()");

		Intent intent = new Intent(this, ResumeGameActivity.class);
		startActivity(intent);
	}

	void onSelectNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectNewGameButton()");

		Intent intent = new Intent(this, NewGameActivity.class);
		startActivity(intent);
	}

	void onSelectFoldersButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectFoldersButton()");

		Intent intent = new Intent(this, FolderListActivity.class);
		intent.putExtra(Constants.EXTRA_FOLDER_ID, importedPuzzlesFolderId);
		startActivity(intent);
	}

	void onSettingsButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSettingsButton()");

		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	void onHelpButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onHelpButton()");

		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

	void onAboutButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onAboutButton()");

		flipper.setDisplayedChild(FLIP_IDX_ABOUT);
	}

	void onBackButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onBackButton()");

		flipper.setDisplayedChild(FLIP_IDX_MENU);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (flipper.getDisplayedChild() != 0) {
				onBackButton();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void restoreOrBackupDatabase() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.w(TAG, "Cannot restore or back up database; external storage not mounted");
			return;
		}

		File dbFile = getDatabasePath(AndokuDatabase.DATABASE_NAME);
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

	private boolean createDir(File dir) {
		if (dir.isDirectory())
			return true;

		final boolean created = dir.mkdirs();
		if (!created)
			Log.w(TAG, "Could not create directory " + dir);

		return created;
	}

	private boolean copyFile(File source, File target) {
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
