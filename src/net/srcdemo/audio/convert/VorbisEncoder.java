package net.srcdemo.audio.convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.vorbis_block;
import org.xiph.libvorbis.vorbis_comment;
import org.xiph.libvorbis.vorbis_dsp_state;
import org.xiph.libvorbis.vorbis_info;
import org.xiph.libvorbis.vorbisenc;

public class VorbisEncoder implements AudioEncoder
{
	private final double bitDepth;
	private final vorbis_block block;
	private final int blockSize;
	private final int channels;
	private final vorbis_dsp_state dsp;
	private final FileOutputStream output;
	private final ogg_packet packet;
	private final ogg_page page;
	private final ogg_stream_state streamState;
	private int writtenSamples = 0;

	public VorbisEncoder(final int channels, final int blockSize, final int sampleRate, final int bitsPerSample,
			final float quality, final File outputFile) throws IOException
	{
		this.channels = channels;
		this.blockSize = blockSize;
		bitDepth = Math.pow(2, bitsPerSample - 1);
		final vorbis_info vi = new vorbis_info();
		final vorbisenc encoder = new vorbisenc();
		encoder.vorbis_encode_init_vbr(vi, channels, sampleRate, Math.max(0, Math.min(1, quality)));
		dsp = new vorbis_dsp_state();
		dsp.vorbis_analysis_init(vi);
		block = new vorbis_block(dsp);
		streamState = new ogg_stream_state(new Random().nextInt(256));
		final vorbis_comment vc = new vorbis_comment();
		final ogg_packet header = new ogg_packet();
		final ogg_packet header_comm = new ogg_packet();
		final ogg_packet header_code = new ogg_packet();
		dsp.vorbis_analysis_headerout(vc, header, header_comm, header_code);
		streamState.ogg_stream_packetin(header);
		streamState.ogg_stream_packetin(header_comm);
		streamState.ogg_stream_packetin(header_code);
		page = new ogg_page();
		packet = new ogg_packet();
		output = new FileOutputStream(new File(outputFile.getParentFile(), outputFile.getName().replaceAll("\\.wav", ".flac")));
		while (streamState.ogg_stream_flush(page)) {
			output.write(page.header, 0, page.header_len);
			output.write(page.body, 0, page.body_len);
		}
	}

	@Override
	public void addSamples(final int[] samples) throws IOException
	{
		final int channelSamples = samples.length / channels;
		final float[][] floatSamples = dsp.vorbis_analysis_buffer(channelSamples);
		int channel;
		for (int i = 0; i < channelSamples; i++) {
			for (channel = 0; channel < channels; channel++) {
				floatSamples[channel][i] = (float) (samples[i] / bitDepth);
			}
		}
		dsp.vorbis_analysis_wrote(channelSamples);
		writtenSamples += channelSamples;
		if (writtenSamples >= blockSize) {
			flush();
		}
	}

	@Override
	public void close() throws IOException
	{
		dsp.vorbis_analysis_wrote(0);
		flush();
		output.close();
	}

	@Override
	public void flush() throws IOException
	{
		while (block.vorbis_analysis_blockout(dsp)) {
			block.vorbis_analysis(null);
			block.vorbis_bitrate_addblock();
			while (dsp.vorbis_bitrate_flushpacket(packet)) {
				streamState.ogg_stream_packetin(packet);
				do {
					if (!streamState.ogg_stream_pageout(page)) {
						break;
					}
					output.write(page.header, 0, page.header_len);
					output.write(page.body, 0, page.body_len);
				}
				while (page.ogg_page_eos() > 0);
			}
		}
		writtenSamples = 0;
		output.flush();
	}
}
