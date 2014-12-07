package com.jonschang.utils;

public class MathUtils {
	private MathUtils() {}
	
	public static boolean isEqual(double a, double b) {
		return MathUtils.isEqual(a,b,.000001f);
	}
	public static boolean isEqual(double a, double b, double tolerance) {
		return Math.abs(a-b) < tolerance;
	}
}
