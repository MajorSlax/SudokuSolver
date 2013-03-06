package com.pavageau.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author pavageau
 * 
 *         Represents a cell in a Boggle board
 */
public class SudokuCell {

	private static final List<Integer> ALL_VALUES = Arrays.asList(1, 2, 3, 4,
			5, 6, 7, 8, 9);

	public final int x;
	public final int y;
	private final List<Integer> possibleValues = new ArrayList<Integer>();
	private int value;

	/**
	 * Default Constructor
	 * 
	 * @param x
	 *            cell's row index in the board
	 * @param y
	 *            cell's column index in the board
	 * @param value
	 *            cell's value (0 for empty)
	 */
	public SudokuCell(int x, int y, int value) {
		this.x = x;
		this.y = y;
		this.value = value;
		if (this.value != 0) {
			this.possibleValues.add(this.value);
		} else {
			this.possibleValues.addAll(ALL_VALUES);
		}
	}

	/**
	 * Clone Constructor
	 * 
	 * @param cell
	 *            cell to clone
	 */
	public SudokuCell(SudokuCell cell) {
		this.x = cell.x;
		this.y = cell.y;
		this.value = cell.value;
		this.possibleValues.addAll(cell.possibleValues);
	}

	public class UnsolvableException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int possibilities = possibleValues.size();
		for (int i = 0; i < 9 - possibilities; i++) {
			sb.append(' ');
		}
		for (Integer i : possibleValues) {
			sb.append(i);
		}
		return sb.toString();
	}

	/**
	 * Remove a value for this cell's list of possible values
	 * 
	 * @param valueToRemove
	 *            the value to remove
	 * @return true if possibleValues was changed, false otherwise
	 * @throws UnsolvableException
	 *             if there are no more possible values after removal
	 */
	public boolean removePossibleValue(Integer valueToRemove)
			throws UnsolvableException {
		boolean remove = possibleValues.remove(valueToRemove);
		if (remove) {
			switch (possibleValues.size()) {
			case 0:
				throw new UnsolvableException();
			case 1:
				setValue(possibleValues.iterator().next());
				break;
			default:
				break;
			}
		}
		return remove;
	}

	public List<Integer> getPossibleValues() {
		return possibleValues;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Sets this cell's value
	 * 
	 * @param value
	 *            the value to set
	 * @throws UnsolvableException
	 *             if possibleValues does not contain value
	 */
	public void setValue(int value) throws UnsolvableException {
		if (!possibleValues.contains(value)) {
			throw new UnsolvableException();
		}
		this.value = value;
		// remove any value other than value from possibleValues
		possibleValues.retainAll(Collections.singleton(value));
	}

	/**
	 * @return true if this cell is fixed, i.e. if value != 0
	 */
	public boolean isFixed() {
		return value != 0;
	}
}
