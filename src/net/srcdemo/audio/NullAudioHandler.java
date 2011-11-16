package net.srcdemo.audio;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import net.srcdemo.SrcDemo;

public class NullAudioHandler implements AudioHandler
{
	private final String filename;
	private final AtomicLong size = new AtomicLong(0L);

	public NullAudioHandler(final SrcDemo demo)
	{
		filename = demo.getSoundFile().getName();
	}

	@Override
	public void close()
	{
		// Do nothing
	}

	@Override
	public void create()
	{
		// Do nothing
	}

	@Override
	public void destroy()
	{
		// Do nothing
	}

	@Override
	public void flush()
	{
		// Unsupported
	}

	@Override
	public long getSize()
	{
		return size.get();
	}

	@Override
	public boolean isLocked()
	{
		return false;
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles)
	{
		existingFiles.add(filename);
	}

	@Override
	public void truncate(final long length)
	{
		// Do nothing
	}

	@Override
	public int write(final byte[] buffer, final long offset)
	{
		final int length = buffer.length;
		size.addAndGet(length);
		return length;
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset)
	{
		final int toWrite = buffer.remaining();
		size.addAndGet(toWrite);
		return toWrite;
	}
}
