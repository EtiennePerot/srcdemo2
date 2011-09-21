package net.srcdemo;

import java.util.Timer;
import java.util.TimerTask;

public class SrcKeepAlive extends Timer
{
	private static final long deadMeat = 60000;
	private static final long pollingInterval = 5000;
	private SrcDemo srcDemo;
	private TimerTask task = new TimerTask()
	{
		@Override
		public void run()
		{
			final long lastTime = srcDemo.getLastClosedFrameTime();
			if (lastTime != -1L && System.currentTimeMillis() - lastTime > deadMeat && !srcDemo.isLocked()) {
				srcDemo.destroy();
				// Cancel TimerTask
				cancel();
				// Also cancel entire Timer
				timerCancel();
			}
		}
	};

	SrcKeepAlive(final SrcDemo srcDemo)
	{
		super(srcDemo.getPrefix() + " checking thread", true);
		this.srcDemo = srcDemo;
		schedule(task, 0, pollingInterval);
	}

	private void timerCancel()
	{
		task = null;
		srcDemo = null;
		cancel();
	}
}
