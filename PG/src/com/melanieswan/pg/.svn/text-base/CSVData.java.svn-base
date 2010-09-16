package com.melanieswan.pg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.res.AssetManager;

import com.melanieswan.pg.utils.CSVParser;
import com.melanieswan.pg.utils.MLog;
import com.melanieswan.pg.utils.Row;

public class CSVData {

	static final String TAG = "CSVData";
	
  // holds the studies csv data 
  static final String FILE_STUDIES = "studies.csv";
  // holds the companies csv data 
  static final String FILE_COMPS = "companies.csv";
  // holds the companies csv data 
  static final String FILE_CONDS = "conditions.csv";
  // holds the variants/snips csv data 
  static final String FILE_VARS = "variants.csv";


  // index files
  
  // holds the companies csv data 
  static final String FILE_STUDY_COND = "studies_conds.csv";

  // holds companies to condition to url data
  static final String FILE_COMP_COND_URL = "comps_conds_url.csv";
  
  // holds companies variant to condition data
  static final String FILE_VARS_COND = "vars_conds.csv";

  // map from variant/condition/company/study
  static final String FILE_VAR_COND_COMP_STUDY = "var_cond_comp_study.csv";
  
  /** no instances please */
  private CSVData() {}
  
  static ArrayList<String> companies;
  
  static ArrayList<String> conditions;
  
  static ArrayList<Study> studies;
  
  static ArrayList<Variant> variants;
  
  static HashMap<String,ArrayList<ConditionVariantTableRow>> conditionVariantTableMap;
  
  /**
   * remember: all indices from csv files start at 1
   * @param assets
   */
  public static void load(AssetManager assets) {
  	MLog.enable(TAG);
    try {
      // company csv: index, companyname
      ArrayList<Row> compdata = CSVParser.parse(assets.open(FILE_COMPS),true);
      companies = new ArrayList<String>(compdata.size()+1);
      companies.add(null);
      int ix = 1;
      for (Row row : compdata) {
//      	MLog.i(TAG,"read comp row: ",row,", ",row.getString(0));//," ",row.getString(1) );
        companies.add(ix++,row.getString(1));
      }
      // conditions csv : index,conditionname
      ArrayList<Row> conddata = CSVParser.parse(assets.open(FILE_CONDS),true);
      conditions = new ArrayList<String>(conddata.size()+1);
      conditions.add(null);
      ix = 1;
      for (Row row : conddata) {
        conditions.add(ix++,row.getString(1));
      }
      // studies csv: index, pubmedid, citation
      ArrayList<Row> studydata = CSVParser.parse(assets.open(FILE_STUDIES),true);
      studies = new ArrayList<Study>(studydata.size()+1);
      studies.add(null);
      ix = 1;
      for (Row row : studydata) {
        studies.add(ix++,new Study(row.getString(1),row.getString(2)));
      }
      // variants csv: index, rsid, locus, gene
      ArrayList<Row> variantdata = CSVParser.parse(assets.open(FILE_VARS),true);
      variants = new ArrayList<Variant>(variantdata.size()+1);
      variants.add(null);
      ix = 1;
      for (Row row : variantdata) {
      	if (row.getSize() == 3) {
      		row.add("");
      	}
//      	MLog.i(TAG, "variant data: ",row);
        variants.add(ix++,new Variant(row.getString(1),row.getString(2),row.getString(3)));
      }
      
      // now the constructed data
      conditionVariantTableMap = new HashMap<String,ArrayList<ConditionVariantTableRow>>();
      
      // var cond comp study csv: index, variantix, condix, compix, studyix
      ArrayList<Row> vccs = CSVParser.parse(assets.open(FILE_VAR_COND_COMP_STUDY),false);
      for (Row row : vccs) {
        Variant variant = variants.get(row.getInteger(1));
        String condition = conditions.get(row.getInteger(2));
        String company = companies.get(row.getInteger(3));
        Study study = studies.get(row.getInteger(4));
        ConditionVariantTableRow cvtr = findCVTR(condition,variant);
        cvtr.addCompanyStudy(company,study);
        //MLog.i(TAG, "add data for ",variant," ",condition," ",company," ",study);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * return table data for a condition
   * @param cond
   * @return
   */
  		
  static ArrayList<ConditionVariantTableRow> getConditionVariantTable(String cond) {
  	return conditionVariantTableMap.get(cond);
  }
  
  static ConditionVariantTableRow findCVTR(String cond, Variant var) {
//  	MLog.i(TAG, "cond variant table find for ",cond);
    ArrayList<ConditionVariantTableRow> all = conditionVariantTableMap.get(cond);
    if (all == null) {
      all = new ArrayList<ConditionVariantTableRow>();
      conditionVariantTableMap.put(cond,all);
    }
    ConditionVariantTableRow result = null;
    for (ConditionVariantTableRow cvtr : all) {
      if (cvtr.variant.equals(var)) {
        return cvtr;
      }
    }
    // none found, add one
    result = new ConditionVariantTableRow(cond, var);
    all.add(result);
    return result;
  }
  
  static int getCompanyIndex(String comp) {
  	return companies.indexOf(comp);
  }
  
}
