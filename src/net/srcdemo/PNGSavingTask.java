package net.srcdemo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PNGSavingTask
{
	private final int height;
	private final int[] pixelData;
	private final int sequenceIndex;
	private final int width;

	PNGSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height)
	{
		this.sequenceIndex = sequenceIndex;
		this.pixelData = pixelData;
		this.width = width;
		this.height = height;
	}

	int getSequenceIndex()
	{
		return sequenceIndex;
	}

	boolean save(final File outputFile)
	{
		SrcLogger.log("Spawned PNG saving task to: " + outputFile);
		final BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		finalImage.setRGB(0, 0, width, height, pixelData, 0, width);
		try {
			ImageIO.write(finalImage, "png", outputFile);
			SrcLogger.log("Finished writing final PNG: " + outputFile);
			return true;
		}
		catch (final IOException e) {
			SrcLogger.error("Error while writing PNG image " + outputFile, e);
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "PNGSavingTask(Frame #" + sequenceIndex + " of size " + width + "x" + height + ")";
	}
}
