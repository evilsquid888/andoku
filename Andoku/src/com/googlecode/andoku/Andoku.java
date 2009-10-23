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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.googlecode.andoku.db.GameStatistics;
import com.googlecode.andoku.db.SaveGameDb;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.Difficulty;
import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleResolver;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.source.PuzzleType;

public class Andoku extends Activity implements OnTouchListener, OnKeyListener, TickListener {
	private static final String TAG = Andoku.class.getName();

	private static final int DIALOG_CONFIRM_RESET_PUZZLE = 0;
	private static final int DIALOG_PUZZLE_IO_ERROR = 1;

	private static final int MENU_CHECK_PUZZLE = 0;
	private static final int MENU_PAUSE_RESUME_PUZZLE = 1;
	private static final int MENU_RESET_PUZZLE = 2;

	private static final String APP_STATE_PUZZLE_ID = "puzzleId";
	private static final String APP_STATE_GAME_STATE = "gameState";

	private static final int GAME_STATE_NEW_ACTIVITY_STARTED = 0;
	private static final int GAME_STATE_ACTIVITY_STATE_RESTORED = 1;
	private static final int GAME_STATE_READY = 2;
	private static final int GAME_STATE_PLAYING = 3;
	private static final int GAME_STATE_SOLVED = 4;
	private static final int GAME_STATE_ERROR = 5;

	private int gameState;

	private SaveGameDb saveGameDb;

	private Vibrator vibrator;

	private WakeLock wakeLock;

	private PuzzleHolder puzzleHolder;
	private AndokuPuzzle puzzle;

	private TickTimer timer = new TickTimer(this);

	private ViewGroup background;
	private TextView puzzleNameView;
	private TextView puzzleDifficultyView;
	private TextView puzzleSourceView;
	private AndokuPuzzleView andokuView;
	private FingertipView fingertipView;
	private TextView timerView;
	private ViewGroup keyPad;
	private ToggleButton[] keyPadButtons;
	private TextView congratsView;
	private Button dismissCongratsButton;
	private ImageButton backButton;
	private ImageButton nextButton;
	private Button startButton;

	private Toast toast;

	private final int[] andokuViewScreenLocation = new int[2];
	private final int[] fingertipViewScreenLocation = new int[2];

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		super.onCreate(savedInstanceState);

		Util.setFullscreenWorkaround(this);

		setContentView(R.layout.andoku);

		saveGameDb = new SaveGameDb(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// TODO: preferences for wakelock
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

		background = (ViewGroup) findViewById(R.id.background);

		puzzleNameView = (TextView) findViewById(R.id.labelPuzzleName);
		puzzleDifficultyView = (TextView) findViewById(R.id.labelPuzzleDifficulty);
		puzzleSourceView = (TextView) findViewById(R.id.labelPuzzleSource);

		andokuView = (AndokuPuzzleView) findViewById(R.id.viewPuzzle);
		andokuView.setOnKeyListener(this);

		fingertipView = (FingertipView) findViewById(R.id.viewFingertip);
		fingertipView.setOnTouchListener(this);

		timerView = (TextView) findViewById(R.id.labelTimer);

		keyPad = (ViewGroup) findViewById(R.id.keyPad);

		keyPadButtons = new ToggleButton[9];
		keyPadButtons[0] = (ToggleButton) findViewById(R.id.input_1);
		keyPadButtons[1] = (ToggleButton) findViewById(R.id.input_2);
		keyPadButtons[2] = (ToggleButton) findViewById(R.id.input_3);
		keyPadButtons[3] = (ToggleButton) findViewById(R.id.input_4);
		keyPadButtons[4] = (ToggleButton) findViewById(R.id.input_5);
		keyPadButtons[5] = (ToggleButton) findViewById(R.id.input_6);
		keyPadButtons[6] = (ToggleButton) findViewById(R.id.input_7);
		keyPadButtons[7] = (ToggleButton) findViewById(R.id.input_8);
		keyPadButtons[8] = (ToggleButton) findViewById(R.id.input_9);

		for (int i = 0; i < 9; i++) {
			final int digit = i;
			keyPadButtons[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onKeyPad(digit);
				}
			});
		}

		congratsView = (TextView) findViewById(R.id.labelCongrats);

		dismissCongratsButton = (Button) findViewById(R.id.buttonDismissCongrats);
		dismissCongratsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onDismissCongratsButton();
			}
		});

		backButton = (ImageButton) findViewById(R.id.buttonBack);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onGotoPuzzleRelative(-1);
			}
		});

		nextButton = (ImageButton) findViewById(R.id.buttonNext);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onGotoPuzzleRelative(1);
			}
		});

		startButton = (Button) findViewById(R.id.buttonStart);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onStartButton();
			}
		});

		Theme theme = createTheme();
		setTheme(theme);

		createPuzzle(savedInstanceState);
	}

	private Theme createTheme() {
		ColorTheme.Builder builder = new ColorTheme.Builder(getResources());

		// TODO: set colors from preferences or something

		// example for an ugly dark theme
//		builder.backgroudColor = 0xff000000;
//		builder.puzzleBackgroundColor = 0xff333333;
//		builder.nameTextColor = 0xffeeeeee;
//		builder.difficultyTextColor = 0xffeeeeee;
//		builder.sourceTextColor = 0xffeeeeee;
//		builder.timerTextColor = 0xffeeeeee;
//		builder.gridColor = 0x40ffffff;
//		builder.borderColor = 0xffcccccc;
//		builder.extraRegionColor = 0x60ff8dff;
//		builder.valueColor = 0xff88cc88;
//		builder.clueColor = 0xffcccccc;
//		builder.errorColor = 0xffff0000;
//		builder.markedCellColor = 0xa000ff00;
//		builder.markedClueColor = 0xa0ff0000;

		return builder.build();
	}

	private void setTheme(Theme theme) {
		background.setBackgroundDrawable(theme.getBackground());
		puzzleNameView.setTextColor(theme.getNameTextColor());
		puzzleDifficultyView.setTextColor(theme.getDifficultyTextColor());
		puzzleSourceView.setTextColor(theme.getSourceTextColor());
		timerView.setTextColor(theme.getTimerTextColor());
		andokuView.setTheme(theme);
	}

	@Override
	protected void onPause() {
		if (Constants.LOG_V)
			Log.v(TAG, "onPause(" + timer + ")");

		super.onPause();

		if (gameState == GAME_STATE_PLAYING) {
			timer.stop();
			autoSavePuzzle();
		}

		setWakeLock(false);
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume(" + timer + ")");

		super.onResume();

		if (gameState == GAME_STATE_PLAYING) {
			setWakeLock(true);

			timer.start();
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onSaveInstanceState(" + timer + ")");

		super.onSaveInstanceState(outState);

		if (puzzleHolder != null) {
			outState.putString(APP_STATE_PUZZLE_ID, puzzleHolder.getPuzzleId());
			outState.putInt(APP_STATE_GAME_STATE, gameState);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_CHECK_PUZZLE, 0, R.string.menu_check_puzzle).setIcon(R.drawable.check);
		menu.add(0, MENU_PAUSE_RESUME_PUZZLE, 0, "");
		menu.add(0, MENU_RESET_PUZZLE, 0, R.string.menu_reset_puzzle).setIcon(R.drawable.reset);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_CHECK_PUZZLE).setEnabled(gameState == GAME_STATE_PLAYING);

		boolean paused = gameState == GAME_STATE_READY && !puzzle.isSolved() && timer.getTime() > 0;
		menu.findItem(MENU_PAUSE_RESUME_PUZZLE).setTitle(
				paused ? R.string.menu_resume : R.string.menu_pause).setIcon(
				paused ? R.drawable.resume : R.drawable.pause).setEnabled(
				gameState == GAME_STATE_PLAYING || paused);

		menu.findItem(MENU_RESET_PUZZLE).setEnabled(gameState == GAME_STATE_PLAYING);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_CHECK_PUZZLE:
				onCheckPuzzle();
				return true;
			case MENU_PAUSE_RESUME_PUZZLE:
				onPauseResumeGame();
				return true;
			case MENU_RESET_PUZZLE:
				onResetPuzzle(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	void onKeyPad(int digit) {
		if (gameState != GAME_STATE_PLAYING)
			return;

		Position mark = andokuView.getMarkedCell();
		if (mark == null) {
			keyPadButtons[digit].setChecked(false);
			return;
		}

		ValueSet values = puzzle.getValues(mark.row, mark.col);

		if (puzzle.isClue(mark.row, mark.col)) {
			keyPadButtons[digit].setChecked(values.contains(digit));
		}
		else {
			if (values.contains(digit)) {
				values.remove(digit);
				keyPadButtons[digit].setChecked(false);
			}
			else {
				values.add(digit);
				keyPadButtons[digit].setChecked(true);
			}

			setCell(mark, values);
		}

		cancelToast();
	}

	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		if (event.getAction() != KeyEvent.ACTION_DOWN)
			return false;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				moveMark(-1, 0);
				return true;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				moveMark(1, 0);
				return true;

			case KeyEvent.KEYCODE_DPAD_LEFT:
				moveMark(0, -1);
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				moveMark(0, 1);
				return true;

			case KeyEvent.KEYCODE_1:
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_9:
				onKeyPad(keyCode - KeyEvent.KEYCODE_1);
				return true;

			default:
				return false;
		}
	}

	private void moveMark(int dy, int dx) {
		final int size = puzzle.getSize();

		Position mark = andokuView.getMarkedCell();
		int row = mark == null ? size / 2 : mark.row;
		int col = mark == null ? size / 2 : mark.col;

		row += dy;
		if (row == -1)
			row = size - 1;
		if (row == size)
			row = 0;

		col += dx;
		if (col == -1)
			col = size - 1;
		if (col == size)
			col = 0;

		setMark(new Position(row, col));
	}

	private void setMark(Position cell) {
		andokuView.markCell(cell);

		if (puzzle != null) {
			ValueSet values = cell == null ? null : puzzle.getValues(cell.row, cell.col);
			for (int v = 0; v < puzzle.getSize(); v++) {
				keyPadButtons[v].setChecked(values != null && values.contains(v));
			}
		}

		cancelToast();
	}

	public boolean onTouch(View view, MotionEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		// TODO: avoid calling getLocationOnScreen every time..
		fingertipView.getLocationOnScreen(fingertipViewScreenLocation);
		andokuView.getLocationOnScreen(andokuViewScreenLocation);

		// translate event x/y from fingertipView to andokuView coordinates
		float x = event.getX() + fingertipViewScreenLocation[0] - andokuViewScreenLocation[0];
		float y = event.getY() + fingertipViewScreenLocation[1] - andokuViewScreenLocation[1];
		Position cell = andokuView.getCell(x, y, 0.2f);

		int action = event.getAction();

		if (cell == null && action == MotionEvent.ACTION_DOWN)
			return false;

		boolean editable = cell != null && !puzzle.isClue(cell.row, cell.col);

		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			if (cell == null) {
				fingertipView.highlight(null, editable);
			}
			else {
				PointF center = andokuView.getCellCenterPoint(cell);
				// translate center from andokuView to fingertipView coordinates
				center.x += andokuViewScreenLocation[0] - fingertipViewScreenLocation[0];
				center.y += andokuViewScreenLocation[1] - fingertipViewScreenLocation[1];

				fingertipView.highlight(center, editable);
			}

			setMark(null);
		}
		else if (action == MotionEvent.ACTION_UP) {
			fingertipView.highlight(null, false);

			setMark(cell);
		}
		else { // MotionEvent.ACTION_CANCEL
			fingertipView.highlight(null, false);

			setMark(null);
		}

		return true;
	}

	// callback from tick timer
	public void onTick(long time) {
		if (timerView.getVisibility() != View.VISIBLE)
			return;

		timerView.setText(DateUtil.formatTime(time));
	}

	private void onGotoPuzzleRelative(int offset) {
		int puzzleNumber = puzzleHolder.getNumber() + offset;
		int numPuzzles = puzzleHolder.getSource().numberOfPuzzles();
		if (puzzleNumber < 0)
			puzzleNumber = numPuzzles - 1;
		if (puzzleNumber >= numPuzzles)
			puzzleNumber = 0;

		gotoPuzzle(puzzleNumber);
	}

	private void gotoPuzzle(int puzzleNumber) {
		try {
			PuzzleSource puzzleSource = puzzleHolder.getSource();
			PuzzleHolder puzzleHolder = puzzleSource.load(puzzleNumber);

			setPuzzle(puzzleHolder);
			enterGameState(GAME_STATE_READY);
		}
		catch (PuzzleIOException e) {
			Log.e(TAG, "Error loading puzzle", e);
			clearPuzzle();
			enterGameState(GAME_STATE_ERROR);
			showDialog(DIALOG_PUZZLE_IO_ERROR);
		}
	}

	void onCheckPuzzle() {
		if (Constants.LOG_V)
			Log.v(TAG, "onCheckPuzzleButton()");

		boolean errors = puzzle.checkForErrors();

		if (errors) {
			showWarning(R.string.warn_puzzle_errors);
		}
		else {
			int missing = puzzle.getMissingValuesCount();
			if (missing == 1) {
				showInfo(R.string.info_puzzle_ok_1);
			}
			else {
				CharSequence text = getText(R.string.info_puzzle_ok_n);
				showInfo(String.format(text.toString(), puzzle.getMissingValuesCount()));
			}
		}

		andokuView.invalidate();
	}

	void onStartButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onStartButton()");

		if (gameState == GAME_STATE_READY && puzzle.isSolved()) {
			onResetPuzzle(false);
		}
		else {
			enterGameState(GAME_STATE_PLAYING);
		}
	}

	void onDismissCongratsButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDismissCongratsButton()");

		enterGameState(GAME_STATE_READY);
	}

	// callback from an input dialog - only when GAME_STATE_PLAYING
	void setCell(Position cell, ValueSet values) {
		boolean sideEffects = puzzle.setValues(cell.row, cell.col, values);

		if (puzzle.isSolved()) {
			timer.stop();
			autoSavePuzzle();

			enterGameState(GAME_STATE_SOLVED);
			return;
		}

		if (sideEffects)
			andokuView.invalidate();
		else
			andokuView.invalidateCell(cell);
	}

	// callback from reset puzzle dialog
	void onResetPuzzle(boolean confirmed) {
		if (!confirmed && !puzzle.isModified())
			confirmed = true;

		if (!confirmed) {
			showDialog(DIALOG_CONFIRM_RESET_PUZZLE);
		}
		else {
			timer.stop();
			deleteAutoSavedPuzzle();

			setPuzzle(puzzleHolder);
			enterGameState(GAME_STATE_READY);
		}
	}

	private void createPuzzle(Bundle savedInstanceState) {
		try {
			String puzzleId = savedInstanceState == null ? null : savedInstanceState
					.getString(APP_STATE_PUZZLE_ID);

			if (puzzleId == null) {
				puzzleId = getPuzzleIdFromIntent();
				PuzzleHolder puzzleHolder = PuzzleResolver.resolve(this, puzzleId);

				gameState = GAME_STATE_NEW_ACTIVITY_STARTED;

				setPuzzle(puzzleHolder);
				boolean start = getIntent().getBooleanExtra(Constants.EXTRA_START_PUZZLE, false);
				enterGameState(start ? GAME_STATE_PLAYING : GAME_STATE_READY);
			}
			else {
				assert savedInstanceState != null;
				PuzzleHolder puzzleHolder = PuzzleResolver.resolve(this, puzzleId);

				gameState = GAME_STATE_ACTIVITY_STATE_RESTORED;

				setPuzzle(puzzleHolder);
				enterGameState(savedInstanceState.getInt(APP_STATE_GAME_STATE));
			}
		}
		catch (PuzzleIOException e) {
			Log.e(TAG, "Error loading puzzle", e);
			clearPuzzle();
			enterGameState(GAME_STATE_ERROR);
			showDialog(DIALOG_PUZZLE_IO_ERROR);
		}
	}

	private String getPuzzleIdFromIntent() {
		Intent intent = getIntent();
		String puzzleId = intent.getStringExtra(Constants.EXTRA_PUZZLE_ID);
		if (puzzleId != null)
			return puzzleId;

		return "asset:standard_n_1:0";
	}

	private void onPauseResumeGame() {
		// TODO: remove RESUME menu entry?

		if (gameState == GAME_STATE_PLAYING) {
			timer.stop();
			autoSavePuzzle();

			enterGameState(GAME_STATE_READY);
		}
		else if (gameState == GAME_STATE_READY) {
			enterGameState(GAME_STATE_PLAYING);
		}
		else
			throw new IllegalStateException("pause/resume");
	}

	private void enterGameState(int newGameState) {
		if (Constants.LOG_V)
			Log.v(TAG, "enterGameState(" + newGameState + ")");

		setWakeLock(newGameState == GAME_STATE_PLAYING);

		if (newGameState != GAME_STATE_PLAYING)
			setMark(null);

		switch (newGameState) {
			case GAME_STATE_READY:
				if (puzzle.isSolved())
					startButton.setText(R.string.button_reset_game);
				else if (timer.getTime() > 0)
					startButton.setText(R.string.button_resume_game);
				else
					startButton.setText(R.string.button_start_game);
				break;

			case GAME_STATE_PLAYING:
				autoSavePuzzle(); // save for correct 'date-created' timestamp
				timer.start();
				break;

			case GAME_STATE_SOLVED:
				updateCongrats();
				break;

			case GAME_STATE_ERROR:
				timer.reset();
				break;

			default:
				throw new IllegalStateException();
		}

		congratsView.setVisibility(newGameState == GAME_STATE_SOLVED ? View.VISIBLE : View.GONE);

		dismissCongratsButton.setVisibility(newGameState == GAME_STATE_SOLVED
				? View.VISIBLE
				: View.GONE);

		int visibilityBackStartNext = newGameState == GAME_STATE_READY ? View.VISIBLE : View.GONE;
		backButton.setVisibility(visibilityBackStartNext);
		startButton.setVisibility(visibilityBackStartNext);
		nextButton.setVisibility(visibilityBackStartNext);

		boolean enableNav = newGameState == GAME_STATE_READY;
		backButton.setEnabled(enableNav);
		nextButton.setEnabled(enableNav);

		keyPad.setVisibility(newGameState == GAME_STATE_PLAYING ? View.VISIBLE : View.GONE);

		andokuView.setPaused(newGameState == GAME_STATE_READY && !puzzle.isSolved()
				&& timer.getTime() > 0);
		andokuView.setPreview(newGameState == GAME_STATE_READY);

		if (gameState != newGameState)
			andokuView.invalidate();

		this.gameState = newGameState;
	}

	private void setWakeLock(boolean lock) {
		if (lock) {
			if (!wakeLock.isHeld()) {
				if (Constants.LOG_V)
					Log.v(TAG, "acquire wakeLock");

				wakeLock.acquire();
			}
		}
		else {
			if (wakeLock.isHeld()) {
				if (Constants.LOG_V)
					Log.v(TAG, "release wakeLock");

				wakeLock.release();
			}
		}
	}

	private void updateCongrats() {
		String puzzleSourceId = puzzleHolder.getSource().getSourceId();
		GameStatistics stats = saveGameDb.getStatistics(puzzleSourceId);

		final Resources resources = getResources();
		PuzzleType puzzleType = puzzleHolder.getPuzzleType();
		String difficulty = getPuzzleDifficulty();
		String name = resources.getString(puzzleType.getNameResId());

		final String format = resources.getString(R.string.message_congrats);
		final String message = String.format(format, name, difficulty, stats.numGamesSolved, DateUtil
				.formatTime(stats.getAverageTime()), DateUtil.formatTime(stats.minTime));
		congratsView.setText(Html.fromHtml(message));
	}

	private void setPuzzle(PuzzleHolder puzzleHolder) {
		this.puzzleHolder = puzzleHolder;
		this.puzzle = AndokuPuzzle.create(puzzleHolder);
		andokuView.setPuzzle(this.puzzle);

		puzzleNameView.setText(getPuzzleName());
		puzzleDifficultyView.setText(getPuzzleDifficulty());
		puzzleSourceView.setText(getPuzzleSource());

		if (!restoreAutoSavedPuzzle()) {
			Log.w(TAG, "unable to restore auto-saved puzzle");
			timer.reset();
		}
	}

	private String getPuzzleName() {
		final Resources resources = getResources();
		PuzzleType puzzleType = puzzleHolder.getPuzzleType();
		return resources.getString(puzzleType.getNameResId());
	}

	private String getPuzzleSource() {
		return "#" + (puzzleHolder.getNumber() + 1) + "/"
				+ puzzleHolder.getSource().numberOfPuzzles();
	}

	private String getPuzzleDifficulty() {
		final Difficulty difficulty = puzzleHolder.getPuzzleDifficulty();
		if (difficulty == Difficulty.UNKNOWN)
			return "";

		final Resources resources = getResources();
		String[] difficulties = resources.getStringArray(R.array.difficulties);
		return difficulties[difficulty.ordinal()];
	}

	private void clearPuzzle() {
		this.puzzleHolder = null;
		this.puzzle = null;
		andokuView.setPuzzle(null);

		puzzleNameView.setText(R.string.name_no_puzzle);
		puzzleDifficultyView.setText("");
		puzzleSourceView.setText("");

		timer.reset();
	}

	private void autoSavePuzzle() {
		String puzzleId = puzzleHolder.getPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "auto-saving puzzle " + puzzleId);

		saveGameDb.saveGame(puzzleId, puzzle, timer);
	}

	private void deleteAutoSavedPuzzle() {
		String puzzleId = puzzleHolder.getPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "deleting auto-save game " + puzzleId);

		saveGameDb.delete(puzzleId);
	}

	private boolean restoreAutoSavedPuzzle() {
		String puzzleId = puzzleHolder.getPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "restoring auto-save game " + puzzleId);

		return saveGameDb.loadGame(puzzleId, puzzle, timer);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONFIRM_RESET_PUZZLE:
				return createConfirmResetPuzzleDialog();
			case DIALOG_PUZZLE_IO_ERROR:
				return createPuzzleIoErrorDialog();
			default:
				return null;
		}
	}

	private Dialog createConfirmResetPuzzleDialog() {
		return new AlertDialog.Builder(this).setIcon(R.drawable.alert_dialog_icon).setTitle(
				R.string.dialog_reset_puzzle).setMessage(R.string.message_reset_puzzle)
				.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						onResetPuzzle(true);
					}
				}).setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						}).create();
	}

	private Dialog createPuzzleIoErrorDialog() {
		return new AlertDialog.Builder(this).setIcon(R.drawable.alert_dialog_icon).setTitle(
				R.string.dialog_io_error).setMessage(R.string.message_error_loading_puzzle)
				.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create();
	}

	private void showInfo(int resId) {
		showInfo(getText(resId));
	}

	private void showInfo(CharSequence message) {
		showToast(message, false);
	}

	private void showWarning(int resId) {
		showWarning(getText(resId));
	}

	private void showWarning(CharSequence message) {
		vibrator.vibrate(new long[] { 0, 80, 80, 120 }, -1);
		showToast(message, true);
	}

	private void showToast(CharSequence message, boolean warning) {
		cancelToast();

		toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		// TODO: change toast background to indicate warning..
		toast.show();
	}

	private void cancelToast() {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
	}
}
