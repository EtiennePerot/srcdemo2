package net.srcdemo.cmd;

import java.io.File;

import net.srcdemo.EnumUtils;
import net.srcdemo.Main;
import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcLogger;
import net.srcdemo.Strings;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.AudioType;
import net.srcdemo.audio.factories.BufferedAudioHandlerFactory;
import net.srcdemo.audio.factories.DiskAudioHandlerFactory;
import net.srcdemo.audio.factories.FlacAudioHandlerFactory;
import net.srcdemo.audio.factories.NullAudioHandlerFactory;
import net.srcdemo.audio.factories.VorbisAudioHandlerFactory;
import net.srcdemo.cmd.Arguments.Category;
import net.srcdemo.userfs.UserFSUtils.DokanNotInstalledException;
import net.srcdemo.userfs.UserFSUtils.DokanVersionException;
import net.srcdemo.video.FrameWeighter;
import net.srcdemo.video.VideoHandlerFactory;
import net.srcdemo.video.VideoType;
import net.srcdemo.video.factories.FrameBlenderVideoHandlerFactory;
import net.srcdemo.video.factories.GaussianFrameWeighter;
import net.srcdemo.video.factories.JPEGSavingFactory;
import net.srcdemo.video.factories.LinearFrameWeighter;
import net.srcdemo.video.factories.NullVideoHandlerFactory;
import net.srcdemo.video.factories.PNGSavingFactory;
import net.srcdemo.video.factories.TGASavingFactory;
import net.srcdemo.video.image.ImageSavingTaskFactory;

import org.apache.commons.lang3.text.WordUtils;

public class SrcDemoCmd {
	private static final int helpEnumArgExtraIndent = 2;
	private static final int helpTableArgumentIndent = 2;
	private static final int helpTableColumnSeparation = 2;
	private static final String linebreakRegex = "[\\r\\n]+";
	public static final int targetTerminalHeight = 30;
	public static final int targetTerminalWidth = 79;

	public static void initError(final Throwable e) {
		if (e instanceof DokanNotInstalledException) {
			System.err.println(Strings.errDokanNotInstalled);
		} else if (e instanceof DokanVersionException) {
			System.err.println(Strings.errInvalidDokan);
		}
	}

	public static void main(final String[] args) {
		final SrcDemoCmd cmd = new SrcDemoCmd(args);
		Runtime.getRuntime().addShutdownHook(new Thread("Unmount shutdown hook") {
			@Override
			public void run() {
				cmd.unmount();
			}
		});
		Main.returnCode(cmd.run());
	}

	private static void printHelp() {
		final String linebreak = System.getProperty("line.separator");
		int maxFormLength = 0;
		int maxDefaultLength = 0;
		for (final Argument arg : Arguments._arguments) {
			maxFormLength = Math.max(maxFormLength, arg.friendlyForm().length());
			maxDefaultLength = Math.max(maxDefaultLength, arg.friendlyDefault() == null ? 0 : arg.friendlyDefault().length());
		}
		maxFormLength += helpTableColumnSeparation;
		maxDefaultLength += helpTableColumnSeparation;
		final int helpDescriptionLength = targetTerminalWidth - maxFormLength - maxDefaultLength - helpTableArgumentIndent;
		final String formatString = new String(new char[helpTableArgumentIndent]).replace('\0', ' ') + "%1$-" + maxFormLength
			+ "s%2$-" + maxDefaultLength + "s";
		final String wrapString = linebreak
			+ new String(new char[helpTableArgumentIndent + maxFormLength + maxDefaultLength]).replace('\0', ' ');
		final String enumIndentString = new String(new char[helpTableArgumentIndent * 3]).replace('\0', ' ');
		final String wrapEnumString = System.getProperty("line.separator") + enumIndentString;
		final String enumExtraIndent = new String(new char[helpEnumArgExtraIndent]).replace('\0', ' ');
		for (final Category category : EnumUtils.iterate(Category.class)) {
			System.out.println(category + ":");
			for (final Argument arg : Arguments._arguments) {
				if (arg.category().equals(category)) {
					System.out.println(String.format(formatString, arg.friendlyForm(),
						arg.friendlyDefault() == null ? "" : arg.friendlyDefault())
						+ (arg.help() == null ? "" : wrap(arg.help(), helpDescriptionLength, linebreak).replaceAll(
							linebreakRegex, wrapString)));
					final String enumHelp = arg.enumHelp();
					if (enumHelp != null && enumHelp.length() != 0) {
						System.out.println(enumIndentString + Strings.cmdEnumPossibleValues);
						System.out.println(enumIndentString
							+ wrap(enumHelp, targetTerminalWidth - enumIndentString.length() - helpEnumArgExtraIndent,
								linebreak + enumExtraIndent).replaceAll(linebreakRegex, wrapEnumString));
					}
				}
			}
			System.out.println(); // Another blank line
		}
	}

	private static String wrap(final String s, final int length, final String addToOtherLines) {
		final String[] lines = s.split(linebreakRegex);
		final StringBuilder out = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			out.append(WordUtils.wrap(lines[i], length, null, true).replaceAll(linebreakRegex, addToOtherLines));
			if (i != lines.length - 1) {
				out.append(System.getProperty("line.separator"));
			}
		}
		return out.toString();
	}

	private final String[] args;
	private StatusDisplay display = null;
	private SrcDemoFS mountedFS = null;

	private SrcDemoCmd(final String[] args) {
		this.args = args;
	}

	private int error(final String message) {
		System.err.println("Error: " + message);
		return 1;
	}

	private boolean isPresent(final Argument arg) {
		return arg.isPresent(args);
	}

	private int run() {
		if (isPresent(Arguments.miscHelp)) {
			printHelp();
			return 0;
		}
		if (isPresent(Arguments.miscVersion)) {
			if (Main.version() == null) {
				System.out.println(Strings.cmdVersionPrefix + Strings.aboutUnknownVersion);
			} else {
				System.out.println(Strings.cmdVersionPrefix + Main.version());
			}
			return 0;
		}
		try {
			if (!isPresent(Arguments.renderMountpoint) || !isPresent(Arguments.renderBackingDir)) {
				return error(Strings.cmdMustIncludeRenderOptions);
			}
			final File mountPoint = new File(Arguments.renderMountpoint.getString(args)).getAbsoluteFile();
			if (!mountPoint.isDirectory()) {
				return error(Strings.errInvalidMountpoint);
			}
			if (mountPoint.list() == null || mountPoint.list().length != 0) {
				return error(Strings.errMountpointNotEmpty);
			}
			final File backingDirectory = new File(Arguments.renderBackingDir.getString(args)).getAbsoluteFile();
			if (!backingDirectory.isDirectory()) {
				return error(Strings.errInvalidBacking);
			}
			if (backingDirectory.equals(mountPoint)) {
				return error(Strings.errDirectoriesEqual);
			}
			boolean videoEnabled = true;
			boolean audioEnabled = true;
			int blendRate = -1;
			VideoHandlerFactory videoFactory = null;
			{
				final VideoType type = Arguments.video.getEnum(VideoType.class, args);
				if (type == null) {
					return error(Strings.errCmdInvalidVideoArgument);
				}
				blendRate = Arguments.videoBlendRate.getInt(args);
				final int shutterAngle = Arguments.videoShutterAngle.getInt(args);
				final ImageSavingTaskFactory imageFactory;
				switch (type) {
					case DISABLED:
						videoFactory = new NullVideoHandlerFactory();
						imageFactory = null;
						videoEnabled = false;
						break;
					case PNG:
						imageFactory = new PNGSavingFactory();
						break;
					case JPEG:
						imageFactory = new JPEGSavingFactory(Arguments.videoJpegQuality.getInt(args));
						break;
					case TGA:
						imageFactory = new TGASavingFactory(Arguments.videoTgaCompression.getBoolean(args));
						break;
					default:
						imageFactory = null;
				}
				final FrameWeighter weighter;
				if (Arguments.videoGaussianVariance.isPresent(args)) {
					weighter = new GaussianFrameWeighter(Arguments.videoGaussianVariance.getDouble(args));
				} else {
					weighter = new LinearFrameWeighter();
				}
				if (videoFactory == null) {
					videoFactory = new FrameBlenderVideoHandlerFactory(imageFactory, blendRate, shutterAngle, weighter);
				}
			}
			AudioHandlerFactory audioFactory = null;
			{
				final AudioType type = Arguments.audio.getEnum(AudioType.class, args);
				if (type == null) {
					return error(Strings.errCmdInvalidAudioArgument);
				}
				audioEnabled = type.requiresBuffer();
				final int bufferSize = Arguments.audioBufferSize.getInt(args);
				final int bufferTimeout = Arguments.audioBufferTimeout.getInt(args);
				switch (type) {
					case DISABLED:
						audioFactory = new NullAudioHandlerFactory();
						break;
					case DISK:
						audioFactory = new DiskAudioHandlerFactory();
						break;
					case BUFFERED:
						audioFactory = new BufferedAudioHandlerFactory(new DiskAudioHandlerFactory(), bufferSize, bufferTimeout);
						break;
					case VORBIS:
						audioFactory = new BufferedAudioHandlerFactory(new VorbisAudioHandlerFactory(
							Arguments.audioVorbisQuality.getInt(args)), bufferSize, bufferTimeout);
						break;
					case FLAC:
						audioFactory = new BufferedAudioHandlerFactory(new FlacAudioHandlerFactory(), bufferSize, bufferTimeout);
						break;
				}
			}
			System.out.println(Strings.cmdGoingToMount);
			System.out.println(mountPoint);
			if (videoEnabled && blendRate != -1) {
				System.out.println(Strings.cmdBlendRate1 + blendRate);
				System.out.println(Strings.cmdBlendRate2);
			}
			System.out.println(Strings.cmdToExit);
			System.out.println(); // Empty line
			final SrcDemoFS mountedFS = new SrcDemoFS(backingDirectory, videoFactory, audioFactory);
			SrcLogger.commandRegisterMountPoint(mountPoint);
			display = new StatusDisplay(videoEnabled, audioEnabled);
			mountedFS.addListener(display);
			display.start();
			mountedFS.mount(mountPoint, true);
			display.interrupt();
			return 0;
		}
		catch (final InvalidFormatArgumentException e) {
			return error(e.toString());
		}
	}

	private void unmount() {
		if (mountedFS != null) {
			if (SrcLogger.getLogMisc()) {
				SrcLogger.log("Unmounting.");
			}
			mountedFS.unmount();
			if (display != null) {
				mountedFS.removeListener(display);
			}
			mountedFS = null;
		}
	}
}
