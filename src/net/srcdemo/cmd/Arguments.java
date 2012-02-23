package net.srcdemo.cmd;

import java.util.ArrayList;
import java.util.List;

import net.srcdemo.EnumUtils;
import net.srcdemo.Params;
import net.srcdemo.audio.AudioType;
import net.srcdemo.cmd.Argument.Type;
import net.srcdemo.video.VideoType;

public final class Arguments {
	static enum Category {
		AUDIO, COMMON, MISC, RENDER, VIDEO;
		static {
			final Category[] order = { RENDER, MISC, COMMON, VIDEO, AUDIO };
			EnumUtils.registerOrder(Category.class, order);
		}

		@Override
		public String toString() {
			switch (this) {
				case AUDIO:
					return "Audio-related arguments";
				case COMMON:
					return "Common arguments (works in both graphical and command-line mode)";
				case MISC:
					return "Miscellaneous arguments";
				case VIDEO:
					return "Video-related arguments";
				case RENDER:
					return "Rendering arguments";
			}
			return null;
		}
	}

	final static List<Argument> _arguments = new ArrayList<Argument>();
	public final static Argument _clientJvm = Argument.present(null, null, "srcdemo-jvm-client", null);
	public static final Argument audio = Argument.createEnum(Category.AUDIO, "a", "audio", Params.audioTypeDef,
		"Set the audio output format.", AudioType.class);
	public static final Argument audioBufferSize = Argument.rangedInt(Category.AUDIO, null, "audio-bufsize",
		Params.audioBufferSizeDef, Params.audioBufferSizeMin, Params.audioBufferSizeMax,
		"Size of the audio buffer, in kilobytes.");
	public static final Argument audioBufferTimeout = Argument.rangedInt(Category.AUDIO, null, "audio-buftime",
		Params.audioBufferTimeoutDef, Params.audioBufferTimeoutMin, Params.audioBufferTimeoutMax,
		"Time before the audio buffer automatically flushes, in seconds.");
	public static final Argument audioVorbisQuality = Argument
		.rangedInt(Category.AUDIO, null, "vorbis-quality", Params.vorbisQualityDef, Params.vorbisQualityMin,
			Params.vorbisQualityMax, "Set the quality used for the vorbis codec.");
	public final static Argument commonDebug = Argument.present(Category.COMMON, null, "srcdemo-debug",
		"Display all debug messages.");
	public final static Argument commonDebugAudio = Argument.present(Category.COMMON, null, "srcdemo-debug-audio",
		"Display audio-related debug messages.");
	public final static Argument commonDebugDemo = Argument.present(Category.COMMON, null, "srcdemo-debug-demo",
		"Display demo-related debug messages.");
	public final static Argument commonDebugFilesystem = Argument.present(Category.COMMON, null, "srcdemo-debug-fs",
		"Display filesystem-related debug messages.");
	public final static Argument commonDebugMisc = Argument.present(Category.COMMON, null, "srcdemo-debug-misc",
		"Display miscellaneous debug messages.");
	public final static Argument commonDebugVideo = Argument.present(Category.COMMON, null, "srcdemo-debug-video",
		"Display video-related debug messages.");
	public final static Argument commonEnableCmd = Argument.present(Category.COMMON, null, "srcdemo-cmd",
		"Turn on command-line mode.");
	public final static Argument miscHelp = Argument.present(Category.MISC, "h", "help", "Displays this help message");
	public final static Argument miscVersion = Argument.present(Category.MISC, "v", "version",
		"Prints this build's version number.");
	public final static Argument renderBackingDir = Argument.create(Category.RENDER, "o", "output", Type.STRING, null,
		"Specify the directory where the final frames and audio will be saved (required in command-line mode).");
	public final static Argument renderMountpoint = Argument.create(Category.RENDER, "m", "mountpoint", Type.STRING, null,
		"Specify the directory where the game will save frames (required in command-line mode).");
	public final static Argument serverJvm = Argument.present(null, null, "srcdemo-jvm-server", null);
	public static final Argument video = Argument.createEnum(Category.VIDEO, "v", "video", Params.videoTypeDef,
		"Set the video output format.", VideoType.class);
	public static final Argument videoBlendRate = Argument.rangedInt(Category.VIDEO, "b", "blendrate", Params.blendRateDef, 1,
		Integer.MAX_VALUE, "The frame blending rate, in frames per frame.");
	public static final Argument videoGaussianVariance = Argument.rangedDouble(Category.VIDEO, null, "gaussian",
		Params.gaussianVarianceDef, Params.gaussianVarianceMin, Params.gaussianVarianceMax,
		"The variance to use for Gaussian blending. Do not specify if you do not want gaussian blending to be used.");
	public static final Argument videoJpegQuality = Argument.rangedInt(Category.VIDEO, null, "jpeg-quality",
		Params.jpegQualityDef, Params.jpegQualityMin, Params.jpegQualityMax,
		"The JPEG quality to use, from 1 (worst) to 100 (best).");
	public static final Argument videoShutterAngle = Argument.rangedInt(Category.VIDEO, "s", "shutter", Params.shutterAngleDef,
		Params.shutterAngleMin, Params.shutterAngleMax, "The shutter angle, in degrees.");
	public static final Argument videoTgaCompression = Argument.create(Category.VIDEO, null, "tga-rle", Type.BOOLEAN,
		Params.tgaRleCompressionDef, "Whether to use RLE compression in TGA files.");
	static {
		{
			_arguments.add(miscHelp);
			_arguments.add(miscVersion);
		}
		{
			_arguments.add(renderMountpoint);
			_arguments.add(renderBackingDir);
		}
		{
			_arguments.add(commonEnableCmd);
			_arguments.add(commonDebug);
			_arguments.add(commonDebugDemo);
			_arguments.add(commonDebugVideo);
			_arguments.add(commonDebugAudio);
			_arguments.add(commonDebugFilesystem);
			_arguments.add(commonDebugMisc);
		}
		{
			_arguments.add(video);
			_arguments.add(videoBlendRate);
			_arguments.add(videoShutterAngle);
			_arguments.add(videoJpegQuality);
			_arguments.add(videoTgaCompression);
			_arguments.add(videoGaussianVariance);
		}
		{
			_arguments.add(audio);
			_arguments.add(audioBufferSize);
			_arguments.add(audioBufferTimeout);
			_arguments.add(audioVorbisQuality);
		}
	}
}
