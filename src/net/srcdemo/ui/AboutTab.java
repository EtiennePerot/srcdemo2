package net.srcdemo.ui;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.CursorShape;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class AboutTab extends QWidget {
	private QCheckBox autoCheck;
	private final SrcDemoUI parent;
	private QPushButton updateButton;
	private QLabel updateStatus;

	AboutTab(final SrcDemoUI parent) {
		this.parent = parent;
		initUI();
		if (getSettings().getAutoCheckUpdates() && SrcDemoUI.getVersion() != null) {
			QCoreApplication.invokeLater(new Runnable() {
				@Override
				public void run() {
					onCheckUpdates();
				}
			});
		}
	}

	private SrcSettings getSettings() {
		return parent.getSettings();
	}

	private void initUI() {
		final QVBoxLayout vbox = new QVBoxLayout();
		{
			final QLabel iconLabel = new QLabel() {
				@Override
				public boolean event(final QEvent event) {
					if (event.type().equals(QEvent.Type.MouseButtonPress)) {
						QDesktopServices.openUrl(new QUrl(Strings.urlHomepage));
						return true;
					}
					return super.event(event);
				}
			};
			iconLabel.setPixmap(new QPixmap(Files.iconAbout.getAbsolutePath()));
			iconLabel.setCursor(new QCursor(CursorShape.PointingHandCursor));
			vbox.addWidget(iconLabel, 0, AlignmentFlag.AlignHCenter);
		}
		{
			final QLabel label = new QLabel(Strings.lblAboutIcon);
			label.setOpenExternalLinks(true);
			vbox.addWidget(label, 0, AlignmentFlag.AlignHCenter);
		}
		{
			final QHBoxLayout hbox = new QHBoxLayout();
			hbox.addWidget(new QLabel(Strings.lblBuildDate), 0, AlignmentFlag.AlignRight);
			hbox.addWidget(new QLabel(SrcDemoUI.getVersion() == null ? Strings.aboutUnknownVersion : SrcDemoUI.getVersion()),
				0, AlignmentFlag.AlignLeft);
			vbox.addLayout(hbox);
		}
		{
			updateButton = new QPushButton(Strings.btnUpdateCheck);
			updateButton.clicked.connect(this, "onCheckUpdates()");
			updateButton.setEnabled(SrcDemoUI.getVersion() != null);
			vbox.addWidget(updateButton, 0, AlignmentFlag.AlignCenter);
		}
		{
			updateStatus = new QLabel(SrcDemoUI.getVersion() == null ? Strings.errUpdateInvalidVersion : "");
			updateStatus.setOpenExternalLinks(true);
			vbox.addWidget(updateStatus, 0, AlignmentFlag.AlignCenter);
		}
		{
			autoCheck = new QCheckBox(Strings.chkUpdateAutoCheck);
			autoCheck.setChecked(getSettings().getAutoCheckUpdates());
			autoCheck.stateChanged.connect(this, "onChangedAutoCheck()");
			vbox.addWidget(autoCheck, 0, AlignmentFlag.AlignCenter);
		}
		setLayout(vbox);
	}

	@SuppressWarnings("unused")
	private void onChangedAutoCheck() {
		getSettings().setAutoCheckUpdates(autoCheck.isChecked());
	}

	private void onCheckUpdates() {
		updateButton.setEnabled(false);
		updateButton.setText(Strings.btnUpdateChecking);
		updateStatus.setText(Strings.lblUpdateChecking);
		new UpdateCheckThread(this).start();
	}

	void onUpdateStatus(final String status, final boolean switchNow) {
		updateStatus.setText(status);
		updateButton.setEnabled(true);
		updateButton.setText(Strings.btnUpdateRecheck);
		if (switchNow) {
			parent.selectTab(this);
		}
	}
}
