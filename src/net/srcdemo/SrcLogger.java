package net.srcdemo;

public class SrcLogger
{
	private static boolean logAll = false;

	public static void error(final String message)
	{
		System.err.println(message);
	}

	public static void error(final String message, final Throwable e)
	{
		System.err.println(message + ": " + e);
		e.printStackTrace();
	}

	public static void log(final String message)
	{
		if (logAll) {
			System.out.println(message);
		}
	}

	public static void setLogAll(final boolean log)
	{
		logAll = log;
	}
}
