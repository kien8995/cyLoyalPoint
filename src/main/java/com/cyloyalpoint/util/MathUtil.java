package com.cyloyalpoint.util;

import java.util.List;
import java.util.Random;

public class MathUtil {
	public static int randomInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max + 1) - min) + min;
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
}
