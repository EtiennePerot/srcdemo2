package net.srcdemo.ui;

import java.util.ArrayList;
import java.util.List;

import net.srcdemo.EnumUtils;

import com.trolltech.qt.gui.QComboBox;

class EnumComboBox<T extends Enum<T>> extends QComboBox {
	private final List<T> values = new ArrayList<T>();

	EnumComboBox(final Class<T> enumType) {
		for (final T val : EnumUtils.iterate(enumType)) {
			values.add(val);
			addItem(val.toString());
		}
	}

	T getCurrentItem() {
		return values.get(currentIndex());
	}

	void setCurrentItem(final T item) {
		setCurrentIndex(values.indexOf(item));
	}
}
