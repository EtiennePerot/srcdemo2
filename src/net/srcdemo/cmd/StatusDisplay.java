package net.srcdemo.cmd;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.RollingRate;
import net.srcdemo.SrcDemoListener;
import net.srcdemo.Strings;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
import net.srcdemo.video.image.CoarseScale;

import com.blogspot.homebrewcode.ASCII_mod;

final class StatusDisplay extends Thread implements SrcDemoListener {
	private static final long refreshDelay = 1000L;
	private int bufferOccupied = 0;
	private int bufferSize = 0;
	private AudioBufferStatus bufferStatus = null;
	private final RollingRate framerate;
	private final AtomicInteger framesProcessed = new AtomicInteger(0);
	private int framesSaved = 0;
	private int lastFrameHeight = -1;
	private int[] lastFramePixels = null;
	private int lastFrameWidth = -1;
	private final Lock lock = new ReentrantLock();

	StatusDisplay(final boolean videoEnabled, final boolean audioEnabled) {
		super("Status display thread");
		framerate = videoEnabled ? new RollingRate() : null;
		bufferStatus = audioEnabled ? AudioBufferStatus.REGULAR : null;
		setDaemon(true);
	}

	@Override
	public void onAudioBuffer(final AudioBufferStatus status, final int occupied, final int total) {
		bufferStatus = status;
		bufferOccupied = occupied;
		bufferSize = total;
	}

	@Override
	public void onFrameProcessed(final String frameName) {
		framesProcessed.incrementAndGet();
		framerate.mark();
	}

	@Override
	public void onFrameSaved(final File savedFrame, final int[] pixels, final int width, final int height) {
		lock.lock();
		framesSaved++;
		lastFramePixels = pixels;
		lastFrameWidth = width;
		lastFrameHeight = height;
		lock.unlock();
	}

	@Override
	public void run() {
		final String line = new String(new char[SrcDemoCmd.targetTerminalWidth]).replace('\0', '-');
		try {
			while (!interrupted()) {
				Thread.sleep(refreshDelay);
				if (interrupted()) {
					break;
				}
				lock.lock();
				if (framerate != null) {
					if (lastFramePixels != null && lastFrameWidth > 0 && lastFrameHeight > 0) {
						System.out.println();
						System.out.println(line);
						// Got a new frame
						System.out.println(ASCII_mod.getAscii(CoarseScale.scale(lastFramePixels, lastFrameWidth,
							lastFrameHeight, SrcDemoCmd.targetTerminalWidth, SrcDemoCmd.targetTerminalHeight),
							SrcDemoCmd.targetTerminalWidth, SrcDemoCmd.targetTerminalHeight));
						lastFramePixels = null;
						System.out.println(line);
					}
					System.out.print("\r" + Strings.cmdDisplayVideo1 + framesProcessed + Strings.cmdDisplayVideo2
						+ framerate.getFormattedRate() + Strings.cmdDisplayVideo3 + framesSaved + Strings.cmdDisplayVideo4);
				} else {
					System.out.print("\r");
				}
				if (bufferStatus != null) {
					switch (bufferStatus) {
						case REGULAR:
							if (bufferSize != 0) {
								System.out.print(Strings.cmdDisplayAudio1 + bufferOccupied / 1024 + Strings.cmdDisplayAudio2
									+ bufferSize / 1024 + Strings.cmdDisplayAudio3 + (bufferOccupied * 100 / bufferSize)
									+ Strings.cmdDisplayAudio4);
							} else {
								System.out.print(Strings.cmdDisplayAudioWaiting);
							}
							break;
						case FLUSHING:
							System.out.print(Strings.cmdDisplayAudioFlushing);
							break;
						case DESTROYED:
							System.out.print(Strings.cmdDisplayAudioDestroyed);
							break;
					}
				}
				lock.unlock();
			}
		}
		catch (final InterruptedException e) {
			// Exit
		}
	}
}
