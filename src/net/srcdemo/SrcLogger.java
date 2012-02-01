package net.srcdemo;

public final class SrcLogger {
	private static boolean logAudio = false;
	private static boolean logDemo = false;
	private static boolean logFS = false;
	private static boolean logMisc = false;
	private static boolean logVideo = false;

	public static final void error(final String message) {
		System.err.println(message);
	}

	public static final void error(final String message, final Throwable e) {
		System.err.println("[E] " + message + ": " + e);
		e.printStackTrace();
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
