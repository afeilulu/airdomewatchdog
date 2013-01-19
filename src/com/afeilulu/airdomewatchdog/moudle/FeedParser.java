package com.afeilulu.airdomewatchdog.moudle;

import java.util.List;

import com.afeilulu.airdomewatchdog.io.Message;

public interface FeedParser {
	List<Message> parse();
}
