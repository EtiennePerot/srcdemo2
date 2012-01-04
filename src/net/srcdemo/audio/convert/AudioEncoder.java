package net.srcdemo.audio.convert;

import java.io.IOException;

public interface AudioEncoder {
	public abstract void addSamples(final int[] samples) throws IOException;

	public abstract void close() throws IOException;

	public void flush() throws IOException;
}
