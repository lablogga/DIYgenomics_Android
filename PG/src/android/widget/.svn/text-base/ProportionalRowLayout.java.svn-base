package android.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.melanieswan.pg.utils.MLog;

public class ProportionalRowLayout extends LinearLayout {

	static final String TAG = "PRLayout";
	
	ArrayList<Integer> ptable;
	
	
	public ProportionalRowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProportionalRowLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		MLog.enable(TAG);
		ptable = new ArrayList<Integer>();
		
	}
	
	public void setNrOfColumns(int n) {
		if (n > 0) {
			// initialize evenly spaced columns
			float p = 100/n;
			for (int i = 0; i < n; i++) {
				ptable.add(Math.round(p));
			}
		}
	}

	/**
	 * set the width of column c to w in percent
	 * @param c
	 * @param w
	 */
	public void setColumnWidth(int c, int w) {
		ptable.set(c, w);
	}
	
	@Override
	protected void onMeasure(int wspec, int hspec) {
		super.onMeasure(wspec,hspec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// now set the width of children
		int n = getChildCount();
		int ww = r-l;
		int al = 0;
		int at = 0;
		for (int i = 0; i < n; i++) {
			int w = (int)(ww*ptable.get(i)/100.0);
			MLog.i(TAG, "child width: ",w);
			View child = getChildAt(i);	
			LayoutParams lp = (LinearLayout.LayoutParams)child.getLayoutParams();
			LayoutParams newlp = new LinearLayout.LayoutParams(w,lp.height);
			child.setLayoutParams(newlp);
			child.layout(al,t,al+w,b); 
			at += child.getHeight();
			
			al += w;
		}
	}
	
}
