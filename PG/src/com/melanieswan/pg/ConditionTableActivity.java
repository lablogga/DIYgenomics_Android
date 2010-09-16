package com.melanieswan.pg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.melanieswan.pg.utils.ConditionVariantTableRowComparator;

public class ConditionTableActivity extends Activity {

	public static final String EXTRA_ROW = "rowData"; 
	
  String mCondition;
  ArrayList<ConditionVariantTableRow> tableData;
  
  View mMainView;
  
  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    mCondition = getIntent().getStringExtra(Main.EXTRA_COND);
    tableData = CSVData.getConditionVariantTable(mCondition);
    
    Collections.sort(tableData, new ConditionVariantTableRowComparator());
    
    mMainView = getLayoutInflater().inflate(R.layout.cond_table, null);
    TextView title = (TextView)mMainView.findViewById(R.id.cond_table_title);
    ListView list = (ListView)mMainView.findViewById(R.id.cond_table_list);
    list.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);    
    list.setAdapter(new CVTRAdapter());
    list.setSelector(getResources().getDrawable(R.drawable.list_selector_background));
    list.setDrawSelectorOnTop(true);
    title.setText(mCondition);
    setContentView(mMainView);
    TextView legend1 = (TextView)mMainView.findViewById(R.id.deCODEme);
    legend1.setTextColor(Constants.COLOR_COMP1);
    TextView legend2 = (TextView)mMainView.findViewById(R.id.Navigenics);
    legend2.setTextColor(Constants.COLOR_COMP2);
    TextView legend3 = (TextView)mMainView.findViewById(R.id.Twenty3andme);
    legend3.setTextColor(Constants.COLOR_COMP3);
    
    list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				
				ConditionVariantTableRow row = tableData.get(pos);
				Intent intent = new Intent();
				intent.putExtra(EXTRA_ROW,(Serializable) row);
				intent.putExtra(Main.EXTRA_COND, mCondition);
				intent.setClassName(StudiesActivity.class.getPackage().getName(), StudiesActivity.class.getName());
				startActivity(intent);
				
			}
    });
    
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
			return position+1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if (convertView == null) {
				convertView = LayoutInflater.from(ConditionTableActivity.this).inflate(R.layout.condvartableitem,null);
			}
//			ProportionalRowLayout prl  = (ProportionalRowLayout)convertView;
//			prl.setNrOfColumns(4);
//			prl.setColumnWidth(0, 25);
//			prl.setColumnWidth(1, 25);
//			prl.setColumnWidth(2, 25);
//			prl.setColumnWidth(3, 25);
			
			if (position % 2 == 0) {
				convertView.setBackgroundColor(Constants.COLOR_BG1);
			} else {
				convertView.setBackgroundColor(Constants.COLOR_BG2);
			}
			
			ConditionVariantTableRow row = tableData.get(position);
			
			TextView rs = (TextView)convertView.findViewById(R.id.cvti_rsid);			
			rs.setText( row.variant.RSID);
//			rs.setText( padRight( row.variant.RSID, 11) );
			TextView locus = (TextView)convertView.findViewById(R.id.cvti_locus);
			locus.setText(row.variant.locus );
//			locus.setText( padRight( row.variant.locus, 11) );
			TextView gene = (TextView)convertView.findViewById(R.id.cvti_gene);
			gene.setText(row.variant.gene);
//			gene.setText( padRight( row.variant.gene, 15) );
			
			View dot1 = convertView.findViewById(R.id.cvti_comp1);
			dot1.setVisibility(View.INVISIBLE);
			View dot2 = convertView.findViewById(R.id.cvti_comp2);
			dot2.setVisibility(View.INVISIBLE);
			View dot3 = convertView.findViewById(R.id.cvti_comp3);
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
				if (row.hasStudy(comp)) {
					view.setVisibility(View.VISIBLE);
				} else {
					view.setVisibility(View.INVISIBLE);
				}
			}
			
			return convertView;
		}
		
		private String padRight(String s, int n) {
			return String.format("%1$-" + n + "s", s); 
		}		

  }//class CVTRAdapter
  
}
