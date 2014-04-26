package ohm.quickdice.util;

import ohm.library.gesture.SwipeDismissTouchListener;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;

public class Behavior {
	
	/**
	 * Touch Listener used to replicate list items' long press. 
	 */
	public static class LongPressListener implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent me){
        	switch (me.getAction() & ACTION_MASK) {
        	case MotionEvent.ACTION_DOWN:
        		startLongPressAnimation(v);
        		break;
        	case MotionEvent.ACTION_UP:
        		resetLongPressAnimation(v);
        		break;
        	case MotionEvent.ACTION_CANCEL:
        		resetLongPressAnimation(v);
        		break;
        	}
        	return false;
        }
        
        CompleteLongPress completeLongPress;
        Handler myHandler = new Handler();
        
        protected void startLongPressAnimation(View view) {
        	view.setPressed(true);
        	Drawable current = view.getBackground().getCurrent();
        	if (current instanceof TransitionDrawable) {
            	((TransitionDrawable)current).startTransition(ViewConfiguration.getLongPressTimeout());
            	if (completeLongPress == null) {
            		completeLongPress = new CompleteLongPress();
            	}
            	completeLongPress.setView(view);
            	myHandler.postDelayed(completeLongPress, ViewConfiguration.getLongPressTimeout() + ViewConfiguration.getTapTimeout());
            }
        }
        
        protected void resetLongPressAnimation(View view) {
    		myHandler.removeCallbacks(completeLongPress);
    		
        	Drawable current = view.getBackground().getCurrent();
    		if (current instanceof TransitionDrawable) {
    			((TransitionDrawable)current).resetTransition();
    		}
        }
        
        class CompleteLongPress implements Runnable {
        	View mView;
        	
        	public void setView(View view) {
        		mView = view;
        	}
        	
            public void run() {
            	//Log.i("CheckLongPress", "LongPressed");
            	Drawable current = mView.getBackground().getCurrent();
            	if (current instanceof TransitionDrawable) {
                	((TransitionDrawable)current).resetTransition();
                	mView.setPressed(false);
                }
            }
        }
    }

    /**
	 * Touch listener that replicate list items' long press and
	 * handle scroll-to-dismiss gesture.
	 * @author Ohmnibus
	 *
	 */
	public static class RollResultTouchListener extends SwipeDismissTouchListener {
        
		public RollResultTouchListener(View view, Object token, DismissCallbacks callbacks) {
			super(view, token, callbacks, DIRECTION_RTOL);
		}

		public boolean onTouch(View v, MotionEvent me){
			if (super.onTouch(v, me)) {
				return true;
			}
			return listOnTouchListener.onTouch(v, me);
        }
	}

    private static final int ACTION_MASK = 0x000000ff; //MotionEvent.ACTION_MASK
    //private static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00; //MotionEvent.ACTION_POINTER_INDEX_MASK
    //private static final int ACTION_POINTER_INDEX_SHIFT = 0x00000008; //MotionEvent.ACTION_POINTER_INDEX_SHIFT

	/**
	 * Default touch Listener instance used to replicate list items' long press. 
	 */
	public static OnTouchListener listOnTouchListener = new LongPressListener();
}
