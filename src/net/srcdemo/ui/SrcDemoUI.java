package net.srcdemo.ui;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.locks.ReentrantLock;

import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcDemoListener;
import net.srcdemo.SrcLogger;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SrcDemoUI extends QWidget implements SrcDemoListener
{
	private static boolean debugMode = false;
	private static boolean dokanLoggingMode = false;
	private static final int relaunchStatusCode = 1337;
	private static int returnCode = 0;
	private static boolean srcDemoHideFiles = false;

	public static void main(final String[] args)
	{
		final String newLibPath = "lib" + File.pathSeparator + System.getProperty("java.library.path");
		System.setProperty("java.library.path", newLibPath);
		Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			if (fieldSysPath != null) {
				fieldSysPath.set(System.class.getClassLoader(), null);
			}
		}
		catch (final Exception e) {
			// Oh well
		}
		for (final String arg : args) {
			if (arg.equals("--srcdemo-debug")) {
				debugMode = true;
				SrcLogger.setLogAll(true);
			}
			if (arg.equals("--dokan-debug")) {
				dokanLoggingMode = true;
			}
			if (arg.equals("--srcdemo-hide-files")) {
				srcDemoHideFiles = true;
			}
		}
		QApplication.initialize(args);
		final String dokanMessage = DokanMessage.check();
		if (dokanMessage == null) {
			final SrcDemoUI ui = new SrcDemoUI();
			Runtime.getRuntime().addShutdownHook(new Thread("Unmount shtudown hook")
			{
				@Override
				public void run()
				{
					ui.unmount();
				}
			});
		}
		else {
			new DokanMessage(dokanMessage).show();
		}
		QApplication.exec();
		System.exit(returnCode);
	}

	private QTabWidget allParams;
	private AudioUI audioUi;
	private QLineEdit backingDirectory;
	private QPushButton backingDirectoryBrowse;
	private QPushButton btnExit;
	private QPushButton btnMount;
	private final ReentrantLock fsLock = new ReentrantLock();
	private LabelUpdater lastFrameProcessedUpdater;
	private LabelUpdater lastFrameSavedUpdated;
	private QLabel lblStatus;
	private SrcDemoFS mountedFS = null;
	private QLineEdit mountpoint;
	private QPushButton mountpointBrowse;
	private final SrcSettings settings;
	private VideoUI videoUi;

	SrcDemoUI()
	{
		setWindowTitle(Strings.windowTitle);
		if (debugMode) {
			setWindowIcon(new QIcon("img/debug-256.png"));
		}
		else {
			setWindowIcon(new QIcon("img/icon-512.png"));
		}
		settings = new SrcSettings();
		initUI();
		show();
	}

	private String badSettings()
	{
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

	void exit(final int returnCode)
	{
		SrcDemoUI.returnCode = returnCode;
		unmount();
		close();
	}

	private File getBackingDirectory()
	{
		return new File(backingDirectory.text());
	}

	private File getMountpoint()
	{
		return new File(mountpoint.text());
	}

	SrcSettings getSettings()
	{
		return settings;
	}

	private void initUI()
	{
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QLabel label = new QLabel(Strings.step1);
			vbox.addWidget(label);
			final QHBoxLayout hbox = new QHBoxLayout();
			mountpoint = new QLineEdit(settings.getLastMountpoint());
			mountpoint.textChanged.connect(this, "updateStatus()");
			hbox.addWidget(mountpoint);
			mountpointBrowse = new QPushButton(Strings.btnBrowse);
			mountpointBrowse.clicked.connect(this, "onBrowseMountpoint()");
			hbox.addWidget(mountpointBrowse);
			vbox.addLayout(hbox);
		}
		{
			final QLabel label = new QLabel(Strings.step2);
			vbox.addWidget(label);
			final QHBoxLayout hbox = new QHBoxLayout();
			backingDirectory = new QLineEdit(settings.getLastBackingDirectory());
			backingDirectory.textChanged.connect(this, "updateStatus()");
			hbox.addWidget(backingDirectory);
			backingDirectoryBrowse = new QPushButton(Strings.btnBrowse);
			backingDirectoryBrowse.clicked.connect(this, "onBrowseBackingDirectory()");
			hbox.addWidget(backingDirectoryBrowse);
			vbox.addLayout(hbox);
		}
		{
			final QLabel label = new QLabel(Strings.step3);
			vbox.addWidget(label);
			allParams = new QTabWidget();
			videoUi = new VideoUI(this);
			allParams.addTab(videoUi, Strings.tabVideo);
			audioUi = new AudioUI(this);
			allParams.addTab(audioUi, Strings.tabAudio);
			vbox.addWidget(allParams);
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
		{
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblLastFrameProcessed));
				final QLabel lblLastFrameProcessed = new QLabel(Strings.lblLastFrameProcessedDefault);
				lblLastFrameProcessed.setAlignment(AlignmentFlag.AlignRight);
				hbox.addWidget(lblLastFrameProcessed);
				vbox.addLayout(hbox);
				lastFrameProcessedUpdater = new LabelUpdater(lblLastFrameProcessed);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblLastFrameSaved));
				final QLabel lblLastFrameSaved = new QLabel(Strings.lblLastFrameSavedDefault);
				lblLastFrameSaved.setAlignment(AlignmentFlag.AlignRight);
				hbox.addWidget(lblLastFrameSaved);
				vbox.addLayout(hbox);
				lastFrameSavedUpdated = new LabelUpdater(lblLastFrameSaved);
			}
		}
		updateStatus();
		setLayout(vbox);
	}

	@SuppressWarnings("unused")
	private void onBrowseBackingDirectory()
	{
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
	private void onBrowseMountpoint()
	{
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
	private void onDeactivate()
	{
		exit(relaunchStatusCode);
	}

	@Override
	public void onFrameProcessed(String frameName)
	{
		while (frameName.charAt(0) == File.separatorChar) {
			frameName = frameName.substring(1);
		}
		lastFrameProcessedUpdater.update(frameName);
	}

	@Override
	public void onFrameSaved(final File savedFrame)
	{
		lastFrameSavedUpdated.update(savedFrame.getName());
	}

	@SuppressWarnings("unused")
	private void onMount()
	{
		SrcLogger.log("Mounting to: " + getMountpoint().getAbsolutePath());
		SrcLogger.log("Backing directory: " + getBackingDirectory().getAbsolutePath());
		videoUi.logParams();
		audioUi.logParams();
		fsLock.lock();
		mountedFS = new SrcDemoFS(getBackingDirectory().getAbsolutePath(), videoUi.getFactory(), audioUi.getFactory());
		mountedFS.addListener(this);
		mountedFS.setLogging(dokanLoggingMode);
		mountedFS.setHideFiles(srcDemoHideFiles);
		mountedFS.mount(getMountpoint().getAbsolutePath());
		fsLock.unlock();
		updateStatus();
	}

	private void unmount()
	{
		fsLock.lock();
		if (mountedFS != null) {
			SrcLogger.log("Unmounting.");
			mountedFS.unmount();
			mountedFS.removeListener(this);
			mountedFS = null;
		}
		fsLock.unlock();
	}

	private void updateStatus()
	{
		final QWidget[] fsOptions = { backingDirectory, backingDirectoryBrowse, mountpoint, mountpointBrowse };
		final boolean isMounted = mountedFS != null;
		for (final QWidget w : fsOptions) {
			w.setEnabled(!isMounted);
		}
		videoUi.enable(!isMounted);
		audioUi.enable(!isMounted);
		if (!isMounted) {
			if (badSettings() == null) {
				lblStatus.setText(Strings.lblPressWhenReady);
				btnMount.setEnabled(true);
			}
			else {
				lblStatus.setText(Strings.lblInvalidSettings + badSettings());
				btnMount.setEnabled(false);
			}
			btnExit.setEnabled(false);
		}
		else {
			lblStatus.setText(Strings.lblReadyToRender);
			btnMount.setEnabled(false);
			btnExit.setEnabled(true);
		}
		settings.setLastMountpoint(mountpoint.text());
		settings.setLastBackingDirectory(backingDirectory.text());
	}
}
