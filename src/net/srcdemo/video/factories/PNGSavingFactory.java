package net.srcdemo.video.factories;

import net.srcdemo.video.image.ImageSavingTask;
import net.srcdemo.video.image.ImageSavingTaskFactory;
import net.srcdemo.video.image.PNGSavingTask;

public class PNGSavingFactory extends ImageSavingTaskFactory {
	@Override
	public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height) {
		return new PNGSavingTask(sequenceIndex, pixelData, width, height);
	}
}
