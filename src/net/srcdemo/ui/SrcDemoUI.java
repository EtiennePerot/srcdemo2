package net.srcdemo.ui;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QMainWindow;

public class SrcDemoUI extends QMainWindow
{
	public void main(final String[] args)
	{
		QApplication.initialize(args);
		QApplication.exec();
	}
}
