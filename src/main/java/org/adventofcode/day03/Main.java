package org.adventofcode.day03;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//--- Day 3: Mull It Over ---
@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// go over the input line by line and collect the valid "mul" instructions
		// multiply the valid "mul" instructions and add them all up
		final String mulRegex = "mul\\(\\d{1,3},\\d{1,3}\\)"; // targets "mul(X,Y)", where X and Y are each 1-3 digit numbers
		final Pattern mulPattern = Pattern.compile(mulRegex);
		long sumOfAllMul = 0;
		for (final String inputByLine : inputByLines) {
			final Matcher mulMatcher = mulPattern.matcher(inputByLine);
			while (mulMatcher.find()) {
				sumOfAllMul += getMulResult(mulMatcher.group());
			}
		}
		System.out.println("sumOfAllMul = " + sumOfAllMul);

		// part 2: also consider "do()" and "don't()" "instructions" before "mul" instructions
		final String doRegex = "do\\(\\)";
		final String dontRegex = "don't\\(\\)";
		final String mulOrDoOrDontRegex = String.format("%s|%s|%s", mulRegex, doRegex, dontRegex); // targets all 3 instructions (mul/do/dont)
		final Pattern mulOrDoOrDontPattern = Pattern.compile(mulOrDoOrDontRegex);
		boolean instructionsEnabled = true;
		long sumOfAllEnabledMul = 0;
		for (final String inputByLine : inputByLines) {
			final Matcher mulOrDoOrDontPatternMatcher = mulOrDoOrDontPattern.matcher(inputByLine);
			while (mulOrDoOrDontPatternMatcher.find()) {
				final String match = mulOrDoOrDontPatternMatcher.group();
				if (match.equals("do()")) {
					instructionsEnabled = true;
				} else if (match.equals("don't()")) {
					instructionsEnabled = false;
				} else if (match.matches(mulRegex)) {
					if (instructionsEnabled) {
						sumOfAllEnabledMul += getMulResult(match);
					}
				} else {
					throw new IllegalArgumentException("Unknown regex match: " + match);
				}
			}
		}
		System.out.println("sumOfAllEnabledMul = " + sumOfAllEnabledMul);
	}

	// 'mulString' example: "mul(382,128)"
	private static long getMulResult(final String mulString) {
		final String twoNumbersSeparatedWithComma = mulString.substring("mul(".length(), mulString.length() - 1);
		final String[] split = twoNumbersSeparatedWithComma.split(",");
		return Long.parseLong(split[0]) * Long.parseLong(split[1]);
	}
}
