package net.srcdemo;

import java.io.File;

import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;

public interface SrcDemoListener
{
	public void onAudioBuffer(AudioBufferStatus status, final int occupied, int total);

	public void onFrameProcessed(final String frameName);

	public void onFrameSaved(final File savedFrame);
}
