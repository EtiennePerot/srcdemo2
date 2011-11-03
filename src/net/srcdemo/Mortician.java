package net.srcdemo;

import java.util.Timer;
import java.util.TimerTask;

public class Mortician extends Timer
{
	public interface Morticianed
	{
		public boolean isBusy();

		public long lastLifeSign();
	}

	private TimerTask task;

	public Mortician(final Morticianed morticianed, final String name, final long pollingInterval, final long deadBeef,
			final boolean keepTrying, final Runnable callback)
	{
		super(name, true);
		task = new TimerTask()
		{
			@Override
			public void run()
			{
				final long lastTime = morticianed.lastLifeSign();
				if (lastTime != -1L && System.currentTimeMillis() - lastTime > deadBeef && !morticianed.isBusy()) {
					if (!keepTrying) {
						// Cancel TimerTask
						cancel();
						// Also cancel entire Timer
						stopService();
					}
					if (callback != null) {
						callback.run();
					}
				}
			}
		};
		schedule(task, 0, pollingInterval);
	}

	public Mortician(final Morticianed morticianed, final String name, final Runnable callback)
	{
		this(morticianed, name, 5000, 60000, false, callback);
	}

	public void stopService()
	{
		task = null;
		cancel();
	}
}
