package net.srcdemo.video;

public abstract class FrameWeighter {
	/**
	 * Weighing function
	 * 
	 * @param framePosition
	 *            The position of the frame within the current blend (from 0 to 1)
	 * @return The weight to use for this frame, as an integer
	 */
	public abstract int weight(final double framePosition);
}
