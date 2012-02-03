package net.srcdemo.userfs;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public final class FuseUserFS extends FuseFilesystemAdapterFull implements UserFSBackend {
	private static final long fakeBlockSize = 4096L;
	private UserFS userFS = null;

	@Override
	public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info) {
		userFS._createFile(path, FileCreationFlags.CREATE_ALWAYS);
		return 0;
	}

	@Override
	public void destroy() {
		userFS._onUnmount();
	}

	@Override
	public int flush(final String path, final FileInfoWrapper info) {
		userFS._flushFile(path);
		return 0;
	}

	@Override
	public int getattr(final String path, final StatWrapper stat) {
		final FileInfo info = userFS._getFileInfo(path);
		if (info != null) {
			stat.setMode(info.isDirectory() ? NodeType.DIRECTORY : NodeType.FILE).size(info.getSize());
		}
		return info == null ? -ErrorCodes.ENOENT : 0;
	}

	@Override
	protected String getName() {
		return userFS.getFilesystemName() + "-" + userFS.getVolumeName();
	}

	@Override
	public int mkdir(final String path, final ModeWrapper mode) {
		userFS._createDirectory(path);
		return 0;
	}

	@Override
	public int open(final String path, final FileInfoWrapper info) {
		userFS._createFile(path, FileCreationFlags.OPEN_EXISTING);
		return 0;
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info) {
		return userFS._readFile(path, buffer, offset);
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler) {
		final Collection<String> files = userFS._listDirectory(path);
		if (files != null && !filler.add(files)) {
			return -ErrorCodes.ENOMEM;
		}
		return 0;
	}

	@Override
	public int release(final String path, final FileInfoWrapper info) {
		userFS._closeFile(path);
		return 0;
	}

	@Override
	public int rename(final String path, final String newName) {
		userFS._moveFile(path, newName, true);
		return 0;
	}

	@Override
	public int rmdir(final String path) {
		userFS._deleteDirectory(path);
		return 0;
	}

	@Override
	public int statfs(final String path, final StatvfsWrapper wrapper) {
		wrapper.setBlockInfo(userFS.getFreeBytes() / fakeBlockSize, userFS.getUsableBytes() / fakeBlockSize,
			userFS.getTotalBytes() / fakeBlockSize).setSizes(fakeBlockSize, 0);
		return 0;
	}

	@Override
	public int truncate(final String path, final long offset) {
		userFS._truncateFile(path, offset);
		return 0;
	}

	@Override
	public int unlink(final String path) {
		userFS._deleteFile(path);
		return 0;
	}

	@Override
	public boolean userfs_mount(final UserFS userFS, final File mountPoint) {
		this.userFS = userFS;
		try {
			mount(mountPoint, true);
		}
		catch (final FuseException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean userfs_unmount(final File mountPoint) {
		try {
			unmount();
		}
		catch (final Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
		final FileInfoWrapper info) {
		return userFS._writeFile(path, buf, writeOffset);
	}
}
