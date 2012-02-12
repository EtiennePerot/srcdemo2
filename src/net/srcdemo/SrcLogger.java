package net.srcdemo;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public final class SrcLogger {
	private static boolean logAudio = false;
	private static boolean logDemo = false;
	private static boolean logFS = false;
	private static boolean logMisc = false;
	private static boolean logVideo = false;

	private static final void command(final String command, final String... args) {
		final StringBuilder builder = new StringBuilder();
		escapeString(builder, command);
		for (final String arg : args) {
			escapeString(builder, arg);
		}
		try {
			System.out.println("[C] " + Base64.encodeBase64String(builder.toString().getBytes("UTF-8")));
		}
		catch (final UnsupportedEncodingException e) {
			// Shouldn't happen
		}
		System.out.flush(); // Make sure it gets through right now
	}

	public static final void commandRegisterMountPoint(final File mountPoint) {
		command("register_mountpoint", mountPoint.getAbsolutePath());
	}

	public static final void commandUnmount(final File mountPoint) {
		command("unmount", mountPoint.getAbsolutePath());
	}

	public static final void error(final String message) {
		System.err.println(message);
	}

	public static final void error(final String message, final Throwable e) {
		System.err.println("[E] " + message + ": " + e);
		e.printStackTrace();
	}

	private static final void escapeString(final StringBuilder builder, final String string) {
		builder.append("\"");
		builder.append(string.replace("\\", "\\\\").replace("\"", "\\\""));
		builder.append("\"");
	}

	public static final boolean getLogAudio() {
		return logAudio;
	}

	public static final boolean getLogDemo() {
		return logDemo;
	}

	public static final boolean getLogFS() {
		return logFS;
	}

	public static final boolean getLogMisc() {
		return logMisc;
	}

	public static final boolean getLogVideo() {
		return logVideo;
	}

	public static final void log(final String message) {
		if (logMisc) {
			System.out.println("[-] " + message);
		}
	}

	public static final void logAudio(final String message) {
		if (logAudio) {
			System.out.println("[A] " + message);
		}
	}

	public static final void logDemo(final String message) {
		if (logDemo) {
			System.out.println("[D] " + message);
		}
	}

	public static final void logFS(final String message) {
		if (logFS) {
			System.out.println("[F] " + message);
		}
	}

	public static final void logVideo(final String message) {
		if (logVideo) {
			System.out.println("[V] " + message);
		}
	}

	public static final void setLogAll(final boolean log) {
		setLogAudio(log);
		setLogDemo(log);
		setLogMisc(log);
		setLogVideo(log);
		setLogFS(log);
	}

	public static final void setLogAudio(final boolean log) {
		logAudio = log;
	}

	public static final void setLogDemo(final boolean log) {
		logDemo = log;
	}

	public static final void setLogFS(final boolean log) {
		logFS = log;
	}

	public static final void setLogMisc(final boolean log) {
		logMisc = log;
	}

	public static final void setLogVideo(final boolean log) {
		logVideo = log;
	}
}
