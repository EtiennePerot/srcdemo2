package net.srcdemo.video;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcDemo;

public class NullVideoHandler implements VideoHandler
{
	private Map<Integer, Long> fakeSizes = new HashMap<Integer, Long>();
	private final ReentrantLock lock = new ReentrantLock();

	public NullVideoHandler(final SrcDemo demo)
	{
		// Do nothing
	}

	@Override
	public void close(final int frameNumber)
	{
		lock.lock();
		fakeSizes.remove(frameNumber);
		lock.unlock();
	}

	@Override
	public void create(final int frameNumber)
	{
		// Do nothing
	}

	@Override
	public void destroy()
	{
		fakeSizes = null;
	}

	@Override
	public long getFrameSize(final int frameNumber)
	{
		return 0;
	}

	@Override
	public boolean isLocked()
	{
		return lock.isLocked();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles)
	{
		// Nothing to do
	}

	@Override
	public void truncate(final int frameNumber, final long length)
	{
		lock.lock();
		if (!fakeSizes.containsKey(frameNumber)) {
			fakeSizes.put(frameNumber, 0L);
		}
		fakeSizes.put(frameNumber, Math.min(fakeSizes.get(frameNumber), length));
		lock.unlock();
	}

	@Override
	public int write(final int frameNumber, final ByteBuffer buffer, final long offset)
	{
		final int toWrite = buffer.remaining();
		lock.lock();
		if (!fakeSizes.containsKey(frameNumber)) {
			fakeSizes.put(frameNumber, 0L);
		}
		fakeSizes.put(frameNumber, fakeSizes.get(frameNumber) + toWrite);
		lock.unlock();
		return toWrite;
	}
}
