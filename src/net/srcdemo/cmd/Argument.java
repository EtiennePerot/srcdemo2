package net.srcdemo.cmd;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import net.srcdemo.EnumUtils;
import net.srcdemo.Strings;
import net.srcdemo.cmd.Arguments.Category;

public class Argument {
	static enum Type {
		BOOLEAN, DOUBLE, ENUM, INT, STRING;
		public String representative() {
			switch (this) {
				case BOOLEAN:
					return "true|false";
				case ENUM:
					return "val";
				case DOUBLE:
				case INT:
					return "num";
				case STRING:
					return "\"...\"";
			}
			return null;
		}

		@Override
		public String toString() {
			switch (this) {
				case BOOLEAN:
					return "boolean";
				case DOUBLE:
					return "float";
				case ENUM:
					return "enum";
				case INT:
					return "integer";
				case STRING:
					return "string";
			}
			return null;
		}
	}

	private static final DecimalFormat doubleFormat = new DecimalFormat("#.###");

	static Argument create(final Category category, final String shortForm, final String longForm, final Type type,
		final Object defaultValue, final String help) {
		return new Argument(category, shortForm, longForm, type, null, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, help);
	}

	static <T extends Enum<T>> Argument createEnum(final Category category, final String shortForm, final String longForm,
		final T defaultValue, final String help, final Class<T> enumClass) {
		final int n = enumClass.getEnumConstants().length;
		final Collection<String> list = new ArrayList<String>(n);
		for (final T t : EnumUtils.iterate(enumClass)) {
			list.add(t.toString());
		}
		return new Argument(category, shortForm, longForm, Type.ENUM, enumClass, defaultValue, Integer.MIN_VALUE,
			Integer.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, help);
	}

	static Argument present(final Category category, final String shortForm, final String longForm, final String help) {
		return create(category, shortForm, longForm, Type.BOOLEAN, null, help);
	}

	static Argument rangedDouble(final Category category, final String shortForm, final String longForm,
		final Object defaultValue, final double minValue, final double maxValue, final String help) {
		return new Argument(category, shortForm, longForm, Type.DOUBLE, null, defaultValue, Integer.MIN_VALUE,
			Integer.MAX_VALUE, minValue, maxValue, help);
	}

	static Argument rangedInt(final Category category, final String shortForm, final String longForm,
		final Object defaultValue, final int minValue, final int maxValue, final String help) {
		return new Argument(category, shortForm, longForm, Type.INT, null, defaultValue, minValue, maxValue,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, help);
	}

	private final Category category;
	private final String defaultValue;
	private final Class<? extends Enum<?>> enumClass;
	private final String help;
	private final String longForm;
	private final String longForm2;
	private final double maxDoubleValue;
	private final int maxIntValue;
	private final double minDoubleValue;
	private final int minIntValue;
	private final String shortForm;
	private final Type type;

	private Argument(final Category category, final String shortForm, final String longForm, final Type type,
		final Class<? extends Enum<?>> enumClass, final Object defaultValue, final int minIntValue, final int maxIntValue,
		final double minDoubleValue, final double maxDoubleValue, final String help) {
		if (shortForm == null && longForm == null) {
			throw new IllegalArgumentException("At least one argument form must exist.");
		}
		this.shortForm = shortForm == null ? null : ("-" + shortForm);
		this.longForm = longForm == null ? null : ("--" + longForm);
		longForm2 = longForm == null ? null : (this.longForm.toLowerCase() + "=");
		this.type = type;
		this.category = category;
		this.enumClass = enumClass;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
		this.minIntValue = minIntValue;
		this.maxIntValue = maxIntValue;
		this.minDoubleValue = minDoubleValue;
		this.maxDoubleValue = maxDoubleValue;
		this.help = help;
	}

	private double _getDouble(final String s) throws NumberFormatException {
		return Math.max(minDoubleValue, Math.min(maxDoubleValue, Double.parseDouble(s)));
	}

	private int _getInt(final String s) throws NumberFormatException {
		return Math.max(minIntValue, Math.min(maxIntValue, Integer.parseInt(s)));
	}

	Category category() {
		return category;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public String enumHelp() {
		if (!type.equals(Type.ENUM)) {
			return "";
		}
		final StringBuilder h = new StringBuilder();
		for (final Object o : EnumUtils.iterateUnsafe(enumClass)) {
			final String desc = EnumUtils.getDescription(o);
			if (desc != null) {
				h.append("* " + o.toString().toLowerCase() + ": " + desc.replaceAll("[\\r\\n]+", " ")
					+ System.getProperty("line.separator"));
			}
		}
		return h.toString().trim();
	}

	public String forms() {
		return (shortForm == null ? "" : shortForm) + (shortForm == null || longForm == null ? "" : "|")
			+ (longForm == null ? "" : longForm);
	}

	public String friendlyDefault() {
		return defaultValue == null ? null : Strings.cmdDefaultPrefix
			+ (type.equals(Type.ENUM) ? defaultValue.toLowerCase() : defaultValue);
	}

	public String friendlyForm() {
		if (type.equals(Type.BOOLEAN) && defaultValue == null) {
			return forms();
		}
		if (type.equals(Type.INT) && minIntValue != Integer.MIN_VALUE) {
			return forms() + " (" + minIntValue + (maxIntValue == Integer.MAX_VALUE ? " and up" : " to " + maxIntValue) + ")";
		}
		if (type.equals(Type.DOUBLE) && minDoubleValue != Double.NEGATIVE_INFINITY) {
			return forms() + " (" + doubleFormat.format(minDoubleValue)
				+ (maxDoubleValue == Double.POSITIVE_INFINITY ? " and up" : " to " + doubleFormat.format(maxDoubleValue)) + ")";
		}
		return forms() + " " + type.representative();
	}

	public String friendlyType() {
		if (type.equals(Type.BOOLEAN) && defaultValue == null) {
			return "";
		}
		return type.toString();
	}

	public boolean getBoolean(final String... args) {
		if (!type.equals(Type.BOOLEAN)) {
			throw new IllegalStateException("This argument is not of type Boolean.");
		}
		final String s = scan(args);
		return Boolean.getBoolean(s == null ? defaultValue : s);
	}

	public double getDouble(final String... args) throws InvalidFormatArgumentException {
		if (!type.equals(Type.DOUBLE)) {
			throw new IllegalStateException("This argument is not of type Double.");
		}
		final String s = scan(args);
		if (s == null) {
			return _getDouble(defaultValue);
		}
		try {
			return _getDouble(s);
		}
		catch (final NumberFormatException e) {
			throw new InvalidFormatArgumentException(this, s);
		}
	}

	public <T extends Enum<T>> T getEnum(final Class<T> enumClass, final String... args) throws InvalidFormatArgumentException {
		if (!type.equals(Type.ENUM)) {
			throw new IllegalStateException("This argument is not of type Enum.");
		}
		final String s = scan(args);
		if (s == null) {
			return EnumUtils.fromName(enumClass, defaultValue, true);
		}
		final T t = EnumUtils.fromName(enumClass, s, true);
		if (t == null) {
			throw new InvalidFormatArgumentException(this, s);
		}
		return t;
	}

	public int getInt(final String... args) throws InvalidFormatArgumentException {
		if (!type.equals(Type.INT)) {
			throw new IllegalStateException("This argument is not of type Integer.");
		}
		final String s = scan(args);
		if (s == null) {
			return _getInt(defaultValue);
		}
		try {
			return _getInt(s);
		}
		catch (final NumberFormatException e) {
			throw new InvalidFormatArgumentException(this, s);
		}
	}

	public String getString(final String... args) {
		if (!type.equals(Type.STRING)) {
			throw new IllegalStateException("This argument is not of type String.");
		}
		final String s = scan(args);
		return s == null ? defaultValue : s;
	}

	public String help() {
		return help;
	}

	public boolean isPresent(final String... args) {
		return scan(args) != null;
	}

	public String longForm() {
		return longForm;
	}

	private String scan(final String... args) {
		String arg;
		if (type.equals(Type.BOOLEAN)) {
			for (int i = 0; i < args.length; i++) {
				arg = args[i];
				if ((shortForm != null && arg.equalsIgnoreCase(shortForm))
					|| (longForm != null && arg.equalsIgnoreCase(longForm))) {
					if (i < args.length - 1 && (args[i + 1].equalsIgnoreCase("true") || args[i + 1].equalsIgnoreCase("false"))) {
						return args[i + 1];
					}
					return "true";
				}
				if (longForm2 != null && arg.toLowerCase().startsWith(longForm2) && arg.length() > longForm2.length()) {
					return arg.substring(longForm2.length());
				}
			}
		} else {
			for (int i = 0; i < args.length; i++) {
				arg = args[i];
				if (((shortForm != null && arg.equalsIgnoreCase(shortForm)) || (longForm != null && arg
					.equalsIgnoreCase(longForm))) && i < args.length - 1) {
					return args[i + 1];
				}
				if (longForm2 != null && arg.toLowerCase().startsWith(longForm2) && arg.length() > longForm2.length()) {
					return arg.substring(longForm2.length());
				}
			}
		}
		return null;
	}

	public String shortForm() {
		return shortForm;
	}

	public Type type() {
		return type;
	}
}
