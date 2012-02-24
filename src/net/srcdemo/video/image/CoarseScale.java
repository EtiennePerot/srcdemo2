package net.srcdemo.video.image;

public final class CoarseScale {
	public static int[] scale(final int[] data, final int width, final int height, final int newWidth, final int newHeight) {
		final double inverseScaleX = (double) width / (double) newWidth;
		final double inverseScaleY = (double) height / (double) newHeight;
		final int totalPixels = newWidth * newHeight;
		final int[] result = new int[totalPixels];
		for (int i = 0; i < totalPixels; i++) {
			result[i] = data[Math.min((int) (inverseScaleY * (i / newWidth)), height) * width
				+ Math.min((int) (inverseScaleX * (i % newWidth)), width)];
		}
		return result;
	}
}
