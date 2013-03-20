package net.srcdemo.ui;

import net.srcdemo.EnumUtils;
import net.srcdemo.Params;
import net.srcdemo.audio.AudioType;
import net.srcdemo.video.VideoType;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QSettings;

class SrcSettings extends QSettings {
	SrcSettings() {
		super(Params.qSettingsName);
	}

	boolean getAutoCheckUpdates() {
		return Boolean.parseBoolean((String) value("autoCheckUpdates", Boolean.toString(Params.autoCheckUpdatesDef)));
	}

	private int getInteger(final String key, final int defaultValue) {
		final Object preference = value(key);
		if (preference instanceof String) {
			return Integer.parseInt((String) preference);
		} else if (preference instanceof Integer) {
			return (Integer) preference;
		}
		return defaultValue;
	}

	int getLastAudioBufferSize() {
		return getInteger("audioBufferSize", Params.audioBufferSizeDef);
	}

	int getLastAudioBufferTimeout() {
		return getInteger("audioBufferTimeout", Params.audioBufferTimeoutDef);
	}

	AudioType getLastAudioType() {
		try {
			return EnumUtils.fromIndex(AudioType.class, getInteger("audioType", EnumUtils.getIndex(Params.audioTypeDef)));
		}
		catch (final Exception e) {
			return Params.audioTypeDef;
		}
	}

	String getLastBackingDirectory() {
		return (String) value("backingDirectory", Params.backingDirectoryDef);
	}

	int getLastBlendRate() {
		return getInteger("blendRate", Params.blendRateDef);
	}

	boolean getLastGaussianBlending() {
		return Boolean.parseBoolean((String) value("gaussianBlending", Boolean.toString(Params.gaussianBlendingDef)));
	}

	double getLastGaussianVariance() {
		return Double.valueOf((String) value("gaussianVariance", Double.toString(Params.gaussianVarianceDef)));
	}

	int getLastJPEGCompressionLevel() {
		return getInteger("jpegCompressionLevel", Params.jpegQualityDef);
	}

	String getLastMountpoint() {
		return (String) value("mountpoint", Params.mountpointDef);
	}

	int getLastShutterAngle() {
		return getInteger("shutterAngle", Params.shutterAngleDef);
	}

	int getLastTargetFps() {
		return getInteger("targetFps", Params.frameRateDef);
	}

	boolean getLastTGACompressionRLE() {
		return Boolean.parseBoolean((String) value("tgaCompressionRLE", Boolean.toString(Params.tgaRleCompressionDef)));
	}

	VideoType getLastVideoType() {
		try {
			return EnumUtils
				.fromIndex(VideoType.class, ((Integer) value("videoType", EnumUtils.getIndex(Params.videoTypeDef))));
		}
		catch (final Exception e) {
			return Params.videoTypeDef;
		}
	}

	int getLastVorbisQuality() {
		return getInteger("vorbisQuality", Params.vorbisQualityDef);
	}

	boolean getPreviewEnabled() {
		return Boolean.parseBoolean((String) value("previewPicture", Boolean.toString(Params.previewEnabledDef)));
	}

	QByteArray getUIGeometry() {
		final Object geometry = value("uiGeometry", Params.uiGeometry);
		if (geometry == null) {
			return null;
		}
		return (QByteArray) geometry;
	}

	void setAutoCheckUpdates(final boolean autocheck) {
		setValue("autoCheckUpdates", autocheck ? "true" : "false");
	}

	void setLastAudioBufferSize(final int size) {
		setValue("audioBufferSize", size);
	}

	void setLastAudioBufferTimeout(final int timeout) {
		setValue("audioBufferTimeout", timeout);
	}

	void setLastAudioType(final AudioType audioType) {
		setValue("audioType", EnumUtils.getIndex(audioType));
	}

	void setLastBackingDirectory(final String backingDirectory) {
		setValue("backingDirectory", backingDirectory);
	}

	void setLastBlendRate(final int blendRate) {
		setValue("blendRate", blendRate);
	}

	void setLastGaussianBlending(final boolean blending) {
		setValue("gaussianBlending", blending ? "true" : "false");
	}

	void setLastGaussianVariance(final double variance) {
		setValue("gaussianVariance", Double.toString(variance));
	}

	void setLastJPEGCompressionLevel(final int compressionLevel) {
		setValue("jpegCompressionLevel", compressionLevel);
	}

	void setLastMountpoint(final String mountpoint) {
		setValue("mountpoint", mountpoint);
	}

	void setLastShutterAngle(final int shutterAngle) {
		setValue("shutterAngle", shutterAngle);
	}

	void setLastTargetFps(final int targetFps) {
		setValue("targetFps", targetFps);
	}

	void setLastTGACompressionRLE(final boolean compressionRLE) {
		setValue("tgaCompressionRLE", compressionRLE ? "true" : "false");
	}

	void setLastVideoType(final VideoType videoType) {
		setValue("videoType", EnumUtils.getIndex(videoType));
	}

	void setLastVorbisQuality(final int quality) {
		setValue("vorbisQuality", quality);
	}

	void setPreviewEnabled(final boolean enabled) {
		setValue("previewPicture", enabled ? "true" : "false");
	}

	void setUIGeometry(final QByteArray settings) {
		setValue("uiGeometry", settings);
	}
}
