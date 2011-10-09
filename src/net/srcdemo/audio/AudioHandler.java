package net.srcdemo.audio;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface AudioHandler
{
	public void close();

	public void create();

	public void destroy();

	public long getSize();

	public boolean isLocked();

	public void modifyFindResults(String pathName, Collection<String> existingFiles);

	public void truncate(long length);

	public int write(ByteBuffer buffer, long offset);
}
