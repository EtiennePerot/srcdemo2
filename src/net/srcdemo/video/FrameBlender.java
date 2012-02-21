package net.srcdemo.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.GCChecker;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;
import net.srcdemo.TimedMap;
import net.srcdemo.video.image.ImageSaver;
import net.srcdemo.video.image.ImageSavingTaskFactory;

public class FrameBlender implements VideoHandler {
	private static final long frameSizeTimeout = 30000;
	private final double acceptedFrameGap;
	private final int blendRate;
	private final ReentrantLock bufferLock = new ReentrantLock();
	private int currentAllocatedSize = -1;
	private int[] currentMergedFrame;
	private int currentWeight = 0;
	private final Map<Integer, ByteArrayOutputStream> frameData = new HashMap<Integer, ByteArrayOutputStream>();
	private final ReentrantLock frameLock = new ReentrantLock();
	private final Map<Integer, Long> frameSize = new TimedMap<Integer, Long>(frameSizeTimeout);
	private ImageSaver imageSaver;
	private int maxAcceptedFrame;
	private int maxEncounteredByteSize = 1048576;
	private int minAcceptedFrame = 0;
	private final ImageSavingTaskFactory savingFactory;
	private final FrameWeighter weighter;

	public FrameBlender(final SrcDemo demo, final ImageSavingTaskFactory savingFactory, final int blendRate,
		final int shutterAngle, final FrameWeighter weighter) {
		this.blendRate = blendRate;
		maxAcceptedFrame = (int) Math.ceil((shutterAngle * blendRate) / 360.0) - 1;
		if (maxAcceptedFrame < blendRate - 1) { // Offset by 1
			maxAcceptedFrame++;
			minAcceptedFrame = 1;
		}
		acceptedFrameGap = maxAcceptedFrame - minAcceptedFrame;
		this.savingFactory = savingFactory;
		this.weighter = weighter;
		imageSaver = new ImageSaver(demo);
	}

	@Override
	public void close(final int frameNumber) {
		if (shouldIgnoreFrame(frameNumber)) {
			if (SrcLogger.getLogVideo()) {
				SrcLogger.logVideo("Frame " + frameNumber + " is being closed. Ignoring.");
			}
			return;
		}
		if (SrcLogger.getLogVideo()) {
			SrcLogger.logVideo("Frame " + frameNumber + " is being closed. Processing.");
		}
		bufferLock.lock();
		if (!frameData.containsKey(frameNumber)) {
			// Duplicate close call; ignore
			bufferLock.unlock();
			return;
		}
		final ByteArrayOutputStream buffer = getFrameByte(frameNumber);
		maxEncounteredByteSize = Math.max(maxEncounteredByteSize, buffer.size());
		frameData.remove(frameNumber);
		bufferLock.unlock();
		handleFrame(frameNumber, buffer.toByteArray());
	}

	@Override
	public void create(final int frameNumber) {
		// Do nothing
	}

	@Override
	public void destroy() {
		// Do some preemptive null-ification
		bufferLock.lock();
		frameLock.lock();
		frameData.clear();
		currentMergedFrame = null;
		imageSaver.interrupt();
		imageSaver = null;
		frameLock.unlock();
		bufferLock.unlock();
	}

	private ByteArrayOutputStream getFrameByte(final int frameNumber) {
		bufferLock.lock();
		if (!frameData.containsKey(frameNumber)) {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream(maxEncounteredByteSize);
			frameData.put(frameNumber, buffer);
			frameSize.put(frameNumber, 0L);
			bufferLock.unlock();
			return buffer;
		}
		final ByteArrayOutputStream buffer = frameData.get(frameNumber);
		bufferLock.unlock();
		return buffer;
	}

	@Override
	public long getFrameSize(final int frameNumber) {
		final Long size = frameSize.get(frameNumber);
		return size == null ? 0L : size;
	}

	private void handleFrame(final int frameNumber, final byte[] frameData) {
		final int framePosition = frameNumber % blendRate;
		final TGAReader tga = new TGAReader(frameData);
		final int numPixels = tga.getNumPixels();
		final int totalNeededSize = numPixels * 3;
		final double frameWeightX = (framePosition - minAcceptedFrame) / acceptedFrameGap;
		final int frameWeight = weighter.weight(frameWeightX);
		frameLock.lock();
		if (framePosition == minAcceptedFrame) { // First frame of the sequence
			if (SrcLogger.getLogVideo()) {
				SrcLogger.logVideo("This is the first frame of the sequence. Allocating memory.");
			}
			if (totalNeededSize != currentAllocatedSize) {
				if (SrcLogger.getLogVideo()) {
					SrcLogger.logVideo("Memory allocation size is different. Needed: " + totalNeededSize + " / Current: "
						+ currentAllocatedSize);
				}
				currentMergedFrame = new int[totalNeededSize];
				currentAllocatedSize = totalNeededSize;
			}
			Arrays.fill(currentMergedFrame, 0);
			currentWeight = 0;
		}
		if (totalNeededSize != currentAllocatedSize) {
			SrcLogger.error("Invalid frame size for frame #" + frameNumber + "! Allocated = " + currentAllocatedSize
				+ "; Frame = " + totalNeededSize);
			frameLock.unlock();
			return;
		}
		if (SrcLogger.getLogVideo()) {
			SrcLogger.logVideo("Merging frame: " + frameNumber + " on thread " + Thread.currentThread().getId());
		}
		if (frameWeight == 1) {
			tga.addToArray(currentMergedFrame);
		} else if (frameWeight > 1) {
			tga.addToArrayWeighted(currentMergedFrame, frameWeight);
		}
		currentWeight += frameWeight;
		if (framePosition == maxAcceptedFrame) { // Last frame of the sequence
			if (SrcLogger.getLogVideo()) {
				SrcLogger.logVideo("This was the last frame of the sequence. Computing final image.");
			}
			final int[] finalPixels = new int[numPixels];
			int rPosition;
			for (int i = 0; i < numPixels; i++) {
				rPosition = i * 3;
				finalPixels[i] = ((currentMergedFrame[rPosition + 2] / currentWeight) << 16)
					| ((currentMergedFrame[rPosition + 1] / currentWeight) << 8)
					| (currentMergedFrame[rPosition] / currentWeight);
			}
			// At this point, we made a full copy, no need to keep the rest waiting
			imageSaver
				.add(savingFactory.buildSavingTask(frameNumber / blendRate, finalPixels, tga.getWidth(), tga.getHeight()));
		}
		frameLock.unlock();
		GCChecker.poke();
	}

	@Override
	public boolean isLocked() {
		return bufferLock.isLocked() || frameLock.isLocked();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles) {
		// Nothing to do
	}

	private boolean shouldIgnoreFrame(final int frameNumber) {
		final int framePosition = frameNumber % blendRate;
		return framePosition < minAcceptedFrame || framePosition > maxAcceptedFrame;
	}

	@Override
	public void truncate(final int frameNumber, final long length) {
		frameSize.put(frameNumber, length);
	}

	@Override
	public int write(final int frameNumber, final ByteBuffer buffer, final long offset) {
		if (shouldIgnoreFrame(frameNumber)) {
			return buffer.remaining();
		}
		final ByteArrayOutputStream output = getFrameByte(frameNumber);
		final int toWrite = buffer.remaining();
		final byte[] gotten = new byte[toWrite];
		buffer.get(gotten);
		try {
			output.write(gotten);
		}
		catch (final IOException e) {
			SrcLogger.error("Error while copying data to frame: " + frameNumber, e);
		}
		frameSize.put(frameNumber, frameSize.get(frameNumber) + toWrite);
		return toWrite;
	}
}
