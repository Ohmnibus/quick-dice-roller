package ohm.quickdice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.DParseException;
import ohm.dexp.exception.DivisionByZero;
import ohm.dexp.exception.LoopDetected;
import ohm.dexp.exception.ExpectedEndOfStatement;
import ohm.dexp.exception.ExpectedParameter;
import ohm.dexp.exception.InvalidCharacter;
import ohm.dexp.exception.MissingOperand;
import ohm.dexp.exception.NothingToEvaluate;
import ohm.dexp.exception.ParameterOutOfBound;
import ohm.dexp.exception.UnbalancedBracket;
import ohm.dexp.exception.UnexpectedError;
import ohm.dexp.exception.UnexpectedParameter;
import ohm.dexp.exception.UnknownFunction;
import ohm.dexp.exception.UnknownVariable;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.dialog.BuilderDialogBase;
import ohm.quickdice.dialog.BuilderDialogBase.OnDiceBuiltListener;
import ohm.quickdice.dialog.DiceBuilderDialog;
import ohm.quickdice.dialog.FunctionBuilderDialog;
import ohm.quickdice.dialog.VariablePickerDialog;
import ohm.quickdice.entity.FunctionDescriptor;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Generic helper class.
 * @author Ohmnibus
 *
 */
public class Helper {
	
	/**
	 * Handle the background image.
	 * @author Ohmnibus
	 *
	 */
	public static class BackgroundManager {
		private static final String BACKGROUND_FOLDER = "backgroundDir";
		private static final String BACKGROUND_TEMP_FILE = "temp_background.jpg";
		private static final String BACKGROUND_FILE = "background.jpg";
		/** Size of the bigger edge of the image */
		private static final int BG_SIZE = 700;
//		private static final int BG_WIDTH = 600;
//		private static final int BG_HEIGHT = 800;
		
		private Context context;
		
		public BackgroundManager(Context context) {
			this.context = context;
		}
		
		/**
		 * Set the image at the specified URI as the new background image.
		 * @param rawImageUri Uri of the image to load.
		 * @param aspect Aspect ratio of the display.
		 * @return {@code true} if succeeded, {@code false} otherwise.
		 */
		public boolean setBackgroundImage(Uri rawImageUri, float aspect) {
			return setBackgroundImage(context, rawImageUri, aspect);
		}
		
		/**
		 * Tell if a background image is available.
		 * @return
		 */
		public boolean exists() {
			return exists(context);
		}
		
		public static String getBackgroundImagePath(Context ctx) {
			return getFile(ctx).getAbsolutePath();
		}
		
		public static File getBackgroundImageFile(Context ctx) {
			return getFile(ctx);
		}
		
		/**
		 * Tell if a background image is available.
		 * @param ctx
		 * @return
		 */
		public static boolean exists(Context ctx) {
			return getFile(ctx).exists();
		}
		
		/**
		 * Set the image at the specified URI as the new background image.
		 * @param ctx Context.
		 * @param rawImageUri Uri of the image to load.
		 * @param aspect Aspect ratio of the display.
		 * @return {@code true} if succeeded, {@code false} otherwise.
		 */
		public static boolean setBackgroundImage(Context ctx, Uri rawImageUri, float aspect) {
			File tmpImageFile = getTempFile(ctx);
			OutputStream fos;
			boolean pass;
			
			//Copy image locally
			pass = Files.copyFile(ctx, rawImageUri, tmpImageFile);
			
			if (! pass) {
				return false;
			}
			
			int width; // = BG_WIDTH;
			int height; // = BG_HEIGHT;
			if (aspect > 1) {
				//Horizontal
				width = BG_SIZE;
				height = (int) (BG_SIZE / aspect);
			} else {
				//Vertical
				width = (int) (BG_SIZE * aspect);
				height = BG_SIZE;
			}
			
			//Load scaled image
			Bitmap image = loadResizedBitmap(tmpImageFile.getAbsolutePath(), width, height, false, false);
			
			if (image == null) {
				return false;
			}
			
			//Save resized image
			pass = false;
			try {
				//Compress & save
				fos = new FileOutputStream(tmpImageFile);
				try {
					image.compress(Bitmap.CompressFormat.JPEG, 95, fos);
					pass = true;
				} finally {
					fos.close();
				}
			} catch (Exception e) {
				//Something went wrong.
				e.printStackTrace();
			}

			if (! pass) {
				return false;
			}

			//Rename image
			File imageFile = getFile(ctx);
			if (imageFile.exists()) {
				imageFile.delete();
			}
			pass = tmpImageFile.renameTo(imageFile);

			return pass;
		}
		
		private static File getDirectory(Context ctx) {
			return ctx.getDir(BACKGROUND_FOLDER, Context.MODE_PRIVATE);
		}
		
		private static File getTempFile(Context ctx) {
			File retVal;

			retVal = new File(
					getDirectory(ctx),
					BACKGROUND_TEMP_FILE);

			return retVal;
		}
		
		private static File getFile(Context ctx) {
			File retVal;

			retVal = new File(
					getDirectory(ctx),
					BACKGROUND_FILE);

			return retVal;
		}
	}

	private Helper() {}

	/* ************* */
	/* ** Dialogs ** */
	/* ************* */
	
	/**
	 * Show a confirm/cancel dialog.
	 * @param context The context to use. Usually your {@link Application} or {@link Activity} object.
	 * @param titleId Resource id for the title string.
	 * @param messageId Resource id for the message string.
	 * @param yesId Resource id for the confirm button message string.
	 * @param yesListener Listener for the confirm action.
	 * @param noId Resource id for the cancel button message string.
	 * @param noListener Listener for the cancel action.
	 */
	public static void showDialog(
			Context context,
			int titleId,
			int messageId,
			int yesId,
			DialogInterface.OnClickListener yesListener,
			int noId,
			DialogInterface.OnClickListener noListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(titleId);
		builder.setMessage(messageId);
		builder.setCancelable(false);
		builder.setPositiveButton(yesId, yesListener);
		builder.setNegativeButton(noId, noListener);
		builder.create().show();
	}

	/* ******************** */
	/* ** Error messages ** */
	/* ******************** */
	
	/**
	 * Display a toast showing the error message.
	 * @param context The context to use. Usually your {@link Application} or {@link Activity} object.
	 * @param e Exception containing error info.
	 */
	public static void showErrorToast(Context context, DException e) {
		Toast.makeText(context, getErrorMessage(context, e), Toast.LENGTH_LONG).show();
	}


	/**
	 * Construct an error message from an exception.
	 * @param context The context to use. Usually your {@link Application} or {@link Activity} object.
	 * @param e Exception containing error info.
	 * @return Localized error message.
	 */
	public static String getErrorMessage(Context context, DException e) {
		String retVal;
		Resources res = context.getResources();

		try {
			throw e;
		} catch (DivisionByZero ex) {
			retVal = res.getString(R.string.exc_division_by_zero);
		} catch (ExpectedEndOfStatement ex) {
			retVal = res.getString(R.string.exc_expected_eos, ex.getFromChar());
		} catch (ExpectedParameter ex) {
			retVal = res.getString(R.string.exc_expected_parameter, ex.getFromChar());
		} catch (InvalidCharacter ex) {
			retVal = res.getString(R.string.exc_invalid_character, ex.getFromChar());
		} catch (LoopDetected ex) {
			retVal = res.getString(R.string.exc_loop_detected, ex.getFunctionName());
		} catch (MissingOperand ex) {
			retVal = res.getString(R.string.exc_missing_operand, ex.getFromChar());
		} catch (NothingToEvaluate ex) {
			retVal = res.getString(R.string.exc_nothing_to_evaluate);
		} catch (ParameterOutOfBound ex) {
			retVal = res.getString(R.string.exc_parameter_out_of_bound, ex.getFunctionName(), ex.getParamIndex());
		} catch (UnbalancedBracket ex) {
			retVal = res.getString(R.string.exc_unbalanced_bracket, ex.getFromChar());
		} catch (UnexpectedError ex) {
			retVal = res.getString(R.string.exc_unexpected_error);
		} catch (UnexpectedParameter ex) {
			retVal = res.getString(R.string.exc_unexpected_parameter, ex.getFromChar());
		} catch (UnknownFunction ex) {
			retVal = res.getString(R.string.exc_unknown_function, ex.getName(), ex.getFromChar());
		} catch (UnknownVariable ex) {
			retVal = res.getString(R.string.exc_unknown_variable, ex.getName(), ex.getPosition());
		} catch (DParseException ex) {
			retVal = res.getString(R.string.exc_generic_parsed);
		} catch (DException ex) {
			retVal = res.getString(R.string.exc_generic);
		}

		return retVal;
	}


	/* ************* */
	/* ** Graphic ** */
	/* ************* */

	/** Used to get the mask of a drawable */
	private static Paint maskPaint = null;
	/** Used to smoothly resize images */
	private static Paint antiAliasPaint = null;
	/** Default canvas used to interact with bitmaps */
	private static Canvas canvas = new Canvas();
	
	/**
	 * Get the {@link Drawable} identified by {@code drawableId} and set its bounds
	 * with the dimensions identified by {@code widthDimenId} and {@code heightDimenId}.
	 * @param ctx Context
	 * @param drawableId Identifier of the drawable.
	 * @param widthDimenId Identifier of the resource to use as the width.
	 * @param heightDimenId Identifier of the resource to use as the height.
	 * @return The {@link Drawable} identified by {@code drawableId} bounded to {@code widthDimenId} and {@code heightDimenId}.
	 */
	public static Drawable boundedDrawable(Context ctx, int drawableId, int widthDimenId, int heightDimenId) {
		Drawable retVal;
		Resources res = ctx.getResources();
		retVal = res.getDrawable(drawableId);
		retVal.setBounds(0, 0, res.getDimensionPixelSize(widthDimenId), res.getDimensionPixelSize(heightDimenId));
		return retVal;
	}
	
	/**
	 * Resize a {@link Drawable} to the given size.<br />
	 * Metrics and density are given by the {@link Resources} bount to this instance.
	 * @param ctx Context.
	 * @param id The desired resource identifier, as generated by the aapt tool. This integer encodes the package, type, and resource entry. The value 0 is an invalid identifier.
	 * @param widthId Reference to the dimension containing the desired width.
	 * @param heightId Reference to the dimension containing the desired height.
	 * @return A scaled {@link Drawable}.
	 */
	public static Drawable resizeDrawable(Context ctx, int id, int widthId, int heightId) {
		return resizeDrawable(ctx, ctx.getResources().getDrawable(id), widthId, heightId);
	}
	

	/**
	 * Resize a {@link Drawable} to the given size.<br />
	 * Metrics and density are given by the {@link Resources} bount to this instance.
	 * @param ctx Context.
	 * @param drawable {@link Drawable} to resize.
	 * @param widthId Reference to the dimension containing the desired width.
	 * @param heightId Reference to the dimension containing the desired height.
	 * @return A scaled {@link Drawable}.
	 */
	public static Drawable resizeDrawable(Context ctx, Drawable drawable, int widthId, int heightId) {
		Resources res = ctx.getResources();
		//float density = res.getDisplayMetrics().density;
		
		//Get a bitmap from the drawable
		Bitmap bmp = drawableToBitmap(drawable);
		
		//Create a scaled bitmap
//		bmp = Bitmap.createScaledBitmap(
//				bmp,
//				(int)(width * density),
//				(int)(height * density),
//				true);
		bmp = Bitmap.createScaledBitmap(
				bmp,
				res.getDimensionPixelSize(widthId),
				res.getDimensionPixelSize(heightId),
				true);
		
		//Convert bitmap to drawable
		return new BitmapDrawable(res, bmp);
	}
	

	/**
	 * Get a mask of color {@code color} using the alpha channel of {@code source}.
	 * @param ctx Context.
	 * @param source Source {@link Drawable} containing the shape alpha channel.
	 * @param color Color of the shape.
	 * @return A {@link Bitmap} containing the shape of the given color.
	 */
	public static Drawable getMask(Context ctx, Drawable source, int color) {
		return new BitmapDrawable(ctx.getResources(), getMask(drawableToBitmap(source), color));
	}
	
	
	/**
	 * Load the image at {@code imagePath} as a {@link Bitmap}, scaling it to
	 * the specified size and preserving the aspect ratio.
	 * @param imagePath Path of the image to load.
	 * @param width Required width of the resulting {@link Bitmap}.
	 * @param height Required height of the resulting {@link Bitmap}.
	 * @return {@link Bitmap} representing the image at {@code imagePath}.
	 */
	public static Bitmap getIconFromImage(String imagePath, int width, int height) {
		return loadResizedBitmap(imagePath, width, height, true, false);
	}
	
	
	/**
	 * Load the image at {@code imagePath} as a {@link Bitmap}, scaling it to
	 * the specified size and preserving the aspect ratio.
	 * @param imagePath Path of the image to load.
	 * @param width Required width of the resulting {@link Bitmap}.
	 * @param height Required height of the resulting {@link Bitmap}.
	 * @param fill {@code true} to fill the empty space with transparent color.
	 * @param crop {@code true} to crop the image, {@code false} to resize without cutting the image.
	 * @return {@link Bitmap} representing the image at {@code imagePath}.
	 */
	public static Bitmap loadResizedBitmap(String imagePath, int width, int height, boolean fill, boolean crop) {
		Bitmap retVal;
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = getScale(imagePath, width, height);
		opts.inJustDecodeBounds = false;
		
		Bitmap image = BitmapFactory.decodeFile(imagePath, opts);
		
		if (image == null) {
			if (imagePath != null) {
				Log.w("Helper", "Cannot decode " + imagePath);
			} else {
				Log.w("Helper", "Path is null: Cannot decode");
			}
			return null;
		}
		
		if (image.getWidth() != width || image.getHeight() != height) {
			//Image need to be resized.
			int scaledWidth = (image.getWidth() * height) / image.getHeight();
			int scaledHeight;
			if ((crop && scaledWidth > width) || (!crop && scaledWidth < width)) {
				scaledHeight = height;
			} else {
				scaledWidth = width;
				scaledHeight = (image.getHeight() * width) / image.getWidth();
			}

			Rect src = new Rect(0, 0, image.getWidth(), image.getHeight());
			Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

			if (fill) {
				retVal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				dst.offset((width - scaledWidth) / 2, (height - scaledHeight) / 2);
			} else {
				retVal = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
			}
			retVal.eraseColor(Color.TRANSPARENT);
			
			synchronized (canvas) {
				if (antiAliasPaint == null) {
					antiAliasPaint = new Paint();
					antiAliasPaint.setAntiAlias(true);
					antiAliasPaint.setFilterBitmap(true);
					antiAliasPaint.setDither(true);
				}
				canvas.setBitmap(retVal);
				canvas.drawBitmap(image, src, dst, antiAliasPaint);
			}
			
			image.recycle();
		} else {
			//No need to scale.
			retVal = image;
		}

		return retVal;
	}
	
	private static int getScale(String imagePath, int desiredWidth, int desiredHeight) {
		//Get image size
		BitmapFactory.Options imageInfo = new BitmapFactory.Options();
		imageInfo.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, imageInfo);
		
		//Compute the scaling factor
		int scale = 1;
		int width = imageInfo.outWidth;
		int height = imageInfo.outHeight;
		//if (size < imageInfo.outHeight) size = imageInfo.outHeight;
		//while (size/(scale*2) >= ICON_SIZE) {
		while (width/(scale*2) >= desiredWidth && height/(scale*2) >= desiredHeight) {
			scale = scale*2;
		}
		
		return scale;
	}
	
	
	/**
	 * Get a mask of color {@code color} using the alpha channel of {@code source}.
	 * @param source Source image containing the shape alpha channel.
	 * @param color Color of the shape.
	 * @return A {@link Bitmap} containing the shape of the given color.
	 */
	private static Bitmap getMask(Bitmap source, int color) {
		Bitmap retVal;
		
		retVal = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		retVal.eraseColor(color);
		
		synchronized (canvas) {
			if (maskPaint == null) {
				maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			}
			
			canvas.setBitmap(retVal);
			canvas.drawBitmap(source, 0, 0, maskPaint);
		}
		
		return retVal;
	}
	
	/**
	 * Return the {@link Bitmap} representing the {@link Drawable}.
	 * @param drawable Object to convert to {@link Bitmap}.
	 * @return {@link Bitmap} representing the {@link Drawable}.
	 */
	private static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap retVal;
		if (drawable instanceof BitmapDrawable) {
			//Easy
			retVal = ((BitmapDrawable)drawable).getBitmap();
		} else {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			retVal = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

			synchronized (canvas) {
				canvas.setBitmap(retVal);
				drawable.draw(canvas);
			}
		}
		return retVal;
	}
	
	
	/* ************ */
	/* ** Others ** */
	/* ************ */

	protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	/**
	 * Convert a byte array to an hexadecimal string.
	 * @param bytes Data to convert
	 * @return Hexadecimal representation of the byte array.
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	/**
	 * Request the BackupManager for a backup.<br />
	 * Include checks for backward compatibility.
	 * @param ctx Context.
	 */
	public static void requestBackup(Context ctx) {
		try {
			Class<?> managerClass = Class.forName("android.app.backup.BackupManager");
			Constructor<?> managerConstructor = managerClass.getConstructor(Context.class);
			Object manager = managerConstructor.newInstance(ctx);
			Method m = managerClass.getMethod("dataChanged");
			m.invoke(manager);
			//Log.d("requestBackup", "Backup requested");
		} catch(ClassNotFoundException e) {
			//Log.d("requestBackup", "No backup manager found");
		} catch(Throwable t) {
			//Log.d("requestBackup", "Scheduling backup failed " + t);
			t.printStackTrace();
		}			
	}

	public static OnClickListener getExpressionActionsClickListener(OnDiceBuiltListener diceBuiltListener) {
		return new ExpressionActionsClickListener(diceBuiltListener);
	}

	protected static class ExpressionActionsClickListener implements View.OnClickListener {

		OnDiceBuiltListener diceBuiltListener;

		public ExpressionActionsClickListener(OnDiceBuiltListener diceBuiltListener) {
			this.diceBuiltListener = diceBuiltListener;
		}

		@Override
		public void onClick(View v) {
			PopupMenu popupMenu = Helper.getExpressionPopupMenu(v, diceBuiltListener);
			popupMenu.show();
		}
	}
	
	public static void setWakeLock(Activity activity, boolean wakeLock) {
		if (wakeLock) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	/**
	 * Substitute the selection of given {@link EditText} with specified text.<br />
	 * If no text is selected then specified text will be inserted at current cursor position.<br />
	 * Newly inserted text will be selected.
	 * @param editText Target {@link EditText}.
	 * @param txt Text to insert.
	 * @param select {@code true} to select newly added text.
	 */
	public static void setTextInsideSelection(EditText editText, String txt, boolean select) {
		Editable editable;
		int selStart;
		int selEnd;
		
		editable = editText.getText();

		selStart = editText.getSelectionStart();
		selEnd = editText.getSelectionEnd();
		if (selStart > selEnd) {
			int tmp = selStart;
			selStart = selEnd;
			selEnd = tmp;
		}
		
		editable.replace(selStart, selEnd, txt);

		if (select) {
			editText.setSelection(selStart, selStart + txt.length());
		} else {
			editText.setSelection(selStart + txt.length());
		}
	}

	/**
	 * Get the pop-up menu to be used with expressions.<br />
	 * @param v View for which the pop-up menu is called.
	 * @param diceBuiltListener Listener to be invoked when the dialog is dismissed.
	 * @return A {@link PopupMenu} ready to be shown.
	 */
	public static PopupMenu getExpressionPopupMenu(View v, OnDiceBuiltListener diceBuiltListener) {
		PopupMenu retVal;
		ActionItem ai;

		retVal = new PopupMenu(v);

		//Verify
		ai = new ActionItem();
		ai.setTitle(v.getContext().getResources().getString(R.string.lblCheckExpression));
		ai.setIcon(v.getContext().getResources().getDrawable(R.drawable.ic_check));
		ai.setOnClickListener(new CheckActionItemClickListener(retVal, diceBuiltListener));
		retVal.addActionItem(ai);

		//Help
		ai = new ActionItem();
		ai.setTitle(v.getContext().getResources().getString(R.string.msgOnlineHelpTitle));
		ai.setIcon(v.getContext().getResources().getDrawable(R.drawable.ic_help));
		ai.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				//i.setData(Uri.parse(v.getResources().getString(R.string.msgOnlineHelpURL)));
				i.setData(Uri.parse(v.getResources().getString(R.string.urlFncList)));
				v.getContext().startActivity(i);
			}
		});
		retVal.addActionItem(ai);

		//Dice Builder
		retVal.addActionItem(
				DiceBuilderDialog.getActionItem(
						v.getContext(),
						retVal,
						diceBuiltListener)
				);

		//Named Values
		ai = VariablePickerDialog.getActionItem(
				v.getContext(),
				retVal,
				diceBuiltListener);
		if (ai != null) {
			retVal.addActionItem(ai);
		}

		//Function Builders
		FunctionDescriptor[] fnc = QuickDiceApp.getInstance().getFunctionDescriptors();

		for (int i = 0; i < fnc.length; i++) {
			retVal.addActionItem(
					FunctionBuilderDialog.getActionItem(
							v.getContext(),
							retVal,
							diceBuiltListener,
							fnc[i])
					);
		}

		return retVal;
	}

	protected static class CheckActionItemClickListener implements View.OnClickListener {

		PopupMenu parent;
		OnDiceBuiltListener diceBuiltListener;

		public CheckActionItemClickListener(PopupMenu parent, OnDiceBuiltListener diceBuiltListener) {
			this.parent = parent;
			this.diceBuiltListener = diceBuiltListener;
		}

		@Override
		public void onClick(View v) {
			View refView = parent != null ? parent.getAnchor() : v;
			diceBuiltListener.onDiceBuilt(refView, true, BuilderDialogBase.ACTION_CHECK, null);
			if (parent != null) {
				parent.dismiss();
			}
		}
	}
}
