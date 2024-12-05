package org.adventofcode.day01;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// put the content of the file into 2 Integer list
		final List<Integer> leftList = new ArrayList<>();
		final List<Integer> rightList = new ArrayList<>();
		inputByLines.forEach(line -> {
			final String[] lineSplit = line.split("   ");
			leftList.add(Integer.parseInt(lineSplit[0]));
			rightList.add(Integer.parseInt(lineSplit[1]));
		});

		// sort the 2 lists
		Collections.sort(leftList);
		Collections.sort(rightList);

		// calculate the diff (=distance) of every pair in the 2 lists and add those diffs together
		long totalDistance = 0;
		for (int i = 0; i < leftList.size(); i++) {
			totalDistance += Math.abs(leftList.get(i) - rightList.get(i));
		}
		System.out.println("totalDistance: " + totalDistance);

		// part 2: calculate the "similarity score". +optimization: use a Map as cache for already calculated numbers
		long totalSimilarityScore = 0;
		final Map<Integer, Long> similarityCacheMap = new HashMap<>();
		for (final Integer leftNumber : leftList) {
			if (!similarityCacheMap.containsKey(leftNumber)) {
				int occurrenceCounter = 0;
				for (final Integer rightNumber : rightList) {
					if (Objects.equals(leftNumber, rightNumber)) {
						occurrenceCounter++;
					}
				}
				final long similarityScore = (long) leftNumber * occurrenceCounter;
				similarityCacheMap.put(leftNumber, similarityScore);
			}
			totalSimilarityScore += similarityCacheMap.get(leftNumber);
		}
		System.out.println("totalSimilarityScore = " + totalSimilarityScore);
	}
}
