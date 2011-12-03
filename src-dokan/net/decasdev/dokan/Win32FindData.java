/*
 * JDokan : Java library for Dokan Copyright (C) 2008 Yu Kobayashi http://yukoba.accelart.jp/ http://decas-dev.net/en This
 * program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.decasdev.dokan;

/** WIN32_FIND_DATA */
public class Win32FindData
{
	public String alternateFileName;
	/** FILETIME */
	public long creationTime;
	public int fileAttributes;
	public String fileName;
	public long fileSize;
	/** FILETIME */
	public long lastAccessTime;
	/** FILETIME */
	public long lastWriteTime;
	public int reserved0;
	public int reserved1;

	public Win32FindData()
	{
	}

	public Win32FindData(final int fileAttributes, final long creationTime, final long lastAccessTime,
			final long lastWriteTime, final long fileSize, final int reserved0, final int reserved1, final String fileName,
			final String alternateFileName)
	{
		this.fileAttributes = fileAttributes;
		this.creationTime = creationTime;
		this.lastAccessTime = lastAccessTime;
		this.lastWriteTime = lastWriteTime;
		this.fileSize = fileSize;
		this.reserved0 = reserved0;
		this.reserved1 = reserved1;
		this.fileName = fileName;
		this.alternateFileName = alternateFileName;
	}

	@Override
	public String toString()
	{
		return "Win32FindData(" + "fileAttributes=" + fileAttributes + "," + "lastAccessTime=" + lastAccessTime + ","
				+ "lastWriteTime=" + lastWriteTime + "," + "fileSize=" + fileSize + "," + "reserved0=" + reserved0 + ","
				+ "reserved1=" + reserved1 + "," + "fileName=" + fileName + "," + "alternateFileName=" + alternateFileName
				+ ")";
	}
}
