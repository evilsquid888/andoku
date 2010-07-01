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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
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

import com.googlecode.andoku.commands.AndokuContext;
import com.googlecode.andoku.commands.EliminateValuesCommand;
import com.googlecode.andoku.commands.SetValuesCommand;
import com.googlecode.andoku.db.AndokuDatabase;
import com.googlecode.andoku.db.GameStatistics;
import com.googlecode.andoku.db.PuzzleId;
import com.googlecode.andoku.history.Command;
import com.googlecode.andoku.history.History;
import com.googlecode.andoku.im.InputMethod;
import com.googlecode.andoku.im.InputMethodTarget;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Difficulty;
import com.googlecode.andoku.model.Position;
import com.googlecode.andoku.model.PuzzleType;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.source.PuzzleSourceIds;
import com.googlecode.andoku.source.PuzzleSourceResolver;

public class AndokuActivity extends Activity
		implements OnTouchListener, OnKeyListener, TickListener {
	private static final String TAG = AndokuActivity.class.getName();

	private static final int DIALOG_CONFIRM_RESET_PUZZLE = 0;

	private static final int MENU_CHECK_PUZZLE = 0;
	private static final int MENU_PAUSE_RESUME_PUZZLE = 1;
	private static final int MENU_ELIMINATE_VALUES = 2;
	private static final int MENU_RESET_PUZZLE = 3;
	private static final int MENU_SETTINGS = 4;

	private static final String APP_STATE_PUZZLE_SOURCE_ID = "puzzleSourceId";
	private static final String APP_STATE_PUZZLE_NUMBER = "puzzleNumber";
	private static final String APP_STATE_GAME_STATE = "gameState";
	private static final String APP_STATE_HIGHLIGHTED_DIGIT = "highlightedDigit";
	private static final String APP_STATE_HISTORY = "history";

	private static final int REQUEST_CODE_SETTINGS = 0;

	private static final int GAME_STATE_NEW_ACTIVITY_STARTED = 0;
	private static final int GAME_STATE_ACTIVITY_STATE_RESTORED = 1;
	private static final int GAME_STATE_READY = 2;
	private static final int GAME_STATE_PLAYING = 3;
	private static final int GAME_STATE_SOLVED = 4;

	private int gameState;

	private AndokuDatabase db;

	private Vibrator vibrator;

	private PuzzleSource source;
	private int puzzleNumber;
	private AndokuPuzzle puzzle;
	private TickTimer timer = new TickTimer(this);

	private History<AndokuContext> history = new History<AndokuContext>(new AndokuContext() {
		public TickTimer getTimer() {
			return timer;
		}

		public AndokuPuzzle getPuzzle() {
			return puzzle;
		}
	});

	private ViewGroup background;
	private FingertipPopup fingertipPopup;
	private TextView puzzleNameView;
	private TextView puzzleDifficultyView;
	private TextView puzzleSourceView;
	private AndokuPuzzleView andokuView;
	private TextView timerView;
	private ViewGroup keypad;
	private KeypadToggleButton[] keypadToggleButtons;
	private TextView congratsView;
	private Button dismissCongratsButton;
	private ImageButton backButton;
	private ImageButton nextButton;
	private Button startOrResetButton;
	private ImageButton undoButton;
	private ImageButton redoButton;

	private Toast toast;

	private final int[] backgroundScreenLocation = new int[2];
	private final int[] andokuViewScreenLocation = new int[2];

	private final InputMethodTarget inputMethodTarget = new InputMethodTarget() {
		public int getPuzzleSize() {
			return puzzle.getSize();
		}
		public Position getMarkedCell() {
			return andokuView.getMarkedCell();
		}
		public void setMarkedCell(Position cell) {
			andokuView.markCell(cell);
			cancelToast();
		}
		public boolean isClue(Position cell) {
			return puzzle.isClue(cell.row, cell.col);
		}
		public ValueSet getCellValues(Position cell) {
			return puzzle.getValues(cell.row, cell.col);
		}
		public void setCellValues(Position cell, ValueSet values) {
			setCell(cell, values);
		}
		public int getNumberOfDigitButtons() {
			return keypadToggleButtons.length;
		}
		public void checkButton(int digit, boolean checked) {
			keypadToggleButtons[digit].setChecked(checked);
		}
		public void highlightDigit(Integer digit) {
			andokuView.highlightDigit(digit);
		}
	};

	private InputMethodPolicy inputMethodPolicy;
	private InputMethod inputMethod;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.andoku);

		db = new AndokuDatabase(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		background = (ViewGroup) findViewById(R.id.background);
		background.setOnTouchListener(this);

		fingertipPopup = new FingertipPopup(background);

		puzzleNameView = (TextView) findViewById(R.id.labelPuzzleName);
		puzzleDifficultyView = (TextView) findViewById(R.id.labelPuzzleDifficulty);
		puzzleSourceView = (TextView) findViewById(R.id.labelPuzzleSource);

		andokuView = (AndokuPuzzleView) findViewById(R.id.viewPuzzle);
		andokuView.setOnKeyListener(this);

		timerView = (TextView) findViewById(R.id.labelTimer);

		keypad = (ViewGroup) findViewById(R.id.keypad);

		keypadToggleButtons = new KeypadToggleButton[9];
		keypadToggleButtons[0] = (KeypadToggleButton) findViewById(R.id.input_1);
		keypadToggleButtons[1] = (KeypadToggleButton) findViewById(R.id.input_2);
		keypadToggleButtons[2] = (KeypadToggleButton) findViewById(R.id.input_3);
		keypadToggleButtons[3] = (KeypadToggleButton) findViewById(R.id.input_4);
		keypadToggleButtons[4] = (KeypadToggleButton) findViewById(R.id.input_5);
		keypadToggleButtons[5] = (KeypadToggleButton) findViewById(R.id.input_6);
		keypadToggleButtons[6] = (KeypadToggleButton) findViewById(R.id.input_7);
		keypadToggleButtons[7] = (KeypadToggleButton) findViewById(R.id.input_8);
		keypadToggleButtons[8] = (KeypadToggleButton) findViewById(R.id.input_9);

		for (int i = 0; i < 9; i++) {
			final int digit = i;
			keypadToggleButtons[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onKeypad(digit);
				}
			});
		}

		KeypadButton clearButton = (KeypadButton) findViewById(R.id.input_clear);
		clearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClear();
			}
		});

		KeypadButton invertButton = (KeypadButton) findViewById(R.id.input_invert);
		invertButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onInvert();
			}
		});

		undoButton = (ImageButton) findViewById(R.id.input_undo);
		undoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onUndo();
			}
		});

		redoButton = (ImageButton) findViewById(R.id.input_redo);
		redoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onRedo();
			}
		});

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

		startOrResetButton = (Button) findViewById(R.id.buttonStart);
		startOrResetButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onStartOrResetButton();
			}
		});

		createThemeFromPreferences();

		createPuzzle(savedInstanceState);

		createInputMethod();

		if (isRestoreSavedInstanceState(savedInstanceState)) {
			inputMethod.onRestoreInstanceState(savedInstanceState);

			if (savedInstanceState.containsKey(APP_STATE_HIGHLIGHTED_DIGIT))
				andokuView.highlightDigit(savedInstanceState.getInt(APP_STATE_HIGHLIGHTED_DIGIT));

			history.restoreInstanceState(savedInstanceState.getBundle(APP_STATE_HISTORY));
			undoButton.setEnabled(history.canUndo());
			redoButton.setEnabled(history.canRedo());
		}
	}

	private void createThemeFromPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		ColorTheme.Builder builder = new ColorTheme.Builder(getResources());

		builder.areaColorPolicy = AreaColorPolicy.valueOf(settings.getString(
				Settings.KEY_COLORED_REGIONS, AreaColorPolicy.STANDARD_X_HYPER_SQUIGGLY.name()));
		builder.highlightDigitsPolicy = HighlightDigitsPolicy.valueOf(settings.getString(
				Settings.KEY_HIGHLIGHT_DIGITS, HighlightDigitsPolicy.ONLY_SINGLE_VALUES.name()));

		ColorThemePolicy colorThemePolicy = ColorThemePolicy.valueOf(settings.getString(
				Settings.KEY_COLOR_THEME, ColorThemePolicy.CLASSIC.name()));
		colorThemePolicy.customize(builder);

		Theme theme = builder.build();

		setTheme(theme);
	}

	private void setTheme(Theme theme) {
		background.setBackgroundDrawable(theme.getBackground());
		puzzleNameView.setTextColor(theme.getNameTextColor());
		puzzleDifficultyView.setTextColor(theme.getDifficultyTextColor());
		puzzleSourceView.setTextColor(theme.getSourceTextColor());
		timerView.setTextColor(theme.getTimerTextColor());
		andokuView.setTheme(theme);
	}

	private void createInputMethod() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		InputMethodPolicy inputMethodPolicy = InputMethodPolicy.valueOf(settings.getString(
				Settings.KEY_INPUT_METHOD, InputMethodPolicy.CELL_THEN_VALUES.name()));
		if (inputMethodPolicy != this.inputMethodPolicy) {
			this.inputMethodPolicy = inputMethodPolicy;
			this.inputMethod = inputMethodPolicy.createInputMethod(inputMethodTarget);
			this.inputMethod.reset();
		}
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

		setKeepScreenOn(false);
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume(" + timer + ")");

		super.onResume();

		if (gameState == GAME_STATE_PLAYING) {
			setKeepScreenOn(true);

			timer.start();
		}
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (source != null) {
			source.close();
		}

		if (db != null) {
			db.close();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onSaveInstanceState(" + timer + ")");

		super.onSaveInstanceState(outState);

		if (source != null) {
			outState.putString(APP_STATE_PUZZLE_SOURCE_ID, source.getSourceId());
			outState.putInt(APP_STATE_PUZZLE_NUMBER, puzzleNumber);
			outState.putInt(APP_STATE_GAME_STATE, gameState);

			Integer highlightedDigit = andokuView.getHighlightedDigit();
			if (highlightedDigit != null)
				outState.putInt(APP_STATE_HIGHLIGHTED_DIGIT, highlightedDigit);

			outState.putBundle(APP_STATE_HISTORY, history.saveInstanceState());

			inputMethod.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Constants.LOG_V)
			Log.v(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");

		switch (requestCode) {
			case REQUEST_CODE_SETTINGS:
				onReturnedFromSettings();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_CHECK_PUZZLE, 0, R.string.menu_check_puzzle).setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, MENU_PAUSE_RESUME_PUZZLE, 0, "");
		menu.add(0, MENU_ELIMINATE_VALUES, 0, R.string.menu_eliminate_values).setIcon(
				R.drawable.ic_menu_eliminate);
		menu.add(0, MENU_RESET_PUZZLE, 0, R.string.menu_reset_puzzle).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_CHECK_PUZZLE).setVisible(gameState == GAME_STATE_PLAYING);

		boolean paused = gameState == GAME_STATE_READY && !puzzle.isSolved() && timer.getTime() > 0;
		menu.findItem(MENU_PAUSE_RESUME_PUZZLE).setTitle(
				paused ? R.string.menu_resume : R.string.menu_pause).setIcon(
				paused ? R.drawable.ic_menu_resume : R.drawable.ic_menu_pause).setVisible(
				gameState == GAME_STATE_PLAYING || paused);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		menu.findItem(MENU_ELIMINATE_VALUES).setVisible(
				gameState == GAME_STATE_PLAYING
						&& settings.getBoolean(Settings.KEY_ENABLE_ELIMINATE_VALUES, false)).setEnabled(
				puzzle.canEliminateValues());

		menu.findItem(MENU_RESET_PUZZLE).setVisible(gameState == GAME_STATE_PLAYING);

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
			case MENU_ELIMINATE_VALUES:
				onEliminateValues();
				return true;
			case MENU_RESET_PUZZLE:
				onResetPuzzle(false);
				return true;
			case MENU_SETTINGS:
				onSettings();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	void onKeypad(int digit) {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onKeypad(digit);

		cancelToast();
	}

	void onClear() {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onClear();

		cancelToast();
	}

	void onInvert() {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onInvert();

		cancelToast();
	}

	void onUndo() {
		if (history.undo())
			onCommandExecuted();
	}

	void onRedo() {
		if (history.redo())
			onCommandExecuted();
	}

	private void setCell(Position cell, ValueSet values) {
		execute(new SetValuesCommand(cell, values));
	}

	private void execute(Command<AndokuContext> command) {
		if (history.execute(command))
			onCommandExecuted();
	}

	private void onCommandExecuted() {
		undoButton.setEnabled(history.canUndo());
		redoButton.setEnabled(history.canRedo());

		if (puzzle.isCompletelyFilled()) {
			if (puzzle.isSolved()) {
				timer.stop();
				autoSavePuzzle();

				enterGameState(GAME_STATE_SOLVED);
				return;
			}
			else {
				showInfo(R.string.info_invalid_solution);
			}
		}

		updateKeypadHighlighing();

		andokuView.invalidate();

		inputMethod.onValuesChanged();
	}

	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		if (event.getAction() != KeyEvent.ACTION_DOWN)
			return false;

		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				inputMethod.onMoveMark(-1, 0);
				return true;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				inputMethod.onMoveMark(1, 0);
				return true;

			case KeyEvent.KEYCODE_DPAD_LEFT:
				inputMethod.onMoveMark(0, -1);
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				inputMethod.onMoveMark(0, 1);
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
				onKeypad(keyCode - KeyEvent.KEYCODE_1);
				return true;

			default:
				return false;
		}
	}

	public boolean onTouch(View view, MotionEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		background.getLocationOnScreen(backgroundScreenLocation);
		andokuView.getLocationOnScreen(andokuViewScreenLocation);

		// translate event x/y from background coordinates to andokuView coordinates
		float x = event.getX() + backgroundScreenLocation[0] - andokuViewScreenLocation[0];
		float y = event.getY() + backgroundScreenLocation[1] - andokuViewScreenLocation[1];
		Position cell = andokuView.getCell(x, y, 0.5f);

		int action = event.getAction();

		if (cell == null && action == MotionEvent.ACTION_DOWN)
			return false;

		boolean editable = cell != null && !puzzle.isClue(cell.row, cell.col);

		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			if (cell == null) {
				fingertipPopup.hide();
			}
			else {
				PointF center = andokuView.getCellCenterPoint(cell);
				// translate center from andokuView coordinates to screen coordinates
				center.x += andokuViewScreenLocation[0];
				center.y += andokuViewScreenLocation[1];

				fingertipPopup.show(center, editable);
			}

			inputMethod.onSweep();
		}
		else if (action == MotionEvent.ACTION_UP) {
			fingertipPopup.hide();

			inputMethod.onTap(cell, editable);
		}
		else { // MotionEvent.ACTION_CANCEL
			fingertipPopup.hide();

			inputMethod.onSweep();
		}

		return true;
	}

	// callback from tick timer
	public void onTick(long time) {
		timerView.setText(DateUtil.formatTime(time));
	}

	private void onGotoPuzzleRelative(int offset) {
		int number = puzzleNumber + offset;
		int numPuzzles = source.numberOfPuzzles();
		if (number < 0)
			number = numPuzzles - 1;
		if (number >= numPuzzles)
			number = 0;

		gotoPuzzle(number);
	}

	private void gotoPuzzle(int number) {
		try {
			setPuzzle(number);

			inputMethod.reset();

			enterGameState(GAME_STATE_READY);
		}
		catch (PuzzleIOException e) {
			handlePuzzleIOException(e);
		}
	}

	void onCheckPuzzle() {
		if (Constants.LOG_V)
			Log.v(TAG, "onCheckPuzzle()");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean checkAgainstSolution = settings.getBoolean(Settings.KEY_CHECK_AGAINST_SOLUTION, true);

		if (checkAgainstSolution) {
			if (puzzle.hasSolution()) {
				checkPuzzle(true);
			}
			else {
				new ComputeSolutionAndCheckPuzzleTask().execute();
			}
		}
		else {
			checkPuzzle(false);
		}
	}

	private void checkPuzzle(boolean checkAgainstSolution) {
		boolean errors = puzzle.checkForErrors(checkAgainstSolution);

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

	void onStartOrResetButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onStartOrResetButton()");

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

			gotoPuzzle(puzzleNumber);
		}
	}

	void onEliminateValues() {
		execute(new EliminateValuesCommand());
	}

	void onSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SETTINGS);
	}

	private void onReturnedFromSettings() {
		createThemeFromPreferences();

		createInputMethod();

		setTimerVisibility(gameState);

		andokuView.invalidate();
	}

	private void createPuzzle(Bundle savedInstanceState) {
		try {
			if (isRestoreSavedInstanceState(savedInstanceState))
				createPuzzleFromSavedInstanceState(savedInstanceState);
			else
				createPuzzleFromIntent();
		}
		catch (PuzzleIOException e) {
			handlePuzzleIOException(e);
		}
	}

	private void handlePuzzleIOException(PuzzleIOException e) {
		Log.e(TAG, "Error loading puzzle", e);

		Resources resources = getResources();
		String title = resources.getString(R.string.error_title_io_error);
		String message = getResources().getString(R.string.error_message_loading_puzzle);

		Intent intent = new Intent(this, DisplayErrorActivity.class);
		intent.putExtra(Constants.EXTRA_ERROR_TITLE, title);
		intent.putExtra(Constants.EXTRA_ERROR_MESSAGE, message);
		intent.putExtra(Constants.EXTRA_ERROR_THROWABLE, e);
		startActivity(intent);

		finish();
	}

	private boolean isRestoreSavedInstanceState(Bundle savedInstanceState) {
		return savedInstanceState != null
				&& savedInstanceState.getString(APP_STATE_PUZZLE_SOURCE_ID) != null;
	}

	private void createPuzzleFromSavedInstanceState(Bundle savedInstanceState)
			throws PuzzleIOException {
		String puzzleSourceId = savedInstanceState.getString(APP_STATE_PUZZLE_SOURCE_ID);
		int number = savedInstanceState.getInt(APP_STATE_PUZZLE_NUMBER);

		if (Constants.LOG_V)
			Log.v(TAG, "createPuzzleFromSavedInstanceState(): " + puzzleSourceId + ":" + number);

		initializePuzzle(puzzleSourceId, number);

		gameState = GAME_STATE_ACTIVITY_STATE_RESTORED;
		enterGameState(savedInstanceState.getInt(APP_STATE_GAME_STATE));
	}

	private void createPuzzleFromIntent() throws PuzzleIOException {
		final Intent intent = getIntent();
		String puzzleSourceId = intent.getStringExtra(Constants.EXTRA_PUZZLE_SOURCE_ID);
		if (puzzleSourceId == null)
			puzzleSourceId = PuzzleSourceIds.forAssetFolder("standard_n_1");
		int number = intent.getIntExtra(Constants.EXTRA_PUZZLE_NUMBER, 0);

		if (Constants.LOG_V)
			Log.v(TAG, "createPuzzleFromIntent(): " + puzzleSourceId + ":" + number);

		initializePuzzle(puzzleSourceId, number);

		gameState = GAME_STATE_NEW_ACTIVITY_STARTED;
		boolean start = getIntent().getBooleanExtra(Constants.EXTRA_START_PUZZLE, false);
		enterGameState(start ? GAME_STATE_PLAYING : GAME_STATE_READY);
	}

	private void initializePuzzle(String puzzleSourceId, int number) throws PuzzleIOException {
		source = PuzzleSourceResolver.resolveSource(this, puzzleSourceId);

		setPuzzle(number);
	}

	private void setPuzzle(int number) throws PuzzleIOException {
		puzzleNumber = number;

		puzzle = createAndokuPuzzle(number);
		history.clear();
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);

		andokuView.setPuzzle(puzzle);

		puzzleNameView.setText(getPuzzleName());
		puzzleDifficultyView.setText(getPuzzleDifficulty());
		puzzleSourceView.setText(getPuzzleSource());

		if (!restoreAutoSavedPuzzle()) {
			Log.w(TAG, "unable to restore auto-saved puzzle");
			timer.reset();
		}
	}

	private AndokuPuzzle createAndokuPuzzle(int number) throws PuzzleIOException {
		PuzzleHolder holder = source.load(number);
		return new AndokuPuzzle(holder.getName(), holder.getPuzzle(), holder.getDifficulty());
	}

	private String getPuzzleName() {
		String name = puzzle.getName();
		if (name != null && name.length() > 0)
			return name;

		PuzzleType puzzleType = puzzle.getPuzzleType();
		return Util.getPuzzleName(getResources(), puzzleType);
	}

	private String getPuzzleDifficulty() {
		final Difficulty difficulty = puzzle.getDifficulty();
		if (difficulty == Difficulty.UNKNOWN)
			return "";

		final Resources resources = getResources();
		String[] difficulties = resources.getStringArray(R.array.difficulties);
		return difficulties[difficulty.ordinal()];
	}

	private String getPuzzleSource() {
		final String suffix = "#" + (puzzleNumber + 1) + "/" + source.numberOfPuzzles();

		final String sourceId = source.getSourceId();
		if (PuzzleSourceIds.isDbSource(sourceId)) {
			return Util.getFolderName(db, sourceId) + " " + suffix;
		}
		else {
			return suffix;
		}
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

		setKeepScreenOn(newGameState == GAME_STATE_PLAYING);

		switch (newGameState) {
			case GAME_STATE_READY:
				if (puzzle.isSolved())
					startOrResetButton.setText(R.string.button_reset_game);
				else if (timer.getTime() > 0)
					startOrResetButton.setText(R.string.button_resume_game);
				else
					startOrResetButton.setText(R.string.button_start_game);
				break;

			case GAME_STATE_PLAYING:
				autoSavePuzzle(); // save for correct 'date-created' timestamp
				timer.start();
				break;

			case GAME_STATE_SOLVED:
				updateCongrats();
				break;

			default:
				throw new IllegalStateException();
		}

		boolean showNameAndDifficulty = newGameState != GAME_STATE_PLAYING
				|| hasSufficientVerticalSpace();
		puzzleNameView.setVisibility(showNameAndDifficulty ? View.VISIBLE : View.GONE);
		puzzleDifficultyView.setVisibility(showNameAndDifficulty ? View.VISIBLE : View.GONE);

		congratsView.setVisibility(newGameState == GAME_STATE_SOLVED ? View.VISIBLE : View.GONE);

		dismissCongratsButton.setVisibility(newGameState == GAME_STATE_SOLVED
				? View.VISIBLE
				: View.GONE);

		int visibilityBackStartNext = newGameState == GAME_STATE_READY ? View.VISIBLE : View.GONE;
		backButton.setVisibility(visibilityBackStartNext);
		startOrResetButton.setVisibility(visibilityBackStartNext);
		nextButton.setVisibility(visibilityBackStartNext);

		boolean enableNav = newGameState == GAME_STATE_READY;
		backButton.setEnabled(enableNav);
		nextButton.setEnabled(enableNav);

		final boolean showKeypad = newGameState == GAME_STATE_PLAYING;
		keypad.setVisibility(showKeypad ? View.VISIBLE : View.GONE);
		if (showKeypad)
			updateKeypadHighlighing();

		andokuView.setPaused(newGameState == GAME_STATE_READY && !puzzle.isSolved()
				&& timer.getTime() > 0);
		andokuView.setPreview(newGameState == GAME_STATE_READY);

		setTimerVisibility(newGameState);

		if (gameState != newGameState)
			andokuView.invalidate();

		this.gameState = newGameState;
	}

	// 320x240 device (e.g. HTC Tattoo) does not have enough vertical space to display title and name) 
	private boolean hasSufficientVerticalSpace() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float aspect = ((float) displayMetrics.heightPixels) / displayMetrics.widthPixels;
		return aspect >= 480f / 320;
	}

	private void setTimerVisibility(int forGameState) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		boolean showTimerWhilePlaying = settings.getBoolean(Settings.KEY_SHOW_TIMER, true);
		boolean showTimer = showTimerWhilePlaying || forGameState != GAME_STATE_PLAYING;
		timerView.setVisibility(showTimer ? View.VISIBLE : View.INVISIBLE);
	}

	private void setKeepScreenOn(boolean keepScreenOn) {
		andokuView.setKeepScreenOn(keepScreenOn);
	}

	private void updateCongrats() {
		String congrats = getResources().getString(R.string.message_congrats);
		String title = getStatisticsTitle();
		String details = getStatisticsDetails();

		String message = congrats + "<br/><br/>" + title + "<br/><br/>" + details;
		congratsView.setText(Html.fromHtml(message));
	}

	private String getStatisticsTitle() {
		final String sourceId = source.getSourceId();
		if (PuzzleSourceIds.isDbSource(sourceId)) {
			String folderName = Util.getFolderName(db, sourceId);
			return getResources().getString(R.string.message_statistics_title_db, folderName);
		}
		else {
			String name = getPuzzleName();
			String difficulty = getPuzzleDifficulty();
			return getResources()
					.getString(R.string.message_statistics_title_assets, name, difficulty);
		}
	}

	private String getStatisticsDetails() {
		GameStatistics stats = db.getStatistics(source.getSourceId());
		return getResources().getString(R.string.message_statistics_details, stats.numGamesSolved,
				DateUtil.formatTime(stats.getAverageTime()), DateUtil.formatTime(stats.minTime));
	}

	private void updateKeypadHighlighing() {
		final int size = puzzle.getSize();
		int[] counter = new int[size];

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				ValueSet values = puzzle.getValues(row, col);
				if (values.size() == 1) {
					int value = values.nextValue(0);
					counter[value]++;
				}
			}
		}

		for (int digit = 0; digit < size; digit++) {
			final boolean digitCompleted = counter[digit] == size;
			keypadToggleButtons[digit].setHighlighted(digitCompleted);
		}
	}

	private void autoSavePuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "auto-saving puzzle " + puzzleId);

		db.saveGame(puzzleId, puzzle, timer);
	}

	private void deleteAutoSavedPuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "deleting auto-save game " + puzzleId);

		db.delete(puzzleId);
	}

	private boolean restoreAutoSavedPuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "restoring auto-save game " + puzzleId);

		return db.loadGame(puzzleId, puzzle, timer);
	}

	private PuzzleId getCurrentPuzzleId() {
		return new PuzzleId(source.getSourceId(), puzzleNumber);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONFIRM_RESET_PUZZLE:
				return createConfirmResetPuzzleDialog();
			default:
				return null;
		}
	}

	private Dialog createConfirmResetPuzzleDialog() {
		return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(
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

	private final class ComputeSolutionAndCheckPuzzleTask extends AsyncTask<Void, Integer, Boolean> {
		private boolean timerRunning;
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			timerRunning = timer.isRunning();
			timer.stop();

			String message = getResources().getString(R.string.message_computing_solution);
			progressDialog = ProgressDialog.show(AndokuActivity.this, "", message, true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return puzzle.computeSolution();
		}

		@Override
		protected void onPostExecute(Boolean solved) {
			progressDialog.dismiss();

			if (timerRunning)
				timer.start();

			if (solved)
				checkPuzzle(true);
			else
				showWarning(R.string.warn_invalid_puzzle);
		}
	}
}
