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

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.db.PuzzleId;
import com.googlecode.andoku.model.PuzzleType;
import com.googlecode.andoku.source.PuzzleSourceIds;

public class ResumeGameActivity extends ListActivity {
	private static final String TAG = ResumeGameActivity.class.getName();

	private AndokuDatabase db;

	private long baseTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.resume_game);

		db = new AndokuDatabase(this);

		Cursor cursor = db.findUnfinishedGames();
		startManagingCursor(cursor);

		String[] from = { AndokuDatabase.COL_TYPE, AndokuDatabase.COL_SOURCE,
				AndokuDatabase.COL_TYPE, AndokuDatabase.COL_TIMER, AndokuDatabase.COL_MODIFIED_DATE };
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

		View backButton = findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume()");

		super.onResume();

		baseTime = System.currentTimeMillis();

		final boolean hasSavedGames = getListAdapter().getCount() != 0;
		if (!hasSavedGames)
			finish();
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

	void onResumeGameItem(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameItem(" + rowId + ")");

		PuzzleId puzzleId = db.puzzleIdByRowId(rowId);

		Intent intent = new Intent(this, AndokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleId.puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, puzzleId.number);
		intent.putExtra(Constants.EXTRA_START_PUZZLE, true);
		startActivity(intent);
	}

	private final class SaveGameViewBinder implements SimpleCursorAdapter.ViewBinder {
		private static final int IDX_SOURCE = AndokuDatabase.IDX_GAME_SOURCE;
		private static final int IDX_NUMBER = AndokuDatabase.IDX_GAME_NUMBER;
		private static final int IDX_TYPE = AndokuDatabase.IDX_GAME_TYPE;
		private static final int IDX_TIMER = AndokuDatabase.IDX_GAME_TIMER;
		private static final int IDX_DATE_MODIFIED = AndokuDatabase.IDX_GAME_MODIFIED_DATE;

		public SaveGameViewBinder(Resources resources) {
		}

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				assert columnIndex == IDX_TYPE;
				PuzzleType puzzleType = PuzzleType.forOrdinal(cursor.getInt(IDX_TYPE));
				Drawable drawable = Util.getPuzzleIcon(getResources(), puzzleType);
				((ImageView) view).setImageDrawable(drawable);
				return true;
			}

			if (!(view instanceof TextView))
				return false;

			TextView textView = (TextView) view;

			switch (columnIndex) {
				case IDX_TYPE:
					PuzzleType puzzleType = PuzzleType.forOrdinal(cursor.getInt(columnIndex));
					String name = Util.getPuzzleName(getResources(), puzzleType);
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

		private String parseDifficultyAndNumber(String sourceId, int number) {
			if (PuzzleSourceIds.isDbSource(sourceId)) {
				String folderName = Util.getFolderName(db, sourceId);
				return folderName + " #" + (number + 1);
			}
			else {
				String[] difficulties = getResources().getStringArray(R.array.difficulties);
				int difficulty = sourceId.charAt(sourceId.length() - 1) - '0' - 1;
				return difficulties[difficulty] + " #" + (number + 1);
			}
		}
	}
}
