package com.melanieswan.pg;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
  
	
  static final String EXTRA_COND = "condition";
  
  ArrayList<String> conditions;
  
  CondAdapter conditionAdapter;
  
  ListView mConditionView;
  
  /** Called when the activity is first created. */ 
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initializeData();
    View view = getLayoutInflater().inflate(R.layout.main, null);
    mConditionView = (ListView)view.findViewById(R.id.main_conditions);
    mConditionView.setSelector(getResources().getDrawable(R.drawable.list_selector_background));
    mConditionView.setDrawSelectorOnTop(true);
    conditionAdapter = new CondAdapter();
    mConditionView.setAdapter(conditionAdapter);
    mConditionView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> list, View view, int pos, long arg3) {
        String condition = (String)conditions.get(pos+1);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COND, condition);
        intent.setClassName("com.melanieswan.pg", ConditionTableActivity.class.getName());
        startActivity(intent);
        
      }
    });
    setContentView(view);
  }
  
  
  

  private void initializeData() {
    CSVData.load(getAssets());
    conditions = CSVData.conditions;
  }
  
  class CondAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return conditions.size()-1; 
		}

		@Override 
		public Object getItem(int position) {
			return conditions.get(position+1);
		}

		@Override
		public long getItemId(int position) {
			return position+1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new TextView(Main.this);
			}
			TextView tv = (TextView)convertView;
			if (position % 2 == 0) {
				tv.setBackgroundColor(Constants.COLOR_BG1);
			} else {
				tv.setBackgroundColor(Constants.COLOR_BG2);
			}
			tv.setText((String)getItem(position));
			tv.setTextSize(24);
			tv.setPadding(4,4,4,4);
			tv.setTextColor(Color.BLACK);
			
			return tv;
		}
  	
  }
  
}