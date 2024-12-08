package org.adventofcode.day06;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class Main {

	private static final char CHAR_GUARD_FACING_NORTH = '^';
	private static final char CHAR_GUARD_FACING_EAST = '>';
	private static final char CHAR_GUARD_FACING_SOUTH = 'v';
	private static final char CHAR_GUARD_FACING_WEST = '<';
	private static final char CHAR_OBSTACLE = '#';
	private static final char CHAR_OBSTACLE_CUSTOM = 'O';
	private static final Set<Character> OBSTACLE_CHARS = Set.of(CHAR_OBSTACLE, CHAR_OBSTACLE_CUSTOM);
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
		final char[][] unModifiedMapCharTable = new char[inputByLines.size()][];
		for (int i = 0; i < inputByLines.size(); i++) {
			final String line = inputByLines.get(i);
			unModifiedMapCharTable[i] = line.toCharArray();
			if (line.contains(String.valueOf(CHAR_GUARD_FACING_NORTH))) {
				guardXpos = line.indexOf(CHAR_GUARD_FACING_NORTH);
				guardYpos = i;
			}
		}
		final int guardStartingXpos = guardXpos;
		final int guardStartingYpos = guardYpos;
		final Direction guardStartingDirection = Direction.NORTH;

		// to remember that I need to flip x and y for array calls
		System.out.printf("guard ('%s') starting (x,y) position: (%d,%d)%n", unModifiedMapCharTable[guardStartingYpos][guardStartingXpos], guardStartingXpos, guardStartingYpos);
		printMap(unModifiedMapCharTable);

		/*
		Move the guard and mark the visited locations with an 'X', until the guard leaves the border of the map.
		Move the guard straight until she reaches an obstacle ('#'), then turn her right 90 degrees.
		 */
		char[][] mapCharTable = cloneMap(unModifiedMapCharTable);
		Direction guardDirection = guardStartingDirection;
		boolean isGuardWithinMapBorders = true;
		final Set<Pair<Integer, Integer>> guardVisitedPositions = new LinkedHashSet<>();
		while (isGuardWithinMapBorders) {
			if (isGuardFacingTheBorderOfTheMap(mapCharTable, guardXpos, guardYpos, guardDirection)) {
				markGuardPosition(mapCharTable, guardXpos, guardYpos);
				isGuardWithinMapBorders = false;
				guardVisitedPositions.add(Pair.of(guardXpos, guardYpos));
			} else if (isGuardFacingObstacle(mapCharTable, guardXpos, guardYpos, guardDirection)) {
				guardDirection = turnGuard90Degrees(guardDirection);
				drawGuard(mapCharTable, guardXpos, guardYpos, guardDirection);
			} else {
				markGuardPosition(mapCharTable, guardXpos, guardYpos);
				final Pair<Integer, Integer> newGuardPosition = moveGuard1Step(guardXpos, guardYpos, guardDirection);
				guardXpos = newGuardPosition.getLeft();
				guardYpos = newGuardPosition.getRight();
				drawGuard(mapCharTable, guardXpos, guardYpos, guardDirection);
				guardVisitedPositions.add(Pair.of(guardXpos, guardYpos));
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

		System.out.println("guardVisitedPositions = " + guardVisitedPositions);
		/*
		part 2: We have to make the guard stuck in a loop by placing down 1 obstacle. We need to count all possible
		positions where we make the guard stuck in a loop if we place an obstacle on that position.
		We only modify the guard's movement if we place the obstacle on a position where she steps into. So we don't
		have to try every possible empty positions, just the ones she stepped on.
		If the guard steps into a position the 3rd time, she is in a loop.
		 */
		//TODO optimize part 2, because it took ~16sec for it to finish on the real input (not the example)
		long guardGotStuckInLoopCounter = 0;
		for (final Pair<Integer, Integer> guardVisitedPosition : guardVisitedPositions) {
			mapCharTable = cloneMap(unModifiedMapCharTable);
			mapCharTable[guardVisitedPosition.getRight()][guardVisitedPosition.getLeft()] = CHAR_OBSTACLE_CUSTOM; // placing down the extra obstacle
			guardXpos = guardStartingXpos;
			guardYpos = guardStartingYpos;
			guardDirection = guardStartingDirection;
			final Map<Pair<Integer, Integer>, AtomicInteger> guardSteppedPositionsCounterMap = new LinkedHashMap<>();
			isGuardWithinMapBorders = true;
			boolean isGuardGotStuckInLoop = false;
			while (isGuardWithinMapBorders && !isGuardGotStuckInLoop) {
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
					// increment the counter for the guard's new position and check if she got stuck in a loop
					if (guardSteppedPositionsCounterMap.containsKey(newGuardPosition)) {
						guardSteppedPositionsCounterMap.get(newGuardPosition).incrementAndGet();
					} else {
						guardSteppedPositionsCounterMap.put(newGuardPosition, new AtomicInteger(1));
					}
					if (guardSteppedPositionsCounterMap.get(newGuardPosition).get() == 4) {
						isGuardGotStuckInLoop = true;
					}
				}
			}
			if (isGuardGotStuckInLoop) {
				guardGotStuckInLoopCounter++;
			}
		}
		System.out.println("guardGotStuckInLoopCounter = " + guardGotStuckInLoopCounter);
	}

	private static void printMap(final char[][] mapCharTable) {
		for (final char[] row : mapCharTable) {
			for (final char c : row) {
				System.out.print(c);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static char[][] cloneMap(final char[][] originalMap) {
		final char[][] clonedMap = new char[originalMap.length][];
		for (int i = 0; i < originalMap.length; i++) {
			clonedMap[i] = Arrays.copyOf(originalMap[i], originalMap[i].length);
		}
		return clonedMap;
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
			case NORTH -> OBSTACLE_CHARS.contains(mapCharTable[guardYpos - 1][guardXpos]);
			case EAST -> OBSTACLE_CHARS.contains(mapCharTable[guardYpos][guardXpos + 1]);
			case SOUTH -> OBSTACLE_CHARS.contains(mapCharTable[guardYpos + 1][guardXpos]);
			case WEST -> OBSTACLE_CHARS.contains(mapCharTable[guardYpos][guardXpos - 1]);
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
