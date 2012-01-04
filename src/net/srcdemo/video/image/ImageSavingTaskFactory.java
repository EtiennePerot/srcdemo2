package net.srcdemo.video.image;

public abstract class ImageSavingTaskFactory {
	public abstract ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width,
		final int height);
}
