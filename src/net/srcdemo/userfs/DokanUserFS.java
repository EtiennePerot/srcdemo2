package net.srcdemo.userfs;

import static net.decasdev.dokan.WinError.ERROR_FILE_NOT_FOUND;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.decasdev.dokan.ByHandleFileInformation;
import net.decasdev.dokan.Dokan;
import net.decasdev.dokan.DokanDiskFreeSpace;
import net.decasdev.dokan.DokanFileInfo;
import net.decasdev.dokan.DokanOperationException;
import net.decasdev.dokan.DokanOperations;
import net.decasdev.dokan.DokanOptions;
import net.decasdev.dokan.DokanVolumeInformation;
import net.decasdev.dokan.Win32FindData;
import net.decasdev.dokan.WinError;

public class DokanUserFS implements DokanOperations, UserFSBackend {
	private static final AtomicInteger volumeSerialNumber = new AtomicInteger(1337);
	private final AtomicLong currentFileHandle = new AtomicLong(0);
	private String mountPoint = null;
	private final int serialNumber = volumeSerialNumber.incrementAndGet();
	private UserFS userFS = null;

	@Override
	public boolean mount(final UserFS userFS, final File mountPoint) {
		this.userFS = userFS;
		this.mountPoint = mountPoint.getAbsolutePath();
		try {
			Dokan.removeMountPoint(this.mountPoint);
		}
		catch (final Throwable e) {
			// Too bad
		}
		final DokanOptions dokanOptions = new DokanOptions(this.mountPoint, 0, 0);
		final int result = Dokan.mount(dokanOptions, this);
		System.out.println(result);
		return result == Dokan.DOKAN_SUCCESS;
	}

	@Override
	public final void onCleanup(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS.implLog("Cleanup", "Clean up: " + fileName);
		// Do nothing
	}

	@Override
	public final void onCloseFile(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._closeFile(fileName);
	}

	@Override
	public final void onCreateDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._createDirectory(fileName);
	}

	@Override
	public final long onCreateFile(final String fileName, final int desiredAccess, final int shareMode,
		final int creationDisposition, final int flagsAndAttributes, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		final FileCreationFlags disposition = FileCreationFlags.fromDokan(creationDisposition);
		if (!userFS._createFile(fileName, disposition)) {
			throw new DokanOperationException(ERROR_FILE_NOT_FOUND);
		}
		return currentFileHandle.incrementAndGet();
	}

	@Override
	public final void onDeleteDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._deleteDirectory(fileName);
	}

	@Override
	public final void onDeleteFile(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._deleteFile(fileName);
	}

	@Override
	public final Win32FindData[] onFindFiles(final String pathName, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		final Collection<String> files = userFS._listDirectory(pathName);
		if (files == null) {
			return null;
		}
		final Win32FindData[] data = new Win32FindData[files.size()];
		int index = 0;
		FileInfo info;
		for (final String s : files) {
			info = userFS._getFileInfo(s);
			if (info != null) {
				data[index] = info.toFindData();
				index++;
			}
		}
		return data;
	}

	@Override
	public final Win32FindData[] onFindFilesWithPattern(final String pathName, final String searchPattern,
		final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS.implLog("FindFilesWithPattern", "Find files in: " + pathName + " with pattern " + searchPattern);
		// Unimplemented
		return null;
	}

	@Override
	public final void onFlushFileBuffers(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._flushFile(fileName);
	}

	@Override
	public final DokanDiskFreeSpace onGetDiskFreeSpace(final DokanFileInfo fileInfo) throws DokanOperationException {
		return new DokanDiskFreeSpace(userFS.getFreeBytes(), userFS.getUsableBytes(), userFS.getTotalBytes());
	}

	@Override
	public final ByHandleFileInformation onGetFileInformation(final String fileName, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		final FileInfo info = userFS._getFileInfo(fileName);
		if (info == null) {
			throw new DokanOperationException(WinError.ERROR_FILE_NOT_FOUND);
		}
		return info.toByhandleFileInformation(serialNumber);
	}

	@Override
	public final DokanVolumeInformation onGetVolumeInformation(final String volumeName, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		userFS.implLog("GetVolumeInformation", "Get volume information");
		return new DokanVolumeInformation(userFS.getVolumeName(), userFS.getFilesystemName(),
			userFS.getMaximumComponentLength(), serialNumber);
	}

	@Override
	public final void onLockFile(final String fileName, final long byteOffset, final long length, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		userFS._lockFile(fileName, byteOffset, length);
	}

	@Override
	public final void onMoveFile(final String existingFileName, final String newFileName, final boolean replaceExisiting,
		final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._moveFile(existingFileName, newFileName, replaceExisiting);
	}

	@Override
	public final long onOpenDirectory(final String fileName, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS.implLog("OpenDirectory", "Open directory: " + fileName);
		return currentFileHandle.incrementAndGet();
	}

	@Override
	public final int onReadFile(final String fileName, final ByteBuffer buffer, final long offset, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		return userFS._readFile(fileName, buffer, offset);
	}

	@Override
	public final void onSetEndOfFile(final String fileName, final long length, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		userFS._truncateFile(fileName, length);
	}

	@Override
	public final void onSetFileAttributes(final String fileName, final int fileAttributes, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		userFS.implLog("SetFileAttributes", "Set file attributes: " + fileName + " to " + fileAttributes);
		// Unimplemented
	}

	@Override
	public final void onSetFileTime(final String fileName, final long creationTime, final long lastAccessTime,
		final long lastWriteTime, final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS.implLog("SetFileTime", "Set file time: " + fileName + " to {C: " + creationTime + "; A: " + lastAccessTime
			+ "; W: " + lastWriteTime + "}");
		// Unimplemented
	}

	@Override
	public final void onUnlockFile(final String fileName, final long byteOffset, final long length, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		userFS._unlockFile(fileName, byteOffset, length);
	}

	@Override
	public final void onUnmount(final DokanFileInfo fileInfo) throws DokanOperationException {
		userFS._unmount();
	}

	@Override
	public final int onWriteFile(final String fileName, final ByteBuffer buffer, final long offset, final DokanFileInfo fileInfo)
		throws DokanOperationException {
		return userFS._writeFile(fileName, buffer, offset);
	}

	@Override
	public boolean unmount(final File mountPoint) {
		return Dokan.removeMountPoint(mountPoint.getAbsolutePath());
	}
}
