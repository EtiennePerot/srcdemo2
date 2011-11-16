package net.srcdemo.audio.convert;

import java.io.File;

public abstract class AudioEncoderFactory
{
	public abstract AudioEncoder buildEncoder(final int channels, final int blockSize, final int sampleRate,
			final int bitsPerSample, final File outputFile);
}
