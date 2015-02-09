package ohm.quickdice.util;

import java.io.File;
import java.lang.ref.WeakReference;

import ohm.library.compat.CompatMisc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class AsyncDrawable extends Drawable /* LevelListDrawable */ {

	/**
	 * Interface for providers that load Drawables
	 * @author Ohmnibus
	 *
	 */
	public interface DrawableProvider {
		
		/**
		 * This is the method that load the image.
		 * @return
		 */
		Drawable getDrawable(Context context);
		
		/**
		 * This is a string that identify the loader.<br />
		 * If two loaders got the same hash, then they must load the same image.
		 * @return
		 */
		String getHash();
	}
	
	public static class PathDrawableProvider implements DrawableProvider {
		
		private final String path;
		private final String hash;
		
		public PathDrawableProvider(File file) {
			this(file.getAbsolutePath());
		}

		public PathDrawableProvider(String path) {
			this.path = path;
			this.hash = this.getClass().getName() + "|" + this.path;
		}

		@Override
		public Drawable getDrawable(Context context) {
			return Drawable.createFromPath(this.path);
		}

		@Override
		public String getHash() {
			return this.hash;
		}
		
	}
	
	private static class DrawableWorkerTask extends AsyncTask<DrawableProvider, Void, Drawable> {
		private final WeakReference<View> viewReference;
		private DrawableProvider drawableProvider;
		private final boolean isBackground;

		public DrawableWorkerTask(View view, DrawableProvider drawableProvider, boolean isBackground) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			this.viewReference = new WeakReference<View>(view);
			this.drawableProvider = drawableProvider;
			this.isBackground = isBackground;
		}

		// Decode image in background.
		@Override
		protected Drawable doInBackground(DrawableProvider... params) {
			Drawable retVal = null;
			if (viewReference != null) {
				DrawableProvider provider = params[0];
				final View view = viewReference.get();
				if (view != null && ! isCancelled()) {
					retVal = provider.getDrawable(view.getContext());
				}
			}
			return retVal;
		}

		// Once complete, see if ImageView is still around and set drawable.
		@Override
		protected void onPostExecute(Drawable drawable) {
			if (isCancelled()) {
				drawable = null;
			}
			if (viewReference != null && drawable != null) {
				final View view = viewReference.get();
				final AsyncDrawable asyncDrawable = getAsyncDrawable(view, isBackground);
				if (asyncDrawable != null && asyncDrawable.getDrawableWorkerTask() == this) {
					asyncDrawable.setDrawable(drawable);

					//Hack to correctly resize Drawable in ImageView
					view.setSelected(view.isSelected());
				}
			}
		}
		
		public void load() {
			execute(drawableProvider);
		}
	}
	
	private final WeakReference<DrawableWorkerTask> drawableWorkerTaskReference;
	
	private AsyncDrawable(Drawable defaultDrawable, DrawableWorkerTask drawableWorkerTask) {
		super();
		this.mDrawable = checkNullDrawable(defaultDrawable);
//		this.mAlpha = this.current.getAlpha();
//		this.mDither = this.current.
//		this.mColorFilter = this.current.getColorFilter();
		drawableWorkerTaskReference = new WeakReference<DrawableWorkerTask>(drawableWorkerTask);
	}
	
	private DrawableWorkerTask getDrawableWorkerTask() {
		return drawableWorkerTaskReference == null ? null : drawableWorkerTaskReference.get();
	}

	/**
	 * Asynchronously load the drawable provided by {@code drawableProvider}.
	 * @param imageView {@link ImageView} where to load the drawable.
	 * @param drawableProvider {@link DrawableProvider} used do load the drawable.
	 */
	public static void setDrawable(ImageView imageView, DrawableProvider drawableProvider) {
		setDrawable(imageView, null, drawableProvider);
	}
	
	/**
	 * Asynchronously load the drawable provided by {@code drawableProvider}.
	 * @param imageView {@link ImageView} where to load the drawable.
	 * @param placeHolder Drawable used as placeholder while loading, if {@code imageView} doesn't have none.
	 * @param drawableProvider {@link DrawableProvider} used do load the drawable.
	 */
	public static void setDrawable(ImageView imageView, Drawable placeHolder, DrawableProvider drawableProvider) {
		if (cancelPotentialWork(imageView, drawableProvider, false)) {
			final DrawableWorkerTask iconWorkerTask = new DrawableWorkerTask(imageView, drawableProvider, false);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					getImageViewDrawable(imageView, placeHolder),
					iconWorkerTask);
			imageView.setImageDrawable(asyncDrawable);
			iconWorkerTask.load();
		}
	}

	/**
	 * Asynchronously load the drawable provided by {@code drawableProvider}.
	 * @param view {@link View} where to load the drawable.
	 * @param drawableProvider {@link DrawableProvider} used do load the drawable.
	 */
	public static void setBackgroundDrawable(View view, DrawableProvider drawableProvider) {
		setBackgroundDrawable(view, null, drawableProvider);
	}
	
	/**
	 * Asynchronously load the drawable provided by {@code drawableProvider}.
	 * @param view {@link ImageView} where to load the drawable.
	 * @param placeHolder Drawable used as placeholder while loading, if {@code imageView} doesn't have none.
	 * @param drawableProvider {@link DrawableProvider} used do load the drawable.
	 */
	public static void setBackgroundDrawable(View view, Drawable placeHolder, DrawableProvider drawableProvider) {
		if (cancelPotentialWork(view, drawableProvider, true)) {
			final DrawableWorkerTask iconWorkerTask = new DrawableWorkerTask(view, drawableProvider, true);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					getViewBackground(view, placeHolder),
					iconWorkerTask);
			CompatMisc.getInstance().setBackgroundDrawable(view, asyncDrawable);
			iconWorkerTask.load();
		}
	}

	/**
	 * Check if an old work is in progress, and cancel it.
	 * @param drawableProvider New provider to check.
	 * @return {@code true} if a new work is need to be scheduled, {@code false} otherwise.
	 */
	private boolean cancelPotentialWork(DrawableProvider drawableProvider) {
		boolean retVal = true;
		final DrawableWorkerTask iconWorkerTask = getDrawableWorkerTask();
		if (iconWorkerTask != null) {
			if (drawableProvider == null || ! drawableProvider.getHash().equals(iconWorkerTask.drawableProvider.getHash())) {
				// Cancel previous task
				iconWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				retVal = false;
			}
		}
		
		// No task associated with the ImageView, or an existing task was cancelled
		return retVal;
	}
	
	private static Drawable getImageViewDrawable(ImageView imageView, Drawable defaultDrawable) {
		Drawable retVal = imageView.getDrawable();
		if (retVal == null) {
			retVal = defaultDrawable;
		} else if (retVal instanceof AsyncDrawable) {
			retVal = ((AsyncDrawable)retVal).getCurrent();
		}
		return retVal;
	}

	
	private static Drawable getViewBackground(View view, Drawable defaultDrawable) {
		Drawable retVal = view.getBackground();
		if (retVal == null) {
			retVal = defaultDrawable;
		} else if (retVal instanceof AsyncDrawable) {
			retVal = ((AsyncDrawable)retVal).getCurrent();
		}
		return retVal;
	}

	
	/**
	 * Check if an outdated work is in progress.
	 * @param view
	 * @param drawableProvider
	 * @return {@code true} if a new work is need to be scheduled, {@code false} otherwise.
	 */
	private static boolean cancelPotentialWork(View view, DrawableProvider drawableProvider, boolean isBackground) {
		boolean retVal = true;
		final AsyncDrawable asyncDrawable = getAsyncDrawable(view, isBackground);
		
		if (asyncDrawable != null) {
			retVal = asyncDrawable.cancelPotentialWork(drawableProvider);
		}
		
		return retVal;
	}

	/**
	 * Return the {@link AsyncDrawable} associated to the given {@link ImageView}, if any.
	 * @param imageView
	 * @return {@link AsyncDrawable} associated to the specified {@link ImageView}, if any, or {@code null}.
	 */
	private static AsyncDrawable getAsyncDrawable(View view, boolean isBackground) {
		AsyncDrawable retVal = null;
		if (view != null) {
			if (isBackground) {
				final Drawable drawable = view.getBackground();
				if (drawable instanceof AsyncDrawable) {
					retVal = (AsyncDrawable)drawable;
				}
			} else if (view instanceof ImageView) {
				retVal = getAsyncDrawable((ImageView)view);
			}
		}
		return retVal;
	}

	/**
	 * Return the {@link AsyncDrawable} associated to the given {@link ImageView}, if any.
	 * @param imageView
	 * @return {@link AsyncDrawable} associated to the specified {@link ImageView}, if any, or {@code null}.
	 */
	private static AsyncDrawable getAsyncDrawable(ImageView imageView) {
		AsyncDrawable retVal = null;
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				retVal = (AsyncDrawable)drawable;
			}
		}
		return retVal;
	}
	
	//********************************
	//** Drawable's wrapper methods **
	//********************************
	private Drawable mDrawable;
	private int mAlpha = 0xFF;
	private ColorFilter mColorFilter;
	private boolean mDither;

	public void setDrawable(Drawable drawable) {
		
		this.mDrawable.setVisible(false, false);
		
		this.mDrawable = checkNullDrawable(drawable);

		this.mDrawable.setVisible(isVisible(), true);
		this.mDrawable.setAlpha(mAlpha);
		this.mDrawable.setDither(mDither);
		this.mDrawable.setColorFilter(mColorFilter);
		this.mDrawable.setState(getState());
		this.mDrawable.setLevel(getLevel());
		this.mDrawable.setBounds(getBounds());

		invalidateSelf();
	}

	private Drawable checkNullDrawable(Drawable drawable) {
		if (drawable != null) {
			return drawable;
		}
		return new Drawable() {

			@Override
			public void setColorFilter(ColorFilter cf) {
			}

			@Override
			public void setAlpha(int alpha) {
			}

			@Override
			public int getOpacity() {
				return 0;
			}

			@Override
			public void draw(Canvas canvas) {
			}
		};
	}

	@Override
	public void draw(Canvas canvas) {
		mDrawable.draw(canvas);
	}

	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations()
				| mDrawable.getChangingConfigurations();
	}

	@Override
	public boolean getPadding(Rect padding) {
		return mDrawable.getPadding(padding);
	}

	@Override
	public void setAlpha(int alpha) {
		if (mAlpha != alpha) {
			mAlpha = alpha;
			mDrawable.setAlpha(alpha);
		}
	}

	@Override
	public void setDither(boolean dither) {
		if (mDither != dither) {
			mDither = dither;
			mDrawable.setDither(dither);
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		if (mColorFilter != cf) {
			mColorFilter = cf;
			mDrawable.setColorFilter(cf);
		}
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		mDrawable.setBounds(bounds);
	}

	@Override
	public boolean isStateful() {
		return mDrawable.isStateful();
	}
	
	@Override
	protected boolean onStateChange(int[] state) {
		return mDrawable.setState(state);
	}

	@Override
	protected boolean onLevelChange(int level) {
		return mDrawable.setLevel(level);
	}

	@Override
	public int getIntrinsicHeight() {
		return mDrawable.getIntrinsicHeight();
	}

	@Override
	public int getIntrinsicWidth() {
		return mDrawable.getIntrinsicWidth();
	}

	@Override
	public int getMinimumHeight() {
		return mDrawable.getMinimumHeight();
	}

	@Override
	public int getMinimumWidth() {
		return mDrawable.getMinimumWidth();
	}

	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		boolean changed = super.setVisible(visible, restart);
		mDrawable.setVisible(visible, restart);
		return changed;
	}

	@Override
	public int getOpacity() {
		return mDrawable.getOpacity();
	}

	@Override
	public ConstantState getConstantState() {
		return mDrawable.getConstantState();
	}

	@Override
	public Drawable mutate() {
		return mDrawable.mutate();
	}
}
