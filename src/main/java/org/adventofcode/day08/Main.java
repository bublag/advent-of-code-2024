package org.adventofcode.day08;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//--- Day 8: Resonant Collinearity ---
@UtilityClass
public class Main {

	private static final char CHAR_EMPTY = '.';
	private static final char CHAR_ANTINODE = '#';

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// convert it into a char 2 dimension array
		// collect the antennas' positions based on their frequency (different characters means different frequencies)
		final Map<Character, List<Pair<Integer, Integer>>> antennaLocationsMap = new TreeMap<>(); // so the characters are sorted and debug is easier if needed
		// 'mapCharTable' is not needed for the solution. only used for visualization
		final char[][] unModifiedMapCharTable = new char[inputByLines.size()][];
		for (int y = 0; y < inputByLines.size(); y++) {
			final String line = inputByLines.get(y);
			unModifiedMapCharTable[y] = line.toCharArray();
			for (int x = 0; x < unModifiedMapCharTable[y].length; x++) {
				final char charInLine = unModifiedMapCharTable[y][x];
				if (charInLine == CHAR_EMPTY) {
					continue;
				}
				antennaLocationsMap.putIfAbsent(charInLine, new ArrayList<>());
				antennaLocationsMap.get(charInLine).add(Pair.of(x, y));
			}
		}
		final int mapMaxX = unModifiedMapCharTable[0].length - 1;
		final int mapMaxY = unModifiedMapCharTable.length - 1;

		// remember that I need to flip x and y for array calls
		System.out.println("starting map:");
		printMap(unModifiedMapCharTable);
		System.out.println("antennaLocationsMap = " + antennaLocationsMap);
		System.out.println();

		// calculate how many unique antinodes are within the map boundaries
		char[][] mapCharTable = cloneMap(unModifiedMapCharTable);
		final Set<Pair<Integer, Integer>> uniqueAntiNodeLocations = new LinkedHashSet<>();
		for (final Map.Entry<Character, List<Pair<Integer, Integer>>> antennaLocationsEntry : antennaLocationsMap.entrySet()) {
			// if the antenna locations size is 1, this combinations list will be empty
			final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> antennaLocationPairCombinations = createPairCombinations(antennaLocationsEntry.getValue());
			for (final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antennaLocationPair : antennaLocationPairCombinations) {
				final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antiNodeLocationsForAntennaPair = createAntiNodeLocations(antennaLocationPair);
				saveAndMarkAntiNodeLocationIfWithinBounds(antiNodeLocationsForAntennaPair.getLeft(), mapMaxX, mapMaxY, uniqueAntiNodeLocations, mapCharTable);
				saveAndMarkAntiNodeLocationIfWithinBounds(antiNodeLocationsForAntennaPair.getRight(), mapMaxX, mapMaxY, uniqueAntiNodeLocations, mapCharTable);
			}
		}
		System.out.println("final map with the antinodes marked");
		System.out.println("(when at a location there is an antenna and the location is also an antinode, the antinode is not marked, only the antenna is marked there):");
		printMap(mapCharTable);
		System.out.println("uniqueAntiNodeLocations size = " + uniqueAntiNodeLocations.size());

		/*
		 part 2: updating the model with "resonant harmonics effect": the antinodes are repeating on the line of 2
		 antennas, and we track them until the line goes off the map
		 + rule: the antennas are also considered antinodes if the antenna is not alone in its frequency
		 */
		mapCharTable = cloneMap(unModifiedMapCharTable);
		final Set<Pair<Integer, Integer>> uniqueAntiNodeLocationsConsideringResonantHarmonicEffect = new LinkedHashSet<>();
		for (final Map.Entry<Character, List<Pair<Integer, Integer>>> antennaLocationsEntry : antennaLocationsMap.entrySet()) {
			final List<Pair<Integer, Integer>> antennaLocationsForCurrentFrequency = antennaLocationsEntry.getValue();
			if (antennaLocationsForCurrentFrequency.size() > 1) { // here is the + rule
				uniqueAntiNodeLocationsConsideringResonantHarmonicEffect.addAll(antennaLocationsForCurrentFrequency);
			}
			// if the antenna locations size is 1, this combinations list will be empty
			final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> antennaLocationPairCombinations = createPairCombinations(antennaLocationsForCurrentFrequency);
			for (final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antennaLocationPair : antennaLocationPairCombinations) {
				uniqueAntiNodeLocationsConsideringResonantHarmonicEffect.addAll(
					createAntiNodeLocationsRightConsideringResonantHarmonicEffect(antennaLocationPair, mapMaxX, mapMaxY, mapCharTable)
				);
				uniqueAntiNodeLocationsConsideringResonantHarmonicEffect.addAll(
					createAntiNodeLocationsLeftConsideringResonantHarmonicEffect(antennaLocationPair, mapMaxX, mapMaxY, mapCharTable)
				);
			}
		}
		System.out.println();
		System.out.println("final map with the antinodes marked also considering \"resonant harmonics effect\"");
		System.out.println("(when at a location there is an antenna and the location is also an antinode, the antinode is not marked, only the antenna is marked there):");
		printMap(mapCharTable);
		System.out.println("uniqueAntiNodeLocationsConsideringResonantHarmonicEffect size = " + uniqueAntiNodeLocationsConsideringResonantHarmonicEffect.size());
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

	private static List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> createPairCombinations(
		final List<Pair<Integer, Integer>> characterLocations
	) {
		final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> locationPairCombinations = new ArrayList<>();
		for (int i = 0; i < characterLocations.size() - 1; i++) {
			for (int j = i + 1; j < characterLocations.size(); j++) {
				locationPairCombinations.add(Pair.of(characterLocations.get(i), characterLocations.get(j)));
			}
		}
		return locationPairCombinations;
	}

	private static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> createAntiNodeLocations(
		final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antennaLocationPair
	) {
		return Pair.of(
			createAntiNodeLocationRight(antennaLocationPair),
			createAntiNodeLocationLeft(antennaLocationPair)
		);
	}

	private static Pair<Integer, Integer> createAntiNodeLocationRight(
		final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antennaLocationPair
	) {
		return Pair.of(
			antennaLocationPair.getLeft().getLeft() + (antennaLocationPair.getLeft().getLeft() - antennaLocationPair.getRight().getLeft()),
			antennaLocationPair.getLeft().getRight() - (antennaLocationPair.getRight().getRight() - antennaLocationPair.getLeft().getRight())
		);
	}

	private static Pair<Integer, Integer> createAntiNodeLocationLeft(
		final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> antennaLocationPair
	) {
		return Pair.of(
			antennaLocationPair.getRight().getLeft() - (antennaLocationPair.getLeft().getLeft() - antennaLocationPair.getRight().getLeft()),
			antennaLocationPair.getRight().getRight() + (antennaLocationPair.getRight().getRight() - antennaLocationPair.getLeft().getRight())
		);
	}

	private static void saveAndMarkAntiNodeLocationIfWithinBounds(
		final Pair<Integer, Integer> antiNodeLocation,
		final int mapMaxX,
		final int mapMaxY,
		final Set<Pair<Integer, Integer>> uniqueAntiNodeLocations,
		final char[][] mapCharTable
	) {
		if (isAntiNodeWithinMapBounds(antiNodeLocation, mapMaxX, mapMaxY)) {
			uniqueAntiNodeLocations.add(antiNodeLocation);
			markAntiNodeOnMapIfLocationIsEmpty(mapCharTable, antiNodeLocation);
		}
	}

	private static boolean isAntiNodeWithinMapBounds(
		final Pair<Integer, Integer> antiNodeLocation,
		final int mapMaxX,
		final int mapMaxY
	) {
		return (antiNodeLocation.getLeft() >= 0 && antiNodeLocation.getLeft() <= mapMaxX)
			&& (antiNodeLocation.getRight() >= 0 && antiNodeLocation.getRight() <= mapMaxY);
	}

	private static void markAntiNodeOnMapIfLocationIsEmpty(
		final char[][] mapCharTable,
		final Pair<Integer, Integer> antiNodeLocation
	) {
		if (mapCharTable[antiNodeLocation.getRight()][antiNodeLocation.getLeft()] == CHAR_EMPTY) {
			mapCharTable[antiNodeLocation.getRight()][antiNodeLocation.getLeft()] = CHAR_ANTINODE;
		}
	}

	private static Set<Pair<Integer, Integer>> createAntiNodeLocationsRightConsideringResonantHarmonicEffect(
		final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> locationPair,
		final int mapMaxX,
		final int mapMaxY,
		final char[][] mapCharTable
	) {
		final Set<Pair<Integer, Integer>> antiNodeLocationsRightConsideringResonantHarmonicEffect = new LinkedHashSet<>();
		final Pair<Integer, Integer> newAntiNodeLocationRight = createAntiNodeLocationRight(locationPair);
		if (isAntiNodeWithinMapBounds(newAntiNodeLocationRight, mapMaxX, mapMaxY)) {
			antiNodeLocationsRightConsideringResonantHarmonicEffect.add(newAntiNodeLocationRight);
			markAntiNodeOnMapIfLocationIsEmpty(mapCharTable, newAntiNodeLocationRight);
			antiNodeLocationsRightConsideringResonantHarmonicEffect.addAll(createAntiNodeLocationsRightConsideringResonantHarmonicEffect(
				Pair.of(newAntiNodeLocationRight, locationPair.getLeft()),
				mapMaxX,
				mapMaxY,
				mapCharTable
			));
		}
		return antiNodeLocationsRightConsideringResonantHarmonicEffect;
	}

	private static Set<Pair<Integer, Integer>> createAntiNodeLocationsLeftConsideringResonantHarmonicEffect(
		final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> locationPair,
		final int mapMaxX,
		final int mapMaxY,
		final char[][] mapCharTable
	) {
		final Set<Pair<Integer, Integer>> antiNodeLocationsLeftConsideringResonantHarmonicEffect = new LinkedHashSet<>();
		final Pair<Integer, Integer> newAntiNodeLocationLeft = createAntiNodeLocationLeft(locationPair);
		if (isAntiNodeWithinMapBounds(newAntiNodeLocationLeft, mapMaxX, mapMaxY)) {
			antiNodeLocationsLeftConsideringResonantHarmonicEffect.add(newAntiNodeLocationLeft);
			markAntiNodeOnMapIfLocationIsEmpty(mapCharTable, newAntiNodeLocationLeft);
			antiNodeLocationsLeftConsideringResonantHarmonicEffect.addAll(createAntiNodeLocationsLeftConsideringResonantHarmonicEffect(
				Pair.of(locationPair.getRight(), newAntiNodeLocationLeft),
				mapMaxX,
				mapMaxY,
				mapCharTable
			));
		}
		return antiNodeLocationsLeftConsideringResonantHarmonicEffect;
	}
}
