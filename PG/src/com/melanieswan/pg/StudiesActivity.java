package com.melanieswan.pg;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.melanieswan.pg.utils.MLog;

public class StudiesActivity extends Activity {

	static final String TAG = "sa";
	
	View mMainView;
	
	ListView list;
	
	
	ArrayList<Study> studies;

	ConditionVariantTableRow row;
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		MLog.enable(TAG);
		studies = new ArrayList<Study>();
		row = (ConditionVariantTableRow)
			getIntent().getExtras().getSerializable(ConditionTableActivity.EXTRA_ROW);
		for (String comp : row.companyRefs.keySet()) {
			ArrayList<Study> sl = row.companyRefs.get(comp);
			for (Study st : sl) {
				if (!studies.contains(st)) {
					studies.add(st);
				}
			}
		}
		Collections.sort(studies);
		String condition = getIntent().getStringExtra(Main.EXTRA_COND);
		mMainView = getLayoutInflater().inflate(R.layout.studytable,null);
		TextView title = (TextView)mMainView.findViewById(R.id.studies_title);
		title.setText("References: "+condition+", "+row.variant.RSID);
    TextView legend1 = (TextView)mMainView.findViewById(R.id.deCODEme);
    legend1.setTextColor(Constants.COLOR_COMP1);
    TextView legend2 = (TextView)mMainView.findViewById(R.id.Navigenics);
    legend2.setTextColor(Constants.COLOR_COMP2);
    TextView legend3 = (TextView)mMainView.findViewById(R.id.Twenty3andme);
    legend3.setTextColor(Constants.COLOR_COMP3);
    list = (ListView)mMainView.findViewById(R.id.studies_list);
    list.setAdapter(new StudyAdapter());
    list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(studies.get(pos).getUrl()));
				startActivity(intent);
				
			}
    });

		setContentView(mMainView);
		
	}
	
	
	
	class StudyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return studies.size();
		}

		@Override
		public Object getItem(int pos) {
			return studies.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.studyitem, null);
			}
			Study study = studies.get(position);
			TextView citation = (TextView)convertView.findViewById(R.id.studyitem_citation);
			citation.setText(study.citation);
			TextView pubmed = (TextView)convertView.findViewById(R.id.studyitem_pubmedid);
			pubmed.setText(study.pubmedid);
			View dot1 = convertView.findViewById(R.id.studyitem_comp1);
			dot1.setVisibility(View.INVISIBLE);
			View dot2 = convertView.findViewById(R.id.studyitem_comp2);
			dot2.setVisibility(View.INVISIBLE);
			View dot3 = convertView.findViewById(R.id.studyitem_comp3);
			dot3.setVisibility(View.INVISIBLE);
			for (String comp : row.getCompanies()) {
				int index = CSVData.getCompanyIndex(comp);
				View view = null;
				switch (index) {
				case 1:
					view = dot1;
					break;
				case 2:
					view = dot2;
					break;
				case 3:
					view = dot3;
					break;
//				case 4:
//					view = convertView.findViewById(R.id.cvti_comp1);
//					break;
//				case 5:
//					view = convertView.findViewById(R.id.cvti_comp1);
//					break;
//				case 6:
//					view = convertView.findViewById(R.id.cvti_comp1);
//					break;
						
				}
				if (row.hasCompanyStudy(comp,study)) {
					view.setVisibility(View.VISIBLE);
				} else {
					view.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}
	}
	
}
