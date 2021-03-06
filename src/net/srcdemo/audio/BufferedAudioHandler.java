package net.srcdemo.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.Mortician;
import net.srcdemo.Mortician.Morticianed;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

public class BufferedAudioHandler implements AudioHandler, Morticianed {
	public enum AudioBufferStatus {
		DESTROYED, FLUSHING, REGULAR;
	}

	private final ByteArrayOutputStream buffer;
	private int bufferOccupiedSize = 0;
	private long bufferOffset = 0;
	private final int bufferSize;
	private final SrcDemo demo;
	private final File file;
	private final ReentrantLock fileLock = new ReentrantLock();
	private long fileSize = 0L;
	private final AtomicLong lastWrite = new AtomicLong(0L);
	private final Mortician mortician;
	private final AudioHandlerFactory subFactory;
	private AudioHandler subHandler = null;

	public BufferedAudioHandler(final SrcDemo demo, final int bufferSize, final int bufferTimeout,
		final AudioHandlerFactory subFactory) {
		this.demo = demo;
		this.subFactory = subFactory;
		file = demo.getSoundFile();
		mortician = new Mortician(this, "Audio checking thread for " + demo.getPrefix(), 1000, bufferTimeout * 1000, false,
			new Runnable() {
				@Override
				public void run() {
					if (SrcLogger.getLogAudio()) {
						SrcLogger.logAudio("Audio buffer timeout fired. Flushing.");
					}
					flush();
				}
			});
		this.bufferSize = bufferSize;
		buffer = new ByteArrayOutputStream(bufferSize);
		if (SrcLogger.getLogAudio()) {
			SrcLogger.logAudio("Initialized audio buffer for file " + file + " with size " + bufferSize + " and timeout "
				+ bufferTimeout);
		}
	}

	@Override
	public void close() {
		// Ignore
	}

	@Override
	public void create() {
		fileLock.lock();
		if (subHandler == null) {
			subHandler = subFactory.buildHandler(demo);
		}
		fileLock.unlock();
	}

	@Override
	public void destroy() {
		if (SrcLogger.getLogAudio()) {
			SrcLogger.logAudio("Audio buffer is being destroyed. Writing out.");
		}
		mortician.stopService();
		fileLock.lock();
		flush();
		subHandler.close();
		subHandler.destroy();
		subHandler = null;
		fileLock.unlock();
		notifyBuffer(AudioBufferStatus.DESTROYED);
	}

	@Override
	public void flush() {
		notifyBuffer(AudioBufferStatus.FLUSHING);
		fileLock.lock();
		if (buffer == null) {
			fileLock.unlock();
			return;
		}
		if (subHandler == null) {
			create();
		}
		subHandler.write(buffer.toByteArray(), bufferOffset);
		subHandler.flush();
		bufferOffset = fileSize;
		bufferOccupiedSize = 0;
		buffer.reset();
		lastWrite.set(System.currentTimeMillis());
		fileLock.unlock();
		notifyBuffer(AudioBufferStatus.REGULAR);
	}

	@Override
	public long getSize() {
		fileLock.lock();
		final long ret = fileSize;
		fileLock.unlock();
		return ret;
	}

	@Override
	public boolean isBusy() {
		return fileLock.isLocked();
	}

	@Override
	public boolean isLocked() {
		return fileLock.isLocked();
	}

	@Override
	public long lastLifeSign() {
		return lastWrite.get();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles) {
		// No need to cheat on anything, the file exists for real.
	}

	private void notifyBuffer(final AudioBufferStatus status) {
		demo.notifyAudioBuffer(status, bufferOccupiedSize, bufferSize);
	}

	@Override
	public void truncate(final long length) {
		fileLock.lock();
		if (SrcLogger.getLogAudio()) {
			SrcLogger.logAudio("Truncating audio buffer from " + fileSize + " to " + length);
		}
		fileSize = Math.min(length, fileSize);
		lastWrite.set(System.currentTimeMillis());
		fileLock.unlock();
	}

	@Override
	public int write(final byte[] buffer, final long offset) {
		final int toWrite = buffer.length;
		if (SrcLogger.getLogAudio()) {
			SrcLogger.logAudio("Writing " + toWrite + " bytes to audio buffer at offset " + offset);
		}
		fileLock.lock();
		if (offset < fileSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Offset is behind current buffer position; flushing and doing raw write.");
			}
			flush();
			subHandler.write(buffer, offset);
			fileLock.unlock();
			return toWrite;
		}
		if (offset != fileSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Offset (" + offset + ") is in front of the current buffer position (" + fileSize
					+ "); flushing.");
			}
			flush();
			fileSize = offset;
		}
		try {
			this.buffer.write(buffer);
		}
		catch (final IOException e) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.error("Warning: Couldn't write " + toWrite + " bytes to sound buffer.", e);
			}
		}
		fileSize += toWrite;
		bufferOccupiedSize = this.buffer.size();
		if (bufferOccupiedSize > bufferSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Buffer is full; flushing.");
			}
			flush();
		} else {
			notifyBuffer(AudioBufferStatus.REGULAR);
		}
		lastWrite.set(System.currentTimeMillis());
		fileLock.unlock();
		return toWrite;
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset) {
		final int toWrite = buffer.remaining();
		if (SrcLogger.getLogAudio()) {
			SrcLogger.logAudio("Writing " + toWrite + " bytes to audio buffer at offset " + offset);
		}
		fileLock.lock();
		if (offset < fileSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Offset is behind current buffer position; flushing and doing raw write.");
			}
			flush();
			subHandler.write(buffer, offset);
			fileLock.unlock();
			return toWrite;
		}
		if (offset != fileSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Offset (" + offset + ") is in front of the current buffer position (" + fileSize
					+ "); flushing.");
			}
			flush();
			fileSize = offset;
		}
		final byte[] data = new byte[toWrite];
		buffer.get(data);
		try {
			this.buffer.write(data);
		}
		catch (final IOException e) {
			SrcLogger.error("Warning: Couldn't write " + toWrite + " bytes to sound buffer.", e);
		}
		fileSize += toWrite;
		bufferOccupiedSize = this.buffer.size();
		if (bufferOccupiedSize > bufferSize) {
			if (SrcLogger.getLogAudio()) {
				SrcLogger.logAudio("Buffer is full; flushing.");
			}
			flush();
		} else {
			notifyBuffer(AudioBufferStatus.REGULAR);
		}
		lastWrite.set(System.currentTimeMillis());
		fileLock.unlock();
		return toWrite;
	}
}
