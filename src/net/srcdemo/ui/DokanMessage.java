package net.srcdemo.ui;

import net.decasdev.dokan.Dokan;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class DokanMessage extends QWidget
{
	public static String check()
	{
		try {
			if (Dokan.getVersion() == 600) {
				return null;
			}
			return Strings.errInvalidDokan;
		}
		catch (final Error e) {
			return Strings.errDokanNotInstalled;
		}
		catch (final Exception e) {
			return Strings.errDokanNotInstalled;
		}
	}

	DokanMessage(final String dokanMessage)
	{
		setWindowTitle(Strings.errDokanTitle);
		final QVBoxLayout vbox = new QVBoxLayout();
		vbox.addWidget(new QLabel(dokanMessage));
		final QPushButton exitButton = new QPushButton(Strings.btnExit);
		exitButton.clicked.connect(this, "onExit()");
		vbox.addWidget(exitButton, 0, AlignmentFlag.AlignCenter);
		setLayout(vbox);
	}

	private void onExit()
	{
		close();
	}
}
