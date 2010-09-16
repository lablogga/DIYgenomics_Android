package com.melanieswan.pg;

import java.io.Serializable;

import com.melanieswan.pg.utils.MLog;

/**
 * holds a variant (formerly known as snip)
 */
public class Variant implements Serializable {

	static final String TAG = "Variant";
	
  public  String RSID;
  public  String locus;
  public  String gene;
  
  private  Locus parsedLocus;
  
  public Variant() {
  }
  
  public Variant(String id,String l,String g) {
    RSID = id;
    locus = l;
    gene = g;
    parsedLocus = new Locus(l);
  }

	public int compareTo(Variant variant2) {
		
//		int order = this.locus.compareTo(variant2.locus);
		int order = parsedLocus.compareTo(variant2.parsedLocus);
		if (order == 0)	order = this.gene.compareTo(variant2.gene);
		if (order == 0)	order = this.RSID.compareTo(variant2.RSID);
		return order;
	}
	
	class Locus implements Serializable {
		
		public int part1;
		public String part2;
		public String part3;
		
		public Locus() {
		}
		
		public Locus(String lstring) {
			if (lstring != null) {
				if (lstring.startsWith("X") || lstring.startsWith("Y")) {
					if (lstring.startsWith("X")) { 
						part1 = 23;
					} else {
						part1 = 24;
					}
					lstring = lstring.substring(1);
					int firstLetter = 1;
					int firstDigit = findDigit(lstring,firstLetter+1);
					if (firstDigit != -1) {
						part2 = lstring.substring(firstLetter,firstDigit);
						part3 = lstring.substring(firstDigit);
					} else {
						part2 = lstring.substring(firstLetter);
					}
				} else {
				int firstLetter = Variant.findLetter(lstring,0);
				if (firstLetter == -1) {
					part1 = Integer.parseInt(lstring);
				} else {
					part1 = Integer.parseInt(lstring.substring(0,firstLetter));
					int firstDigit = findDigit(lstring,firstLetter+1);
					if (firstDigit != -1) {
						part2 = lstring.substring(firstLetter,firstDigit);
						part3 = lstring.substring(firstDigit);
					} else {
						part2 = lstring.substring(firstLetter);
					}
					
				}
				
			}	
			}
			MLog.i(TAG, this.toString());
		}
		
		public int compareTo(Locus other) {
			int res = (part1 > other.part1) ? 1 : ((part1 < other.part1) ? -1 : 0);
			if (res == 0) {
				if (part2 == null) {
					if (other.part2 == null) return 0;
					return -1;
				} else {
					if (other.part2 == null) {
						return 1;
					}	else {
						return part2.compareTo(other.part2);
					}
				}
			}
			return res;
		}
		
		public String toString() {
			return part1+"/"+part2+"/"+part3;
		}
			
		
	}
	
	static int findLetter(String s,int start) {
		if (s == null) return -1;
		int i = start; 
		while (i < s.length()) {
			char c = s.charAt(i);
			if (!Character.isDigit(c)) {
				return i;
				
			}
			i++;
		}
		return -1;
		
	}

	static int findDigit(String s,int start) {
		if (s == null) return -1;
		int i = start; 
		while (i < s.length()) {
			char c = s.charAt(i);
			if (Character.isDigit(c)) {
				return i;
				
			}
			i++;
		}
		return -1;
		
	}

}