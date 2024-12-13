package org.adventofcode.day12;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//--- Day 12: Garden Groups ---
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
		final char[][] map = new char[inputByLines.size()][];
		for (int y = 0; y < inputByLines.size(); y++) {
			final String line = inputByLines.get(y);
			map[y] = new char[line.length()];
			for (int x = 0; x < line.length(); x++) {
				map[y][x] = line.charAt(x);
			}
		}
		final int mapMaxX = map[0].length - 1;
		final int mapMaxY = map.length - 1;

		// remember that I need to flip x and y for array calls
		System.out.println("map:");
		printMap(map);

		/*
		garden plot = region = an area in which same type of plants are connected (4-directional neighbors)
		area of a region = the number of garden plot (plant) inside it
		perimeter of the region = the number of sides of garden plots in the region that do not touch another garden plot in the same region
		 */
		// go over every point and build up a list of regions and calculate their perimeter at the same time
		final Set<Point> visitedPoints = new LinkedHashSet<>();
		final List<Region> regions = new ArrayList<>();
		for (int y = 0; y < mapMaxY + 1; y++) {
			for (int x = 0; x < mapMaxX + 1; x++) {
				final Point currentPoint = new Point(x, y);
				if (visitedPoints.contains(currentPoint)) {
					continue;
				}
				final Set<Point> visitedPointsWithinRegion = new LinkedHashSet<>();
				final char plant = map[y][x];
				final int perimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, currentPoint, plant, visitedPointsWithinRegion, 0);
				visitedPoints.addAll(visitedPointsWithinRegion);
				regions.add(new Region(visitedPointsWithinRegion, plant, perimeter));
			}
		}

		// price of fence required for a region is found by multiplying that region's area by its perimeter
		// count the total price of fencing all the regions
		long totalPriceOfFencingRegions = 0;
		for (final Region region : regions) {
			totalPriceOfFencingRegions += ((long) region.plantPoints().size()) * region.perimeter();
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("totalPriceOfFencingRegions = " + totalPriceOfFencingRegions);
		System.out.println();
	}

	private static void printMap(final char[][] map) {
		for (final char[] y : map) {
			for (final char x : y) {
				System.out.print(x);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static int buildRegionWithDepthFirstSearch(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char plant,
		final Set<Point> visitedPointsWithinRegion,
		final int currentPerimeter
	) {
		if (visitedPointsWithinRegion.contains(currentPoint)) {
			return currentPerimeter;
		}
		visitedPointsWithinRegion.add(currentPoint);
		int newCurrentPerimeter = currentPerimeter;
		// go "deeper" in the graph recursively in all 4 directions, if the neighbor location is the same plant as the current (and inside map bounds)
		// if we cant go in a direction, that means a fence has to be put there so we increase the perimeter by 1
		if (currentPoint.y() > 0) { // 1 up is still within bounds
			final Point pointToOneUp = new Point(currentPoint.x(), currentPoint.y() - 1);
			if (map[pointToOneUp.y()][pointToOneUp.x()] == plant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneUp, plant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
			    newCurrentPerimeter++;
			}
		} else {
		    newCurrentPerimeter++;
		}
		if (currentPoint.x() < mapMaxX) { // 1 right is still within bounds
			final Point pointToOneRight = new Point(currentPoint.x() + 1, currentPoint.y());
			if (map[pointToOneRight.y()][pointToOneRight.x()] == plant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneRight, plant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		if (currentPoint.y() < mapMaxY) { // 1 down is still within bounds
			final Point pointToOneDown = new Point(currentPoint.x(), currentPoint.y() + 1);
			if (map[pointToOneDown.y()][pointToOneDown.x()] == plant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneDown, plant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		if (currentPoint.x() > 0) { // 1 left is still within bounds
			final Point pointToOneLeft = new Point(currentPoint.x() - 1, currentPoint.y());
			if (map[pointToOneLeft.y()][pointToOneLeft.x()] == plant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneLeft, plant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		return newCurrentPerimeter;
	}

	private record Point(int x, int y) {}

	private record Region(Set<Point> plantPoints, char plant, int perimeter) {}
}
