package net.srcdemo.ui;

import java.util.Timer;
import java.util.TimerTask;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.gui.QLabel;

class LabelUpdater implements Runnable
{
	private final QLabel label;
	private boolean needUpdate = false;
	private String text = "";
	private final Timer updateTimer = new Timer("Label updater", true);

	LabelUpdater(final QLabel label)
	{
		this.label = label;
		text = label.text();
		final LabelUpdater oldThis = this;
		updateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (needUpdate) {
					QCoreApplication.invokeLater(oldThis);
				}
			}
		}, 1000, 2500);
	}

	@Override
	public void run()
	{
		label.setText(text);
		needUpdate = false;
	}

	void update(final String newText)
	{
		needUpdate = needUpdate || !text.equals(newText);
		text = newText;
	}
}
