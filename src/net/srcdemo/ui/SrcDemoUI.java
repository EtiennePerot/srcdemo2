package net.srcdemo.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcLogger;
import net.srcdemo.userfs.UserFSUtils;
import net.srcdemo.userfs.UserFSUtils.DokanNotInstalledException;
import net.srcdemo.userfs.UserFSUtils.DokanVersionException;

import org.apache.commons.io.FileUtils;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFocusEvent;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SrcDemoUI extends QWidget {
	private static final int concurrentRunPort = 63281;
	private static boolean debugMode = false;
	private static boolean isRunningConcurrently = false;
	private static final int relaunchStatusCode = 1337;
	private static int returnCode = 0;
	private static String version = null;

	public static String getVersion() {
		return version;
	}

	public static void main(final String[] args) {
		for (final String arg : args) {
			if (arg.equals(Strings.cmdFlagDebugMode)) {
				debugMode = true;
				SrcLogger.setLogAll(true);
			}
			if (arg.equals(Strings.cmdFlagDebugAudio)) {
				debugMode = true;
				SrcLogger.setLogAudio(true);
			}
			if (arg.equals(Strings.cmdFlagDebugVideo)) {
				debugMode = true;
				SrcLogger.setLogVideo(true);
			}
			if (arg.equals(Strings.cmdFlagDebugDemo)) {
				debugMode = true;
				SrcLogger.setLogDemo(true);
			}
			if (arg.equals(Strings.cmdFlagDebugMisc)) {
				debugMode = true;
				SrcLogger.setLogMisc(true);
			}
			if (arg.equals(Strings.cmdFlagDebugFS)) {
				debugMode = true;
				SrcLogger.setLogFS(true);
			}
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
		QApplication.initialize(args);
		QCoreApplication.setApplicationName(Strings.productName);
		if (version != null) {
			QCoreApplication.setApplicationVersion(version);
		}
		boolean initialized = false;
		try {
			initialized = UserFSUtils.init();
		}
		catch (final DokanNotInstalledException e) {
			new UserFSMessage(Strings.errDokanNotInstalled).show();
		}
		catch (final DokanVersionException e) {
			new UserFSMessage(Strings.errInvalidDokan).show();
		}
		ServerSocket socket = null;
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
		if (initialized) {
			final SrcDemoUI ui = new SrcDemoUI(QApplication.instance());
			Runtime.getRuntime().addShutdownHook(new Thread("Unmount shutdown hook") {
				@Override
				public void run() {
					ui.flushAudioBuffer(true);
					ui.unmount();
				}
			});
			QApplication.exec();
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

	private QTabWidget allTabs;
	private final QApplication application;
	private AudioUI audioUi;
	private QLineEdit backingDirectory;
	private QPushButton backingDirectoryBrowse;
	private QPushButton btnExit;
	private QPushButton btnMount;
	private final Set<QWidget> disablableWidgets = new HashSet<QWidget>();
	private final ReentrantLock fsLock = new ReentrantLock();
	private QLabel lblStatus;
	private SrcDemoFS mountedFS = null;
	private QLineEdit mountpoint;
	private QPushButton mountpointBrowse;
	private RenderingTab renderTab;
	private final SrcSettings settings;
	private VideoUI videoUi;

	SrcDemoUI(final QApplication application) {
		this.application = application;
		setWindowTitle(Strings.productName + (getVersion() == null ? "" : Strings.titleBuildPrefix + getVersion()));
		final QIcon icon;
		if (debugMode) {
			icon = new QIcon(Files.iconWindowDebug.getAbsolutePath());
		} else {
			icon = new QIcon(Files.iconWindowMain.getAbsolutePath());
		}
		// Need to do both in order for it to work on OS X
		QApplication.setWindowIcon(icon);
		setWindowIcon(icon);
		settings = new SrcSettings();
		// Attempt unmount in case something went bad last time
		final String lastMountPoint = settings.getLastMountpoint();
		if (!isRunningConcurrently && lastMountPoint != null && !lastMountPoint.isEmpty()) {
			SrcLogger.commandUnmount(new File(lastMountPoint));
		}
		// Build a dummy QMenuBar to make the OS X guys happy
		@SuppressWarnings("unused")
		final QMenuBar dummyBar = new QMenuBar();
		initUI();
		final QByteArray geometry = settings.getUIGeometry();
		if (geometry != null) {
			restoreGeometry(geometry);
		}
		show();
	}

	private String badSettings() {
		if (backingDirectory.text().length() == 0 || getBackingDirectory() == null || !getBackingDirectory().isDirectory()) {
			return Strings.errInvalidBacking;
		}
		if (mountpoint.text().length() == 0 || getMountpoint() == null || !getMountpoint().isDirectory()) {
			return Strings.errInvalidMountpoint;
		}
		if (getMountpoint().list() == null || getMountpoint().list().length != 0) {
			return Strings.errMountpointNotEmpty;
		}
		if (getBackingDirectory().equals(getMountpoint())) {
			return Strings.errDirectoriesEqual;
		}
		return null;
	}

	@Override
	protected void closeEvent(final QCloseEvent event) {
		settings.setUIGeometry(saveGeometry());
		super.closeEvent(event);
	}

	private QWidget disablableWidget(final QWidget widget) {
		disablableWidgets.add(widget);
		return widget;
	}

	void exit(final int returnCode) {
		SrcDemoUI.returnCode = returnCode;
		unmount();
		close();
	}

	void flushAudioBuffer(final boolean block) {
		fsLock.lock();
		if (mountedFS != null) {
			mountedFS.flushAudioBuffer(block);
		}
		fsLock.unlock();
	}

	@Override
	protected void focusInEvent(final QFocusEvent event) {
		super.focusInEvent(event);
		System.out.println("In");
	}

	@Override
	protected void focusOutEvent(final QFocusEvent event) {
		super.focusOutEvent(event);
		System.out.println("Out");
	}

	private File getBackingDirectory() {
		return new File(backingDirectory.text());
	}

	private File getMountpoint() {
		return new File(mountpoint.text());
	}

	QApplication getQApplication() {
		return application;
	}

	SrcSettings getSettings() {
		return settings;
	}

	private void initUI() {
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			vbox.addWidget(disablableWidget(new QLabel(Strings.step1)));
			final QHBoxLayout hbox = new QHBoxLayout();
			mountpoint = new QLineEdit(settings.getLastMountpoint());
			mountpoint.textChanged.connect(this, "updateStatus()");
			hbox.addWidget(disablableWidget(mountpoint));
			mountpointBrowse = new QPushButton(Strings.btnBrowse);
			mountpointBrowse.clicked.connect(this, "onBrowseMountpoint()");
			hbox.addWidget(disablableWidget(mountpointBrowse));
			vbox.addLayout(hbox);
		}
		{
			vbox.addWidget(disablableWidget(new QLabel(Strings.step2)));
			final QHBoxLayout hbox = new QHBoxLayout();
			backingDirectory = new QLineEdit(settings.getLastBackingDirectory());
			backingDirectory.textChanged.connect(this, "updateStatus()");
			hbox.addWidget(disablableWidget(backingDirectory));
			backingDirectoryBrowse = new QPushButton(Strings.btnBrowse);
			backingDirectoryBrowse.clicked.connect(this, "onBrowseBackingDirectory()");
			hbox.addWidget(disablableWidget(backingDirectoryBrowse));
			vbox.addLayout(hbox);
		}
		{
			vbox.addWidget(disablableWidget(new QLabel(Strings.step3)));
			allTabs = new QTabWidget();
			{
				videoUi = new VideoUI(this);
				allTabs.addTab(videoUi, Strings.tabVideo);
			}
			{
				audioUi = new AudioUI(this);
				allTabs.addTab(audioUi, Strings.tabAudio);
			}
			{
				renderTab = new RenderingTab(this);
				allTabs.addTab(renderTab, Strings.tabRender);
			}
			{
				final AboutTab aboutTab = new AboutTab(this);
				allTabs.addTab(aboutTab, Strings.tabAbout);
			}
			vbox.addWidget(allTabs);
		}
		{
			lblStatus = new QLabel();
			vbox.addWidget(lblStatus);
			final QHBoxLayout hbox = new QHBoxLayout();
			btnMount = new QPushButton(Strings.btnActivate);
			btnMount.clicked.connect(this, "onMount()");
			hbox.addWidget(btnMount);
			btnExit = new QPushButton(Strings.btnDeactivate);
			btnExit.clicked.connect(this, "onDeactivate()");
			hbox.addWidget(btnExit);
			vbox.addLayout(hbox);
		}
		updateStatus();
		setLayout(vbox);
	}

	boolean isAudioBufferInUse() {
		return audioUi.isBufferInUse();
	}

	@SuppressWarnings("unused")
	private void onBrowseBackingDirectory() {
		final String selectedFolder = QFileDialog.getExistingDirectory(this, Strings.step1Dialog, backingDirectory.text());
		if (selectedFolder.length() > 0) {
			backingDirectory.setText(selectedFolder);
		}
		if (!getBackingDirectory().isDirectory()) {
			backingDirectory.setText("");
		}
		updateStatus();
	}

	@SuppressWarnings("unused")
	private void onBrowseMountpoint() {
		final String selectedFolder = QFileDialog.getExistingDirectory(this, Strings.step2Dialog, mountpoint.text());
		if (selectedFolder.length() > 0) {
			mountpoint.setText(selectedFolder);
		}
		if (!getMountpoint().isDirectory()) {
			mountpoint.setText("");
		}
		updateStatus();
	}

	@SuppressWarnings("unused")
	private void onDeactivate() {
		exit(relaunchStatusCode);
	}

	@SuppressWarnings("unused")
	private void onMount() {
		final File mountPoint = getMountpoint();
		final File backingDirectory = getBackingDirectory();
		if (SrcLogger.getLogMisc()) {
			SrcLogger.log("Mounting to: " + mountPoint.getAbsolutePath());
			SrcLogger.log("Backing directory: " + backingDirectory.getAbsolutePath());
		}
		videoUi.logParams();
		audioUi.logParams();
		fsLock.lock();
		mountedFS = new SrcDemoFS(backingDirectory, videoUi.getFactory(), audioUi.getFactory());
		mountedFS.addListener(renderTab);
		SrcLogger.commandRegisterMountPoint(mountPoint);
		mountedFS.mount(mountPoint, false);
		fsLock.unlock();
		updateStatus();
		selectTab(renderTab);
	}

	void selectTab(final QWidget tab) {
		allTabs.setCurrentWidget(tab);
	}

	private void unmount() {
		fsLock.lock();
		if (mountedFS != null) {
			if (SrcLogger.getLogMisc()) {
				SrcLogger.log("Unmounting.");
			}
			mountedFS.unmount();
			mountedFS.removeListener(renderTab);
			mountedFS = null;
		}
		fsLock.unlock();
	}

	private void updateStatus() {
		final boolean isMounted = mountedFS != null;
		for (final QWidget w : disablableWidgets) {
			w.setEnabled(!isMounted);
		}
		videoUi.enable(!isMounted);
		audioUi.enable(!isMounted);
		allTabs.setTabEnabled(allTabs.indexOf(renderTab), isMounted);
		if (!isMounted) {
			if (badSettings() == null) {
				lblStatus.setText(Strings.lblPressWhenReady);
				btnMount.setEnabled(true);
			} else {
				lblStatus.setText(Strings.lblInvalidSettings + badSettings());
				btnMount.setEnabled(false);
			}
			btnExit.setEnabled(false);
		} else {
			final Integer recordingFps = videoUi.getEffectiveRecordingFps();
			if (recordingFps == null) {
				lblStatus.setText(Strings.lblReadyToRenderNoVideo);
			} else {
				lblStatus.setText(Strings.lblReadyToRender1 + recordingFps + Strings.lblReadyToRender2);
			}
			btnMount.setEnabled(false);
			btnExit.setEnabled(true);
		}
		settings.setLastMountpoint(mountpoint.text());
		settings.setLastBackingDirectory(backingDirectory.text());
	}
}
