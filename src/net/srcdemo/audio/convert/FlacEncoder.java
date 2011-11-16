package net.srcdemo.audio.convert;

import java.io.File;
import java.io.IOException;

import net.sourceforce.javaflacencoder.FLACEncoder;
import net.sourceforce.javaflacencoder.FLACFileOutputStream;
import net.sourceforce.javaflacencoder.StreamConfiguration;

public class FlacEncoder implements AudioEncoder
{
	private final int channels;
	private final FLACEncoder encoder;

	public FlacEncoder(final int channels, final int blockSize, final int sampleRate, final int bitsPerSample,
			final File outputFile) throws IOException
	{
		this.channels = channels;
		encoder = new FLACEncoder();
		encoder.setStreamConfiguration(new StreamConfiguration(channels, blockSize, blockSize, sampleRate, bitsPerSample));
		encoder.setOutputStream(new FLACFileOutputStream(outputFile));
		encoder.openFLACStream();
	}

	@Override
	public void addSamples(final int[] samples) throws IOException
	{
		final int len = samples.length;
		encoder.addSamples(samples, len / channels);
		while (encoder.fullBlockSamplesAvailableToEncode() > 0) {
			encoder.encodeSamples(encoder.fullBlockSamplesAvailableToEncode(), false);
		}
	}

	@Override
	public void close() throws IOException
	{
		encoder.encodeSamples(0, true);
	}

	public void flush() throws IOException
	{
		encoder.flush();
	}
}
