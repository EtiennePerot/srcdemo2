package net.srcdemo.video.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PNGSavingTask extends ImageSavingTask
{
	public PNGSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height)
	{
		super(sequenceIndex, pixelData, width, height);
	}

	@Override
	protected boolean doSave(final File outputFile) throws IOException
	{
		final BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		finalImage.setRGB(0, 0, width, height, pixelData, 0, width);
		ImageIO.write(finalImage, "png", outputFile);
		return true;
	}

	@Override
	public String getExtension()
	{
		return "png";
	}
}
