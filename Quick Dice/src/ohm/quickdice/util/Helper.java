package ohm.quickdice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ohm.quickdice.dialog.DiceBuilderDialog;
import ohm.quickdice.dialog.FunctionBuilderDialog;
import ohm.quickdice.dialog.VariablePickerDialog;
import ohm.quickdice.dialog.BuilderDialogBase.ReadyListener;
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
	 * Resize a {@link Drawable} to the given size.<br />
	 * Metrics and density are given by the {@link Resources} bount to this instance.
	 * @param ctx Context.
	 * @param id The desired resource identifier, as generated by the aapt tool. This integer encodes the package, type, and resource entry. The value 0 is an invalid identifier.
	 * @param width Desired width in {@code dp}.
	 * @param height Desired height in {@code dp}.
	 * @return A scaled {@link Drawable}.
	 */
	public static Drawable resizeDrawable(Context ctx, int id, int width, int height) {
		return resizeDrawable(ctx, ctx.getResources().getDrawable(id), width, height);
	}
	

	/**
	 * Resize a {@link Drawable} to the given size.<br />
	 * Metrics and density are given by the {@link Resources} bount to this instance.
	 * @param ctx Context.
	 * @param drawable {@link Drawable} to resize.
	 * @param width Desired width in {@code dp}.
	 * @param height Desired height in {@code dp}.
	 * @return A scaled {@link Drawable}.
	 */
	public static Drawable resizeDrawable(Context ctx, Drawable drawable, int width, int height) {
		Resources res = ctx.getResources();
		float density = res.getDisplayMetrics().density;
		
		//Get a bitmap from the drawable
		Bitmap bmp = drawableToBitmap(drawable);
		
		//Create a scaled bitmap
		bmp = Bitmap.createScaledBitmap(
				bmp,
				(int)(width * density),
				(int)(height * density),
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
		Bitmap retVal;
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = getScale(imagePath, width, height);
		opts.inJustDecodeBounds = false;
		
		Bitmap image = BitmapFactory.decodeFile(imagePath, opts);
		
		if (image == null) {
			return null;
		}
		
		if (image.getWidth() != width || image.getHeight() != height) {
			//Image need to be resized.
			int scaledWidth = image.getWidth();
			int scaledHeight = image.getHeight();
			if (scaledWidth - width > scaledHeight - height) {
				scaledHeight = (scaledHeight * width) / scaledWidth;
				scaledWidth = width;
			} else {
				scaledWidth = (scaledWidth * height) / scaledHeight;
				scaledHeight = height;
			}
			//image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, true);

			retVal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			retVal.eraseColor(Color.TRANSPARENT);
			
			Rect src = new Rect(0, 0, image.getWidth(), image.getHeight());
			Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);
			dst.offset((width - scaledWidth) / 2, (height - scaledHeight) / 2);
			
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
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap retVal;
		if (drawable instanceof BitmapDrawable) {
			//Easy
			retVal = ((BitmapDrawable)drawable).getBitmap();
		} else {
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

	/**
	 * Copy all the files from a folder to another.
	 * @param srcFolder Source folder.
	 * @param dstFolder Destination folder.
	 * @param cleanDstFolder Tell if destination folder has to be cleared before the copy starts.
	 * @return {@code true} if all the files where copied, {@code false} otherwise.
	 */
	public static boolean copyFiles(File srcFolder, File dstFolder, boolean cleanDstFolder) {
		boolean retVal = true;
		File[] files;
		
		//Clean destination folder
		if (cleanDstFolder) {
			files = dstFolder.listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			}
		}
		
		//Copy files
		files = srcFolder.listFiles();
		if (files != null) {
			for (File file : files) {
				boolean copied = copyFile(
						file,
						new File(dstFolder, file.getName()));
				retVal = retVal && copied;
			}
		}
		
		return retVal;
	}

	/**
	 * Copy a file.
	 * @param src Source file.
	 * @param dst Destination file.
	 * @return {@code true} if the copy succeeded, {@code false} otherwise.
	 */
	public static boolean copyFile(File src, File dst) {
		boolean retVal = false;
		try {
			FileInputStream inStream = new FileInputStream(src);
			try {
				FileOutputStream outStream = new FileOutputStream(dst);
				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = inStream.read(buf)) > 0) {
						outStream.write(buf, 0, len);
					}
					retVal = true;
				} finally {
					outStream.close();
				}
			} finally {
				inStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
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
	@SuppressWarnings("rawtypes")
	public static void requestBackup(Context ctx) {
		try {
			Class managerClass = Class.forName("android.app.backup.BackupManager");
			Constructor managerConstructor = managerClass.getConstructor(Context.class);
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

	public static OnClickListener getExpressionActionsClickListener(BuilderDialogBase.ReadyListener builderReadyListener) {
		return new ExpressionActionsClickListener(builderReadyListener);
	}

	protected static class ExpressionActionsClickListener implements View.OnClickListener {

		ReadyListener readyListener;

		public ExpressionActionsClickListener(ReadyListener readyListener) {
			this.readyListener = readyListener;
		}

		@Override
		public void onClick(View v) {
			PopupMenu popupMenu = Helper.getExpressionPopupMenu(v, readyListener);
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
	 * @param builderReadyListener Listener to be invoked when the dialog is dismissed.
	 * @return A {@link PopupMenu} ready to be shown.
	 */
	public static PopupMenu getExpressionPopupMenu(View v, BuilderDialogBase.ReadyListener builderReadyListener) {
		PopupMenu retVal;
		ActionItem ai;

		retVal = new PopupMenu(v);

		//Verify
		ai = new ActionItem();
		ai.setTitle(v.getContext().getResources().getString(R.string.lblCheckExpression));
		ai.setIcon(v.getContext().getResources().getDrawable(R.drawable.ic_check));
		ai.setOnClickListener(new CheckActionItemClickListener(retVal, builderReadyListener));
		retVal.addActionItem(ai);

		//Help
		ai = new ActionItem();
		ai.setTitle(v.getContext().getResources().getString(R.string.msgOnlineHelpTitle));
		ai.setIcon(v.getContext().getResources().getDrawable(R.drawable.ic_help));
		ai.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(v.getResources().getString(R.string.msgOnlineHelpURL)));
				v.getContext().startActivity(i);
			}
		});
		retVal.addActionItem(ai);

		//Dice Builder
		retVal.addActionItem(
				DiceBuilderDialog.getActionItem(
						v.getContext(),
						retVal,
						builderReadyListener)
				);

		//Named Values
		ai = VariablePickerDialog.getActionItem(
				v.getContext(),
				retVal,
				builderReadyListener);
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
							builderReadyListener,
							fnc[i])
					);
		}

		return retVal;
	}

	protected static class CheckActionItemClickListener implements View.OnClickListener {

		PopupMenu parent;
		ReadyListener readyListener;

		public CheckActionItemClickListener(PopupMenu parent, ReadyListener readyListener) {
			this.parent = parent;
			this.readyListener = readyListener;
		}

		@Override
		public void onClick(View v) {
			View refView = parent != null ? parent.getAnchor() : v;
			readyListener.ready(refView, true, BuilderDialogBase.ACTION_CHECK, null);
			if (parent != null) {
				parent.dismiss();
			}
		}
	}
}
