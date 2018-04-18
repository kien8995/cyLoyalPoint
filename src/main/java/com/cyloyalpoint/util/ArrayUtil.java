package com.cyloyalpoint.util;

import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

	private ArrayUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static void swapArrays(float[] array1, float[] array2, int size) {
		float[] buffer = new float[size];
		for (int i = 0; i < size; i++) {
			buffer[i] = array1[i];
			array1[i] = array2[i];
			array2[i] = buffer[i];
		}
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}

	public static float maxValue(float[] array) {
		float max = Float.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	public static int[][] splitArrayIntoChunks(int[] arrayToSplit, int chunkSize) {
		if (chunkSize <= 0) {
			return new int[][] {};
		}
		int rest = arrayToSplit.length % chunkSize;

		int chunks = arrayToSplit.length / chunkSize + (rest > 0 ? 1 : 0);

		int[][] arrays = new int[chunks][];

		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
		}
		if (rest > 0) {
			arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize,
					(chunks - 1) * chunkSize + rest);
		}
		return arrays;
	}

	public static int[][] splitArrayBaseOnRatio(int[] arrayToSplit, int numberOfChunk, int[] ratio) {
		if (numberOfChunk <= 0 || numberOfChunk != ratio.length) {
			return new int[][] {};
		}
		int rest = arrayToSplit.length % Arrays.stream(ratio).sum();

		int elementPerPart = arrayToSplit.length / Arrays.stream(ratio).sum() + (rest > 0 ? 1 : 0);

		int[][] arrays = new int[numberOfChunk][];

		for (int i = 0; i < (rest > 0 ? numberOfChunk - 1 : numberOfChunk); i++) {
			arrays[i] = Arrays.copyOfRange(arrayToSplit, i * elementPerPart * ratio[i],
					i * elementPerPart * ratio[i] + (elementPerPart * ratio[i]));
		}
		if (rest > 0) {
			arrays[numberOfChunk - 1] = Arrays.copyOfRange(arrayToSplit,
					(numberOfChunk - 1) * elementPerPart * ratio[(numberOfChunk - 1)],
					(numberOfChunk - 1) * elementPerPart * ratio[(numberOfChunk - 1)] + rest);
		}
		return arrays;
	}
}
