package com.indyforge.twod.engine.graphics.rendering.scenegraph.math;

/**
 * A math class.
 * 
 * @author Christopher Probst
 * 
 */
public final class MathExt {

	/**
	 * Used to compare floats with a given threshold.
	 */
	public static final float kEpsilon = 1E-4f;

	/**
	 * 
	 * @param a
	 *            The first value.
	 * @param b
	 *            The seconds valud.
	 * 
	 * @return true if |a-b| is smaller than {@link MathExt#kEpsilon}, otherwise
	 *         false.
	 */
	public static boolean equals(float a, float b) {
		return equals(a, b, kEpsilon);
	}

	/**
	 * 
	 * @param a
	 *            The first value.
	 * @param b
	 *            The seconds valud.
	 * @param threshold
	 *            The threshold.
	 * @return true if |a-b| is smaller than the given threshold, otherwise
	 *         false.
	 */
	public static boolean equals(float a, float b, float threshold) {
		return Math.abs(a - b) < Math.abs(threshold);
	}

	/**
	 * Clamps the absolute value of a value.
	 * 
	 * @param v
	 *            The value you want to clamp.
	 * @param min
	 *            The positive min value.
	 * @param max
	 *            The positive max value.
	 * @return the absolute clamped value.
	 */
	public static float absClamp(float v, float min, float max) {
		if (max < min) {
			throw new IllegalArgumentException("max is smaller than min");
		} else if (min < 0f) {
			throw new IllegalArgumentException("min and max must be positive");
		}

		// Get the accidental and calc abs value
		float accidental = v < 0 ? -1f : 1f, absV = Math.abs(v);

		// Do absolute clamping
		if (absV < min) {
			v = min * accidental;
		} else if (absV > max) {
			v = max * accidental;
		}
		return v;
	}

	/**
	 * Clamps a float value.
	 * 
	 * @param v
	 *            The value you want to clamp.
	 * @param min
	 *            The min value.
	 * @param max
	 *            The max value.
	 * @return the clamped value.
	 */
	public static float clamp(float v, float min, float max) {
		if (max < min) {
			throw new IllegalArgumentException("max is smaller than min");
		}

		// Clamp...
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}

	/**
	 * Clamps an int value.
	 * 
	 * @param v
	 *            The value you want to clamp.
	 * @param min
	 *            The min value.
	 * @param max
	 *            The max value.
	 * @return the clamped value.
	 */
	public static int clamp(int v, int min, int max) {
		if (max < min) {
			throw new IllegalArgumentException("max is smaller than min");
		}

		// Clamp...
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}

	// Should not be instantiated
	private MathExt() {
	}
}
