package ohm.quickdice.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import ohm.quickdice.R;
import ohm.quickdice.control.IIconManager;
import ohm.quickdice.util.AsyncDrawable;
import ohm.quickdice.util.Helper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

/**
 * Class to handle an Icon object.
 * @author Ohmnibus
 *
 */
public abstract class Icon {

	/** Resource identifier for the default icon */
	public static final int DEFAULT_ICON_RES_ID = R.drawable.ic_dxx_gray;
	//public static final int DEFAULT_COLOR_RES_ID = R.color.default_dice_color;
	/** Default icon mask color */
	public static final int DEFAULT_COLOR = Color.argb(0xff, 0x88, 0x88, 0x88);
	
	private int id;
	protected IIconManager parent = null;
	
	/**
	 * Handle a system icon.
	 * @author Ohmnibus
	 *
	 */
	public static class SystemIcon extends Icon {
		private int drawableResId;
		private int color;
		
		protected SystemIcon(int drawableResId, int color) {
			this.drawableResId = drawableResId;
			this.color = color;
		}

		@Override
		public boolean isCustom() {
			return false;
		}

		@Override
		public Drawable getDrawable(Context ctx) {
			return ctx.getResources().getDrawable(drawableResId);
		}

		@Override
		public int getColor(Context ctx) {
			return color;
		}

		@Override
		public void setDrawable(ImageView imageView) {
			//This is a system icon. No need to load asynchronously
			imageView.setImageDrawable(getDrawable(imageView.getContext()));
		}

		@Override
		public boolean equals(Object o) {
			if (! (o instanceof SystemIcon)) {
				return false;
			}
			SystemIcon other = (SystemIcon)o;
			return drawableResId == other.drawableResId && color == other.color;
		}

		@Override
		public void recycle(Context ctx) {
			//NOOP
		}
		
		@Override
		public void folderChanged() {
			//NOOP
		}
	}
	
	/**
	 * Handle a custom icon, represented by a png file.
	 * @author Ohmnibus
	 *
	 */
	public static class CustomIcon extends Icon implements AsyncDrawable.DrawableProvider {
		
		private static final String TAG = "CustomIcon";
		
		//private static final String ICON_TEMP_FOLDER = "iconTmpDir";
		//private static final String ICON_FOLDER = "iconDir";
		//private static final String ICON_BACKUP_FOLDER = "iconBkuDir";
		private static final String ICON_NAME_PREFIX = "Icon";
		private static final String ICON_NAME_SUFFIX = ".png";
		private static final String ICON_NAME_FMT = ICON_NAME_PREFIX + "%05d" + ICON_NAME_SUFFIX;

		private static final String ICON_TEMP_NAME_PREFIX = "TmpIcon";
		private static final String ICON_TEMP_NAME_SUFFIX = ".png";

		private static final int ICON_SIZE = 72;
		
		private String hash;
		private String iconPath;
		private boolean pendingId = false;
		
		protected CustomIcon(String hash, String iconPath) {
			this.hash = hash;
			this.iconPath = iconPath;
		}
		
		@Override
		public void setId(int id) {
			int oldId = getId();
			super.setId(id);
			if (oldId != id) {
				//A new ID was assigned to this instance
//				//Rename file
//				File oldPath = new File(iconPath);
//				File newPath = new File(oldPath.getParent(), String.format(ICON_NAME_FMT, getId()));
//				if (newPath.exists()) {
//					newPath.delete();
//				}
//				oldPath.renameTo(newPath);
//				iconPath = newPath.getAbsolutePath();
				pendingId = true;
			}
			checkMoveFile();
		}
		
		@Override
		public boolean isCustom() {
			return true;
		}

		@Override
		public Drawable getDrawable(Context ctx) {
			Drawable retVal = Drawable.createFromPath(iconPath);
			if (retVal == null) {
				retVal = ctx.getResources().getDrawable(DEFAULT_ICON_RES_ID);
			}
			return retVal;
		}

		@Override
		public int getColor(Context ctx) {
			return DEFAULT_COLOR;
		}
		
		private static Drawable defaultDrawable = null;
		
		@Override
		public void setDrawable(ImageView imageView) {
			if (defaultDrawable == null) {
				defaultDrawable = imageView.getResources().getDrawable(DEFAULT_ICON_RES_ID);
			}
			AsyncDrawable.setDrawable(imageView, defaultDrawable, this);
		}

		@Override
		public boolean equals(Object o) {
			if (! (o instanceof CustomIcon)) {
				return false;
			}
			CustomIcon other = (CustomIcon)o;
			return hash.equals(other.hash);
		}
		
		@Override
		public void recycle(Context ctx) {
			File file = new File(getIconPath());
			file.delete();
		}
		
		@Override
		protected void setParent(IIconManager parent) {
			super.setParent(parent);
			checkMoveFile();
		}
		
		@Override
		public void folderChanged() {
			checkMoveFile();
		}

		/**
		 * Get the hash code associated with this icon.
		 * @return Hash code (md5)
		 */
		public String getHash() {
			return hash;
		}
		
		/**
		 * Absolute path of the icon file.
		 * @return
		 */
		public String getIconPath() {
			return iconPath;
		}
		
		/**
		 * Check if the icon file need to be moved/renamed.
		 */
		private void checkMoveFile() {
			
			if (parent == null) {
				Log.i(TAG, "checkMoveFile: Waiting for parent.");
				return; //No operation allowed without parent
			}
			
//			if (pendingId && parent != null) {
//				//Rename or move file
//				
//				File oldPath = new File(iconPath);
//				File newPath = new File(parent.getIconDirectory(), String.format(ICON_NAME_FMT, getId()));
//				if (newPath.exists()) {
//					newPath.delete();
//				}
//				oldPath.renameTo(newPath);
//				iconPath = newPath.getAbsolutePath();
//
//				pendingId = false;
//			}

			File oldPath = new File(iconPath);
			File newPath = getIconFile(parent.getIconFolder(), getId()); // new File(parent.getIconFolder(), String.format(ICON_NAME_FMT, getId()));
			if (/* pendingId ||*/ oldPath.equals(newPath) == false) {
				if (newPath.exists()) {
					newPath.delete();
				}
				oldPath.renameTo(newPath);
				iconPath = newPath.getAbsolutePath();
				pendingId = false;
				Log.i(TAG, "checkMoveFile: Moved from " + oldPath.getAbsolutePath() + " to " + iconPath);
			} else {
				if (pendingId)
					Log.i(TAG, "checkMoveFile: Same file name:" + iconPath);
				else
					Log.i(TAG, "checkMoveFile: No pending request");
			}
		}
		

		/**
		 * Create an instance of {@link CustomIcon} given the URI of an image.
		 * @param ctx Context.
		 * @param rawIconUri URI of the image to use as icon.
		 * @return New instance of {@link CustomIcon}, or {@code null} if an error occurred.
		 */
		protected static CustomIcon createIcon(Context ctx, Uri rawIconUri) {
			CustomIcon retVal = null;
			
			//Import image locally
			File tmpIconFile = CustomIcon.getTempFile(ctx);
			InputStream fis;
			OutputStream fos;
			int byteCount;
			byte[] buffer = new byte[2048];
			boolean imported = false;
			
			try {
				fis = ctx.getContentResolver().openInputStream(rawIconUri);
				try {
					fos = new FileOutputStream(tmpIconFile);
					try {
						while ((byteCount = fis.read(buffer)) > 0) {
							fos.write(buffer, 0, byteCount);
						}
						imported = true;
					} finally {
						fos.close();
					}
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (! imported) {
				return null;
			}
			
			//Load scaled image
			Bitmap image = Helper.getIconFromImage(tmpIconFile.getAbsolutePath(), ICON_SIZE, ICON_SIZE);
			
			if (image == null) {
				return null;
			}
			
			//Save resized image and compute Md5
			MessageDigest md5;
			String hash = null;

			try {
				//Compress & save icon
				fos = new FileOutputStream(tmpIconFile);
				try {
					image.compress(Bitmap.CompressFormat.PNG, 100, fos);
				} finally {
					fos.close();
				}
				
				//Compute md5
				md5 = MessageDigest.getInstance("MD5");
				
				fis = new FileInputStream(tmpIconFile);
				try {
					while ((byteCount = fis.read(buffer)) > 0) {
						md5.update(buffer, 0, byteCount);
					}
					hash = Helper.bytesToHex(md5.digest());
				} finally {
					fis.close();
				}
			} catch (Exception e) {
				//Something went wrong.
				hash = null;
				e.printStackTrace();
			}

			if (hash != null) {
				retVal = new CustomIcon(hash, tmpIconFile.getAbsolutePath());
				Log.i(TAG, "createIcon: Imported from " + rawIconUri.toString() + " to " + retVal.getIconPath());
			} else {
				Log.w(TAG, "createIcon: Cannot import from " + rawIconUri.toString());
			}
			
			return retVal;
		}
		
		/**
		 * Get a temporary file to store the icon.
		 * @param ctx Context
		 * @return Reference to a temporary file.
		 */
		public static File getTempFile(Context ctx) {
			File retVal;
			//File tempDir = ctx.getDir(ICON_TEMP_FOLDER, Context.MODE_PRIVATE);
			File tempDir = ctx.getCacheDir();
			try {
				retVal = File.createTempFile(ICON_TEMP_NAME_PREFIX, ICON_TEMP_NAME_SUFFIX, tempDir);
			} catch (IOException e) {
				retVal = new File(tempDir, ICON_TEMP_NAME_PREFIX + "_exc" + ICON_TEMP_NAME_SUFFIX);
				e.printStackTrace();
			}
			return retVal;
		}

		public static File getIconFile(Context ctx, String iconFolder, int id) {
			return getIconFile(ctx.getDir(iconFolder, Context.MODE_PRIVATE), id);
		}

		public static File getIconFile(File iconFolder, int id) {
			return new File(iconFolder, String.format(ICON_NAME_FMT, id));
		}
	}
	
	/**
	 * Create a new instance of Icon representing a system icon.
	 * @param drawableResId Resource ID of the icon's {@link Drawable}.
	 * @param color Color for the background of the icon.
	 * @return A new instance of a {@link SystemIcon}.
	 */
	public static Icon newIcon(int drawableResId, int color) {
		return new SystemIcon(drawableResId, color);
	}
	
	/**
	 * Create a new instance of Icon representing a custom icon.
	 * @param ctx Context.
	 * @param rawIconUri URI of the image to use as icon.
	 * @return New instance of {@link CustomIcon}, or {@code null} if an error occurred.
	 */
	public static Icon newIcon(Context ctx, Uri rawIconUri) {
		return CustomIcon.createIcon(ctx, rawIconUri);
	}
	
	/**
	 * Create a new instance of Icon representing a custom icon.
	 * @param hash Hash of the image file.
	 * @param iconPath Path of the image file.
	 * @return New instance of {@link CustomIcon}.
	 */
	public static Icon newIcon(String hash, String iconPath) {
		return new CustomIcon(hash, iconPath);
	}
	
	/**
	 * The unique ID of the icon.<br />
	 * If an ID is below 1000, then is a system icon, otherwise is a custom icon.
	 * @return Unique ID of the icon
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the unique ID of the icon.<br />
	 * If an ID is below 1000, then is a system icon, otherwise is a custom icon.
	 * @param id Unique ID of the icon
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the {@link IIconManager} containing this instance, or {@code null} if not assigned.
	 * @return The {@link IIconManager} containing this instance, or {@code null} if not assigned
	 */
	public IIconManager getParent() {
		return parent;
	}

	/**
	 * Assign a parent {@link IIconManager} containing this instance.<br>
	 * Use {@code null} to reset parenthood.
	 * @param parent The {@link IIconManager} containing this instance.
	 */
	protected void setParent(IIconManager parent) {
		this.parent = parent;
	}


	/**
	 * Convenience method to tell if this is a custom or a system icon.
	 * @return {@code true} is is a custom icon, {@code false} otherwise.
	 */
	public abstract boolean isCustom();
	
	/**
	 * Get the drawable corresponding to this icon.
	 * @param ctx Context.
	 * @return Drawable associated to this icon.
	 */
	public abstract Drawable getDrawable(Context ctx);
	
	/**
	 * Asynchronously load the drawable of this icon to the specified ImageView.
	 * @param imageView
	 */
	public abstract void setDrawable(ImageView imageView);
	
	/**
	 * Get the color to use as background.
	 * @param ctx
	 * @return
	 */
	public abstract int getColor(Context ctx);
	
	/**
	 * Clear all the resource used by this icon.<br />
	 * This method is meant to be called upon icon deletion.
	 * @param ctx
	 */
	public abstract void recycle(Context ctx);
	
	/**
	 * Notify the change of the icon folder.
	 */
	public abstract void folderChanged();
}
