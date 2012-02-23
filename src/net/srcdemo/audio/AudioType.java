package net.srcdemo.audio;

import net.srcdemo.EnumUtils;
import net.srcdemo.Strings;

public enum AudioType {
	BUFFERED, DISABLED, DISK, FLAC, VORBIS;
	static {
		final AudioType[] order = { DISK, BUFFERED, FLAC, VORBIS, DISABLED };
		EnumUtils.registerOrder(AudioType.class, order);
	}

	public String getDescription() {
		switch (this) {
			case DISABLED:
				return Strings.audioOptDisabledExplanation;
			case BUFFERED:
				return Strings.audioOptBufferedExplanation;
			case FLAC:
				return Strings.audioOptFlacExplanation;
			case VORBIS:
				return Strings.audioOptVorbisExplanation;
			case DISK:
				return Strings.audioOptDiskExplanation;
		}
		return null;
	}

	public String getLabel() {
		switch (this) {
			case DISABLED:
				return Strings.audioOptDisabled;
			case BUFFERED:
				return Strings.audioOptBuffered;
			case FLAC:
				return Strings.audioOptFlac;
			case VORBIS:
				return Strings.audioOptVorbis;
			case DISK:
				return Strings.audioOptDisk;
		}
		return null;
	}

	public boolean requiresBuffer() {
		switch (this) {
			case DISABLED:
			case DISK:
				return false;
			default:
				return true;
		}
	}
}
