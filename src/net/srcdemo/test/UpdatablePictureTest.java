package net.srcdemo.test;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.srcdemo.ui.UpdatablePicture;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

final class UpdatablePictureTest {
	private static final class PictureTest extends QWidget {
		private final UpdatablePicture pic;

		private PictureTest(final int[] initial, final int width, final int height) {
			setWindowTitle("Test UpdatablePicture");
			final QVBoxLayout vbox = new QVBoxLayout();
			pic = new UpdatablePicture(initial, width, height);
			vbox.addWidget(pic);
			setLayout(vbox);
			show();
		}
	}

	private static int[] getRandomBytes(final int width, final int height) {
		final int[] data = new int[width * height];
		final Random random = new Random();
		for (int i = 0; i < data.length; i++) {
			data[i] = (random.nextInt(255) << 16) + (random.nextInt(255) << 8) + random.nextInt(255);
		}
		return data;
	}

	public static void main(final String[] args) {
		final int width = 1920;
		final int height = 1080;
		final int[] data = getRandomBytes(width, height);
		QApplication.initialize(args);
		final PictureTest test = new PictureTest(data, width, height);
		new Timer("Updater", true).schedule(new TimerTask() {
			@Override
			public void run() {
				QCoreApplication.invokeLater(new Runnable() {
					@Override
					public void run() {
						test.pic.updatePicture();
					}
				});
			}
		}, 0, 150);
		final Thread pusher = new Thread() {
			@Override
			public void run() {
				int[] data;
				try {
					while (true) {
						data = getRandomBytes(width, height);
						test.pic.push(data, width, height);
						Thread.sleep(100);
					}
				}
				catch (final InterruptedException e) {
				}
			}
		};
		pusher.start();
		QCoreApplication.exec();
		pusher.interrupt();
	}
}
