package net.srcdemo.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.Mortician;
import net.srcdemo.Mortician.Morticianed;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

public class BufferedAudioHandler implements AudioHandler, Morticianed
{
	private final ByteArrayOutputStream buffer;
	private final int bufferSize;
	private final File file;
	private FileChannel fileChannel = null;
	private final ReentrantLock fileLock = new ReentrantLock();
	private long fileSize = 0L;
	private long lastWrite = -1L;
	private final Mortician mortician;

	public BufferedAudioHandler(final SrcDemo demo, final int bufferSize, final int bufferTimeout)
	{
		file = demo.getSoundFile();
		mortician = new Mortician(this, "Audio checking thread for " + demo.getPrefix(), 1000, bufferTimeout * 1000, false,
				new Runnable()
				{
					@Override
					public void run()
					{
						SrcLogger.logAudio("Audio buffer timeout fired. Writing out.");
						writeOut();
					}
				});
		this.bufferSize = bufferSize;
		buffer = new ByteArrayOutputStream(bufferSize);
		SrcLogger.logAudio("Initialized audio buffer for file " + file + " with size " + bufferSize + " and timeout "
				+ bufferTimeout);
	}

	@Override
	public void close()
	{
		// Ignore
	}

	@Override
	public void create()
	{
		fileLock.lock();
		if (fileChannel == null) {
			SrcLogger.logAudio("Audio buffer is being created.");
			try {
				fileChannel = new RandomAccessFile(file, "rw").getChannel();
			}
			catch (final FileNotFoundException e) {
				SrcLogger.error("Could not open audio file: " + file, e);
			}
		}
		fileLock.unlock();
	}

	@Override
	public void destroy()
	{
		SrcLogger.logAudio("Audio buffer is being destroyed. Writing out.");
		writeOut();
		mortician.stopService();
	}

	private void flush()
	{
		fileLock.lock();
		if (fileChannel == null) {
			create();
		}
		try {
			fileChannel.write(ByteBuffer.wrap(buffer.toByteArray()));
		}
		catch (final IOException e) {
			SrcLogger.error("Warning: Couldn't write to sound file at " + file + ".", e);
		}
		lastWrite = System.currentTimeMillis();
		buffer.reset();
		fileLock.unlock();
	}

	@Override
	public long getSize()
	{
		fileLock.lock();
		final long ret = fileSize;
		fileLock.unlock();
		return ret;
	}

	@Override
	public boolean isBusy()
	{
		return fileLock.isLocked();
	}

	@Override
	public boolean isLocked()
	{
		return fileLock.isLocked();
	}

	@Override
	public long lastLifeSign()
	{
		return lastWrite;
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles)
	{
		// No need to cheat on anything, the file exists for real.
	}

	@Override
	public void truncate(final long length)
	{
		fileLock.lock();
		SrcLogger.logAudio("Truncating audio buffer from " + fileSize + " to " + length);
		fileSize = Math.min(length, fileSize);
		fileLock.unlock();
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset)
	{
		final int toWrite = buffer.remaining();
		SrcLogger.logAudio("Writing " + toWrite + " bytes to audio buffer at offset " + offset);
		fileLock.lock();
		lastWrite = System.currentTimeMillis();
		if (offset < fileSize) {
			SrcLogger.logAudio("Offset is behind current buffer position; flushing and doing raw write.");
			flush();
			try {
				fileChannel.write(buffer, offset);
			}
			catch (final IOException e) {
				SrcLogger.error("Warning: Couldn't write to sound file at " + file + ".", e);
			}
			fileLock.unlock();
			return toWrite;
		}
		if (offset != fileSize) {
			SrcLogger.logAudio("Offset (" + offset + ") is in front of the current buffer position (" + fileSize
					+ "); flushing.");
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
		if (this.buffer.size() > bufferSize) {
			SrcLogger.logAudio("Buffer is full; flushing.");
			flush();
		}
		fileLock.unlock();
		return toWrite;
	}

	private void writeOut()
	{
		fileLock.lock();
		flush();
		try {
			fileChannel.close();
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't close sound file at " + file + ".");
		}
		fileChannel = null;
		fileLock.unlock();
	}
}
