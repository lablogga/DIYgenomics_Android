package com.melanieswan.pg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


@SuppressWarnings("serial")
public class MappingItem implements Serializable {

  public String item;
  private Variant mVariant;
  private int mVariantIndex;

  public HashMap<String,ArrayList<Study>> companyRefs;
  
  public MappingItem(String catitem, int variantIndex, Variant variant) {
    item = catitem;
    mVariant = variant;
    companyRefs = new HashMap<String,ArrayList<Study>>();
    mVariantIndex = variantIndex;
  }
  
  public ArrayList<String> getCompanies() {
  	ArrayList<String> res = new ArrayList<String>(6);
  	for (String comp : companyRefs.keySet()) {
  		res.add(comp);
  	}
  	return res;
  }
  
  public void addCompanyStudy(String company, Study study) {
    ArrayList<Study> slist = companyRefs.get(company);
    if (slist == null) {
      slist = new ArrayList<Study>();
      companyRefs.put(company,slist);
    }
    slist.add(study);
  }

  public int getVariantIndex() {
	  return mVariantIndex;
  }
  
  public Variant getVariant() {
  	return mVariant;
  }

  public boolean hasStudy(String company) {
    return (companyRefs.get(company) != null);
  }
  
  public boolean hasCompanyStudy(String company, Study study) {
  	ArrayList<Study> sts = companyRefs.get(company);
  	if (sts != null) {
  		return sts.contains(study);
  	}
  	return false;
  }
  
  public int getCompanyStudiesCount(String company) {
	  ArrayList<Study> sts = companyRefs.get(company);
	  if (sts != null) {
		  return sts.size();
	  } else {
		  return 0;
	  }
  }
  
  
}
