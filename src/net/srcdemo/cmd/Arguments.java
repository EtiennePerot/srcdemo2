package net.srcdemo.cmd;

import java.util.ArrayList;
import java.util.List;

import net.srcdemo.EnumUtils;
import net.srcdemo.cmd.Argument.Type;

public final class Arguments {
	static enum Category {
		AUDIO, COMMON, MISC, RENDER, VIDEO;
		static {
			final Category[] order = { COMMON, RENDER, MISC, VIDEO, AUDIO };
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
	public final static Argument renderBackingDir = new Argument(Category.RENDER, "o", "output", Type.STRING, null,
		"Specify the directory where the final frames and audio will be saved (required in command-line mode).");
	public final static Argument renderMountpoint = new Argument(Category.RENDER, "m", "mountpoint", Type.STRING, null,
		"Specify the directory where the game will save frames (required in command-line mode).");
	public final static Argument serverJvm = Argument.present(null, null, "srcdemo-jvm-server", null);
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
	}
}
