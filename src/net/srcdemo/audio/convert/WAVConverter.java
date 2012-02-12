package net.srcdemo.audio.convert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcLogger;
import net.srcdemo.audio.AudioHandler;

public class WAVConverter implements AudioHandler {
	private static enum ckType {
		DATA, FACT, FMT, RIFF;
		private static final Map<String, ckType> stringMapping = new HashMap<String, ckType>(ckType.values().length);
		static {
			stringMapping.put("RIFF", RIFF);
			stringMapping.put("fmt ", FMT);
			stringMapping.put("data", DATA);
			stringMapping.put("fact", FACT);
		}

		private static ckType fromString(final String str) {
			return stringMapping.get(str);
		}
	}

	private static final int defaultHeaderBufferSize = 64;
	private static final int minimumHeaderLength = 44;
	private short bitsPerSample = -1;
	private short blockSize = -1;
	private short channels = -1;
	private byte[] ckIDByte = new byte[4];
	private boolean decodable = true;
	private AudioEncoder encoder = null;
	private final AudioEncoderFactory encoderFactory;
	private ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream(defaultHeaderBufferSize);
	private boolean headerDecoded = false;
	private int headerOffset = Integer.MAX_VALUE;
	private final ReentrantLock lock = new ReentrantLock();
	private final File outputFile;
	private int sampleRate = -1;

	public WAVConverter(final AudioEncoderFactory encoderFactory, final File outputFile) {
		this.encoderFactory = encoderFactory;
		this.outputFile = outputFile;
	}

	private void addSamples(final byte[] bytes) throws IOException {
		final int length = bytes.length;
		int i = 0;
		int[] samples = null;
		switch (bitsPerSample) {
			case 8:
				samples = new int[length];
				for (final byte b : bytes) {
					samples[i] = (byte) (b & 0xff);
					i++;
				}
				break;
			case 16:
				final int halfLength = length / 2;
				samples = new int[halfLength];
				for (i = 0; i < halfLength; i++) {
					samples[i] = (short) ((bytes[i * 2] & 0xff) | ((bytes[i * 2 + 1] & 0xff) << 8));
				}
				break;
			case 24:
				final int thirdLength = length / 3;
				samples = new int[thirdLength];
				for (i = 0; i < thirdLength; i++) {
					samples[i] = (bytes[i * 3] & 0xff) | ((bytes[i * 3 + 1] & 0xff) << 8) | ((bytes[i * 3 + 2] & 0xff) << 16);
				}
				break;
			case 32:
				final int quaterLength = length / 4;
				samples = new int[quaterLength];
				for (i = 0; i < quaterLength; i++) {
					samples[i] = (bytes[i * 4] & 0xff) | ((bytes[i * 4 + 1] & 0xff) << 8) | ((bytes[i * 4 + 2] & 0xff) << 16)
						| ((bytes[i * 4 + 3] & 0xff) << 24);
				}
				break;
		}
		if (samples != null) {
			encoder.addSamples(samples);
		}
	}

	@Override
	public void close() {
		// Ignore the call; this is called way to frequently while we need to call it only once.
	}

	@Override
	public void create() {
		// Nothing
	}

	private void decodeHeader() {
		final ByteBuffer header = ByteBuffer.wrap(headerBuffer.toByteArray());
		header.order(ByteOrder.LITTLE_ENDIAN);
		String ckID;
		int ckSize;
		while (decodable && headerBuffer != null && header.hasRemaining()) {
			header.get(ckIDByte);
			ckSize = header.getInt();
			ckID = new String(ckIDByte);
			switch (ckType.fromString(ckID)) {
				case RIFF:
					header.position(header.position() + 4); // Skip WAVE ID field
					break;
				case FACT:
					decodable = false;
					break;
				case FMT:
					decodable = header.getShort() == 0x1; // Decode PCM only
					if (decodable) {
						channels = header.getShort();
						sampleRate = header.getInt();
						header.position(header.position() + 4); // Skip data rate
						blockSize = header.getShort();
						bitsPerSample = header.getShort();
						if (ckSize > 16) {
							header.position(header.position() + ckSize - 16);
						}
						headerOffset = header.position();
						headerDecoded = true;
					}
					break;
				case DATA:
					if (!headerDecoded) {
						decodable = false;
					} else {
						try {
							encoder = encoderFactory.buildEncoder(channels, blockSize, sampleRate, bitsPerSample, outputFile);
							if (header.hasRemaining()) {
								final byte[] remaining = new byte[header.remaining()];
								header.get(remaining);
								addSamples(remaining);
							}
						}
						catch (final IOException e) {
							decodable = false;
							SrcLogger.error("Error: Couldn\'t open FLAC file for writing: " + outputFile, e);
						}
						ckIDByte = null;
						headerBuffer = null;
					}
					break;
			}
		}
	}

	@Override
	public void destroy() {
		lock.lock();
		flush();
		if (encoder != null) {
			try {
				encoder.close();
				encoder = null;
			}
			catch (final IOException e) {
				SrcLogger.error("Error while closing audio encoder of file " + outputFile, e);
			}
		}
		lock.unlock();
	}

	@Override
	public void flush() {
		// Ignore the call
	}

	@Override
	public long getSize() {
		// Impossible to determine precisely
		return 0;
	}

	@Override
	public boolean isLocked() {
		return lock.isLocked();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles) {
		// No need to cheat on anything, the file exists for real.
	}

	@Override
	public void truncate(final long length) {
		// Irrelevant
	}

	@Override
	public int write(final byte[] buffer, final long offset) {
		final int length = buffer.length;
		lock.lock();
		if (!decodable) {
			lock.unlock();
			return length;
		}
		if (headerBuffer != null) {
			try {
				headerBuffer.write(buffer);
			}
			catch (final IOException e) {
				SrcLogger.error("Warning: Couldn't write " + length + " bytes to WAV header buffer.", e);
			}
			if (headerBuffer.size() > minimumHeaderLength) {
				decodeHeader();
			}
		} else if (encoder != null && offset >= headerOffset) {
			try {
				addSamples(buffer);
			}
			catch (final IOException e) {
				SrcLogger.error("Warning: Couldn't write samples bytes to audio encoder of file " + outputFile, e);
			}
		} else if (headerDecoded && offset < headerOffset) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Writing in the header after writing samples; considering the audio file to be finalized.");
			}
			flush();
			destroy();
		}
		lock.unlock();
		return length;
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset) {
		final byte[] buf = new byte[buffer.remaining()];
		buffer.get(buf);
		return write(buf, offset);
	}
}
