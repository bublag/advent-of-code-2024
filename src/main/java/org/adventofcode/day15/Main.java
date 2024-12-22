package org.adventofcode.day15;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//--- Day 15: Warehouse Woes ---
@UtilityClass
public class Main {

	private static final char CHAR_ROBOT = '@';
	private static final char CHAR_BOX = 'O';
	private static final char CHAR_WALL = '#';
	private static final char CHAR_FREE = '.';
	private static final char CHAR_ROBOT_MOVEMENT_UP = '^';
	private static final char CHAR_ROBOT_MOVEMENT_RIGHT = '>';
	private static final char CHAR_ROBOT_MOVEMENT_DOWN = 'v';
	private static final char CHAR_ROBOT_MOVEMENT_LEFT = '<';

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// split the input to 2, to the map and to the movements
		final List<String> inputMapLines = new ArrayList<>();
		final List<String> inputMovementsLines = new ArrayList<>();
		for (final String inputByLine : inputByLines) {
			if (StringUtils.isBlank(inputByLine)) {
				continue;
			}
			final char firstCharInLine = inputByLine.charAt(0);
			if (firstCharInLine == CHAR_WALL) {
				inputMapLines.add(inputByLine);
			} else if (List.of(CHAR_ROBOT_MOVEMENT_UP,
					CHAR_ROBOT_MOVEMENT_RIGHT,
					CHAR_ROBOT_MOVEMENT_DOWN,
					CHAR_ROBOT_MOVEMENT_LEFT
				).contains(firstCharInLine)
			) {
				inputMovementsLines.add(inputByLine);
			}
		}

		final long start = System.currentTimeMillis();
		// convert the map into a char 2 dimension array, and save the position of the robot
		final Position robotPosition = new Position(0, 0);
		final char[][] map = new char[inputMapLines.size()][];
		for (int y = 0; y < inputMapLines.size(); y++) {
			final String line = inputMapLines.get(y);
			map[y] = new char[line.length()];
			for (int x = 0; x < line.length(); x++) {
				final char currentChar = line.charAt(x);
				map[y][x] = currentChar;
				if (currentChar == CHAR_ROBOT) {
					robotPosition.setX(x);
					robotPosition.setY(y);
				}
			}
		}

		// convert the list of moves
		final List<Direction> moves = new ArrayList<>();
		for (final String inputMovementsLine : inputMovementsLines) {
			for (final char move : inputMovementsLine.toCharArray()) {
				moves.add(Direction.fromChar(move));
			}
		}

		// remember that I need to flip x and y for array calls
		System.out.println("initial map:");
		printMap(map);
		System.out.println("moves.size() = " + moves.size());

		/*
		Iterate over every move and try to apply it to the robot. The robot can push boxes in front of it. The robot can
		not move and can not push boxes if there is a wall blocking it in that direction.
		 */
		for (final Direction move : moves) {
//			System.out.println(move);
			moveRobotOrPushBoxIfNotBlockedByWall(map, robotPosition, move);
			// printing the map is slow so is only here for debug
//			printMap(map);
		}
		System.out.println("end result map:");
		printMap(map);
		// iterate over the map positions and sum up the boxes' custom GPS coordinates
		long sumOfBoxGpsCoordinates = 0;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				if (map[y][x] == CHAR_BOX) {
					sumOfBoxGpsCoordinates += y * 100L + x;
				}
			}
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("sumOfBoxGpsCoordinates = " + sumOfBoxGpsCoordinates);
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

	private static boolean moveRobotOrPushBoxIfNotBlockedByWall(
		final char[][] map,
		final Position currentPosition,
		final Direction move
	) {
		final char nextPositionChar = getNextPositionChar(map, currentPosition, move);
		return switch (nextPositionChar) {
			case CHAR_FREE -> {
				moveCurrentPositionToNext(map, currentPosition, move);
				yield true;
			}
			case CHAR_BOX -> {
				final Position nextPosition = getNextPosition(currentPosition, move);
				final boolean isNextPositionMoved = moveRobotOrPushBoxIfNotBlockedByWall(map, nextPosition, move);
				if (isNextPositionMoved) {
					moveCurrentPositionToNext(map, currentPosition, move);
				}
				yield isNextPositionMoved;
			}
			case CHAR_WALL -> false;
			default -> throw new IllegalStateException("Unexpected value: " + nextPositionChar);
		};
	}

	private static char getNextPositionChar(
		final char[][] map,
		final Position currentPosition,
		final Direction move
	) {
		final Position nextPosition = getNextPosition(currentPosition, move);
		return map[nextPosition.getY()][nextPosition.getX()];
	}

	private static Position getNextPosition(final Position currentPosition, final Direction move) {
		return switch (move) {
			case UP -> new Position(currentPosition.getX(), currentPosition.getY() - 1);
			case RIGHT -> new Position(currentPosition.getX() + 1, currentPosition.getY());
			case DOWN -> new Position(currentPosition.getX(), currentPosition.getY() + 1);
			case LEFT -> new Position(currentPosition.getX() - 1, currentPosition.getY());
			case null -> throw new IllegalStateException("Unexpected value: " + null);
		};
	}

	private static void moveCurrentPositionToNext(
		final char[][] map,
		final Position currentPosition,
		final Direction move
	) {
		final char currentCharToMove = map[currentPosition.getY()][currentPosition.getX()];
		final Position nextPosition = getNextPosition(currentPosition, move);
		map[nextPosition.getY()][nextPosition.getX()] = currentCharToMove;
		map[currentPosition.getY()][currentPosition.getX()] = CHAR_FREE;
		// so that we always update the robot's position and can keep track of it
		currentPosition.setX(nextPosition.getX());
		currentPosition.setY(nextPosition.getY());
	}

	@Getter
	private enum Direction {
		UP,
		RIGHT,
		DOWN,
		LEFT;

		public static Direction fromChar(final char directionChar) {
			return switch (directionChar) {
				case CHAR_ROBOT_MOVEMENT_UP -> UP;
				case CHAR_ROBOT_MOVEMENT_RIGHT -> RIGHT;
				case CHAR_ROBOT_MOVEMENT_DOWN -> DOWN;
				case CHAR_ROBOT_MOVEMENT_LEFT -> LEFT;
				default -> throw new IllegalStateException("Unexpected value: " + directionChar);
			};
		}
	}

	@Data
	@AllArgsConstructor
	private static final class Position {
		private int x;
		private int y;
	}
}
