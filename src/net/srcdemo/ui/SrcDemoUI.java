package net.srcdemo.ui;

import java.io.File;
import java.lang.reflect.Field;

import net.srcdemo.SrcDemoFS;
import net.srcdemo.SrcDemoListener;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QFrame.Shadow;
import com.trolltech.qt.gui.QFrame.Shape;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SrcDemoUI extends QWidget implements SrcDemoListener
{
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
		QApplication.initialize(args);
		SrcDemoUI ui = null;
		final String dokanMessage = DokanMessage.check();
		if (dokanMessage == null) {
			ui = new SrcDemoUI();
		}
		else {
			new DokanMessage(dokanMessage).show();
		}
		QApplication.exec();
		if (ui != null) {
			ui.unmount();
		}
		System.exit(0);
	}

	private QLineEdit backingDirectory;
	private QPushButton backingDirectoryBrowse;
	private QSpinBox blendRate;
	private QPushButton btnExit;
	private QPushButton btnMount;
	private QLabel effectiveRecordingFps;
	private QLabel effectiveRecordingFpsCommand;
	private LabelUpdater lastFrameProcessedUpdater;
	private LabelUpdater lastFrameSavedUpdated;
	private QLabel lblStatus;
	private SrcDemoFS mountedFS = null;
	private QLineEdit mountpoint;
	private QPushButton mountpointBrowse;
	private final SrcSettings settings;
	private QSpinBox shutterAngle;
	private QSpinBox targetFps;

	SrcDemoUI()
	{
		setWindowTitle(Strings.windowTitle);
		setWindowIcon(new QIcon("img/icon-512.png"));
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

	private File getBackingDirectory()
	{
		return new File(backingDirectory.text());
	}

	private File getMountpoint()
	{
		return new File(mountpoint.text());
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
			{
				final QLabel label = new QLabel(Strings.step3);
				vbox.addWidget(label);
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblTargetFps));
				targetFps = new QSpinBox();
				targetFps.setRange(12, 300);
				targetFps.setSuffix(Strings.spnTargetFps);
				targetFps.setValue(settings.getLastTargetFps());
				targetFps.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(targetFps);
				vbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblBlendRate));
				blendRate = new QSpinBox();
				blendRate.setRange(1, 99);
				blendRate.setSuffix(Strings.spnBlendRate);
				blendRate.setValue(settings.getLastBlendRate());
				blendRate.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(blendRate);
				vbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblShutterAngle));
				shutterAngle = new QSpinBox();
				shutterAngle.setRange(1, 360);
				shutterAngle.setSuffix(Strings.spnShutterAngle);
				shutterAngle.setValue(settings.getLastShutterAngle());
				blendRate.valueChanged.connect(this, "saveFrameSettings()");
				hbox.addWidget(shutterAngle);
				vbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblEffectiveFps));
				effectiveRecordingFps = new QLabel();
				hbox.addWidget(effectiveRecordingFps);
				vbox.addLayout(hbox);
				vbox.addWidget(new QLabel(Strings.lblMakeSureFramerate));
				effectiveRecordingFpsCommand = new QLabel();
				vbox.addWidget(effectiveRecordingFpsCommand);
			}
			updateEffectiveRecordingFps();
		}
		{
			final QFrame hLine = new QFrame();
			hLine.setFrameShape(Shape.HLine);
			hLine.setFrameShadow(Shadow.Sunken);
			vbox.addWidget(hLine);
		}
		{
			lblStatus = new QLabel();
			vbox.addWidget(lblStatus);
			final QHBoxLayout hbox = new QHBoxLayout();
			btnMount = new QPushButton(Strings.btnActivate);
			btnMount.clicked.connect(this, "onMount()");
			hbox.addWidget(btnMount);
			btnExit = new QPushButton(Strings.btnExit);
			btnExit.clicked.connect(this, "onExit()");
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
			settings.setLastBackingDirectory(selectedFolder);
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
			settings.setLastMountpoint(selectedFolder);
		}
		if (!getMountpoint().isDirectory()) {
			mountpoint.setText("");
		}
		updateStatus();
	}

	@SuppressWarnings("unused")
	private void onExit()
	{
		close();
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
		mountedFS = new SrcDemoFS(getBackingDirectory().getAbsolutePath(), blendRate.value(), shutterAngle.value());
		mountedFS.addListener(this);
		mountedFS.setLogging(false);
		mountedFS.mount(getMountpoint().getAbsolutePath());
		updateStatus();
	}

	private void saveFrameSettings()
	{
		settings.setLastTargetFps(targetFps.value());
		settings.setLastBlendRate(blendRate.value());
		settings.setLastShutterAngle(shutterAngle.value());
	}

	private void unmount()
	{
		if (mountedFS != null) {
			mountedFS.unmount();
			mountedFS.removeListener(this);
		}
	}

	private void updateEffectiveRecordingFps()
	{
		final int effectiveFps = blendRate.value() * targetFps.value();
		effectiveRecordingFps.setText("" + effectiveFps);
		effectiveRecordingFpsCommand.setText(Strings.cmdHostFramerate + effectiveFps);
		saveFrameSettings();
	}

	private void updateStatus()
	{
		final QWidget[] fsOptions = { backingDirectory, backingDirectoryBrowse, blendRate, mountpoint, mountpointBrowse,
				shutterAngle, targetFps };
		for (final QWidget w : fsOptions) {
			w.setEnabled(mountedFS == null);
		}
		if (mountedFS == null) {
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
	}
}
