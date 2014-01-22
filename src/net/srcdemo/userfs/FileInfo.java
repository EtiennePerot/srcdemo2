package net.srcdemo.userfs;

import java.io.File;

import net.decasdev.dokan.ByHandleFileInformation;
import net.decasdev.dokan.FileAttribute;
import net.decasdev.dokan.Win32FindData;

public class FileInfo {
    public static FileInfo fromDirectory(final String fileName, long creationTime, long lastAccessTime, long lastWriteTime) {
    	System.out.println("Path: " + fileName);
        return new FileInfo(fileName, true, 0, creationTime, lastAccessTime, lastWriteTime);
    }

    public static FileInfo fromFile(final String fileName, final long fileSize, long creationTime, long lastAccessTime, long lastWriteTime) {
    	System.out.println("Path: " + fileName);
        return new FileInfo(fileName, false, fileSize, creationTime, lastAccessTime, lastWriteTime);
    }

	private final ByHandleFileInformation byHandleInfo = new ByHandleFileInformation();
	private final String fileName;
	private long fileSize;
	private final int index;
	private final boolean isDirectory;
    private final long creationTime;
    private final long lastAccessTime;
    private final long lastWriteTime;

    public FileInfo(final String fileName, final boolean isDirectory, final long fileSize, long creationTime, long lastAccessTime, long lastWriteTime) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.fileSize = fileSize;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastWriteTime = lastWriteTime;
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
        byHandleInfo.creationTime = creationTime;
        byHandleInfo.lastAccessTime = lastAccessTime;
        byHandleInfo.lastWriteTime = lastWriteTime;
		byHandleInfo.volumeSerialNumber = volumeSerialNumber;
		byHandleInfo.fileSize = fileSize;
		byHandleInfo.numberOfLinks = 1;
		byHandleInfo.fileIndex = index;
		return byHandleInfo;
	}

    public Win32FindData toFindData() {
        final String name = new File(fileName).getName();
        return new Win32FindData(getDokanAttributes(), unixLong2Longlong(creationTime), unixLong2Longlong(lastAccessTime), unixLong2Longlong(lastWriteTime), fileSize, 0, 0, name, name);
    }
    
    public static long unixLong2Longlong(long unixTimeMillisec) {
    	long ll = unixTimeMillisec;
    	ll *= 10000L;
    	ll += 116444736000000000L;
    	return ll;
    }

	@Override
	public String toString() {
		return "FileInfo(fileName=" + fileName + "/isDirectory=" + isDirectory + "/fileSize=" + fileSize + ")";
	}
}
