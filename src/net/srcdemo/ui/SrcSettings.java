package net.srcdemo.ui;

import net.srcdemo.ui.VideoUI.VideoType;

import com.trolltech.qt.core.QSettings;

class SrcSettings extends QSettings
{
	SrcSettings()
	{
		super("SrcDemo");
	}

	boolean getAutoCheckUpdates()
	{
		return Boolean.parseBoolean((String) value("autoCheckUpdates", "false"));
	}

	String getLastBackingDirectory()
	{
		return (String) value("backingDirectory", "");
	}

	int getLastBlendRate()
	{
		return (Integer) value("blendRate", 32);
	}

	int getLastJPEGCompressionLevel()
	{
		return (Integer) value("jpegCompressionLevel", 95);
	}

	String getLastMountpoint()
	{
		return (String) value("mountpoint", "");
	}

	int getLastShutterAngle()
	{
		return (Integer) value("shutterAngle", 180);
	}

	int getLastTargetFps()
	{
		return (Integer) value("targetFps", 30);
	}

	boolean getLastTGACompressionRLE()
	{
		return Boolean.parseBoolean((String) value("tgaCompressionRLE", "false"));
	}

	VideoType getLastVideoType()
	{
		try {
			return VideoType.fromIndex((Integer) value("videoType", VideoType.PNG.getIndex()));
		}
		catch (final Exception e) {
			return VideoType.PNG;
		}
	}

	void setAutoCheckUpdates(final boolean autocheck)
	{
		setValue("autoCheckUpdates", autocheck ? "true" : "false");
	}

	void setLastBackingDirectory(final String backingDirectory)
	{
		setValue("backingDirectory", backingDirectory);
	}

	void setLastBlendRate(final int blendRate)
	{
		setValue("blendRate", blendRate);
	}

	void setLastJPEGCompressionLevel(final int compressionLevel)
	{
		setValue("jpegCompressionLevel", compressionLevel);
	}

	void setLastMountpoint(final String mountpoint)
	{
		setValue("mountpoint", mountpoint);
	}

	void setLastShutterAngle(final int shutterAngle)
	{
		setValue("shutterAngle", shutterAngle);
	}

	void setLastTargetFps(final int targetFps)
	{
		setValue("targetFps", targetFps);
	}

	void setLastTGACompressionRLE(final boolean compressionRLE)
	{
		setValue("tgaCompressionRLE", compressionRLE ? "true" : "false");
	}

	void setLastVideoType(final VideoType videoType)
	{
		setValue("videoType", videoType.getIndex());
	}
}
