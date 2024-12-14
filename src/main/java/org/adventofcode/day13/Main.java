package org.adventofcode.day13;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//--- Day 13: Claw Contraption ---
@UtilityClass
public class Main {

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert it claw machines
		final List<ClawMachine> clawMachines = new ArrayList<>();
		for (int i = 0; i < inputByLines.size(); i += 4) {
			final String lineButtonA = inputByLines.get(i);
			final String lineButtonB = inputByLines.get(i + 1);
			final String linePrize = inputByLines.get(i + 2);
			clawMachines.add(new ClawMachine(
				convertButtonLine(lineButtonA),
				convertButtonLine(lineButtonB),
				convertPrizeLine(linePrize)
			));
		}
		System.out.println("total number of claw machines from input: " + clawMachines.size());
//		clawMachines.forEach(System.out::println);
//		System.out.println();

		/*
		Pre-filter: filter out those claw machines, which is not winnable because to reach their 'x' or 'y' coordinates,
		more than 200 button press would be needed. Only need to checking the button press for 'x' and 'y' which is
		bigger from the 2 buttons (A and B).
		 */
		clawMachines.removeIf(clawMachine -> {
			final int higherX = Math.max(clawMachine.buttonA().x(), clawMachine.buttonB().x());
			final int lowerX = Math.min(clawMachine.buttonA().x(), clawMachine.buttonB().x());
			final int higherY = Math.max(clawMachine.buttonA().y(), clawMachine.buttonB().y());
			final int lowerY = Math.max(clawMachine.buttonA().y(), clawMachine.buttonB().y());
			return clawMachine.prize().x() > higherX * 100 + lowerX * 100
				|| clawMachine.prize().y() > higherY * 100 + lowerY * 100;
		});
		System.out.println("total number of claw machines after pre-filtering: " + clawMachines.size());
//		clawMachines.forEach(System.out::println);
		System.out.println();

		// go over the claw machines and find the cheapest way to win the game (pressing button A costs 3, while pressing button B costs only 1 token)
		final List<ClawMachineWinnable> clawMachinesWinnables = new ArrayList<>();
		for (final ClawMachine clawMachine : clawMachines) {
			final ClawMachineWinnable clawMachineWinnable = findCheapestWayToWin(clawMachine);
			if (clawMachineWinnable != null) {
				clawMachinesWinnables.add(clawMachineWinnable);
			}
		}

		// count the total tokens needed to win on every claw machine
		final long fewestTokensToWinEveryWinnableClawMachine = clawMachinesWinnables.stream()
			.map(ClawMachineWinnable::minimumTokensToWin)
			.mapToLong(value -> value)
			.sum();
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("clawMachinesWinnables.size() = " + clawMachinesWinnables.size());
		System.out.println("fewestTokensToWinEveryWinnableClawMachine = " + fewestTokensToWinEveryWinnableClawMachine);
		System.out.println();
	}

	/**
	 * Using a minimum search to only save a winning combination of Button A and Button B presses where we use the
	 * fewest tokens. Pressing Button A costs 3 tokens, while pressing Button B costs only 1 token. To find a winning
	 * combination, we check if it is possible to get the prize number from the combinations of 2 numbers (some product
	 * of Button A and some other product of Button B).
	 */
	private static ClawMachineWinnable findCheapestWayToWin(final ClawMachine clawMachine) {
		ClawMachineWinnable clawMachineWinnable = null;
		int minTokensToWin = Integer.MAX_VALUE;
		final boolean isAbuttonsXhigher = clawMachine.buttonA().x() >= clawMachine.buttonB().x();
		final int higherXvalue = isAbuttonsXhigher ? clawMachine.buttonA().x() : clawMachine.buttonB().x();
		final int lowerXvalue = isAbuttonsXhigher ? clawMachine.buttonB().x() : clawMachine.buttonA().x();
		final int maxPossibleButtonPressFromHigherX = clawMachine.prize().x() / higherXvalue;
		for (int i = maxPossibleButtonPressFromHigherX; i >=0; i--) {
			final int distanceNeededToFillWithOtherButtonPresses = clawMachine.prize().x() - i * higherXvalue;
			if (distanceNeededToFillWithOtherButtonPresses % lowerXvalue == 0) {
				final int buttonPressesFromHigherX = i;
				final int buttonPressesFromLowerX = distanceNeededToFillWithOtherButtonPresses / lowerXvalue;
				if (buttonPressesFromHigherX > 100 || buttonPressesFromLowerX > 100) {
					continue;
				}
				final int yProduct1 = isAbuttonsXhigher ? buttonPressesFromHigherX * clawMachine.buttonA().y() : buttonPressesFromHigherX * clawMachine.buttonB().y();
				final int yProduct2 = isAbuttonsXhigher ? buttonPressesFromLowerX * clawMachine.buttonB().y() : buttonPressesFromLowerX * clawMachine.buttonA().y();
				if (yProduct1 + yProduct2 != clawMachine.prize().y()) {
					continue;
				}
				final int tokensNeededFromHigherXbuttonPresses = isAbuttonsXhigher ? buttonPressesFromHigherX * 3 : buttonPressesFromHigherX;
				final int tokensNeededFromLowerXbuttonPresses = isAbuttonsXhigher ? buttonPressesFromLowerX : buttonPressesFromLowerX * 3;
				final int neededTokensForThisWinningCombination = tokensNeededFromHigherXbuttonPresses + tokensNeededFromLowerXbuttonPresses;
				if (neededTokensForThisWinningCombination < minTokensToWin) {
					minTokensToWin = neededTokensForThisWinningCombination;
					clawMachineWinnable = new ClawMachineWinnable(
						new Button(clawMachine.buttonA().x(), clawMachine.buttonA().y()),
						new Button(clawMachine.buttonB().x(), clawMachine.buttonB().y()),
						new Prize(clawMachine.prize().x(), clawMachine.prize().y()),
						minTokensToWin,
						isAbuttonsXhigher ? buttonPressesFromHigherX : buttonPressesFromLowerX,
						isAbuttonsXhigher ? buttonPressesFromLowerX : buttonPressesFromHigherX
					);
				}
			}
		}
		return clawMachineWinnable;
	}

	private static Button convertButtonLine(final String lineButton) {
		final String[] split = lineButton.split(" ");
		return new Button(
			Integer.parseInt(split[2].substring(2, split[2].length() - 1)),
			Integer.parseInt(split[3].substring(2))
		);
	}

	private static Prize convertPrizeLine(final String linePrize) {
		final String[] split = linePrize.split(" ");
		return new Prize(
			Integer.parseInt(split[1].substring(2, split[1].length() - 1)),
			Integer.parseInt(split[2].substring(2))
		);
	}

	private record Button(int x, int y) {}

	private record Prize(int x, int y) {}

	private record ClawMachine(Button buttonA, Button buttonB, Prize prize) {}

	private record ClawMachineWinnable(
		Button buttonA,
		Button buttonB,
		Prize prize,
		int minimumTokensToWin,
		int aButtonPresses,
		int bButtonPresses
	) {}
}
