package net.srcdemo.userfs;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;

import net.srcdemo.SrcLogger;
import net.srcdemo.SymlinkResolver;

public abstract class UserFS {
	private final UserFSBackend backend;
	private File mountPoint;

	public UserFS() {
		backend = UserFSUtils.getNewBackend();
	}

	final void _closeFile(final String fileName) {
		log("CloseFile", "Close file: " + fileName);
		closeFile(fileName);
	}

	final void _createDirectory(final String fileName) {
		log("CreateDirectory", "Create directory: " + fileName);
		createDirectory(fileName);
	}

	final boolean _createFile(final String fileName, final FileCreationFlags flags) {
		log("CreateFile", "Create file: " + fileName + " / Creation flags: " + flags);
		return createFile(fileName, flags);
	}

	final void _deleteDirectory(final String fileName) {
		log("DeleteDirectory", "Delete directory: " + fileName);
		deleteDirectory(fileName);
	}

	final void _deleteFile(final String fileName) {
		log("DeleteFile", "Delete file: " + fileName);
		deleteFile(fileName);
	}

	final void _flushFile(final String fileName) {
		log("FlushFileBuffer", "Flush file buffer: " + fileName);
		flushFile(fileName);
	}

	final FileInfo _getFileInfo(final String fileName) {
		log("GetFileInfo", "Getting file info of " + fileName);
		return getFileInfo(fileName);
	}

	final Collection<String> _listDirectory(final String pathName) {
		log("FindFiles", "Find files in: " + pathName);
		return listDirectory(pathName);
	}

	final void _lockFile(final String fileName, final long byteOffset, final long length) {
		log("LockFile", "Lock file: " + fileName);
		lockFile(fileName, byteOffset, length);
	}

	final void _moveFile(final String existingFileName, final String newFileName, final boolean replaceExisiting) {
		log("MoveFile", "Move file: " + existingFileName + " -> " + newFileName);
		moveFile(existingFileName, newFileName, replaceExisiting);
	}

	final int _readFile(final String fileName, final ByteBuffer buffer, final long offset) {
		log("ReadFile", "Read file: " + fileName);
		return readFile(fileName, buffer, offset);
	}

	final void _truncateFile(final String fileName, final long length) {
		log("SetEndOfFile", "Set end of file: " + fileName + " at " + length);
		truncateFile(fileName, length);
	}

	final void _unlockFile(final String fileName, final long byteOffset, final long length) {
		log("UnlockFile", "Unlock file: " + fileName);
		unlockFile(fileName, byteOffset, length);
	}

	final boolean _unmount() {
		log("Unmount", "Unmounting.");
		if (unmount()) {
			return backend.unmount(mountPoint);
		}
		return false;
	}

	final int _writeFile(final String fileName, final ByteBuffer buffer, final long offset) {
		log("WriteFile", "Write file: " + fileName);
		return writeFile(fileName, buffer, offset);
	}

	protected abstract void closeFile(String fileName);

	protected abstract void createDirectory(String fileName);

	protected abstract boolean createFile(String fileName, FileCreationFlags flags);

	protected abstract void deleteDirectory(String fileName);

	protected abstract void deleteFile(String fileName);

	protected void flushFile(final String fileName) {
		// Do nothing
	}

	protected abstract FileInfo getFileInfo(String fileName);

	protected abstract String getFilesystemName();

	protected abstract long getFreeBytes();

	protected int getMaximumComponentLength() {
		return 255;
	}

	protected abstract long getTotalBytes();

	protected abstract long getUsableBytes();

	protected abstract String getVolumeName();

	final void implLog(final String method, final String message) {
		SrcLogger.logFS("(U) " + method + ": " + message);
	}

	protected abstract Collection<String> listDirectory(String pathName);

	protected void lockFile(final String fileName, final long byteOffset, final long length) {
		// Do nothing
	}

	private final void log(final String method, final String message) {
		SrcLogger.logFS(method + ": " + message);
	}

	public final boolean mount(final File mountPoint, final boolean blocking) {
		if (!blocking) {
			new Thread() {
				@Override
				public void run() {
					mount(mountPoint, true);
				}
			}.start();
			return true;
		}
		this.mountPoint = SymlinkResolver.resolveSymlinks(mountPoint);
		log("Mount", "Mounting to: " + this.mountPoint);
		final boolean result = backend.mount(this, mountPoint);
		log("Mount", "Mounting " + (result ? "succeeded" : "failed"));
		return result;
	}

	protected abstract void moveFile(String existingFileName, String newFileName, boolean replaceExisiting);

	protected abstract int readFile(String fileName, ByteBuffer buffer, long offset);

	protected abstract void truncateFile(String fileName, long length);

	protected void unlockFile(final String fileName, final long byteOffset, final long length) {
		// Do nothing
	}

	protected boolean unmount() {
		return true;
	}

	protected abstract int writeFile(String fileName, ByteBuffer buffer, long offset);
}
