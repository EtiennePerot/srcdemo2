package net.srcdemo.ui;

import java.io.IOException;

import net.srcdemo.Strings;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.WindowModality;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

final class SecretStoatWindow extends QWidget {
	private static final String[] labels = { Strings.lblStoat1, Strings.lblStoat2, Strings.lblStoat3, Strings.lblStoat4,
		Strings.lblStoat5, Strings.lblStoat6, Strings.lblStoat7, Strings.lblStoat8 };
	private QPixmap stoatPixmap;

	SecretStoatWindow() {
		try {
			setWindowTitle(Strings.lblStoatTitle);
			stoatPixmap = PNGPixmap.make(Files.secretStoatImage.getAbsolutePath());
			setWindowIcon(new QIcon(stoatPixmap));
			initUI();
			setWindowModality(WindowModality.ApplicationModal);
			show();
		}
		catch (final IOException e) {
			hide();
		}
	}

	private void initUI() {
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QLabel stoatImage = new QLabel();
			stoatImage.setPixmap(stoatPixmap);
			vbox.addWidget(new QLabel(Strings.lblStoat0), 0, AlignmentFlag.AlignHCenter);
			vbox.addWidget(stoatImage, 0, AlignmentFlag.AlignHCenter);
			for (final String label : labels) {
				vbox.addWidget(new QLabel(label));
			}
		}
		setLayout(vbox);
	}
}
