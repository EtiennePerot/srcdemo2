package net.srcdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnumUtils {
	private static Map<Class<?>, List<?>> order = new HashMap<Class<?>, List<?>>();

	public static <T extends Enum<T>> int compare(final T element1, final T element2) {
		if (!element1.getDeclaringClass().equals(element2.getDeclaringClass())) {
			return 0;
		}
		return getIndex(element1) > getIndex(element2) ? 1 : -1;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T fromIndex(final Class<T> enumClass, final int index) {
		if (order.containsKey(enumClass)) {
			return (T) order.get(enumClass).get(index);
		}
		return enumClass.getEnumConstants()[index];
	}

	public static Object fromIndexUnsafe(final Class<?> enumClass, final int index) {
		if (order.containsKey(enumClass)) {
			return order.get(enumClass).get(index);
		}
		return enumClass.getEnumConstants()[index];
	}

	public static <T extends Enum<T>> T fromName(final Class<T> enumClass, final String name, final boolean ignoreCase) {
		for (final T t : iterate(enumClass)) {
			if ((ignoreCase && t.toString().equalsIgnoreCase(name)) || t.toString().equals(name)) {
				return t;
			}
		}
		return null;
	}

	public static String getDescription(final Object o) {
		return getString(o, "getDescription");
	}

	public static <T extends Enum<T>> int getIndex(final T element) {
		if (order.containsKey(element.getDeclaringClass())) {
			return order.get(element.getDeclaringClass()).indexOf(element);
		}
		return element.ordinal();
	}

	public static String getLabel(final Object o) {
		return getString(o, "getLabel");
	}

	public static String getString(final Object o, final String methodName) {
		if (o == null) {
			return null;
		}
		final Class<?> c = o.getClass();
		try {
			return c.getMethod(methodName).invoke(o).toString();
		}
		catch (final Exception e) {
			return null;
		}
	}

	public static <T extends Enum<T>> Iterable<T> iterate(final Class<T> enumClass) {
		final int n = enumClass.getEnumConstants().length;
		final List<T> list = new ArrayList<T>(n);
		for (int i = 0; i < n; i++) {
			list.add(fromIndex(enumClass, i));
		}
		return list;
	}

	public static Iterable<Object> iterateUnsafe(final Class<?> enumClass) {
		final int n = enumClass.getEnumConstants().length;
		final List<Object> list = new ArrayList<Object>(n);
		for (int i = 0; i < n; i++) {
			list.add(fromIndexUnsafe(enumClass, i));
		}
		return list;
	}

	public static <T extends Enum<T>> void registerOrder(final Class<T> enumClass, final T[] order) {
		final List<T> list = Arrays.asList(order);
		EnumUtils.order.put(enumClass, list);
	}
}
