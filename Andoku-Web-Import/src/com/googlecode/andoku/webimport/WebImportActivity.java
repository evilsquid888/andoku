/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2011  Markus Wiederkehr
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

package com.googlecode.andoku.webimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.andoku.client.AndokuClient;
import com.googlecode.andoku.client.UriAndPath;

/**
 * Install this app, then open http://draculik.eu/opensudoku/ in the browser and click on one of the
 * links at the bottom of the page (Puzzles from www.sudocue.net). When the dialog called
 * "Complete action using" appears select AndokuWebImport.
 */
public class WebImportActivity extends Activity {
	private static final int PROGRESS_UPDATE_INTERVAL_MS = 150;

	private TextView progressTitle;
	private ProgressBar progressBar;
	private TextView progressText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.web_import);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

		progressTitle = (TextView) findViewById(R.id.progressTitle);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressText = (TextView) findViewById(R.id.progressText);

		new ImportTask().execute(getIntent());
	}

	private final class ImportTask extends AsyncTask<Intent, Action, Uri> {
		@Override
		protected Uri doInBackground(Intent... params) {
			try {
				final Resources resources = getResources();

				Intent intent = params[0];
				Uri uri = intent.getData();
				String folderName = getFolderName(uri);

				publishProgress(new SetTitleAction(resources.getString(
						R.string.message_downloading_puzzles, uri)));

				ArrayList<String> puzzles = loadPuzzles(uri);

				UriAndPath uriAndPath = createFolder(folderName);

				publishProgress(new SetTitleAction(resources.getString(
						R.string.message_importing_puzzles, uriAndPath.getPath())));

				return importPuzzles(uriAndPath.getUri(), puzzles);
			}
			catch (IOException e) {
				e.printStackTrace(); // TODO: better error handling
				return null;
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			// TODO: display error message if an error occurred
			finish();
		}

		@Override
		protected void onProgressUpdate(Action... values) {
			for (Action progress : values)
				progress.run();
		}

		private String getFolderName(Uri uri) {
			String folderName = uri.getLastPathSegment();
			if (folderName.endsWith(".sdm"))
				folderName = folderName.substring(0, folderName.length() - 4);

			return folderName;
		}

		private ArrayList<String> loadPuzzles(Uri uri) throws IOException {
			URL url = new URL(uri.toString());
			InputStream in = url.openStream();
			try {
				return loadPuzzles(in);
			}
			finally {
				in.close();
			}
		}

		private ArrayList<String> loadPuzzles(InputStream in) throws IOException {
			long lastUpdate = System.currentTimeMillis();
			publishProgress(new IndeterminateProgressAction(0));

			ArrayList<String> result = new ArrayList<String>();

			Reader reader = new InputStreamReader(in, "us-ascii");
			BufferedReader br = new BufferedReader(reader);
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				line = line.trim();

				if (!looksSane(line))
					continue;

				result.add(line);

				long now = System.currentTimeMillis();
				if (now - lastUpdate > PROGRESS_UPDATE_INTERVAL_MS) {
					publishProgress(new IndeterminateProgressAction(result.size()));
					lastUpdate = now;
				}
			}

			publishProgress(new IndeterminateProgressAction(result.size()));

			return result;
		}

		private boolean looksSane(String line) {
			if (line.length() != 81) {
				return false;
			}

			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (c < '0' || c > '9') {
					return false;
				}
			}

			return true;
		}

		private UriAndPath createFolder(String folderName) {
			AndokuClient client = new AndokuClient(WebImportActivity.this);
			return client.createUniqueFolder(folderName);
		}

		private Uri importPuzzles(Uri folderUri, ArrayList<String> puzzles) {
			List<ContentValues> valuesArray = toContentValues(puzzles);

			final int total = valuesArray.size();
			publishProgress(new DeterminateProgressAction(0, total));

			ContentObserver observer = new ContentObserver(null) {
				private long lastUpdate = System.currentTimeMillis();
				private int counter = 0;

				@Override
				public void onChange(boolean selfChange) {
					counter++;

					long now = System.currentTimeMillis();
					if (now - lastUpdate > PROGRESS_UPDATE_INTERVAL_MS) {
						publishProgress(new DeterminateProgressAction(counter, total));
						lastUpdate = now;
					}
				}
			};

			AndokuClient client = new AndokuClient(WebImportActivity.this);
			final Uri uri = client.insertPuzzles(folderUri, valuesArray, observer);

			publishProgress(new DeterminateProgressAction(total, total));

			return uri;
		}

		private List<ContentValues> toContentValues(ArrayList<String> puzzles) {
			List<ContentValues> values = new ArrayList<ContentValues>(puzzles.size());

			for (String clues : puzzles) {
				ContentValues contentValues = new ContentValues();
				contentValues.put(AndokuClient.KEY_CLUES, clues);

				values.add(contentValues);
			}

			return values;
		}
	}

	private abstract class Action {
		protected abstract void run();
	}

	private final class SetTitleAction extends Action {
		private final String title;

		public SetTitleAction(String title) {
			this.title = title;
		}

		@Override
		protected void run() {
			progressTitle.setText(title);
		}
	}

	private final class IndeterminateProgressAction extends Action {
		private final int value;

		public IndeterminateProgressAction(int value) {
			this.value = value;
		}

		@Override
		protected void run() {
			progressBar.setIndeterminate(true);
			progressText.setText(String.valueOf(value));
		}
	}

	private final class DeterminateProgressAction extends Action {
		private final int value;
		private final int total;

		public DeterminateProgressAction(int value, int total) {
			this.value = value;
			this.total = total;
		}

		@Override
		protected void run() {
			progressBar.setIndeterminate(false);
			progressBar.setProgress(value);
			progressBar.setMax(total);
			progressText.setText(value + "/" + total);
		}
	}
}
