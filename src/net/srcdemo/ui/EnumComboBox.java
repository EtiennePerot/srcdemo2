package net.srcdemo.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.srcdemo.EnumUtils;

import com.trolltech.qt.gui.QComboBox;

class EnumComboBox<T extends Enum<T>> extends QComboBox {
	private final List<T> values = new ArrayList<T>();

	EnumComboBox(final Class<T> enumType) {
		final Collection<String> labels = new ArrayList<String>();
		boolean gotNull = false;
		for (final T val : EnumUtils.iterate(enumType)) {
			values.add(val);
			final String s = EnumUtils.getLabel(val);
			if (s == null) {
				gotNull = true;
				break;
			}
			labels.add(s);
		}
		if (gotNull) {
			values.clear();
			labels.clear();
			for (final T val : EnumUtils.iterate(enumType)) {
				values.add(val);
				labels.add(val.toString());
			}
		}
		for (final String l : labels) {
			addItem(l);
		}
	}

	T getCurrentItem() {
		return values.get(currentIndex());
	}

	void setCurrentItem(final T item) {
		setCurrentIndex(values.indexOf(item));
	}
}
