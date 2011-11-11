package net.srcdemo.ui;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import net.srcdemo.SrcDemoListener;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QProgressBar;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class RenderingTab extends QWidget implements SrcDemoListener
{
	/**
	 * Time between UI updates, in milliseconds
	 */
	private static final long uiUpdateInterval = 750;
	private QProgressBar audioBuffer;
	private int audioBufferOccupied = 0;
	private int audioBufferTotal = 1;
	private final Runnable audioBufferWriteout = new Runnable()
	{
		@Override
		public void run()
		{
			lblAudioBuffer1.setText(Strings.lblRenderAudioBufferWriting);
			lblAudioBuffer2.setText("");
			btnFlushAudioBuffer.setText(Strings.btnRenderAudioBufferFlushing);
		}
	};
	private QPushButton btnFlushAudioBuffer;
	private boolean flushButtonEnabled = false;
	private String lastFrameProcessed = "";
	private String lastFrameSaved = "";
	private QLabel lblAudioBuffer1;
	private QLabel lblAudioBuffer2;
	private QLabel lblLastFrameProcessed;
	private QLabel lblLastFrameSaved;
	private final SrcDemoUI parent;

	RenderingTab(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
		final Runnable doUpdate = new Runnable()
		{
			@Override
			public void run()
			{
				updateUI();
			}
		};
		new Timer("Rendering tab updater", true).schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				QCoreApplication.invokeLater(doUpdate);
			}
		}, 0, uiUpdateInterval);
	}

	private void initUI()
	{
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblLastFrameProcessed));
			lblLastFrameProcessed = new QLabel(Strings.lblLastFrameProcessedDefault);
			lblLastFrameProcessed.setAlignment(AlignmentFlag.AlignRight);
			hbox.addWidget(lblLastFrameProcessed);
			vbox.addLayout(hbox);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblLastFrameSaved));
			lblLastFrameSaved = new QLabel(Strings.lblLastFrameSavedDefault);
			lblLastFrameSaved.setAlignment(AlignmentFlag.AlignRight);
			hbox.addWidget(lblLastFrameSaved);
			vbox.addLayout(hbox);
		}
		{
			final QVBoxLayout audioVbox = new QVBoxLayout();
			audioBuffer = new QProgressBar();
			audioBuffer.setOrientation(Orientation.Vertical);
			audioVbox.addWidget(audioBuffer, 1, AlignmentFlag.AlignCenter);
			lblAudioBuffer1 = new QLabel();
			audioVbox.addWidget(lblAudioBuffer1, 0, AlignmentFlag.AlignCenter);
			lblAudioBuffer2 = new QLabel();
			audioVbox.addWidget(lblAudioBuffer2, 0, AlignmentFlag.AlignCenter);
			btnFlushAudioBuffer = new QPushButton(Strings.btnRenderAudioBufferFlush);
			btnFlushAudioBuffer.clicked.connect(this, "onFlushAudioBuffer()");
			btnFlushAudioBuffer.setEnabled(flushButtonEnabled);
			audioVbox.addWidget(btnFlushAudioBuffer, 0, AlignmentFlag.AlignCenter);
			vbox.addLayout(audioVbox);
		}
		setLayout(vbox);
	}

	@Override
	public void onAudioBuffer(final int occupied, final int total)
	{
		audioBufferOccupied = occupied;
		audioBufferTotal = total;
		flushButtonEnabled = true;
	}

	@Override
	public void onAudioBufferWriteout()
	{
		audioBufferTotal = -1;
		QCoreApplication.invokeLater(audioBufferWriteout);
	}

	@SuppressWarnings("unused")
	private void onFlushAudioBuffer()
	{
		flushButtonEnabled = false;
		btnFlushAudioBuffer.setEnabled(false);
		btnFlushAudioBuffer.setText(Strings.btnRenderAudioBufferFlushing);
		parent.flushAudioBuffer();
	}

	@Override
	public void onFrameProcessed(String frameName)
	{
		while (frameName.charAt(0) == File.separatorChar) {
			frameName = frameName.substring(1);
		}
		lastFrameProcessed = frameName;
	}

	@Override
	public void onFrameSaved(final File savedFrame)
	{
		lastFrameSaved = savedFrame.getName();
	}

	private void updateUI()
	{
		lblLastFrameProcessed.setText(lastFrameProcessed);
		lblLastFrameSaved.setText(lastFrameSaved);
		if (audioBufferTotal == -1) {
			audioBuffer.setValue(0);
		}
		else {
			audioBuffer.setMaximum(audioBufferTotal);
			audioBuffer.setValue(audioBufferOccupied);
			lblAudioBuffer1.setText((audioBufferOccupied / 1024) + Strings.lblRenderAudioBuffer1
					+ (audioBufferOccupied * 100 / audioBufferTotal) + Strings.lblRenderAudioBuffer2);
			lblAudioBuffer2.setText("(" + (audioBufferTotal / 1024) + Strings.lblRenderAudioBuffer3);
			btnFlushAudioBuffer.setText(Strings.btnRenderAudioBufferFlush);
		}
		btnFlushAudioBuffer.setEnabled(flushButtonEnabled && audioBufferOccupied > 0);
	}
}
