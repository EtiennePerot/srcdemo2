package net.srcdemo.ui;

import net.decasdev.dokan.Dokan;
import net.srcdemo.SrcLogger;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class DokanMessage extends QWidget
{
	static String check()
	{
		try {
			if (Dokan.getVersion() == 600) {
				SrcLogger
						.log("Starting with version = " + Dokan.getVersion() + " / Driver = " + Dokan.getDriverVersion() + ".");
				return null;
			}
			SrcLogger.error("Invalid Dokan version: " + Dokan.getVersion());
			return Strings.errInvalidDokan;
		}
		catch (final Throwable e) {
			SrcLogger.error("Error caught while initializing Dokan", e);
			return Strings.errDokanNotInstalled;
		}
	}

	DokanMessage(final String dokanMessage)
	{
		setWindowTitle(Strings.errDokanTitle);
		final QVBoxLayout vbox = new QVBoxLayout();
		vbox.addWidget(new QLabel(dokanMessage));
		final QPushButton exitButton = new QPushButton(Strings.btnQuit);
		exitButton.clicked.connect(this, "onExit()");
		vbox.addWidget(exitButton, 0, AlignmentFlag.AlignCenter);
		setLayout(vbox);
	}

	@SuppressWarnings("unused")
	private void onExit()
	{
		close();
	}
}
