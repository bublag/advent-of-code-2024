package org.adventofcode.day09;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//--- Day 9: Disk Fragmenter ---
@UtilityClass
public class Main {

	private static final char CHAR_EMPTY = '.';

	public static void main(final String[] args) throws IOException {
		// read the file
		final List<String> inputByLines = FileUtils.readLines(
			new File(Main.class.getResource("input.txt").getFile()),
			StandardCharsets.UTF_8
		);

		final long start = System.currentTimeMillis();
		// convert the "disk map"'s dense format (input) into a layout that shows the files and free spaces on the disk
		final List<DiskBlock> diskPart1 = new ArrayList<>();
		int fileIdCounter = 0;
		DiskBlockType alternatingDiskBlockType = DiskBlockType.FILE;
		for (final String inputByLine : inputByLines) {
			for (final char digitChar : inputByLine.toCharArray()) {
				final int digitInt = Character.getNumericValue(digitChar);
				switch (alternatingDiskBlockType) {
					case FILE -> {
						for (int i = 0; i < digitInt; i++) {
							diskPart1.add(new DiskBlock(DiskBlockType.FILE, fileIdCounter));
						}
						fileIdCounter++;
					}
					case FREE_SPACE -> {
						if (digitInt > 0) {
							for (int i = 0; i < digitInt; i++) {
								diskPart1.add(new DiskBlock(DiskBlockType.FREE_SPACE, null));
							}
						}
					}
					case null, default -> throw new IllegalStateException("Unexpected value: " + alternatingDiskBlockType);
				}
				alternatingDiskBlockType = alternatingDiskBlockType.alternate();
			}
		}
		System.out.println("disk1 before de-fragmentation:");
		printDiskPart1(diskPart1);

		// de-fragment the disk by moving 1 file at a time. moving the last file on the disk to the first empty space on the disk
		for (int i = diskPart1.size() - 1; i >= 0; i--) {
			final DiskBlock fileBlockToCheckAndMove = diskPart1.get(i);
			if (fileBlockToCheckAndMove.diskBlockType() == DiskBlockType.FREE_SPACE) {
				continue;
			}
			final int firstFreeSpacePosition = findFirstFreeSpace(diskPart1, i);
			if (firstFreeSpacePosition != -1) {
				final DiskBlock firstFreeDiskBlock = diskPart1.get(firstFreeSpacePosition);
				// simply swap the file block with the free block
				diskPart1.set(firstFreeSpacePosition, fileBlockToCheckAndMove);
				diskPart1.set(i, firstFreeDiskBlock);
			}
		}
		System.out.println("disk1 after de-fragmentation:");
		printDiskPart1(diskPart1);

		// calculate the filesystem checksum: add up the result of multiplying each of the blocks' position with the file ID number it contains
		long checksum = 0;
		for (int i = 0; i < diskPart1.size(); i++) {
			final DiskBlock diskBlock = diskPart1.get(i);
			if (diskBlock.diskBlockType() == DiskBlockType.FILE) {
				checksum += (long) i * (long) diskBlock.fileId();
			}
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("part1 checksum = " + checksum);
		System.out.println();

		// part 2: de-fragment the disk by moving whole files. moving the last file on the disk to the first empty space on the disk where the file fits
		// re-read the input because we are now using 'DiskBlockWithLength' instead
		final long start2 = System.currentTimeMillis();
		final List<DiskBlockWithLength> diskPart2 = new ArrayList<>();
		fileIdCounter = 0;
		alternatingDiskBlockType = DiskBlockType.FILE;
		for (final String inputByLine : inputByLines) {
			for (final char digitChar : inputByLine.toCharArray()) {
				final int digitInt = Character.getNumericValue(digitChar);
				switch (alternatingDiskBlockType) {
					case FILE -> {
						diskPart2.add(new DiskBlockWithLength(DiskBlockType.FILE, digitInt, fileIdCounter));
						fileIdCounter++;
					}
					case FREE_SPACE -> {
						if (digitInt > 0) {
							diskPart2.add(new DiskBlockWithLength(DiskBlockType.FREE_SPACE, digitInt, null));
						}
					}
					case null, default -> throw new IllegalStateException("Unexpected value: " + alternatingDiskBlockType);
				}
				alternatingDiskBlockType = alternatingDiskBlockType.alternate();
			}
		}
		System.out.println("disk2 before de-fragmentation:");
		printDiskPart2(diskPart2);

		// de-fragment
		for (int i = diskPart2.size() - 1; i >= 0; i--) {
			final DiskBlockWithLength wholeFileToCheckAndMove = diskPart2.get(i);
			if (wholeFileToCheckAndMove.diskBlockType() == DiskBlockType.FREE_SPACE) {
				continue;
			}
			final int firstFreeSpacePosition = findFirstFreeSpacePart2(diskPart2, wholeFileToCheckAndMove.length(), i);
			if (firstFreeSpacePosition != -1) {
				final DiskBlockWithLength firstFreeDiskBlock = diskPart2.get(firstFreeSpacePosition);
				final int remainingFreeSpaceAtFirstFreeSpacePosition = firstFreeDiskBlock.length() - wholeFileToCheckAndMove.length();
				if (remainingFreeSpaceAtFirstFreeSpacePosition == 0) { // simply swap the file block with the free block, because their length are the same
					diskPart2.set(firstFreeSpacePosition, wholeFileToCheckAndMove);
					diskPart2.set(i, firstFreeDiskBlock);
				} else { // swap and create a smaller free disk space for the remaining free size right after where we just put the file block
					diskPart2.set(firstFreeSpacePosition, wholeFileToCheckAndMove);
					diskPart2.set(i, new DiskBlockWithLength(DiskBlockType.FREE_SPACE, wholeFileToCheckAndMove.length(), null));
					diskPart2.add(firstFreeSpacePosition + 1, new DiskBlockWithLength(DiskBlockType.FREE_SPACE, remainingFreeSpaceAtFirstFreeSpacePosition, null));
					// we just shifted every element in the list and made it's size 1 bigger so we have to adjust the iterator variable
					i++;
				}
			}
		}
		System.out.println("disk2 after de-fragmentation:");
		printDiskPart2(diskPart2);

		// calculate the checksum
		int blockPosition = 0;
		checksum = 0;
		for (final DiskBlockWithLength diskBlockWithLength : diskPart2) {
			switch (diskBlockWithLength.diskBlockType()) {
				case FILE -> {
					for (int j = 0; j < diskBlockWithLength.length(); j++) {
						checksum += (long) blockPosition * (long) diskBlockWithLength.fileId();
						blockPosition++;
					}
				}
				case FREE_SPACE -> blockPosition += diskBlockWithLength.length();
				case null, default -> throw new IllegalStateException("Unexpected value: " + diskBlockWithLength.diskBlockType());
			}

		}
		System.out.println("part2 solution runtime in milliseconds = " + (System.currentTimeMillis() - start2));
		System.out.println("part2 checksum = " + checksum);
	}

	private static void printDiskPart1(final List<DiskBlock> disk) {
		for (final DiskBlock diskBlock : disk) {
			switch (diskBlock.diskBlockType()) {
				case FILE -> System.out.print(diskBlock.fileId());
				case FREE_SPACE -> System.out.print(CHAR_EMPTY);
				case null, default -> throw new IllegalStateException("Unexpected value: " + diskBlock.diskBlockType());
			}
		}
		System.out.println();
	}

	private static int findFirstFreeSpace(final List<DiskBlock> disk, final int lastPossibleFreeSpacePosition) {
		for (int i = 0; i < lastPossibleFreeSpacePosition; i++) {
			if (disk.get(i).diskBlockType() == DiskBlockType.FREE_SPACE) {
				return i;
			}
		}
		return -1;
	}

	private static void printDiskPart2(final List<DiskBlockWithLength> disk) {
		for (final DiskBlockWithLength diskBlockWithLength : disk) {
			for (int i = 0; i < diskBlockWithLength.length(); i++) {
				switch (diskBlockWithLength.diskBlockType()) {
					case FILE -> System.out.print(diskBlockWithLength.fileId());
					case FREE_SPACE -> System.out.print(CHAR_EMPTY);
					case null, default -> throw new IllegalStateException("Unexpected value: " + diskBlockWithLength.diskBlockType());
				}
			}
		}
		System.out.println();
	}

	private static int findFirstFreeSpacePart2(final List<DiskBlockWithLength> disk, final int neededFreeSpaceSize, final int lastPossibleFreeSpacePosition) {
		for (int i = 0; i < lastPossibleFreeSpacePosition; i++) {
			if (disk.get(i).diskBlockType() == DiskBlockType.FREE_SPACE
				&& disk.get(i).length() >= neededFreeSpaceSize
			) {
				return i;
			}
		}
		return -1;
	}

	private record DiskBlock(DiskBlockType diskBlockType, Integer fileId) {}

	private record DiskBlockWithLength(DiskBlockType diskBlockType, int length, Integer fileId) {}

	private enum DiskBlockType {
		FILE, FREE_SPACE;

		public DiskBlockType alternate() {
			return switch (this) {
				case FILE -> FREE_SPACE;
				case FREE_SPACE -> FILE;
			};
		}
	}
}
