/*
 * Copyright (C) 2010 Preston Lacey http://javaflacencoder.sourceforge.net/ All Rights Reserved. This library is free software;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option) any later version. This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the
 * GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package net.sourceforce.javaflacencoder;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * FLAC_FileEncoder is a class to encode an input wav File to an output Flac file. It allows the EncodingConfiguration to be set
 * only once, prior to encoding the entire File.
 * 
 * @author Preston Lacey
 */
public class FLAC_FileEncoder
{
	/** Status enum for encode result */
	public enum Status
	{
		/** Generic file IO Error */
		FILE_IO_ERROR,
		/** Everything went well */
		FULL_ENCODE,
		/** Something unspecified went wrong... */
		GENERAL_ERROR,
		/**
		 * internal error is something that went haywire that was discovered due to internal sanity checks. A problem in API.
		 */
		INTERNAL_ERROR,
		/** No errors found. */
		OK,
		/** Error with output file */
		OUTPUT_FILE_ERROR,
		/** Unknown State. */
		UNKNOWN,
		/** File given was not able to be read */
		UNSUPPORTED_FILE,
		/** Sample size unsupported */
		UNSUPPORTED_SAMPLE_SIZE
	}

	/** Maximum number of bytes to read from file at once */
	private static final int MAX_READ = 16384;
	EncodingConfiguration ec = null;
	FLACEncoder flac = null;
	int lastTotalSamples = 0;
	File outFile = null;
	StreamConfiguration sc = null;
	boolean useThreads;

	/**
	 * Constructor creates a FLAC_FileEncoder object with default StreamConfiguration configuration and default
	 * EncodingConfiguration. Thread use defaults to true.
	 */
	public FLAC_FileEncoder()
	{
		flac = new FLACEncoder();
		sc = new StreamConfiguration();
		ec = new EncodingConfiguration();
		useThreads = true;
	}

	private void adjustConfigurations(final AudioFormat format)
	{
		final int sampleRate = (int) format.getSampleRate();
		final int sampleSize = format.getSampleSizeInBits();
		final int channels = format.getChannels();
		// int blockSize = sc.getMaxBlockSize();
		/*
		 * sc = new StreamConfiguration(channels, blockSize, blockSize, sampleRate, sampleSize);
		 */
		sc.setSampleRate(sampleRate);
		sc.setBitsPerSample(sampleSize);
		sc.setChannelCount(channels);
	}

	/**
	 * Encode the given input wav file to an output file.
	 * 
	 * @param inputFile
	 *            Input wav file to encode.
	 * @param outputFile
	 *            Output file to write FLAC stream to. If file exists, it will be overwritten without prompting.
	 * @return Status flag for encode
	 */
	public Status encode(final File inputFile, final File outputFile)
	{
		Status status = Status.FULL_ENCODE;
		outFile = outputFile;
		// take file and initial configuration.
		// open file
		AudioInputStream sin = null;
		AudioFormat format = null;
		// File inputFile = new File("encoderTest.wav");
		try {
			sin = AudioSystem.getAudioInputStream(inputFile);
		}
		catch (final IOException e) {
			status = Status.FILE_IO_ERROR;
		}
		catch (final UnsupportedAudioFileException e) {
			status = Status.UNSUPPORTED_FILE;
		}
		finally {
			if (status != Status.FULL_ENCODE) {
				return status;
			}
		}
		try {
			format = sin.getFormat();
			// sanitize and optimize configurations
			adjustConfigurations(format);
			// open stream
			openStream();
			final int frameSize = format.getFrameSize();
			final int sampleSize = format.getSampleSizeInBits();
			final int bytesPerSample = sampleSize / 8;
			if (sampleSize % 8 != 0) {
				// end processing now
				final Exception newEx = new Exception(Status.UNSUPPORTED_SAMPLE_SIZE.name());
				throw newEx;
			}
			final int channels = format.getChannels();
			final boolean bigEndian = format.isBigEndian();
			final byte[] samplesIn = new byte[MAX_READ];
			int samplesRead;
			int framesRead;
			final int[] sampleData = new int[MAX_READ * channels / frameSize];
			final int blockSize = sc.getMaxBlockSize();
			int unencodedSamples = 0;
			int totalSamples = 0;
			while ((samplesRead = sin.read(samplesIn, 0, MAX_READ)) != -1) {
				// System.err.println("Read: " + read);
				framesRead = samplesRead / (frameSize);
				if (bigEndian) {
					for (int i = 0; i < framesRead * channels; i++) {
						final int lower8Mask = 255;
						int temp = 0;
						int totalTemp = 0;
						for (int x = bytesPerSample - 1; x >= 0; x++) {
							final int upShift = 8 * x;
							if (x == 0) {
								temp = ((samplesIn[bytesPerSample * i + x]) << upShift);
							}
							else {
								temp = ((samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift);
							}
							totalTemp = totalTemp | temp;
						}
						sampleData[i] = totalTemp;
					}
				}
				else {
					for (int i = 0; i < framesRead * channels; i++) {
						final int lower8Mask = 255;
						int temp = 0;
						int totalTemp = 0;
						for (int x = 0; x < bytesPerSample; x++) {
							final int upShift = 8 * x;
							if (x == bytesPerSample - 1) {
								temp = ((samplesIn[bytesPerSample * i + x]) << upShift);
							}
							else {
								temp = ((samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift);
							}
							totalTemp = totalTemp | temp;
						}
						sampleData[i] = totalTemp;
					}
				}
				if (framesRead > 0) {
					flac.addSamples(sampleData, framesRead);
					unencodedSamples += framesRead;
				}
				// if(unencodedSamples > blockSize*100) {
				if (useThreads) {
					unencodedSamples -= flac.t_encodeSamples(unencodedSamples, false);
				}
				else {
					unencodedSamples -= flac.encodeSamples(unencodedSamples, false);
				}
				totalSamples += unencodedSamples;
				// unencodedSamples = 0;
				// }
				// System.err.println("read : "+ samplesRead);
			}
			totalSamples += unencodedSamples;
			if (useThreads) {
				unencodedSamples -= flac.t_encodeSamples(unencodedSamples, true);
			}
			else {
				unencodedSamples -= flac.encodeSamples(unencodedSamples, true);
			}
			// unencodedSamples = 0;
			lastTotalSamples = totalSamples;
		}
		catch (final IOException e) {
			status = Status.FILE_IO_ERROR;
		}
		catch (final Exception e) {
			status = Status.GENERAL_ERROR;
			final String message = e.getMessage();
			if (message == null) {
				e.printStackTrace();
			}
			else if (message.equals(Status.UNSUPPORTED_SAMPLE_SIZE.name())) {
				status = Status.UNSUPPORTED_SAMPLE_SIZE;
			}
		}
		// System.err.print("LastTotalSamples: "+lastTotalSamples);
		return status;
	}

	/**
	 * Get the total number of samples encoded in last encode. This is here primarily for use as a sanity check during
	 * debugging.
	 * 
	 * @return Total number of samples encoded in last encode attempt.
	 */
	public int getLastTotalSamplesEncoded()
	{
		return lastTotalSamples;
	}

	private Status openStream()
	{
		Status status = Status.OK;
		boolean result = flac.setStreamConfiguration(sc);
		result = result & flac.setEncodingConfiguration(ec);
		if (!result) {
			status = Status.INTERNAL_ERROR;
		}
		else {
			FLACFileOutputStream fout = null;
			try {
				fout = new FLACFileOutputStream(outFile.getPath());
			}
			catch (final IOException e) {
				status = Status.OUTPUT_FILE_ERROR;
				e.printStackTrace();
			}
			if (status == Status.OK) {
				flac.setOutputStream(fout);
				try {
					flac.openFLACStream();
				}
				catch (final IOException e) {
					status = Status.INTERNAL_ERROR;
				}
			}
			else {
				status = Status.OUTPUT_FILE_ERROR;
			}
		}
		return status;
	}

	/**
	 * Set the EncodingConfiguration to use for encoding.
	 * 
	 * @param config
	 *            EncodingConfiguration to use.
	 */
	public void setEncodingConfig(final EncodingConfiguration config)
	{
		ec = config;
	}

	/**
	 * Set the stream configuration for this encoder to use. Note that the audio characteristics(number of channels, sample
	 * rate, and sample size), will be set to match the input file at encode time, so needn't be set in the given
	 * StreamConfiguration object.
	 * 
	 * @param config
	 *            StreamConfiguration to use for encoding.
	 */
	public void setStreamConfig(final StreamConfiguration config)
	{
		sc = config;
	}

	/**
	 * Specify whether to use multiple threads or not.
	 * 
	 * @param val
	 *            true to use threads, false otherwise.
	 */
	public void useThreads(final boolean val)
	{
		useThreads = val;
	}
}
