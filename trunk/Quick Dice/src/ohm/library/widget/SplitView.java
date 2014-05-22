package ohm.library.widget;

import ohm.quickdice.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

/**
 * Strongly tailored for QDR layout.
 * @author Ohmnibus
 *
 */
public class SplitView extends LinearLayout implements OnTouchListener {

	/**
	 * Identifies the first element of the split view.
	 */
	final static public int FIRST = 0;
	/**
	 * Identifies the second element of the split view.
	 */
	final static public int SECOND = 1;

	private int mHandleId;
	private View mHandle;

	private int mFirstContentId;
	private View mFirstContent;

	private int mSecondContentId;
	private View mSecondContent;

	private int mLastFirstContentSize;

	private long mDraggingStarted;
	private float mDragStartX;
	private float mDragStartY;

	private float mPointerOffset;
	
	private ResizeListener mResizeListener = null;

	private static final int MATCH_PARENT = LayoutParams.FILL_PARENT; //FILL_PARENT for backward compatibility
	private static final int MAXIMIZED_VIEW_TOLERANCE_DIP = 30;
	private static final int TAP_DRIFT_TOLERANCE = 3;
	private static final int SINGLE_TAP_MAX_TIME = 175;
	
	/**
	 * Interface for the resize listener.
	 * @author Ohmnibus
	 *
	 */
	public interface ResizeListener {
		/**
		 * Callback method for the resize event.
		 * @param orientation Orientation of the SplitView.
		 * @param newSize New size of the first content.
		 */
		void onResize(int orientation, int newSize);
	}
	
	public class LayoutParams extends LinearLayout.LayoutParams {

		final static private int DEFAULT_MIN_SIZE = 0;
		
		public int minSize = DEFAULT_MIN_SIZE;
		
		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
		
		public LayoutParams(LinearLayout.LayoutParams source) {
			super((ViewGroup.LayoutParams)source);
			gravity = source.gravity;
			weight = source.weight;
		}
		
		public LayoutParams(LayoutParams source) {
			this((LinearLayout.LayoutParams)source);
			minSize = source.minSize;
		}
		
		public LayoutParams(int width, int height) {
			super(width, height);
		}
	}

	public SplitView(Context context) {
		super(context);
	}

	public SplitView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray viewAttrs = context.obtainStyledAttributes(attrs, R.styleable.SplitView);

		RuntimeException e = null;
		mHandleId = viewAttrs.getResourceId(R.styleable.SplitView_handle, 0);
		if (mHandleId == 0) {
			e = new IllegalArgumentException(
					viewAttrs.getPositionDescription()
					+ ": The required attribute \"handle\" must refer to a valid child view.");
		}

		mFirstContentId = viewAttrs.getResourceId(R.styleable.SplitView_firstContent, 0);
		if (mFirstContentId == 0) {
			e = new IllegalArgumentException(
					viewAttrs.getPositionDescription()
					+ ": The required attribute \"firstContent\" must refer to a valid child view.");
		}

		mSecondContentId = viewAttrs.getResourceId(R.styleable.SplitView_secondContent, 0);
		if (mSecondContentId == 0) {
			e = new IllegalArgumentException(
					viewAttrs.getPositionDescription()
					+ ": The required attribute \"secondContent\" must refer to a valid child view.");
		}

		viewAttrs.recycle();

		if (e != null) {
			throw e;
		}
	}

	@Override
	public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
		if (isInEditMode()) return new LayoutParams(super.generateLayoutParams(attrs));
		
		LayoutParams retVal = new LayoutParams(super.generateLayoutParams(attrs));
		
		TypedArray viewAttrs = this.getContext().obtainStyledAttributes(attrs, R.styleable.SplitView_LayoutParams);
		
		retVal.minSize = viewAttrs.getDimensionPixelOffset(R.styleable.SplitView_LayoutParams_minSize, LayoutParams.DEFAULT_MIN_SIZE);
		
		viewAttrs.recycle();
		
		return retVal;
	}
	
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return super.checkLayoutParams(p);
	}
	
	@Override
	protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
		if (isInEditMode()) return super.generateDefaultLayoutParams();
		
		LayoutParams retVal = new LayoutParams(super.generateDefaultLayoutParams());
		retVal.minSize = LayoutParams.DEFAULT_MIN_SIZE;
		return retVal;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		isMeasured = true;
		if (initRequired) {
			//If a resize has been requested, resize now.
			initRequired = !setContentSize(initWho, initSize);
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mHandle = findViewById(mHandleId);
		checkError(mHandle, mHandleId);

		mFirstContent = findViewById(mFirstContentId);
		checkError(mFirstContent, mFirstContentId);

		mSecondContent = findViewById(mSecondContentId);
		checkError(mSecondContent, mSecondContentId);

		mHandle.setOnTouchListener(this);
	}
	
	private void checkError(View view, int viewId) {
		if (view == null) {
			String name = getResources().getResourceEntryName(viewId);
			throw new RuntimeException("View \"R.id." + name + "\" not defined.");
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {

		if (view != mHandle) {
			return false;
		}

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			mDraggingStarted = SystemClock.elapsedRealtime();
			mDragStartX = me.getX();
			mDragStartY = me.getY();
			if (getOrientation() == VERTICAL) {
				mPointerOffset = me.getRawY() - mFirstContent.getHeight();
			} else {
				mPointerOffset = me.getRawX() - mFirstContent.getWidth();
			}
			return true;
		}
		else if (me.getAction() == MotionEvent.ACTION_UP) {
			int newSize;
			if (	mDragStartX < (me.getX() + TAP_DRIFT_TOLERANCE) && 
					mDragStartX > (me.getX() - TAP_DRIFT_TOLERANCE) && 
					mDragStartY < (me.getY() + TAP_DRIFT_TOLERANCE) &&
					mDragStartY > (me.getY() - TAP_DRIFT_TOLERANCE) &&		
					((SystemClock.elapsedRealtime() - mDraggingStarted) < SINGLE_TAP_MAX_TIME)) {

				if (isFirstContentMaximized() || isSecondContentMaximized()) {
					setContentSize(FIRST, mLastFirstContentSize);
					newSize = mLastFirstContentSize;
				} else {
					maximizeSecondContent();
					newSize = ((LayoutParams)mFirstContent.getLayoutParams()).minSize;
				}
			} else {
				newSize = getContentSize(FIRST);
			}
			if (mResizeListener != null) {
				mResizeListener.onResize(getOrientation(), newSize);
			}
			return true;
		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			if (getOrientation() == VERTICAL) {
				setContentHeight(mFirstContent, mSecondContent, (int)(me.getRawY() - mPointerOffset));
			} else {
				setContentWidth(mFirstContent, mSecondContent, (int)(me.getRawX() - mPointerOffset));
			}
		}
		return true;
	}

	/**
	 * Set the listener to the resize event.
	 * @param listener Listener to where notify to.
	 */
	public void setOnResizeListener(ResizeListener listener) {
		mResizeListener = listener;
	}
	
	/**
	 * Get a reference to the handle.
	 * @return
	 */
	public View getHandle() {
		return mHandle;
	}

	/**
	 * Get the specified content size.
	 * @param whichContent Content for which the size has to be read. It can be either {@link #FIRST} or {@link #SECOND}. 
	 * @return Size of the specified content.
	 */
	public int getContentSize(int whichContent) {
		int retVal;
		if (whichContent == FIRST) {
			if (getOrientation() == VERTICAL) {
				//return mFirstContent.getMeasuredHeight();
				retVal = mFirstContent.getHeight();
			} else {
				//return mFirstContent.getMeasuredWidth();
				retVal = mFirstContent.getWidth();
			}
		} else if (whichContent == SECOND) {
			if (getOrientation() == VERTICAL) {
				//return mSecondContent.getMeasuredHeight();
				retVal = mSecondContent.getHeight();
			} else {
				//return mSecondContent.getMeasuredWidth();
				retVal = mSecondContent.getWidth();
			}
		} else {
			throw new IllegalArgumentException("The content addressed for the operation must be \"FIRST\" or \"SECOND\".");
		}
		return retVal;
	}

	boolean isMeasured = false;
	boolean initRequired = false;
	int initWho = 0;
	int initSize = 0;
	
	/**
	 * Set the size of the specified content
	 * @param whichContent Content for which the size has to be set. It can be either {@link #FIRST} or {@link #SECOND}.
	 * @param newSize New size.
	 * @return A boolean indicating if the update is made.
	 */
	public boolean setContentSize(int whichContent, int newSize) {
		if (! isMeasured) {
			//If the view hasn't been measured, resize won't work.
			//Save new size request and use it after measure.
			initRequired = true;
			initWho = whichContent;
			initSize = newSize;
			return false;
		} else {
			if (whichContent == FIRST) {
				if (getOrientation() == VERTICAL) {
					return setContentHeight(mFirstContent, mSecondContent, newSize);
				} else {
					return setContentWidth(mFirstContent, mSecondContent, newSize);
				}
			} else if (whichContent == SECOND) {
				if (getOrientation() == VERTICAL) {
					return setContentHeight(mSecondContent, mFirstContent, newSize);
				} else {
					return setContentWidth(mSecondContent, mFirstContent, newSize);
				}
			} else {
				throw new IllegalArgumentException("The content addressed for the operation must be \"FIRST\" or \"SECOND\".");
			}
		}
	}

	/**
	 * Tell if the specified content is maximized.
	 * @param whichContent Content for which the size has to be set. It can be either {@link #FIRST} or {@link #SECOND}.
	 * @return A boolean indicating if the content is maximized.
	 */
	public boolean isContentMaximized(int whichContent) {
		if (whichContent == FIRST) {
			return isFirstContentMaximized();
		} else if (whichContent == SECOND) {
			return isSecondContentMaximized();
		} else {
			throw new IllegalArgumentException("The content addressed for the operation must be \"FIRST\" or \"SECOND\".");
		}
	}

	/**
	 * Maximize the specified content.
	 * @param whichContent Content for which the size has to be set. It can be either {@link #FIRST} or {@link #SECOND}.
	 */
	public void maximizeContent(int whichContent) {
		if (whichContent == FIRST) {
			maximizeFirstContent();
		} else if (whichContent == SECOND) {
			maximizeSecondContent();
		} else {
			throw new IllegalArgumentException("The content addressed for the operation must be \"FIRST\" or \"SECOND\".");
		}
	}

	private boolean isFirstContentMaximized() {
		LayoutParams params = (LayoutParams)mSecondContent.getLayoutParams();
		return
				(getOrientation() == VERTICAL && (mSecondContent.getHeight() < params.minSize + MAXIMIZED_VIEW_TOLERANCE_DIP) )
				||
				(getOrientation() == HORIZONTAL && (mSecondContent.getWidth() < params.minSize + MAXIMIZED_VIEW_TOLERANCE_DIP) );
	}

	private boolean isSecondContentMaximized() {
		LayoutParams params = (LayoutParams)mFirstContent.getLayoutParams();
		return
				(getOrientation() == VERTICAL && (mFirstContent.getHeight() < params.minSize + MAXIMIZED_VIEW_TOLERANCE_DIP) )
				||
				(getOrientation() == HORIZONTAL && (mFirstContent.getWidth() < params.minSize + MAXIMIZED_VIEW_TOLERANCE_DIP) );
	}

	private void maximizeFirstContent() {
		maximizeContentPane(mFirstContent, mSecondContent);
		//Set the first param to fill the remaining space.
		LayoutParams params = (LayoutParams)mFirstContent.getLayoutParams();
		params.weight = 1;
		params.height = MATCH_PARENT;
		mFirstContent.setLayoutParams(params);
	}

	private void maximizeSecondContent() {
		maximizeContentPane(mSecondContent, mFirstContent);
		//Set the first param to fill the remaining space.
		LayoutParams params = (LayoutParams)mFirstContent.getLayoutParams();
		params.weight = 1;
		params.height = MATCH_PARENT;
		mFirstContent.setLayoutParams(params);
	}

	private void maximizeContentPane(View toMaximize, View toMinimize) {
		mLastFirstContentSize = getContentSize(FIRST);

//		LayoutParams toMinParams = (LayoutParams)toMinimize.getLayoutParams();
//		LayoutParams toMaxParams = (LayoutParams)toMaximize.getLayoutParams();
//		toMinParams.weight = 0;
//		toMaxParams.weight = 0;
//		if (getOrientation() == VERTICAL) {
//			toMinParams.height = toMinParams.minSize;
//			toMaxParams.height = LayoutParams.FILL_PARENT; //getLayoutParams().height - mHandle.getLayoutParams().height;
//		} else {
//			toMinParams.width = toMinParams.minSize;
//			toMaxParams.width = LayoutParams.FILL_PARENT; //getLayoutParams().width - mHandle.getLayoutParams().width;
//		}
//		toMinimize.setLayoutParams(toMinParams);
//		toMaximize.setLayoutParams(toMaxParams);

		LayoutParams toMinParams = (LayoutParams)toMinimize.getLayoutParams();
		LayoutParams toMaxParams = (LayoutParams)toMaximize.getLayoutParams();
		
		toMinParams.weight = 0;
		toMaxParams.weight = 0;
		if (getOrientation() == VERTICAL) {
			int toMinSize = toMinimize.getHeight();
			int toMaxSize = toMaximize.getHeight();

			toMinParams.height = toMinParams.minSize;
			toMaxParams.height = toMaxSize + (toMinSize - toMinParams.minSize);
		} else {
			int toMinSize = toMinimize.getWidth();
			int toMaxSize = toMaximize.getWidth();

			toMinParams.width = toMinParams.minSize;
			toMaxParams.width = toMaxSize + (toMinSize - toMinParams.minSize);
		}
		toMinimize.setLayoutParams(toMinParams);
		toMaximize.setLayoutParams(toMaxParams);
	}
	
	private boolean setContentHeight(View targetView, View otherView, int newHeight) {
		int targetHeight = targetView.getHeight();
		int otherHeight = otherView.getHeight();
		LayoutParams targetParams = (LayoutParams)targetView.getLayoutParams();
		LayoutParams otherParams = (LayoutParams)otherView.getLayoutParams();
		
		if (newHeight > targetHeight) { //Top view is growing
			if (otherHeight <= otherParams.minSize) { //Bottom view is at it's min size
				return false;
			}
		
			int delta = newHeight - targetHeight;
			int maxDelta = otherHeight - otherParams.minSize;
			if (delta > maxDelta) { //Resized too much
				newHeight = targetHeight + maxDelta;
			}
		} else { //Top view is shrinking
			if (targetHeight <= targetParams.minSize) { //Top view is at it's min size
				return false;
			}
			
			if (newHeight < targetParams.minSize) { //Resized too much
				newHeight = targetParams.minSize;
			}
		}
		
		if (newHeight >= 0) {
//			otherParams.weight = 1;
//			otherParams.height = LayoutParams.FILL_PARENT;
//			otherView.setLayoutParams(otherParams);
//			
//			targetParams.weight = 0;
//			targetParams.height = newHeight;
//			targetView.setLayoutParams(targetParams);
			otherParams.weight = 0;
			otherParams.height = otherHeight - (newHeight - targetHeight);
			otherView.setLayoutParams(otherParams);
			
			targetParams.weight = 1;
			targetParams.height = MATCH_PARENT;
			targetView.setLayoutParams(targetParams);
		}
		
		return true;
	}
	
	private boolean setContentWidth(View targetView, View otherView, int newWidth) {
		int targetWidth = targetView.getWidth();
		int otherWidth = otherView.getWidth();
		LayoutParams targetParams = (LayoutParams)targetView.getLayoutParams();
		LayoutParams otherParams = (LayoutParams)otherView.getLayoutParams();
		
		if (newWidth > targetWidth) { //Left view is growing
			if (otherWidth <= otherParams.minSize) { //Left view is at it's min size
				return false;
			}
		
			int delta = newWidth - targetWidth;
			int maxDelta = otherWidth - otherParams.minSize;
			if (delta > maxDelta) { //Resized too much
				newWidth = targetWidth + maxDelta;
			}
		} else { //Left view is shrinking
			if (targetWidth <= targetParams.minSize) { //Left view is at it's min size
				return false;
			}
			
			if (newWidth < targetParams.minSize) { //Resized too much
				newWidth = targetParams.minSize;
			}
		}
		
		if (newWidth >= 0) {
//			otherParams.weight = 0;
//			if (otherParams.width > 0) {
//				otherParams.width = otherParams.width - (newWidth - targetParams.width);
//			} else {
//				otherParams.width = LayoutParams.FILL_PARENT;
//			}
//			otherView.setLayoutParams(otherParams);
//			
//			targetParams.weight = 0;
//			targetParams.width = newWidth;
//			targetView.setLayoutParams(targetParams);
			otherParams.weight = 0;
			otherParams.width = otherWidth - (newWidth - targetWidth);
			otherView.setLayoutParams(otherParams);
			
			targetParams.weight = 1;
			targetParams.height = MATCH_PARENT;
			targetView.setLayoutParams(targetParams);
		}
		
		return true;
	}
}