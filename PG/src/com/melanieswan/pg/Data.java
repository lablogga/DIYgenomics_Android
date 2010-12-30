package com.melanieswan.pg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.melanieswan.pg.utils.CSVParser;
import com.melanieswan.pg.utils.MLog;
import com.melanieswan.pg.utils.Row;

public class Data {

	static final String TAG = "CSVData";

	// holds the studies csv data
	static final String FILE_STUDIES = "studies.csv";
	//Key,PMID,Citation
	static final int STUDIES_PMID = 1;
	static final int STUDIES_CITATION = 2;
	
	// holds the companies csv data
	static final String FILE_COMPS = "companies.csv";
	// key, name
	static final int COMPANIES_NAME = 1;
	
	// holds the companies csv data
	static final String FILE_CONDS = "conditions.csv";
	
	// holds the variants/snips csv data
	static final String FILE_VARS = "variants.csv";
	// Key,Variant,Locus,Gene,23andMe_normal,rank
	
	static final int VAR_KEY = 0;
	static final int VAR_RSID = 1;
	static final int VAR_LOCUS = 2;
	static final int VAR_GENE = 3;
	static final int VAR_GTYPE = 4;
	static final int VAR_RANK = 5;
	
	
	// map from variant/categoryitem*/company/study
	static final String FILE_MAPPING = "mapping.csv";
	
	// Key,Variant_key,Company_key,Study_key,Condition_key,Drug_key,Athperf_cat_key, product
	static final int MAP_VARIANT = 1;
	static final int MAP_COMPANY = 2;
	static final int MAP_STUDY = 3;
	static final int MAP_CONDITION = 4;
	static final int MAP_DRUG = 5;
	static final int MAP_ATHPERF = 6;
	static final int MAP_PRODUCT = 7;
	
	// personal genotype data
	static final String FILE_GENOME = "genome.csv";
	
	static final int GENOME_VARIANT = 0;
	static final int GENOME_GTYPE = 1;

	private ArrayList<Category> categories;
	private ArrayList<String> companies;
	private ArrayList<Study> studies;
	private Map<Integer, Variant> variants;
	private Map<String, Object> variantMap;
	private Map<String, Genome> genome;
	private HashMap<String, ArrayList<MappingItem>> categoryItemMapping;
	private static Data mInstance;
	
	public static Data getInstance() {
		if (mInstance == null) {
			mInstance = new Data();
		}
		return mInstance;
	}
	
	/** no instances please */
	private Data() {
		categories = new ArrayList<Category>(10);
	}

	public List<Category> getCategories() {
		return categories;
	}

	public Map<String, Genome> getGenotype() {
		return genome;
	}
	
	public Map<String, Object> getVariantMap() {
		return variantMap;
	}
	
	public List<String> getCompanies() {
		return companies;
	}
	
	public void loadGenome(Context ctx) {
		try {
			File file = ctx.getFileStreamPath(FILE_GENOME);
			if (file.exists()) {
				ArrayList<Row> gdata = CSVParser.parse(
						new FileInputStream(file.getAbsolutePath()), false);
				genome = new HashMap<String, Genome>(gdata.size());
				for (Row row : gdata) {
					String variant = row.getString(GENOME_VARIANT);
					String gtype = row.getString(GENOME_GTYPE);
					if ((gtype != null) && (gtype.length() > 0)) {
						boolean correct = true;
						for (int i = 0; i < gtype.length(); i++) {
							correct = correct
									& ("ACGTID".indexOf(gtype.charAt(i)) != -1);
							if (!correct)
								break;
						}
						if (correct) {
							genome.put(variant, new Genome(variant, gtype));
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadCategory(AssetManager assets, String name, String file,
			int mapindex) {
		Category cat = new Category(name, mapindex);
		categories.add(cat);
		try {
			cat.load(assets, file);
		} catch (IOException e) {
			Log.e("genomics", "error loading category " + name + ": " + e);
			// drop silently
			e.printStackTrace();
		}
	}

	/**
	 * remember: all indices from csv files start at 1
	 * 
	 * @param assets
	 */
	public void load(AssetManager assets) {
		MLog.enable(TAG);
		try {
			// load all categories

			// company csv: index, companyname
			ArrayList<Row> compdata = CSVParser.parse(assets.open(FILE_COMPS),
					false);
			companies = new ArrayList<String>(compdata.size() + 1);
			companies.add(null);
			int ix = 1;
			for (Row row : compdata) {
				// MLog.i(TAG,"read comp row: ",row,", ",row.getString(0));//," ",row.getString(1)
				// );
				companies.add(ix++, row.getString(COMPANIES_NAME));
			}
			// studies csv: index, pubmedid, citation
			ArrayList<Row> studydata = CSVParser.parse(
					assets.open(FILE_STUDIES), false);
			studies = new ArrayList<Study>(studydata.size() + 1);
			studies.add(null);
			ix = 1;
			for (Row row : studydata) {
				studies.add(ix++, new Study(row.getString(STUDIES_PMID),
						row.getString(STUDIES_CITATION)));
			}
			// variants csv: index, rsid, locus, gene
			ArrayList<Row> variantdata = CSVParser.parse(
					assets.open(FILE_VARS), false);
			variants = new HashMap<Integer, Variant>(variantdata.size() + 1);
			variantMap = new HashMap<String, Object>(variantdata.size());
			for (Row row : variantdata) {
				// MLog.i(TAG, "variant data: ",row);
				if (Variant.isValidLocus(row.getString(VAR_LOCUS))) {
					// variants get keyed by their database key
					// there are gaps in the data because not all is exported to the app
					// the rsid gets whitespace removed to remove the hack for the web app
					String rsid = row.getString(VAR_RSID).trim();
					variants.put(row.getInteger(VAR_KEY),
							new Variant(rsid, row.getString(VAR_LOCUS), row.getString(VAR_GENE),
									row.getString(VAR_GTYPE), row.getInteger(VAR_RANK)));
					variantMap.put(rsid, rsid);
				}
			}

			// now the constructed data
			categoryItemMapping = new HashMap<String, ArrayList<MappingItem>>();

			ArrayList<Row> mappings = CSVParser.parse(assets.open(FILE_MAPPING),
					false);

			for (Row row : mappings) {
				// MLog.i(TAG, "row: ",row);
				int variantIndex = row.getInteger(MAP_VARIANT);
				String company = companies.get(row.getInteger(MAP_COMPANY));
				Study study = studies.get(row.getInteger(MAP_STUDY));
				// now read the category item keys
				for (Category cat : categories) {
					int catitemindex = row.getInteger(cat.getMapIndex());
					if (catitemindex != 0) {
						// the get method adjusts already, so compensate
						String catitem = cat.get(catitemindex - 1); 
						MappingItem mapitem = findMappingItem(catitem, variantIndex);
						mapitem.addCompanyStudy(company, study);
					}
				}
				// MLog.i(TAG,
				// "add data for ",variant," ",condition," ",company," ",study);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * return table data for a condition
	 * 
	 * @param cond
	 * @return
	 */

	ArrayList<MappingItem> getCategoryItemVariantTable(
			String catItem) {
		return categoryItemMapping.get(catItem);
	}

	MappingItem findMappingItem(String catItem, int variantindex) {
		// MLog.i(TAG, "cond variant table find for ",cond);
		ArrayList<MappingItem> all = categoryItemMapping
				.get(catItem);
		if (all == null) {
			all = new ArrayList<MappingItem>();
			categoryItemMapping.put(catItem, all);
		}
		MappingItem result = null;
		for (MappingItem mapping : all) {
			if (mapping.getVariantIndex() == variantindex) {
				return mapping;
			}
		}
		// none found, add one
		result = new MappingItem(catItem, variantindex, variants.get(variantindex));
		all.add(result);
		return result;
	}

	int getCompanyIndex(String comp) {
		return companies.indexOf(comp);
	}

	interface LoaderCallback {
		public void done(Data data);
	}
}
