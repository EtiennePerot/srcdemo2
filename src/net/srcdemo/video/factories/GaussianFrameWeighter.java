package net.srcdemo.video.factories;

import net.srcdemo.video.FrameWeighter;

public class GaussianFrameWeighter extends FrameWeighter {
	private static final double weightResolution = 1024d;
	private final double mean;
	private final double stdDev;
	private final double variance;

	public GaussianFrameWeighter(final double variance) {
		this(0, variance);
	}

	public GaussianFrameWeighter(final double mean, final double variance) {
		this(mean, variance, Math.sqrt(variance));
	}

	public GaussianFrameWeighter(final double mean, final double variance, final double stdDev) {
		this.mean = mean;
		this.variance = variance;
		this.stdDev = stdDev;
	}

	@Override
	public int weight(final double framePosition) {
		final double x = framePosition * 2d - 1d;
		return (int) (weightResolution * Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))),
			1 / (stdDev * Math.sqrt(2 * Math.PI))));
	}
}
