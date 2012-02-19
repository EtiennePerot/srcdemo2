package net.srcdemo.userfs;

import java.io.File;

import net.decasdev.dokan.ByHandleFileInformation;
import net.decasdev.dokan.FileAttribute;
import net.decasdev.dokan.Win32FindData;

public class FileInfo {
	public static FileInfo fromDirectory(final String fileName) {
		return new FileInfo(fileName, true, 0);
	}

	public static FileInfo fromFile(final String fileName, final long fileSize) {
		return new FileInfo(fileName, false, fileSize);
	}

	private final ByHandleFileInformation byHandleInfo = new ByHandleFileInformation();
	private final String fileName;
	private long fileSize;
	private final int index;
	private final boolean isDirectory;

	public FileInfo(final String fileName, final boolean isDirectory, final long fileSize) {
		this.fileName = fileName;
		this.isDirectory = isDirectory;
		this.fileSize = fileSize;
		index = fileName.hashCode();
	}

	public int getDokanAttributes() {
		int attrib = FileAttribute.FILE_ATTRIBUTE_NORMAL;
		if (isDirectory) {
			attrib |= FileAttribute.FILE_ATTRIBUTE_DIRECTORY;
		}
		return attrib;
	}

	public long getIndexInt() {
		return index;
	}

	public long getIndexLong() {
		return index;
	}

	public long getSize() {
		return fileSize;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public FileInfo setSize(final long size) {
		fileSize = size;
		return this;
	}

	public ByHandleFileInformation toByhandleFileInformation(final int volumeSerialNumber) {
		byHandleInfo.fileAttributes = getDokanAttributes();
		byHandleInfo.creationTime = 0;
		byHandleInfo.lastAccessTime = 0;
		byHandleInfo.lastWriteTime = 0;
		byHandleInfo.volumeSerialNumber = volumeSerialNumber;
		byHandleInfo.fileSize = fileSize;
		byHandleInfo.numberOfLinks = 1;
		byHandleInfo.fileIndex = index;
		return byHandleInfo;
	}

	public Win32FindData toFindData() {
		final String name = new File(fileName).getName();
		return new Win32FindData(getDokanAttributes(), 0, 0, 0, fileSize, 0, 0, name, name);
	}

	@Override
	public String toString() {
		return "FileInfo(fileName=" + fileName + "/isDirectory=" + isDirectory + "/fileSize=" + fileSize + ")";
	}
}
