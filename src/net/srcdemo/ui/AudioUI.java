package net.srcdemo.ui;

import net.srcdemo.EnumUtils;
import net.srcdemo.SrcDemo;
import net.srcdemo.SrcLogger;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.BufferedAudioHandler;
import net.srcdemo.audio.DiskAudioHandler;
import net.srcdemo.audio.NullAudioHandler;

import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class AudioUI extends QWidget
{
	static enum AudioType
	{
		BUFFERED, DISABLED, DISK;
		static {
			final AudioType[] order = { DISK, BUFFERED, DISABLED };
			EnumUtils.registerOrder(AudioType.class, order);
		}

		@Override
		public String toString()
		{
			switch (this) {
				case DISABLED:
					return Strings.audioOptDisabled;
				case BUFFERED:
					return Strings.audioOptBuffered;
				case DISK:
					return Strings.audioOptDisk;
			}
			return null;
		}
	}

	private static final double maximumAudioMemoryPortion = 0.65;
	private EnumComboBox<AudioType> audioType;
	private QSpinBox bufferSize;
	private QSpinBox bufferTimeout;
	private final SrcDemoUI parent;

	AudioUI(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
	}

	void enable(final boolean enable)
	{
		// TODO
	}

	AudioHandlerFactory getFactory()
	{
		final AudioType type = audioType.getCurrentItem();
		if (type.equals(AudioType.DISK)) {
			return new AudioHandlerFactory()
			{
				@Override
				public AudioHandler buildHandler(final SrcDemo demo)
				{
					return new DiskAudioHandler(demo);
				}
			};
		}
		if (type.equals(AudioType.DISABLED)) {
			return new AudioHandlerFactory()
			{
				@Override
				public AudioHandler buildHandler(final SrcDemo demo)
				{
					return new NullAudioHandler(demo);
				}
			};
		}
		if (type.equals(AudioType.BUFFERED)) {
			final int bufferBytes = bufferSize.value() * 1024;
			final int timeout = bufferTimeout.value();
			return new AudioHandlerFactory()
			{
				@Override
				public AudioHandler buildHandler(final SrcDemo demo)
				{
					return new BufferedAudioHandler(demo, bufferBytes, timeout);
				}
			};
		}
		return null;
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
			hbox.addWidget(new QLabel(Strings.lblAudioType));
			audioType = new EnumComboBox<AudioType>(AudioType.class);
			audioType.setCurrentItem(getSettings().getLastAudioType());
			audioType.currentIndexChanged.connect(this, "saveAudioSettings()");
			hbox.addWidget(audioType);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblAudioBufferSize));
			bufferSize = new QSpinBox();
			bufferSize.setRange(4, (int) (Runtime.getRuntime().maxMemory() * maximumAudioMemoryPortion / 1024));
			bufferSize.setSuffix(Strings.spnAudioBufferSize);
			bufferSize.setValue(getSettings().getLastAudioBufferSize());
			bufferSize.valueChanged.connect(this, "saveAudioSettings()");
			hbox.addWidget(bufferSize);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblAudioBufferTimeout));
			bufferTimeout = new QSpinBox();
			bufferTimeout.setRange(3, 180);
			bufferTimeout.setSuffix(Strings.spnAudioBufferTimeout);
			bufferTimeout.setValue(getSettings().getLastAudioBufferTimeout());
			bufferTimeout.valueChanged.connect(this, "saveAudioSettings()");
			hbox.addWidget(bufferTimeout);
			vbox.addLayout(hbox);
		}
		setLayout(vbox);
	}

	void logParams()
	{
		SrcLogger.log("~ Audio parameters block ~");
		SrcLogger.log("(Unimplemented)");
		SrcLogger.log("~ End of audio parameters block ~");
	}

	@SuppressWarnings("unused")
	private void saveAudioSettings()
	{
		getSettings().setLastAudioType(audioType.getCurrentItem());
		getSettings().setLastAudioBufferSize(bufferSize.value());
		getSettings().setLastAudioBufferTimeout(bufferTimeout.value());
	}
}
