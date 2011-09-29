package net.srcdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SrcDemo
{
	private final File backingDirectory;
	private final SrcDemoFS backingFS;
	private final int blendRate;
	private final ReentrantLock bufferLock = new ReentrantLock();
	private int currentAllocatedSize = -1;
	private int[] currentMergedFrame;
	private final String demoPrefix;
	private final int demoPrefixLength;
	private final Map<Integer, ByteArrayOutputStream> frameData = new HashMap<Integer, ByteArrayOutputStream>();
	private final ReentrantLock frameLock = new ReentrantLock();
	private int framesMerged = -1;
	private SrcKeepAlive keepAlive;
	private long lastClosedFrameTime = -1L;
	private int maxAcceptedFrame;
	private int maxEncounteredByteSize = 1048576;
	private int minAcceptedFrame = 0;
	private PNGSaver pngSaver;

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
		pngSaver = new PNGSaver(backingFS, demoPrefix);
		keepAlive = new SrcKeepAlive(this);
	}

	void closeFile(final String fileName)
	{
		final Integer frameNumber = getFrameNumber(fileName);
		if (shouldIgnoreFrame(frameNumber)) {
			SrcLogger.log("Frame " + fileName + " is being closed. Ignoring.");
			return;
		}
		lastClosedFrameTime = System.currentTimeMillis();
		SrcLogger.log("Frame " + fileName + " is being closed. Processing.");
		bufferLock.lock();
		final ByteArrayOutputStream buffer = getFrameByte(frameNumber);
		maxEncounteredByteSize = Math.max(maxEncounteredByteSize, buffer.size());
		frameData.remove(frameNumber);
		bufferLock.unlock();
		handleFrame(frameNumber, buffer.toByteArray());
		backingFS.notifyFrameProcessed(fileName);
		SrcLogger.log("Finished processing frame: " + fileName);
	}

	void createFile(final String fileName)
	{
		// Just create it
		getFrameByte(fileName);
	}

	void destroy()
	{
		SrcLogger.log("Destroying SrcDemo object: " + this);
		// Do some preemptive null-ification
		bufferLock.lock();
		frameLock.lock();
		frameData.clear();
		currentMergedFrame = null;
		pngSaver.interrupt();
		pngSaver = null;
		keepAlive.cancel();
		keepAlive = null;
		bufferLock.unlock();
		frameLock.unlock();
		// Notify the upper layer that we're dead, Jim
		backingFS.destroy(this);
		System.gc();
		SrcLogger.log("Fully destroyed SrcDemo object: " + this);
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
		bufferLock.lock();
		if (!frameData.containsKey(number)) {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream(maxEncounteredByteSize);
			frameData.put(number, buffer);
			bufferLock.unlock();
			return buffer;
		}
		final ByteArrayOutputStream buffer = frameData.get(number);
		bufferLock.unlock();
		return buffer;
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

	long getLastClosedFrameTime()
	{
		return lastClosedFrameTime;
	}

	public String getPrefix()
	{
		return demoPrefix;
	}

	private void handleFrame(final int frameNumber, final byte[] frameData)
	{
		final int framePosition = frameNumber % blendRate;
		final TGAReader tga = new TGAReader(frameData);
		final int numPixels = tga.getNumPixels();
		final int totalNeededSize = numPixels * 3;
		frameLock.lock();
		if (framePosition == minAcceptedFrame) { // First frame of the sequence
			SrcLogger.log("This is the first frame of the sequence. Allocating memory.");
			if (totalNeededSize != currentAllocatedSize) {
				SrcLogger.log("Memmory allocation size is different. Needed: " + totalNeededSize + " / Current: "
						+ currentAllocatedSize);
				currentMergedFrame = new int[totalNeededSize];
				currentAllocatedSize = totalNeededSize;
			}
			Arrays.fill(currentMergedFrame, 0);
			framesMerged = 0;
		}
		if (totalNeededSize != currentAllocatedSize) {
			SrcLogger.error("Invalid frame size for frame #" + frameNumber + "! Allocated = " + currentAllocatedSize
					+ "; Frame = " + totalNeededSize);
			frameLock.unlock();
			return;
		}
		SrcLogger.log("Merging frame: " + frameNumber + " on thread " + Thread.currentThread().getId());
		tga.addToArray(currentMergedFrame);
		framesMerged++;
		if (framePosition == maxAcceptedFrame) { // Last frame of the sequence
			SrcLogger.log("This was the last frame of the sequence. Computing final image.");
			final int[] finalPixels = new int[numPixels];
			for (int i = 0; i < numPixels; i++) {
				final int rPosition = i * 3;
				finalPixels[i] = ((currentMergedFrame[rPosition + 2] / framesMerged) << 16)
						| ((currentMergedFrame[rPosition + 1] / framesMerged) << 8)
						| (currentMergedFrame[rPosition] / framesMerged);
			}
			// At this point, we made a full copy, no need to keep the rest waiting
			pngSaver.add(new PNGSavingTask(frameNumber / blendRate, finalPixels, tga.getWidth(), tga.getHeight()));
		}
		frameLock.unlock();
	}

	boolean isLocked()
	{
		return bufferLock.isLocked() || frameLock.isLocked();
	}

	private boolean shouldIgnoreFrame(final Integer frameNumber)
	{
		if (frameNumber == null) {
			return true;
		}
		final int framePosition = frameNumber % blendRate;
		return framePosition < minAcceptedFrame || framePosition > maxAcceptedFrame;
	}

	@Override
	public String toString()
	{
		return "SrcDemo(Prefix = " + demoPrefix + ")";
	}

	void truncateFile(final String fileName, final long length)
	{
		final ByteArrayOutputStream buffer = getFrameByte(fileName);
		if (buffer != null && buffer.size() != length) {
			SrcLogger.error("Buffer size does not match truncate call for frame: " + fileName + "!");
			SrcLogger.error("Buffer size: " + buffer.size() + " / Truncate call: " + length);
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
				SrcLogger.error("Error while copying data to frame: " + fileName, e);
			}
			return toRead;
		}
		// Pretend write was OK anyway
		return buffer.remaining();
	}
}
