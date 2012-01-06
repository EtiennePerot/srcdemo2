package net.srcdemo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
import net.srcdemo.userfs.FileCreationFlags;
import net.srcdemo.userfs.FileInfo;
import net.srcdemo.userfs.LoopbackFS;
import net.srcdemo.video.VideoHandlerFactory;

public class SrcDemoFS extends LoopbackFS {
	private static final Pattern demoNamePattern = Pattern.compile("\\d+\\.tga$|\\.wav$", Pattern.CASE_INSENSITIVE);
	private final AudioHandlerFactory audioHandlerFactory;
	private final Set<SrcDemoListener> demoListeners = new HashSet<SrcDemoListener>();
	private final ReentrantLock demoLock = new ReentrantLock();
	private final Map<String, SrcDemo> demos = new HashMap<String, SrcDemo>();
	private final VideoHandlerFactory videoHandlerFactory;

	public SrcDemoFS(final String backingStorage, final VideoHandlerFactory videoHandlerFactory,
		final AudioHandlerFactory audioHandlerFactory) {
		super(backingStorage);
		this.audioHandlerFactory = audioHandlerFactory;
		this.videoHandlerFactory = videoHandlerFactory;
	}

	public void addListener(final SrcDemoListener listener) {
		demoListeners.add(listener);
	}

	@Override
	protected void closeFile(final String fileName) {
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			super.closeFile(fileName);
		}
		demo.closeFile(fileName);
	}

	@Override
	protected boolean createFile(final String fileName, final FileCreationFlags creation) {
		if (!creation.shouldCreate()) {
			return super.createFile(fileName, creation);
		}
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.createFile(fileName, creation);
		}
		demo.createFile(fileName);
		return true;
	}

	void destroy(final SrcDemo srcDemo) {
		if (srcDemo == null) {
			return;
		}
		String toDelete = null;
		demoLock.lock();
		for (final String demoPrefix : demos.keySet()) {
			if (srcDemo.equals(demos.get(demoPrefix))) {
				toDelete = demoPrefix;
				break;
			}
		}
		if (toDelete != null) {
			demos.remove(toDelete);
		}
		demoLock.unlock();
	}

	public void flushAudioBuffer(final boolean block) {
		final Thread t = new Thread("Audio buffer flush") {
			@Override
			public void run() {
				demoLock.lock();
				for (final SrcDemo demo : demos.values()) {
					demo.flushAudioBuffer();
				}
				demoLock.unlock();
			}
		};
		if (block) {
			t.run();
		} else {
			t.start();
		}
	}

	private SrcDemo getDemo(final String fileName) {
		final Matcher match = demoNamePattern.matcher(fileName);
		if (!match.find()) {
			return null;
		}
		final String demoName = fileName.substring(0, match.start());
		final String demoNameLowercase = demoName.toLowerCase();
		demoLock.lock();
		if (!demos.containsKey(demoNameLowercase)) {
			demos.put(demoNameLowercase, new SrcDemo(this, demoName, videoHandlerFactory, audioHandlerFactory));
		}
		demoLock.unlock();
		return demos.get(demoNameLowercase);
	}

	@Override
	protected FileInfo getFileInfo(final String fileName) {
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.getFileInfo(fileName);
		}
		return demo.getFileInfo(fileName);
	}

	@Override
	protected Collection<String> listDirectory(final String pathName) {
		// To avoid lack of garbage collection on those objects, return null
		return null;
	}

	void notifyAudioBuffer(final AudioBufferStatus status, final int occupied, final int total) {
		for (final SrcDemoListener listener : demoListeners) {
			listener.onAudioBuffer(status, occupied, total);
		}
	}

	void notifyFrameProcessed(final String frameName) {
		for (final SrcDemoListener listener : demoListeners) {
			listener.onFrameProcessed(frameName);
		}
	}

	void notifyFrameSaved(final File savedFrame, final int[] pixelData, final int width, final int height) {
		for (final SrcDemoListener listener : demoListeners) {
			listener.onFrameSaved(savedFrame, pixelData, width, height);
		}
	}

	public void removeListener(final SrcDemoListener listener) {
		demoListeners.remove(listener);
	}

	@Override
	protected void truncateFile(final String fileName, final long length) {
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			super.truncateFile(fileName, length);
		} else {
			demo.truncateFile(fileName, length);
		}
	}

	@Override
	public boolean unmount() {
		demoLock.lock();
		for (final SrcDemo demo : demos.values()) {
			demo.destroy();
		}
		demoLock.unlock();
		return true;
	}

	@Override
	protected int writeFile(final String fileName, final ByteBuffer buffer, final long offset) {
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.writeFile(fileName, buffer, offset);
		}
		return demo.writeFile(fileName, buffer, offset);
	}
}
