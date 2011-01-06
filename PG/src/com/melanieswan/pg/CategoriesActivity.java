package com.melanieswan.pg;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.BasicExpandableList;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.melanieswan.pg.utils.Flurry;
import com.melanieswan.pg.utils.MLog;

public class CategoriesActivity extends Activity 
		implements OnGroupClickListener, OnChildClickListener, OnClickListener {

	static final String TAG = "Categories";
	static final String EXTRA_ITEM = "item";

	private CategoriesAdapter mCategoriesAdapter;
	private BasicExpandableList mCategories;
	private Data mData;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MLog.enable(TAG);
		mData = Main.getData();
		View view = getLayoutInflater().inflate(R.layout.categories, null);
		TextView title = (TextView) view.findViewById(R.id.title);
		View info = view.findViewById(R.id.info);
		title.setText(getString(R.string.categories_title));
		mCategories = (BasicExpandableList) view.findViewById(R.id.categories);
		mCategoriesAdapter = new CategoriesAdapter();
		mCategories.setAdapter(mCategoriesAdapter);
		mCategories.setOnGroupClickListener(this);
		mCategories.setOnChildClickListener(this);
		info.setOnClickListener(this);
		FlurryAgent.onEvent(Flurry.EVENT_CATEGORIES);
		setContentView(view);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete) {
			mData.deleteGenome(this);
			return true;
		}
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView arg0, View arg1, int group,
			int child, long arg4) {
		String catitem = mData.getCategories().get(group).get(child);
		Intent intent = new Intent(CategoriesActivity.this, CategoryItemActivity.class);
		intent.putExtra(EXTRA_ITEM, catitem);
		startActivity(intent);
		return true;
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View groupview, int group,
			long arg3) {
		View ind = groupview.findViewById(R.id.selector);
		ind.setEnabled(mCategories.isGroupExpanded(group));
		return true;
	}
	
	@Override
	public void onClick(View arg0) {
		InfoHandler.getInstance().showInfo(this, R.string.info_categories);
	}
	
	class CategoriesAdapter extends BaseExpandableListAdapter {

		@Override
		public String getChild(int group, int child) {
			return mData.getCategories().get(group).get(child);
		}

		@Override
		public long getChildId(int group, int child) {
			return 0;
		}

		@Override
		public View getChildView(final int group, final int child, boolean isLast, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.categories_item, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.label);
			View ind = convertView.findViewById(R.id.selector);
			ind.setVisibility(View.GONE);
			if (child % 2 == 0) {
				tv.setBackgroundResource(R.drawable.list_selector_background);
			} else {
				tv.setBackgroundResource(R.drawable.list_selector_background2);
			}
			tv.setText((String) getChild(group, child));
			tv.setEnabled(true);
			return convertView;
		}

		@Override
		public int getChildrenCount(int group) {
			return mData.getCategories().get(group).size();
		}

		@Override
		public Category getGroup(int group) {
			return mData.getCategories().get(group);
		}

		@Override
		public int getGroupCount() {
			return mData.getCategories().size();
		}

		@Override
		public long getGroupId(int group) {
			return 0;
		}

		@Override
		public View getGroupView(int group, boolean isLast, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.categories_item, null);
			}
			convertView.setBackgroundResource(R.drawable.category_selector_background);
			View ind = convertView.findViewById(R.id.selector);
			ind.setEnabled(mCategories.isGroupExpanded(group));
			TextView tv = (TextView) convertView.findViewById(R.id.label);
			tv.setText(getGroup(group).getName());
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return false;
		}
		
	}

}
