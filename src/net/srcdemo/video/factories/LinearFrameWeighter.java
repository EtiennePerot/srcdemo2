package net.srcdemo.video.factories;

import net.srcdemo.video.FrameWeighter;

public class LinearFrameWeighter extends FrameWeighter {
	@Override
	public int weight(final double framePosition) {
		return 1;
	}
}
