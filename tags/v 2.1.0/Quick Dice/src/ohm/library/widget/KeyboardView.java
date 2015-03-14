package ohm.library.widget;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;
import android.view.View;

/**
 * Extend the {@link android.inputmethodservice.KeyboardView} in order to support
 * keyboard resize in dialogs.
 * @author Ohmnibus
 *
 */
public class KeyboardView extends android.inputmethodservice.KeyboardView {

	/**
	 * Constructor that is called when inflating a view from XML.
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 */
	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Perform inflation from XML and apply a class-specific base style.
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 * @param defStyle An attribute in the current theme that contains a reference to a style resource to apply to this view. If 0, no default style will be applied.
	 */
	public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Keyboard keyboard = getKeyboard();
		
		int widthAvailable = View.MeasureSpec.getSize(widthMeasureSpec);
		
		//Compute Keyboard width
		int keyboardWidth; // = 51 * 5 + 64;
		int curWidth;
		
		keyboardWidth = 0;
		
		for(Key key : keyboard.getKeys()) {
			curWidth = key.x + key.width + key.gap;
			if (curWidth > keyboardWidth) {
				keyboardWidth = curWidth;
			}
		}
		
		//Resize keyboard
		if (keyboardWidth != widthAvailable) { //Use a tolerance
			for(Key key : keyboard.getKeys()) {
				key.width = (key.width * widthAvailable) / keyboardWidth;
				key.x = (key.x * widthAvailable) / keyboardWidth;
				key.gap = (key.gap * widthAvailable) / keyboardWidth;
			}
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
