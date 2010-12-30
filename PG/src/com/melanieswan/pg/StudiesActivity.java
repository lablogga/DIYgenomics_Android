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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.melanieswan.pg.utils.MLog;

public class StudiesActivity extends Activity {

	static final String TAG = "sa";

	Data mData;

	View mMainView;

	ListView list;

	ArrayList<Study> studies;

	MappingItem row;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		MLog.enable(TAG);
		mData = Main.getData();
		studies = new ArrayList<Study>();
		row = (MappingItem) getIntent().getExtras()
				.getSerializable(CategoryItemActivity.EXTRA_ROW);
		for (String comp : row.companyRefs.keySet()) {
			ArrayList<Study> sl = row.companyRefs.get(comp);
			for (Study st : sl) {
				if (!studies.contains(st)) {
					studies.add(st);
				}
			}
		}
		Collections.sort(studies);
		String condition = getIntent().getStringExtra(CategoriesActivity.EXTRA_ITEM);
		mMainView = getLayoutInflater().inflate(R.layout.studytable, null);
		TextView title = (TextView) mMainView.findViewById(R.id.title);
		title.setText("References: " + condition + ", " + row.getVariant().RSID);
		list = (ListView) mMainView.findViewById(R.id.list);
		list.setAdapter(new StudyAdapter());
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(studies.get(pos).getUrl()));
				startActivity(intent);

			}
		});
		DotUtils.populateCompanyNames(mMainView, mData);
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
				convertView = getLayoutInflater().inflate(R.layout.studyitem,
						null);
			}
			convertView.setBackgroundResource(R.drawable.list_selector_background);
			Study study = studies.get(position);
			TextView citation = (TextView) convertView.findViewById(R.id.citation);
			citation.setText(study.citation);
			TextView pubmed = (TextView) convertView.findViewById(R.id.pubmedid);
			pubmed.setText(study.pubmedid);
			DotUtils.populateDotViews(convertView, row, study, mData);
			return convertView;
		}
	}

}
