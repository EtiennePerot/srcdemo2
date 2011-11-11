package net.srcdemo;

import java.io.File;

public interface SrcDemoListener
{
	public void onAudioBuffer(final int occupied, int total);

	public void onAudioBufferWriteout();

	public void onFrameProcessed(final String frameName);

	public void onFrameSaved(final File savedFrame);
}
