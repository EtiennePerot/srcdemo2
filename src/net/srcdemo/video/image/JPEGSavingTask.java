package net.srcdemo.video.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class JPEGSavingTask extends ImageSavingTask {
	private final float quality;

	public JPEGSavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height, final float quality) {
		super(sequenceIndex, pixelData, width, height);
		this.quality = quality;
	}

	@Override
	protected boolean doSave(final File outputFile) throws IOException {
		// I hate Java IO
		final BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		finalImage.setRGB(0, 0, width, height, pixelData, 0, width);
		final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		final ImageWriteParam params = writer.getDefaultWriteParam();
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(quality);
		writer.setOutput(new FileImageOutputStream(outputFile));
		final IIOImage image = new IIOImage(finalImage, null, null);
		writer.write(null, image, params);
		return true;
	}

	@Override
	public String getExtension() {
		return "jpg";
	}
}
