package net.srcdemo.ui;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

class UserFSMessage extends QWidget {
	UserFSMessage(final String errorMessage) {
		setWindowTitle(Strings.errDokanTitle);
		final QVBoxLayout vbox = new QVBoxLayout();
		vbox.addWidget(new QLabel(errorMessage));
		final QPushButton exitButton = new QPushButton(Strings.btnQuit);
		exitButton.clicked.connect(this, "onExit()");
		vbox.addWidget(exitButton, 0, AlignmentFlag.AlignCenter);
		setLayout(vbox);
	}

	@SuppressWarnings("unused")
	private void onExit() {
		close();
	}
}
