package com.melanieswan.pg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class ConditionVariantTableRow implements Serializable {

  public String condition;
  public Variant variant;
  

  public HashMap<String,ArrayList<Study>> companyRefs;
  
  public ConditionVariantTableRow(String cond, Variant vari) {
    condition = cond;
    variant = vari;
    companyRefs = new HashMap<String,ArrayList<Study>>();
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

  public Variant getVariant() {
  	return variant;
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
  
  
}
