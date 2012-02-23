package net.srcdemo.ui;

import java.util.HashSet;
import java.util.Set;

import net.srcdemo.Params;
import net.srcdemo.SrcLogger;
import net.srcdemo.Strings;
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

import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDoubleSpinBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QSlider.TickPosition;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class VideoUI extends QWidget {
	private QSpinBox blendRate;
	private final Set<QWidget> disablableVideoOptions = new HashSet<QWidget>();
	private QLabel effectiveRecordingFps;
	private QLabel effectiveRecordingFpsCommand;
	private QLabel formatExplanation;
	private QCheckBox gaussianCheckbox;
	private QGaussianCurve gaussianCurve;
	private QDoubleSpinBox gaussianVariance;
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

	VideoUI(final SrcDemoUI parent) {
		this.parent = parent;
		initUI();
	}

	private QWidget disablableVideoWidget(final QWidget widget) {
		disablableVideoOptions.add(widget);
		return widget;
	}

	void enable(final boolean enable) {
		updateVideoType();
		videoTypeLabel.setEnabled(enable);
		videoType.setEnabled(enable);
		jpegCompressionLevelLabel.setEnabled(enable);
		jpegCompressionLevel.setEnabled(enable);
		tgaCompressionRLESpacer.setEnabled(enable);
		tgaCompressionRLE.setEnabled(enable);
		for (final QWidget widget : disablableVideoOptions) {
			widget.setEnabled(enable);
		}
		updateGaussian();
	}

	Integer getEffectiveRecordingFps() {
		if (videoType.getCurrentItem().equals(VideoType.DISABLED)) {
			return null;
		}
		return blendRate.value() * targetFps.value();
	}

	VideoHandlerFactory getFactory() {
		final int blend = blendRate.value();
		final int shutter = shutterAngle.value();
		final VideoType vidType = videoType.getCurrentItem();
		final ImageSavingTaskFactory imgFactory;
		switch (vidType) {
			case JPEG:
				imgFactory = new JPEGSavingFactory(jpegCompressionLevel.value() / 100f);
				break;
			case PNG:
				imgFactory = new PNGSavingFactory();
				break;
			case TGA:
				imgFactory = new TGASavingFactory(tgaCompressionRLE.isChecked());
				break;
			case DISABLED:
				return new NullVideoHandlerFactory();
			default:
				// Necessary to satisfy the "final" constraint on imgFactory
				imgFactory = null;
		}
		final FrameWeighter frameWeighter;
		if (gaussianCheckbox.isChecked()) {
			frameWeighter = new GaussianFrameWeighter(gaussianCurve.mean(), gaussianCurve.variance(), gaussianCurve.stdDev());
		} else {
			frameWeighter = new LinearFrameWeighter();
		}
		return new FrameBlenderVideoHandlerFactory(imgFactory, blend, shutter, frameWeighter);
	}

	private SrcSettings getSettings() {
		return parent.getSettings();
	}

	private void initUI() {
		mainVbox = new QVBoxLayout();
		{
			videoTypeVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				videoTypeLabel = new QLabel(Strings.lblVideoType);
				hbox.addWidget(videoTypeLabel);
				videoType = new EnumComboBox<VideoType>(VideoType.class);
				videoType.setCurrentItem(getSettings().getLastVideoType());
				videoType.currentIndexChanged.connect(this, "updateVideoType()");
				hbox.addWidget(videoType);
				videoTypeVbox.addLayout(hbox);
			}
			{
				// JPEG settings
				jpegOptionsBox = new QHBoxLayout();
				jpegCompressionLevelLabel = new QLabel();
				jpegOptionsBox.addWidget(jpegCompressionLevelLabel, 1);
				jpegCompressionLevel = new QSlider();
				jpegCompressionLevel.setOrientation(Orientation.Horizontal);
				jpegCompressionLevel.setRange(Params.jpegQualityMin, Params.jpegQualityMax);
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
			mainVbox.addLayout(videoTypeVbox);
		}
		{
			formatExplanation = new QLabel();
			formatExplanation.setWordWrap(true);
			mainVbox.addWidget(formatExplanation);
		}
		{
			globalVideoOptionsVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(disablableVideoWidget(new QLabel(Strings.lblTargetFps)));
				targetFps = new QSpinBox();
				targetFps.setRange(Params.frameRateMin, Params.frameRateMax);
				targetFps.setValue(getSettings().getLastTargetFps());
				targetFps.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				disablableVideoOptions.add(targetFps);
				hbox.addWidget(disablableVideoWidget(targetFps));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(disablableVideoWidget(new QLabel(Strings.lblBlendRate)));
				blendRate = new QSpinBox();
				blendRate.setRange(Params.blendRateMin, Params.blendRateMax);
				blendRate.setValue(getSettings().getLastBlendRate());
				blendRate.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(disablableVideoWidget(blendRate));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				final QLabel shutterAngleUI = new QLabel(Strings.lblShutterAngle);
				shutterAngleUI.setOpenExternalLinks(true);
				hbox.addWidget(disablableVideoWidget(shutterAngleUI));
				shutterAngle = new QSpinBox();
				shutterAngle.setRange(Params.shutterAngleMin, Params.shutterAngleMax);
				shutterAngle.setValue(getSettings().getLastShutterAngle());
				shutterAngle.valueChanged.connect(this, "updateEffectiveRecordingFps()");
				hbox.addWidget(disablableVideoWidget(shutterAngle));
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				gaussianCheckbox = new QCheckBox(Strings.lblGaussianBlending);
				gaussianCheckbox.setChecked(getSettings().getLastGaussianBlending());
				gaussianCheckbox.stateChanged.connect(this, "updateGaussian()");
				hbox.addWidget(disablableVideoWidget(gaussianCheckbox));
				{
					final QHBoxLayout innerHbox = new QHBoxLayout();
					gaussianVariance = new QDoubleSpinBox();
					gaussianVariance.setMinimum(Params.gaussianVarianceMin);
					gaussianVariance.setMaximum(Params.gaussianVarianceMax);
					gaussianVariance.setSingleStep(0.01d);
					gaussianVariance.setDecimals(3);
					gaussianVariance.setValue(getSettings().getLastGaussianVariance());
					gaussianVariance.valueChanged.connect(this, "updateGaussian()");
					innerHbox.addWidget(disablableVideoWidget(gaussianVariance));
					gaussianCurve = new QGaussianCurve();
					innerHbox.addWidget(disablableVideoWidget(gaussianCurve));
					hbox.addLayout(innerHbox);
				}
				globalVideoOptionsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(disablableVideoWidget(new QLabel(Strings.lblEffectiveFps)));
				effectiveRecordingFps = new QLabel();
				hbox.addWidget(disablableVideoWidget(effectiveRecordingFps));
				globalVideoOptionsVbox.addLayout(hbox);
				globalVideoOptionsVbox.addWidget(disablableVideoWidget(new QLabel(Strings.lblMakeSureFramerate)));
				effectiveRecordingFpsCommand = new QLabel();
				globalVideoOptionsVbox.addWidget(disablableVideoWidget(effectiveRecordingFpsCommand));
			}
		}
		setLayout(mainVbox);
		updateVideoType();
		updateGaussian();
		updateEffectiveRecordingFps();
	}

	void logParams() {
		if (SrcLogger.getLogVideo()) {
			SrcLogger.logVideo("~ Video parameters block ~");
			final VideoType current = videoType.getCurrentItem();
			SrcLogger.logVideo("Video type: " + current);
			if (current.equals(VideoType.JPEG)) {
				SrcLogger.logVideo("JPEG compression: " + jpegCompressionLevel.value());
			} else if (current.equals(VideoType.TGA)) {
				SrcLogger.logVideo("TGA RLE compression: " + (tgaCompressionRLE.isChecked() ? "Enabled" : "Disabled"));
			}
			if (!current.equals(VideoType.DISABLED)) {
				SrcLogger.logVideo("Target framerate: " + targetFps.value());
				SrcLogger.logVideo("Blend rate: " + blendRate.value());
				SrcLogger.logVideo("Shutter angle: " + shutterAngle.value());
				final boolean gaussian = gaussianCheckbox.isChecked();
				SrcLogger.logVideo("Gaussian blending: " + (gaussian ? "Enabled" : "Disabled"));
				if (gaussian) {
					SrcLogger.logVideo("Gaussian variance: " + gaussianVariance.value());
				}
			}
			SrcLogger.logVideo("~ End of video parameters block ~");
		}
	}

	private void saveVideoSettings() {
		getSettings().setLastVideoType(videoType.getCurrentItem());
		getSettings().setLastTargetFps(targetFps.value());
		getSettings().setLastBlendRate(blendRate.value());
		getSettings().setLastShutterAngle(shutterAngle.value());
		getSettings().setLastJPEGCompressionLevel(jpegCompressionLevel.value());
		getSettings().setLastTGACompressionRLE(tgaCompressionRLE.isChecked());
	}

	private void updateEffectiveRecordingFps() {
		final Integer effectiveFps = getEffectiveRecordingFps();
		if (effectiveFps != null) {
			effectiveRecordingFps.setText("" + effectiveFps);
			effectiveRecordingFpsCommand.setText(Strings.cmdHostFramerate + effectiveFps);
		}
		targetFps.setSuffix(targetFps.value() == 1 ? Strings.spnTargetFpsSingular : Strings.spnTargetFpsPlural);
		blendRate.setSuffix(blendRate.value() == 1 ? Strings.spnBlendRateSingular : Strings.spnBlendRatePlural);
		shutterAngle.setSuffix(shutterAngle.value() == 1 ? Strings.spnShutterAngleSingular : Strings.spnShutterAnglePlural);
		saveVideoSettings();
	}

	private void updateGaussian() {
		final boolean enabled = gaussianCheckbox.isChecked();
		getSettings().setLastGaussianBlending(enabled);
		gaussianCheckbox.setText(enabled ? Strings.lblGaussianVariance : Strings.lblGaussianBlending);
		final boolean enabledControls = gaussianCheckbox.isEnabled() && enabled;
		gaussianVariance.setEnabled(enabledControls);
		gaussianCurve.setEnabled(enabledControls);
		final double variance = gaussianVariance.value();
		gaussianCurve.variance(variance);
		getSettings().setLastGaussianVariance(variance);
	}

	@SuppressWarnings("unused")
	private void updateJPEGCompressionLevel() {
		jpegCompressionLevelLabel.setText(Strings.lblJpegQuality + ": " + jpegCompressionLevel.value() + "%");
		saveVideoSettings();
	}

	private void updateVideoType() {
		final VideoType current = videoType.getCurrentItem();
		formatExplanation.setText(current.getDescription());
		{
			// Disabled
			final boolean disabled = current.equals(VideoType.DISABLED);
			if (disabled) {
				if (globalVideoOptionsVbox.parent() != null) {
					mainVbox.removeItem(globalVideoOptionsVbox);
					globalVideoOptionsVbox.setParent(null);
				}
			} else {
				if (globalVideoOptionsVbox.parent() == null) {
					mainVbox.addLayout(globalVideoOptionsVbox);
				}
			}
			for (final QWidget widget : disablableVideoOptions) {
				widget.setVisible(!disabled);
			}
		}
		{
			// JPEG
			if (current.equals(VideoType.JPEG) && jpegOptionsBox.parent() == null) {
				videoTypeVbox.addLayout(jpegOptionsBox);
			} else if (!current.equals(VideoType.JPEG) && jpegOptionsBox.parent() != null) {
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
			} else if (!current.equals(VideoType.TGA) && tgaOptionsBox.parent() != null) {
				videoTypeVbox.removeItem(tgaOptionsBox);
				tgaOptionsBox.setParent(null);
			}
			tgaCompressionRLESpacer.setVisible(current.equals(VideoType.TGA));
			tgaCompressionRLE.setVisible(current.equals(VideoType.TGA));
		}
		saveVideoSettings();
	}
}
