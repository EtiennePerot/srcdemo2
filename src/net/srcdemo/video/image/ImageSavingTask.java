package net.srcdemo.video.image;

import java.io.File;
import java.io.IOException;

import net.srcdemo.SrcLogger;

public abstract class ImageSavingTask
{
	protected final int height;
	protected int[] pixelData;
	protected final int sequenceIndex;
	protected final int width;

	public ImageSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height)
	{
		this.sequenceIndex = sequenceIndex;
		this.pixelData = pixelData;
		this.width = width;
		this.height = height;
	}

	protected abstract boolean doSave(File outputFile) throws IOException;

	public abstract String getExtension();

	int getSequenceIndex()
	{
		return sequenceIndex;
	}

	boolean save(final File outputFile)
	{
		SrcLogger.logVideo("Spawned " + getExtension() + " image saving task to: " + outputFile);
		boolean result;
		try {
			result = doSave(outputFile);
		}
		catch (final IOException e) {
			SrcLogger.error("Error while writing " + getExtension() + " to " + outputFile, e);
			return false;
		}
		if (result) {
			SrcLogger.logVideo("Finished writing " + getExtension() + " to " + outputFile);
		}
		else {
			SrcLogger.error("Error while writing " + getExtension() + " to " + outputFile + " (unspecified).");
		}
		return result;
	}

	@Override
	public String toString()
	{
		return getExtension().toUpperCase() + "SavingTask(Frame #" + sequenceIndex + " of size " + width + "x" + height + ")";
	}
}
