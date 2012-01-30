package net.srcdemo.userfs;

import java.io.File;

public interface UserFSBackend {
	public boolean userfs_mount(UserFS userFS, File mountPoint);

	public boolean userfs_unmount(File mountPoint);
}
