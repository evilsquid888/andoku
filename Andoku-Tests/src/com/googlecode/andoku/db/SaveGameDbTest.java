package com.googlecode.andoku.db;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.googlecode.andoku.TickListener;
import com.googlecode.andoku.TickTimer;
import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Solution;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.transfer.PuzzleDecoder;
import com.googlecode.andoku.util.MockPuzzleSource;

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

		String puzzleId = MockPuzzleSource.SOURCE_ID + ':' + number;
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

		String puzzleId = MockPuzzleSource.SOURCE_ID + ':' + number;
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
		solve(puzzle, PuzzleDecoder.decodeValues(MockPuzzleSource.SOLUTIONS[number]));
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

	private static final class MockTickListener implements TickListener {
		public void onTick(long time) {
		}
	}
}
