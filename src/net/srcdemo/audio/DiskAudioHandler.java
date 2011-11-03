package net.srcdemo.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

// FIXME: Optimize
public class DiskAudioHandler implements AudioHandler
{
	private final File file;
	private FileChannel fileChannel;

	public DiskAudioHandler(final SrcDemo demo)
	{
		file = demo.getSoundFile();
	}

	@Override
	public void close()
	{
		try {
			fileChannel.close();
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Error while closing audio file at " + file + ".");
		}
		fileChannel = null;
	}

	@Override
	public void create()
	{
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
	}

	@Override
	public void destroy()
	{
		try {
			if (fileChannel != null) {
				fileChannel.close();
			}
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't properly close sound file at " + file + ".");
		}
	}

	@Override
	public long getSize()
	{
		return file.length();
	}

	@Override
	public boolean isLocked()
	{
		return false;
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles)
	{
		// No need to cheat on anything, the file exists for real.
	}

	@Override
	public void truncate(final long length)
	{
		if (fileChannel == null) {
			create();
		}
		try {
			fileChannel.truncate(length);
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't truncate sound file at " + file + " to size " + length + ".");
		}
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset)
	{
		if (fileChannel == null) {
			create();
		}
		try {
			final int w = fileChannel.write(buffer, offset);
			return w;
		}
		catch (final IOException e) {
			SrcLogger.logAudio("Warning: Couldn't write to sound file at " + file + ".");
			return buffer.remaining();
		}
	}
}
