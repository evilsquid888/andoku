package com.googlecode.andoku;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.source.PuzzleSourceIds;

public class FolderListActivity extends ListActivity {
	private static final String TAG = FolderListActivity.class.getName();

	private AndokuDatabase db;

	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		super.onCreate(savedInstanceState);

		Util.setFullscreenWorkaround(this);

		setContentView(R.layout.folders);

		long folderId = getFolderIdFromIntent();

		db = new AndokuDatabase(this);
		Cursor cursor = db.getFolders(folderId);
		startManagingCursor(cursor);

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursor,
				new String[] { AndokuDatabase.COL_FOLDER_NAME }, new int[] { android.R.id.text1 });
		setListAdapter(listAdapter);

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onOpenFolder(id);
			}
		});

		Util.saveSetOnClickListener(findViewById(R.id.backButton), new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private long getFolderIdFromIntent() {
		Bundle extras = getIntent().getExtras();
		return extras.getLong(Constants.EXTRA_FOLDER_ID, AndokuDatabase.ROOT_FOLDER_ID);
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

	void onOpenFolder(long folderId) {
		cancelToast();

		if (!db.hasPuzzles(folderId)) {
			String message = getResources().getString(R.string.message_empty_folder,
					db.getFolderName(folderId));
			toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		String puzzleSourceId = PuzzleSourceIds.forDbFolder(folderId);

		Intent intent = new Intent(this, AndokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, 0); // TODO: find new puzzle
		startActivity(intent);
	}

	private void cancelToast() {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
	}
}
