package net.srcdemo.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import net.srcdemo.SrcDemo;
import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcDemoListener;
import net.srcdemo.SrcLogger;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
import net.srcdemo.audio.DiskAudioHandler;
import net.srcdemo.userfs.UserFSUtils;
import net.srcdemo.video.FrameBlender;
import net.srcdemo.video.FrameWeighter;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;
import net.srcdemo.video.factories.GaussianFrameWeighter;
import net.srcdemo.video.image.ImageSavingTask;
import net.srcdemo.video.image.ImageSavingTaskFactory;
import net.srcdemo.video.image.PNGSavingTask;

final class BlendFSTest implements SrcDemoListener {
	private static final File _testImagesDirectory = new File("tests/resources/");
	private static final File testAudio = new File(_testImagesDirectory, "frame.wav");
	/**
	 * First image; no RLE compression
	 */
	private static final File testImage0 = new File(_testImagesDirectory, "frame0.tga");
	/**
	 * Second image; has RLE compression
	 */
	private static final File testImage1 = new File(_testImagesDirectory, "frame1.tga");
	/**
	 * Third image; no RLE compression
	 */
	private static final File testImage2 = new File(_testImagesDirectory, "frame2.tga");
	/**
	 * Fourth image; has RLE compression
	 */
	private static final File testImage3 = new File(_testImagesDirectory, "frame3.tga");
	private static final String testImageReferenceSuffix = "reference.png";

	public static final void main(final String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: outputdir mountpoint");
			System.exit(1);
		}
		System.out.println("Testing linear blending...");
		try {
			new BlendFSTest(args[0], args[1], false);
		}
		catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Testing gaussian blending...");
		try {
			new BlendFSTest(args[0], args[1], true);
		}
		catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	private final String filePrefix;
	private File mountPoint;
	private final AtomicBoolean receivedFrame = new AtomicBoolean(false);
	private final AtomicBoolean validFrame = new AtomicBoolean(false);

	private BlendFSTest(final String outputdir, final String mountPoint, final boolean gaussian) throws Exception {
		SrcLogger.setLogAll(true);
		UserFSUtils.init();
		this.mountPoint = new File(mountPoint);
		filePrefix = gaussian ? "gaussian-" : "regular-";
		final SrcDemoFS mountedFS = new SrcDemoFS(new File(outputdir), new VideoHandlerFactory() {
			@Override
			public VideoHandler buildHandler(final SrcDemo demo) {
				final FrameWeighter weighter;
				if (gaussian) {
					weighter = new GaussianFrameWeighter(0.3d);
				} else {
					weighter = new FrameWeighter() {
						@Override
						public int weight(final double framePosition) {
							return 1;
						}
					};
				}
				return new FrameBlender(demo, new ImageSavingTaskFactory() {
					@Override
					public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width,
						final int height) {
						return new PNGSavingTask(sequenceIndex, pixelData, width, height);
					}
				}, 3, 360, weighter);
			}
		}, new AudioHandlerFactory() {
			@Override
			public AudioHandler buildHandler(final SrcDemo demo) {
				return new DiskAudioHandler(demo);
			}
		});
		mountedFS.addListener(this);
		if (!mountedFS.mount(this.mountPoint, false)) {
			throw new Exception("Error while mounting.");
		}
		// Wait for mount
		Thread.sleep(5000);
		final Thread audioThread = new Thread() {
			@Override
			public void run() {
				try {
					copy(testAudio);
				}
				catch (final IOException e) {
					System.err.println("Error while copying audio file: " + e);
					e.printStackTrace();
				}
			}
		};
		audioThread.start();
		copy(testImage0);
		copy(testImage1);
		copy(testImage2);
		copy(testImage3);
		audioThread.join();
		if (!mountedFS.unmount()) {
			throw new Exception("Error while unmounting");
		}
		Thread.sleep(5000);
		if (receivedFrame.get()) {
			if (validFrame.get()) {
				System.out.println("Frame received and matched reference.");
			} else {
				System.err.println("Frame received, but did not match reference.");
			}
		} else {
			System.err.println("Frame not received.");
		}
		final String md5ref = md5Hash(testAudio);
		final String md5out = md5Hash(new File(outputdir, filePrefix + testAudio.getName()));
		if (md5ref.equals(md5out)) {
			System.out.println("Audio file matched: " + md5ref);
		} else {
			System.err.println("Audio file did not match: " + md5out + " vs reference " + md5ref);
		}
	}

	private void copy(final File file) throws IOException {
		System.out.println("Copying " + file.getName());
		final File target = new File(mountPoint, filePrefix + file.getName());
		final InputStream in = new FileInputStream(file);
		final OutputStream out = new FileOutputStream(target);
		final byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private String md5Hash(final File file) {
		try {
			final InputStream f = new FileInputStream(file);
			final byte[] buf = new byte[1024];
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			int read;
			do {
				read = f.read(buf);
				if (read > 0) {
					digest.update(buf, 0, read);
				}
			} while (read != -1);
			f.close();
			return new BigInteger(1, digest.digest()).toString(16);
		}
		catch (final Exception e) {
			return null;
		}
	}

	@Override
	public void onAudioBuffer(final AudioBufferStatus status, final int occupied, final int total) {
	}

	@Override
	public void onFrameProcessed(final String frameName) {
	}

	@Override
	public void onFrameSaved(final File savedFrame, final int[] pixels, final int width, final int height) {
		final BufferedImage reference;
		try {
			reference = ImageIO.read(new File(_testImagesDirectory, filePrefix + testImageReferenceSuffix));
		}
		catch (final IOException e) {
			System.err.println("Error while reading reference frame: " + e);
			e.printStackTrace();
			return;
		}
		final int refWidth = reference.getWidth();
		final int refHeight = reference.getHeight();
		if (refWidth != width || refHeight != height) {
			System.err.println("Mismatched dimensions: " + width + "x" + height + " vs reference: " + refWidth + "x"
				+ refHeight);
			receivedFrame.set(true);
			return;
		}
		final int[] referencePixels = reference.getRGB(0, 0, refWidth, refHeight, null, 0, refWidth);
		for (int i = 0; i < pixels.length; i++) {
			referencePixels[i] = referencePixels[i] & 0xFFFFFF;
			if (pixels[i] != referencePixels[i]) {
				System.err.println("Mismatched pixel value: " + pixels[i] + " vs reference " + referencePixels[i]
					+ " at index " + i);
				receivedFrame.set(true);
				return;
			}
		}
		validFrame.set(true);
		receivedFrame.set(true);
	}
}
