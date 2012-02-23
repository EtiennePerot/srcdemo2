package net.srcdemo;

import net.srcdemo.audio.AudioType;
import net.srcdemo.video.VideoType;

public interface Params {
	static final double audioBufferMemoryPortion = 0.65d;
	static final int audioBufferSizeDef = 1024;
	static final int audioBufferSizeMax = (int) Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory()
		* audioBufferMemoryPortion / 1024L);
	static final int audioBufferSizeMin = 4;
	static final int audioBufferTimeoutDef = 6;
	static final int audioBufferTimeoutMax = 180;
	static final int audioBufferTimeoutMin = 3;
	static final AudioType audioTypeDef = AudioType.BUFFERED;
	static final boolean autoCheckUpdatesDef = false;
	static final String backingDirectoryDef = "";
	static final int blendRateDef = 32;
	static final int blendRateMax = Integer.MAX_VALUE;
	static final int blendRateMin = 1;
	static final int frameRateDef = 30;
	static final int frameRateMax = Integer.MAX_VALUE;
	static final int frameRateMin = 1;
	static final boolean gaussianBlendingDef = false;
	static final double gaussianVarianceDef = 0.150d;
	static final double gaussianVarianceMax = 10d;
	static final double gaussianVarianceMin = 0.001d;
	static final int jpegQualityDef = 95;
	static final int jpegQualityMax = 100;
	static final int jpegQualityMin = 1;
	static final String mountpointDef = "";
	static final boolean previewEnabledDef = true;
	static final String qSettingsName = "SrcDemo";
	static final int shutterAngleDef = 180;
	static final int shutterAngleMax = 360;
	static final int shutterAngleMin = 1;
	static final boolean tgaRleCompressionDef = false;
	static final Object uiGeometry = null;
	static final VideoType videoTypeDef = VideoType.PNG;
	static final int vorbisQualityDef = 8;
	static final int vorbisQualityMax = 10;
	static final int vorbisQualityMin = -2;
}
