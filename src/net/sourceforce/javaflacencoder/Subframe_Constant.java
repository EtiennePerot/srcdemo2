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
 * Implements the Subframe abstract class, providing encoding support for the FLAC Constant Subframe.
 * 
 * @author Preston Lacey
 */
public class Subframe_Constant extends Subframe
{
	/**
	 * For debugging: Higher values equals greater output, generally by increments of 10.
	 */
	public static int DEBUG_LEV = 0;
	/** Subframe type supported by this implementation. */
	public static final EncodingConfiguration.SubframeType type = EncodingConfiguration.SubframeType.VERBATIM;
	int sampleSize = 0;

	/**
	 * Constructor. Sets StreamConfiguration to use. If the StreamConfiguration must later be changed, a new Subframe object
	 * must be created as well.
	 * 
	 * @param sc
	 *            StreamConfiguration to use for encoding.
	 */
	public Subframe_Constant(final StreamConfiguration sc)
	{
		super(sc);
		sampleSize = sc.getBitsPerSample();
	}

	@Override
	public int encodeSamples(final int[] samples, final int count, final int start, final int skip, final EncodedElement data,
			final int offset, final int bitsPerSample)
	{
		if (DEBUG_LEV > 0) {
			System.err.println("Subframe_Verbatim::encodeSamples(...)");
		}
		int encodedSamples = count;
		final int bits = bitsPerSample + offset + 8;
		int bytesNeeded = bits / 8;
		if (bits % 8 != 0) {
			bytesNeeded++;
		}
		data.clear(bytesNeeded, offset);
		data.addInt(0, 1);
		data.addInt(0, 6);
		data.addInt(0, 1);
		final int value = samples[start];
		final int increment = skip + 1;
		final int end = start + increment * count;
		int lastValid = end - increment;// assume all were the same
		for (int i = start; i < end; i += increment) {
			if (samples[i] != value) {
				lastValid = i - increment;// if one differed, find where
				break;
			}
		}
		encodedSamples = (lastValid - start) / increment + 1;
		data.addInt(value, bitsPerSample);
		lastEncodedSize = bits - offset;
		System.out.flush();
		if (DEBUG_LEV > 0) {
			System.err.println("Subframe_Verbatim::encodeSamples(...): End");
		}
		if (DEBUG_LEV > 10) {
			System.err.println("--: bitsUsed : " + bits + "  : Bytes : " + bytesNeeded);
		}
		return encodedSamples;
	}

	/**
	 * This method is used to set the encoding configuration.
	 * 
	 * @param ec
	 *            encoding configuration to use.
	 * @return true if configuration was changed, false otherwise
	 */
	@Override
	public boolean registerConfiguration(final EncodingConfiguration ec)
	{
		super.registerConfiguration(ec);
		return true;
	}
}
