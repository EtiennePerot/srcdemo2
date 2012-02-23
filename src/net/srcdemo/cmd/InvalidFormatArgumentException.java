package net.srcdemo.cmd;

public class InvalidFormatArgumentException extends IllegalArgumentException {
	private static final long serialVersionUID = 7779628140006557407L;
	private final Argument argument;
	private final String value;

	public InvalidFormatArgumentException(final Argument argument, final String value) {
		this.argument = argument;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Invalid value for argument \"" + argument.forms() + "\": \"" + value + "\".";
	}
}
