package net.srcdemo.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QImage.Format;
import com.trolltech.qt.gui.QPixmap;

/**
 * Hack in order to not use libpng directly, so that the application can be used on systems where libpng doesn't match the
 * version Qt-Jambi was compiled with.
 * Yes, this is stupid, but it works.
 */
final class PNGPixmap {
	static QPixmap make(final File file) throws IOException {
		final BufferedImage image = ImageIO.read(file);
		final int width = image.getWidth();
		final int height = image.getHeight();
		final int[] imageData = image.getRGB(0, 0, width, height, null, 0, width);
		final byte[] imageDataBytes = new byte[width * height * 4];
		for (int i = 0; i < imageData.length; i++) {
			imageDataBytes[i * 4] = (byte) (imageData[i] & 0xFF);
			imageDataBytes[i * 4 + 1] = (byte) ((imageData[i] >> 8) & 0xFF);
			imageDataBytes[i * 4 + 2] = (byte) ((imageData[i] >> 16) & 0xFF);
			imageDataBytes[i * 4 + 3] = (byte) ((imageData[i] >> 24) & 0xFF);
		}
		final QImage qImage = new QImage(imageDataBytes, width, height, Format.Format_ARGB32);
		return QPixmap.fromImage(qImage);
	}

	static QPixmap make(final String path) throws IOException {
		return make(new File(path));
	}

	static QIcon makeIcon(final File file) throws IOException {
		return new QIcon(make(file));
	}

	static QIcon makeIcon(final String path) throws IOException {
		return makeIcon(new File(path));
	}
}
