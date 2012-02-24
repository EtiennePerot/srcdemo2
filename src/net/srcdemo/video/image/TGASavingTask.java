package net.srcdemo.video.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TGASavingTask extends ImageSavingTask {
	private final TGAWriter tgaWriter;

	public TGASavingTask(final int sequenceIndex, final int[] pixelData, final int width, final int height,
		final boolean compression) {
		super(sequenceIndex, pixelData, width, height);
		tgaWriter = new TGAWriter(pixelData, width, height, compression);
	}

	@Override
	protected boolean doSave(final File outputFile) throws IOException {
		final FileOutputStream stream = new FileOutputStream(outputFile);
		final FileChannel chan = stream.getChannel();
		chan.write(tgaWriter.getBuffer());
		chan.force(true);
		chan.close();
		stream.close();
		return true;
	}

	@Override
	public String getExtension() {
		return "tga";
	}
}
