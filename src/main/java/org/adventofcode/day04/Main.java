package org.adventofcode.day04;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

//--- Day 4: Ceres Search ---
@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// convert it into a char 2 dimension array
		final char[][] charTable = new char[inputByLines.size()][];
		for (int i = 0; i < inputByLines.size(); i++) {
			charTable[i] = inputByLines.get(i).toCharArray();
		}

		/*
		Iterate over every character of the 2 dimension array.
		If the character is an 'X', then check the 8 possible direction for the word 'XMAS'.
		 */
		long xmasWordCounter = 0;
		for (int i = 0; i < charTable.length; i++) {
			for (int j = 0; j < charTable[i].length; j++) {
				final char currentChar = charTable[i][j];
				if (currentChar == 'X') {
					if (checkXmasWordToStraightRight(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToStraightLeft(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToStraightDown(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToStraightUp(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToDiagonalRightDown(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToDiagonalLeftUp(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToDiagonalLeftDown(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
					if (checkXmasWordToDiagonalRightUp(charTable, i, j, charTable.length - 1, charTable[i].length - 1)) {
						xmasWordCounter++;
					}
				}
			}
		}
		System.out.println("xmasWordCounter = " + xmasWordCounter);

		// part 2: similar to part 1, but now we stop at character 'A' and check for 2 'MAS' words in a shape of an "X".
		long xShaped2MasWordsCounter = 0;
		for (int i = 0; i < charTable.length; i++) {
			for (int j = 0; j < charTable[i].length; j++) {
				final char currentChar = charTable[i][j];
				if (currentChar == 'A'
					&& checkMasWordTopLeftBottomRight(charTable, i, j, charTable.length - 1, charTable[i].length - 1)
					&& checkMasWordTopRightBottomLeft(charTable, i, j, charTable.length - 1, charTable[i].length - 1)
				) {
					xShaped2MasWordsCounter++;
				}
			}
		}
		System.out.println("xShaped2MasWordsCounter = " + xShaped2MasWordsCounter);
	}

	/**
	 * -->
	 */
	private static boolean checkXmasWordToStraightRight(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar + 3 <= maxXpos) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar + 1][currentYposOfXchar] == 'M'
				&& charTable[currentXposOfXchar + 2][currentYposOfXchar] == 'A'
				&& charTable[currentXposOfXchar + 3][currentYposOfXchar] == 'S';
		}
		return false;
	}

	/**
	 * <--
	 */
	private static boolean checkXmasWordToStraightLeft(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar >= 3) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar - 1][currentYposOfXchar] == 'M'
				&& charTable[currentXposOfXchar - 2][currentYposOfXchar] == 'A'
				&& charTable[currentXposOfXchar - 3][currentYposOfXchar] == 'S';
		}
		return false;
	}

	/**
	 * |
	 * v
	 */
	private static boolean checkXmasWordToStraightDown(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentYposOfXchar + 3 <= maxYpos) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar][currentYposOfXchar + 1] == 'M'
				&& charTable[currentXposOfXchar][currentYposOfXchar + 2] == 'A'
				&& charTable[currentXposOfXchar][currentYposOfXchar + 3] == 'S';
		}
		return false;
	}

	/**
	 * ^
	 * |
	 */
	private static boolean checkXmasWordToStraightUp(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentYposOfXchar >= 3) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar][currentYposOfXchar - 1] == 'M'
				&& charTable[currentXposOfXchar][currentYposOfXchar - 2] == 'A'
				&& charTable[currentXposOfXchar][currentYposOfXchar - 3] == 'S';
		}
		return false;
	}

	/**
	 * \
	 *  _|
	 */
	private static boolean checkXmasWordToDiagonalRightDown(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar + 3 <= maxXpos && currentYposOfXchar + 3 <= maxYpos) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar + 1][currentYposOfXchar + 1] == 'M'
				&& charTable[currentXposOfXchar + 2][currentYposOfXchar + 2] == 'A'
				&& charTable[currentXposOfXchar + 3][currentYposOfXchar + 3] == 'S';
		}
		return false;
	}

	/**
	 *  _
	 * |
	 *   \
	 */
	private static boolean checkXmasWordToDiagonalLeftUp(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar >= 3 && currentYposOfXchar >= 3) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar - 1][currentYposOfXchar - 1] == 'M'
				&& charTable[currentXposOfXchar - 2][currentYposOfXchar - 2] == 'A'
				&& charTable[currentXposOfXchar - 3][currentYposOfXchar - 3] == 'S';
		}
		return false;
	}

	/**
	 *   /
	 * |_
	 */
	private static boolean checkXmasWordToDiagonalLeftDown(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar >= 3 && currentYposOfXchar + 3 <= maxYpos) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar - 1][currentYposOfXchar + 1] == 'M'
				&& charTable[currentXposOfXchar - 2][currentYposOfXchar + 2] == 'A'
				&& charTable[currentXposOfXchar - 3][currentYposOfXchar + 3] == 'S';
		}
		return false;
	}

	/**
	 *  _
	 *   |
	 * /
	 */
	private static boolean checkXmasWordToDiagonalRightUp(
		final char[][] charTable,
		final int currentXposOfXchar,
		final int currentYposOfXchar,
		final int maxXpos,
		final int maxYpos
	) {
		if (currentXposOfXchar + 3 <= maxXpos && currentYposOfXchar >= 3) { // prevent ArrayIndexOutOfBoundsException
			return charTable[currentXposOfXchar + 1][currentYposOfXchar - 1] == 'M'
				&& charTable[currentXposOfXchar + 2][currentYposOfXchar - 2] == 'A'
				&& charTable[currentXposOfXchar + 3][currentYposOfXchar - 3] == 'S';
		}
		return false;
	}

	/**
	 *  _
	 * |
	 *   \
	 *    _|
	 */
	private static boolean checkMasWordTopLeftBottomRight(
		final char[][] charTable,
		final int currentXposOfAchar,
		final int currentYposOfAchar,
		final int maxXpos,
		final int maxYpos
	) {
		if ((currentXposOfAchar >= 1 && currentYposOfAchar >= 1)
			&& (currentXposOfAchar + 1 <= maxXpos && currentYposOfAchar + 1 <= maxYpos)
		) { // prevent ArrayIndexOutOfBoundsException
			final char topLeftCharFromAChar = charTable[currentXposOfAchar - 1][currentYposOfAchar - 1];
			final char bottomRightCharFromAChar = charTable[currentXposOfAchar + 1][currentYposOfAchar + 1];
			return (topLeftCharFromAChar == 'M' && bottomRightCharFromAChar == 'S')
				|| (topLeftCharFromAChar == 'S' && bottomRightCharFromAChar == 'M');
		}
		return false;
	}

	/**
	 *    _
	 *     |
	 *   /
	 * |_
	 */
	private static boolean checkMasWordTopRightBottomLeft(
		final char[][] charTable,
		final int currentXposOfAchar,
		final int currentYposOfAchar,
		final int maxXpos,
		final int maxYpos
	) {
		if ((currentXposOfAchar + 1 <= maxXpos && currentYposOfAchar >= 1)
			&& (currentXposOfAchar >= 1 && currentYposOfAchar + 1 <= maxYpos)
		) { // prevent ArrayIndexOutOfBoundsException
			final char topRightCharFromAChar = charTable[currentXposOfAchar + 1][currentYposOfAchar - 1];
			final char bottomLeftCharFromAChar = charTable[currentXposOfAchar - 1][currentYposOfAchar + 1];
			return (topRightCharFromAChar == 'M' && bottomLeftCharFromAChar == 'S')
				|| (topRightCharFromAChar == 'S' && bottomLeftCharFromAChar == 'M');
		}
		return false;
	}
}
