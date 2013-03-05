package com.pavageau.sudoku;

import java.util.Map;
import java.util.Set;

import com.pavageau.sudoku.SudokuBoard.SolvedException;
import com.pavageau.sudoku.SudokuCell.UnsolvableException;

/**
 * @author pavageau
 * 
 *         Sudoku Solver app, packageable as a runnable JAR
 */
public class Solver {

	/**
	 * @param args
	 *            expects one argument, an 81 digit long string containing only
	 *            numbers 0 through 9. 0 represents an empty cell
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out
					.println("usage: java -jar SudokuSolver.jar <boardAsString>\n\nboardAsString: String representation of a Sudoku board, must be 81 characters long containing only numbers 0 through 9 (0 represents an empty cell). First 9 characters are the first line, next 9 characters are the second line, and so on.");
			return;
		}

		SudokuBoard board = new SudokuBoard(args[0]);
		// all the replace is for the printed output to be pretty.
		System.out.println(board.toString().replace("solved", "provided")
				.replace("123456789", "         ").replace("        ", ""));
		long start = System.currentTimeMillis();
		try {
			recursiveSolve(board);
		} catch (SolvedException e) {
			long duration = System.currentTimeMillis() - start;
			System.out.println("This board took " + duration + "ms to solve.");
			System.exit(0);
		} catch (UnsolvableException e) {
			long duration = System.currentTimeMillis() - start;
			System.out.println("This board has no solution. (" + duration
					+ "ms)");
			System.exit(0);
		}
		// this should never happen, in theory
		System.out.println("this board is too tough for me...");
	}

	/**
	 * Recusively solves a Sudoku board
	 * 
	 * @param board
	 *            the board to solve
	 * @throws UnsolvableException
	 *             when the board has no solution
	 * @throws SolvedException
	 *             when the board is solved
	 */
	private static void recursiveSolve(SudokuBoard board)
			throws UnsolvableException, SolvedException {
		board.singleValueCleanup();
		Map<Integer, Set<SudokuCell>> unfixedCellMap = board
				.getCellsPerNumberOfPossibleValues();
		for (int i = 2; i < 10; i++) {
			Set<SudokuCell> cells = unfixedCellMap.get(i);
			if (cells != null) {
				for (SudokuCell cell : cells) {
					Integer[] possiblevaluesClone = cell.getPossibleValues()
							.toArray(new Integer[0]);
					for (int possibleValue : possiblevaluesClone) {
						SudokuCell newCell = new SudokuCell(cell.x, cell.y,
								possibleValue);
						SudokuBoard newBoard = new SudokuBoard(board, newCell);
						try {
							recursiveSolve(newBoard);
						} catch (UnsolvableException e) {
							cell.removePossibleValue(possibleValue);
						}
					}
				}
			}
		}
	}
}
