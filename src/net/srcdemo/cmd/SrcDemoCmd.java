package net.srcdemo.cmd;

import java.io.File;

import net.srcdemo.EnumUtils;
import net.srcdemo.Main;
import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcDemoListener;
import net.srcdemo.SrcLogger;
import net.srcdemo.Strings;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.AudioType;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
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

public class SrcDemoCmd implements SrcDemoListener {
	private static final int helpTableArgumentIndent = 2;
	private static final int helpTableColumnSeparation = 4;
	private static final String linebreakRegex = "[\\r\\n]+";
	private static final int targetTerminalWidth = 80;

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
		final int helpDescriptionLength = targetTerminalWidth - maxFormLength - maxDefaultLength;
		final String formatString = new String(new char[helpTableArgumentIndent]).replace('\0', ' ') + "%1$-" + maxFormLength
			+ "s%2$-" + maxDefaultLength + "s";
		final String wrapString = linebreak
			+ new String(new char[helpTableArgumentIndent + maxFormLength + maxDefaultLength]).replace('\0', ' ');
		final String enumIndentString = new String(new char[helpTableArgumentIndent * 3]).replace('\0', ' ');
		final String wrapEnumString = System.getProperty("line.separator") + enumIndentString;
		final int totalLineLength = helpTableArgumentIndent + maxFormLength + maxDefaultLength + helpDescriptionLength;
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
							+ wrap(enumHelp, totalLineLength - enumIndentString.length(), linebreak + "  ").replaceAll(
								linebreakRegex, wrapEnumString));
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

	@Override
	public void onAudioBuffer(final AudioBufferStatus status, final int occupied, final int total) {
	}

	@Override
	public void onFrameProcessed(final String frameName) {
		System.out.println("Frame processed: " + frameName);
	}

	@Override
	public void onFrameSaved(final File savedFrame, final int[] pixels, final int width, final int height) {
		// TODO Do cool stuff
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
			VideoHandlerFactory videoFactory = null;
			{
				final VideoType type = Arguments.video.getEnum(VideoType.class, args);
				if (type == null) {
					return error(Strings.errCmdInvalidVideoArgument);
				}
				final int blendRate = Arguments.videoBlendRate.getInt(args);
				final int shutterAngle = Arguments.videoShutterAngle.getInt(args);
				final ImageSavingTaskFactory imageFactory;
				switch (type) {
					case DISABLED:
						videoFactory = new NullVideoHandlerFactory();
						imageFactory = null;
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
			final SrcDemoFS mountedFS = new SrcDemoFS(backingDirectory, videoFactory, audioFactory);
			mountedFS.addListener(this);
			SrcLogger.commandRegisterMountPoint(mountPoint);
			mountedFS.mount(mountPoint, true);
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
			mountedFS.removeListener(this);
			mountedFS = null;
		}
	}
}
