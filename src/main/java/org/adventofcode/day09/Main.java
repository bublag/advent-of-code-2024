package org.adventofcode.day09;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
		final List<DiskBlock> disk = new ArrayList<>();
		int fileIdCounter = 0;
		DiskBlockType alternatingDiskBlockType = DiskBlockType.FILE;
		for (final String inputByLine : inputByLines) {
			for (final char digitChar : inputByLine.toCharArray()) {
				final int digitInt = Character.getNumericValue(digitChar);
				switch (alternatingDiskBlockType) {
					case FILE -> {
						for (int i = 0; i < digitInt; i++) {
							disk.add(new DiskBlock(DiskBlockType.FILE, fileIdCounter));
						}
						fileIdCounter++;
					}
					case FREE_SPACE -> {
						if (digitInt > 0) {
							for (int i = 0; i < digitInt; i++) {
								disk.add(new DiskBlock(DiskBlockType.FREE_SPACE, null));
							}
						}
					}
					case null, default -> throw new IllegalStateException("Unexpected value: " + alternatingDiskBlockType);
				}
				alternatingDiskBlockType = alternatingDiskBlockType.alternate();
			}
		}
		System.out.println("disk before de-fragmentation:");
		printDisk(disk);

		// de-fragment the disk by moving 1 file at a time. moving the last file on the disk to the first empty space on the disk
		for (int i = disk.size() - 1; i >= 0; i--) {
			final DiskBlock fileBlockToCheckAndMove = disk.get(i);
			if (fileBlockToCheckAndMove.diskBlockType() == DiskBlockType.FREE_SPACE) {
				continue;
			}
			final int firstFreeSpacePosition = findFirstFreeSpace(disk, i);
			if (firstFreeSpacePosition != -1) {
				final DiskBlock firstFreeDiskBlock = disk.get(firstFreeSpacePosition);
				// simply swap the file block with the free block
				disk.set(firstFreeSpacePosition, fileBlockToCheckAndMove);
				disk.set(i, firstFreeDiskBlock);
			}
		}
		System.out.println("disk after de-fragmentation:");
		printDisk(disk);

		// calculate the filesystem checksum: add up the result of multiplying each of the blocks' position with the file ID number it contains
		long checksum = 0;
		for (int i = 0; i < disk.size(); i++) {
			final DiskBlock diskBlock = disk.get(i);
			if (diskBlock.diskBlockType() == DiskBlockType.FILE) {
				final long add = (long) i * (long) diskBlock.fileId();
				checksum += add;
			}
		}
		System.out.println("part1 solution runtime in milliseconds = " + (System.currentTimeMillis() - start));
		System.out.println("checksum = " + checksum);
	}

	private static int findFirstFreeSpace(final List<DiskBlock> disk, final int lastPossibleFreeSpacePosition) {
		for (int i = 0; i < lastPossibleFreeSpacePosition; i++) {
			if (disk.get(i).diskBlockType() == DiskBlockType.FREE_SPACE) {
				return i;
			}
		}
		return -1;
	}

	private static void printDisk(final List<DiskBlock> disk) {
		for (final DiskBlock diskBlock : disk) {
			switch (diskBlock.diskBlockType()) {
				case FILE -> System.out.print(diskBlock.fileId());
				case FREE_SPACE -> System.out.print(CHAR_EMPTY);
				case null, default -> throw new IllegalStateException("Unexpected value: " + diskBlock.diskBlockType());
			}
		}
		System.out.println();
	}

	private record DiskBlock(DiskBlockType diskBlockType, Integer fileId) {}

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
