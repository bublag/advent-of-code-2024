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

		/*
		part 2: instead of calculating the perimeters, we need to count the sides of the regions
		"Each straight section of fence counts as a side, regardless of how long it is."
		The regions' number of sides are equal to their number of corners. We are counting the corners instead.
		Every "inside" and "outside" corner has to be calculated. I will call a corner "inside" corner if 1 plant is
		inside the corner. I will call a corner "outside" if 3 plants are in the region, but the 1 plant in the corner
		is a different plant in a different region.
		 */
		// go over every point and build up a list of regions and calculate their corners (=sides) at the same time
		final long start2 = System.currentTimeMillis();
		final Set<Point> visitedPoints2 = new LinkedHashSet<>();
		final List<RegionWithSides> regionWithSides = new ArrayList<>();
		for (int y = 0; y < mapMaxY + 1; y++) {
			for (int x = 0; x < mapMaxX + 1; x++) {
				final Point currentPoint = new Point(x, y);
				if (visitedPoints2.contains(currentPoint)) {
					continue;
				}
				final Set<Point> visitedPointsWithinRegion = new LinkedHashSet<>();
				final char plant = map[y][x];
				final int corners = buildRegionWithSidesWithDepthFirstSearch(map, mapMaxX, mapMaxY, currentPoint, plant, visitedPointsWithinRegion, 0);
				visitedPoints2.addAll(visitedPointsWithinRegion);
				regionWithSides.add(new RegionWithSides(visitedPointsWithinRegion, plant, corners));
			}
		}

		// price of fence required for a region is found by multiplying that region's area by its perimeter
		// count the total price of fencing all the regions
		long totalPriceOfFencingRegionsWithDiscount = 0;
		for (final RegionWithSides region : regionWithSides) {
			totalPriceOfFencingRegionsWithDiscount += ((long) region.plantPoints().size()) * region.sides();
		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.println("totalPriceOfFencingRegionsWithDiscount = " + totalPriceOfFencingRegionsWithDiscount);
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

	/**
	 * Go "deeper" in the graph recursively in all 4 directions, if the neighbor location is the same plant as the
	 * current (and inside map bounds). If we cant go in a direction, that means a fence has to be put there so we
	 * increase the perimeter by 1.
	 */
	private static int buildRegionWithDepthFirstSearch(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant,
		final Set<Point> visitedPointsWithinRegion,
		final int currentPerimeter
	) {
		if (visitedPointsWithinRegion.contains(currentPoint)) {
			return currentPerimeter;
		}
		visitedPointsWithinRegion.add(currentPoint);
		int newCurrentPerimeter = currentPerimeter;
		if (currentPoint.y() > 0) { // 1 up is still within bounds
			final Point pointToOneUp = new Point(currentPoint.x(), currentPoint.y() - 1);
			if (map[pointToOneUp.y()][pointToOneUp.x()] == currentPlant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneUp, currentPlant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
			    newCurrentPerimeter++;
			}
		} else {
		    newCurrentPerimeter++;
		}
		if (currentPoint.x() < mapMaxX) { // 1 right is still within bounds
			final Point pointToOneRight = new Point(currentPoint.x() + 1, currentPoint.y());
			if (map[pointToOneRight.y()][pointToOneRight.x()] == currentPlant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneRight, currentPlant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		if (currentPoint.y() < mapMaxY) { // 1 down is still within bounds
			final Point pointToOneDown = new Point(currentPoint.x(), currentPoint.y() + 1);
			if (map[pointToOneDown.y()][pointToOneDown.x()] == currentPlant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneDown, currentPlant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		if (currentPoint.x() > 0) { // 1 left is still within bounds
			final Point pointToOneLeft = new Point(currentPoint.x() - 1, currentPoint.y());
			if (map[pointToOneLeft.y()][pointToOneLeft.x()] == currentPlant) {
				newCurrentPerimeter = buildRegionWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneLeft, currentPlant, visitedPointsWithinRegion, newCurrentPerimeter);
			} else {
				newCurrentPerimeter++;
			}
		} else {
			newCurrentPerimeter++;
		}
		return newCurrentPerimeter;
	}

	/**
	 * Go "deeper" in the graph recursively in all 4 directions, if the neighbor location is the same plant as the
	 * current (and inside map bounds). We have to count the "inside" and the "outside" corners the region has.
	 */
	private static int buildRegionWithSidesWithDepthFirstSearch(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant,
		final Set<Point> visitedPointsWithinRegion,
		final int currentCorners
	) {
		if (visitedPointsWithinRegion.contains(currentPoint)) {
			return currentCorners;
		}
		visitedPointsWithinRegion.add(currentPoint);
		int newCurrentCorners = currentCorners;
		if (checkCornersTopRight(map, mapMaxX, mapMaxY, currentPoint, currentPlant)) {
			newCurrentCorners++;
		}
		if (checkCornersBottomRight(map, mapMaxX, mapMaxY, currentPoint, currentPlant)) {
			newCurrentCorners++;
		}
		if (checkCornersBottomLeft(map, mapMaxX, mapMaxY, currentPoint, currentPlant)) {
			newCurrentCorners++;
		}
		if (checkCornersTopLeft(map, mapMaxX, mapMaxY, currentPoint, currentPlant)) {
			newCurrentCorners++;
		}
		if (currentPoint.y() > 0) { // 1 up is still within bounds
			final Point pointToOneUp = new Point(currentPoint.x(), currentPoint.y() - 1);
			if (map[pointToOneUp.y()][pointToOneUp.x()] == currentPlant) {
				newCurrentCorners = buildRegionWithSidesWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneUp, currentPlant, visitedPointsWithinRegion, newCurrentCorners);
			}
		}
		if (currentPoint.x() < mapMaxX) { // 1 right is still within bounds
			final Point pointToOneRight = new Point(currentPoint.x() + 1, currentPoint.y());
			if (map[pointToOneRight.y()][pointToOneRight.x()] == currentPlant) {
				newCurrentCorners = buildRegionWithSidesWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneRight, currentPlant, visitedPointsWithinRegion, newCurrentCorners);
			}
		}
		if (currentPoint.y() < mapMaxY) { // 1 down is still within bounds
			final Point pointToOneDown = new Point(currentPoint.x(), currentPoint.y() + 1);
			if (map[pointToOneDown.y()][pointToOneDown.x()] == currentPlant) {
				newCurrentCorners = buildRegionWithSidesWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneDown, currentPlant, visitedPointsWithinRegion, newCurrentCorners);
			}
		}
		if (currentPoint.x() > 0) { // 1 left is still within bounds
			final Point pointToOneLeft = new Point(currentPoint.x() - 1, currentPoint.y());
			if (map[pointToOneLeft.y()][pointToOneLeft.x()] == currentPlant) {
				newCurrentCorners = buildRegionWithSidesWithDepthFirstSearch(map, mapMaxX, mapMaxY, pointToOneLeft, currentPlant, visitedPointsWithinRegion, newCurrentCorners);
			}
		}
		return newCurrentCorners;
	}

	/**
	 * We find an "inside" corner, if both 2 neighbors (for example up+right) next to the point have different plant.
	 * We find an "outside" corner, if both 2 neighbors next to the point are the same plant, and the diagonal point in
	 * that direction has a different plant. We only check for "outside" corner where there is no "inside" corner in
	 * that diagonal direction.
	 */
	private static boolean checkCornersTopRight(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant
	) {
		final boolean isTopWithinBounds = currentPoint.y() > 0;
		final Point pointToOneUp = new Point(currentPoint.x(), currentPoint.y() - 1);
		final boolean isTopTheSamePlant = isTopWithinBounds && map[pointToOneUp.y()][pointToOneUp.x()] == currentPlant;

		final boolean isRightWithinBounds = currentPoint.x() < mapMaxX;
		final Point pointToOneRight = new Point(currentPoint.x() + 1, currentPoint.y());
		final boolean isRightTheSamePlant = isRightWithinBounds && map[pointToOneRight.y()][pointToOneRight.x()] == currentPlant;

		if (!isTopTheSamePlant && !isRightTheSamePlant) {
			return true;
		} else if (isTopTheSamePlant && isRightTheSamePlant) {
			final Point pointToTopRightDiagonal = new Point(currentPoint.x() + 1, currentPoint.y() - 1);
			return map[pointToTopRightDiagonal.y()][pointToTopRightDiagonal.x()] != currentPlant; // not the same plant -> "outside corner"
		}
		return false;
	}

	/**
	 * @see #checkCornersTopRight(char[][], int, int, Point, char)
	 */
	private static boolean checkCornersBottomRight(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant
	) {
		final boolean isBottomWithinBounds = currentPoint.y() < mapMaxY;
		final Point pointToOneDown = new Point(currentPoint.x(), currentPoint.y() + 1);
		final boolean isBottomTheSamePlant = isBottomWithinBounds && map[pointToOneDown.y()][pointToOneDown.x()] == currentPlant;

		final boolean isRightWithinBounds = currentPoint.x() < mapMaxX;
		final Point pointToOneRight = new Point(currentPoint.x() + 1, currentPoint.y());
		final boolean isRightTheSamePlant = isRightWithinBounds && map[pointToOneRight.y()][pointToOneRight.x()] == currentPlant;

		if (!isBottomTheSamePlant && !isRightTheSamePlant) {
			return true;
		} else if (isBottomTheSamePlant && isRightTheSamePlant) {
			final Point pointToBottomRightDiagonal = new Point(currentPoint.x() + 1, currentPoint.y() + 1);
			return map[pointToBottomRightDiagonal.y()][pointToBottomRightDiagonal.x()] != currentPlant; // not the same plant -> "outside corner"
		}
		return false;
	}

	/**
	 * @see #checkCornersTopRight(char[][], int, int, Point, char)
	 */
	private static boolean checkCornersBottomLeft(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant
	) {
		final boolean isBottomWithinBounds = currentPoint.y() < mapMaxY;
		final Point pointToOneDown = new Point(currentPoint.x(), currentPoint.y() + 1);
		final boolean isBottomTheSamePlant = isBottomWithinBounds && map[pointToOneDown.y()][pointToOneDown.x()] == currentPlant;

		final boolean isLeftWithinBounds = currentPoint.x() > 0;
		final Point pointToOneLeft = new Point(currentPoint.x() - 1, currentPoint.y());
		final boolean isLeftTheSamePlant = isLeftWithinBounds && map[pointToOneLeft.y()][pointToOneLeft.x()] == currentPlant;

		if (!isBottomTheSamePlant && !isLeftTheSamePlant) {
			return true;
		} else if (isBottomTheSamePlant && isLeftTheSamePlant) {
			final Point pointToBottomLeftDiagonal = new Point(currentPoint.x() - 1, currentPoint.y() + 1);
			return map[pointToBottomLeftDiagonal.y()][pointToBottomLeftDiagonal.x()] != currentPlant; // not the same plant -> "outside corner"
		}
		return false;
	}

	/**
	 * @see #checkCornersTopRight(char[][], int, int, Point, char)
	 */
	private static boolean checkCornersTopLeft(
		final char[][] map,
		final int mapMaxX,
		final int mapMaxY,
		final Point currentPoint,
		final char currentPlant
	) {
		final boolean isTopWithinBounds = currentPoint.y() > 0;
		final Point pointToOneUp = new Point(currentPoint.x(), currentPoint.y() - 1);
		final boolean isTopTheSamePlant = isTopWithinBounds && map[pointToOneUp.y()][pointToOneUp.x()] == currentPlant;

		final boolean isLeftWithinBounds = currentPoint.x() > 0;
		final Point pointToOneLeft = new Point(currentPoint.x() - 1, currentPoint.y());
		final boolean isLeftTheSamePlant = isLeftWithinBounds && map[pointToOneLeft.y()][pointToOneLeft.x()] == currentPlant;

		if (!isTopTheSamePlant && !isLeftTheSamePlant) {
			return true;
		} else if (isTopTheSamePlant && isLeftTheSamePlant) {
			final Point pointToTopRightDiagonal = new Point(currentPoint.x() - 1, currentPoint.y() - 1);
			return map[pointToTopRightDiagonal.y()][pointToTopRightDiagonal.x()] != currentPlant; // not the same plant -> "outside corner"
		}
		return false;
	}

	private record Point(int x, int y) {}

	private record Region(Set<Point> plantPoints, char plant, int perimeter) {}
	private record RegionWithSides(Set<Point> plantPoints, char plant, int sides) {}
}
