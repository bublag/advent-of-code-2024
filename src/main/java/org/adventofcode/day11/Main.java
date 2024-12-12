package org.adventofcode.day11;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it into a List of String
		final List<String> stones = new ArrayList<>();
		for (final String line : inputByLines) {
			stones.addAll(
				Arrays.stream(line.split(" "))
					.toList()
			);
		}

		// blink 25 times and count how many stones we have in the end
		final int blinkCount = 25;
		for (int i = 0; i < blinkCount; i++) {
			for (int j = 0; j < stones.size(); j++) {
				final String stoneString = stones.get(j);
				final long stoneNumber = Long.parseLong(stoneString);
				if (stoneNumber == 0) {
					stones.set(j, "1");
				} else if (stoneString.length() % 2 == 0) {
					final String newLeftStone = stoneString.substring(0, stoneString.length() / 2);
					final String newRightStone = cutLeadingZerosFromNumber(
						stoneString.substring(stoneString.length() / 2)
					);
					stones.set(j, newLeftStone);
					stones.add(j + 1, newRightStone);
					j++; // skip 'newRightStone' in the iteration
				} else {
					stones.set(j, String.valueOf(stoneNumber * 2024));
				}
			}
//			printStones(stones);
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.printf("number of stones after blinking %d times = %d", blinkCount, stones.size());
		System.out.println();
	}

	private static void printStones(final List<String> stones) {
		System.out.println(String.join(" ", stones));
	}

	private static String cutLeadingZerosFromNumber(final String number) {
		return String.valueOf(Long.valueOf(number));
	}
}
