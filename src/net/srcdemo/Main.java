package net.srcdemo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import net.srcdemo.cmd.Arguments;
import net.srcdemo.cmd.SrcDemoCmd;
import net.srcdemo.ui.Files;
import net.srcdemo.ui.SrcDemoUI;
import net.srcdemo.userfs.UserFSUtils;
import net.srcdemo.userfs.UserFSUtils.DokanNotInstalledException;
import net.srcdemo.userfs.UserFSUtils.DokanVersionException;

import org.apache.commons.io.FileUtils;

public final class Main {
	private static final int concurrentRunPort = 63281;
	private static boolean debugMode = false;
	private static boolean isCommandLine = false;
	private static boolean isRunningConcurrently = false;
	private static boolean isServerJvm = true;
	public static final int relaunchStatusCode = 57;
	private static int returnCode = 0;
	private static ServerSocket socket = null;
	private static String version = null;

	private static final void commonInit(final String[] args) throws DokanNotInstalledException, DokanVersionException {
		isCommandLine = Arguments.commonEnableCmd.isPresent(args);
		isServerJvm = !Arguments._clientJvm.isPresent(args);
		if (Arguments.commonDebug.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogAll(true);
		}
		if (Arguments.commonDebugDemo.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogDemo(true);
		}
		if (Arguments.commonDebugVideo.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogVideo(true);
		}
		if (Arguments.commonDebugAudio.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogAudio(true);
		}
		if (Arguments.commonDebugMisc.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogMisc(true);
		}
		if (Arguments.commonDebugFilesystem.isPresent(args)) {
			debugMode = true;
			SrcLogger.setLogFS(true);
		}
		if (Files.versionFile.exists()) {
			try {
				version = FileUtils.readFileToString(Files.versionFile);
			}
			catch (final Exception e) {
				// Consider the version number to be unknown
				version = null;
			}
		}
		UserFSUtils.init();
		try {
			socket = new ServerSocket(concurrentRunPort, 8, InetAddress.getLocalHost());
			if (SrcLogger.getLogMisc()) {
				SrcLogger.log("Bound to port " + concurrentRunPort + "; first instance running.");
			}
		}
		catch (final UnknownHostException e) {
			// Shouldn't happen, but if it does then we're pretty screwed
			SrcLogger.error("Couldn't get localhost address", e);
			System.exit(1);
		}
		catch (final IOException e) {
			// Port taken
			if (SrcLogger.getLogMisc()) {
				SrcLogger.log("Could not bind to port " + concurrentRunPort + "; assuming another instance is running.");
			}
			isRunningConcurrently = true;
		}
	}

	public static final boolean isDebugMode() {
		return debugMode;
	}

	public static final boolean isRunningConcurrently() {
		return isRunningConcurrently;
	}

	public static final boolean isServerJvm() {
		return isServerJvm;
	}

	public static final void main(final String[] args) {
		Throwable initError = null;
		try {
			commonInit(args);
		}
		catch (final Throwable e) {
			initError = e;
			returnCode = 1;
		}
		if (isCommandLine) {
			if (initError == null) {
				SrcDemoCmd.main(args);
			} else {
				SrcDemoCmd.initError(initError);
			}
		} else {
			if (initError == null) {
				SrcDemoUI.main(args);
			} else {
				SrcDemoUI.initError(initError);
			}
		}
		if (socket != null) {
			try {
				socket.close();
			}
			catch (final IOException e) {
				// We don't really care at this point
			}
		}
		System.exit(returnCode);
	}

	public static final void returnCode(final int returnCode) {
		Main.returnCode = returnCode;
	}

	public static final String version() {
		return version;
	}
}
