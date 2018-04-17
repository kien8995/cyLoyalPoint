package com.cyloyalpoint.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MathUtil {

	private MathUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static int randomInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max + 1) - min) + min;
	}

	public static boolean nearlyEqual(float a, float b, float eps) {
		return Math.abs(a - b) < eps;
	}

	public static float zero(float num, float eps) {
		if (Math.abs(num) < eps) {
			return 0.0f;
		}
		return num;
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

	public static int[][] splitArray(int[] arrayToSplit, int chunkSize) {
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
}
