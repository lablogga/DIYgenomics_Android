package com.melanieswan.pg.utils;

import java.util.ArrayList;

public class Row {

  ArrayList<String> data;
  
  
  public Row() {
    data = new ArrayList<String>();
  }
  
  public int getSize() {
  	return data.size();
  }
  
  public void add(String s) {
    data.add(s);
  }
  
  public int getInteger(int index) {
    return Integer.parseInt(data.get(index));
  }
  
  public String getString(int index) {
    return data.get(index);
  }
  
  public String toString() {
  	StringBuilder sb = new StringBuilder();
  	int ix = 0;
  	for (String s : data) {
  		if (ix > 0) {
  			sb.append(",");
  		}
  		sb.append(s);
  		ix++;
  	}
  	return sb.toString();
  }
}
