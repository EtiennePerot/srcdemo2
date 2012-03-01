package net.srcdemo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;

import net.srcdemo.Mortician.Morticianed;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
import net.srcdemo.userfs.FileInfo;
import net.srcdemo.userfs.UserFSUtils;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;

public class SrcDemo implements Morticianed {
	private static final long frameSizeTimeout = 30000;
	private final AudioHandler audioHandler;
	private final SrcDemoFS backingFS;
	private final String demoDirectory;
	private final String demoPrefix;
	private final int demoPrefixLength;
	private final boolean enableFrameFileInfo = !UserFSUtils.getOperatingSystem().isWindows();
	private final TimedMap<Integer, Long> frameSize = new TimedMap<Integer, Long>(frameSizeTimeout);
	private long lastClosedFrameTime = -1L;
	private final Mortician mortician;
	private final File soundFile;
	private final FileInfo soundFileInfo;
	private final String soundFileName;
	private final String soundFileNameLowercase;
	private final VideoHandler videoHandler;

	SrcDemo(final SrcDemoFS backingFS, final String prefix, final VideoHandlerFactory videoHandlerFactory,
		final AudioHandlerFactory audioHandlerFactory) {
		if (SrcLogger.getLogDemo()) {
			SrcLogger.logDemo("Creating new SrcDemo with prefix " + prefix);
		}
		this.backingFS = backingFS;
		demoPrefix = prefix;
		demoPrefixLength = demoPrefix.length();
		// Will be empty string if there is no File.separator in the string, which is perfect:
		demoDirectory = demoPrefix.substring(0, demoPrefix.indexOf(File.separator) + 1);
		soundFile = getBackedFile(".wav");
		soundFileName = soundFile.getName();
		soundFileNameLowercase = soundFileName.toLowerCase();
		soundFileInfo = new FileInfo(soundFileName, false, 0L);
		mortician = new Mortician(this, "Checking thread for " + prefix, new Runnable() {
			@Override
			public void run() {
				destroy();
			}
		});
		videoHandler = videoHandlerFactory.buildHandler(this);
		audioHandler = audioHandlerFactory.buildHandler(this);
		GCChecker.poke(); // Make sure it is going
	}

	void closeFile(final String fileName) {
		if (isSoundFile(fileName)) {
			audioHandler.close();
		} else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				lastClosedFrameTime = System.currentTimeMillis();
				videoHandler.close(frameNumber);
				backingFS.notifyFrameProcessed(fileName);
				if (SrcLogger.getLogDemo()) {
					SrcLogger.logDemo("Finished processing frame: " + fileName);
				}
			}
		}
	}

	void createFile(final String fileName) {
		if (isSoundFile(fileName)) {
			audioHandler.create();
		} else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				videoHandler.create(frameNumber);
			}
		}
	}

	void destroy() {
		if (SrcLogger.getLogDemo()) {
			SrcLogger.logDemo("Destroying SrcDemo object: " + this);
		}
		mortician.stopService();
		videoHandler.destroy();
		audioHandler.destroy();
		// Notify the upper layer that we're dead, Jim
		backingFS.destroy(this);
		System.gc();
		if (SrcLogger.getLogDemo()) {
			SrcLogger.logDemo("Fully destroyed SrcDemo object: " + this);
		}
	}

	public void flushAudioBuffer() {
		audioHandler.flush();
	}

	public File getBackedFile(final String fileSuffix) {
		return backingFS.getBackedFile(demoPrefix + fileSuffix);
	}

	FileInfo getFileInfo(final String fileName) {
		if (isSoundFile(fileName)) {
			return soundFileInfo.setSize(audioHandler.getSize());
		}
		if (enableFrameFileInfo) {
			final Integer frameNumer = getFrameNumber(fileName);
			if (frameNumer != null) {
				return new FileInfo(fileName, false, frameSize.getDefault(frameNumer, 0L));
			}
		}
		return null;
	}

	private Integer getFrameNumber(final String fileName) {
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

	public String getPrefix() {
		return demoPrefix;
	}

	public File getSoundFile() {
		return soundFile;
	}

	@Override
	public boolean isBusy() {
		return videoHandler.isLocked() || audioHandler.isLocked();
	}

	private boolean isSoundFile(final String fileName) {
		// Needs to be case-insensitive because Source Recorder sometimes like to pass all-uppercase filenames.
		return fileName.toLowerCase().endsWith(soundFileNameLowercase);
	}

	@Override
	public long lastLifeSign() {
		return lastClosedFrameTime;
	}

	public void modifyFindResults(final String pathName, final Collection<String> actualFiles) {
		if (!pathName.equals(demoDirectory)) {
			return;
		}
		audioHandler.modifyFindResults(pathName, actualFiles);
		videoHandler.modifyFindResults(pathName, actualFiles);
	}

	public void notifyAudioBuffer(final AudioBufferStatus status, final int occupied, final int total) {
		backingFS.notifyAudioBuffer(status, occupied, total);
	}

	public void notifyFrameSaved(final File frame, final int[] pixelData, final int width, final int height) {
		backingFS.notifyFrameSaved(frame, pixelData, width, height);
	}

	@Override
	public String toString() {
		return "SrcDemo(Prefix = " + demoPrefix + ")";
	}

	void truncateFile(final String fileName, final long length) {
		if (isSoundFile(fileName)) {
			audioHandler.truncate(length);
		} else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				videoHandler.truncate(frameNumber, length);
				frameSize.put(frameNumber, length);
			}
		}
	}

	int writeFile(final String fileName, final ByteBuffer buffer, final long offset) {
		if (isSoundFile(fileName)) {
			return audioHandler.write(buffer, offset);
		}
		final Integer frameNumber = getFrameNumber(fileName);
		if (frameNumber != null) {
			final int w = videoHandler.write(frameNumber, buffer, offset);
			frameSize.put(frameNumber, frameSize.getDefault(frameNumber, 0L) + w);
			return w;
		}
		return buffer.remaining();
	}
}
