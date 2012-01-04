package net.srcdemo.video;

import java.nio.ByteBuffer;
import java.util.Collection;

import net.srcdemo.SrcDemo;

public class NullVideoHandler implements VideoHandler {
	public NullVideoHandler(final SrcDemo demo) {
		// Do nothing
	}

	@Override
	public void close(final int frameNumber) {
		// Do nothing
	}

	@Override
	public void create(final int frameNumber) {
		// Do nothing
	}

	@Override
	public void destroy() {
		// Do nothing
	}

	@Override
	public long getFrameSize(final int frameNumber) {
		return 0;
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public void modifyFindResults(final String pathName, final Collection<String> existingFiles) {
		// Do nothing
	}

	@Override
	public void truncate(final int frameNumber, final long length) {
		// Do nothing
	}

	@Override
	public int write(final int frameNumber, final ByteBuffer buffer, final long offset) {
		return buffer.remaining();
	}
}
