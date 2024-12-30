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

	private static final char CHAR_BOX_PART2_LEFT = '[';
	private static final char CHAR_BOX_PART2_RIGHT = ']';

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

		/*
		part 2: Everything except the robot is twice as wide. Make the wider starting map for part2. Boxes can be
		aligned that they push 2 other boxes (only possible with up and down directions).
		 */
		final long start2 = System.currentTimeMillis();
		final Position robotPositionPart2 = new Position(0, 0);
		final char[][] mapPart2 = new char[inputMapLines.size()][];
		for (int y = 0; y < inputMapLines.size(); y++) {
			final String line = inputMapLines.get(y);
			mapPart2[y] = new char[line.length() * 2];
			for (int x = 0; x < line.length(); x++) {
				final char currentChar = line.charAt(x) == CHAR_BOX ? CHAR_BOX_PART2_LEFT : line.charAt(x);
				final char currentCharWiderPair = switch (currentChar) {
					case CHAR_WALL -> CHAR_WALL;
					case CHAR_BOX_PART2_LEFT -> CHAR_BOX_PART2_RIGHT;
					case CHAR_FREE, CHAR_ROBOT -> CHAR_FREE;
					default -> throw new IllegalStateException("Unexpected value: " + currentChar);
				};
				mapPart2[y][x * 2] = currentChar;
				mapPart2[y][x * 2 + 1] = currentCharWiderPair;
				if (currentChar == CHAR_ROBOT) {
					robotPositionPart2.setX(x * 2);
					robotPositionPart2.setY(y);
				}
			}
		}
		System.out.println("part2 initial map:");
		printMap(mapPart2);

		for (final Direction move : moves) {
//			System.out.println(move);
			moveRobotOrPushBoxIfNotBlockedByWallPart2(mapPart2, robotPositionPart2, move);
			// printing the map is slow so is only here for debug
//			printMap(mapPart2);
		}
		System.out.println("part2 end result map:");
		printMap(mapPart2);
		// iterate over the map positions and sum up the boxes' custom GPS coordinates
		long sumOfBoxGpsCoordinatesPart2 = 0;
		for (int y = 0; y < mapPart2.length; y++) {
			for (int x = 0; x < mapPart2[y].length; x++) {
				if (mapPart2[y][x] == CHAR_BOX_PART2_LEFT) {
					sumOfBoxGpsCoordinatesPart2 += y * 100L + x;
				}
			}
		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.println("sumOfBoxGpsCoordinatesPart2 = " + sumOfBoxGpsCoordinatesPart2);
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

	private static void moveRobotOrPushBoxIfNotBlockedByWallPart2(
		final char[][] map,
		final Position currentRobotPosition,
		final Direction move
	) {
		final char nextPositionChar = getNextPositionChar(map, currentRobotPosition, move);
		switch (nextPositionChar) {
			case CHAR_WALL -> {/* can not move and can not push -> nothing to do */}
			case CHAR_FREE -> moveCurrentPositionToNext(map, currentRobotPosition, move);
			case CHAR_BOX_PART2_LEFT -> {
				final Position nextPositionBoxLeftSide = getNextPosition(currentRobotPosition, move);
				final boolean isBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
				if (isBoxPushed) {
					moveCurrentPositionToNext(map, currentRobotPosition, move);
				}
			}
			case CHAR_BOX_PART2_RIGHT -> {
				final Position nextPositionBoxRightSide = getNextPosition(currentRobotPosition, move);
				final Position nextPositionBoxLeftSide = new Position(nextPositionBoxRightSide.getX() - 1, nextPositionBoxRightSide.getY());
				final boolean isNextPositionMoved = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
				if (isNextPositionMoved) {
					moveCurrentPositionToNext(map, currentRobotPosition, move);
				}
			}
			default -> throw new IllegalStateException("Unexpected value: " + nextPositionChar);
		}
	}

	private static boolean pushBoxIfNotBlockedByWallPart2(
		final char[][] map,
		final Position currentBoxLeftPosition,
		final Direction move
	) {
		final Position currentBoxRightPosition = new Position(currentBoxLeftPosition.getX() + 1, currentBoxLeftPosition.getY());
		final char nextPositionLeftChar = getNextPositionChar(map, currentBoxLeftPosition, move);
		final char nextPositionRightChar = getNextPositionChar(map, currentBoxRightPosition, move);
		switch (move) {
			case RIGHT -> {
				switch (nextPositionRightChar) {
					case CHAR_WALL -> {
						return false;
					}
					case CHAR_FREE -> {
						moveWideBox(map, currentBoxLeftPosition, move);
						return true;
					}
					case CHAR_BOX_PART2_LEFT -> {
						final Position nextPositionBoxLeftSide = getNextPosition(currentBoxRightPosition, move);
						final boolean isNextBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
						if (isNextBoxPushed) {
							moveWideBox(map, currentBoxLeftPosition, move);
						}
						return isNextBoxPushed;
					}
					default -> throw new IllegalStateException("Unexpected value: " + nextPositionRightChar);
				}
			}
			case LEFT -> {
				switch (nextPositionLeftChar) {
					case CHAR_WALL -> {
						return false;
					}
					case CHAR_FREE -> {
						moveWideBox(map, currentBoxLeftPosition, move);
						return true;
					}
					case CHAR_BOX_PART2_RIGHT -> {
						final Position nextPositionBoxRightSide = getNextPosition(currentBoxLeftPosition, move);
						final Position nextPositionBoxLeftSide = new Position(nextPositionBoxRightSide.getX() - 1, nextPositionBoxRightSide.getY());
						final boolean isNextBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
						if (isNextBoxPushed) {
							moveWideBox(map, currentBoxLeftPosition, move);
						}
						return isNextBoxPushed;
					}
					default -> throw new IllegalStateException("Unexpected value: " + nextPositionLeftChar);
				}
			}
			case UP, DOWN -> {
				if (nextPositionLeftChar == CHAR_WALL || nextPositionRightChar == CHAR_WALL) {
					return false;
				} else if (nextPositionLeftChar == CHAR_FREE && nextPositionRightChar == CHAR_FREE) {
					moveWideBox(map, currentBoxLeftPosition, move);
					return true;
				} else if (nextPositionLeftChar == CHAR_BOX_PART2_LEFT) {
					final Position nextPositionBoxLeftSide = getNextPosition(currentBoxLeftPosition, move);
					final boolean isNextBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
					if (isNextBoxPushed) {
						moveWideBox(map, currentBoxLeftPosition, move);
					}
					return isNextBoxPushed;
				} else if (nextPositionLeftChar == CHAR_BOX_PART2_RIGHT && nextPositionRightChar == CHAR_FREE) {
					final Position nextPositionBoxRightSide = getNextPosition(currentBoxLeftPosition, move);
					final Position nextPositionBoxLeftSide = new Position(nextPositionBoxRightSide.getX() - 1, nextPositionBoxRightSide.getY());
					final boolean isNextBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
					if (isNextBoxPushed) {
						moveWideBox(map, currentBoxLeftPosition, move);
					}
					return isNextBoxPushed;
				} else if (nextPositionLeftChar == CHAR_FREE && nextPositionRightChar == CHAR_BOX_PART2_LEFT) {
					final Position nextPositionBoxLeftSide = getNextPosition(currentBoxRightPosition, move);
					final boolean isNextBoxPushed = pushBoxIfNotBlockedByWallPart2(map, nextPositionBoxLeftSide, move);
					if (isNextBoxPushed) {
						moveWideBox(map, currentBoxLeftPosition, move);
					}
					return isNextBoxPushed;
				} else if (nextPositionLeftChar == CHAR_BOX_PART2_RIGHT && nextPositionRightChar == CHAR_BOX_PART2_LEFT) {
					final Position nextPositionLeftBoxRightSide = getNextPosition(currentBoxLeftPosition, move);
					final Position nextPositionLeftBoxLeftSide = new Position(nextPositionLeftBoxRightSide.getX() - 1, nextPositionLeftBoxRightSide.getY());
					final boolean canPushLeftBox = canPushBox(map, nextPositionLeftBoxLeftSide, move);
					final Position nextPositionRightBoxLeftSide = getNextPosition(currentBoxRightPosition, move);
					final boolean canPushRightBox = canPushBox(map, nextPositionRightBoxLeftSide, move);
					if (canPushLeftBox && canPushRightBox) {
						pushBoxIfNotBlockedByWallPart2(map, nextPositionLeftBoxLeftSide, move);
						pushBoxIfNotBlockedByWallPart2(map, nextPositionRightBoxLeftSide, move);
						moveWideBox(map, currentBoxLeftPosition, move);
						return true;
					}
					return false;
				}
			}
		}
		return false;
	}

	private static void moveWideBox(
		final char[][] map,
		final Position boxLeftPosition,
		final Direction move
	) {
		final Position boxRightPosition = new Position(boxLeftPosition.getX() + 1, boxLeftPosition.getY());
		switch (move) {
			case UP -> {
				map[boxLeftPosition.getY() - 1][boxLeftPosition.getX()] = CHAR_BOX_PART2_LEFT;
				map[boxLeftPosition.getY() - 1][boxLeftPosition.getX() + 1] = CHAR_BOX_PART2_RIGHT;
				map[boxLeftPosition.getY()][boxLeftPosition.getX()] = CHAR_FREE;
				map[boxRightPosition.getY()][boxRightPosition.getX()] = CHAR_FREE;
			}
			case RIGHT -> {
				map[boxRightPosition.getY()][boxRightPosition.getX() + 1] = CHAR_BOX_PART2_RIGHT;
				map[boxLeftPosition.getY()][boxLeftPosition.getX() + 1] = CHAR_BOX_PART2_LEFT;
				map[boxLeftPosition.getY()][boxLeftPosition.getX()] = CHAR_FREE;
			}
			case DOWN -> {
				map[boxLeftPosition.getY() + 1][boxLeftPosition.getX()] = CHAR_BOX_PART2_LEFT;
				map[boxLeftPosition.getY() + 1][boxLeftPosition.getX() + 1] = CHAR_BOX_PART2_RIGHT;
				map[boxLeftPosition.getY()][boxLeftPosition.getX()] = CHAR_FREE;
				map[boxRightPosition.getY()][boxRightPosition.getX()] = CHAR_FREE;
			}
			case LEFT -> {
				map[boxLeftPosition.getY()][boxLeftPosition.getX() - 1] = CHAR_BOX_PART2_LEFT;
				map[boxRightPosition.getY()][boxRightPosition.getX() - 1] = CHAR_BOX_PART2_RIGHT;
				map[boxRightPosition.getY()][boxRightPosition.getX()] = CHAR_FREE;
			}
			default -> throw new IllegalStateException("Unexpected value: " + move);
		}
	}

	private static boolean canPushBox(
		final char[][] map,
		final Position currentBoxLeftPosition,
		final Direction move
	) {
		final Position currentBoxRightPosition = new Position(currentBoxLeftPosition.getX() + 1, currentBoxLeftPosition.getY());
		final char nextPositionLeftChar = getNextPositionChar(map, currentBoxLeftPosition, move);
		final char nextPositionRightChar = getNextPositionChar(map, currentBoxRightPosition, move);
		if (move == Direction.UP || move == Direction.DOWN) {
			if (nextPositionLeftChar == CHAR_WALL || nextPositionRightChar == CHAR_WALL) {
				return false;
			} else if (nextPositionLeftChar == CHAR_FREE && nextPositionRightChar == CHAR_FREE) {
				return true;
			} else if (nextPositionLeftChar == CHAR_BOX_PART2_LEFT) {
				final Position nextPositionBoxLeftSide = getNextPosition(currentBoxLeftPosition, move);
				return canPushBox(map, nextPositionBoxLeftSide, move);
			} else if (nextPositionLeftChar == CHAR_BOX_PART2_RIGHT && nextPositionRightChar == CHAR_FREE) {
				final Position nextPositionBoxRightSide = getNextPosition(currentBoxLeftPosition, move);
				final Position nextPositionBoxLeftSide = new Position(nextPositionBoxRightSide.getX() - 1, nextPositionBoxRightSide.getY());
				return canPushBox(map, nextPositionBoxLeftSide, move);
			} else if (nextPositionLeftChar == CHAR_FREE && nextPositionRightChar == CHAR_BOX_PART2_LEFT) {
				final Position nextPositionBoxLeftSide = getNextPosition(currentBoxRightPosition, move);
				return canPushBox(map, nextPositionBoxLeftSide, move);
			} else if (nextPositionLeftChar == CHAR_BOX_PART2_RIGHT && nextPositionRightChar == CHAR_BOX_PART2_LEFT) {
				final Position nextPositionLeftBoxRightSide = getNextPosition(currentBoxLeftPosition, move);
				final Position nextPositionLeftBoxLeftSide = new Position(nextPositionLeftBoxRightSide.getX() - 1, nextPositionLeftBoxRightSide.getY());
				final boolean canPushLeftBox = canPushBox(map, nextPositionLeftBoxLeftSide, move);
				final Position nextPositionRightBoxLeftSide = getNextPosition(currentBoxRightPosition, move);
				final boolean canPushRightBox = canPushBox(map, nextPositionRightBoxLeftSide, move);
				return canPushLeftBox && canPushRightBox;
			} else {
			    throw new IllegalStateException("Unexpected next positions: '" + nextPositionLeftChar + "', '" + nextPositionRightChar + "'");
			}
		} else {
			throw new IllegalStateException("Unexpected value: " + move);
		}
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
