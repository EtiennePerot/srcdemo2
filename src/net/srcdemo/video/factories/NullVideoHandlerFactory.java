package net.srcdemo.video.factories;

import net.srcdemo.SrcDemo;
import net.srcdemo.video.NullVideoHandler;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;

public class NullVideoHandlerFactory extends VideoHandlerFactory {
	@Override
	public VideoHandler buildHandler(final SrcDemo demo) {
		return new NullVideoHandler(demo);
	}
}
