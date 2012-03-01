package net.srcdemo.ui;

import java.io.File;

public interface Files {
	static final File _imgDirectory = new File("img");
	static final File _libDirectory = new File("lib");
	static final File _toolsDirectory = new File("tools");
	static final File _toolsDirectoryOSX = new File(_toolsDirectory, "osx");
	static final File _toolsDirectoryWindows = new File(_toolsDirectory, "windows");
	static final File iconAbout = new File(_imgDirectory, "logo-92.png");
	static final File iconRenderingDefault = new File(_imgDirectory, "logo-92.png");
	static final File iconWindowDebug = new File(_imgDirectory, "debug-256.png");
	static final File iconWindowMain = new File(_imgDirectory, "icon-512.png");
	static final File libDirectoryLinux32 = new File(_libDirectory, "linux32");
	static final File libDirectoryLinux64 = new File(_libDirectory, "linux64");
	static final File libDirectoryOSX = new File(_libDirectory, "osx");
	static final File libDirectoryWindows = new File(_libDirectory, "windows");
	static final File oggEncWindows = new File(_toolsDirectoryWindows, "oggenc2/oggenc2.exe");
	static final File resolveSymlinksWindows = new File(_toolsDirectoryWindows, "resolve-symlinks/resolve-symlinks.exe");
	static final File secretStoatImage = new File(_imgDirectory, "stoat.png");
	static final File versionFile = new File("version.txt");
}
