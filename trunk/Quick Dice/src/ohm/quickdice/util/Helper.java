package ohm.quickdice.util;

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
import ohm.quickdice.dialog.BuilderDialogBase.ReadyListener;
import ohm.quickdice.entity.FunctionDescriptor;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Generic helper class.
 * @author Ohmnibus
 *
 */
public class Helper {

	Context ctx;
	Resources res;
	
	/**
	 * Initialize the helper based on a context.
	 * @param context
	 */
	public Helper(Context context) {
		ctx = context;
		res = ctx.getResources();
	}
	
	/**
	 * Show a confirm/cancel dialog.
	 * @param titleId Resource id for the title string.
	 * @param messageId Resource id for the message string.
	 * @param yesId Resource id for the confirm button message string.
	 * @param yesListener Listener for the confirm action.
	 * @param noId Resource id for the cancel button message string.
	 * @param noListener Listener for the cancel action.
	 */
	public void showDialog(
			int titleId,
			int messageId,
			int yesId,
			DialogInterface.OnClickListener yesListener,
			int noId,
			DialogInterface.OnClickListener noListener) {
		
		showDialog(
				ctx,
				titleId,
				messageId,
				yesId,
				yesListener,
				noId,
				noListener);
	}
	
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
	
	/**
	 * Display a toast showing the error message.
	 * @param e Exception containing error info.
	 */
	public void showErrorToast(DException e) {
		showErrorToast(ctx, e);
	}
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
	 * @param e Exception containing error info.
	 * @return Localized error message.
	 */
	public String getErrorMessage(DException e) {
		return getErrorMessage(ctx, e);
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
		} catch (LoopDetected ex) {
			retVal = String.format(res.getString(R.string.exc_loop_detected), ex.getFunctionName());
		} catch (ExpectedEndOfStatement ex) {
			retVal = String.format(res.getString(R.string.exc_expected_eos), ex.getFromChar());
		} catch (ExpectedParameter ex) {
			retVal = String.format(res.getString(R.string.exc_expected_parameter), ex.getFromChar());
		} catch (InvalidCharacter ex) {
			retVal = String.format(res.getString(R.string.exc_invalid_character), ex.getFromChar());
		} catch (MissingOperand ex) {
			retVal = String.format(res.getString(R.string.exc_missing_operand), ex.getFromChar());
		} catch (NothingToEvaluate ex) {
			retVal = res.getString(R.string.exc_nothing_to_evaluate);
		} catch (UnbalancedBracket ex) {
			retVal = String.format(res.getString(R.string.exc_unbalanced_bracket), ex.getFromChar());
		} catch (UnexpectedError ex) {
			retVal = res.getString(R.string.exc_unexpected_error);
		} catch (UnexpectedParameter ex) {
			retVal = String.format(res.getString(R.string.exc_unexpected_parameter), ex.getFromChar());
		} catch (UnknownFunction ex) {
			retVal = String.format(res.getString(R.string.exc_unknown_function), ex.getName(), ex.getFromChar());
		} catch (UnknownVariable ex) {
			retVal = String.format(res.getString(R.string.exc_unknown_variable), ex.getName(), ex.getFromChar());
		} catch (DParseException ex) {
			retVal = res.getString(R.string.exc_generic_parsed);
		} catch (DException ex) {
			retVal = res.getString(R.string.exc_generic);
		}
		
		return retVal;
	}
	
	/**
	 * Get a resource string given it's {@link Context} and it's id.
	 * @param context {@link Context} of the string.
	 * @param resId Unique resource id.
	 * @return String from resources.
	 */
	public static String getString(Context context, int resId) {
		return getString(context.getResources(), resId);
	}
	
	/**
	 * Get a formatted resource string given it's {@link Context} and it's id.
	 * @param context {@link Context} of the string.
	 * @param resId Unique resource id.
	 * @param args Arguments used to format the string.
	 * @return String from resources.
	 */
	public static String getString(Context context, int resId, Object... args) {
		return getString(context.getResources(), resId, args);
	}

	/**
	 * Get a resource string given the context resources and it's id.
	 * @param resources {@link Resources} from which get the string.
	 * @param resId Unique resource id.
	 * @return String from resources.
	 */
	public static String getString(Resources resources, int resId) {
		return resources.getString(resId);
	}
	
	/**
	 * Get a formatted resource string given the context resources and it's id.
	 * @param resources {@link Resources} from which get the string.
	 * @param resId Unique resource id.
	 * @param args
	 * @return String from resources.
	 */
	public static String getString(Resources resources, int resId, Object... args) {
		return String.format(getString(resources, resId), args);
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
