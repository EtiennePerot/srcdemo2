package net.srcdemo.ui;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.AspectRatioMode;
import com.trolltech.qt.core.Qt.TransformationMode;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy.Policy;

class UpdatablePicture extends QLabel
{
	private static final int imageBorderPixels = 2;
	private String image;
	private final Lock lock = new ReentrantLock();
	private boolean needUpdate = true;
	private QPixmap pixmapOriginal = null;
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
			pixmapOriginal = new QPixmap(image);
			setPixmap(pixmapOriginal.scaled(getTargetSize(), AspectRatioMode.KeepAspectRatio,
					TransformationMode.SmoothTransformation));
			needUpdate = false;
			doGc = true;
		}
		lock.unlock();
		if (doGc) {
			System.gc();
		}
	}
}
