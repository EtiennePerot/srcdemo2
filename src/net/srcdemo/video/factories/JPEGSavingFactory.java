package net.srcdemo.video.factories;

import net.srcdemo.video.image.ImageSavingTask;
import net.srcdemo.video.image.ImageSavingTaskFactory;
import net.srcdemo.video.image.JPEGSavingTask;

public class JPEGSavingFactory extends ImageSavingTaskFactory {
	private final float quality;

	public JPEGSavingFactory(final float quality) {
		this.quality = quality;
	}

	@Override
	public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height) {
		return new JPEGSavingTask(sequenceIndex, pixelData, width, height, quality);
	}
}
