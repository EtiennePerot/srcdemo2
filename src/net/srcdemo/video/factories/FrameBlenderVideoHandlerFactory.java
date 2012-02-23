package net.srcdemo.video.factories;

import net.srcdemo.SrcDemo;
import net.srcdemo.video.FrameBlender;
import net.srcdemo.video.FrameWeighter;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;
import net.srcdemo.video.image.ImageSavingTaskFactory;

public class FrameBlenderVideoHandlerFactory extends VideoHandlerFactory {
	private final int blendRate;
	private final FrameWeighter frameWeighter;
	private final ImageSavingTaskFactory imgFactory;
	private final int shutterAngle;

	public FrameBlenderVideoHandlerFactory(final ImageSavingTaskFactory imgFactory, final int blendRate,
		final int shutterAngle, final FrameWeighter frameWeighter) {
		this.imgFactory = imgFactory;
		this.blendRate = blendRate;
		this.shutterAngle = shutterAngle;
		this.frameWeighter = frameWeighter;
	}

	@Override
	public VideoHandler buildHandler(final SrcDemo demo) {
		return new FrameBlender(demo, imgFactory, blendRate, shutterAngle, frameWeighter);
	}
}
