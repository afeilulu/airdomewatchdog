package com.afeilulu.airdomewatchdog.moudle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseFeedParser implements FeedParser {

	// names of the XML tags
//	static final String CHANNEL = "channel";
//	static final String PUB_DATE = "pubDate";
//	static final  String DESCRIPTION = "description";
//	static final  String LINK = "link";
//	static final  String TITLE = "title";
//	static final  String ITEM = "item";
	
	static final String DATA = "data";
	static final String NEXTTIME = "NextTime";
	static final String PERIOD = "Period";
	static final String PASSWORD = "Password";
	static final String DIGITAL = "Digital";
	static final String ANALOG = "Analog";
	
	private final URL feedUrl;

	protected BaseFeedParser(String feedUrl){
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream getInputStream() {
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}