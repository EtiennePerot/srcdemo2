package net.srcdemo;

import static net.decasdev.dokan.WinError.ERROR_FILE_NOT_FOUND;

import java.nio.ByteBuffer;
import java.util.Collection;

import net.decasdev.dokan.ByHandleFileInformation;
import net.decasdev.dokan.CreationDispositionEnum;
import net.decasdev.dokan.Dokan;
import net.decasdev.dokan.DokanDiskFreeSpace;
import net.decasdev.dokan.DokanFileInfo;
import net.decasdev.dokan.DokanOperationException;
import net.decasdev.dokan.DokanOperations;
import net.decasdev.dokan.DokanOptions;
import net.decasdev.dokan.DokanVolumeInformation;
import net.decasdev.dokan.Win32FindData;
import net.decasdev.dokan.WinError;

public abstract class DokanFSStub implements DokanOperations
{
	private static final int volumeSerialNumber = 1337;
	private long currentFileHandle = 1;
	private boolean logging = true;
	private String mountPoint;

	protected abstract void closeFile(String fileName);

	protected abstract void createDirectory(String fileName);

	protected abstract boolean createFile(String fileName, CreationDispositionEnum creation);

	protected abstract void deleteDirectory(String fileName);

	protected abstract void deleteFile(String fileName);

	protected abstract Collection<String> findFiles(String pathName);

	protected void flushFileBuffer(final String fileName)
	{
		// Do nothing
	}

	protected abstract FileInfo getFileInfo(String fileName);

	protected String getFilesystemName()
	{
		return "Dokan filesystem";
	}

	protected abstract long getFreeBytes();

	protected int getMaximumComponentLength()
	{
		return 255;
	}

	private long getNextFileHandle()
	{
		return currentFileHandle++;
	}

	protected int getSerialNumber()
	{
		return volumeSerialNumber;
	}

	protected abstract long getTotalBytes();

	protected long getUsableBytes()
	{
		return getFreeBytes();
	}

	protected String getVolumeName()
	{
		return "Dokan volume";
	}

	protected void lockFile(final String fileName, final long byteOffset, final long length)
	{
		// Do nothing
	}

	private void log(final String method, final String message)
	{
		if (logging) {
			System.out.println("[" + method + "] " + message);
		}
	}

	public void mount(final String mountPoint)
	{
		final DokanFSStub oldThis = this;
		new Thread("Dokan filesystem thread")
		{
			@Override
			public void run()
			{
				oldThis.mountPoint = mountPoint;
				final DokanOptions dokanOptions = new DokanOptions(mountPoint, 0, 0);
				final int result = Dokan.mount(dokanOptions, oldThis);
				log("Mount", "Mounting result: " + result);
			}
		}.start();
	}

	protected abstract void moveFile(String existingFileName, String newFileName, boolean replaceExisiting);

	@Override
	public void onCleanup(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("Cleanup", "Clean up: " + fileName);
	}

	@Override
	public void onCloseFile(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("CloseFile", "Close file: " + fileName);
		closeFile(fileName);
	}

	@Override
	public void onCreateDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("CreateDirectory", "Create directory: " + fileName);
		createDirectory(fileName);
	}

	@Override
	public long onCreateFile(final String fileName, final int desiredAccess, final int shareMode,
			final int creationDisposition, final int flagsAndAttributes, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		final CreationDispositionEnum disposition = CreationDispositionEnum.fromInt(creationDisposition);
		log("CreateFile", "Create file: " + fileName + " / Creation disposition: " + disposition);
		if (!createFile(fileName, disposition)) {
			throw new DokanOperationException(ERROR_FILE_NOT_FOUND);
		}
		final long handle = getNextFileHandle();
		log("CreateFile", "Returning handle: " + handle);
		return handle;
	}

	@Override
	public void onDeleteDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("DeleteDirectory", "Delete directory: " + fileName);
		deleteDirectory(fileName);
	}

	@Override
	public void onDeleteFile(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("DeleteFile", "Delete file: " + fileName);
		deleteFile(fileName);
	}

	@Override
	public Win32FindData[] onFindFiles(final String pathName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("FindFiles", "Find files in: " + pathName);
		final Collection<String> files = findFiles(pathName);
		final Win32FindData[] data = new Win32FindData[files.size()];
		int index = 0;
		for (final String s : files) {
			final FileInfo info = getFileInfo(s);
			if (info != null) {
				data[index] = info.toFindData();
				index++;
			}
		}
		return data;
	}

	@Override
	public Win32FindData[] onFindFilesWithPattern(final String pathName, final String searchPattern,
			final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("FindFilesWithPattern", "Find files in: " + pathName + " with pattern " + searchPattern);
		final Win32FindData[] data = new Win32FindData[0];
		return data;
	}

	@Override
	public void onFlushFileBuffers(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("FlushFileBuffer", "Flush file buffer: " + fileName);
		flushFileBuffer(fileName);
	}

	@Override
	public DokanDiskFreeSpace onGetDiskFreeSpace(final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("GetDiskFreeSpace", "Get disk free space");
		return new DokanDiskFreeSpace(getFreeBytes(), getTotalBytes());
	}

	@Override
	public ByHandleFileInformation onGetFileInformation(final String fileName, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("GetFileInformation", "Get file information: " + fileName);
		final FileInfo info = getFileInfo(fileName);
		if (info == null) {
			throw new DokanOperationException(WinError.ERROR_FILE_NOT_FOUND);
		}
		return info.toByhandleFileInformation(getSerialNumber());
	}

	@Override
	public DokanVolumeInformation onGetVolumeInformation(final String volumeName, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("GetVolumeInformation", "Get volume information");
		return new DokanVolumeInformation(getVolumeName(), getFilesystemName(), getMaximumComponentLength(), getSerialNumber());
	}

	@Override
	public void onLockFile(final String fileName, final long byteOffset, final long length, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("LockFile", "Lock file: " + fileName);
		lockFile(fileName, byteOffset, length);
	}

	@Override
	public void onMoveFile(final String existingFileName, final String newFileName, final boolean replaceExisiting,
			final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("MoveFile", "Move file: " + existingFileName + " -> " + newFileName);
		moveFile(existingFileName, newFileName, replaceExisiting);
	}

	@Override
	public long onOpenDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("OpenDirectory", "Open directory: " + fileName);
		return getNextFileHandle();
	}

	@Override
	public int onReadFile(final String fileName, final ByteBuffer buffer, final long offset, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("ReadFile", "Read file: " + fileName);
		return readFile(fileName, buffer, offset);
	}

	@Override
	public void onSetEndOfFile(final String fileName, final long length, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("SetEndOfFile", "Set end of file: " + fileName + " at " + length);
		// truncateFile(fileName, length);
	}

	@Override
	public void onSetFileAttributes(final String fileName, final int fileAttributes, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("SetFileAttributes", "Set file attributes: " + fileName + " to " + fileAttributes);
	}

	@Override
	public void onSetFileTime(final String fileName, final long creationTime, final long lastAccessTime,
			final long lastWriteTime, final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("SetFileTime", "Set file time: " + fileName + " to {C: " + creationTime + "; A: " + lastAccessTime + "; W: "
				+ lastWriteTime + "}");
	}

	@Override
	public void onUnlockFile(final String fileName, final long byteOffset, final long length, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("UnlockFile", "Unlock file: " + fileName);
		unlockFile(fileName, byteOffset, length);
	}

	@Override
	public void onUnmount(final DokanFileInfo fileInfo) throws DokanOperationException
	{
		log("Unmount", "Unmounting.");
	}

	@Override
	public int onWriteFile(final String fileName, final ByteBuffer buffer, final long offset, final DokanFileInfo fileInfo)
			throws DokanOperationException
	{
		log("WriteFile", "Write file: " + fileName);
		return writeFile(fileName, buffer, offset);
	}

	protected abstract int readFile(String fileName, ByteBuffer buffer, long offset);

	protected void setLogging(final boolean enabled)
	{
		logging = enabled;
	}

	protected abstract void truncateFile(String fileName, long length);

	protected void unlockFile(final String fileName, final long byteOffset, final long length)
	{
		// Do nothing
	}

	public void unmount()
	{
		Dokan.removeMountPoint(mountPoint);
	}

	protected abstract int writeFile(String fileName, ByteBuffer buffer, long offset);
}
