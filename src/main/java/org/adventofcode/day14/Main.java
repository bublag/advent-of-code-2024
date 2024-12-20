package org.adventofcode.day14;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//--- Day 14: Restroom Redoubt ---
@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it to robots
		int robotIdCounter = 0;
		final List<Robot> robots = new ArrayList<>();
		final List<Robot> robotsPart2 = new ArrayList<>();
		for (final String line : inputByLines) {
			final Robot robot = convertLineToRobot(line, robotIdCounter);
			robots.add(robot);
			robotsPart2.add(Robot.copyOf(robot));
			robotIdCounter++;
		}
		final int mapWide = 101;
		final int mapTall = 103;
		final int mapMaxX = mapWide - 1;
		final int mapMaxY = mapTall - 1;

		/*
		The robots are moving with their velocity every second, teleporting to the other side if they reach a wall (the
		map is a rectangle). Simulate 100 seconds and move the robots 100 times.
		 */
		for (int i = 0; i < 100; i++) {
			for (final Robot robot : robots) {
				moveRobotOneSecond(robot, mapMaxX, mapMaxY);
			}
		}
		System.out.println("robots after 100 seconds =");
		robots.forEach(System.out::println);
		System.out.println();

		// calculate how many robots are in the 4 quadrant, without considering the middle row and the middle column
		final int mapMiddleRow = mapMaxY / 2;
		final int mapMiddleColumn = mapMaxX / 2;
		final int robotsInTopLeft = countRobotsInQuadrant(robots, 0, mapMiddleColumn - 1, 0, mapMiddleRow - 1);
		final int robotsInTopRight = countRobotsInQuadrant(robots, mapMiddleColumn + 1, mapMaxX, 0, mapMiddleRow - 1);
		final int robotsInBottomRight = countRobotsInQuadrant(robots, mapMiddleColumn + 1, mapMaxX, mapMiddleRow + 1, mapMaxY);
		final int robotsInBottomLeft = countRobotsInQuadrant(robots, 0, mapMiddleColumn - 1, mapMiddleRow + 1, mapMaxY);
		final long totalSafetyFactor = ((long) robotsInTopLeft) * robotsInTopRight * robotsInBottomRight * robotsInBottomLeft;
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("totalSafetyFactor = " + totalSafetyFactor);

		// part 2: print them out every second to see when the robots' position themselves into the shape of a Christmas tree
		final long start2 = System.currentTimeMillis();
		long secondsCounter = 0;
		while (!isAny9BlockFullOfRobots(robotsPart2, mapMaxX, mapMaxY)) {
			System.out.println("secondsCounter = " + ++secondsCounter);
			for (final Robot robot : robotsPart2) {
				moveRobotOneSecond(robot, mapMaxX, mapMaxY);
			}
			printRobots(robotsPart2, mapWide, mapTall);
		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.println("secondsCounter = " + secondsCounter);
	}

	private static Robot convertLineToRobot(final String line, final int robotId) {
		final String[] lineSplit = line.split(" ");
		final String positionPart = lineSplit[0];
		final String[] positionSplit = positionPart.split(",");
		final String velocityPart = lineSplit[1];
		final String[] velocitySplit = velocityPart.split(",");
		return new Robot(
			robotId,
			new Position(
				Integer.parseInt(positionSplit[0].substring(2)),
				Integer.parseInt(positionSplit[1])
			),
			new Velocity(
				Integer.parseInt(velocitySplit[0].substring(2)),
				Integer.parseInt(velocitySplit[1])
			)
		);
	}

	private static void moveRobotOneSecond(final Robot robot, final int mapMaxX, final int mapMaxY) {
		final Position position = robot.position();
		final Velocity velocity = robot.velocity();
		final int xPositionAndVelocitySum = position.getX() + velocity.x();
		if (xPositionAndVelocitySum > mapMaxX) { // teleport from right wall to left wall
			position.setX((xPositionAndVelocitySum) - mapMaxX - 1);
		} else if (xPositionAndVelocitySum < 0) { // teleport from left wall to right wall
			position.setX(mapMaxX + xPositionAndVelocitySum + 1);
		} else {
		    position.setX(xPositionAndVelocitySum); // does not teleport
		}
		final int yPositionAndVelocitySum = position.getY() + velocity.y();
		if (yPositionAndVelocitySum > mapMaxY) { // teleport from bottom wall to top wall
			position.setY((yPositionAndVelocitySum) - mapMaxY - 1);
		} else if (yPositionAndVelocitySum < 0) { // teleport from top wall to bottom wall
			position.setY(mapMaxY + yPositionAndVelocitySum + 1);
		} else {
		    position.setY(yPositionAndVelocitySum); // does not teleport
		}
	}

	private static int countRobotsInQuadrant(
		final List<Robot> robots,
		final int fromX,
		final int toX,
		final int fromY,
		final int toY
	) {
		int robotCounter = 0;
		for (final Robot robot : robots) {
			if (robot.position().getX() >= fromX
				&& robot.position().getX() <= toX
				&& robot.position().getY() >= fromY
				&& robot.position().getY() <= toY
			) {
				robotCounter++;
			}
		}
		return robotCounter;
	}

	private static void printRobots(final List<Robot> robotsPart2, final int mapWide, final int mapTall) {
		final char[][] map = createEmptyMap(mapWide, mapTall);
		for (final Robot robot : robotsPart2) {
			map[robot.position().getY()][robot.position().getX()] = '*';
		}
		for (final char[] row : map) {
			for (final char c : row) {
				System.out.print(c);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static char[][] createEmptyMap(final int mapWide, final int mapTall) {
		final char[][] map = new char[mapTall][];
		for (int y = 0; y < mapTall; y++) {
			map[y] = new char[mapWide];
			for (int x = 0; x < mapWide; x++) {
				map[y][x] = '.';
			}
		}
		return map;
	}

	/**
	 * Hoping to find a Christmas tree shape where there is a 3*3 block full of robots. Fortunately, it worked...
	 */
	private static boolean isAny9BlockFullOfRobots(final List<Robot> robots, final int mapMaxX, final int mapMaxY) {
		final Set<Position> robotsPositions = new HashSet<>();
		for (final Robot robot : robots) {
			robotsPositions.add(robot.position());
		}
		for (int y = 1; y < mapMaxY; y++) {
			for (int x = 1; x < mapMaxX; x++) {
				if (robotsPositions.contains(new Position(x, y)) // middle
					&& robotsPositions.contains(new Position(x, y - 1)) // top
					&& robotsPositions.contains(new Position(x + 1, y - 1)) // top-right
					&& robotsPositions.contains(new Position(x + 1, y)) // right
					&& robotsPositions.contains(new Position(x + 1, y + 1)) // bottom-right
					&& robotsPositions.contains(new Position(x, y + 1)) // bottom
					&& robotsPositions.contains(new Position(x - 1, y + 1)) // bottom-left
					&& robotsPositions.contains(new Position(x - 1, y)) // left
					&& robotsPositions.contains(new Position(x - 1, y - 1)) // top-left
				) {
					return true;
				}
			}
		}
		return false;
	}

	@Data
	@AllArgsConstructor
	private static final class Position {
		private int x;
		private int y;

		public static Position copyOf(final Position position) {
			return new Position(position.getX(), position.getY());
		}
	}

	private record Velocity(int x, int y) {}

	private record Robot(int id, Position position, Velocity velocity) {
		public static Robot copyOf(final Robot robot) {
			return new Robot(robot.id(), Position.copyOf(robot.position()), robot.velocity());
		}
	}
}
