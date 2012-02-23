package net.srcdemo.audio.factories;

import net.srcdemo.SrcDemo;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler;

public class BufferedAudioHandlerFactory extends AudioHandlerFactory {
	private final int bufferBytes;
	private final AudioHandlerFactory subFactory;
	private final int timeout;

	public BufferedAudioHandlerFactory(final AudioHandlerFactory subFactory, final int bufferSize, final int bufferTimeout) {
		this.subFactory = subFactory;
		bufferBytes = bufferSize * 1024;
		timeout = bufferTimeout;
	}

	@Override
	public AudioHandler buildHandler(final SrcDemo demo) {
		return new BufferedAudioHandler(demo, bufferBytes, timeout, subFactory);
	}
}
