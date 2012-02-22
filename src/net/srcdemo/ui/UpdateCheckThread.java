package net.srcdemo.ui;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.srcdemo.Main;
import net.srcdemo.Strings;

import org.horrabin.horrorss.RssItemBean;
import org.horrabin.horrorss.RssParser;

import com.trolltech.qt.core.QCoreApplication;

class UpdateCheckThread extends Thread {
	private static class Version implements Comparable<Version> {
		private final static Pattern versionMatch = Pattern.compile("(\\d\\d\\d\\d)" + Strings.dateSeparator + "(\\d\\d)"
			+ Strings.dateSeparator + "(\\d\\d)(?:\\.exe)?$", Pattern.CASE_INSENSITIVE);
		private int day;
		private String downloadLink = null;
		private final boolean matches;
		private int month;
		private int year;

		private Version(final RssItemBean item) {
			this(item.getLink());
			downloadLink = item.getLink();
		}

		private Version(final String version) {
			final Matcher m = versionMatch.matcher(version);
			matches = m.find();
			if (matches) {
				year = Integer.parseInt(m.group(1));
				month = Integer.parseInt(m.group(2));
				day = Integer.parseInt(m.group(3));
			}
		}

		@Override
		public int compareTo(final Version other) {
			if (!matches) {
				return -1;
			}
			if (!other.matches) {
				return 1;
			}
			if (year != other.year) {
				return year > other.year ? 1 : -1;
			}
			if (month != other.month) {
				return month > other.month ? 1 : -1;
			}
			if (day != other.day) {
				return day > other.day ? 1 : -1;
			}
			return 0;
		}

		String getDownloadLink() {
			return downloadLink;
		}

		@Override
		public String toString() {
			return year + Strings.dateSeparator + month + Strings.dateSeparator + day;
		}
	}

	private static UpdateCheckThread previousInstance = null;
	private final AtomicBoolean invalidated = new AtomicBoolean(false);
	private final AboutTab parent;
	private final RssParser parser;

	UpdateCheckThread(final AboutTab parent) {
		this.parent = parent;
		if (previousInstance != null) {
			previousInstance.invalidate();
		}
		previousInstance = this;
		parser = new RssParser(Strings.urlUpdateFeed);
	}

	private void invalidate() {
		invalidated.set(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if (invalidated.get()) {
			return;
		}
		if (Main.version() == null) {
			setStatus(Strings.errUpdateInvalidVersion, false);
			return;
		}
		final Version selfVersion = new Version(Main.version());
		try {
			parser.parse();
		}
		catch (final Exception e) {
			setStatus(Strings.errUpdateContact, false);
			return;
		}
		if (invalidated.get()) {
			return;
		}
		Collection<RssItemBean> items = null;
		try {
			items = parser.getItems();
		}
		catch (final Exception e) {
			setStatus(Strings.errUpdateReading, false);
			return;
		}
		if (invalidated.get()) {
			return;
		}
		final SortedSet<Version> versions = new TreeSet<Version>();
		versions.add(selfVersion);
		for (final RssItemBean item : items) {
			versions.add(new Version(item));
		}
		if (versions.last() == selfVersion) {
			setStatus(Strings.lblUpdateIsUpToDate, false);
			return;
		}
		if (versions.last().getDownloadLink() == null) {
			setStatus("<a href=\"" + Strings.lblDefaultDownloadLink + "\">" + Strings.lblUpdateNewVersion + versions.last()
				+ "</a>" + Strings.lblUpdateNewVersionNoLink, true);
			return;
		}
		{
			setStatus("<a href=\"" + versions.last().getDownloadLink() + "\">" + Strings.lblUpdateNewVersion + versions.last()
				+ "</a>", true);
			return;
		}
	}

	private void setStatus(final String status, final boolean switchNow) {
		if (!invalidated.get()) {
			QCoreApplication.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (!invalidated.get()) {
						parent.onUpdateStatus(status, switchNow);
					}
				}
			});
		}
	}
}
