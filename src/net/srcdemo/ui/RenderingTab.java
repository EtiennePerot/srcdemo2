package net.srcdemo.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import net.srcdemo.SrcDemoListener;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QProgressBar;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class RenderingTab extends QWidget implements SrcDemoListener
{
	private static final DecimalFormat framesProcessedPerSecondFormat = new DecimalFormat("#.##");
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
	private final Set<QWidget> audioWidgets = new HashSet<QWidget>();
	private QPushButton btnFlushAudioBuffer;
	private boolean flushButtonEnabled = false;
	private final AtomicInteger framesProcessed = new AtomicInteger(0);
	private final RollingRate framesProcessRate = new RollingRate();
	private final AtomicInteger framesSaved = new AtomicInteger(0);
	private QLabel lblAudioBuffer1;
	private QLabel lblAudioBuffer2;
	private QLabel lblFramesProcessedPerSecond;
	private QLabel lblLastFrameProcessed;
	private QLabel lblLastFrameSaved;
	private final SrcDemoUI parent;
	private UpdatablePicture previewPicture;

	RenderingTab(final SrcDemoUI parent)
	{
		this.parent = parent;
		initUI();
		final Runnable updateUi = new Runnable()
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
				QCoreApplication.invokeLater(updateUi);
			}
		}, 0, uiUpdateInterval);
	}

	private QWidget audioWidget(final QWidget widget)
	{
		audioWidgets.add(widget);
		return widget;
	}

	private void initUI()
	{
		final QHBoxLayout mainHbox = new QHBoxLayout();
		{
			final QGroupBox videoBox = new QGroupBox(Strings.grpRenderingVideoFrames);
			final QVBoxLayout videoVbox = new QVBoxLayout();
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblLastFrameProcessed));
				lblLastFrameProcessed = new QLabel(Strings.lblLastFrameProcessedDefault);
				lblLastFrameProcessed.setAlignment(AlignmentFlag.AlignRight);
				hbox.addWidget(lblLastFrameProcessed);
				videoVbox.addLayout(hbox, 0);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblFramesProcessedPerSecond));
				lblFramesProcessedPerSecond = new QLabel(Strings.lblFramesProcessedPerSecondDefault);
				lblFramesProcessedPerSecond.setAlignment(AlignmentFlag.AlignRight);
				hbox.addWidget(lblFramesProcessedPerSecond);
				videoVbox.addLayout(hbox, 0);
			}
			{
				final QHBoxLayout hbox = new QHBoxLayout();
				hbox.addWidget(new QLabel(Strings.lblLastFrameSaved));
				lblLastFrameSaved = new QLabel(Strings.lblLastFrameSavedDefault);
				lblLastFrameSaved.setAlignment(AlignmentFlag.AlignRight);
				hbox.addWidget(lblLastFrameSaved);
				videoVbox.addLayout(hbox, 0);
			}
			{
				previewPicture = new UpdatablePicture(Files.iconRenderingDefault);
				videoVbox.addWidget(previewPicture, 1);
			}
			videoBox.setLayout(videoVbox);
			mainHbox.addWidget(videoBox, 1);
		}
		{
			final QGroupBox audioBox = new QGroupBox(Strings.grpRenderingAudioBuffer);
			final QVBoxLayout audioVbox = new QVBoxLayout();
			{
				audioBuffer = new QProgressBar();
				audioBuffer.setOrientation(Orientation.Vertical);
				audioBuffer.setSizePolicy(Policy.Expanding, Policy.Expanding);
				audioVbox.addWidget(audioWidget(audioBuffer), 1, AlignmentFlag.AlignHCenter);
				lblAudioBuffer1 = new QLabel();
				audioVbox.addWidget(audioWidget(lblAudioBuffer1), 0, AlignmentFlag.AlignCenter);
				lblAudioBuffer2 = new QLabel();
				audioVbox.addWidget(audioWidget(lblAudioBuffer2), 0, AlignmentFlag.AlignCenter);
				btnFlushAudioBuffer = new QPushButton(Strings.btnRenderAudioBufferFlush);
				btnFlushAudioBuffer.clicked.connect(this, "onFlushAudioBuffer()");
				btnFlushAudioBuffer.setEnabled(flushButtonEnabled);
				audioVbox.addWidget(audioWidget(btnFlushAudioBuffer), 0, AlignmentFlag.AlignCenter);
			}
			audioBox.setLayout(audioVbox);
			mainHbox.addWidget(audioWidget(audioBox), 0);
		}
		setLayout(mainHbox);
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
		parent.flushAudioBuffer(false);
	}

	@Override
	public void onFrameProcessed(final String frameName)
	{
		framesProcessed.incrementAndGet();
		framesProcessRate.mark();
	}

	@Override
	public void onFrameSaved(final File savedFrame)
	{
		framesSaved.incrementAndGet();
		previewPicture.push(savedFrame);
	}

	private void updateUI()
	{
		lblLastFrameProcessed.setText(Integer.toString(framesProcessed.get()));
		final Double framerate = framesProcessRate.getRatePerSecond();
		lblFramesProcessedPerSecond.setText(framerate == null ? Strings.lblFramesProcessedPerSecondDefault
				: framesProcessedPerSecondFormat.format(framerate));
		lblLastFrameSaved.setText(Integer.toString(framesSaved.get()));
		previewPicture.updatePicture();
		if (parent.isAudioBufferInUse()) {
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
			btnFlushAudioBuffer.setEnabled(flushButtonEnabled && audioBufferOccupied > 0 && audioBufferTotal > 0);
		}
		else {
			for (final QWidget w : audioWidgets) {
				w.setEnabled(false);
			}
		}
	}
}
