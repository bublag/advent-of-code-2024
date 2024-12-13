package org.adventofcode.day05;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//--- Day 5: Print Queue ---
@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// saving the first section, "page ordering rules" into a Map
		// saving the second section, "page numbers of each update" to a List
		final Map<Integer, Set<Integer>> pageOrderingRules = new HashMap<>();
		final List<List<Integer>> updates = new ArrayList<>();
		for (final String inputByLine : inputByLines) {
			if (inputByLine.contains("|")) {
				final String[] split = inputByLine.split("\\|");
				final int leftNumber = Integer.parseInt(split[0]);
				final int rightNumber = Integer.parseInt(split[1]);
				if (pageOrderingRules.containsKey(leftNumber)) {
					pageOrderingRules.get(leftNumber)
						.add(rightNumber);
				} else {
					final Set<Integer> rulesForLeftNumber = new HashSet<>();
					rulesForLeftNumber.add(rightNumber);
					pageOrderingRules.put(leftNumber, rulesForLeftNumber);
				}
			}
			if (inputByLine.contains(",")) {
				updates.add(
					Arrays.stream(inputByLine.split(","))
						.map(Integer::parseInt)
						.toList()
				);
			}
		}

		/*
		Go over the updates and consider them if they are valid (the order of the numbers inside them are valid
		considering the page ordering rules).
		If an update is valid, get the number in the middle and sum all those up.
		 */
		long validUpdatesMiddleNumbersSum = 0;
		final List<List<Integer>> incorrectlyOrderedUpdates = new ArrayList<>();
		for (final List<Integer> update : updates) {
			boolean isUpdateValid = true;
			for (int i = 1; i < update.size(); i++) { // no need to validate the first number
				final Integer currentNumberToValidate = update.get(i);
				final Set<Integer> rulesForCurrentNumber = SetUtils.emptyIfNull(pageOrderingRules.get(currentNumberToValidate));
				// go back in the list of numbers and check if there is any number before this which cant be based on the rules
				boolean isNumberValid = true;
				for (int j = i - 1; j >= 0 ; j--) {
					final Integer previousNumber = update.get(j);
					if (rulesForCurrentNumber.contains(previousNumber)) {
						isNumberValid = false;
						break;
					}
				}
				if (!isNumberValid) {
					isUpdateValid = false;
					break;
				}
			}
			if (isUpdateValid) {
				validUpdatesMiddleNumbersSum += getMiddleNumber(update);
			} else {
				incorrectlyOrderedUpdates.add(new ArrayList<>(update));
			}
		}
		System.out.println("validUpdatesMiddleNumbersSum = " + validUpdatesMiddleNumbersSum);

		// part 2: correctly order the incorrect rules with the page ordering rules
		long correctedUpdatesMiddleNumbersSum = 0;
		for (final List<Integer> incorrectlyOrderedUpdate : incorrectlyOrderedUpdates) {
			for (int i = 1; i < incorrectlyOrderedUpdate.size(); i++) { // cant move the first number more back
				final Integer currentNumberToCheckAndPossiblyMoveBack = incorrectlyOrderedUpdate.get(i);
				int currentPositionOfCurrentNumberToCheckAndPossiblyMoveBack = i;
				final Set<Integer> rulesForCurrentNumber = SetUtils.emptyIfNull(pageOrderingRules.get(currentNumberToCheckAndPossiblyMoveBack));
				// go back in the list of numbers and swap positions if needed based on the page ordering rules
				for (int j = i - 1; j >= 0 ; j--) {
					final Integer previousNumber = incorrectlyOrderedUpdate.get(j);
					if (rulesForCurrentNumber.contains(previousNumber)) { // swap positions in the list
						incorrectlyOrderedUpdate.set(currentPositionOfCurrentNumberToCheckAndPossiblyMoveBack, previousNumber);
						incorrectlyOrderedUpdate.set(j, currentNumberToCheckAndPossiblyMoveBack);
						currentPositionOfCurrentNumberToCheckAndPossiblyMoveBack = j;
					}
				}
			}
			// after the above iteration, 'incorrectlyOrderedUpdate' is now correctly ordered
			correctedUpdatesMiddleNumbersSum += getMiddleNumber(incorrectlyOrderedUpdate);
		}
		System.out.println("correctedUpdatesMiddleNumbersSum = " + correctedUpdatesMiddleNumbersSum);
	}

	private static int getMiddleNumber(final List<Integer> update) {
		if (update.size() % 2 == 0) {
			throw new UnsupportedOperationException("The size of the update is even. Can't pick the middle number if there are 2 middle numbers. The update: " + update);
		} else {
		    return update.get((update.size() / 2));
		}
	}
}
