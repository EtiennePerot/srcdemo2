package net.srcdemo.audio;

import net.srcdemo.SrcDemo;

public abstract class AudioHandlerFactory
{
	public abstract AudioHandler buildHandler(SrcDemo demo);
}
