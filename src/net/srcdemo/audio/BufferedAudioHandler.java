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

public class BufferedAudioHandler implements AudioHandler
{
	private final byte[] buffer;
	private int bufferOffset = 0;
	private final int bufferSize;
	private final int bufferTimeout;
	private final File file;
	private FileChannel fileChannel = null;
	private long fileLength = 0L;
	private final ReentrantLock fileLock = new ReentrantLock();

	public BufferedAudioHandler(final SrcDemo demo, final int bufferSize, final int bufferTimeout)
	{
		file = demo.getSoundFile();
		this.bufferSize = bufferSize;
		this.bufferTimeout = bufferTimeout;
		buffer = new byte[bufferSize];
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
		flush();
	}

	private void flush()
	{
		fileLock.lock();
		try {
			fileChannel.write(ByteBuffer.wrap(buffer));
		}
		catch (final IOException e) {
			SrcLogger.log("Warning: Couldn't write to sound file at " + file + ".");
		}
		bufferOffset = 0;
		fileLock.unlock();
	}

	@Override
	public long getSize()
	{
		fileLock.lock();
		final long ret = fileLength;
		fileLock.unlock();
		return ret;
	}

	@Override
	public boolean isLocked()
	{
		return fileLock.isLocked();
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
		fileLength = Math.min(length, fileLength);
		fileLock.unlock();
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset)
	{
		final int toReturn = buffer.remaining();
		int toWrite = toReturn;
		fileLock.lock();
		while (toWrite > 0) {
			int canWrite = Math.min(toWrite, bufferSize - bufferOffset);
			if (canWrite <= 0) {
				flush();
				canWrite = Math.min(toWrite, bufferSize);
			}
			buffer.get(this.buffer, bufferOffset, canWrite);
			bufferOffset += canWrite;
			fileLength += canWrite;
			toWrite -= canWrite;
		}
		fileLock.unlock();
		return toReturn;
	}
}
