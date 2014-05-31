package ohm.library.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return new CompatMiscJellyBean();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new CompatMiscIceCreamSandwich();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatMiscHoneycomb();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return new CompatMiscFroyo();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			return new CompatMiscEclaire();
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
	 * The value to be assigned to {@link #LAYOUT_MATCH_PARENT}.
	 * @return Value to be assigned to {@link #LAYOUT_MATCH_PARENT}
	 */
	protected abstract int getLayoutMatchParent();

	/**
	 * The value to be assigned to {@link #LAYOUT_WRAP_CONTENT}.
	 * @return Value to be assigned to {@link #LAYOUT_WRAP_CONTENT}
	 */
	protected abstract int getLayoutWrapContent();
}
