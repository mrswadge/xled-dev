package net.sbs.xled.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public enum LogFactory {
	instance;
	static {
		try {
			InputStream stream = Boot.class.getClassLoader().getResourceAsStream("logging.properties");
			LogManager.getLogManager().readConfiguration(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Logger getLogger(String name) {
		return Logger.getLogger(name);
	}
}
