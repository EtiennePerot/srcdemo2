package net.srcdemo.userfs;

import java.io.File;

final class UnconsequentialFiles {
	private static final String[] unconsequential = { "Thumbs.db", ".DS_STORE", ".Trash", ".Trash-1000", ".xdg-volume-info",
		".directory" };

	static final boolean clearUnconsequentialFiles(final File directory) {
		if (!directory.isDirectory()) {
			return false;
		}
		boolean returnValue = false;
		File f;
		for (final String s : unconsequential) {
			f = new File(directory, s);
			if (f.exists()) {
				returnValue = f.delete() || returnValue;
			}
		}
		return returnValue;
	}
}
