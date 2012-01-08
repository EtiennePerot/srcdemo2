package net.srcdemo.userfs;

import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collection;

import net.srcdemo.SrcLogger;

import org.apache.commons.logging.Log;

import com.sun.security.auth.module.UnixSystem;

import fuse.Errno;
import fuse.Filesystem3;
import fuse.FuseDirFiller;
import fuse.FuseException;
import fuse.FuseGetattrSetter;
import fuse.FuseMount;
import fuse.FuseOpenSetter;
import fuse.FuseSizeSetter;
import fuse.FuseStatfsSetter;
import fuse.XattrLister;
import fuse.XattrSupport;

public class FuseUserFS implements Filesystem3, UserFSBackend, XattrSupport {
	private static class NullLogger implements Log {
		@Override
		public void debug(final Object message) {
		}

		@Override
		public void debug(final Object message, final Throwable t) {
		}

		@Override
		public void error(final Object message) {
		}

		@Override
		public void error(final Object message, final Throwable t) {
		}

		@Override
		public void fatal(final Object message) {
		}

		@Override
		public void fatal(final Object message, final Throwable t) {
		}

		@Override
		public void info(final Object message) {
		}

		@Override
		public void info(final Object message, final Throwable t) {
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public boolean isFatalEnabled() {
			return false;
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void trace(final Object message) {
		}

		@Override
		public void trace(final Object message, final Throwable t) {
		}

		@Override
		public void warn(final Object message) {
		}

		@Override
		public void warn(final Object message, final Throwable t) {
		}
	}

	private static final int userGID;
	private static final int userUID;
	static {
		final UnixSystem unix = new UnixSystem();
		userUID = (int) unix.getUid();
		userGID = (int) unix.getGid();
	}
	private String mountPoint = null;
	private UserFS userFS = null;

	@Override
	public int chmod(final String path, final int mode) throws FuseException {
		userFS.implLog("chmod", "Tried to chmod " + path + " to " + mode);
		return 0;
	}

	@Override
	public int chown(final String path, final int uid, final int gid) throws FuseException {
		userFS.implLog("chown", "Tried to chown " + path + " to " + uid + "/" + gid);
		return 0;
	}

	@Override
	public int flush(final String path, final Object fh) throws FuseException {
		userFS._flushFile(path);
		return 0;
	}

	@Override
	public int fsync(final String path, final Object fh, final boolean isDatasync) throws FuseException {
		userFS._flushFile(path);
		return 0;
	}

	@Override
	public int getattr(final String path, final FuseGetattrSetter getattrSetter) throws FuseException {
		final FileInfo info = userFS._getFileInfo(path);
		if (info != null) {
			final long size = info.getSize();
			getattrSetter.set(info.getIndexLong(), info.getUnixMode(), 1, userUID, userGID, 0, size, size, 0, 0, 0);
			return 0;
		}
		return Errno.ENOENT;
	}

	@Override
	public int getdir(final String path, final FuseDirFiller dirFiller) throws FuseException {
		final Collection<String> files = userFS._listDirectory(path);
		if (files == null) {
			return 0;
		}
		FileInfo info;
		for (final String file : files) {
			info = userFS._getFileInfo(file);
			if (info != null) {
				dirFiller.add(new File(file).getName(), info.getIndexLong(), info.getUnixMode());
			}
		}
		return 0;
	}

	@Override
	public int getxattr(final String path, final String name, final ByteBuffer dst, final int position) throws FuseException,
		BufferOverflowException {
		userFS.implLog("getxattr", "Tried to get xattr " + name + " from " + path);
		return 0;
	}

	@Override
	public int getxattrsize(final String path, final String name, final FuseSizeSetter sizeSetter) throws FuseException {
		userFS.implLog("getxattrsize", "Tried to get xattr size " + name + " from " + path);
		return 0;
	}

	@Override
	public int link(final String from, final String to) throws FuseException {
		userFS.implLog("link", "Tried to make link from " + from + " to " + to);
		return 0;
	}

	@Override
	public int listxattr(final String path, final XattrLister lister) throws FuseException {
		userFS.implLog("listxattr", "Tried to list xattrs from on " + path);
		return 0;
	}

	@Override
	public int mkdir(final String path, final int mode) throws FuseException {
		userFS._createDirectory(path);
		return 0;
	}

	@Override
	public int mknod(final String path, final int mode, final int rdev) throws FuseException {
		userFS._createFile(path, FileCreationFlags.CREATE_ALWAYS);
		return 0;
	}

	@Override
	public boolean mount(final UserFS userFS, final File mountPoint) {
		this.userFS = userFS;
		this.mountPoint = mountPoint.getAbsolutePath();
		final String[] args = { this.mountPoint, "-f" };
		try {
			FuseMount.mount(args, this, new NullLogger());
		}
		catch (final Exception e) {
			SrcLogger.error("Failed to mount FUSE filesystem on " + this.mountPoint, e);
			return false;
		}
		return true;
	}

	@Override
	public int open(final String path, final int flags, final FuseOpenSetter openSetter) throws FuseException {
		return userFS._createFile(path, FileCreationFlags.fromFuse(flags)) ? 0 : Errno.EIO;
	}

	@Override
	public int read(final String path, final Object fh, final ByteBuffer buf, final long offset) throws FuseException {
		userFS._readFile(path, buf, offset);
		return 0;
	}

	@Override
	public int readlink(final String path, final CharBuffer link) throws FuseException {
		userFS.implLog("readlink", "Tried to read link " + path);
		return 0;
	}

	@Override
	public int release(final String path, final Object fh, final int flags) throws FuseException {
		userFS._closeFile(path);
		return 0;
	}

	@Override
	public int removexattr(final String path, final String name) throws FuseException {
		userFS.implLog("removexattr", "Tried to remove xattr " + name + " from " + path);
		return 0;
	}

	@Override
	public int rename(final String from, final String to) throws FuseException {
		userFS._moveFile(from, to, true);
		return 0;
	}

	@Override
	public int rmdir(final String path) throws FuseException {
		userFS._deleteDirectory(path);
		return 0;
	}

	@Override
	public int setxattr(final String path, final String name, final ByteBuffer value, final int flags, final int position)
		throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int statfs(final FuseStatfsSetter statfsSetter) throws FuseException {
		statfsSetter.set(1, (int) (userFS.getTotalBytes()), (int) (userFS.getFreeBytes()), (int) (userFS.getUsableBytes()), 1,
			1, userFS.getMaximumComponentLength());
		return 0;
	}

	@Override
	public int symlink(final String from, final String to) throws FuseException {
		userFS.implLog("symlink", "Tried to create a symlink from " + from + " to " + to);
		return 0;
	}

	@Override
	public int truncate(final String path, final long size) throws FuseException {
		userFS._truncateFile(path, size);
		return 0;
	}

	@Override
	public int unlink(final String path) throws FuseException {
		userFS._deleteFile(path);
		return 0;
	}

	@Override
	public boolean unmount(final File mountPoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int utime(final String path, final int atime, final int mtime) throws FuseException {
		userFS.implLog("utime", "Tried to set utime on " + path);
		return 0;
	}

	@Override
	public int write(final String path, final Object fh, final boolean isWritepage, final ByteBuffer buf, final long offset)
		throws FuseException {
		userFS._writeFile(path, buf, offset);
		return 0;
	}
}
