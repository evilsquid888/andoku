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

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.googlecode.andoku.db.PuzzleId;
import com.googlecode.andoku.db.SaveGameDb;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSourceResolver;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.source.PuzzleType;

public class Main extends ListActivity {
	private static final String TAG = Main.class.getName();

	private static final String ANDOKU_DIR = "Andoku";
	private static final String DATABASE_BACKUP_FILE = SaveGameDb.DATABASE_NAME + ".bak";
	private static final String DATABASE_UPDATE_FILE = SaveGameDb.DATABASE_NAME + ".update";

	private static final int FLIP_IDX_MENU = 0;
	private static final int FLIP_IDX_SELECT_LEVEL = 1;
	private static final int FLIP_IDX_OPEN_SAVED = 2;
	private static final int FLIP_IDX_HELP = 3;
	private static final int FLIP_IDX_ABOUT = 4;

	private static final String PREF_KEY_PUZZLE_GRID = "puzzleGrid";
	private static final String PREF_KEY_PUZZLE_EXTRA_REGION = "puzzleExtraRegions";
	private static final String PREF_KEY_PUZZLE_DIFFICULTY = "puzzleDifficulty";

	private static final String APP_STATE_FLIPPER = "flipper";

	private ViewFlipper flipper;

	private Spinner gridSpinner;
	private Spinner extraRegionsSpinner;
	private Spinner difficultySpinner;

	private Button resumeGameButton;

	private SaveGameDb saveGameDb;

	private long baseTime;

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

		saveGameDb = new SaveGameDb(this);
		Cursor cursor = saveGameDb.findUnfinishedGames();
		startManagingCursor(cursor);

		String[] from = { SaveGameDb.COL_TYPE, SaveGameDb.COL_SOURCE, SaveGameDb.COL_TYPE,
				SaveGameDb.COL_TIMER, SaveGameDb.COL_MODIFIED_DATE };
		int[] to = { R.id.save_game_icon, R.id.save_game_difficulty, R.id.save_game_title,
				R.id.save_game_timer, R.id.save_game_modified };
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.save_game_list_item,
				cursor, from, to);
		listAdapter.setViewBinder(new SaveGameViewBinder(getResources()));
		setListAdapter(listAdapter);

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onResumeGameItem(id);
			}
		});

		Button newGameButton = (Button) findViewById(R.id.selectNewGameButton);
		newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectNewGameButton();
			}
		});

		resumeGameButton = (Button) findViewById(R.id.resumeGameButton);
		resumeGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onResumeGameButton();
			}
		});

		Button startNewGameButton = (Button) findViewById(R.id.startNewGameButton);
		startNewGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onStartNewGameButton();
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
		setOnClickListener(findViewById(R.id.backButton1), backListener);
		setOnClickListener(findViewById(R.id.backButton2), backListener);
		setOnClickListener(findViewById(R.id.backButton3), backListener);

		gridSpinner = (Spinner) findViewById(R.id.gridSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.grid_styles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridSpinner.setAdapter(adapter);

		extraRegionsSpinner = (Spinner) findViewById(R.id.extraRegionsSpinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.extra_regions,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extraRegionsSpinner.setAdapter(adapter);

		difficultySpinner = (Spinner) findViewById(R.id.difficultySpinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.difficulties,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		difficultySpinner.setAdapter(adapter);

		WebView helpWebView = (WebView) findViewById(R.id.helpWebView);
		helpWebView.loadUrl("file:///android_asset/"
				+ getResources().getString(R.string.html_page_help));

		WebView aboutWebView = (WebView) findViewById(R.id.aboutWebView);
		aboutWebView.loadUrl("file:///android_asset/"
				+ getResources().getString(R.string.html_page_about));

		loadPuzzlePreferences();
	}

	private void setOnClickListener(View view, OnClickListener backListener) {
		if (view != null)
			view.setOnClickListener(backListener);
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

		baseTime = System.currentTimeMillis();

		final boolean hasSavedGames = getListAdapter().getCount() != 0;
		if (hasSavedGames) {
			resumeGameButton.setEnabled(true);
		}
		else {
			resumeGameButton.setEnabled(false);

			if (flipper.getDisplayedChild() == FLIP_IDX_OPEN_SAVED) {
				flipper.setDisplayedChild(FLIP_IDX_MENU);
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (saveGameDb != null) {
			saveGameDb.close();
		}
	}

	void onSelectNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectNewGameButton()");

		flipper.setDisplayedChild(FLIP_IDX_SELECT_LEVEL);
	}

	void onResumeGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameButton()");

		long now = System.currentTimeMillis();
		if (now - baseTime >= 60000) {
			baseTime = now;
			getListView().invalidateViews();
		}

		flipper.setDisplayedChild(FLIP_IDX_OPEN_SAVED);
	}

	void onHelpButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onHelpButton()");

		flipper.setDisplayedChild(FLIP_IDX_HELP);
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

	void onStartNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onStartNewGameButton()");

		savePuzzlePreferences();

		String puzzleSourceId = getSelectedPuzzleSource();
		int number = findAvailableGame(puzzleSourceId);

		Intent intent = new Intent(this, Andoku.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
		startActivity(intent);
	}

	void onResumeGameItem(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameItem(" + rowId + ")");

		PuzzleId puzzleId = saveGameDb.puzzleIdByRowId(rowId);

		Intent intent = new Intent(this, Andoku.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleId.puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, puzzleId.number);
		intent.putExtra(Constants.EXTRA_START_PUZZLE, true);
		startActivity(intent);
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

	private String getSelectedPuzzleSource() {
		StringBuilder sb = new StringBuilder("asset:");

		switch (gridSpinner.getSelectedItemPosition()) {
			case 0:
				sb.append("standard_");
				break;
			case 1:
				sb.append("squiggly_");
				break;
			default:
				throw new IllegalStateException();
		}

		switch (extraRegionsSpinner.getSelectedItemPosition()) {
			case 0:
				sb.append("n_");
				break;
			case 1:
				sb.append("x_");
				break;
			case 2:
				sb.append("h_");
				break;
			default:
				throw new IllegalStateException();
		}

		int difficulty = difficultySpinner.getSelectedItemPosition() + 1;
		if (difficulty < 1 || difficulty > 5)
			throw new IllegalStateException();

		sb.append(difficulty);

		return sb.toString();
	}

	private int findAvailableGame(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "findAvailableGame(" + puzzleSourceId + ")");

		int candidate = 0;
		int fallback = -1; // use an unsolved game if no unplayed game found

		Cursor c = saveGameDb.findGamesBySource(puzzleSourceId);

		try {
			PuzzleSource puzzleSource = PuzzleSourceResolver.resolveSource(this, puzzleSourceId);
			// TODO: close puzzleSource?

			while (c.moveToNext()) {
				int number = c.getInt(SaveGameDb.IDX_GAME_BY_SOURCE_NUMBER);

				// is there a gap and candidate number is available?
				if (number > candidate) {
					if (Constants.LOG_V)
						Log.v(TAG, "found gap before save game " + number + "; returning " + candidate);

					return candidate;
				}

				boolean solved = c.getInt(SaveGameDb.IDX_GAME_BY_SOURCE_SOLVED) != 0;
				if (!solved && fallback == -1)
					fallback = number;

				candidate++;
			}

			if (puzzleExists(puzzleSource, candidate)) {
				if (Constants.LOG_V)
					Log.v(TAG, "returning next game after save games: " + candidate);

				return candidate;
			}

			if (fallback != -1 && puzzleExists(puzzleSource, fallback)) {
				if (Constants.LOG_V)
					Log.v(TAG, "all games played; returning first uncomplete: " + fallback);

				return fallback;
			}

			if (Constants.LOG_V)
				Log.v(TAG, "all games solved; returning 0");

			return 0;
		}
		catch (PuzzleIOException e) {
			Log.e(TAG, "Could not determine if puzzle exists", e);
			return 0;
		}
		finally {
			c.close();
		}
	}

	private boolean puzzleExists(PuzzleSource puzzleSource, int number) {
		return number >= 0 && number < puzzleSource.numberOfPuzzles();
	}

	private void savePuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "savePuzzlePreferences()");

		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt(PREF_KEY_PUZZLE_GRID, gridSpinner.getSelectedItemPosition());
		editor.putInt(PREF_KEY_PUZZLE_EXTRA_REGION, extraRegionsSpinner.getSelectedItemPosition());
		editor.putInt(PREF_KEY_PUZZLE_DIFFICULTY, difficultySpinner.getSelectedItemPosition());
		editor.commit();
	}

	private void loadPuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "loadPuzzlePreferences()");

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		gridSpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_GRID, 0));
		extraRegionsSpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_EXTRA_REGION, 0));
		difficultySpinner.setSelection(preferences.getInt(PREF_KEY_PUZZLE_DIFFICULTY, 0));
	}

	private void restoreOrBackupDatabase() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.w(TAG, "Cannot restore or back up database; external storage not mounted");
			return;
		}

		File dbFile = getDatabasePath(SaveGameDb.DATABASE_NAME);
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

	private final class SaveGameViewBinder implements SimpleCursorAdapter.ViewBinder {
		private static final int IDX_SOURCE = SaveGameDb.IDX_GAME_SOURCE;
		private static final int IDX_NUMBER = SaveGameDb.IDX_GAME_NUMBER;
		private static final int IDX_TYPE = SaveGameDb.IDX_GAME_TYPE;
		private static final int IDX_TIMER = SaveGameDb.IDX_GAME_TIMER;
		private static final int IDX_DATE_MODIFIED = SaveGameDb.IDX_GAME_MODIFIED_DATE;

		public SaveGameViewBinder(Resources resources) {
		}

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				assert columnIndex == IDX_TYPE;
				PuzzleType puzzleType = PuzzleType.forOrdinal(cursor.getInt(IDX_TYPE));
				Drawable drawable = getResources().getDrawable(puzzleType.getIconResId());
				((ImageView) view).setImageDrawable(drawable);
				return true;
			}

			if (!(view instanceof TextView))
				return false;

			TextView textView = (TextView) view;

			switch (columnIndex) {
				case IDX_TYPE:
					PuzzleType puzzleType = PuzzleType.forOrdinal(cursor.getInt(columnIndex));
					String name = getResources().getString(puzzleType.getNameResId());
					textView.setText(name);
					return true;

				case IDX_TIMER:
					String time = DateUtil.formatTime(cursor.getLong(columnIndex));
					textView.setText(time);
					return true;

				case IDX_DATE_MODIFIED:
					String age = DateUtil.formatTimeSpan(getResources(), baseTime, cursor
							.getLong(columnIndex));
					textView.setText(age);
					return true;

				case IDX_SOURCE:
					String difficultyAndNumber = parseDifficultyAndNumber(cursor.getString(IDX_SOURCE),
							cursor.getInt(IDX_NUMBER));
					textView.setText(difficultyAndNumber);
					return true;
			}

			return false;
		}

		private String parseDifficultyAndNumber(String puzzleSourceId, int number) {
			String[] difficulties = getResources().getStringArray(R.array.difficulties);
			int difficulty = puzzleSourceId.charAt(puzzleSourceId.length() - 1) - '0' - 1;
			return difficulties[difficulty] + " #" + (number + 1);
		}
	}
}
