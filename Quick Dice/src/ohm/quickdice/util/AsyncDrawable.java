package ohm.quickdice.util;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class AsyncDrawable extends LevelListDrawable {

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
	
	private static class DrawableWorkerTask extends AsyncTask<DrawableProvider, Void, Drawable> {
		private final WeakReference<ImageView> imageViewReference;
		private DrawableProvider drawableProvider;

		public DrawableWorkerTask(ImageView imageView, DrawableProvider drawableProvider) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			this.imageViewReference = new WeakReference<ImageView>(imageView);
			this.drawableProvider = drawableProvider;
		}

		// Decode image in background.
		@Override
		protected Drawable doInBackground(DrawableProvider... params) {
			Drawable retVal = null;
			if (imageViewReference != null) {
				DrawableProvider provider = params[0];
				final ImageView imageView = imageViewReference.get();
				if (imageView != null && ! isCancelled()) {
					retVal = provider.getDrawable(imageView.getContext());
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
			if (imageViewReference != null && drawable != null) {
				final ImageView imageView = imageViewReference.get();
				final AsyncDrawable asyncDrawable = getAsyncDrawable(imageView);
				if (asyncDrawable != null && asyncDrawable.getDrawableWorkerTask() == this) {
					asyncDrawable.addLevel(DRAWABLE_INDEX_COMPLETE, DRAWABLE_INDEX_COMPLETE, drawable);
					asyncDrawable.setLevel(DRAWABLE_INDEX_COMPLETE);
				}
			}
		}
		
		public void load() {
			execute(drawableProvider);
		}
	}
	
	private static final int DRAWABLE_INDEX_DEFAULT = 0;
	private static final int DRAWABLE_INDEX_COMPLETE = 1;
	private final WeakReference<DrawableWorkerTask> drawableWorkerTaskReference;
	
	private AsyncDrawable(Resources res, Drawable defaultDrawable, DrawableWorkerTask drawableWorkerTask) {
		super();
		addLevel(DRAWABLE_INDEX_DEFAULT, DRAWABLE_INDEX_DEFAULT, defaultDrawable);
		setLevel(DRAWABLE_INDEX_DEFAULT);
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
		if (cancelPotentialWork(imageView, drawableProvider)) {
			final DrawableWorkerTask iconWorkerTask = new DrawableWorkerTask(imageView, drawableProvider);
			final Resources resources = imageView.getResources();
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					resources,
					getImageViewDrawable(imageView, placeHolder),
					iconWorkerTask);
			imageView.setImageDrawable(asyncDrawable);
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

	
	/**
	 * Check if an outdated work is in progress.
	 * @param imageView
	 * @param drawableProvider
	 * @return {@code true} if a new work is need to be scheduled, {@code false} otherwise.
	 */
	private static boolean cancelPotentialWork(ImageView imageView, DrawableProvider drawableProvider) {
		boolean retVal = true;
		final AsyncDrawable asyncDrawable = getAsyncDrawable(imageView);
		
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
}
