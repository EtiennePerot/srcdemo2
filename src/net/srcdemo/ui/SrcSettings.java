package net.srcdemo.ui;

import net.srcdemo.EnumUtils;
import net.srcdemo.ui.AudioUI.AudioType;
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

	int getLastAudioBufferSize()
	{
		return (Integer) value("audioBufferSize", 4096);
	}

	public int getLastAudioBufferTimeout()
	{
		return (Integer) value("audioBufferTimeout", 6);
	}

	AudioType getLastAudioType()
	{
		try {
			return EnumUtils.fromIndex(AudioType.class, ((Integer) value("audioType", EnumUtils.getIndex(AudioType.BUFFERED))));
		}
		catch (final Exception e) {
			return AudioType.BUFFERED;
		}
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
			return EnumUtils.fromIndex(VideoType.class, ((Integer) value("videoType", EnumUtils.getIndex(VideoType.PNG))));
		}
		catch (final Exception e) {
			return VideoType.PNG;
		}
	}

	void setAutoCheckUpdates(final boolean autocheck)
	{
		setValue("autoCheckUpdates", autocheck ? "true" : "false");
	}

	void setLastAudioBufferSize(final int size)
	{
		setValue("audioBufferSize", size);
	}

	void setLastAudioBufferTimeout(final int timeout)
	{
		setValue("audioBufferTimeout", timeout);
	}

	void setLastAudioType(final AudioType audioType)
	{
		setValue("audioType", EnumUtils.getIndex(audioType));
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
		setValue("videoType", EnumUtils.getIndex(videoType));
	}
}
