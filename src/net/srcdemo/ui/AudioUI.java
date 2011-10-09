package net.srcdemo.ui;

import net.srcdemo.SrcDemo;
import net.srcdemo.audio.AudioHandler;
import net.srcdemo.audio.AudioHandlerFactory;
import net.srcdemo.audio.DiskAudioHandler;

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class AudioUI extends QWidget
{
	private QComboBox audioType;
	private final SrcDemoUI parent;

	AudioUI(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
	}

	AudioHandlerFactory getFactory()
	{
		return new AudioHandlerFactory()
		{
			@Override
			public AudioHandler buildHandler(final SrcDemo demo)
			{
				return new DiskAudioHandler(demo);
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
			hbox.addWidget(new QLabel(Strings.lblAudioType));
			audioType = new QComboBox();
			// TODO: Constantify
			audioType.addItem("WAV (Straight to disk)");
			audioType.addItem("WAV (Timed buffer)");
			audioType.addItem("WAV (All in memory)");
			audioType.addItem("Disabled");
			hbox.addWidget(audioType);
			vbox.addLayout(hbox);
		}
		setLayout(vbox);
	}
}
