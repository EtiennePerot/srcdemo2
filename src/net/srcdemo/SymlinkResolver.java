package net.srcdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import net.srcdemo.ui.Files;

public final class SymlinkResolver {
	public static File resolveSymlinks(File file) {
		file = file.getAbsoluteFile();
		Process resolveSymlinks = null;
		try {
			resolveSymlinks = new ProcessBuilder(Files.resolveSymlinksWindows.toString()).start();
		}
		catch (final IOException e) {
			return file;
		}
		final String fileString = file.toString();
		try {
			final OutputStream stdin = resolveSymlinks.getOutputStream();
			stdin.write(fileString.getBytes("UTF-8"));
			stdin.write('\n');
			stdin.flush();
		}
		catch (final Exception e) {
			return file;
		}
		final InputStream stdout = resolveSymlinks.getInputStream();
		final int length = fileString.length();
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream(length);
		final byte[] buf = new byte[length];
		int read;
		while (true) {
			try {
				read = stdout.read(buf);
				if (read == -1) {
					break;
				}
				buffer.write(buf, 0, read);
			}
			catch (final IOException e) {
				break;
			}
		}
		final byte[] finalFileBytes = buffer.toByteArray();
		try {
			return new File(new String(finalFileBytes, "UTF-8"));
		}
		catch (final UnsupportedEncodingException e) {
			return file;
		}
	}
}
