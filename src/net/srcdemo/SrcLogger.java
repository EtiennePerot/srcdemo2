package net.srcdemo;

public class SrcLogger
{
	private static boolean logAudio = false;
	private static boolean logDemo = false;
	private static boolean logMisc = false;
	private static boolean logVideo = false;

	public static void error(final String message)
	{
		System.err.println(message);
	}

	public static void error(final String message, final Throwable e)
	{
		System.err.println("[E] " + message + ": " + e);
		e.printStackTrace();
	}

	public static void log(final String message)
	{
		if (logMisc) {
			System.out.println("[-] " + message);
		}
	}

	public static void logAudio(final String message)
	{
		if (logAudio) {
			System.out.println("[A] " + message);
		}
	}

	public static void logDemo(final String message)
	{
		if (logDemo) {
			System.out.println("[D] " + message);
		}
	}

	public static void logVideo(final String message)
	{
		if (logVideo) {
			System.out.println("[V] " + message);
		}
	}

	public static void setLogAll(final boolean log)
	{
		setLogAudio(log);
		setLogDemo(log);
		setLogMisc(log);
		setLogVideo(log);
	}

	public static void setLogAudio(final boolean log)
	{
		logAudio = log;
	}

	public static void setLogDemo(final boolean log)
	{
		logDemo = log;
	}

	public static void setLogMisc(final boolean log)
	{
		logMisc = log;
	}

	public static void setLogVideo(final boolean log)
	{
		logVideo = log;
	}
}
