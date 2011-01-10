package org.diygenomics.pg;

import java.io.IOException;
import java.util.ArrayList;

import org.diygenomics.pg.utils.CSVParser;
import org.diygenomics.pg.utils.Row;


import android.content.res.AssetManager;

/**
 * simple category data holds a list of values and defines its index in the map
 * data
 */
public class Category {

	static final int CATITEM_NAME = 1;
	
	ArrayList<String> mValues;
	String mName;
	int mMapIndex;

	public Category(String name, int mapindex) {
		mName = name;
		mMapIndex = mapindex;
	}

	public String getName() {
		return mName;
	}

	public int getMapIndex() {
		return mMapIndex;
	}

	public void load(AssetManager assets, String csvfile) throws IOException {
		// company csv: index, companyname
		ArrayList<Row> data = CSVParser.parse(assets.open(csvfile), false);
		mValues = new ArrayList<String>(data.size() + 1);
		mValues.add(null); // make index start at 1 to match DB tables
		int ix = 1;
		for (Row row : data) {
			mValues.add(ix++, row.getString(CATITEM_NAME));
		}
	}
	
	public int size() {
		return mValues.size() - 1;
	}
	
	public String get(int ix) {
		return mValues.get(ix + 1);
	}

}
