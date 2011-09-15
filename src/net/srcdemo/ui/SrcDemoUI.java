package net.srcdemo.ui;

import java.io.File;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SrcDemoUI extends QWidget
{
	public static void main(final String[] args)
	{
		QApplication.initialize(args);
		new SrcDemoUI();
		QApplication.exec();
	}

	private QLineEdit backingDirectory;
	private QPushButton backingDirectoryBrowse;
	private QSpinBox blendRate;
	private QLabel effectiveRecordingFps;
	private QLabel effectiveRecordingFpsCommand;
	private QLineEdit mountpoint;
	private QPushButton mountpointBrowse;
	private QSpinBox targetFps;

	SrcDemoUI()
	{
		setWindowTitle(Strings.windowTitle);
		initUI();
		show();
	}

	private void initUI()
	{
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QLabel label = new QLabel(Strings.step1);
			vbox.addWidget(label);
			final QHBoxLayout hbox = new QHBoxLayout();
			mountpoint = new QLineEdit();
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
			backingDirectory = new QLineEdit();
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
				targetFps.setValue(30);
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
				blendRate.setValue(25);
				blendRate.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(blendRate);
				vbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblEffectiveFps));
				effectiveRecordingFps = new QLabel();
				hbox.addWidget(effectiveRecordingFps);
				vbox.addLayout(hbox);
				vbox.addWidget(new QLabel("Make sure to set host_framerate before you start rendering!"));
				effectiveRecordingFpsCommand = new QLabel();
				vbox.addWidget(effectiveRecordingFpsCommand);
			}
			updateEffectiveRecordingFps();
		}
		setLayout(vbox);
	}

	private void onBrowseBackingDirectory()
	{
		final String selectedFolder = QFileDialog.getExistingDirectory(this, Strings.step1Dialog);
		if (selectedFolder.length() > 0) {
			backingDirectory.setText(selectedFolder);
		}
		if (!(new File(selectedFolder).isDirectory())) {
			backingDirectory.setText("");
		}
	}

	private void onBrowseMountpoint()
	{
		final String selectedFolder = QFileDialog.getExistingDirectory(this, Strings.step2Dialog);
		if (selectedFolder.length() > 0) {
			mountpoint.setText(selectedFolder);
		}
		if (!(new File(selectedFolder).isDirectory())) {
			mountpoint.setText("");
		}
	}

	private void updateEffectiveRecordingFps()
	{
		final int effectiveFps = blendRate.value() * targetFps.value();
		effectiveRecordingFps.setText("" + effectiveFps);
		effectiveRecordingFpsCommand.setText("host_framerate " + effectiveFps);
	}
}
