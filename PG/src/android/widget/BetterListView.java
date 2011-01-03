package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.melanieswan.pg.R;
import com.melanieswan.pg.utils.MLog;

public class BetterListView extends LinearLayout implements OnScrollListener,
		OnGroupClickListener, OnClickListener {

	static final String TAG = "BetterList";

	private ExpandableListView mList;
	private LayoutParams mHeaderParams;
	private LayoutParams mListParams;
	private View mHeaderView;
	private BetterAdapter mAdapter;
	private OnGroupClickListener mGroupClickListener;
	private boolean mBlockDraw;
	private boolean ignoreNext;

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
		setOrientation(LinearLayout.VERTICAL);
		mListParams = new LayoutParams(LayoutParams.FILL_PARENT, 0);
		mListParams.weight = 1.0f;
		mList = new ExpandableListView(getContext());
		mList.setVerticalFadingEdgeEnabled(false);
		addView(mList, mListParams);
		mHeaderParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mList.setOnScrollListener(this);
		mList.setOnGroupClickListener(this);
		mBlockDraw = false;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mBlockDraw) {
			MLog.i(TAG,"draw blocked");
			return;
		}
		super.onDraw(canvas);
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		boolean changed = updateHeader();
		if (!changed) {
			mBlockDraw = true;
		} else {
			mBlockDraw = false;
		}
	}

	private boolean updateHeader() {
		View first = mList.getChildAt(0);

		if (first != null) {
			if (getChild(first) == -1) {
				// first is group
				MLog.i(TAG, "group first");
				if (getTop(first) == 0) {
					MLog.i(TAG, "group at top, remove header");
					removeHeader();
					return true;
				} else if ((getBottom(first) > first.getHeight())) {
					MLog.i(TAG, "pull out header");
					if (getGroup(first) > 0) {
						MLog.i(TAG, "show prev header");
						// need previous header
						if ((mHeaderView == null)
								|| (getGroup(mHeaderView) != getGroup(first) - 1)) {
							if (mHeaderView != null) {
								removeHeader();
							}
							// header view is incorrect
							MLog.i(TAG, "add previous header");
							View header = mAdapter.getGroupView(
									getGroup(first) - 1, false, null, null);
							setHeaderView(header,
									getTop(first) - first.getHeight());
						} else {
							MLog.i(TAG, "adjust previous header");
							// have the right header view
							adjustHeader(getTop(first)
									- mHeaderView.getHeight());
						}
					} else {
						MLog.i(TAG, "first group, hide header");
						removeHeader();
					}
					return true;
				} else if (getBottom(first) < first.getHeight()) {
					if (mHeaderView != null) {
						removeHeader();
					}
					MLog.i(TAG, "need current group header");
					View header = mAdapter.getGroupView(getGroup(first), false,
							null, null);
					setHeaderView(header, 0);
					return true;
				}
			} else {
				MLog.i(TAG, "child first");
				// child first
				if ((mHeaderView == null)
						|| (getGroup(first) != getGroup(mHeaderView))) {
					MLog.i(TAG, "child is first, need a new header");
					if (mHeaderView != null) {
						removeHeader();
					}
					View header = mAdapter.getGroupView(getGroup(first), false,
							null, null);
					setHeaderView(header);
					return true;
				} else if ((mHeaderView != null)
						&& (getGroup(first) == getGroup(mHeaderView))
						&& (getTop(mHeaderView) != 0)) {
					MLog.i(TAG, "header needs reposition ignore: ", ignoreNext);
					adjustHeader(0);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	private int getTop(View listelement) {
		int[] pos1 = new int[2];
		getLocationInWindow(pos1);
		int[] pos = new int[2];
		listelement.getLocationInWindow(pos);
		return pos[1] - pos1[1];
	}

	private int getBottom(View elem) {
		return getTop(elem) + elem.getHeight();
	}

	private void setHeaderView(View view) {
		setHeaderView(view, 0);
	}

	private void setHeaderView(View view, int top) {
		MLog.i(TAG, "set header groupid ", getGroup(view), " top " + top);
		mHeaderView = view;
		mHeaderParams.topMargin = top;
		addView(mHeaderView, 0, mHeaderParams);
		mHeaderView.setOnClickListener(this);
	}

	private void adjustHeader(int top) {
		MLog.i(TAG, "adjust header top ", top);
		LayoutParams lp = (LinearLayout.LayoutParams) mHeaderView
				.getLayoutParams();
		lp.topMargin = top;
		mHeaderView.setLayoutParams(lp);
	}

	private void removeHeader() {
		MLog.i(TAG, "remove header ");
		if (mHeaderView != null) {
			removeView(mHeaderView);
			mHeaderView = null;
		}
	}

	@Override
	public void onClick(View groupview) {
		// user clicked headerview
		int group = getGroup(groupview);
		mBlockDraw = true;
		removeHeader();
		if (mList.isGroupExpanded(group)) {
			mList.collapseGroup(group);
		} else {
			mList.expandGroup(group);
		}
		// update header state
		post(new Runnable() {
			public void run() {
				requestLayout();
				updateHeader();
				if (mHeaderView != null) {
					MLog.i(TAG, "header at:", mHeaderView.getTop(),
							mHeaderView.getHeight(), mHeaderView.getWidth());
				}
				mBlockDraw = false;

			}
		});
	}

	public void setOnGroupClickListener(OnGroupClickListener gcl) {
		mGroupClickListener = gcl;
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View groupview,
			int group, long arg3) {
		updateExpandedStatus(groupview, group);
		if (mHeaderView != null) {
			updateExpandedStatus(mHeaderView, getGroup(mHeaderView));
		}
		if (mGroupClickListener != null) {
			return mGroupClickListener.onGroupClick(arg0, groupview, group,
					arg3);
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
	 * simple wrapper for the original adapter adds required Tag to views
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
