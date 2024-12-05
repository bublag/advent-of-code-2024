package org.adventofcode.day02;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// put the content of the file into a list (1000 size), which consists of around 5-8 Integers
		final List<List<Integer>> input = new ArrayList<>(1000);
		inputByLines.forEach(line -> {
			final List<Integer> numbersInTheLine = new ArrayList<>(8);
			final String[] lineSplit = line.split(" ");
			for (final String numberInTheLine : lineSplit) {
				numbersInTheLine.add(Integer.parseInt(numberInTheLine));
			}
			input.add(numbersInTheLine);
		});

		/*
		Count how many reports (lines) are safe (the numbers inside are called levels). A report is safe if:
		The levels are either all increasing or all decreasing
		AND
		Any two adjacent levels differ by at least one and at most three.
		 */
		int safeReportCounter = 0;
		for (final List<Integer> report : input) {
			if (isSafeReport(report)) {
				safeReportCounter++;
			}
		}
		System.out.println("safeReportCounter = " + safeReportCounter);

		/*
		part 2: calculate the safe reports considering the "Problem Dampener"
		"if removing a single level from an unsafe report would make it safe, the report instead counts as safe"
		TODO the 2 iteration over 'input' could be 1 instead, but it is just a 1000 entry and I want to separate better the 2 parts of the puzzle instead
		 */
		int safeReportCounterWithProblemDampener = 0;
		for (final List<Integer> report : input) {
			if (isSafeReport(report)) {
				safeReportCounterWithProblemDampener++;
			} else {
				// check if the report is safe if we remove 1 element from it (try until it is considered safe or until we tried with every level)
				//TODO probably less tries would be enough to test if the report would be safe, but I am leaving this "unoptimised" solution since there are so few levels in 1 report
				for (int i = 0; i < report.size(); i++) {
					final List<Integer> oneLevelRemovedReport = new ArrayList<>(report);
					oneLevelRemovedReport.remove(i);
					if (isSafeReport(oneLevelRemovedReport)) {
						safeReportCounterWithProblemDampener++;
						break;
					}
				}
			}
		}
		System.out.println("safeReportCounterWithProblemDampener = " + safeReportCounterWithProblemDampener);
	}

	private static boolean isSafeReport(final List<Integer> report) {
		final boolean isLevelsIncreasing = report.get(1) > report.getFirst();
		for (int i = 0; i < report.size() - 1; i++) { // because we always look 1 element further
			final Integer currentLevel = report.get(i);
			final Integer nextLevel = report.get(i + 1);
			if (isLevelsIncreasing && currentLevel >= nextLevel) {
				break; // levels decrease but should increase -> unsafe
			}
			if (!isLevelsIncreasing && currentLevel < nextLevel) {
				break; // levels increase but should decrease -> unsafe
			}
			final int levelDifference = Math.abs(currentLevel - nextLevel);
			if (levelDifference < 1 || levelDifference > 3) {
				break; // levels difference are too small (0) or too big (more than 3) -> unsafe
			}
			if (i == report.size() - 2) {
				return true; // we did not stop the iteration of the levels and reached the end -> this report is safe
			}
		}
		return false;
	}
}
