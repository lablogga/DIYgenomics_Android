package org.diygenomics.pg;

import android.view.View;
import android.widget.TextView;

public class DotUtils {

	private DotUtils() {}
	
	public static void populateDotViews(View convertView, MappingItem row, Study study, Data data) {
		TextView dot1 = (TextView) convertView.findViewById(R.id.dot1);
		TextView dot2 = (TextView) convertView.findViewById(R.id.dot2);
		TextView dot3 = (TextView) convertView.findViewById(R.id.dot3);
		TextView dot4 = (TextView) convertView.findViewById(R.id.dot4);
		TextView dot5 = (TextView) convertView.findViewById(R.id.dot5);
		TextView dot6 = (TextView) convertView.findViewById(R.id.dot6);
		TextView[] dots = new TextView[] {
			dot1, dot2, dot3, dot4, dot5, dot6
		};
		for (View v : dots) {
			v.setVisibility(View.GONE);
		}
		for (String comp : row.getCompanies()) {
			int index = data.getCompanyIndex(comp);
			if (study == null) {
				// interested in total number 
				int nstudies = row.getCompanyStudiesCount(comp);
				dots[index - 1].setVisibility(View.VISIBLE);
				dots[index - 1].setText(""+nstudies);
			} else {
				if (row.hasCompanyStudy(comp, study)) {
					dots[index - 1].setVisibility(View.VISIBLE);
					dots[index - 1].setText("");
				}
			}
		}
	}

	public static void populateCompanyNames(View convertView, Data data) {
		TextView comp1 = (TextView) convertView.findViewById(R.id.comp1);
		TextView comp2 = (TextView) convertView.findViewById(R.id.comp2);
		TextView comp3 = (TextView) convertView.findViewById(R.id.comp3);
		TextView comp4 = (TextView) convertView.findViewById(R.id.comp4);
		TextView comp5 = (TextView) convertView.findViewById(R.id.comp5);
		TextView comp6 = (TextView) convertView.findViewById(R.id.comp6);
		TextView[] dots = new TextView[] {
			comp1, comp2, comp3, comp4, comp5, comp6
		};
		for (View v : dots) {
			v.setVisibility(View.GONE);
		}
		int ix = 0;
		for (String comp : data.getCompanies()) {
			if (comp != null) {
				dots[ix].setText(comp);
				dots[ix].setVisibility(View.VISIBLE);
				ix++;
			}
		}
	}

	
}
