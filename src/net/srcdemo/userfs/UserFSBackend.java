package net.srcdemo.userfs;

import java.io.File;

public interface UserFSBackend {
	public boolean mount(UserFS userFS, File mountPoint);

	public boolean unmount(File mountPoint);
}
