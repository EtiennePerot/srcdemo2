package net.srcdemo.video.image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class TGAWriter {
	private static final int maximumRLEpixels = 127;
	private static final int minimumRLEpixels = 3;
	private static final int tgaBufferPad = 64;
	private static final int tgaHeaderLength = 18;
	private final boolean compression;
	private final int height;
	private final int[] pixelData;
	private final int width;

	public TGAWriter(final int[] pixelData, final int width, final int height, final boolean compression) {
		this.pixelData = pixelData;
		this.width = width;
		this.height = height;
		this.compression = compression;
	}

	public ByteBuffer getBuffer() {
		return ByteBuffer.wrap(getBytes());
	}

	public byte[] getBytes() {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream(tgaHeaderLength + pixelData.length * 4 + tgaBufferPad);
		{
			// Header
			buf.write((byte) 0); // ID length (0 = length 0)
			buf.write((byte) 0); // Color map (0 = none)
			if (compression) {
				buf.write((byte) 10); // Compression (10 = run-length truecolor)
			} else {
				buf.write((byte) 2); // Compression (2 = uncompressed truecolor)
			}
			writeShortSmallEndian(buf, (short) 0); // First entry index of the color map (0 because there is no color map)
			writeShortSmallEndian(buf, (short) 0); // Number of entries of the color map (0 because there is no color map)
			buf.write((byte) 0); // Number of bits per pixel in the color map (0 because there is no color map)
			writeShortSmallEndian(buf, (short) 0); // X offset (0 = no offset)
			writeShortSmallEndian(buf, (short) 0); // Y offset (0 = no offset)
			writeShortSmallEndian(buf, (short) width); // Image width
			writeShortSmallEndian(buf, (short) height); // Image height
			buf.write((byte) 24); // Bits per pixel (24 = RGB)
			buf.write((byte) 0x20); // Image direction (0x20 = from top left)
		}
		{
			// Data
			if (compression) {
				// RLE compression
				int currentPixel = pixelData[0];
				int lastPixel = currentPixel;
				int currentPixelCount = 1;
				int j;
				final int maxPixelCount = Math.min(width, maximumRLEpixels);
				for (int i = 1; i < pixelData.length; i++) {
					currentPixel = pixelData[i];
					if (currentPixel == lastPixel && currentPixelCount < maxPixelCount) {
						currentPixelCount++;
					} else {
						// Actually write
						if (currentPixelCount < minimumRLEpixels) {
							// Non-RLE packet
							buf.write((byte) (currentPixelCount - 1));
							for (j = 0; j < currentPixelCount; j++) {
								buf.write((byte) (lastPixel & 0xFF));
								buf.write((byte) ((lastPixel >> 8) & 0xFF));
								buf.write((byte) ((lastPixel >> 16) & 0xFF));
							}
						} else {
							// RLE packet
							buf.write((byte) (currentPixelCount + 127));
							buf.write((byte) (lastPixel & 0xFF));
							buf.write((byte) ((lastPixel >> 8) & 0xFF));
							buf.write((byte) ((lastPixel >> 16) & 0xFF));
						}
						// Initialize for next run
						lastPixel = currentPixel;
						currentPixelCount = 1;
					}
				}
				// Write out final pixel run
				if (currentPixelCount < minimumRLEpixels) {
					// Non-RLE packet
					buf.write((byte) (currentPixelCount - 1));
					for (j = 0; j < currentPixelCount; j++) {
						buf.write((byte) (lastPixel & 0xFF));
						buf.write((byte) ((lastPixel >> 8) & 0xFF));
						buf.write((byte) ((lastPixel >> 16) & 0xFF));
					}
				} else {
					// RLE packet
					buf.write((byte) (currentPixelCount + 127));
					buf.write((byte) (lastPixel & 0xFF));
					buf.write((byte) ((lastPixel >> 8) & 0xFF));
					buf.write((byte) ((lastPixel >> 16) & 0xFF));
				}
			} else {
				// No compression
				for (final int currentPixel : pixelData) {
					buf.write((byte) (currentPixel & 0xFF));
					buf.write((byte) ((currentPixel >> 8) & 0xFF));
					buf.write((byte) ((currentPixel >> 16) & 0xFF));
				}
			}
		}
		return buf.toByteArray();
	}

	private void writeShortSmallEndian(final ByteArrayOutputStream buffer, final short value) {
		buffer.write((byte) (value & 0xFF));
		buffer.write((byte) (value >> 8));
	}
}
