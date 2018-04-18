package com.cyloyalpoint.util;

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
}
