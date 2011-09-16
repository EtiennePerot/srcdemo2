package net.srcdemo.ui;

import com.trolltech.qt.core.QSettings;

public class SrcSettings extends QSettings
{
	SrcSettings()
	{
		super("SrcDemo");
	}

	String getLastBackingDirectory()
	{
		return (String) this.value("backingDirectory", "");
	}

	String getLastMountpoint()
	{
		return (String) this.value("mountpoint", "");
	}

	void setLastBackingDirectory(final String backingDirectory)
	{
		setValue("backingDirectory", backingDirectory);
	}

	void setLastMountpoint(final String mountpoint)
	{
		setValue("mountpoint", mountpoint);
	}
}
