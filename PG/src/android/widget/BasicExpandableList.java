package android.widget;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class BasicExpandableList extends BasicListView implements
		OnItemClickListener {

	static final String TAG = "BaseExList";

	private ExAdapterWrapper mAdapter;
	private OnGroupClickListener mOnGroupClickListener;
	private OnChildClickListener mOnChildClickListener;
	
	private Point mExtra;
	private View mExtraView;

	public BasicExpandableList(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnItemClickListener(this);
	}

	public void setAdapter(ExpandableListAdapter exadapter) {
		mAdapter = new ExAdapterWrapper(exadapter);
		super.setAdapter(mAdapter);
	}

	public void setOnGroupClickListener(OnGroupClickListener l) {
		mOnGroupClickListener = l;
	}

	public void setOnChildClickListener(OnChildClickListener l) {
		mOnChildClickListener = l;
	}

	public boolean isGroupExpanded(int group) {
		return mAdapter.isExpanded(group);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterview, View childview,
			int pos, long id) {
		Point p = mAdapter.getGroupAndChild(pos);
		if (p.y == -1) {
			if (mAdapter.isExpanded(p.x)) {
				mAdapter.collapseGroup(p.x);
			} else {
				mAdapter.expandGroup(p.x);
			}
			if (mOnGroupClickListener != null) {
				mOnGroupClickListener.onGroupClick(null, childview, p.x, id);
			}
		} else {
			if (mOnChildClickListener != null) {
				mOnChildClickListener.onChildClick(null, childview, p.x, p.y,
						id);
			}
		}
	}
	
	@Override 
	protected void doLayout(int left, int top, int right, int bottom) {
		if (mExtraView != null) {
			removeViewInLayout(mExtraView);
		}
		super.doLayout(left, top, right, bottom);
		int pos = getFirstItemPosition();
		Point p = mAdapter.getGroupAndChild(pos);
		if ((mExtraView == null) || (mExtra.x != p.x)) {
			mExtra = p;
			mExtraView = mAdapter.getGroupView(p.x);
		}
		addAndMeasureChild(mExtraView, LAYOUT_MODE_BELOW);
		mLastView = getChildCount() - 2;
		// now do its layout
		int etop = getScrollY();
		if (pos < mAdapter.getCount() - 1) {
			Point next = mAdapter.getGroupAndChild(pos + 1);
			if (next.y == -1) {
				//a group follows, so move the extra above
				etop = getChildAt(1).getTop() - mExtraView.getMeasuredHeight();
			}
		}
		mExtraView.layout(0, etop, getWidth(), etop + mExtraView.getMeasuredHeight());
	}

	int mLastView = -1;
	
	@Override
	protected View getLastView() {
		View last = getChildAt(getChildCount() - 1);
		if (last == mExtraView) {
			last = getChildAt(getChildCount() - 2);
		}
		return last;
	}
	
	protected int getListChildCount() {
		return getChildCount() - ((mExtraView != null) ? 1 : 0);
	}

	/**
	 * Calls the item click listener for the child with at the specified
	 * coordinates
	 * 
	 * @param x
	 *            The x-coordinate
	 * @param y
	 *            The y-coordinate
	 */
	protected void clickChildAt(final int x, final int y) {
		final int index = getContainingChildIndex(x, y);
		if (index != INVALID_INDEX) {
			final View itemView = getChildAt(index);
			int position = mFirstItemPosition + index;
			if (itemView == mExtraView) {
				Point p = mAdapter.getGroupAndChild(mFirstItemPosition);
				position = mAdapter.getLinearGroupPosition(p.x);
			}				
			final long id = mAdapter.getItemId(position);
			performItemClick(itemView, position, id);
		}
	}


	class ExAdapterWrapper extends BaseAdapter {

		ExpandableListAdapter exAdapter;

		Set<Integer> expandedGroups;
		int mCount = 0;

		public ExAdapterWrapper(ExpandableListAdapter exadapter) {
			exAdapter = exadapter;
			expandedGroups = new HashSet<Integer>(7);
			mCount = exAdapter.getGroupCount();
		}
		
		public View getGroupView(int group) {
			return exAdapter.getGroupView(group, false, null, null);
		}
		
		public int getLinearGroupPosition(int groupIx) {
			int pos = 0;
			int group = 0;
			while (group < groupIx) {
				pos++;
				if (isExpanded(group)) {
					pos += exAdapter.getChildrenCount(group);
				}
				group++;
			}
			return pos;
		}

		public Point getGroupAndChild(int pos) {
			Point res = null;
			if ((pos < 0) || (pos >= getCount())) {
				res = new Point(-1, -1);
			} else {
				int group = 0;
				while (true) {
					// invariant: group is the next group to check
					// pos is the remaining positions

					// check for group first
					if (pos == 0) {
						// current group is at position
						res = new Point(group, -1);
						break;

					} else {
						pos--;
						if (isExpanded(group)) {
							int child = pos;
							int nc = exAdapter.getChildrenCount(group);
							if (child >= nc) {
								pos -= nc;
								group++;
							} else {
								res = new Point(group, pos);
								break;
							}
						} else {
							group++;
							if (group >= exAdapter.getGroupCount()) {
								Log.e(TAG, "illegal position: " + pos);
								res = new Point(-1, -1);
								break;
							}
						}
					}
				}
			}
			return res;
		}

		public void expandGroup(int group) {
			mCount += exAdapter.getChildrenCount(group);
			expandedGroups.add(group);
			notifyDataSetChanged();
		}

		public void collapseGroup(int group) {
			mCount -= exAdapter.getChildrenCount(group);
			expandedGroups.remove(group);
			notifyDataSetChanged();
		}

		public boolean isExpanded(int group) {
			boolean res = expandedGroups.contains(group);
			return res;
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public Object getItem(int position) {
			Point p = getGroupAndChild(position);
			if (p.y == -1) {
				return exAdapter.getGroup(p.x);
			} else {
				return exAdapter.getChild(p.x, p.y);
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Point p = getGroupAndChild(position);
			if (p.x == -1) {
				// invlaid position
				return null;
			} else if (p.y == -1) {
				return exAdapter.getGroupView(p.x, false, convertView, parent);
			} else {
				return exAdapter.getChildView(p.x, p.y, false, convertView,
						parent);
			}
		}

		@Override
		public int getViewTypeCount() {
			return 0;
		}

	}

}
