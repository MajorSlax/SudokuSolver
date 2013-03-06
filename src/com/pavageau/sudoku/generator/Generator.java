package com.pavageau.sudoku.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.pavageau.sudoku.Solver;
import com.pavageau.sudoku.SudokuBoard;
import com.pavageau.sudoku.SudokuBoard.SolvedException;
import com.pavageau.sudoku.SudokuCell.UnsolvableException;

public class Generator {

	public static void main(String[] args) {
		Map<String, Long> solved = new HashMap<String, Long>();
		Map<String, Long> unsolved = new HashMap<String, Long>();
		int testSize = 10000;
		for (int i = 0; i < testSize; i++) {
			String boardAsString = generateRandomBoard();
			if (!unsolved.keySet().contains(boardAsString)
					&& !solved.keySet().contains(boardAsString)) {
				SudokuBoard board = new SudokuBoard(boardAsString);
				long start = System.currentTimeMillis();
				try {
					Solver.recursiveSolve(board);
				} catch (UnsolvableException e) {
					long duration = System.currentTimeMillis() - start;
					unsolved.put(boardAsString, duration);
				} catch (SolvedException e) {
					long duration = System.currentTimeMillis() - start;
					solved.put(boardAsString, duration);
				}
				System.out.println("Processed " + (i + 1) + " boards");
			}
		}
		System.out.println("\n");
		System.out.println(String.format("SOLVED: %d%%\n", solved.size() * 100
				/ testSize));
		printResult(solved);
		System.out.println(String.format("UNSOLVED: %d%%\n", unsolved.size()
				* 100 / testSize));
		printResult(unsolved);
	}

	private static void printResult(Map<String, Long> result) {
		if (result.size() > 0) {
			double averageSolveTime = 0;
			long maxSolveTime = 0;
			String hardestBoard = "";
			for (Entry<String, Long> entry : result.entrySet()) {
				long time = entry.getValue();
				averageSolveTime += time;
				if (time > maxSolveTime) {
					maxSolveTime = time;
					hardestBoard = entry.getKey();
				}
			}
			System.out.println(String.format(
					"Average time to complete: %fms.\n", averageSolveTime
							/ result.size()));
			String boardAsFormattedString = new SudokuBoard(hardestBoard)
					.toString().replace("123456789", "         ")
					.replace("        ", "");
			System.out.println(String.format(
					"Toughest board: %s\n%s\nTook %dms.\n", hardestBoard,
					boardAsFormattedString, maxSolveTime));
		} else {
			System.out.println("No board in this set.");
		}
	}

	private static String generateRandomBoard() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 81; i++) {
			sb.append('0');
		}
		String allZeros = sb.toString();
		// 17 is the proven minimum number of clues for a Sudoku to have
		// potentially at most 1 solution (less will always have 2 or more).
		// Thus, filling 17 cells with numbers 1 through 9
		int differentValues = 0;
		StringBuilder boardBuilder = null;
		while (differentValues < 8) {
			boardBuilder = new StringBuilder(allZeros);
			Random rand = new Random();
			Set<Integer> presentValues = new HashSet<Integer>();
			int filledCells = 17;
			for (Integer i = 0; i < filledCells; i++) {
				int randIndex = rand.nextInt(81);
				while (boardBuilder.charAt(randIndex) != '0') {
					randIndex = rand.nextInt(81);
				}
				Integer randNumber = 1 + rand.nextInt(9);
				presentValues.add(randNumber);
				boardBuilder.replace(randIndex, randIndex + 1,
						randNumber.toString());
			}
			// check that we have at least 8 different digits in the grid (less
			// makes for 2 solutions or more)
			differentValues = presentValues.size();
		}
		return boardBuilder.toString();
	}
}
