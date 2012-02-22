package net.srcdemo.ui;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QLinearGradient;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPainter.RenderHint;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPolygon;
import com.trolltech.qt.gui.QWidget;

public class QGaussianCurve extends QWidget {
	private static final int disabledFactor = 125;
	private double mean;
	private double stdDev;
	private double variance;

	public QGaussianCurve() {
		this(0, 1);
	}

	public QGaussianCurve(final double variance) {
		this(0, variance);
	}

	public QGaussianCurve(final double mean, final double variance) {
		this.mean = mean;
		this.variance = variance;
		stdDev = Math.sqrt(variance);
	}

	public QGaussianCurve(final QWidget parent) {
		super(parent);
	}

	public double getY(final double x) {
		return Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))), 1 / (stdDev * Math.sqrt(2 * Math.PI)));
	}

	public double mean() {
		return mean;
	}

	public QGaussianCurve mean(final double mean) {
		this.mean = mean;
		update();
		return this;
	}

	@Override
	protected void paintEvent(final QPaintEvent event) {
		final QPainter painter = new QPainter(this);
		painter.setRenderHint(RenderHint.Antialiasing);
		final QPolygon polygon = new QPolygon();
		final int width = width();
		final int height = height();
		final double widthFactor = 2d / width;
		final double halfWidth = width / 2d;
		final double heightFactor = height;
		for (int w = 0; w < width; w++) {
			polygon.add(w, height - (int) (heightFactor * getY((w - halfWidth) * widthFactor)));
		}
		polygon.add(width - 1, height - 1);
		polygon.add(0, height - 1);
		final QPalette palette = QApplication.palette();
		final QLinearGradient gradient = new QLinearGradient(0, 0, 0, height - 1);
		QColor color1 = palette.mid().color();
		QColor color2 = palette.midlight().color();
		QColor lineColor = palette.text().color();
		if (!isEnabled()) {
			color1 = color1.darker(disabledFactor);
			color2 = color2.darker(disabledFactor);
			lineColor = lineColor.darker(disabledFactor);
		}
		gradient.setColorAt(0, color1);
		gradient.setColorAt(1, color2);
		final QBrush gradientBrush = new QBrush(gradient);
		painter.setBrush(gradientBrush);
		painter.setPen(lineColor);
		painter.drawPolygon(polygon);
	}

	public double stdDev() {
		return stdDev;
	}

	public QGaussianCurve stdDev(final double stdDev) {
		this.stdDev = stdDev;
		variance = stdDev * stdDev;
		update();
		return this;
	}

	public double variance() {
		return variance;
	}

	public QGaussianCurve variance(final double variance) {
		this.variance = variance;
		stdDev = Math.sqrt(variance);
		update();
		return this;
	}
}
