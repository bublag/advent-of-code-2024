package org.adventofcode.day07;

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

	public static final String OPERATOR_MULTIPLY = "*";
	public static final String OPERATOR_ADD = "+";
	// not using "||", because I can call 'java.lang.String.substring(int, int)' easier if all operator is 1 length long
	public static final String OPERATOR_CONCATENATION = "|";

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		// convert it into CalibrationEquation objects
		final List<CalibrationEquation> calibrationEquations = new ArrayList<>();
		for (final String inputByLine : inputByLines) {
			final String[] splitByColon = inputByLine.split(":");
			final String[] numbers = splitByColon[1].trim().split(" ");
			calibrationEquations.add(new CalibrationEquation(
				Long.parseLong(splitByColon[0]),
				Arrays.stream(numbers)
					.map(Integer::parseInt)
					.toList()
			));
		}

		final long start = System.currentTimeMillis();
		// building up a map of the operators variations by the length (length == number of needed operators)
		final Map<Integer, List<String>> twoOperatorsVariationsMap = new HashMap<>();
		long sumOfValidEquationsTestValues = 0;
		for (final CalibrationEquation calibrationEquation : calibrationEquations) {
			final int neededOperatorsNumber = calibrationEquation.numbers().size() - 1;
			final List<String> operatorVariations = twoOperatorsVariationsMap.computeIfAbsent(
				neededOperatorsNumber,
				k -> createVariationsForTwoOperators(neededOperatorsNumber)
			);
			for (final String operatorVariation : operatorVariations) {
				final long resultForVariation = evaluateLeftToRightTwoOperators(calibrationEquation.numbers(), operatorVariation);
				if (resultForVariation == calibrationEquation.testValue()) {
					sumOfValidEquationsTestValues += resultForVariation;
					break;
				}
			}
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("sumOfValidEquationsTestValues = " + sumOfValidEquationsTestValues);

		// part 2: a new operator, concatenation also has to be considered in the operator variations
		final long start2 = System.currentTimeMillis();
		// building up a map of the operators variations by the length (length == number of needed operators)
		final Map<Integer, List<String>> threeOperatorsVariationsMap = new HashMap<>();
		long sumOfValidEquationsTestValuesPart2 = 0;
		for (final CalibrationEquation calibrationEquation : calibrationEquations) {
			final int neededOperatorsNumber = calibrationEquation.numbers().size() - 1;
			final List<String> operatorVariations = threeOperatorsVariationsMap.computeIfAbsent(
				neededOperatorsNumber,
				k -> createVariationsForThreeOperators(neededOperatorsNumber)
			);
			for (final String operatorVariation : operatorVariations) {
				final long resultForVariation = evaluateLeftToRightThreeOperators(calibrationEquation.numbers(), operatorVariation);
				if (resultForVariation == calibrationEquation.testValue()) {
					sumOfValidEquationsTestValuesPart2 += resultForVariation;
					break;
				}
			}
		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.println("sumOfValidEquationsTestValuesPart2 = " + sumOfValidEquationsTestValuesPart2);
	}

	/**
	 * stringLength == 1: [*, +]
	 * stringLength == 2: [**, *+, +*, ++]
	 * stringLength == 3: [***, **+, *+*, *++, +**, +*+, ++*, +++]
	 * stringLength == 4: [****, ***+, **+*, **++, *+**, *+*+, *++*, *+++, +***, +**+, +*+*, +*++, ++**, ++*+, +++*, ++++]
	 */
	public static List<String> createVariationsForTwoOperators(final int stringLength) {
		if (stringLength == 1) {
			return List.of(OPERATOR_MULTIPLY, OPERATOR_ADD);
		} else {
			final List<String> variations = createVariationsForTwoOperators(stringLength - 1);
			final List<String> newVariations = new ArrayList<>(2);
			for (final String variation : variations) {
				newVariations.add(variation + OPERATOR_MULTIPLY);
				newVariations.add(variation + OPERATOR_ADD);
			}
			return newVariations;
		}
	}

	/**
	 * @see #createVariationsForTwoOperators(int)
	 */
	public static List<String> createVariationsForThreeOperators(final int stringLength) {
		if (stringLength == 1) {
			return List.of(OPERATOR_MULTIPLY, OPERATOR_ADD, OPERATOR_CONCATENATION);
		} else {
			final List<String> variations = createVariationsForThreeOperators(stringLength - 1);
			final List<String> newVariations = new ArrayList<>(3);
			for (final String variation : variations) {
				newVariations.add(variation + OPERATOR_MULTIPLY);
				newVariations.add(variation + OPERATOR_ADD);
				newVariations.add(variation + OPERATOR_CONCATENATION);
			}
			return newVariations;
		}
	}

	private static long evaluateLeftToRightTwoOperators(final List<Integer> numbers, final String operatorVariation) {
		long result = numbers.getFirst();
		for (int i = 1; i < numbers.size(); i++) {
			final String currentOperator = operatorVariation.substring(i - 1, i);
			switch (currentOperator) {
				case OPERATOR_MULTIPLY:
					result *= numbers.get(i);
					break;
				case OPERATOR_ADD:
					result += numbers.get(i);
					break;
				default:
					throw new IllegalStateException("Unexpected currentOperator: " + currentOperator);
			}
		}
		return result;
	}

	private static long evaluateLeftToRightThreeOperators(final List<Integer> numbers, final String operatorVariation) {
		long result = numbers.getFirst();
		for (int i = 1; i < numbers.size(); i++) {
			final String currentOperator = operatorVariation.substring(i - 1, i);
			switch (currentOperator) {
				case OPERATOR_MULTIPLY:
					result *= numbers.get(i);
					break;
				case OPERATOR_ADD:
					result += numbers.get(i);
					break;
				case OPERATOR_CONCATENATION:
					result = Long.parseLong(String.valueOf(result) + numbers.get(i));
					break;
				default:
					throw new IllegalStateException("Unexpected currentOperator: " + currentOperator);
			}
		}
		return result;
	}

	private record CalibrationEquation(Long testValue, List<Integer> numbers) {}
}
