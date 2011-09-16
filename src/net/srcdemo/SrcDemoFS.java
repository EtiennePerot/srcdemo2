package net.srcdemo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.decasdev.dokan.CreationDispositionEnum;

public class SrcDemoFS extends LoopbackFS
{
	private static final Pattern demoNamePattern = Pattern.compile("(\\d+)\\.tga$", Pattern.CASE_INSENSITIVE);
	private final int blendRate;
	private final Set<SrcDemoListener> demoListeners = new HashSet<SrcDemoListener>();
	private final Map<String, SrcDemo> demos = new HashMap<String, SrcDemo>();
	private final int shutterAngle;

	public SrcDemoFS(final String backingStorage, final int blendRate, final int shutterAngle)
	{
		super(backingStorage);
		this.blendRate = blendRate;
		this.shutterAngle = shutterAngle;
	}

	public void addListener(final SrcDemoListener listener)
	{
		demoListeners.add(listener);
	}

	@Override
	protected void closeFile(final String fileName)
	{
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			super.closeFile(fileName);
		}
		demo.closeFile(fileName);
	}

	@Override
	protected boolean createFile(final String fileName, final CreationDispositionEnum creation)
	{
		if (!creation.shouldCreate()) {
			return super.createFile(fileName, creation);
		}
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.createFile(fileName, creation);
		}
		demo.createFile(fileName);
		return true;
	}

	private SrcDemo getDemo(final String fileName)
	{
		final Matcher match = demoNamePattern.matcher(fileName);
		if (!match.find()) {
			return null;
		}
		final String demoName = fileName.substring(0, match.start());
		if (!demos.containsKey(demoName)) {
			demos.put(demoName, new SrcDemo(this, getBackingStorage(), demoName, blendRate, shutterAngle));
		}
		return demos.get(demoName);
	}

	@Override
	protected FileInfo getFileInfo(final String fileName)
	{
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.getFileInfo(fileName);
		}
		return demo.getFileInfo(fileName);
	}

	void notifyFrameProcessed(final String frameName)
	{
		for (final SrcDemoListener listener : demoListeners) {
			listener.onFrameProcessed(frameName);
		}
	}

	void notifyFrameSaved(final File savedFrame)
	{
		for (final SrcDemoListener listener : demoListeners) {
			listener.onFrameSaved(savedFrame);
		}
	}

	public void removeListener(final SrcDemoListener listener)
	{
		demoListeners.remove(listener);
	}

	@Override
	protected void truncateFile(final String fileName, final long length)
	{
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			super.truncateFile(fileName, length);
		}
		demo.truncateFile(fileName, length);
	}

	@Override
	protected int writeFile(final String fileName, final ByteBuffer buffer, final long offset)
	{
		final SrcDemo demo = getDemo(fileName);
		if (demo == null) {
			return super.writeFile(fileName, buffer, offset);
		}
		return demo.writeFile(fileName, buffer, offset);
	}
}
