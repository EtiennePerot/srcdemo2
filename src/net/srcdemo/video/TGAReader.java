package net.srcdemo.video;

class TGAReader {
	private final byte[] data;
	private boolean headerDecoded = false;
	private int height = -1;
	private int numPixels = -1;
	private int offset = 0;
	private int width = -1;

	TGAReader(final byte[] data) {
		this.data = data;
	}

	public void addToArray(final int[] pixels) {
		decodeHeader();
		int i = 0;
		final int maxValues = numPixels * 3;
		if (data[2] == 0x02 && data[16] == 0x20) {
			while (i < maxValues) {
				pixels[i++] += read(data);
				pixels[i++] += read(data);
				pixels[i++] += read(data);
				skip(1); // Ignore alpha
			}
		} else if (data[2] == 0x02 && data[16] == 0x18) {
			while (i < maxValues) {
				pixels[i++] += read(data);
				pixels[i++] += read(data);
				pixels[i++] += read(data);
			}
		} else {
			while (i < maxValues) {
				int nb = read(data);
				if ((nb & 0x80) == 0) {
					for (int j = 0; j <= nb; j++) {
						pixels[i++] += read(data);
						pixels[i++] += read(data);
						pixels[i++] += read(data);
					}
				} else {
					nb &= 0x7f;
					final int b = read(data);
					final int g = read(data);
					final int r = read(data);
					for (int j = 0; j <= nb; j++) {
						pixels[i++] += b;
						pixels[i++] += g;
						pixels[i++] += r;
					}
				}
			}
		}
	}

	void decodeHeader() {
		if (headerDecoded) {
			return;
		}
		skip(12);
		width = read(data) + (read(data) << 8);
		height = read(data) + (read(data) << 8);
		numPixels = width * height;
		skip(2);
		headerDecoded = true;
	}

	int getHeight() {
		decodeHeader();
		return height;
	}

	int getNumPixels() {
		decodeHeader();
		return numPixels;
	}

	int getWidth() {
		decodeHeader();
		return width;
	}

	private int read(final byte[] buf) {
		final int b = buf[offset++];
		return (b < 0 ? 256 + b : b);
	}

	private void skip(final int bytes) {
		offset += bytes;
	}
}
