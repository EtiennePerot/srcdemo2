package net.srcdemo.audio.factories;

import java.io.File;
import java.io.IOException;

import net.srcdemo.SrcDemo;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.convert.AudioEncoder;
import net.srcdemo.audio.convert.AudioEncoderFactory;
import net.srcdemo.audio.convert.FlacEncoder;
import net.srcdemo.audio.convert.WAVConverter;

public class FlacAudioHandlerFactory extends AudioHandlerFactory {
	private AudioEncoderFactory encoderFactory;

	public FlacAudioHandlerFactory() {
		encoderFactory = new AudioEncoderFactory() {
			@Override
			public AudioEncoder buildEncoder(final int channels, final int blockSize, final int sampleRate,
				final int bitsPerSample, final File outputFile) throws IOException {
				return new FlacEncoder(channels, blockSize, sampleRate, bitsPerSample, outputFile);
			}
		};
	}

	@Override
	public AudioHandler buildHandler(final SrcDemo demo) {
		return new WAVConverter(encoderFactory, demo.getSoundFile());
	}
}
