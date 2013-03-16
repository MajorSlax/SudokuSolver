package com.pavageau.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.pavageau.sudoku.SudokuCell.UnsolvableException;

/**
 * @author pavageau
 * 
 *         Represents a Sudoku board
 */
public class SudokuBoard {

	private final SudokuCell[][] board = new SudokuCell[9][9];

	public class SolvedException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Creates a BoggleBoard from a 81 digit String
	 * 
	 * @param input
	 *            81 digit String to use as a source. First 9 digits represent
	 *            the first line of the grid, Next 9 characters represent the
	 *            second line, and so on. Only characters 0-9 are allowed. 0
	 *            represents an empty cell.
	 */
	public SudokuBoard(String input) {
		if (!input.matches("[0-9]{81}")) {
			throw new IllegalArgumentException(
					"Input String can only contain numbers and must contain 81 characters.");
		}

		int index = 0;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				this.board[i][j] = new SudokuCell(i, j,
						input.charAt(index++) - '0');
			}
		}
	}

	/**
	 * Clone constructor. Creates a new board from an existing board, with one
	 * cell differing
	 * 
	 * @param board
	 *            the existing board to clone
	 * @param cell
	 *            the differing cell, which will replace the cell at the same
	 *            x,y coordinates in the board
	 */
	public SudokuBoard(SudokuBoard board, SudokuCell cell) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				this.board[i][j] = new SudokuCell(board.get(i, j));
			}
		}
		this.board[cell.x][cell.y] = cell;
	}

	/**
	 * @return the SudokuCell at position (x,y)
	 */
	private SudokuCell get(int x, int y) {
		return board[x][y];
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 9; i++) {
			sb.append(Arrays.toString(board[i]) + "\n");
		}
		int solvedCount = getSolvedCellCount();
		return solvedCount == 81 ? String
				.format("%s\nCompletely Solved!\n", sb).replaceAll("        ",
						"") : String.format("%s\n%d cells solved.\n", sb,
				solvedCount);
	}

	/**
	 * Get all the relatives (20 of them) for a given cell, excluding the cell
	 * itself. That's all the cells in the same row, all the cells in the same
	 * column, and all the cells in the same 3*3 square
	 * 
	 * @param cell
	 * @return the collection described above, as a Set
	 */
	private Set<SudokuCell> getAllRelatives(SudokuCell cell) {
		HashSet<SudokuCell> result = new HashSet<SudokuCell>();
		SudokuCell[] squareCells = getSquare(cell);
		SudokuCell[] rowCells = getRow(cell.x);
		SudokuCell[] colCells = getCol(cell.y);
		for (int i = 0; i < 9; i++) {
			result.add(squareCells[i]);
			result.add(rowCells[i]);
			result.add(colCells[i]);
		}
		result.remove(cell);
		return result;
	}

	/**
	 * Get all the units (rows, lines, and 3*3 squares) in teh board
	 * 
	 * @return all the units in the board.
	 */
	private SudokuCell[][] getAllUnits() {
		SudokuCell[][] allUnits = new SudokuCell[27][9];
		for (int i = 0; i < 9; i++) {
			allUnits[i] = getRow(i);
			allUnits[9 + i] = getCol(i);
			allUnits[18 + i] = getSquare(i);
		}
		return allUnits;
	}

	/**
	 * Get all the cells from a cell's 3*3 square
	 * 
	 * @param cell
	 * @return
	 */
	private SudokuCell[] getSquare(SudokuCell cell) {
		SudokuCell[] result = new SudokuCell[9];
		int[] rowIndexes = getSquareIndexes(cell.x);
		int[] colIndexes = getSquareIndexes(cell.y);
		int index = 0;
		for (int i : rowIndexes) {
			for (int j : colIndexes) {
				result[index++] = board[i][j];
			}
		}
		return result;
	}

	/**
	 * Same as above, convenience method to be able to iterate through the 3*3
	 * squares using an index
	 * 
	 * @param index
	 * @return
	 */
	private SudokuCell[] getSquare(int index) {
		return getSquare(board[3 * (index % 3)][3 * (index / 3)]);
	}

	/**
	 * Get a row of the board
	 * 
	 * @param index
	 * @return
	 */
	private SudokuCell[] getRow(int index) {
		return board[index];
	}

	/**
	 * Get a column of the board
	 * 
	 * @param index
	 * @return
	 */
	private SudokuCell[] getCol(int index) {
		SudokuCell[] result = new SudokuCell[9];
		for (int i = 0; i < 9; i++) {
			result[i] = board[i][index];
		}
		return result;
	}

	/**
	 * Convenience method, based on a cell's index (x or y), return the 3
	 * indexes corresponding to the 3*3 square that the index belongs to
	 * 
	 * @param index
	 * @return
	 */
	private int[] getSquareIndexes(int index) {
		int root = 3 * (index / 3);
		int[] result = { root, root + 1, root + 2 };
		return result;
	}

	/**
	 * Iterates over the board and for each cell, removes possible values
	 * according to the cell's 20 neighbors' values
	 * 
	 * @throws SolvedException
	 *             when the board is solved
	 * @throws UnsolvableException
	 *             when a cell throws an UnsolvableException
	 */
	private void simpleCleanUp() throws SolvedException, UnsolvableException {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					SudokuCell cell = board[i][j];
					Set<SudokuCell> neighbors = getAllRelatives(cell);
					if (cell.isFixed()) {
						// remove cell value from all neighbors
						for (SudokuCell neighbor : neighbors) {
							if (neighbor.removePossibleValue(cell.getValue())) {
								changed = true;
							}
						}
					} else {
						// remove all fixed neighbor values from cell possible
						// values
						for (SudokuCell neighbor : neighbors) {
							if (neighbor.isFixed()
									&& cell.removePossibleValue(neighbor
											.getValue())) {
								changed = true;
							}
						}
					}
				}
			}
			if (isSolved()) {
				System.out.println(this);
				throw new SolvedException();
			}
		}
	}

	/**
	 * Runs simpleCleanup, then for each unit (row, column, or 3*3 square),
	 * looks for unique values in unfixed cells and assigns them if any, then
	 * runs simpleCleanup. Loops until nothing changes using this algorithm
	 * 
	 * @throws SolvedException
	 *             when the board is solved (thrown by simpleCleanup)
	 * @throws UnsolvableException
	 *             when a cell throws an UnsolvableException
	 */
	public void singleValueCleanup() throws SolvedException,
			UnsolvableException {
		simpleCleanUp();
		for (SudokuCell[] unit : getAllUnits()) {
			if (handleUniqueValuesInUnit(unit)) {
				simpleCleanUp();
			}
		}
	}

	/**
	 * For a given unit (row, column, or 3*3 square), looks for unique values in
	 * unfixed cells and assigns them if any
	 * 
	 * @param unit
	 *            the row, column, or 3*3 square
	 * @return true if any change was made, false otherwise
	 * @throws UnsolvableException
	 *             if a cell throws an UnsolvableException
	 */
	private boolean handleUniqueValuesInUnit(SudokuCell[] unit)
			throws UnsolvableException {
		Map<Integer, List<SudokuCell>> valueCount = new HashMap<Integer, List<SudokuCell>>();
		for (SudokuCell cell : unit) {
			// handle only unfixed cells
			if (!cell.isFixed()) {
				for (int i : cell.getPossibleValues()) {
					if (!valueCount.containsKey(i)) {
						valueCount.put(i, new ArrayList<SudokuCell>());
					}
					valueCount.get(i).add(cell);
				}
			}
		}
		boolean changed = false;
		for (Entry<Integer, List<SudokuCell>> entry : valueCount.entrySet()) {
			List<SudokuCell> cells = entry.getValue();
			if (cells.size() == 1) {
				// set cell value
				cells.get(0).setValue(entry.getKey());
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * @return the number of fixed cells for the board
	 */
	private int getSolvedCellCount() {
		int solvedCounter = 0;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (board[i][j].isFixed()) {
					solvedCounter++;
				}
			}
		}
		return solvedCounter;
	}

	/**
	 * @return true if the board is solved, false otherwise
	 */
	private boolean isSolved() {
		return getSolvedCellCount() == 81;
	}

	/**
	 * @return A map in which unsolved cells are grouped by their number of
	 *         possible values
	 */
	public Map<Integer, List<SudokuCell>> getCellsPerNumberOfPossibleValues() {
		Map<Integer, List<SudokuCell>> result = new HashMap<Integer, List<SudokuCell>>();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				SudokuCell cell = board[i][j];
				if (!cell.isFixed()) {
					int size = cell.getPossibleValues().size();
					if (!result.containsKey(size)) {
						result.put(size, new ArrayList<SudokuCell>());
					}
					result.get(size).add(cell);
				}
			}
		}
		return result;
	}
}
