package net.srcdemo.audio.convert;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcLogger;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.ui.Files;

public class VorbisEncoder implements AudioHandler
{
	private class OutputStreamThread extends Thread
	{
		@Override
		public void run()
		{
			final byte[] buffer = new byte[outputFileBuffer];
			int read;
			while (true) {
				try {
					read = stdout.read(buffer);
					if (read == -1) {
						break;
					}
					fileSize.addAndGet(read);
					output.write(buffer);
				}
				catch (final IOException e) {
					SrcLogger.error("Couldn't read from OggEnc stdout.", e);
					e.printStackTrace();
				}
			}
			lock.lock();
			stdoutThreadDead.set(true);
			stdoutFinished.signal();
			lock.unlock();
		}
	}

	private static final int outputFileBuffer = 1024;
	private final AtomicLong fileSize = new AtomicLong(0L);
	private final ReentrantLock lock = new ReentrantLock();
	private FileOutputStream output = null;
	private OutputStream stdin = null;
	private InputStream stdout = null;
	private final Condition stdoutFinished;
	private final AtomicBoolean stdoutThreadDead = new AtomicBoolean(false);
	private long writtenSize = 0L;

	public VorbisEncoder(final File outputFile, final int quality)
	{
		final String[] command = { Files.oggEnc.toString(), "-q", Integer.toString(Math.max(-2, Math.min(10, quality))),
				"--ignorelength", "--quiet", "-" };
		SrcLogger.logAudio("Starting OggEnc: " + command);
		stdoutFinished = lock.newCondition();
		Process oggEnc = null;
		try {
			oggEnc = new ProcessBuilder(command).start();
		}
		catch (final IOException e) {
			SrcLogger.error("Couldn't find oggenc.exe.", e);
		}
		if (oggEnc != null) {
			stdin = new BufferedOutputStream(oggEnc.getOutputStream());
			stdout = oggEnc.getInputStream();
			final File outputOggFile = new File(outputFile.getParentFile(), outputFile.getName().replaceAll("\\.wav", ".ogg"));
			try {
				output = new FileOutputStream(outputOggFile);
			}
			catch (final FileNotFoundException e) {
				SrcLogger.error("Couldn't open file " + outputOggFile, e);
				e.printStackTrace();
			}
		}
		if (output != null) {
			new OutputStreamThread().start();
		}
	}

	@Override
	public void close()
	{
		// Ignore the call
	}

	@Override
	public void create()
	{
		// Ignore; this is already done in the constructor
	}

	@Override
	public void destroy()
	{
		lock.lock();
		flush();
		try {
			stdin.close();
		}
		catch (final IOException e) {
			SrcLogger.error("Couldn't close stdin of OggEnc", e);
			e.printStackTrace();
		}
		while (!stdoutThreadDead.get()) {
			try {
				stdoutFinished.await();
			}
			catch (final InterruptedException e) {
				SrcLogger.logAudio("Interrupted while waiting for OggEnc stdout to close. Continuing anyway.");
				break;
			}
		}
		lock.unlock();
	}

	@Override
	public void flush()
	{
		lock.lock();
		try {
			stdin.flush();
		}
		catch (final IOException e) {
			SrcLogger.error("Couln't flush stdin of OggEnc", e);
			e.printStackTrace();
		}
		lock.unlock();
	}

	@Override
	public long getSize()
	{
		return fileSize.get();
	}

	@Override
	public boolean isLocked()
	{
		return lock.isLocked();
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles)
	{
		// Nothing to do here
	}

	@Override
	public void truncate(final long length)
	{
		// Can't truncate
	}

	@Override
	public int write(final byte[] buffer, final long offset)
	{
		final int length = buffer.length;
		if (offset != writtenSize) {
			return length; // Skip all write requests that aren't exactly where the stream stopped
		}
		lock.lock();
		try {
			stdin.write(buffer);
		}
		catch (final IOException e) {
			SrcLogger.error("Couldn't write to OggEnc", e);
			e.printStackTrace();
		}
		writtenSize += length;
		lock.unlock();
		return length;
	}

	@Override
	public int write(final ByteBuffer buffer, final long offset)
	{
		final int length = buffer.remaining();
		final byte[] buf = new byte[length];
		buffer.get(buf);
		return write(buf, offset);
	}
}
