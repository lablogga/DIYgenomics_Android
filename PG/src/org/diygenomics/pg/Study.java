package org.diygenomics.pg;

import java.io.Serializable;

/**
 * a study referenced by a company
 */
public class Study implements Serializable, Comparable {

	static final String PUBMED_PREFIX = "http://www.ncbi.nlm.nih.gov/pubmed/";

	public String citation;
	public String pubmedid;

	public Study() {
	}

	/**
	 * create a new study object given its citation string and the pubmedid
	 * 
	 * @param id
	 *            the pubmedid
	 * @param citation
	 */
	public Study(String id, String cit) {
		citation = cit;
		pubmedid = id;
	}

	public int compareTo(Study other) {
		return citation.compareTo(other.citation);
	}

	public String getUrl() {
		return PUBMED_PREFIX + pubmedid;
	}

	public String toString() {
		return pubmedid + "/" + citation;
	}

	@Override
	public int compareTo(Object obj) {
		if (obj instanceof Study) {
			return compareTo((Study) obj);
		} else {
			return -1;
		}
	}

}
