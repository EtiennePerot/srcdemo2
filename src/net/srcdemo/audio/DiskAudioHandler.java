package net.srcdemo.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

public class DiskAudioHandler implements AudioHandler {
	private final File file;
	private FileChannel fileChannel;
	private final ReentrantLock lock = new ReentrantLock();

	public DiskAudioHandler(final SrcDemo demo) {
		file = demo.getSoundFile();
	}

	@Override
	public void close() {
		lock.lock();
		try {
			fileChannel.close();
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Error while closing audio file at " + file + ".");
		}
		fileChannel = null;
		lock.unlock();
	}

	@Override
	public void create() {
		lock.lock();
		try {
			fileChannel = new RandomAccessFile(file, "rw").getChannel();
			fileChannel.position(fileChannel.size());
		}
		catch (final FileNotFoundException e) {
			SrcLogger.error("Could not open audio file: " + file, e);
		}
		catch (final IOException e) {
			SrcLogger.error("Could not seek on audio file: " + file, e);
		}
		lock.unlock();
	}

	@Override
	public void destroy() {
		lock.lock();
		try {
			if (fileChannel != null) {
				fileChannel.close();
			}
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't properly close sound file at " + file + ".");
		}
		lock.unlock();
	}

	@Override
	public void flush() {
		// Unsupported
	}

	@Override
	public long getSize() {
		lock.lock();
		final long length = file.length();
		lock.unlock();
		return length;
	}

	@Override
	public boolean isLocked() {
		return lock.isLocked();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles) {
		// No need to cheat on anything, the file exists for real.
	}

	@Override
	public void truncate(final long length) {
		lock.lock();
		if (fileChannel == null) {
			create();
		}
		try {
			fileChannel.truncate(length);
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't truncate sound file at " + file + " to size " + length + ".");
		}
		lock.unlock();
	}

	@Override
	public int write(final byte[] buffer, final long offset) {
		return write(ByteBuffer.wrap(buffer), offset);
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset) {
		lock.lock();
		if (fileChannel == null) {
			create();
		}
		try {
			final int w = fileChannel.write(buffer, offset);
			lock.unlock();
			return w;
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't write to sound file at " + file + ".");
			lock.unlock();
			return buffer.remaining();
		}
	}
}
