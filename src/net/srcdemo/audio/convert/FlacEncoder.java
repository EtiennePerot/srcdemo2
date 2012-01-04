package net.srcdemo.audio.convert;

import java.io.File;
import java.io.IOException;

import net.sourceforge.javaflacencoder.EncodingConfiguration;
import net.sourceforge.javaflacencoder.FLACEncoder;
import net.sourceforge.javaflacencoder.FLACFileOutputStream;
import net.sourceforge.javaflacencoder.StreamConfiguration;

public class FlacEncoder implements AudioEncoder {
	private final int channels;
	private final FLACEncoder encoder;

	public FlacEncoder(final int channels, final int blockSize, final int sampleRate, final int bitsPerSample,
		final File outputFile) throws IOException {
		final File flacFile = new File(outputFile.getParentFile(), outputFile.getName().replaceAll("\\.wav", ".flac"));
		this.channels = channels;
		encoder = new FLACEncoder();
		encoder.setStreamConfiguration(new StreamConfiguration(channels, blockSize, blockSize, sampleRate, bitsPerSample));
		encoder.setEncodingConfiguration(new EncodingConfiguration());
		encoder.setOutputStream(new FLACFileOutputStream(flacFile));
		encoder.openFLACStream();
	}

	@Override
	public void addSamples(final int[] samples) throws IOException {
		encoder.addSamples(samples, samples.length / channels);
		while (encoder.fullBlockSamplesAvailableToEncode() > 0) {
			encoder.encodeSamples(encoder.fullBlockSamplesAvailableToEncode(), false);
		}
	}

	@Override
	public void close() throws IOException {
		encoder.encodeSamples(encoder.samplesAvailableToEncode(), true);
	}

	@Override
	public void flush() throws IOException {
		// Unsupported
	}
}
