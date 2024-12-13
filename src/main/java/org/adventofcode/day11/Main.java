package org.adventofcode.day11;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it into a List of String and Long
		final List<String> stones = new ArrayList<>();
		final List<Long> stonesPart2 = new ArrayList<>();
		for (final String line : inputByLines) {
			stones.addAll(
				Arrays.stream(line.split(" "))
					.toList()
			);
			stonesPart2.addAll(
				Arrays.stream(line.split(" "))
					.map(Long::parseLong)
					.toList()
			);
		}

		// blink 25 times and count how many stones we have in the end considering the 3 rules
		final int blinkCount = 25;
		blink(stones, blinkCount);
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.printf("number of stones after blinking %d times = %d", blinkCount, stones.size());
		System.out.println();
		System.out.println();

		/*
		part 2: blink 75 times and count how many stones we have in the end considering the 3 rules
		My part 1 brute force solution starts to slow down exponentially as the number of stones are increasing
		exponentially (around after blink number 30).
		So the first optimization was to use a recursive method for walking through a tree (which we build up as we
		walk) from root node (each starting stone from the input is a root with a different tree) to every leaf node.
		This was still slow because the trees are huge with 75 depth and visiting every node is too much.
		The second optimization was to use a cache where the key is a pair of stone number + the blink number that stone
		still needs, and the value is how many leaf node that stone with blink number will have. We put every node in
		the cache which has every of its leaf node visited. And the cache is checked every time a node needs to go down
		in the tree.
		 */
		final long start2 = System.currentTimeMillis();
		final Map<StoneNumberBlinkCounterPair, Long> stoneBlinkPairCache = new HashMap<>();
		final int blinkCountPart2 = 75;
		long totalLeafStoneCounter = 0;
		for (final Long stoneNumber : stonesPart2) {
			final StoneNumberBlinkCounterPair rootNodeStone = new StoneNumberBlinkCounterPair(stoneNumber, blinkCountPart2);
			totalLeafStoneCounter += blinkForOneStone(rootNodeStone, stoneBlinkPairCache);
		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.printf("part2: number of stones after blinking %d times = %d", blinkCountPart2, totalLeafStoneCounter);
		System.out.println();
	}

	private static void blink(final List<String> stones, final int blinkCount) {
		long start;
		for (int i = 0; i < blinkCount; i++) {
			start = System.currentTimeMillis();
			int newStonesCreatedCounter = 0;
			for (int j = 0; j < stones.size() - newStonesCreatedCounter; j++) {
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
					// we can put the new stone to the end of the list because order does not matter, plus we don't have to shift the entire remaining arraylist
					stones.add(newRightStone);
					newStonesCreatedCounter++;
				} else {
					stones.set(j, String.valueOf(stoneNumber * 2024));
				}
			}
			System.out.printf("the %d. blinkCount took %d milliseconds. current amount of stones: %d%s", i, System.currentTimeMillis() - start, stones.size(), System.lineSeparator());
//			printStones(stones);
		}
	}

	private static long blinkForOneStone(
		final StoneNumberBlinkCounterPair currentStoneAndBlink,
		final Map<StoneNumberBlinkCounterPair, Long> stoneBlinkPairCache
	) {
		if (currentStoneAndBlink.blinkCount() == 0) {
			return 1;
		}
		if (stoneBlinkPairCache.containsKey(currentStoneAndBlink)) {
			return stoneBlinkPairCache.get(currentStoneAndBlink);
		}
		final int decreasedBlink = currentStoneAndBlink.blinkCount() - 1;
		final long stoneNumber = currentStoneAndBlink.stoneNumber();
		if (stoneNumber == 0) {
			final StoneNumberBlinkCounterPair stoneWithNumberOnePair = new StoneNumberBlinkCounterPair(1, decreasedBlink);
			final long childrenStoneCount = blinkForOneStone(stoneWithNumberOnePair, stoneBlinkPairCache);
			stoneBlinkPairCache.put(stoneWithNumberOnePair, childrenStoneCount);
			return childrenStoneCount;
		} else if (isLengthOfNumberEven(stoneNumber)) {
			final String stoneString = String.valueOf(stoneNumber);
			final StoneNumberBlinkCounterPair leftChildStonePair = new StoneNumberBlinkCounterPair(
				Long.parseLong(stoneString.substring(0, stoneString.length() / 2)),
				decreasedBlink
			);
			final long leftChildrenStoneCount = blinkForOneStone(leftChildStonePair, stoneBlinkPairCache);
			stoneBlinkPairCache.put(leftChildStonePair, leftChildrenStoneCount);
			final StoneNumberBlinkCounterPair rightChildStonePair = new StoneNumberBlinkCounterPair(
				Long.parseLong(stoneString.substring(stoneString.length() / 2)),
				decreasedBlink
			);
			final long rightChildrenStoneCount = blinkForOneStone(rightChildStonePair, stoneBlinkPairCache);
			stoneBlinkPairCache.put(rightChildStonePair, rightChildrenStoneCount);
			return leftChildrenStoneCount + rightChildrenStoneCount;
		} else {
			final StoneNumberBlinkCounterPair stoneWithNumberMultipliedPair = new StoneNumberBlinkCounterPair(stoneNumber * 2024, decreasedBlink);
			final long childrenStoneCount = blinkForOneStone(stoneWithNumberMultipliedPair, stoneBlinkPairCache);
			stoneBlinkPairCache.put(stoneWithNumberMultipliedPair, childrenStoneCount);
			return childrenStoneCount;
		}
	}

	private static boolean isLengthOfNumberEven(final long number) {
		return String.valueOf(number).length() % 2 == 0;
	}

	/**
	 * @see #isLengthOfNumberEven(long)
	 */
	private static boolean isLengthOfNumberEvenWithoutStringParse(final long number) {
		double decreasingNumber = number;
		boolean even = true;
		while (decreasingNumber >= 1.0) {
			decreasingNumber /= 10.0;
			even = !even;
		}
		return even;
	}

	private static void printStones(final List<String> stones) {
		System.out.println(String.join(" ", stones));
	}

	private static String cutLeadingZerosFromNumber(final String number) {
		return String.valueOf(Long.valueOf(number));
	}

	private record StoneNumberBlinkCounterPair(long stoneNumber, int blinkCount) {}
}
