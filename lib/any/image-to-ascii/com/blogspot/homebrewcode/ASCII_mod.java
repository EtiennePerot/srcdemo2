package com.blogspot.homebrewcode;

// Modified version of ASCII.java for SrcDemo2 purposes
// See ASCII.java for license
public class ASCII_mod {
	private static final double blueFactor = 0.1140d;
	private static final double greenFactor = 0.5870d;
	private static final String linebreak = System.getProperty("line.separator");
	private static final double redFactor = 0.2989d;

	public static String getAscii(final int[] data, final int width, final int height) {
		final StringBuilder s = new StringBuilder();
		int p;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				p = data[y * width + x];
				s.append(ASCII.returnStrNeg(((p & 0xff0000) >> 16) * redFactor + ((p & 0xff00) >> 8) * greenFactor + (p & 0xff)
					* blueFactor));
			}
			s.append(linebreak);
		}
		return s.toString();
	}
}
