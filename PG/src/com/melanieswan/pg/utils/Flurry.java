package com.melanieswan.pg.utils;

import java.util.HashMap;
import java.util.Map;

public class Flurry {

	public static final String KEY = "KQQKPFJ5T6HBYS63WC6H";
	
	public static final String EVENT_CATEGORIES = "view categories";
	public static final String EVENT_CATITEM = "view catitem";
	public static final String EVENT_VARIANT = "view variant";
	public static final String EVENT_STUDY = "view study";

	private static Map<String, String> smap = new HashMap<String, String>(3);
	
	public static Map<String, String> map(String key, String val) {
		smap.clear();
		smap.put(key, val);
		return smap;
	}
}
