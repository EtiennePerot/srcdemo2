package net.srcdemo.cmd;

import net.srcdemo.cmd.Arguments.Category;

public class Argument {
	static enum Type {
		BOOLEAN, DOUBLE, INT, STRING;
		public String representative() {
			switch (this) {
				case BOOLEAN:
					return "true|false";
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
				case INT:
					return "integer";
				case STRING:
					return "string";
			}
			return null;
		}
	}

	static Argument present(final Category category, final String shortForm, final String longForm, final String help) {
		return new Argument(category, shortForm, longForm, Type.BOOLEAN, null, help);
	}

	private final Category category;
	private final String defaultValue;
	private final String help;
	private final String longForm;
	private final String longForm2;
	private final String shortForm;
	private final Type type;

	Argument(final Category category, final String shortForm, final String longForm, final Type type,
		final Object defaultValue, final String help) {
		if (shortForm == null && longForm == null) {
			throw new IllegalArgumentException("At least one argument form must exist.");
		}
		this.shortForm = shortForm == null ? null : ("-" + shortForm);
		this.longForm = longForm == null ? null : ("--" + longForm);
		longForm2 = longForm == null ? null : (this.longForm.toLowerCase() + "=");
		this.type = type;
		this.category = category;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
		this.help = help;
	}

	Category category() {
		return category;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public String friendlyForm() {
		final String argLess = (shortForm == null ? "" : shortForm) + (shortForm == null || longForm == null ? "" : ", ")
			+ (longForm == null ? "" : longForm);
		if (type.equals(Type.BOOLEAN) && defaultValue == null) {
			return argLess;
		}
		return argLess + " " + type.representative();
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

	public double getDouble(final String... args) {
		if (!type.equals(Type.DOUBLE)) {
			throw new IllegalStateException("This argument is not of type Double.");
		}
		final String s = scan(args);
		if (s == null) {
			return Double.parseDouble(defaultValue);
		}
		try {
			return Double.parseDouble(s);
		}
		catch (final NumberFormatException e) {
			return Double.parseDouble(defaultValue);
		}
	}

	public int getInt(final String... args) {
		if (!type.equals(Type.INT)) {
			throw new IllegalStateException("This argument is not of type Integer.");
		}
		final String s = scan(args);
		if (s == null) {
			return Integer.parseInt(defaultValue);
		}
		try {
			return Integer.parseInt(s);
		}
		catch (final NumberFormatException e) {
			return Integer.parseInt(defaultValue);
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
