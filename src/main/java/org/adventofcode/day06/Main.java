package org.adventofcode.day06;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@UtilityClass
public class Main {

	private static final char CHAR_GUARD_FACING_NORTH = '^';
	private static final char CHAR_GUARD_FACING_EAST = '>';
	private static final char CHAR_GUARD_FACING_SOUTH = 'v';
	private static final char CHAR_GUARD_FACING_WEST = '<';
	private static final char CHAR_OBSTACLE = '#';
	private static final char CHAR_MARK = 'X';

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// convert it into a char 2 dimension array
		// save the guard's starting position
		int guardXpos = 0;
		int guardYpos = 0;
		final char[][] mapCharTable = new char[inputByLines.size()][];
		for (int i = 0; i < inputByLines.size(); i++) {
			final String line = inputByLines.get(i);
			mapCharTable[i] = line.toCharArray();
			if (line.contains(String.valueOf(CHAR_GUARD_FACING_NORTH))) {
				guardXpos = line.indexOf(CHAR_GUARD_FACING_NORTH);
				guardYpos = i;
			}
		}

		// to remember that I need to flip x and y for array calls
		System.out.printf("guard ('%s') starting (x,y) position: (%d,%d)%n", mapCharTable[guardYpos][guardXpos], guardXpos, guardYpos);
		printMap(mapCharTable);

		/*
		Move the guard and mark the visited locations with an 'X', until the guard leaves the border of the map.
		Move the guard straight until she reaches an obstacle ('#'), then turn her right 90 degrees.
		 */
		Direction guardDirection = Direction.NORTH;
		boolean isGuardWithinMapBorders = true;
		while (isGuardWithinMapBorders) {
			if (isGuardFacingTheBorderOfTheMap(mapCharTable, guardXpos, guardYpos, guardDirection)) {
				markGuardPosition(mapCharTable, guardXpos, guardYpos);
				isGuardWithinMapBorders = false;
			} else if (isGuardFacingObstacle(mapCharTable, guardXpos, guardYpos, guardDirection)) {
				guardDirection = turnGuard90Degrees(guardDirection);
				drawGuard(mapCharTable, guardXpos, guardYpos, guardDirection);
			} else {
				markGuardPosition(mapCharTable, guardXpos, guardYpos);
				final Pair<Integer, Integer> newGuardPosition = moveGuard1Step(guardXpos, guardYpos, guardDirection);
				guardXpos = newGuardPosition.getLeft();
				guardYpos = newGuardPosition.getRight();
				drawGuard(mapCharTable, guardXpos, guardYpos, guardDirection);
			}
		}
		System.out.println("final map which shows where the guard was with 'X' characters:");
		printMap(mapCharTable);
		long guardVisitedPositionsCounter = 0;
		for (final char[] rows : mapCharTable) {
			for (final char position : rows) {
				if (position == CHAR_MARK) {
					guardVisitedPositionsCounter++;
				}
			}
		}
		System.out.println("guardVisitedPositionsCounter = " + guardVisitedPositionsCounter);
	}

	private static void printMap(final char[][] mapCharTable) {
		for (final char[] rows : mapCharTable) {
			for (final char c : rows) {
				System.out.print(c);
			}
			System.out.println();
		}
	}

	private static boolean isGuardFacingTheBorderOfTheMap(
		final char[][] mapCharTable,
		final int guardXpos,
		final int guardYpos,
		final Direction guardDirection
	) {
		return switch (guardDirection) {
			case NORTH -> guardYpos == 0; // top row
			case EAST -> guardXpos == mapCharTable[guardYpos].length - 1; // rightmost position of the current row
			case SOUTH -> guardYpos == mapCharTable.length - 1; // bottom row
			case WEST -> guardXpos == 0; // leftmost position of the current row
		};
	}

	private static void markGuardPosition(final char[][] mapCharTable, final int guardXpos, final int guardYpos) {
		mapCharTable[guardYpos][guardXpos] = CHAR_MARK;
	}

	private static boolean isGuardFacingObstacle(
		final char[][] mapCharTable,
		final int guardXpos,
		final int guardYpos,
		final Direction guardDirection
	) {
		return switch (guardDirection) {
			case NORTH -> mapCharTable[guardYpos - 1][guardXpos] == CHAR_OBSTACLE;
			case EAST -> mapCharTable[guardYpos][guardXpos + 1] == CHAR_OBSTACLE;
			case SOUTH -> mapCharTable[guardYpos + 1][guardXpos] == CHAR_OBSTACLE;
			case WEST -> mapCharTable[guardYpos][guardXpos - 1] == CHAR_OBSTACLE;
		};
	}

	private static Direction turnGuard90Degrees(final Direction guardDirection) {
		return switch (guardDirection) {
			case NORTH -> Direction.EAST;
			case EAST -> Direction.SOUTH;
			case SOUTH -> Direction.WEST;
			case WEST -> Direction.NORTH;
		};
	}

	private static Pair<Integer, Integer> moveGuard1Step(
		final int guardXpos,
		final int guardYpos,
		final Direction guardDirection
	) {
		return switch (guardDirection) {
			case NORTH -> Pair.of(guardXpos, guardYpos - 1);
			case EAST -> Pair.of(guardXpos + 1, guardYpos);
			case SOUTH -> Pair.of(guardXpos, guardYpos + 1);
			case WEST -> Pair.of(guardXpos - 1, guardYpos);
		};
	}

	private static void drawGuard(
		final char[][] mapCharTable,
		final int guardXpos,
		final int guardYpos,
		final Direction guardDirection
	) {
		mapCharTable[guardYpos][guardXpos] = guardDirection.getCharGuard();
	}

	@Getter
	private enum Direction {
		NORTH(CHAR_GUARD_FACING_NORTH),
		EAST(CHAR_GUARD_FACING_EAST),
		SOUTH(CHAR_GUARD_FACING_SOUTH),
		WEST(CHAR_GUARD_FACING_WEST);

		private final char charGuard;

		Direction(final char charGuard) {
			this.charGuard = charGuard;
		}
	}
}
