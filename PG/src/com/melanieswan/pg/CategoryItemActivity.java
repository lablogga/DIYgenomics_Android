package com.melanieswan.pg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.melanieswan.pg.utils.ConditionVariantTableRowComparator;
import com.melanieswan.pg.utils.Flurry;

public class CategoryItemActivity extends Activity
	implements OnClickListener {

	public static final String EXTRA_ROW = "rowData";

	String mCategoryItem;
	ArrayList<MappingItem> tableData;

	View mMainView;
	Data mData;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mData = Main.getData();
		mCategoryItem = getIntent().getStringExtra(CategoriesActivity.EXTRA_ITEM);
		tableData = mData.getCategoryItemVariantTable(mCategoryItem);
		Collections.sort(tableData, new ConditionVariantTableRowComparator());
		mMainView = getLayoutInflater().inflate(R.layout.category_items, null);
		TextView title = (TextView) mMainView
				.findViewById(R.id.title);
		ListView list = (ListView) mMainView.findViewById(R.id.list);
		list.setAdapter(new CVTRAdapter());
		title.setText(mCategoryItem);
		DotUtils.populateCompanyNames(mMainView, mData);
		View info = mMainView.findViewById(R.id.info);
		info.setOnClickListener(this);
		FlurryAgent.onEvent(Flurry.EVENT_CATITEM, Flurry.map("item", mCategoryItem));
		setContentView(mMainView);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				MappingItem row = tableData.get(pos);
				Intent intent = new Intent();
				intent.putExtra(EXTRA_ROW, (Serializable) row);
				intent.putExtra(CategoriesActivity.EXTRA_ITEM, mCategoryItem);
				intent.setClassName(StudiesActivity.class.getPackage()
						.getName(), StudiesActivity.class.getName());
				startActivity(intent);

			}
		});

	}
	
	@Override
	public void onClick(View arg0) {
		InfoHandler.getInstance().showInfo(this, R.string.info_catitem);
	}

	class CVTRAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return tableData.size();
		}

		@Override
		public Object getItem(int position) {
			return tableData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(CategoryItemActivity.this)
						.inflate(R.layout.category_item, null);
			}
			if (position % 2 == 0) {
				convertView
						.setBackgroundResource(R.drawable.list_selector_background);
			} else {
				convertView
						.setBackgroundResource(R.drawable.list_selector_background2);
			}
			MappingItem row = tableData.get(position);

			TextView rs = (TextView) convertView.findViewById(R.id.rsid);
			rs.setText(row.getVariant().RSID);
			// rs.setText( padRight( row.variant.RSID, 11) );
			TextView locus = (TextView) convertView
					.findViewById(R.id.locus);
			locus.setText(row.getVariant().getLocus());
			// locus.setText( padRight( row.variant.locus, 11) );
			TextView gene = (TextView) convertView.findViewById(R.id.gene);
			gene.setText(row.getVariant().gene);
			TextView normal = (TextView) convertView.findViewById(R.id.normal);
			String ngenotype = row.getVariant().genotype;
			if (ngenotype != null) {
				normal.setText(ngenotype);
				TextView mine2 = (TextView) convertView.findViewById(R.id.mine2);
				TextView mine1 = (TextView) convertView.findViewById(R.id.mine1);
				if (mData.getGenotype() != null) {
					Genome g = mData.getGenotype().get(row.getVariant().RSID);
					if (g != null) {
						String mine = g.genotype;
						if (mine.length() > 0) {
							String letter = mine.substring(0,1);
							mine1.setText(letter);
							if (letter.equals(ngenotype)) {
								mine1.setTextColor(getResources().getColor(R.color.normal));
							} else {
								mine1.setTextColor(getResources().getColor(R.color.mutated));
							}
							if (mine.length() > 1) {
								letter = mine.substring(1,2);
								mine2.setText(letter);
								if (letter.equals(ngenotype)) {
									mine2.setTextColor(getResources().getColor(R.color.normal));
								} else {
									mine2.setTextColor(getResources().getColor(R.color.mutated));
								}
							} else {
								mine2.setText("");
							}
						} else {
							mine1.setText("");
							mine2.setText("");
						}
					} else {
						mine1.setText("");
						mine2.setText("");
					}
				} else {
					mine1.setText("");
					mine2.setText("");
				}
			} else {
				normal.setText("");
			}
			DotUtils.populateDotViews(convertView, row, null, mData);
			ProgressBar rank = (ProgressBar) convertView.findViewById(R.id.rank);
			rank.setProgress(row.getVariant().rank);
			return convertView;
		}

	}// class CVTRAdapter

}
