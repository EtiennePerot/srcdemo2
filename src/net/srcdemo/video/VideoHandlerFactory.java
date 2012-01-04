package net.srcdemo.video;

import net.srcdemo.SrcDemo;

public abstract class VideoHandlerFactory {
	public abstract VideoHandler buildHandler(SrcDemo demo);
}
