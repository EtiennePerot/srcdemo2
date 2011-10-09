package net.srcdemo.video;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

public class PNGSaver extends Thread
{
	private SrcDemo demo;
	private BlockingQueue<PNGSavingTask> tasks = new ArrayBlockingQueue<PNGSavingTask>(4);

	PNGSaver(final SrcDemo demo)
	{
		super("PNG saving thread");
		this.demo = demo;
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
		demo = null;
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
			final File outputFile = demo.getBackedFile(task.getSequenceIndex() + ".png");
			if (task.save(outputFile)) {
				demo.notifyFrameSaved(outputFile);
			}
		}
	}
}