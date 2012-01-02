package net.srcdemo.userfs;

import net.decasdev.dokan.Dokan;
import net.srcdemo.SrcLogger;

public final class UserFS
{
	public static final class DokanNotInstalledException extends Exception
	{
		private static final long serialVersionUID = -332027883155497034L;
	}

	public static final class DokanVersionException extends Exception
	{
		private static final long serialVersionUID = -3397141069355824035L;
	}

	/**
	 * Initialize the user filesystem library. Will throw an exception if things go wrong.
	 * 
	 * @throws DokanNotInstalledException
	 *             When Dokan is not installed
	 * @throws DokanVersionException
	 *             When Dokan is installed but doesn't have the right version number
	 * @return True if successful
	 */
	public static boolean init() throws DokanNotInstalledException, DokanVersionException
	{
		try {
			if (Dokan.getVersion() == 600) {
				SrcLogger
						.log("Starting with version = " + Dokan.getVersion() + " / Driver = " + Dokan.getDriverVersion() + ".");
				return true;
			}
			SrcLogger.error("Invalid Dokan version: " + Dokan.getVersion());
		}
		catch (final Throwable e) {
			SrcLogger.error("Error caught while initializing Dokan", e);
			throw new DokanNotInstalledException();
		}
		throw new DokanVersionException();
	}
}
