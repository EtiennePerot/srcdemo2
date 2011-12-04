/********************************************************************
 * * Utility class that encodes and decodes byte arrays in Base64 * representation. * * Should be thread safe. * Credit:
 * markefarr * http://forum.java.sun.com/thread.jspa?threadID=477461 * * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2002 *
 * by the Xiph.Org Foundation http://www.xiph.org/ * *
 ********************************************************************/
package org.xiph.libshout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class Base64EncoderDecoder extends AbstractPreferences
{
	private final Map<String, String> encodedStore = new HashMap<String, String>();

	public Base64EncoderDecoder(final AbstractPreferences prefs, final String string)
	{
		super(prefs, string);
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException
	{
		return null;
	}

	// dummy implementation as AbstractPreferences is extended to access methods above
	@Override
	protected AbstractPreferences childSpi(final String name)
	{
		return null;
	}

	public String decodeBase64(final String key, final String base64String) throws UnsupportedEncodingException, IOException
	{
		final byte[] def = { (byte) 'D', (byte) 'E', (byte) 'F' };// placeholder
		encodedStore.put(key, base64String);
		final byte[] byteResults = getByteArray(key, def);
		return new String(byteResults, "UTF8");
	}

	public String encodeBase64(final String raw) throws UnsupportedEncodingException
	{
		final byte[] rawUTF8 = raw.getBytes("UTF8");
		putByteArray(raw, rawUTF8);
		return encodedStore.get(raw);
	}

	public String encodeBase64(final String key, final String raw) throws UnsupportedEncodingException
	{
		final byte[] rawUTF8 = raw.getBytes("UTF8");
		putByteArray(key, rawUTF8);
		return encodedStore.get(key);
	}

	@Override
	protected void flushSpi() throws BackingStoreException
	{
	}

	@Override
	public String get(final String key, final String def)
	{
		return encodedStore.get(key);
	}

	@Override
	protected String getSpi(final String key)
	{
		return null;
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException
	{
		return null;
	}

	@Override
	public void put(final String key, final String value)
	{
		encodedStore.put(key, value);
	}

	@Override
	protected void putSpi(final String key, final String value)
	{
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException
	{
	}

	@Override
	protected void removeSpi(final String key)
	{
	}

	@Override
	protected void syncSpi() throws BackingStoreException
	{
	}
}
