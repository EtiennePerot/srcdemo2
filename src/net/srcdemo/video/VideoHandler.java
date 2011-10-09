package net.srcdemo.video;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface VideoHandler
{
	public void close(final int frameNumber);

	public void create(final int frameNumber);

	public void destroy();

	public long getFrameSize(final int frameNumber);

	public boolean isLocked();

	public void modifyFindResults(String pathName, Collection<String> existingFiles);

	public void truncate(final int frameNumber, final long length);

	public int write(final int frameNumber, final ByteBuffer buffer, final long offset);
}
