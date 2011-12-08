package net.srcdemo.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.srcdemo.EnumUtils;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler;
import net.srcdemo.audio.DiskAudioHandler;
import net.srcdemo.audio.NullAudioHandler;
import net.srcdemo.audio.convert.AudioEncoder;
import net.srcdemo.audio.convert.AudioEncoderFactory;
import net.srcdemo.audio.convert.FlacEncoder;
import net.srcdemo.audio.convert.VorbisEncoder;
import net.srcdemo.audio.convert.WAVConverter;

import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QSlider.TickPosition;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class AudioUI extends QWidget
{
	static enum AudioType
	{
		BUFFERED, DISABLED, DISK, FLAC, VORBIS;
		static {
			final AudioType[] order = { DISK, BUFFERED, FLAC, VORBIS, DISABLED };
			EnumUtils.registerOrder(AudioType.class, order);
		}

		public String getDescription()
		{
			switch (this) {
				case DISABLED:
					return Strings.audioOptDisabledExplanation;
				case BUFFERED:
					return Strings.audioOptBufferedExplanation;
				case FLAC:
					return Strings.audioOptFlacExplanation;
				case VORBIS:
					return Strings.audioOptVorbisExplanation;
				case DISK:
					return Strings.audioOptDiskExplanation;
			}
			return null;
		}

		private boolean requiresBuffer()
		{
			switch (this) {
				case DISABLED:
				case DISK:
					return false;
				default:
					return true;
			}
		}

		@Override
		public String toString()
		{
			switch (this) {
				case DISABLED:
					return Strings.audioOptDisabled;
				case BUFFERED:
					return Strings.audioOptBuffered;
				case FLAC:
					return Strings.audioOptFlac;
				case VORBIS:
					return Strings.audioOptVorbis;
				case DISK:
					return Strings.audioOptDisk;
			}
			return null;
		}
	}

	private class BufferedAudioHandlerFactory extends AudioHandlerFactory
	{
		private final int bufferBytes;
		private final AudioHandlerFactory subFactory;
		private final int timeout;

		private BufferedAudioHandlerFactory(final AudioHandlerFactory subFactory)
		{
			this.subFactory = subFactory;
			bufferBytes = bufferSize.value() * 1024;
			timeout = bufferTimeout.value();
		}

		@Override
		public AudioHandler buildHandler(final SrcDemo demo)
		{
			return new BufferedAudioHandler(demo, bufferBytes, timeout, subFactory);
		}
	}

	private class DiskAudioHandlerFactory extends AudioHandlerFactory
	{
		@Override
		public AudioHandler buildHandler(final SrcDemo demo)
		{
			return new DiskAudioHandler(demo);
		}
	}

	private class FlacAudioHandlerFactory extends AudioHandlerFactory
	{
		private AudioEncoderFactory encoderFactory;

		private FlacAudioHandlerFactory()
		{
			encoderFactory = new AudioEncoderFactory()
			{
				@Override
				public AudioEncoder buildEncoder(final int channels, final int blockSize, final int sampleRate,
						final int bitsPerSample, final File outputFile) throws IOException
				{
					return new FlacEncoder(channels, blockSize, sampleRate, bitsPerSample, outputFile);
				}
			};
		}

		@Override
		public AudioHandler buildHandler(final SrcDemo demo)
		{
			return new WAVConverter(encoderFactory, demo.getSoundFile());
		}
	}

	private class NullAudioHandlerFactory extends AudioHandlerFactory
	{
		@Override
		public AudioHandler buildHandler(final SrcDemo demo)
		{
			return new NullAudioHandler(demo);
		}
	}

	private class OggAudioHandlerFactory extends AudioHandlerFactory
	{
		private final int quality;

		private OggAudioHandlerFactory(final int quality)
		{
			this.quality = quality;
		}

		@Override
		public AudioHandler buildHandler(final SrcDemo demo)
		{
			return new VorbisEncoder(demo.getSoundFile(), quality);
		}
	}

	private static final double maximumAudioMemoryPortion = 0.65;
	private EnumComboBox<AudioType> audioType;
	private QLabel audioTypeLabel;
	private QVBoxLayout audioTypeVbox;
	private QVBoxLayout bufferSettingsVbox;
	private QSpinBox bufferSize;
	private QSpinBox bufferTimeout;
	private final Set<QWidget> disablableAudioOptions = new HashSet<QWidget>();
	private QLabel formatExplanation;
	private QVBoxLayout mainVbox;
	private final SrcDemoUI parent;
	private QHBoxLayout vorbisOptionsBox;
	private QSlider vorbisQuality;
	private QLabel vorbisQualityLabel;

	AudioUI(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
	}

	private QWidget disablableAudioWidget(final QWidget widget)
	{
		disablableAudioOptions.add(widget);
		return widget;
	}

	void enable(final boolean enable)
	{
		updateAudioType();
		audioTypeLabel.setEnabled(enable);
		audioType.setEnabled(enable);
		vorbisQualityLabel.setEnabled(enable);
		vorbisQuality.setEnabled(enable);
		for (final QWidget w : disablableAudioOptions) {
			w.setEnabled(enable);
		}
	}

	AudioHandlerFactory getFactory()
	{
		final AudioType type = audioType.getCurrentItem();
		if (type.equals(AudioType.DISK)) {
			return new DiskAudioHandlerFactory();
		}
		if (type.equals(AudioType.DISABLED)) {
			return new NullAudioHandlerFactory();
		}
		if (type.equals(AudioType.FLAC)) {
			return new BufferedAudioHandlerFactory(new FlacAudioHandlerFactory());
		}
		if (type.equals(AudioType.VORBIS)) {
			return new BufferedAudioHandlerFactory(new OggAudioHandlerFactory(vorbisQuality.value()));
		}
		if (type.equals(AudioType.BUFFERED)) {
			return new BufferedAudioHandlerFactory(new DiskAudioHandlerFactory());
		}
		return null;
	}

	private SrcSettings getSettings()
	{
		return parent.getSettings();
	}

	private void initUI()
	{
		mainVbox = new QVBoxLayout();
		{
			audioTypeVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				audioTypeLabel = new QLabel(Strings.lblAudioType);
				hbox.addWidget(audioTypeLabel);
				audioType = new EnumComboBox<AudioType>(AudioType.class);
				audioType.setCurrentItem(getSettings().getLastAudioType());
				audioType.currentIndexChanged.connect(this, "updateAudioType()");
				hbox.addWidget(audioType);
				audioTypeVbox.addLayout(hbox);
			}
			{
				// Vorbis settings
				vorbisOptionsBox = new QHBoxLayout();
				vorbisQualityLabel = new QLabel();
				vorbisOptionsBox.addWidget(vorbisQualityLabel, 1);
				vorbisQuality = new QSlider();
				vorbisQuality.setOrientation(Orientation.Horizontal);
				vorbisQuality.setRange(-2, 10);
				vorbisQuality.setSingleStep(1);
				vorbisQuality.setPageStep(1);
				vorbisQuality.setTickPosition(TickPosition.TicksBelow);
				vorbisQuality.setTickInterval(1);
				vorbisQuality.setTracking(true);
				vorbisQuality.setValue(getSettings().getLastVorbisQuality());
				vorbisQuality.valueChanged.connect(this, "updateVorbisQualityLevel()");
				vorbisOptionsBox.addWidget(vorbisQuality, 1);
				vorbisQualityLabel.setText(Strings.lblVorbisQuality + ": " + Strings.lblVorbisQualityPrefix
						+ vorbisQuality.value());
			}
			mainVbox.addLayout(audioTypeVbox);
		}
		{
			formatExplanation = new QLabel();
			formatExplanation.setWordWrap(true);
			mainVbox.addWidget(formatExplanation);
		}
		{
			bufferSettingsVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(disablableAudioWidget(new QLabel(Strings.lblAudioBufferSize)));
				bufferSize = new QSpinBox();
				bufferSize.setRange(4, (int) (Runtime.getRuntime().maxMemory() * maximumAudioMemoryPortion / 1024));
				bufferSize.setSuffix(Strings.spnAudioBufferSize);
				bufferSize.setValue(getSettings().getLastAudioBufferSize());
				bufferSize.valueChanged.connect(this, "saveAudioSettings()");
				hbox.addWidget(disablableAudioWidget(bufferSize));
				bufferSettingsVbox.addLayout(hbox);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(disablableAudioWidget(new QLabel(Strings.lblAudioBufferTimeout)));
				bufferTimeout = new QSpinBox();
				bufferTimeout.setRange(3, 180);
				bufferTimeout.setSuffix(Strings.spnAudioBufferTimeout);
				bufferTimeout.setValue(getSettings().getLastAudioBufferTimeout());
				bufferTimeout.valueChanged.connect(this, "saveAudioSettings()");
				hbox.addWidget(disablableAudioWidget(bufferTimeout));
				bufferSettingsVbox.addLayout(hbox);
			}
			{
				final QLabel warningLabel = new QLabel(Strings.lblBufferWarning);
				warningLabel.setWordWrap(true);
				bufferSettingsVbox.addWidget(disablableAudioWidget(warningLabel));
			}
			mainVbox.addLayout(bufferSettingsVbox);
		}
		setLayout(mainVbox);
		updateAudioType();
	}

	boolean isBufferInUse()
	{
		return audioType.getCurrentItem().requiresBuffer();
	}

	void logParams()
	{
		SrcLogger.logAudio("~ Audio parameters block ~");
		final AudioType current = audioType.getCurrentItem();
		SrcLogger.logAudio("Audio type: " + current);
		if (current.equals(AudioType.VORBIS)) {
			SrcLogger.logAudio("Vorbis quality: " + Strings.lblVorbisQualityPrefix + vorbisQuality.value());
		}
		if (current.requiresBuffer()) {
			SrcLogger.logAudio("Buffer size: " + bufferSize.value() + " kilobytes");
			SrcLogger.logAudio("Buffer timeout: " + bufferTimeout.value() + " seconds");
		}
		SrcLogger.logAudio("~ End of audio parameters block ~");
	}

	private void saveAudioSettings()
	{
		getSettings().setLastAudioType(audioType.getCurrentItem());
		getSettings().setLastAudioBufferSize(bufferSize.value());
		getSettings().setLastAudioBufferTimeout(bufferTimeout.value());
		getSettings().setLastVorbisQuality(vorbisQuality.value());
	}

	private void updateAudioType()
	{
		final AudioType current = audioType.getCurrentItem();
		formatExplanation.setText(current.getDescription());
		{
			// Buffer usage
			final boolean useBuffer = current.requiresBuffer();
			if (useBuffer) {
				if (bufferSettingsVbox.parent() == null) {
					mainVbox.addLayout(bufferSettingsVbox);
				}
			}
			else {
				if (bufferSettingsVbox.parent() != null) {
					mainVbox.removeItem(bufferSettingsVbox);
					bufferSettingsVbox.setParent(null);
				}
			}
			for (final QWidget widget : disablableAudioOptions) {
				widget.setVisible(useBuffer);
			}
		}
		{
			// Vorbis
			if (current.equals(AudioType.VORBIS) && vorbisOptionsBox.parent() == null) {
				audioTypeVbox.addLayout(vorbisOptionsBox);
			}
			else if (!current.equals(AudioType.VORBIS) && vorbisOptionsBox.parent() != null) {
				audioTypeVbox.removeItem(vorbisOptionsBox);
				vorbisOptionsBox.setParent(null);
			}
			vorbisQualityLabel.setVisible(current.equals(AudioType.VORBIS));
			vorbisQuality.setVisible(current.equals(AudioType.VORBIS));
		}
		saveAudioSettings();
	}

	@SuppressWarnings("unused")
	private void updateVorbisQualityLevel()
	{
		vorbisQualityLabel.setText(Strings.lblVorbisQuality + ": " + Strings.lblVorbisQualityPrefix + vorbisQuality.value());
		saveAudioSettings();
	}
}
