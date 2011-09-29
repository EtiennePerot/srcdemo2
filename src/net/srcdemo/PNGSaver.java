package net.srcdemo;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PNGSaver extends Thread
{
	private final File backingDirectory;
	private SrcDemoFS backingFS;
	private final String demoPrefix;
	private BlockingQueue<PNGSavingTask> tasks = new ArrayBlockingQueue<PNGSavingTask>(4);

	PNGSaver(final SrcDemoFS backingFS, final String demoPrefix)
	{
		super("PNG saving thread");
		this.backingFS = backingFS;
		backingDirectory = backingFS.getBackingStorage();
		this.demoPrefix = demoPrefix;
		setDaemon(true);
		start();
	}

	void add(final PNGSavingTask pngSavingTask)
	{
		try {
			tasks.put(pngSavingTask);
			SrcLogger.log("PNG saving task queued: " + pngSavingTask);
		}
		catch (final InterruptedException e) {
			SrcLogger.log("PNGSaver interrupted while putting: " + pngSavingTask);
		}
	}

	@Override
	public void interrupt()
	{
		tasks = null;
		backingFS = null;
		super.interrupt();
	}

	@Override
	public void run()
	{
		while (true) {
			PNGSavingTask task;
			try {
				task = tasks.take();
			}
			catch (final InterruptedException e) {
				SrcLogger.log("PNGSaver interrupted while waiting for task.");
				break;
			}
			final File outputFile = new File(backingDirectory, demoPrefix + task.getSequenceIndex() + ".png");
			if (task.save(outputFile)) {
				backingFS.notifyFrameSaved(outputFile);
			}
		}
	}
}