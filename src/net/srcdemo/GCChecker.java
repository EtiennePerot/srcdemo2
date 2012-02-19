package net.srcdemo;

public final class GCChecker extends Thread {
	private static final long gcCheckInterval = 600000;
	private static final long gcPokedCheckInterval = 30000;
	private static final double gcThreshold = 0.25;
	private static final GCChecker instance = new GCChecker();

	public static final void poke() {
		instance.interrupt();
	}

	private long lastGC = 0L;

	private GCChecker() {
		super("Garbage collector checker");
		setDaemon(true);
		start();
	}

	@Override
	public final void run() {
		final Runtime runtime = Runtime.getRuntime();
		long now;
		while (true) {
			now = System.currentTimeMillis();
			if (now - lastGC > gcPokedCheckInterval
				&& (double) runtime.freeMemory() / (double) runtime.totalMemory() < gcThreshold) {
				System.gc();
				lastGC = now;
			}
			try {
				Thread.sleep(gcCheckInterval);
			}
			catch (final InterruptedException e) {
				// Loop
			}
		}
	}
}
