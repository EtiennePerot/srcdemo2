package net.srcdemo.video.factories;

import net.srcdemo.video.image.ImageSavingTask;
import net.srcdemo.video.image.ImageSavingTaskFactory;
import net.srcdemo.video.image.TGASavingTask;

public class TGASavingFactory extends ImageSavingTaskFactory {
	private final boolean rleCompression;

	public TGASavingFactory(final boolean rleCompression) {
		this.rleCompression = rleCompression;
	}

	@Override
	public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height) {
		return new TGASavingTask(sequenceIndex, pixelData, width, height, rleCompression);
	}
}
