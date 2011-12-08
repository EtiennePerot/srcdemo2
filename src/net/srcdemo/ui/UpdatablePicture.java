package net.srcdemo.ui;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.AspectRatioMode;
import com.trolltech.qt.core.Qt.TransformationMode;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QImage.Format;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy.Policy;

class UpdatablePicture extends QLabel
{
	private static final int imageBorderPixels = 2;
	private String image = null;
	private final Lock lock = new ReentrantLock();
	private boolean needUpdate = true;
	private QPixmap pixmapOriginal = null;
	private int rawHeight = 0;
	private int[] rawPixelData;
	private int rawWidth = 0;
	private QSize size = null;

	UpdatablePicture(final File initialImage)
	{
		image = initialImage.getAbsolutePath();
		setAlignment(AlignmentFlag.AlignCenter);
		setSizePolicy(Policy.MinimumExpanding, Policy.MinimumExpanding);
		setMinimumSize(1, 1);
	}

	private QSize getTargetSize()
	{
		size = pixmapOriginal.size();
		final QSize targetSize = new QSize(Math.min(size.width(), width() - imageBorderPixels), Math.min(size.height(),
				height() - imageBorderPixels));
		size.scale(targetSize, AspectRatioMode.KeepAspectRatio);
		return size;
	}

	void push(final File newImage)
	{
		final String path = newImage.getAbsolutePath();
		lock.lock();
		needUpdate = !path.equals(image);
		image = path;
		rawPixelData = null;
		lock.unlock();
	}

	void push(final int[] pixelData, final int width, final int height)
	{
		lock.lock();
		needUpdate = true; // Comparing each pixel would be expensive; assume it did change
		image = null;
		rawPixelData = pixelData;
		rawWidth = width;
		rawHeight = height;
		lock.unlock();
	}

	@Override
	protected void resizeEvent(final QResizeEvent event)
	{
		lock.lock();
		if (!getTargetSize().equals(pixmap().size())) {
			needUpdate = true;
			updatePicture();
		}
		lock.unlock();
	}

	void updatePicture()
	{
		boolean doGc = false;
		lock.lock();
		if (needUpdate) {
			if (image != null) {
				pixmapOriginal = new QPixmap(image);
			}
			else if (rawPixelData != null) {
				final int length = rawPixelData.length;
				final byte[] rgb = new byte[length * 3];
				for (int i = 0; i < length; i++) {
					rgb[i * 3] = (byte) ((rawPixelData[i] & 0xff0000) >> 16);
					rgb[i * 3 + 1] = (byte) ((rawPixelData[i] & 0xff00) >> 8);
					rgb[i * 3 + 2] = (byte) (rawPixelData[i] & 0xff);
				}
				pixmapOriginal = QPixmap.fromImage(new QImage(rgb, rawWidth, rawHeight, Format.Format_RGB888));
				rawPixelData = null;
			}
			if (!pixmapOriginal.isNull()) {
				setPixmap(pixmapOriginal.scaled(getTargetSize(), AspectRatioMode.KeepAspectRatio,
						TransformationMode.SmoothTransformation));
			}
			needUpdate = false;
			doGc = true;
		}
		lock.unlock();
		if (doGc) {
			System.gc();
		}
	}
}
