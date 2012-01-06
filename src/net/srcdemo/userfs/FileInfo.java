package net.srcdemo.userfs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.decasdev.dokan.ByHandleFileInformation;
import net.decasdev.dokan.FileAttribute;
import net.decasdev.dokan.Win32FindData;

public class FileInfo {
	private static Map<String, Long> fileIndex = new HashMap<String, Long>();
	private static long maxIndex = 2;

	public static FileInfo fromDirectory(final String fileName) {
		return new FileInfo(fileName, true, 0);
	}

	public static FileInfo fromFile(final String fileName, final long fileSize) {
		return new FileInfo(fileName, false, fileSize);
	}

	private final String fileName;
	private final long fileSize;
	private final ReentrantLock indexLock = new ReentrantLock();
	private final boolean isDirectory;

	private FileInfo(final String fileName, final boolean isDirectory, final long fileSize) {
		this.fileName = fileName;
		this.isDirectory = isDirectory;
		this.fileSize = fileSize;
	}

	public int getAttributes() {
		int attrib = FileAttribute.FILE_ATTRIBUTE_NORMAL;
		if (isDirectory) {
			attrib |= FileAttribute.FILE_ATTRIBUTE_DIRECTORY;
		}
		return attrib;
	}

	private long getFileIndex() {
		final String lookup = (isDirectory ? "d" : "f") + fileName;
		indexLock.lock();
		if (!fileIndex.containsKey(lookup)) {
			maxIndex++;
			fileIndex.put(lookup, maxIndex);
		}
		final long ret = fileIndex.get(lookup);
		indexLock.unlock();
		return ret;
	}

	public ByHandleFileInformation toByhandleFileInformation(final int volumeSerialNumber) {
		return new ByHandleFileInformation(getAttributes(), 0, 0, 0, volumeSerialNumber, fileSize, 1, getFileIndex());
	}

	public Win32FindData toFindData() {
		final String name = new File(fileName).getName();
		return new Win32FindData(getAttributes(), 0, 0, 0, fileSize, 0, 0, name, name);
	}

	@Override
	public String toString() {
		return "FileInfo(fileName=" + fileName + "/isDirectory=" + isDirectory + "/fileSize=" + fileSize + ")";
	}
}
