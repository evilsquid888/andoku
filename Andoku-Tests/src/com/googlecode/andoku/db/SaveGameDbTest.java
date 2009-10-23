package com.googlecode.andoku.db;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.googlecode.andoku.TickListener;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Solution;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.Difficulty;
import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.transfer.PuzzleDecoder;

public class SaveGameDbTest extends AndroidTestCase {
	private SaveGameDb db;

	@Override
	protected void setUp() throws Exception {
		db = new SaveGameDb(getContext());
		db.resetAll();
	}

	@Override
	protected void tearDown() throws Exception {
		db.close();
	}

	public void testSaveAndLoadGame() throws Exception {
		int number = 0;
		AndokuPuzzle puzzleSave = createPuzzle(number);
		puzzleSave.setValues(0, 1, ValueSet.single(7));
		puzzleSave.setValues(0, 2, ValueSet.single(3));

		TickTimer timerSave = new TickTimer(new MockTickListener());
		timerSave.setTime(700);

		String puzzleId = MOCK_SOURCE + ':' + number;
		db.saveGame(puzzleId, puzzleSave, timerSave);

		AndokuPuzzle puzzleLoad = createPuzzle(number);
		TickTimer timerLoad = new TickTimer(new MockTickListener());

		assertTrue(db.loadGame(puzzleId, puzzleLoad, timerLoad));
		assertEquals(ValueSet.single(7), puzzleLoad.getValues(0, 1));
		assertEquals(ValueSet.single(3), puzzleLoad.getValues(0, 2));
		assertEquals(700, timerLoad.getTime());
	}

	public void testDeleteGame() throws Exception {
		int number = 0;
		AndokuPuzzle puzzle = createPuzzle(number);
		TickTimer timer = new TickTimer(new MockTickListener());

		String puzzleId = MOCK_SOURCE + ':' + number;
		db.saveGame(puzzleId, puzzle, timer);
		assertTrue(db.loadGame(puzzleId, puzzle, timer));

		db.delete(puzzleId);
		assertFalse(db.loadGame(puzzleId, puzzle, timer));
	}

	public void testFindAllGames() throws Exception {
		AndokuPuzzle puzzle1 = createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		long t1 = System.currentTimeMillis();
		db.saveGame("mock:17:1", puzzle1, timer1);
		long t2 = System.currentTimeMillis();
		db.saveGame("mock:16:2", puzzle2, timer2);
		long t3 = System.currentTimeMillis();
		db.saveGame("mock:17:3", puzzle3, timer3);
		long t4 = System.currentTimeMillis();

		Cursor cursor = db.findAllGames();
		// cursor: ID, PUZZLE_ID, TYPE, TIMER, CREATED_DATE, MODIFIED_DATE

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17:1", cursor.getString(1));
		assertEquals(puzzle1.getPuzzleType().ordinal(), cursor.getInt(2));
		assertEquals(timer1.getTime(), cursor.getLong(3));
		assertTrue(cursor.getLong(4) >= t1);
		assertTrue(cursor.getLong(4) < t2);
		assertEquals(cursor.getLong(5), cursor.getLong(4));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:16:2", cursor.getString(1));
		assertEquals(puzzle2.getPuzzleType().ordinal(), cursor.getInt(2));
		assertEquals(timer2.getTime(), cursor.getLong(3));
		assertTrue(cursor.getLong(4) >= t2);
		assertTrue(cursor.getLong(4) < t3);
		assertEquals(cursor.getLong(5), cursor.getLong(4));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17:3", cursor.getString(1));
		assertEquals(puzzle3.getPuzzleType().ordinal(), cursor.getInt(2));
		assertEquals(timer3.getTime(), cursor.getLong(3));
		assertTrue(cursor.getLong(4) >= t3);
		assertTrue(cursor.getLong(4) < t4);
		assertEquals(cursor.getLong(5), cursor.getLong(4));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testFindUnfinishedGames() throws Exception {
		AndokuPuzzle puzzle1 = createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		long t1 = System.currentTimeMillis();
		db.saveGame("mock:17:1", puzzle1, timer1);
		long t2 = System.currentTimeMillis();
		db.saveGame("mock:16:2", puzzle2, timer2);
		long t3 = System.currentTimeMillis();
		db.saveGame("mock:17:3", puzzle3, timer3);
		long t4 = System.currentTimeMillis();

		Cursor cursor = db.findUnfinishedGames();
		// cursor: ID, PUZZLE_ID, TYPE, TIMER, CREATED_DATE, MODIFIED_DATE

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17:3", cursor.getString(1));
		assertEquals(puzzle3.getPuzzleType().ordinal(), cursor.getInt(2));
		assertEquals(timer3.getTime(), cursor.getLong(3));
		assertTrue(cursor.getLong(4) >= t3);
		assertTrue(cursor.getLong(4) < t4);
		assertEquals(cursor.getLong(5), cursor.getLong(4));

		assertTrue(cursor.moveToNext());
		assertEquals("mock:17:1", cursor.getString(1));
		assertEquals(puzzle1.getPuzzleType().ordinal(), cursor.getInt(2));
		assertEquals(timer1.getTime(), cursor.getLong(3));
		assertTrue(cursor.getLong(4) >= t1);
		assertTrue(cursor.getLong(4) < t2);
		assertEquals(cursor.getLong(5), cursor.getLong(4));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testFindGamesBySource() throws Exception {
		AndokuPuzzle puzzle1 = createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = createSolvedPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		db.saveGame("mock:17:1", puzzle1, timer1);
		db.saveGame("mock:16:2", puzzle2, timer2);
		db.saveGame("mock:17:3", puzzle3, timer3);

		Cursor cursor = db.findGamesBySource("mock:16");
		// cursor: NUMBER, SOLVED

		assertTrue(cursor.moveToNext());
		assertEquals(2, cursor.getInt(0));
		assertEquals(1, cursor.getInt(1));

		assertFalse(cursor.moveToNext());

		cursor.close();

		cursor = db.findGamesBySource("mock:17");

		assertTrue(cursor.moveToNext());
		assertEquals(1, cursor.getInt(0));
		assertEquals(0, cursor.getInt(1));

		assertTrue(cursor.moveToNext());
		assertEquals(3, cursor.getInt(0));
		assertEquals(1, cursor.getInt(1));

		assertFalse(cursor.moveToNext());

		cursor.close();
	}

	public void testGetStatistics() throws Exception {
		AndokuPuzzle puzzle1 = createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		timer1.setTime(700);
		AndokuPuzzle puzzle2 = createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		timer2.setTime(800);
		AndokuPuzzle puzzle3 = createSolvedPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());
		timer3.setTime(900);

		db.saveGame("mock:17:1", puzzle1, timer1);
		db.saveGame("mock:17:2", puzzle2, timer2);
		db.saveGame("mock:17:3", puzzle3, timer3);

		GameStatistics statistics = db.getStatistics("mock:17");
		assertEquals(2, statistics.numGamesSolved);
		assertEquals(800, statistics.minTime);
		assertEquals(800 + 900, statistics.sumTime);
		assertEquals((800 + 900) / 2, statistics.getAverageTime());
	}

	public void testGetPuzzleIdByRowId() throws Exception {
		AndokuPuzzle puzzle1 = createPuzzle(1);
		TickTimer timer1 = new TickTimer(new MockTickListener());
		AndokuPuzzle puzzle2 = createSolvedPuzzle(4);
		TickTimer timer2 = new TickTimer(new MockTickListener());
		AndokuPuzzle puzzle3 = createPuzzle(7);
		TickTimer timer3 = new TickTimer(new MockTickListener());

		db.saveGame("mock:17:1", puzzle1, timer1);
		db.saveGame("mock:16:2", puzzle2, timer2);
		db.saveGame("mock:17:3", puzzle3, timer3);

		Cursor cursor = db.findAllGames();
		assertTrue(cursor.moveToNext());
		long id1 = cursor.getLong(0);
		assertTrue(cursor.moveToNext());
		long id2 = cursor.getLong(0);
		assertTrue(cursor.moveToNext());
		long id3 = cursor.getLong(0);
		assertFalse(cursor.moveToNext());
		cursor.close();

		assertEquals("mock:17:1", db.puzzleIdByRowId(id1));
		assertEquals("mock:16:2", db.puzzleIdByRowId(id2));
		assertEquals("mock:17:3", db.puzzleIdByRowId(id3));
	}

	private AndokuPuzzle createPuzzle(int number) throws PuzzleIOException {
		PuzzleSource source = new MockPuzzleSource();
		PuzzleHolder puzzleHolder = source.load(number);
		return AndokuPuzzle.create(puzzleHolder);
	}

	private AndokuPuzzle createSolvedPuzzle(int number) throws PuzzleIOException {
		AndokuPuzzle puzzle = createPuzzle(number);
		solve(puzzle, PuzzleDecoder.decodeValues(SOLUTIONS[number]));
		return puzzle;
	}

	private void solve(AndokuPuzzle puzzle, Solution solution) {
		int size = puzzle.getSize();
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (!puzzle.isClue(row, col))
					puzzle.setValues(row, col, ValueSet.single(solution.getValue(row, col)));
			}
		}
	}

	private static final class MockPuzzleSource implements PuzzleSource {
		public String getSourceId() {
			return MOCK_SOURCE;
		}

		public PuzzleHolder load(int number) throws PuzzleIOException {
			Difficulty difficulty = DIFFICULTIES[number];
			Puzzle puzzle = PuzzleDecoder.decode(PUZZLES[number]);
			Solution solution = PuzzleDecoder.decodeValues(SOLUTIONS[number]);
			return new PuzzleHolder(this, number, difficulty, puzzle, solution);
		}
		public int numberOfPuzzles() {
			return PUZZLES.length;
		}
	}

	private static final class MockTickListener implements TickListener {
		public void onTick(long time) {
		}
	}

	private static final String MOCK_SOURCE = "mock:1";

	private static final String P1 = "9..1........9.23.612.....89.............6.............71.....252.96.7........5..7|112222334112222334111552334116553334666654444677755488677955888677999988677999988|H";
	private static final String S1 = "987153264574982316123576489461829753395468172642731598718394625259647831836215947";
	private static final Difficulty D1 = Difficulty.HARD;
	private static final String P2 = ".......6.643......8.....1...9185......4...9......4129...2.....9......758.8.......|111122223111123333144422233445425336445555566477586566778886669777789999788889999|H";
	private static final String S2 = "925178364643729581856294137491857623134685972768341295372516849219463758587932416";
	private static final Difficulty D2 = Difficulty.HARD;
	private static final String P3 = ".....2........9..4..59..6........82.....3.....81........9..13..6..3........7.....|112233333111233334111222444512226444566666664555677784555777888599997888999997788|H";
	private static final String S3 = "193572486726859134835924671347165829418236795581497263279641358652318947964783512";
	private static final Difficulty D3 = Difficulty.HARD;
	private static final String P4 = "..4.2...9..875.342...4.8...426.....8.........5.....967...3.9...952.764..8...4.7..";
	private static final String S4 = "714623859698751342235498176426917538379865214581234967147389625952176483863542791";
	private static final Difficulty D4 = Difficulty.MEDIUM;
	private static final String P5 = "..1...8....8726......5...76....9...51.2...9.33...7....25...8......6125....6...4..";
	private static final String S5 = "761439852538726194429581376684293715172865943395174268257948631943612587816357429";
	private static final Difficulty D5 = Difficulty.MEDIUM;
	private static final String P6 = "87.4.6.3.1.........5.9..4....87....5.95...74.2....89....1..3.7.........6.8.6.7.52";
	private static final String S6 = "879426531142375689356981427438769215695132748217548963561293874723854196984617352";
	private static final Difficulty D6 = Difficulty.MEDIUM;
	private static final String P7 = "..9....3.4....2...2..7..8......87..6..5...4..6..15......3..1..4...9....8.2....6..||X";
	private static final String S7 = "759648132481532769236719845312487956875296413694153287963821574147965328528374691";
	private static final Difficulty D7 = Difficulty.FIENDISH;
	private static final String P8 = "......9..6293...........1.....1...989...5...138...4.....6...........8567..2......||X";
	private static final String S8 = "714682953629315784538749126265137498947856231381294675856971342193428567472563819";
	private static final Difficulty D8 = Difficulty.FIENDISH;
	private static final String P9 = "...92.1.....6...3.9....1..23........58..1..24........37..8....1.6...7.....8.49...||X";
	private static final String S9 = "856923147172654839943781562394278615587316924621495783739862451465137298218549376";
	private static final Difficulty D9 = Difficulty.FIENDISH;

	private static final String[] PUZZLES = { P1, P2, P3, P4, P5, P6, P7, P8, P9 };
	private static final Difficulty[] DIFFICULTIES = { D1, D2, D3, D4, D5, D6, D7, D8, D9 };
	private static final String[] SOLUTIONS = { S1, S2, S3, S4, S5, S6, S7, S8, S9 };
}
