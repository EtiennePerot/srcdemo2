package net.srcdemo.ui;

import java.util.HashSet;
import java.util.Set;

import net.srcdemo.EnumUtils;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;
import net.srcdemo.video.FrameBlender;
import net.srcdemo.video.NullVideoHandler;
import net.srcdemo.video.VideoHandler;
import net.srcdemo.video.VideoHandlerFactory;
import net.srcdemo.video.image.ImageSavingTask;
import net.srcdemo.video.image.ImageSavingTaskFactory;
import net.srcdemo.video.image.JPEGSavingTask;
import net.srcdemo.video.image.PNGSavingTask;
import net.srcdemo.video.image.TGASavingTask;

import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QSlider.TickPosition;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class VideoUI extends QWidget
{
	static enum VideoType
	{
		DISABLED, JPEG, PNG, TGA;
		static {
			final VideoType[] order = { PNG, TGA, JPEG, DISABLED };
			EnumUtils.registerOrder(VideoType.class, order);
		}

		@Override
		public String toString()
		{
			switch (this) {
				case PNG:
					return Strings.videoOptPng;
				case TGA:
					return Strings.videoOptTga;
				case JPEG:
					return Strings.videoOptJpg;
				case DISABLED:
					return Strings.videoOptDisabled;
			}
			return null;
		}
	}

	private QSpinBox blendRate;
	private QLabel disabledLabel;
	private QVBoxLayout disabledOptionsBox;
	private QLabel effectiveRecordingFps;
	private QLabel effectiveRecordingFpsCommand;
	private final Set<QWidget> globalVideoOptions = new HashSet<QWidget>();
	private QVBoxLayout globalVideoOptionsVbox;
	private QSlider jpegCompressionLevel;
	private QLabel jpegCompressionLevelLabel;
	private QHBoxLayout jpegOptionsBox;
	private QVBoxLayout mainVbox;
	private final SrcDemoUI parent;
	private QSpinBox shutterAngle;
	private QSpinBox targetFps;
	private QCheckBox tgaCompressionRLE;
	private QLabel tgaCompressionRLESpacer;
	private QHBoxLayout tgaOptionsBox;
	private EnumComboBox<VideoType> videoType;
	private QLabel videoTypeLabel;
	private QVBoxLayout videoTypeVbox;

	VideoUI(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
		updateEffectiveRecordingFps();
	}

	void enable(final boolean enable)
	{
		updateVideoType();
		videoTypeLabel.setEnabled(enable);
		videoType.setEnabled(enable);
		jpegCompressionLevelLabel.setEnabled(enable);
		jpegCompressionLevel.setEnabled(enable);
		tgaCompressionRLESpacer.setEnabled(enable);
		tgaCompressionRLE.setEnabled(enable);
		for (final QWidget widget : globalVideoOptions) {
			widget.setEnabled(enable);
		}
	}

	VideoHandlerFactory getFactory()
	{
		final int blend = blendRate.value();
		final int shutter = shutterAngle.value();
		final VideoType vidType = videoType.getCurrentItem();
		ImageSavingTaskFactory imgFactory = null;
		switch (vidType) {
			case JPEG:
				final float quality = jpegCompressionLevel.value() / 100f;
				imgFactory = new ImageSavingTaskFactory()
				{
					@Override
					public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width,
							final int height)
					{
						return new JPEGSavingTask(sequenceIndex, pixelData, width, height, quality);
					}
				};
				break;
			case PNG:
				imgFactory = new ImageSavingTaskFactory()
				{
					@Override
					public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width,
							final int height)
					{
						return new PNGSavingTask(sequenceIndex, pixelData, width, height);
					}
				};
				break;
			case TGA:
				final boolean rleCompression = tgaCompressionRLE.isChecked();
				imgFactory = new ImageSavingTaskFactory()
				{
					@Override
					public ImageSavingTask buildSavingTask(final int sequenceIndex, final int[] pixelData, final int width,
							final int height)
					{
						return new TGASavingTask(sequenceIndex, pixelData, width, height, rleCompression);
					}
				};
				break;
			case DISABLED:
				return new VideoHandlerFactory()
				{
					@Override
					public VideoHandler buildHandler(final SrcDemo demo)
					{
						return new NullVideoHandler(demo);
					}
				};
		}
		// Need to make a final version to be able to access it within the inner class
		final ImageSavingTaskFactory finalImgFactory = imgFactory;
		return new VideoHandlerFactory()
		{
			@Override
			public VideoHandler buildHandler(final SrcDemo demo)
			{
				return new FrameBlender(demo, finalImgFactory, blend, shutter);
			}
		};
	}

	private SrcSettings getSettings()
	{
		return parent.getSettings();
	}

	private void initUI()
	{
		mainVbox = new QVBoxLayout();
		{
			videoTypeVbox = new QVBoxLayout();
			final QHBoxLayout hbox = new QHBoxLayout();
			videoTypeLabel = new QLabel(Strings.lblVideoType);
			hbox.addWidget(videoTypeLabel);
			videoType = new EnumComboBox<VideoType>(VideoType.class);
			videoType.setCurrentItem(getSettings().getLastVideoType());
			videoType.currentIndexChanged.connect(this, "updateVideoType()");
			hbox.addWidget(videoType);
			videoTypeVbox.addLayout(hbox);
			{
				// JPEG settings
				jpegOptionsBox = new QHBoxLayout();
				jpegCompressionLevelLabel = new QLabel();
				jpegOptionsBox.addWidget(jpegCompressionLevelLabel, 1);
				jpegCompressionLevel = new QSlider();
				jpegCompressionLevel.setOrientation(Orientation.Horizontal);
				jpegCompressionLevel.setRange(1, 100);
				jpegCompressionLevel.setSingleStep(1);
				jpegCompressionLevel.setPageStep(10);
				jpegCompressionLevel.setTickPosition(TickPosition.TicksBelow);
				jpegCompressionLevel.setTickInterval(10);
				jpegCompressionLevel.setTracking(true);
				jpegCompressionLevel.setValue(getSettings().getLastJPEGCompressionLevel());
				jpegCompressionLevel.valueChanged.connect(this, "updateJPEGCompressionLevel()");
				jpegOptionsBox.addWidget(jpegCompressionLevel, 1);
				jpegCompressionLevelLabel.setText(Strings.lblJpegQuality + ": " + jpegCompressionLevel.value() + "%");
			}
			{
				// TGA settings
				tgaOptionsBox = new QHBoxLayout();
				tgaCompressionRLESpacer = new QLabel();
				tgaOptionsBox.addWidget(tgaCompressionRLESpacer);
				tgaCompressionRLE = new QCheckBox(Strings.lblTgaCompressionRLE);
				tgaCompressionRLE.setChecked(getSettings().getLastTGACompressionRLE());
				tgaCompressionRLE.stateChanged.connect(this, "saveVideoSettings()");
				tgaOptionsBox.addWidget(tgaCompressionRLE);
			}
			{
				// Disabled settings
				disabledOptionsBox = new QVBoxLayout();
				disabledLabel = new QLabel(Strings.lblVideoDisabled);
				disabledLabel.setWordWrap(true);
				disabledOptionsBox.addWidget(disabledLabel);
			}
			mainVbox.addLayout(videoTypeVbox);
		}
		{
			globalVideoOptionsVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(registerGlobalVideoWidget(new QLabel(Strings.lblTargetFps)));
				targetFps = new QSpinBox();
				targetFps.setRange(1, 600);
				targetFps.setValue(getSettings().getLastTargetFps());
				targetFps.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				globalVideoOptions.add(targetFps);
				hbox.addWidget(registerGlobalVideoWidget(targetFps));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(registerGlobalVideoWidget(new QLabel(Strings.lblBlendRate)));
				blendRate = new QSpinBox();
				blendRate.setRange(1, 1000);
				blendRate.setValue(getSettings().getLastBlendRate());
				blendRate.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(registerGlobalVideoWidget(blendRate));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				final QLabel shutterAngleUI = new QLabel(Strings.lblShutterAngle);
				shutterAngleUI.setOpenExternalLinks(true);
				hbox.addWidget(registerGlobalVideoWidget(shutterAngleUI));
				shutterAngle = new QSpinBox();
				shutterAngle.setRange(1, 360);
				shutterAngle.setValue(getSettings().getLastShutterAngle());
				shutterAngle.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(registerGlobalVideoWidget(shutterAngle));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(registerGlobalVideoWidget(new QLabel(Strings.lblEffectiveFps)));
				effectiveRecordingFps = new QLabel();
				hbox.addWidget(registerGlobalVideoWidget(effectiveRecordingFps));
				globalVideoOptionsVbox.addLayout(hbox);
				globalVideoOptionsVbox.addWidget(registerGlobalVideoWidget(new QLabel(Strings.lblMakeSureFramerate)));
				effectiveRecordingFpsCommand = new QLabel();
				globalVideoOptionsVbox.addWidget(registerGlobalVideoWidget(effectiveRecordingFpsCommand));
			}
		}
		setLayout(mainVbox);
		updateVideoType();
	}

	void logParams()
	{
		SrcLogger.log("~ Video parameters block ~");
		final VideoType current = videoType.getCurrentItem();
		SrcLogger.log("Video type: " + current);
		if (current.equals(VideoType.JPEG)) {
			SrcLogger.log("JPEG compression: " + jpegCompressionLevel.value());
		}
		else if (current.equals(VideoType.TGA)) {
			SrcLogger.log("TGA RLE compression is " + (tgaCompressionRLE.isChecked() ? "enabled" : "disabled"));
		}
		else if (!current.equals(VideoType.DISABLED)) {
			SrcLogger.log("Target framerate: " + targetFps.value());
			SrcLogger.log("Blend rate: " + blendRate.value());
			SrcLogger.log("Shutter angle: " + shutterAngle.value());
		}
		SrcLogger.log("~ End of video parameters block ~");
	}

	private QWidget registerGlobalVideoWidget(final QWidget widget)
	{
		globalVideoOptions.add(widget);
		return widget;
	}

	private void saveVideoSettings()
	{
		getSettings().setLastVideoType(videoType.getCurrentItem());
		getSettings().setLastTargetFps(targetFps.value());
		getSettings().setLastBlendRate(blendRate.value());
		getSettings().setLastShutterAngle(shutterAngle.value());
		getSettings().setLastJPEGCompressionLevel(jpegCompressionLevel.value());
		getSettings().setLastTGACompressionRLE(tgaCompressionRLE.isChecked());
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

	@SuppressWarnings("unused")
	private void updateJPEGCompressionLevel()
	{
		jpegCompressionLevelLabel.setText(Strings.lblJpegQuality + ": " + jpegCompressionLevel.value() + "%");
		saveVideoSettings();
	}

	private void updateVideoType()
	{
		final VideoType current = videoType.getCurrentItem();
		{
			// Disabled
			final boolean disabled = current.equals(VideoType.DISABLED);
			if (disabled) {
				if (disabledOptionsBox.parent() == null) {
					videoTypeVbox.addLayout(disabledOptionsBox);
				}
				if (globalVideoOptionsVbox.parent() != null) {
					mainVbox.removeItem(globalVideoOptionsVbox);
					globalVideoOptionsVbox.setParent(null);
				}
			}
			else {
				if (disabledOptionsBox.parent() != null) {
					videoTypeVbox.removeItem(disabledOptionsBox);
					disabledOptionsBox.setParent(null);
				}
				if (globalVideoOptionsVbox.parent() == null) {
					mainVbox.addLayout(globalVideoOptionsVbox);
				}
			}
			disabledLabel.setVisible(disabled);
			for (final QWidget widget : globalVideoOptions) {
				widget.setVisible(!disabled);
			}
		}
		{
			// JPEG
			if (current.equals(VideoType.JPEG) && jpegOptionsBox.parent() == null) {
				videoTypeVbox.addLayout(jpegOptionsBox);
			}
			else if (!current.equals(VideoType.JPEG) && jpegOptionsBox.parent() != null) {
				videoTypeVbox.removeItem(jpegOptionsBox);
				jpegOptionsBox.setParent(null);
			}
			jpegCompressionLevelLabel.setVisible(current.equals(VideoType.JPEG));
			jpegCompressionLevel.setVisible(current.equals(VideoType.JPEG));
		}
		{
			// TGA
			if (current.equals(VideoType.TGA) && tgaOptionsBox.parent() == null) {
				videoTypeVbox.addLayout(tgaOptionsBox);
			}
			else if (!current.equals(VideoType.TGA) && tgaOptionsBox.parent() != null) {
				videoTypeVbox.removeItem(tgaOptionsBox);
				tgaOptionsBox.setParent(null);
			}
			tgaCompressionRLESpacer.setVisible(current.equals(VideoType.TGA));
			tgaCompressionRLE.setVisible(current.equals(VideoType.TGA));
		}
		saveVideoSettings();
	}
}
