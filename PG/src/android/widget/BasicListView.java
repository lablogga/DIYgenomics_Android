/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * A simple list view that displays the items as 3D blocks
 */
public class BasicListView extends AdapterView<Adapter> {
	
	static final String TAG = "BasicList";

	/** Unit used for the velocity tracker */
	private static final int PIXELS_PER_SECOND = 600;

	/** Represents an invalid child index */
	protected static final int INVALID_INDEX = -1;

	/** Distance to drag before we intercept touch events */
	private static final int TOUCH_SCROLL_THRESHOLD = 10;

	/** Children added with this layout mode will be added below the last child */
	protected static final int LAYOUT_MODE_BELOW = 0;

	/** Children added with this layout mode will be added above the first child */
	private static final int LAYOUT_MODE_ABOVE = 1;

	/** User is not touching the list */
	private static final int TOUCH_STATE_RESTING = 0;

	/** User is touching the list and right now it's still a "click" */
	private static final int TOUCH_STATE_CLICK = 1;

	/** User is scrolling the list */
	private static final int TOUCH_STATE_SCROLL = 2;

	/** The adapter with all the data */
	private Adapter mAdapter;

	/** Current touch state */
	private int mTouchState = TOUCH_STATE_RESTING;

	/** X-coordinate of the down event */
	private int mTouchStartX;

	/** Y-coordinate of the down event */
	private int mTouchStartY;

	/** The adaptor position of the first visible item */
	protected int mFirstItemPosition;

	/** The adaptor position of the last visible item */
	protected int mLastItemPosition;

	/** Velocity tracker used to get fling velocities */
	private VelocityTracker mVelocityTracker;

	private Scroller mScroller;

	/** Runnable used to animate fling and snap */
	private Runnable mScrollerRunnable;

	/** Used to check for long press actions */
	private Runnable mLongPressRunnable;

	/** Reusable rect */
	private Rect mRect;
	
	private DataSetObserver mObserver;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 * @param attrs
	 *            Attributes
	 */
	public BasicListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		//MLog.enable(TAG);
		mObserver = new DataSetObserver() {

			@Override
			public void onChanged() {
				rebuild();
				super.onChanged();
			}

			@Override
			public void onInvalidated() {
				rebuild();
				super.onInvalidated();
			}

		};
		mScroller = new Scroller(context);
	}

	@Override
	public void setAdapter(final Adapter adapter) {
		mAdapter = adapter;
		removeAllViewsInLayout();
		requestLayout();
		mAdapter.registerDataSetObserver(mObserver);
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setSelection(final int position) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public View getSelectedView() {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startTouch(event);
			return false;

		case MotionEvent.ACTION_MOVE:
			return startScrollIfNeeded(event);

		default:
			endTouch(0);
			return false;
		}
	}

	float lastY;
	
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (getChildCount() == 0) {
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startTouch(event);
			lastY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_CLICK) {
				startScrollIfNeeded(event);
			}
			if (mTouchState == TOUCH_STATE_SCROLL) {
				mVelocityTracker.addMovement(event);
				scrollList((int) (event.getY() - lastY));
				lastY = event.getY();
			}
			break;

		case MotionEvent.ACTION_UP:
			float velocity = 0;
			if (mTouchState == TOUCH_STATE_CLICK) {
				clickChildAt((int) event.getX(), (int) event.getY());
			} else if (mTouchState == TOUCH_STATE_SCROLL) {
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND);
				velocity = mVelocityTracker.getYVelocity();
			}
			endTouch(velocity);
			break;

		default:
			endTouch(0);
			break;
		}
		return true;
	}
	
	View mDownView;

	/**
	 * Sets and initializes all things that need to when we start a touch
	 * gesture.
	 * 
	 * @param event
	 *            The down event
	 */
	private void startTouch(final MotionEvent event) {
		// user is touching the list -> no more fling
		removeCallbacks(mScrollerRunnable);

		// save the start place
		mTouchStartX = (int) event.getX();
		mTouchStartY = (int) event.getY();
		pressView(mTouchStartX, mTouchStartY);
		// start checking for a long press
		startLongPressCheck();

		// obtain a velocity tracker and feed it its first event
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);

		// we don't know if it's a click or a scroll yet, but until we know
		// assume it's a click
		mTouchState = TOUCH_STATE_CLICK;
	}

	/**
	 * Resets and recycles all things that need to when we end a touch gesture
	 * 
	 * @param velocity
	 *            The velocity of the gesture
	 */
	private void endTouch(final float velocity) {
		// recycle the velocity tracker
		mVelocityTracker.recycle();
		mVelocityTracker = null;
		unpressView();
		// remove any existing check for longpress
		removeCallbacks(mLongPressRunnable);

		// create the dynamics runnable if we haven't before
		if (mScrollerRunnable == null) {
			mScrollerRunnable = new Runnable() {
				public void run() {
					if (mScroller == null) {
						return;
					}
					boolean running = mScroller.computeScrollOffset();
					scrollListTo(mScroller.getCurrY());
					if (running) {
						postDelayed(this, 16);
					} else {
						scrollListTo(mScroller.getFinalY());
						removeCallbacks(mScrollerRunnable);
					}
				}
			};
		}
		if ((mTouchState == TOUCH_STATE_SCROLL) && (mScroller != null))  {
			int max = mAdapter.getCount() * getChildAt(0).getHeight();

			mScroller.fling(0, getScrollY(), 0, (int) -velocity, 0, 0, 0, max);
			post(mScrollerRunnable);
		}

		// reset touch state
		mTouchState = TOUCH_STATE_RESTING;
	}

	/**
	 * Posts (and creates if necessary) a runnable that will when executed call
	 * the long click listener
	 */
	private void startLongPressCheck() {
		// create the runnable if we haven't already
		if (mLongPressRunnable == null) {
			mLongPressRunnable = new Runnable() {
				public void run() {
					if (mTouchState == TOUCH_STATE_CLICK) {
						final int index = getContainingChildIndex(mTouchStartX,
								mTouchStartY);
						unpressView();
						if (index != INVALID_INDEX) {
							longClickChild(index);
						}
					}
				}
			};
		}

		// then post it with a delay
		postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
	}

	/**
	 * Checks if the user has moved far enough for this to be a scroll and if
	 * so, sets the list in scroll mode
	 * 
	 * @param event
	 *            The (move) event
	 * @return true if scroll was started, false otherwise
	 */
	private boolean startScrollIfNeeded(final MotionEvent event) {
		final int xPos = (int) event.getX();
		final int yPos = (int) event.getY();
		if (xPos < mTouchStartX - TOUCH_SCROLL_THRESHOLD
				|| xPos > mTouchStartX + TOUCH_SCROLL_THRESHOLD
				|| yPos < mTouchStartY - TOUCH_SCROLL_THRESHOLD
				|| yPos > mTouchStartY + TOUCH_SCROLL_THRESHOLD) {
			// we've moved far enough for this to be a scroll
			removeCallbacks(mLongPressRunnable);
			unpressView();
			mTouchState = TOUCH_STATE_SCROLL;
			return true;
		}
		return false;
	}

	
	private void pressView(int evx, int evy) {
		int ix = getContainingChildIndex(mTouchStartX, mTouchStartY);
		if (ix != INVALID_INDEX) {
			View v = getChildAt(ix);
			mDownView = v;
			v.setPressed(true);
		}
	}
	
	private void unpressView () {
		if (mDownView != null) {
			mDownView.setPressed(false);
			mDownView = null;
		}
	}
	
	protected View getLastView() {
		return getChildAt(getChildCount() - 1);
	}
	
	protected int getListChildCount() {
		return getChildCount();
	}

	/**
	 * Returns the index of the child that contains the coordinates given.
	 * 
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return The index of the child that contains the coordinates. If no child
	 *         is found then it returns INVALID_INDEX
	 */
	protected int getContainingChildIndex(final int x, int y) {
		if (mRect == null) {
			mRect = new Rect();
		}
		y += getScrollY();
		for (int index = getChildCount() - 1; index >= 0; index--) {
			View child = getChildAt(index);
			child.getHitRect(mRect);
			if (mRect.contains(x, y)) {
				return index;
			}
		}
		return INVALID_INDEX;
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
			final int position = mFirstItemPosition + index;
			final long id = mAdapter.getItemId(position);
			itemView.setPressed(true);
			performItemClick(itemView, position, id);
			itemView.setPressed(false);
		}
	}

	/**
	 * Calls the item long click listener for the child with the specified index
	 * 
	 * @param index
	 *            Child index
	 */
	private void longClickChild(final int index) {
		final View itemView = getChildAt(index);
		final int position = mFirstItemPosition + index;
		final long id = mAdapter.getItemId(position);
		final OnItemLongClickListener listener = getOnItemLongClickListener();
		if (listener != null) {
			listener.onItemLongClick(this, itemView, position, id);
		}
	}
	
	/**
	 * Scrolls the list. Takes care of updating rotation (if enabled) and
	 * snapping
	 * 
	 * @param scrolledDistance
	 *            The distance to scroll
	 */
	private void scrollList(int scrollBy) {
		scrollBy = - scrollBy;
		if ((mLastItemPosition == mAdapter.getCount() - 1) && (scrollBy > 0)) {
			if (getLastView().getBottom() - getScrollY() <= getHeight()) {
				// can't scroll further
				return;
			} else if ((scrollBy > getLastView().getBottom() - getScrollY() - getHeight())) {
				// cap scroll at bottom of list
				scrollBy = getLastView().getBottom() - getScrollY() - getHeight();
			}
		} else if (getScrollY() + scrollBy < 0) {
			scrollBy = getScrollY();
		}
		scrollBy(0, scrollBy);
		requestLayout();
	}

	/**
	 * Scrolls the list. Takes care of updating rotation (if enabled) and
	 * snapping
	 * 
	 * @param scrolledDistance
	 *            The distance to scroll
	 */
	private void scrollListTo(int scrollTo) {
		if (scrollTo < 0) {
			scrollTo = 0;
		}
		if ((mLastItemPosition == mAdapter.getCount() - 1)) {
			if (getLastView().getBottom() - getScrollY() < getHeight()) {
				return;
			} else if (scrollTo > getLastView().getBottom() - getHeight()) {
				// cap scroll at bottom of list
				scrollTo = getLastView().getBottom() - getHeight();
			}
		}
		scrollTo(0, scrollTo);
		//setSnapPoint();
		requestLayout();
	}
	
	@Override
	protected void onLayout(final boolean changed, final int left,
			final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		doLayout(left, top, right, bottom);
		invalidate();
	}

	protected void doLayout(int left, int top, int right, int bottom) {
		// if we don't have an adapter, we don't need to do anything
		if (mAdapter == null) {
			return;
		}
		if (getChildCount() == 0) {
			mLastItemPosition = -1;
			fillListDown();
		} else {
			removeNonVisibleViews();
			// the new items are layed out using the old items
			fillListUp();
			fillListDown();
		}
	}
	
	public int getFirstItemPosition() {
		return mFirstItemPosition;
	}
	
	public int getLastItemPosition() {
		return mLastItemPosition;
	}
	
	
	protected void rebuild() {
		// start from firstItemPosition and go back to determine the first position that is valid
		removeAllViewsInLayout();
		if (mAdapter.getCount() > 0) {
			while (mFirstItemPosition >= mAdapter.getCount()) {
				mFirstItemPosition--;
			}
			// determine the top for the first item
			View firstView = mAdapter.getView(mFirstItemPosition, null, this);
			addAndMeasureChild(firstView, LAYOUT_MODE_ABOVE);
			// keep track of visible items height
			int h = firstView.getMeasuredHeight();
			
			if (mFirstItemPosition < mAdapter.getCount() - 1) {
				// try filling down
				int pos = mFirstItemPosition + 1;
				while ((pos < mAdapter.getCount()) &&
						(h < getHeight())) {
					View v = mAdapter.getView(pos, null, this);
					addAndMeasureChild(v, LAYOUT_MODE_BELOW);
					h += v.getMeasuredHeight();
					mLastItemPosition = pos;
					pos++;
				}
			} else {
				mLastItemPosition = mFirstItemPosition;
			}
			// now try filling up and determine scroll value
			int previtem = mFirstItemPosition - 1;
			int ntop = 0;
			while (previtem >= 0) {
				View mview = mAdapter.getView(previtem, null, this);
				if (mview != null) {
					if (h <= getHeight()) {
						addAndMeasureChild(mview, LAYOUT_MODE_ABOVE);
						h += mview.getMeasuredHeight();
						ntop = 0;
						mFirstItemPosition = previtem;
					} else {
						measureChild(mview);
						ntop += mview.getMeasuredHeight();
					}
				}
				previtem--;
			}
			// now we have a top
			// set scroll value and layout first
			scrollTo(0, ntop);
			// position children
			for (int i = 0; i < getChildCount(); i++) {
				View v = getChildAt(i);
				int vh = v. getMeasuredHeight();
				v.layout(0, ntop, getWidth(), ntop + vh);
				ntop += vh;
			}
			mLastItemPosition = mFirstItemPosition + getChildCount() - 1;
			requestLayout();
		} else {
			mFirstItemPosition = -1;
			mLastItemPosition = -1;
			requestLayout();
		}
	}
	
	/**
	 * Removes view that are outside of the visible part of the list. Will not
	 * remove all views.
	 * 
	 * @param offset
	 *            Offset of the visible area
	 */
	private void removeNonVisibleViews() {
		// We need to keep close track of the child count in this function. We
		// should never remove all the views, because if we do, we loose track
		// of were we are.
		int childCount = getChildCount();

		// if we are not at the bottom of the list and have more than one child
		if (mLastItemPosition != mAdapter.getCount() - 1 && childCount > 1) {
			// check if we should remove any views in the top
			View firstChild = getChildAt(0);
			while (firstChild != null && firstChild.getBottom() < getScrollY()) {
				// remove the top view
				removeViewInLayout(firstChild);
				childCount--;
				mFirstItemPosition++;

				// Continue to check the next child only if we have more than
				// one child left
				if (childCount > 1) {
					firstChild = getChildAt(0);
				} else {
					firstChild = null;
				}
			}
		}

		// if we are not at the top of the list and have more than one child
		if (mFirstItemPosition != 0 && childCount > 1) {
			// check if we should remove any views in the bottom
			View lastChild = getLastView();
			while (lastChild != null
					&& lastChild.getTop() > getScrollY() + getHeight()) {
				// remove the bottom view
				removeViewInLayout(lastChild);
				childCount--;
				mLastItemPosition--;
				// Continue to check the next child only if we have more than
				// one child left
				if (childCount > 1) {
					lastChild = getLastView();
				} else {
					lastChild = null;
				}
			}
		}
	}

	/**
	 * Starts at the bottom and adds children until we've passed the list bottom
	 * 
	 * @param bottomEdge
	 *            The bottom edge of the currently last child
	 * @param offset
	 *            Offset of the visible area
	 */
	private void fillListDown() {
		int bottomEdge = 0;
		if (getChildCount() != 0) {
			bottomEdge = getLastView().getBottom();
		}
		while (bottomEdge < getScrollY() + getHeight()
				&& mLastItemPosition < mAdapter.getCount() - 1) {
			mLastItemPosition++;
			final View newBottomchild = mAdapter.getView(mLastItemPosition,
					null, this);
			addAndMeasureChild(newBottomchild, LAYOUT_MODE_BELOW);
			newBottomchild.layout(0, bottomEdge, getWidth(), bottomEdge + newBottomchild.getMeasuredHeight()); 
			bottomEdge += newBottomchild.getMeasuredHeight();
		}
	}

	/**
	 * Starts at the top and adds children until we've passed the list top
	 * 
	 * @param topEdge
	 *            The top edge of the currently first child
	 * @param offset
	 *            Offset of the visible area
	 */
	private void fillListUp() {
		int topEdge = getChildAt(0).getTop();
		
		while (topEdge > getScrollY() && mFirstItemPosition > 0) {
			mFirstItemPosition--;
			final View newTopCild = mAdapter.getView(mFirstItemPosition,
					null, this);
			addAndMeasureChild(newTopCild, LAYOUT_MODE_ABOVE);
			final int childHeight = newTopCild.getMeasuredHeight();
			newTopCild.layout(0, topEdge - childHeight, getWidth(), topEdge); 
			topEdge -= childHeight;
		}
	}

	/**
	 * Adds a view as a child view and takes care of measuring it
	 * 
	 * @param child
	 *            The view to add
	 * @param layoutMode
	 *            Either LAYOUT_MODE_ABOVE or LAYOUT_MODE_BELOW
	 */
	protected void addAndMeasureChild(final View child, final int layoutMode) {
		measureChild(child);
		final int index = layoutMode == LAYOUT_MODE_ABOVE ? 0 : -1;
		addViewInLayout(child, index, child.getLayoutParams(), true);
	}

	/**
	 * measure a child view
	 */
	protected void measureChild(final View child) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
		}
		child.measure(MeasureSpec.EXACTLY | getWidth(), MeasureSpec.UNSPECIFIED);
	}

}
