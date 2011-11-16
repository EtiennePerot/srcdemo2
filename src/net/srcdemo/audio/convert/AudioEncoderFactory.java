package net.srcdemo.audio.convert;

import java.io.File;
import java.io.IOException;

public abstract class AudioEncoderFactory
{
	public abstract AudioEncoder buildEncoder(final int channels, final int blockSize, final int sampleRate,
			final int bitsPerSample, final File outputFile) throws IOException;
}
