package org.adventofcode.day10;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it into a char 2 dimension array
		// build up a topographic map which has the height information of every position
		final int[][] map = new int[inputByLines.size()][];
		final List<TrailHead> trailHeads = new ArrayList<>();
		for (int y = 0; y < inputByLines.size(); y++) {
			final String line = inputByLines.get(y);
			map[y] = new int[line.length()];
			for (int x = 0; x < line.length(); x++) {
				final int height = Character.getNumericValue(line.charAt(x));
				map[y][x] = height;
				if (height == 0) {
					trailHeads.add(new TrailHead(new Point(x, y)));
				}
			}
		}
		final int mapMaxX = map[0].length - 1;
		final int mapMaxY = map.length - 1;

		// remember that I need to flip x and y for array calls
		System.out.println("map:");
		printMap(map);

		// count the scores of every trailhead and add those together
		for (final TrailHead trailHead : trailHeads) {
			trailHead.setScore(countScoreDepthFirstSearch(map, mapMaxX, mapMaxY, trailHead.getPoint(), new LinkedHashSet<>(), 0));
		}
		System.out.println("trailHeads = " + trailHeads);
		final long sumOfAllTrailHeadScores = trailHeads.stream()
			.map(TrailHead::getScore)
			.mapToLong(value -> value)
			.sum();
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("sumOfAllTrailHeadScores = " + sumOfAllTrailHeadScores);
	}

	private static int countScoreDepthFirstSearch(
		final int[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point pointFrom,
		final Set<Point> visitedLocations,
		final int currentScore
	) {
		if (visitedLocations.contains(pointFrom)) { // already visited location in the current graph search
			return currentScore;
		}
		visitedLocations.add(pointFrom);
		final int pointFromHeight = map[pointFrom.y()][pointFrom.x()];
		if (pointFromHeight == 9) { // found a 9-height location -> increase the score
			return currentScore + 1;
		}
		int newCurrentScore = currentScore;
		// go "deeper" in the graph recursively in all 4 directions, if the neighbor location is exactly 1 height greater (and inside map bounds)
		if (pointFrom.y() > 0) { // 1 up is still within bounds
			final Point pointToOneUp = new Point(pointFrom.x(), pointFrom.y() - 1);
			if (map[pointToOneUp.y()][pointToOneUp.x()] == pointFromHeight + 1) {
				newCurrentScore = countScoreDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneUp, visitedLocations, newCurrentScore);
			}
		}
		if (pointFrom.x() < mapMaxX) { // 1 right is still within bounds
			final Point pointToOneRight = new Point(pointFrom.x() + 1, pointFrom.y());
			if (map[pointToOneRight.y()][pointToOneRight.x()] == pointFromHeight + 1) {
				newCurrentScore = countScoreDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneRight, visitedLocations, newCurrentScore);
			}
		}
		if (pointFrom.y() < mapMaxY) { // 1 down is still within bounds
			final Point pointToOneDown = new Point(pointFrom.x(), pointFrom.y() + 1);
			if (map[pointToOneDown.y()][pointToOneDown.x()] == pointFromHeight + 1) {
				newCurrentScore = countScoreDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneDown, visitedLocations, newCurrentScore);
			}
		}
		if (pointFrom.x() > 0) { // 1 left is still within bounds
			final Point pointToOneLeft = new Point(pointFrom.x() - 1, pointFrom.y());
			if (map[pointToOneLeft.y()][pointToOneLeft.x()] == pointFromHeight + 1) {
				newCurrentScore = countScoreDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneLeft, visitedLocations, newCurrentScore);
			}
		}
		return newCurrentScore;
	}

	private static void printMap(final int[][] map) {
		for (final int[] y : map) {
			for (final int x : y) {
				System.out.print(x);
			}
			System.out.println();
		}
		System.out.println();
	}

	private record Point(int x, int y) {}

	@Getter
	@ToString
	private static final class TrailHead {
		private final Point point;
		@Setter
		private int score = 0;

		private TrailHead(final Point point) {
			this.point = point;
		}
	}
}
