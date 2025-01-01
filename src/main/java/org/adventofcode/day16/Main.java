package org.adventofcode.day16;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

//--- Day 16: Reindeer Maze ---
@UtilityClass
public class Main {

	private static final char CHAR_START = 'S';
	private static final char CHAR_END = 'E';
	private static final char CHAR_WALL = '#';

	private static final char CHAR_PATH_VISITED = 'X';

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it into a char 2 dimension array (the map of the maze)
		final char[][] map = new char[inputByLines.size()][];
		Point startPoint = null;
		for (int y = 0; y < inputByLines.size(); y++) {
			final String line = inputByLines.get(y);
			map[y] = new char[line.length()];
			for (int x = 0; x < line.length(); x++) {
				final char currentChar = line.charAt(x);
				map[y][x] = currentChar;
				if (currentChar == CHAR_START) {
					startPoint = new Point(x, y);
				}
			}
		}
		// remember that I need to flip x and y for array calls
		System.out.println("map:");
		printMap(map);

		// get some possible paths which starts from the startPoint and ends at the endPoint
		final List<Path> paths = findPathDepthFirstSearch(map, startPoint, Direction.EAST, new ArrayList<>(), 0L, new LinkedHashMap<>(), new ArrayList<>());
		// get the Path with the lowest score
		final Path lowestScorePath = paths.stream()
			.min(Comparator.comparing(Path::score))
			.orElseThrow(RuntimeException::new);
		final long lowestScorePathScore = lowestScorePath.score();
		System.out.println("path with the lowest score visualized on the map:");
		printPathOnMap(cloneMap(map), startPoint, lowestScorePath);
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("found a total of " + paths.size() + " paths");
		System.out.println("lowestScorePathScore = " + lowestScorePathScore);
//		for (final Path path : paths) {
//			printPathOnMap(cloneMap(map), startPoint, path);
//		}
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

	private static List<Path> findPathDepthFirstSearch(
		final char[][] map,
		final Point pointFrom,
		final Direction directionFacing,
		final List<Point> currentPointsOnPath,
		final long currentScore,
		final LinkedHashMap<Point, Long> visitedPointWithLowestScoreCache,
		final List<Path> foundPaths
	) {
		final char pointFromChar = map[pointFrom.y()][pointFrom.x()];
		if (pointFromChar == CHAR_END) { // found a path to the end -> save it
			foundPaths.add(new Path(currentPointsOnPath, currentScore));
			return foundPaths;
		}
		if (visitedPointWithLowestScoreCache.containsKey(pointFrom)) { // already visited this point previously from anywhere
			final Long savedScoreForThisPoint = visitedPointWithLowestScoreCache.get(pointFrom);
			if (currentScore < savedScoreForThisPoint) { // found a lower score for this point -> update the cache and continue the search
				visitedPointWithLowestScoreCache.put(pointFrom, currentScore);
			} else { // the current score is higher than the cached score for this point -> do not continue the search
				return foundPaths;
			}
		} else { // not yet visited point -> cache it and continue the search
			visitedPointWithLowestScoreCache.put(pointFrom, currentScore);
		}
		currentPointsOnPath.add(pointFrom);
		/*
		We can use the list of visited points for 1 direction. But if we go to multiple directions from here, we need to
		clone the list and pass the new cloned list to the other directions (the different paths can not use the same
		list).
		 */
		boolean firstDirection = true;
		final List<Point> currentPointsOnPathClone = new ArrayList<>(currentPointsOnPath);
		// go "deeper" in the graph recursively in 0-3 directions (never turn back), if the neighbor point is not a wall
		final Point pointToOneNorth = new Point(pointFrom.x(), pointFrom.y() - 1);
		if (directionFacing != Direction.SOUTH && map[pointToOneNorth.y()][pointToOneNorth.x()] != CHAR_WALL) {
			long newCurrentScore = currentScore;
			newCurrentScore++;
			if (directionFacing != Direction.NORTH) { // turning 90 degrees
				newCurrentScore += 1000;
			}
			findPathDepthFirstSearch(
				map,
				pointToOneNorth,
				Direction.NORTH,
				currentPointsOnPath,
				newCurrentScore,
				visitedPointWithLowestScoreCache,
				foundPaths
			);
			firstDirection = false;
		}
		final Point pointToOneEast = new Point(pointFrom.x() + 1, pointFrom.y());
		if (directionFacing != Direction.WEST && map[pointToOneEast.y()][pointToOneEast.x()] != CHAR_WALL) {
			long newCurrentScore = currentScore;
			newCurrentScore++;
			if (directionFacing != Direction.EAST) { // turning 90 degrees
				newCurrentScore += 1000;
			}
			findPathDepthFirstSearch(
				map,
				pointToOneEast,
				Direction.EAST,
				firstDirection ? currentPointsOnPath : new ArrayList<>(currentPointsOnPathClone),
				newCurrentScore,
				visitedPointWithLowestScoreCache,
				foundPaths
			);
			firstDirection = false;
		}
		final Point pointToOneSouth = new Point(pointFrom.x(), pointFrom.y() + 1);
		if (directionFacing != Direction.NORTH && map[pointToOneSouth.y()][pointToOneSouth.x()] != CHAR_WALL) {
			long newCurrentScore = currentScore;
			newCurrentScore++;
			if (directionFacing != Direction.SOUTH) { // turning 90 degrees
				newCurrentScore += 1000;
			}
			findPathDepthFirstSearch(
				map,
				pointToOneSouth,
				Direction.SOUTH,
				firstDirection ? currentPointsOnPath : new ArrayList<>(currentPointsOnPathClone),
				newCurrentScore,
				visitedPointWithLowestScoreCache,
				foundPaths
			);
			firstDirection = false;
		}
		final Point pointToOneWest = new Point(pointFrom.x() - 1, pointFrom.y());
		if (directionFacing != Direction.EAST && map[pointToOneWest.y()][pointToOneWest.x()] != CHAR_WALL) {
			long newCurrentScore = currentScore;
			newCurrentScore++;
			if (directionFacing != Direction.WEST) { // turning 90 degrees
				newCurrentScore += 1000;
			}
			findPathDepthFirstSearch(
				map,
				pointToOneWest,
				Direction.WEST,
				firstDirection ? currentPointsOnPath : new ArrayList<>(currentPointsOnPathClone),
				newCurrentScore,
				visitedPointWithLowestScoreCache,
				foundPaths
			);
		}
		return foundPaths;
	}

	private static void printPathOnMap(final char[][] map, final Point startPoint, final Path path) {
		for (final Point pathPoint : path.points()) {
			map[pathPoint.y()][pathPoint.x()] = CHAR_PATH_VISITED;
		}
		map[startPoint.y()][startPoint.x()] = CHAR_START;
		System.out.println("path with score: " + path.score());
		for (final char[] row : map) {
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

	private record Point(int x, int y) {}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}

	private record Path(List<Point> points, long score) {}
}
