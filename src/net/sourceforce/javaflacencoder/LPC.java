/*
 * Copyright (C) 2010 Preston Lacey http://javaflacencoder.sourceforge.net/ All Rights Reserved. This library is free software;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option) any later version. This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the
 * GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package net.sourceforce.javaflacencoder;

/**
 * This class is used to calculate LPC Coefficients for a FLAC stream.
 * 
 * @author Preston Lacey
 */
public class LPC
{
	/**
	 * Calculate an LPC using the given Auto-correlation data. Static method used since this is slightly faster than a more
	 * strictly object-oriented approach.
	 * 
	 * @param lpc
	 *            LPC to calculate
	 * @param R
	 *            Autocorrelation data to use
	 */
	public static void calculate(final LPC lpc, final long[] R)
	{
		final int coeffCount = lpc.order;
		// calculate first iteration directly
		final double[] A = lpc.rawCoefficients;
		for (int i = 0; i < coeffCount + 1; i++) {
			A[i] = 0.0;
		}
		A[0] = 1;
		double E = R[0];
		// calculate remaining iterations
		if (R[0] == 0) {
			for (int i = 0; i < coeffCount + 1; i++) {
				A[i] = 0.0;
			}
		}
		else {
			final double[] ATemp = lpc.tempCoefficients;
			for (int i = 0; i < coeffCount + 1; i++) {
				ATemp[i] = 0.0;
			}
			for (int k = 0; k < coeffCount; k++) {
				double lambda = 0.0;
				double temp = 0;
				for (int j = 0; j <= k; j++) {
					temp += A[j] * R[k + 1 - j];
				}
				lambda = -temp / E;
				for (int i = 0; i <= k + 1; i++) {
					ATemp[i] = A[i] + lambda * A[k + 1 - i];
				}
				System.arraycopy(ATemp, 0, A, 0, coeffCount + 1);
				E = (1 - lambda * lambda) * E;
			}
		}
		lpc.rawError = E;
	}

	/**
	 * Calculate an LPC using a prior order LPC's values to save calculations.
	 * 
	 * @param lpc
	 *            LPC to calculate
	 * @param R
	 *            Auto-correlation data to use.
	 * @param priorLPC
	 *            Prior order LPC to use(may be any order lower than our target LPC)
	 */
	public static void calculateFromPrior(final LPC lpc, final long[] R, final LPC priorLPC)
	{
		final int coeffCount = lpc.order;
		// calculate first iteration directly
		final double[] A = lpc.rawCoefficients;
		for (int i = 0; i < coeffCount + 1; i++) {
			A[i] = 0.0;
		}
		A[0] = 1;
		double E = R[0];
		int startIter = 0;
		if (priorLPC != null && priorLPC.order < lpc.order) {
			startIter = priorLPC.order;
			E = priorLPC.rawError;
			System.arraycopy(priorLPC.rawCoefficients, 0, A, 0, startIter + 1);
		}
		// calculate remaining iterations
		if (R[0] == 0) {
			for (int i = 0; i < coeffCount + 1; i++) {
				A[i] = 0.0;
			}
		}
		else {
			final double[] ATemp = lpc.tempCoefficients;
			for (int i = 0; i < coeffCount + 1; i++) {
				ATemp[i] = 0.0;
			}
			for (int k = startIter; k < coeffCount; k++) {
				double lambda = 0.0;
				double temp = 0.0;
				for (int j = 0; j <= k; j++) {
					temp -= A[j] * R[k - j + 1];
				}
				lambda = temp / E;
				for (int i = 0; i <= k + 1; i++) {
					ATemp[i] = A[i] + lambda * A[k + 1 - i];
				}
				System.arraycopy(ATemp, 0, A, 0, coeffCount + 1);
				E = (1 - lambda * lambda) * E;
			}
		}
		lpc.rawError = E;
	}

	/**
	 * Create auto-correlation coefficients(up to a maxOrder of 32).
	 * 
	 * @param R
	 *            Array to put results in.
	 * @param samples
	 *            Samples to calculate the auto-correlation for.
	 * @param count
	 *            number of samples to use
	 * @param start
	 *            index of samples array to start at
	 * @param increment
	 *            number of indices to increment between valid samples(for interleaved arrays)
	 * @param maxOrder
	 *            maximum order to calculate.
	 */
	public static void createAutoCorrelation(final long[] R, final int[] samples, final int count, final int start,
			final int increment, final int maxOrder)
	{
		if (increment == 1 && start == 0) {
			for (int i = 0; i <= maxOrder; i++) {
				R[i] = 0;
				long temp = 0;
				for (int j = 0; j < count - i; j++) {
					temp += samples[j] * samples[j + i];
				}
				R[i] += temp;
			}
		}
		else {
			for (int i = 0; i <= maxOrder; i++) {
				R[i] = 0;
				final int baseIndex = increment * i;
				long temp = 0;
				final int innerLimit = (count - i) * increment;
				for (int j = start; j < innerLimit; j += increment) {
					temp += samples[j] * samples[j + baseIndex];
				}
				R[i] += temp;
			}
		}
	}

	/**
	 * Apply a window function to sample data
	 * 
	 * @param samples
	 *            Samples to apply window to. Values in this array are left unaltered.
	 * @param count
	 *            number of samples to use
	 * @param start
	 *            index of samples array to start at
	 * @param increment
	 *            number of indices to increment between valid samples(for interleaved arrays)
	 * @param windowedSamples
	 *            array containing windowed values. Return values are packed(increment of one).
	 */
	public static void window(final int[] samples, final int count, final int start, final int increment,
			final int[] windowedSamples)
	{
		final int[] values = windowedSamples;
		int loopCount = 0;
		final float halfway = count / 2.0f;
		final float hth = halfway * halfway;
		float windowCount = -halfway;
		final int limit = count * increment + start;
		for (int i = start; i < limit; i += increment) {
			final float innerCount = (windowCount < 0) ? -windowCount : windowCount;
			windowCount++;
			// double val = 1.0-(double)(innerCount/halfway);
			final float val = 1.0f - ((innerCount * innerCount) / (hth));
			double temp = ((double) samples[i]) * val;
			temp = (temp > 0) ? temp + 0.5 : temp - 0.5;
			values[loopCount++] = (int) temp;
		}
	}

	/** The order of this LPC calculation */
	protected int order;
	/** The coefficients as calculated by the LPC algorithm */
	protected double[] rawCoefficients;
	/** The error calculated by the LPC algorithm */
	protected double rawError;
	private final double[] tempCoefficients;

	/**
	 * Constructor creates an LPC object of the given order.
	 * 
	 * @param order
	 *            Order for this LPC calculation.
	 */
	public LPC(final int order)
	{
		this.order = order;
		rawError = 0;
		rawCoefficients = new double[order + 1];
		tempCoefficients = new double[order + 1];
	}

	/**
	 * Get the calculated LPC Coefficients as an array.
	 * 
	 * @return lpc coefficients in an array.
	 */
	public double[] getCoefficients()
	{
		return rawCoefficients;
	}

	/**
	 * Get the error for this LPC calculation
	 * 
	 * @return lpc error
	 */
	public double getError()
	{
		return rawError;
	}

	/**
	 * Get this LPC object's order
	 * 
	 * @return order used for this LPC calculation.
	 */
	public int getOrder()
	{
		return order;
	}
}
