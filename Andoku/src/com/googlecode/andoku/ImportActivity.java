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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.db.PuzzleInfo;
import com.googlecode.andoku.source.PuzzleSourceIds;

public class ImportActivity extends Activity {
	private ProgressBar progressBar;
	private TextView progressText;

	private AndokuDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.import_puzzles);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

		TextView progressTitle = (TextView) findViewById(R.id.progressTitle);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressText = (TextView) findViewById(R.id.progressText);

		db = new AndokuDatabase(this);

		Intent intent = getIntent();
		if (!isValidIntent(intent)) {
			setResult(RESULT_CANCELED);
			finish();
		}
		else {
			setProgressTitle(progressTitle, intent);
			new ImportTask().execute(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (db != null) {
			db.close();
		}
	}

	private boolean isValidIntent(Intent intent) {
		if (!isValidPath(intent.getData().getPathSegments()))
			return false;

		if (!isValidPuzzles(intent.getExtras()))
			return false;

		return true;
	}

	private boolean isValidPath(List<String> pathSegments) {
		if (pathSegments.isEmpty())
			return false;

		if (pathSegments.size() != 1) // TODO: allow sub-folders
			return false;

		for (String pathSegment : pathSegments)
			if (pathSegment.length() == 0)
				return false;

		return true;
	}

	private boolean isValidPuzzles(Bundle extras) {
		List<String> puzzles = getPuzzles(extras);
		if (puzzles.isEmpty())
			return false;

		for (String puzzle : puzzles) {
			String[] parts = puzzle.split("\\|");
			if (parts.length < 1 || parts.length > 3)
				return false;

			String clues = parts[0];
			String areas = parts.length > 1 ? parts[1] : "";
			String extraRegions = parts.length > 2 ? parts[2] : "";

			if (clues.length() != 81)
				return false;

			if (areas.length() > 0 && areas.length() != 81)
				return false;

			if (!extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_NONE)
					&& !extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_HYPER)
					&& !extraRegions.equalsIgnoreCase(PuzzleInfo.EXTRA_X))
				return false;
		}

		// TODO: more tests
		return true;
	}

	private void setProgressTitle(TextView progressTitle, Intent intent) {
		String path = intent.getData().getPath();
		if (path.startsWith("/"))
			path = path.substring(1);

		String title = getResources().getString(R.string.message_importing_puzzles, path);
		progressTitle.setText(title);
	}

	List<String> getPuzzles(Bundle extras) {
		String key = Constants.EXTRA_PUZZLES;
		if (!extras.containsKey(key))
			return Collections.emptyList();

		Object value = extras.get(key);
		if (value instanceof String[])
			return Arrays.asList((String[]) value);

		if (value instanceof ArrayList<?>) {
			ArrayList<String> list = extras.getStringArrayList(key);
			if (list != null)
				return list;
		}

		return Collections.emptyList();
	}

	private final class ImportTask extends AsyncTask<Intent, Integer, Intent> {
		@Override
		protected Intent doInBackground(Intent... params) {
			db.beginTransaction();
			try {
				Intent data = importIntent(params[0]);

				db.setTransactionSuccessful();

				return data;
			}
			finally {
				db.endTransaction();
			}
		}

		@Override
		protected void onPostExecute(Intent data) {
			setResult(RESULT_OK, data);
			finish();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
			progressBar.setMax(values[1]);
			progressText.setText(values[0] + "/" + values[1]);
		}

		private Intent importIntent(Intent intent) {
			long folderId = createFolder(intent.getData().getPathSegments());
			int number = db.getNumberOfPuzzles(folderId);

			int progress = 0;

			List<String> puzzles = getPuzzles(intent.getExtras());
			for (String puzzle : puzzles) {
				publishProgress(progress++, puzzles.size());

				PuzzleInfo puzzleInfo = buildPuzzleInfo(puzzle);
				db.insertPuzzle(folderId, puzzleInfo);
			}

			publishProgress(progress++, puzzles.size());

			return createIntentForOpeningFolder(folderId, number);
		}

		private long createFolder(List<String> pathSegments) {
			long parentId = db.getOrCreateFolder(Constants.IMPORTED_PUZZLES_FOLDER);
			for (String pathSegment : pathSegments) {
				parentId = db.getOrCreateFolder(parentId, pathSegment);
			}
			return parentId;
		}

		private PuzzleInfo buildPuzzleInfo(String puzzle) {
			String[] parts = puzzle.split("\\|");
			String clues = parts[0];
			String areas = parts.length > 1 ? parts[1] : "";
			String extraRegions = parts.length > 2 ? parts[2].toUpperCase() : "";

			PuzzleInfo.Builder builder = new PuzzleInfo.Builder(clues);
			if (areas.length() > 0)
				builder.setAreas(areas);
			if (extraRegions.length() > 0)
				builder.setExtraRegions(extraRegions);
			return builder.build();
		}

		private Intent createIntentForOpeningFolder(long folderId, int number) {
			Intent data = new Intent(ImportActivity.this, AndokuActivity.class);
			data.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, PuzzleSourceIds.forDbFolder(folderId));
			data.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
			return data;
		}
	}
}
