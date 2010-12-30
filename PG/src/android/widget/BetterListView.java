package android.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.melanieswan.pg.R;
import com.melanieswan.pg.utils.MLog;

public class BetterListView extends FrameLayout 
		implements OnScrollListener, OnGroupClickListener {
	
	static final String TAG = "BetterList";
	
	private ExpandableListView mList;
	private LayoutParams mHeaderParams;
	private View mHeaderView;
	private BetterAdapter mAdapter;
	private OnGroupClickListener mGroupClickListener;

	public BetterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BetterListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BetterListView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		MLog.enable(TAG);
		mList = new ExpandableListView(getContext());
		addView(mList, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mHeaderParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//		mList.setOnScrollListener(this);
		mList.setOnGroupClickListener(this);
	}
	
	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		View first = mList.getChildAt(0);
		if (first != null) {
			if (getChild(first) == -1) {
				// a group view
				int top = getTop(first);
				if (mHeaderView != null) {
					// there is a group sticking out under the header
				}
			} else {
				
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}
	
	// calculate the distance from the top of the list
	private int getTop(View v) {
		return v.getTop() - mList.getScrollY();
	}
	
	private View getFirstGroupView() {
		int n = mList.getChildCount();
		for (int i = 0; i < n; i++) {
			View v = mList.getChildAt(i);
			if (getChild(v) == -1) {
				return v;
			}
		}
		return null;
	}
	
	private void setHeaderView(int group) {
//		if ((mHeaderView == null) 
//				|| (getGroupFromTag(mHeaderView.getTag()) != getFirstVisibleGroup())) {
//			MLog.i(TAG,"swapping header view");
//			if (mHeaderView != null) {
//				removeView(mHeaderView);
//			}
//			mHeaderView = mAdapter.getGroupView(group, false, null, null);
//			mHeaderView.setLayoutParams(mHeaderParams);
//			addView(mHeaderView);
//			requestLayout();
//			LayoutParams lp = (FrameLayout.LayoutParams) mList.getLayoutParams();
//			MLog.i(TAG, "header height: ",mHeaderView.getHeight());
//			lp.topMargin = mHeaderView.getHeight();
//			mList.setLayoutParams(lp);
//			mList.scrollTo(mList.getScrollX(), mList.getScrollY() + mHeaderView.getHeight());
//		} else {
//			updateExpandedStatus(mHeaderView, group);
//		}
	}
	
	private void bumpHeaderView(View first) {
		LayoutParams lp = (FrameLayout.LayoutParams) mHeaderView.getLayoutParams();
		int top = first.getTop() - mList.getScrollY();
		if ((top > 0) && (top < mHeaderView.getHeight())) {
			lp.topMargin = top - mHeaderView.getHeight();
			MLog.i(TAG,"bump header to ", lp.topMargin);
		} else {
			lp.topMargin = 0;
		}
		mHeaderView.setLayoutParams(lp);
	}

	private void setHeaderView(View view, int offset) {
		if ((mHeaderView != null) && (getGroup(mHeaderView) != getGroup(view))) {
			removeView(mHeaderView);
		}
		mHeaderView = view;
		mHeaderParams.topMargin = offset;
		addView(mHeaderView, mHeaderParams);
	}

	private void removeHeader() {
		if (mHeaderView != null) {
			removeView(mHeaderView);
			mHeaderView = null;
		}
	}

	
	public void setOnGroupClickListener(OnGroupClickListener gcl) {
		mGroupClickListener = gcl;
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View groupview, int group,
			long arg3) {
		updateExpandedStatus(groupview, group);
		if (mHeaderView != null) {
			updateExpandedStatus(mHeaderView, getGroup(mHeaderView));
		}
		if (mGroupClickListener != null) {
			return mGroupClickListener.onGroupClick(arg0, groupview, group, arg3);
		}
		return false;
	}

	private int getGroup(View view) {
		return ((Point) view.getTag()).x;
	}

	private int getChild(View view) {
		return ((Point) view.getTag()).y;
	}
	
	private void updateExpandedStatus(View v, int group) {
		View ind = v.findViewById(R.id.selector);
		ind.setEnabled(mList.isGroupExpanded(group));
	}

	public boolean isGroupExpanded(int group) {
		return mList.isGroupExpanded(group);
	}
	
	public void setGroupIndicator(Drawable d) {
		mList.setGroupIndicator(d);
	}
	
	public void setAdapter(ExpandableListAdapter adapter) {
		mAdapter = new BetterAdapter(adapter);
		mList.setAdapter(mAdapter);
	}

	/**
	 * simple wrapper for the original adapter
	 * adds required Tag to views
	 */
	class BetterAdapter extends BaseExpandableListAdapter {

		ExpandableListAdapter mAdapter;
		
		BetterAdapter(ExpandableListAdapter adapter) {
			mAdapter = adapter;
		}
		
		@Override
		public Object getChild(int group, int child) {
			mAdapter.getChild(group, child);
			return null;
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			return 0;
		}

		@Override
		public View getChildView(int group, int child, boolean arg2, View arg3,
				ViewGroup arg4) {
			View res = mAdapter.getChildView(group, child, arg2, arg3, arg4);
			res.setTag(makeTag(group, child));
			return res;
		}

		@Override
		public int getChildrenCount(int group) {
			return mAdapter.getChildrenCount(group);
		}

		@Override
		public Object getGroup(int group) {
			return mAdapter.getGroup(group);
		}

		@Override
		public int getGroupCount() {
			return mAdapter.getGroupCount();
		}

		@Override
		public long getGroupId(int arg0) {
			return 0;
		}

		@Override
		public View getGroupView(int group, boolean arg1, View arg2,
				ViewGroup arg3) {
			View view = mAdapter.getGroupView(group, arg1, arg2, arg3);
			view.setTag(makeTag(group, -1));
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int group, int child) {
			return mAdapter.isChildSelectable(group, child);
		}
		
		Point makeTag(int group, int child) {
			return new Point(group, child);
		}
		
	}
	
}
