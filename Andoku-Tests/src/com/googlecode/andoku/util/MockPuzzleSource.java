package com.googlecode.andoku.util;

import com.googlecode.andoku.model.AndokuPuzzle;
import com.googlecode.andoku.model.Puzzle;
import com.googlecode.andoku.model.Solution;
import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.source.Difficulty;
import com.googlecode.andoku.source.PuzzleHolder;
import com.googlecode.andoku.source.PuzzleIOException;
import com.googlecode.andoku.source.PuzzleSource;
import com.googlecode.andoku.transfer.PuzzleDecoder;

public class MockPuzzleSource implements PuzzleSource {
	public static final String SOURCE_ID = "mock:1";

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

	public static final String[] PUZZLES = { P1, P2, P3, P4, P5, P6, P7, P8, P9 };
	public static final Difficulty[] DIFFICULTIES = { D1, D2, D3, D4, D5, D6, D7, D8, D9 };
	public static final String[] SOLUTIONS = { S1, S2, S3, S4, S5, S6, S7, S8, S9 };

	public static AndokuPuzzle createPuzzle(int number) throws PuzzleIOException {
		PuzzleSource source = new MockPuzzleSource();
		PuzzleHolder puzzleHolder = source.load(number);
		return AndokuPuzzle.create(puzzleHolder);
	}

	public static AndokuPuzzle createSolvedPuzzle(int number) throws PuzzleIOException {
		AndokuPuzzle puzzle = createPuzzle(number);
		solve(puzzle, PuzzleDecoder.decodeValues(SOLUTIONS[number]));
		return puzzle;
	}

	private static void solve(AndokuPuzzle puzzle, Solution solution) {
		int size = puzzle.getSize();
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (!puzzle.isClue(row, col))
					puzzle.setValues(row, col, ValueSet.single(solution.getValue(row, col)));
			}
		}
	}

	public String getSourceId() {
		return SOURCE_ID;
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
