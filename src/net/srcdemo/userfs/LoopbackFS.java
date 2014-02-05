package net.srcdemo.userfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcLogger;

public class LoopbackFS extends UserFS {
	public static void main(final String... args) {
		if (args.length != 2) {
			System.err.println("Usage: loopbackfs backingDirectory mountpoint");
			System.exit(1);
		}
		SrcLogger.setLogAll(true);
		try {
			UserFSUtils.init();
		}
		catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
		new LoopbackFS(args[0]).mount(new File(args[1]), true);
	}

	private final File backingStorage;
	private final ReentrantLock filesHandleLock = new ReentrantLock();
	private final Map<File, FileChannel> openedFiles = new HashMap<File, FileChannel>();

	protected LoopbackFS(final File backingStorage) {
		this.backingStorage = backingStorage;
		if (!backingStorage.isDirectory()) {
			if (!backingStorage.mkdirs()) {
				System.err.println("Error: Couldn't create directory for storage: " + backingStorage);
				System.exit(1);
			}
		}
	}

	protected LoopbackFS(final String storageFolder) {
		this(new File(storageFolder));
	}

	@Override
	protected void closeFile(final String fileName) {
		filesHandleLock.lock();
		final FileChannel handle = getFileHandle(fileName, false);
		if (handle != null) {
			try {
				handle.close();
			}
			catch (final IOException e) {
				System.err.println("Error while closing handle: " + fileName);
				e.printStackTrace();
			}
			openedFiles.remove(getBackedFile(fileName));
		}
		filesHandleLock.unlock();
	}

	@Override
	protected void createDirectory(final String fileName) {
		getBackedFile(fileName).mkdirs();
	}

	@Override
	protected boolean createFile(final String fileName, final FileCreationFlags creation) {
		if (creation.shouldCreate()) {
			try {
				getBackedFile(fileName).createNewFile();
			}
			catch (final IOException e) {
				System.err.println("Error in file creation: " + fileName);
				e.printStackTrace();
			}
		} else if (creation.hasToExist() && getFileInfo(fileName) == null) {
			return false;
		}
		if (creation.shouldTruncate()) {
			truncateFile(fileName, 0);
		}
		return true;
	}

	@Override
	protected void deleteDirectory(final String fileName) {
		getBackedFile(fileName).delete();
	}

	@Override
	protected void deleteFile(final String fileName) {
		getBackedFile(fileName).delete();
	}

	@Override
	protected void flushFile(final String fileName) {
		// Nothing
	}

	public File getBackedFile(final String subPath) {
		return new File(backingStorage, subPath);
	}

	protected File getBackingStorage() {
		return backingStorage;
	}

	private FileChannel getFileHandle(final File backing, final boolean open) {
		filesHandleLock.lock();
		if (!openedFiles.containsKey(backing)) {
			if (!open || !backing.exists()) {
				filesHandleLock.unlock();
				return null;
			}
			try {
				@SuppressWarnings("resource")
				final FileChannel handle = new RandomAccessFile(backing, "rw").getChannel();
				openedFiles.put(backing, handle);
			}
			catch (final FileNotFoundException e) {
				System.err.println("Unable to open handle to file: " + backing);
				e.printStackTrace();
			}
		}
		final FileChannel handle = openedFiles.get(backing);
		filesHandleLock.unlock();
		return handle;
	}

	private FileChannel getFileHandle(final String fileName, final boolean open) {
		return getFileHandle(getBackedFile(fileName), open);
	}

	@Override
	protected FileInfo getFileInfo(final String fileName) {
		final File backing = getBackedFile(fileName);
		long createTime = 0L;
		long lastAccess = 0L;
		final long lastWrite = backing.lastModified();
		try {
			final BasicFileAttributes attr = Files.readAttributes(backing.toPath(), BasicFileAttributes.class);
			createTime = attr.creationTime().toMillis();
			lastAccess = attr.lastAccessTime().toMillis();
		}
		catch (final IOException e) {
			System.err.println("Failed to obtain file attributes for file: " + backing);
			return null;
		}
		if (backing.isDirectory()) {
			return FileInfo.fromDirectory(fileName, createTime, lastAccess, lastWrite);
		} else if (backing.isFile()) {
			return FileInfo.fromFile(fileName, backing.length(), createTime, lastAccess, lastWrite);
		}
		return null;
	}

	@Override
	protected String getFilesystemName() {
		return "SrcDemo fake filesystem";
	}

	@Override
	protected long getFreeBytes() {
		return backingStorage.getFreeSpace();
	}

	@Override
	protected long getTotalBytes() {
		return backingStorage.getTotalSpace();
	}

	@Override
	protected long getUsableBytes() {
		return backingStorage.getUsableSpace();
	}

	@Override
	protected String getVolumeName() {
		return "SrcDemo";
	}

	@Override
	protected Collection<String> listDirectory(final String pathName) {
		final String[] files = getBackedFile(pathName).list();
		final ArrayList<String> list = new ArrayList<String>(files.length);
		for (final String s : files) {
			list.add(new File(pathName, s).getPath());
		}
		return list;
	}

	@Override
	protected void moveFile(final String existingFileName, final String newFileName, final boolean replaceExisiting) {
		final File newFile = getBackedFile(newFileName);
		if (newFile.exists() && !replaceExisiting) {
			return;
		}
		getBackedFile(existingFileName).renameTo(newFile);
	}

	@Override
	protected int readFile(final String fileName, final ByteBuffer buffer, final long offset) {
		final File backed = getBackedFile(fileName);
		try {
			return getFileHandle(backed, true).read(buffer, offset);
		}
		catch (final Exception e) {
			System.err.println("Error reading file: " + fileName);
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected void truncateFile(final String fileName, final long length) {
		try {
			getFileHandle(fileName, true).truncate(length);
		}
		catch (final IOException e) {
			System.err.println("Error truncating file: " + fileName);
			e.printStackTrace();
		}
	}

	@Override
	protected int writeFile(final String fileName, final ByteBuffer buffer, final long offset) {
		final File backed = getBackedFile(fileName);
		try {
			return getFileHandle(backed, true).write(buffer, offset);
		}
		catch (final IOException e) {
			System.err.println("Error writing file: " + fileName);
			e.printStackTrace();
		}
		return 0;
	}
}
