package net.srcdemo.video.image;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;

public class ImageSaver extends Thread
{
	private SrcDemo demo;
	private BlockingQueue<ImageSavingTask> tasks = new ArrayBlockingQueue<ImageSavingTask>(4);

	public ImageSaver(final SrcDemo demo)
	{
		super("Image saving thread");
		this.demo = demo;
		setDaemon(true);
		start();
	}

	public void add(final ImageSavingTask imgSavingTask)
	{
		try {
			tasks.put(imgSavingTask);
			SrcLogger.log("Image saving task queued: " + imgSavingTask);
		}
		catch (final InterruptedException e) {
			SrcLogger.log("ImageSaver interrupted while putting: " + imgSavingTask);
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
			ImageSavingTask task;
			try {
				task = tasks.take();
			}
			catch (final InterruptedException e) {
				SrcLogger.log("ImageSaver interrupted while waiting for task.");
				break;
			}
			final File outputFile = demo.getBackedFile(task.getSequenceIndex() + "." + task.getExtension());
			if (task.save(outputFile)) {
				demo.notifyFrameSaved(outputFile);
			}
		}
	}
}