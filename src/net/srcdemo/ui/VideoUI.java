package net.srcdemo.ui;

import net.srcdemo.SrcDemo;
import net.srcdemo.video.FrameBlender;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class VideoUI extends QWidget
{
	enum VideoType
	{
		DISABLED, JPG, PNG, TGA;
		static VideoType fromIndex(final int index)
		{
			for (final VideoType type : values()) {
				if (type.getIndex() == index) {
					return type;
				}
			}
			return null;
		}

		int getIndex()
		{
			switch (this) {
				case PNG:
					return 0;
				case TGA:
					return 1;
				case JPG:
					return 2;
				case DISABLED:
					return 3;
			}
			return Integer.MAX_VALUE;
		}

		String getLabel()
		{
			switch (this) {
				case PNG:
					return Strings.videoOptPng;
				case TGA:
					return Strings.videoOptTga;
				case JPG:
					return Strings.videoOptJpg;
				case DISABLED:
					return Strings.videoOptDisabled;
			}
			return null;
		}
	}

	private QSpinBox blendRate;
	private QLabel effectiveRecordingFps;
	private QLabel effectiveRecordingFpsCommand;
	private final SrcDemoUI parent;
	private QSpinBox shutterAngle;
	private QSpinBox targetFps;
	private QComboBox videoType;

	VideoUI(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
		updateEffectiveRecordingFps();
	}

	void enable(final boolean enable)
	{
		targetFps.setEnabled(enable);
		blendRate.setEnabled(enable);
		shutterAngle.setEnabled(enable);
	}

	VideoHandlerFactory getFactory()
	{
		final int blend = blendRate.value();
		final int shutter = shutterAngle.value();
		return new VideoHandlerFactory()
		{
			@Override
			public VideoHandler buildHandler(final SrcDemo demo)
			{
				return new FrameBlender(demo, blend, shutter);
			}
		};
	}

	private SrcSettings getSettings()
	{
		return parent.getSettings();
	}

	private void initUI()
	{
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblVideoType));
			videoType = new QComboBox();
			for (int i = 0; i < VideoType.values().length; i++) {
				videoType.addItem(VideoType.fromIndex(i).getLabel(), VideoType.fromIndex(i));
			}
			videoType.setCurrentIndex(getSettings().getLastVideoType().getIndex());
			videoType.currentStringChanged.connect(this, "saveVideoSettings();");
			hbox.addWidget(videoType);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblTargetFps));
			targetFps = new QSpinBox();
			targetFps.setRange(1, 600);
			targetFps.setValue(getSettings().getLastTargetFps());
			targetFps.valueChanged.connect(this, "updateEffectiveRecordingFps()");
			hbox.addWidget(targetFps);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblBlendRate));
			blendRate = new QSpinBox();
			blendRate.setRange(1, 1000);
			blendRate.setValue(getSettings().getLastBlendRate());
			blendRate.valueChanged.connect(this, "updateEffectiveRecordingFps()");
			hbox.addWidget(blendRate);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblShutterAngle));
			shutterAngle = new QSpinBox();
			shutterAngle.setRange(1, 360);
			shutterAngle.setValue(getSettings().getLastShutterAngle());
			shutterAngle.valueChanged.connect(this, "updateEffectiveRecordingFps()");
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
		setLayout(vbox);
	}

	private void saveVideoSettings()
	{
		getSettings().setLastVideoType(VideoType.fromIndex(videoType.currentIndex()));
		getSettings().setLastTargetFps(targetFps.value());
		getSettings().setLastBlendRate(blendRate.value());
		getSettings().setLastShutterAngle(shutterAngle.value());
	}

	private void updateEffectiveRecordingFps()
	{
		final int effectiveFps = blendRate.value() * targetFps.value();
		effectiveRecordingFps.setText("" + effectiveFps);
		effectiveRecordingFpsCommand.setText(Strings.cmdHostFramerate + effectiveFps);
		targetFps.setSuffix(targetFps.value() == 1 ? Strings.spnTargetFpsSingular : Strings.spnTargetFpsPlural);
		blendRate.setSuffix(blendRate.value() == 1 ? Strings.spnBlendRateSingular : Strings.spnBlendRatePlural);
		shutterAngle.setSuffix(shutterAngle.value() == 1 ? Strings.spnShutterAngleSingular : Strings.spnShutterAnglePlural);
		saveVideoSettings();
	}
}
