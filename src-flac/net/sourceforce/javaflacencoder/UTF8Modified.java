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
 * This is a utility class that provides methods to both encode to and decode from the extended version of UTF8 used by the FLAC
 * format. All functions should work with standard UTF8 as well, since this only extends it to handle larger input values.
 * 
 * @author Preston Lacey
 */
public class UTF8Modified
{
	static final long _fiveByteLimit = (long) Math.pow(2, 26);
	static final long _fourByteLimit = (long) Math.pow(2, 21);
	static final long _oneByteLimit = (long) Math.pow(2, 7);
	static final long _sevenByteLimit = (long) Math.pow(2, 36);
	static final long _sixByteLimit = (long) Math.pow(2, 31);
	static final long _threeByteLimit = (long) Math.pow(2, 16);
	static final long _twoByteLimit = (long) Math.pow(2, 11);
	/**
	 * For debugging: Higher value equals more output, generally by increments of 10
	 */
	public static int DEBUG_LEV = 0;
	static long[] limits = { _oneByteLimit, _twoByteLimit, _threeByteLimit, _fourByteLimit, _fiveByteLimit, _sixByteLimit,
			_sevenByteLimit };

	/**
	 * Convert a value to an extended UTF8 format(as used in FLAC).
	 * 
	 * @param value
	 *            value to convert to extended UTF8(value must be positive and 36 bits or less in size)
	 * @return extended UTF8 encoded value(array size is equal to the number of usable bytes)
	 */
	public static byte[] convertToExtendedUTF8(final long value)
	{
		// calculate bytes needed
		int bytesNeeded = 1;
		for (int i = 0; i < 7; i++) {
			if (value >= limits[i]) {
				bytesNeeded++;
			}
		}
		// create space
		final byte[] result = new byte[bytesNeeded];
		int byteIndex = 0;
		int inputIndex = 0;
		int bytesLeft = bytesNeeded;
		while (bytesLeft > 1) {
			final int midByteMarker = 0x80;// 10 in leftmost bits
			final int midByteMask = 0x3F;// 00111111
			final int val = ((int) (value >>> inputIndex) & midByteMask) | midByteMarker;
			result[byteIndex++] = (byte) val;
			inputIndex += 6;
			bytesLeft--;
		}
		int onesNeeded = inputIndex / 6;
		if (onesNeeded > 0) {
			onesNeeded++;
		}
		final int startMask = 255 >>> (onesNeeded + 1);
		final int ones = 255 << (8 - onesNeeded);
		final int val = ((int) (value >>> inputIndex) & startMask) | ones;
		result[byteIndex++] = (byte) val;
		final byte[] finalResult = new byte[bytesNeeded];
		for (int i = 0; i < bytesNeeded; i++) {
			final int sourceIndex = bytesNeeded - 1 - i;
			final int destIndex = i;
			finalResult[destIndex] = result[sourceIndex];
		}
		if (DEBUG_LEV > 10) {
			System.err.print("input:result_length:result :: " + value + ":" + finalResult.length + "::");
			for (final byte element : finalResult) {
				System.err.print(Integer.toHexString(element) + ":");
			}
			System.err.println();
		}
		return finalResult;
	}

	/**
	 * Decode an extended UTF8(as used in FLAC), to a long value.
	 * 
	 * @param input
	 *            extended UTF8 encoded value.
	 * @return value represented by the UTF8 input.
	 */
	public static long decodeFromExtendedUTF8(final byte[] input)
	{
		int leadOnes = 0;
		final int leadMask = 128;
		int work = input[0];
		while ((work & leadMask) > 0) {
			leadOnes++;
			work = work << 1;
		}
		final int valMask = 255 >>> (leadOnes + 1);
		long val = input[0] & valMask;
		for (int i = 1; i < leadOnes; i++) {
			final int midMask = 0x3F;
			val = val << 6;
			val = (input[i] & midMask) | val;
		}
		return val;
	}

	/**
	 * Constructor. This Class provides only static methods and static fields.
	 */
	public UTF8Modified()
	{
	}
}
