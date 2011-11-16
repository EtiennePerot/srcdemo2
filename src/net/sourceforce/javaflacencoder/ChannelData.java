/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package net.sourceforce.javaflacencoder;

/**
 * @author preston
 */
public class ChannelData
{
	public enum ChannelName
	{
		INDEPENDENT, LEFT, MID, RIGHT, SIDE
	}

	private int count;
	private ChannelName name;
	private int[] samples = null;
	private int sampleSize;

	public ChannelData(final int[] samples, final int count, final int sampleSize, final ChannelName n)
	{
		this.count = count;
		this.samples = samples;
		this.sampleSize = sampleSize;
		name = n;
	}

	public ChannelName getChannelName()
	{
		return name;
	}

	public int getCount()
	{
		return count;
	}

	public int[] getSamples()
	{
		return samples;
	}

	public int getSampleSize()
	{
		return sampleSize;
	}

	public void setChannelName(final ChannelName cn)
	{
		name = cn;
	}

	public int setCount(final int count)
	{
		this.count = (count <= samples.length) ? count : samples.length;
		return this.count;
	}

	public int setData(final int[] newSamples, final int count, final int sampleSize, final ChannelName n)
	{
		samples = newSamples;
		this.sampleSize = sampleSize;
		name = n;
		return setCount(count);
	}
}
