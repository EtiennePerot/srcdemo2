package net.srcdemo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

public class SrcDemo
{
	private final File backingDirectory;
	private final SrcDemoFS backingFS;
	private final int blendRate;
	private int currentAllocatedSize = -1;
	private int[] currentMergedFrame;
	private final String demoPrefix;
	private final int demoPrefixLength;
	private final Map<Integer, ByteArrayOutputStream> frameData = new HashMap<Integer, ByteArrayOutputStream>();
	private final ReentrantLock frameLock = new ReentrantLock();
	private int framesMerged = -1;
	private int maxAcceptedFrame;
	private int maxEncounteredByteSize = 1048576;
	private int minAcceptedFrame = 0;

	SrcDemo(final SrcDemoFS backingFS, final File backingDirectory, final String prefix, final int blendRate,
			final int shutterAngle)
	{
		this.backingFS = backingFS;
		this.backingDirectory = backingDirectory;
		demoPrefix = prefix;
		demoPrefixLength = demoPrefix.length();
		this.blendRate = blendRate;
		maxAcceptedFrame = (int) Math.ceil((shutterAngle * blendRate) / 360.0) - 1;
		if (maxAcceptedFrame < blendRate - 1) { // Offset by 1
			maxAcceptedFrame++;
			minAcceptedFrame = 1;
		}
	}

	void closeFile(final String fileName)
	{
		final Integer frameNumber = getFrameNumber(fileName);
		if (shouldIgnoreFrame(frameNumber)) {
			return;
		}
		final ByteArrayOutputStream buffer = getFrameByte(frameNumber);
		maxEncounteredByteSize = Math.max(maxEncounteredByteSize, buffer.size());
		frameData.remove(frameNumber);
		handleFrame(frameNumber, buffer.toByteArray());
		backingFS.notifyFrameProcessed(fileName);
	}

	void createFile(final String fileName)
	{
		// Just create it
		getFrameByte(fileName);
	}

	public FileInfo getFileInfo(final String fileName)
	{
		final ByteArrayOutputStream buffer = getFrameByte(fileName);
		if (buffer != null) {
			return FileInfo.fromFile(fileName, buffer.size());
		}
		return FileInfo.fromFile(fileName, 0);
	}

	private ByteArrayOutputStream getFrameByte(final Integer number)
	{
		if (number == null) {
			return null;
		}
		if (!frameData.containsKey(number)) {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream(maxEncounteredByteSize);
			frameData.put(number, buffer);
			return buffer;
		}
		return frameData.get(number);
	}

	private ByteArrayOutputStream getFrameByte(final String fileName)
	{
		final Integer frameNumber = getFrameNumber(fileName);
		if (shouldIgnoreFrame(frameNumber)) {
			return null;
		}
		return getFrameByte(frameNumber);
	}

	private Integer getFrameNumber(final String fileName)
	{
		if (!fileName.startsWith(demoPrefix)) {
			return null;
		}
		final String numberString = fileName.substring(demoPrefixLength, fileName.lastIndexOf('.'));
		try {
			return Integer.parseInt(numberString);
		}
		catch (final NumberFormatException e) {
			return null;
		}
	}

	private void handleFrame(final int frameNumber, final byte[] frameData)
	{
		final int framePosition = frameNumber % blendRate;
		final TGAReader tga = new TGAReader(frameData);
		final int numPixels = tga.getNumPixels();
		final int totalNeededSize = numPixels * 3;
		frameLock.lock();
		if (framePosition == minAcceptedFrame) { // First frame of the sequence
			if (totalNeededSize != currentAllocatedSize) {
				currentMergedFrame = new int[totalNeededSize];
				currentAllocatedSize = totalNeededSize;
			}
			Arrays.fill(currentMergedFrame, 0);
			framesMerged = 0;
		}
		if (totalNeededSize != currentAllocatedSize) {
			System.err.println("Invalid frame size for frame #" + frameNumber + "! Allocated = " + currentAllocatedSize
					+ "; Frame = " + totalNeededSize);
			frameLock.unlock();
			return;
		}
		tga.addToArray(currentMergedFrame);
		framesMerged++;
		if (framePosition == maxAcceptedFrame) { // Last frame of the sequence
			final int[] finalPixels = new int[numPixels];
			for (int i = 0; i < numPixels; i++) {
				final int rPosition = i * 3;
				finalPixels[i] = ((currentMergedFrame[rPosition + 2] / framesMerged) << 16)
						| ((currentMergedFrame[rPosition + 1] / framesMerged) << 8)
						| (currentMergedFrame[rPosition] / framesMerged);
			}
			// At this point, we made a full copy, no need to keep the rest waiting
			new Thread()
			{
				@Override
				public void run()
				{
					final int width = tga.getWidth();
					final int height = tga.getHeight();
					final BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					finalImage.setRGB(0, 0, width, height, finalPixels, 0, width);
					final int sequenceIndex = frameNumber / blendRate;
					final File outputFile = new File(backingDirectory, demoPrefix + sequenceIndex + ".png");
					try {
						ImageIO.write(finalImage, "png", outputFile);
						backingFS.notifyFrameSaved(outputFile);
					}
					catch (final IOException e) {
						System.err.println("Error while writing PNG image: " + outputFile);
						e.printStackTrace();
					}
				}
			}.start();
		}
		frameLock.unlock();
	}

	private boolean shouldIgnoreFrame(final Integer frameNumber)
	{
		if (frameNumber == null) {
			return true;
		}
		final int framePosition = frameNumber % blendRate;
		return framePosition < minAcceptedFrame || framePosition > maxAcceptedFrame;
	}

	void truncateFile(final String fileName, final long length)
	{
		final ByteArrayOutputStream buffer = getFrameByte(fileName);
		if (buffer != null && buffer.size() != length) {
			System.err.println("Buffer size does not match truncate call for frame: " + fileName + "!");
			System.err.println("Buffer size: " + buffer.size() + " / Truncate call: " + length);
		}
	}

	int writeFile(final String fileName, final ByteBuffer buffer, final long offset)
	{
		final ByteArrayOutputStream output = getFrameByte(fileName);
		if (output != null) {
			final int toRead = buffer.remaining();
			final byte[] gotten = new byte[toRead];
			buffer.get(gotten);
			try {
				output.write(gotten);
			}
			catch (final IOException e) {
				System.err.println("Error while copying data to frame: " + fileName);
				e.printStackTrace();
			}
			return toRead;
		}
		// Pretend write was OK anyway
		return buffer.remaining();
	}
}
