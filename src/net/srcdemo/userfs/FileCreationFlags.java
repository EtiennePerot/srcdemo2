package net.srcdemo.userfs;

import net.decasdev.dokan.CreationDisposition;
import fuse.FilesystemConstants;

public enum FileCreationFlags {
	CREATE_ALWAYS, CREATE_NEW, OPEN_ALWAYS, OPEN_EXISTING, TRUNCATE_EXISTING;
	public static FileCreationFlags fromDokan(final int value) {
		switch (value) {
			case CreationDisposition.CREATE_NEW:
				return CREATE_NEW;
			case CreationDisposition.OPEN_ALWAYS:
				return OPEN_ALWAYS;
			case CreationDisposition.OPEN_EXISTING:
				return OPEN_EXISTING;
			case CreationDisposition.CREATE_ALWAYS:
				return CREATE_ALWAYS;
			case CreationDisposition.TRUNCATE_EXISTING:
				return TRUNCATE_EXISTING;
		}
		return null;
	}

	public static FileCreationFlags fromFuse(final int value) {
		if ((value & (FilesystemConstants.O_WRONLY | FilesystemConstants.O_RDWR)) != 0) {
			return CREATE_ALWAYS;
		}
		return OPEN_EXISTING;
	}

	public boolean hasToExist() {
		return shouldOpen() || shouldTruncate();
	}

	public boolean shouldCreate() {
		return equals(CREATE_NEW) || equals(CREATE_ALWAYS) || equals(OPEN_ALWAYS);
	}

	public boolean shouldOpen() {
		return equals(OPEN_ALWAYS) || equals(OPEN_EXISTING);
	}

	public boolean shouldTruncate() {
		return equals(TRUNCATE_EXISTING);
	}
}
