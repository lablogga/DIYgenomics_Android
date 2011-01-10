package org.diygenomics.pg.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLog {

	static boolean globalEnabled = true;
	
	static boolean enabled = false;
	static Set<String> _map = new HashSet<String>();

	private MLog() {
	}

	public static void setGlobalEnabled(boolean f) {
		globalEnabled = f;
		if (!f) {
			Logger.getLogger("com.skyfire").setLevel(Level.OFF);
		} else {
			Logger.getLogger("com.skyfire").setLevel(Level.INFO);
		}
	}
	
	public static boolean getGlobalEnabled() {
		return globalEnabled;
	}
	
	public static void enable() {
		enabled = true;
	}

	public static void disable() {
		enabled = false;
	}

	public static void enable(String tag) {
		_map.add(tag);
	}

	public static void disable(String tag) {
		_map.remove(tag);
	}

	private static boolean isEnabled(String tag) {
		return globalEnabled && (_map.contains(tag) || enabled);
	}

	public static void setLogging(boolean f) {
		enabled = f;
	}

	public static void i(String tag, Object... vals) {
		if (isEnabled(tag)) {
			//Log.i(tag, buildString(vals));
			Logger.getLogger(tag).log(Level.INFO, buildString(vals));
		}
	}

	public static void e(String tag, Object... vals) {
		if (isEnabled(tag)) {
			//Log.e(tag, buildString(vals));
			Logger.getLogger(tag).log(Level.SEVERE, buildString(vals));
		}
	}

	private static String buildString(Object... strings) {
		StringBuilder sb = new StringBuilder();
		for (Object s : strings) {
			sb.append(s);
		}
		return sb.toString();
	}

}
