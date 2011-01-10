package org.diygenomics.pg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.diygenomics.pg.utils.Row;


public class CSVParser {
  
  static final String TAG = "CSVParser";
  
  private CSVParser() {
  };
  
  
  public static ArrayList<Row> parse(InputStream in, boolean hasHeader) {
  	MLog.enable(TAG);
    ArrayList<Row> lines = new ArrayList<Row>();
    BufferedReader reader  = new BufferedReader(new InputStreamReader(in),1024);
    String line = null;
    try {
      if (hasHeader) {
        // read off first line with table headers
        line = reader.readLine();
      }
      String del = ",";
      int ix = 0;
      while ((line = reader.readLine()) != null) {
        Row row = new Row(); 
        StringTokenizer st = new StringTokenizer(line,del, true);
        boolean lastWasDelimiter = false;
        while (st.hasMoreTokens()) {
        	String tok = st.nextToken();
        	
        	if (tok.equals(del)) {
        		if (lastWasDelimiter) {
        			row.add("");
        		}
        		lastWasDelimiter = true;
        	} else {
        		row.add(tok);
        		lastWasDelimiter = false;
        	}
        }
        if (lastWasDelimiter) {
        	// compensate for missing last element
        	row.add("");
        }
        lines.add(row);
        ix++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  
  
}
