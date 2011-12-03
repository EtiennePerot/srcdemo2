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
 * Class to calculate a CRC16 checksum.
 * 
 * @author Preston Lacey
 */
public class CRC16
{
	/** For Debugging: Higher level equals more debug statements */
	public static int DEBUG_LEV = 0;
	/** CRC Divisor: 0x8005(implicit 1 at MSB for 0x18005) */
	static final int divisorCRC16 = 0x8005;
	private static final short xorTable[] = generateTable();

	private static short[] generateTable()
	{
		final short[] table = new short[256];
		for (int i = 0; i < table.length; i++) {
			final int polynomial = divisorCRC16;
			int xorVal = i << 8;
			final int topmask = 1 << 16;
			for (int x = 0; x < 8; x++) {
				xorVal = xorVal << 1;
				if ((xorVal & topmask) > 0) {
					xorVal = (xorVal) ^ polynomial;
				}
			}
			table[i] = (short) (xorVal & 0xFFFF);
		}
		return table;
	}

	/** working checksum stored between calls to update(..) */
	protected int workingCRC;

	/**
	 * Constructor. Creates a CRC16 object that is ready to be used. Next step would be to call update(...) with appropriate
	 * data.
	 */
	public CRC16()
	{
		reset();
	}

	public short checksum()
	{
		return (short) (workingCRC & 0xFFFF);
	}

	/**
	 * Resets stored data, preparing object for a new checksum.
	 */
	public void reset()
	{
		workingCRC = 0;
	}

	public int update(final byte input)
	{
		workingCRC = (workingCRC << 8) ^ xorTable[((workingCRC >>> 8) ^ input) & 0xFF];
		return workingCRC;
	}

	public int update(final byte[] input, final int start, final int stop)
	{
		for (int i = start; i < stop; i++) {
			final byte b = input[i];
			workingCRC = (workingCRC << 8) ^ xorTable[((workingCRC >>> 8) ^ b) & 0xFF];
		}
		return workingCRC;
	}
}
