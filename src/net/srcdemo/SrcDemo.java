package net.srcdemo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;

import net.srcdemo.Mortician.Morticianed;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;

public class SrcDemo implements Morticianed
{
	private final AudioHandler audioHandler;
	private final SrcDemoFS backingFS;
	private final String demoDirectory;
	private final String demoPrefix;
	private final int demoPrefixLength;
	private long lastClosedFrameTime = -1L;
	private final Mortician mortician;
	private final File soundFile;
	private final String soundFileName;
	private final String soundFileNameLowercase;
	private final VideoHandler videoHandler;

	SrcDemo(final SrcDemoFS backingFS, final String prefix, final VideoHandlerFactory videoHandlerFactory,
			final AudioHandlerFactory audioHandlerFactory)
	{
		SrcLogger.logDemo("Creating new SrcDemo with prefix " + prefix);
		this.backingFS = backingFS;
		demoPrefix = prefix;
		demoPrefixLength = demoPrefix.length();
		// Will be empty string if there is no File.separator in the string, which is perfect:
		demoDirectory = demoPrefix.substring(0, demoPrefix.indexOf(File.separator) + 1);
		soundFile = getBackedFile(".wav");
		soundFileName = soundFile.getName();
		soundFileNameLowercase = soundFileName.toLowerCase();
		mortician = new Mortician(this, "Checking thread for " + prefix, new Runnable()
		{
			@Override
			public void run()
			{
				destroy();
			}
		});
		videoHandler = videoHandlerFactory.buildHandler(this);
		audioHandler = audioHandlerFactory.buildHandler(this);
	}

	void closeFile(final String fileName)
	{
		if (isSoundFile(fileName)) {
			audioHandler.close();
		}
		else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				lastClosedFrameTime = System.currentTimeMillis();
				videoHandler.close(frameNumber);
				backingFS.notifyFrameProcessed(fileName);
				SrcLogger.logDemo("Finished processing frame: " + fileName);
			}
		}
	}

	void createFile(final String fileName)
	{
		if (isSoundFile(fileName)) {
			audioHandler.create();
		}
		else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				videoHandler.create(frameNumber);
			}
		}
	}

	private void destroy()
	{
		SrcLogger.logDemo("Destroying SrcDemo object: " + this);
		mortician.stopService();
		videoHandler.destroy();
		audioHandler.destroy();
		// Notify the upper layer that we're dead, Jim
		backingFS.destroy(this);
		System.gc();
		SrcLogger.logDemo("Fully destroyed SrcDemo object: " + this);
	}

	public File getBackedFile(final String fileSuffix)
	{
		return backingFS.getBackedFile(demoPrefix + fileSuffix);
	}

	FileInfo getFileInfo(final String fileName)
	{
		if (isSoundFile(fileName)) {
			return FileInfo.fromFile(soundFileName, audioHandler.getSize());
		}
		final Integer frameNumber = getFrameNumber(fileName);
		if (frameNumber != null) {
			return FileInfo.fromFile(demoPrefix + frameNumber + ".tga", videoHandler.getFrameSize(frameNumber));
		}
		return FileInfo.fromFile(fileName, 0);
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

	public String getPrefix()
	{
		return demoPrefix;
	}

	public File getSoundFile()
	{
		return soundFile;
	}

	@Override
	public boolean isBusy()
	{
		return videoHandler.isLocked() || audioHandler.isLocked();
	}

	private boolean isSoundFile(final String fileName)
	{
		// Needs to be case-insensitive because Source Recorder sometimes like to pass all-uppercase filenames.
		return fileName.toLowerCase().endsWith(soundFileNameLowercase);
	}

	@Override
	public long lastLifeSign()
	{
		return lastClosedFrameTime;
	}

	public void modifyFindResults(final String pathName, final Collection<String> actualFiles)
	{
		if (!pathName.equals(demoDirectory)) {
			return;
		}
		audioHandler.modifyFindResults(pathName, actualFiles);
		videoHandler.modifyFindResults(pathName, actualFiles);
	}

	public void notifyFrameSaved(final File frame)
	{
		backingFS.notifyFrameSaved(frame);
	}

	@Override
	public String toString()
	{
		return "SrcDemo(Prefix = " + demoPrefix + ")";
	}

	void truncateFile(final String fileName, final long length)
	{
		if (isSoundFile(fileName)) {
			audioHandler.truncate(length);
		}
		else {
			final Integer frameNumber = getFrameNumber(fileName);
			if (frameNumber != null) {
				videoHandler.truncate(frameNumber, length);
			}
		}
	}

	int writeFile(final String fileName, final ByteBuffer buffer, final long offset)
	{
		if (isSoundFile(fileName)) {
			return audioHandler.write(buffer, offset);
		}
		final Integer frameNumber = getFrameNumber(fileName);
		if (frameNumber != null) {
			final int w = videoHandler.write(frameNumber, buffer, offset);
			return w;
		}
		return buffer.remaining();
	}
}
