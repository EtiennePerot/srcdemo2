package net.srcdemo.video;

import net.srcdemo.EnumUtils;
import net.srcdemo.Strings;

public enum VideoType {
	DISABLED, JPEG, PNG, TGA;
	static {
		final VideoType[] order = { PNG, TGA, JPEG, DISABLED };
		EnumUtils.registerOrder(VideoType.class, order);
	}

	public String getDescription() {
		switch (this) {
			case PNG:
				return Strings.videoOptPngExplanation;
			case TGA:
				return Strings.videoOptTgaExplanation;
			case JPEG:
				return Strings.videoOptJpgExplanation;
			case DISABLED:
				return Strings.videoOptDisabledExplanation;
		}
		return null;
	}

	public String getLabel() {
		switch (this) {
			case PNG:
				return Strings.videoOptPng;
			case TGA:
				return Strings.videoOptTga;
			case JPEG:
				return Strings.videoOptJpg;
			case DISABLED:
				return Strings.videoOptDisabled;
		}
		return null;
	}
}
