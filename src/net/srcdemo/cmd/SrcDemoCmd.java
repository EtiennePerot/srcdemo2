package net.srcdemo.cmd;

import java.io.File;

import net.srcdemo.EnumUtils;
import net.srcdemo.Main;
import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcDemoListener;
import net.srcdemo.SrcLogger;
import net.srcdemo.Strings;
import net.srcdemo.audio.BufferedAudioHandler.AudioBufferStatus;
import net.srcdemo.cmd.Arguments.Category;
import net.srcdemo.userfs.UserFSUtils.DokanNotInstalledException;
import net.srcdemo.userfs.UserFSUtils.DokanVersionException;

import org.apache.commons.lang3.text.WordUtils;

public class SrcDemoCmd implements SrcDemoListener {
	private static final int helpDescriptionLength = 64;
	private static final int helpTableArgumentIndent = 2;
	private static final int helpTableColumnSeparation = 3;

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
		int maxFormLength = 0;
		int maxDefaultLength = 0;
		for (final Argument arg : Arguments._arguments) {
			maxFormLength = Math.max(maxFormLength, arg.friendlyForm().length());
			maxDefaultLength = Math.max(maxDefaultLength, arg.defaultValue() == null ? 0 : arg.defaultValue().length());
		}
		maxFormLength += helpTableColumnSeparation;
		maxDefaultLength += helpTableColumnSeparation;
		final String formatString = new String(new char[helpTableArgumentIndent]).replace('\0', ' ') + "%1$-" + maxFormLength
			+ "s%2$-" + maxDefaultLength + "s";
		final String wrapString = System.getProperty("line.separator")
			+ new String(new char[helpTableArgumentIndent + maxFormLength + maxDefaultLength]).replace('\0', ' ');
		for (final Category category : EnumUtils.iterate(Category.class)) {
			System.out.println(category + ":");
			for (final Argument arg : Arguments._arguments) {
				if (arg.category().equals(category)) {
					System.out.println(String.format(formatString, arg.friendlyForm(),
						arg.defaultValue() == null ? "" : arg.defaultValue())
						+ (arg.help() == null ? "" : WordUtils.wrap(arg.help(), helpDescriptionLength, wrapString, true)));
				}
			}
			System.out.println(""); // Another blank line
		}
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
		final SrcDemoFS mountedFS = new SrcDemoFS(backingDirectory, null, null);
		mountedFS.addListener(this);
		SrcLogger.commandRegisterMountPoint(mountPoint);
		mountedFS.mount(mountPoint, true);
		return 0;
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
