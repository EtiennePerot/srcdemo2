package net.srcdemo.audio.factories;

import net.srcdemo.SrcDemo;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.convert.VorbisEncoder;

public class VorbisAudioHandlerFactory extends AudioHandlerFactory {
	private final int quality;

	public VorbisAudioHandlerFactory(final int quality) {
		this.quality = quality;
	}

	@Override
	public AudioHandler buildHandler(final SrcDemo demo) {
		return new VorbisEncoder(demo.getSoundFile(), quality);
	}
}
