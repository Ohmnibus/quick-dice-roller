package ohm.library.compat;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

/**
 * Provides compatibility access to various methods.
 * @author Ohmnibus
 *
 */
public abstract class CompatMisc {

	private static CompatMisc instance = null;

	/**
	 * Special value for the height or width requested by a View.<br />
	 * {@code MATCH_PARENT} means that the view wants to be as big as its 
	 * parent, minus the parent's padding, if any.<br />
	 */
	public final int LAYOUT_MATCH_PARENT = getLayoutMatchParent();
	//public static final int LAYOUT_MATCH_PARENT = 0xffffffff;

	/**
	 * Special value for the height or width requested by a View.<br />
	 * {@code WRAP_CONTENT} means that the view wants to be just large enough 
	 * to fit its own internal content, taking its own padding into account.
	 */
	public final int LAYOUT_WRAP_CONTENT = getLayoutWrapContent();
	//public static final int LAYOUT_WRAP_CONTENT = LayoutParams.WRAP_CONTENT;
	
	/**
	 * Get the instance of the compatibility manager.
	 * @return Instance of {@link CompatMisc}.
	 */
	public static CompatMisc getInstance() {
		if (instance == null) {
			instance = createInstance();
		}
		return instance;
	}

	private static CompatMisc createInstance() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return new CompatMiscLollipop();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return new CompatMiscKitKat();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return new CompatMiscJellyBean();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new CompatMiscIceCreamSandwich();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatMiscHoneycomb();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return new CompatMiscFroyo();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			return new CompatMiscEclair();
		} else {
			return new CompatMiscDonut();
		}
	}
	
	/**
	 * Set the background of the view to a given Drawable.
	 * @param v The View to add the Drawable as background.
	 * @param d The Drawable to use as the background, or {@code null} to remove the background
	 */
	public abstract void setBackgroundDrawable(View v, Drawable d);

	/**
	 * Set the input type of the {@link EditText} in order to disable system soft keyboard.
	 * @param editText The {@link EditText} for which to disable system soft keyboard
	 */
	public abstract void setInputTypeNoKeyboard(EditText editText);
	
	/**
	 * The value to be assigned to {@link #LAYOUT_MATCH_PARENT}.
	 * @return Value to be assigned to {@link #LAYOUT_MATCH_PARENT}
	 */
	protected abstract int getLayoutMatchParent();

	/**
	 * The value to be assigned to {@link #LAYOUT_WRAP_CONTENT}.
	 * @return Value to be assigned to {@link #LAYOUT_WRAP_CONTENT}
	 */
	protected abstract int getLayoutWrapContent();
	
	/**
	 * Speaks the text using the specified queuing strategy.
	 * @param tts Instance of the TTS engine.
	 * @param text The string of text to be spoken.
	 * @param queueMode The queuing strategy to use, {@link TextToSpeech.QUEUE_ADD} or {@link TextToSpeech.QUEUE_FLUSH}.
	 * @return {@link TextToSpeech.ERROR} or {@link TextToSpeech.SUCCESS} of queuing the speak operation.
	 */
	public abstract int speak(TextToSpeech tts, String text, int queueMode);
	
	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.DONUT} (API 4).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.DONUT)
	private static class CompatMiscDonut extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackgroundDrawable(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.FILL_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}
	}
	
	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.ECLAIR} (API 5).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class CompatMiscEclair extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackgroundDrawable(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.FILL_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}
		
		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}

	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.FROYO} (API 8).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.FROYO)
	private static class CompatMiscFroyo extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackgroundDrawable(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}

	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.HONEYCOMB} (API 11).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class CompatMiscHoneycomb extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackgroundDrawable(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);

			editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
			editText.setTextIsSelectable(true);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}

	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.ICE_CREAM_SANDWICH} (API 14).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class CompatMiscIceCreamSandwich extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackgroundDrawable(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);

			editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
			editText.setTextIsSelectable(true);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}
	
	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.JELLY_BEAN} (API 16).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static class CompatMiscJellyBean extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackground(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);

			editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
			editText.setTextIsSelectable(true);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}
	
	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.KITKAT} (API 19).
	 * @author Ohmnibus
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static class CompatMiscKitKat extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackground(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);

			editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
			editText.setTextIsSelectable(true);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null);
		}

	}
	
	/**
	 * Derived class to be used with {@link Build.VERSION_CODES.LOLLIPOP} (API 21).
	 * @author Ohmnibus
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class CompatMiscLollipop extends CompatMisc {

		@Override
		public void setBackgroundDrawable(View v, Drawable d) {
			v.setBackground(d);
		}

		@Override
		public void setInputTypeNoKeyboard(EditText editText) {
			editText.setInputType(InputType.TYPE_NULL);

			editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
			editText.setTextIsSelectable(true);
		}
		
		@Override
		protected int getLayoutMatchParent() {
			return LayoutParams.MATCH_PARENT;
		}

		@Override
		protected int getLayoutWrapContent() {
			return LayoutParams.WRAP_CONTENT;
		}

		@Override
		public int speak(TextToSpeech tts, String text, int queueMode) {
			return tts.speak(text, queueMode, null, null);
		}

	}
}
