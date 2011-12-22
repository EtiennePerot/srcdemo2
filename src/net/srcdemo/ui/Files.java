package net.srcdemo.ui;

import java.io.File;

public interface Files
{
	static final File _toolsDirectory = new File("tools");
	static final File iconAbout = new File("img/icon-92.png");
	static final File iconRenderingDefault = new File("img/icon-92.png");
	static final File iconWindowDebug = new File("img/debug-256.png");
	static final File iconWindowMain = new File("img/icon-512.png");
	static final File libDirectory = new File("lib");
	static final File oggEnc = new File(_toolsDirectory, "oggenc2.exe");
	static final File resolveSymlinks = new File(_toolsDirectory, "resolve-symlinks/resolve-symlinks.exe");
	static final File versionFile = new File("version.txt");
}
