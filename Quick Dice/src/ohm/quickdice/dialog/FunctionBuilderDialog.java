package ohm.quickdice.dialog;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.DParseException;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.dialog.BuilderDialogBase.ReadyListener;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.FunctionDescriptor;
import ohm.quickdice.entity.FunctionDescriptor.ParamDescriptor;
import ohm.quickdice.util.Helper;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FunctionBuilderDialog extends BuilderDialogBase {

	FunctionDescriptor fncDesc;

	ViewGroup paramContainer;
	EditText[] txtParamArray;

	public FunctionBuilderDialog(Context context, View view, FunctionDescriptor functionDescriptor, ReadyListener readyListener) {
		super(context, view, readyListener);

		this.fncDesc = functionDescriptor;
	}

	@SuppressLint("InflateParams")
	@Override
	protected void setupDialog(AlertDialog dialog) {
		Context context = dialog.getContext();
		LayoutInflater inflater = dialog.getLayoutInflater();

		dialog.setTitle(fncDesc.getName());

		View mView = inflater.inflate(R.layout.function_builder_dialog, null);

		((TextView)mView.findViewById(R.id.fbFunctionBuilderMessage)).setText(fncDesc.getDesc());

		String msg;

		msg = "<a href=\"" + fncDesc.getUrl() + "\">" +
				context.getString(R.string.msgOnlineHelp) +
				"</a>";

		((TextView)mView.findViewById(R.id.fbFunctionBuilderLink)).setText(Html.fromHtml(msg, null, null));
		((TextView)mView.findViewById(R.id.fbFunctionBuilderLink)).setMovementMethod(LinkMovementMethod.getInstance());

		paramContainer = (ViewGroup)mView.findViewById(R.id.fbParamList);

		paramContainer.removeAllViews();

		ParamDescriptor[] paramArray; 
		ParamDescriptor param; 
		View paramView;
		TextView paramLabel;
		EditText paramValue = null;
		ImageButton paramMenu;

		paramArray = fncDesc.getParameters();
		txtParamArray = new EditText[paramArray.length];
		
		int baseId = mView.getId();

		for (int i = 0; i < paramArray.length; i++) {
			param = paramArray[i];

			paramView = inflater.inflate(R.layout.function_builder_item, paramContainer, false);

			paramLabel = (TextView)paramView.findViewById(R.id.fbiLabel);
			paramValue = (EditText)paramView.findViewById(R.id.fbiValue);
			paramMenu = (ImageButton)paramView.findViewById(R.id.fbiMenu);

			paramLabel.setText(param.getLabel());
			paramValue.setHint(param.getHint());

			paramMenu.setTag(paramValue);
			paramMenu.setOnClickListener(Helper.getExpressionActionsClickListener(builderReadyListener));

			paramContainer.addView(paramView);
			
			txtParamArray[i] = paramValue;
			//txtParamArray[i].setId(View.generateViewId());
			
			do {
				baseId++;
			} while (mView.findViewById(baseId) != null);
			
			txtParamArray[i].setId(baseId);
			
			if (i > 0) {
				//txtParamArray[i-1].setNextFocusForwardId(txtParamArray[i].getId());
				txtParamArray[i-1].setNextFocusRightId(txtParamArray[i].getId());
			}
			txtParamArray[i].setFocusableInTouchMode(true);
			registerEditText(txtParamArray[i]);
		}

		dialog.setView(mView);

		dialog.getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}
	
	@Override
	protected int getActionType() {
		return BuilderDialogBase.ACTION_EDIT;
	}

	@Override
	protected boolean checkExpression() {
		boolean retVal;

		//Check validity of all subexpressions
		retVal = true;
		for (int i=0; i<txtParamArray.length && retVal; i++){
			retVal = checkExpression(txtParamArray[i]);
		}

		return retVal;
	}

	@Override
	protected String getExpression() {
		String retVal;

		retVal = fncDesc.getToken() + "(";

		for (int i=0;i<txtParamArray.length;i++){
			if (i > 0) {
				retVal += ",";
			}
			retVal += txtParamArray[i].getText().toString();
		}

		retVal += ")";

		return retVal;
	}


	private BuilderDialogBase.ReadyListener builderReadyListener = new ReadyListener() {
		@Override
		public void ready(View view, boolean confirmed, int action, String diceExpression) {
			if (confirmed) {
				if (action == BuilderDialogBase.ACTION_EDIT) {
					EditText txt;
//					int selStart;
//					int selEnd;
//					String oldDiceExp;
//					String newDiceExp;
//
					txt = (EditText)view.getTag();
//					selStart = txt.getSelectionStart();
//					selEnd = txt.getSelectionEnd();
//					oldDiceExp = txt.getText().toString();
//
//					newDiceExp = oldDiceExp.substring(0, selStart) +
//							diceExpression +
//							oldDiceExp.substring(selEnd);
//
//					txt.setText(newDiceExp);
//					txt.setSelection(selStart, selStart + diceExpression.length());
					Helper.setTextInsideSelection(txt, diceExpression, true);
					txt.requestFocus();
				} else {
					if (checkExpression((EditText)view.getTag())) {
						//The expression is valid
						Toast.makeText(view.getContext(), R.string.lblCheckPassed, Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};

	/**
	 * Check the expression contained in the specified {@link EditText}
	 * @param txt {@link EditText} containing the expression to test.
	 * @return {@code true} id the expression is valid, {@code false} otherwise.
	 */
	protected boolean checkExpression(EditText txt) {
		boolean retVal;
		Dice dExp;

		retVal = true;

		dExp = new Dice();
		dExp.setID(-1);
		dExp.setName("");
		dExp.setDescription("");
		dExp.setExpression(txt.getText().toString());

		try {
			//Make a dummy roll to check for error.
			dExp.setContext(QuickDiceApp.getInstance().getBagManager().getCurrent());
			dExp.getNewResult();
		} catch (DException e) {
			showExpressionError(e, txt);
			retVal = false;
		}
		return retVal;
	}

	protected void showExpressionError(DException e, EditText txt) {
		if (e instanceof DParseException) {
			DParseException ex = (DParseException) e;
			if ((ex.getFromChar() - 1) >= 0 && (ex.getToChar() - 1) < txt.getText().length()) {
				txt.setSelection(ex.getFromChar() - 1, ex.getToChar() - 1);
			}
		}
		txt.requestFocus();

		Helper.showErrorToast(txt.getContext(), e);
	}

	/**
	 * Get an {@link ActionItem} that can be used to populate a QuickAction element.<br />
	 * The {@link ActionItem}, if clicked, open the {@link DiceBuilderDialog} and then
	 * invoke the specified {@link ReadyListener} when the dialog is dismissed.
	 * @param context Context
	 * @param readyListener Listener to be invoked when the dialog is dismissed.
	 * @param functionDescriptor The descriptor of the function for which this {@link ActionItem} is created.
	 * @return An {@link ActionItem}
	 */
	public static ActionItem getActionItem(Context context, PopupMenu parent, ReadyListener readyListener, FunctionDescriptor functionDescriptor) {
		ActionItem retVal;

		retVal = new ActionItem();
		retVal.setTitle(functionDescriptor.getName());
		retVal.setIcon(context.getResources().getDrawable(functionDescriptor.getResId()));
		retVal.setOnClickListener(new FunctionBuilderActionItemClickListener(parent, functionDescriptor, readyListener));

		return retVal;
	}

	protected static class FunctionBuilderActionItemClickListener implements View.OnClickListener {

		PopupMenu parent;
		FunctionDescriptor functionDescriptor;
		ReadyListener readyListener;

		public FunctionBuilderActionItemClickListener(PopupMenu parent, FunctionDescriptor functionDescriptor, ReadyListener readyListener) {
			this.parent = parent;
			this.functionDescriptor = functionDescriptor;
			this.readyListener = readyListener;
		}

		@Override
		public void onClick(View v) {
			View refView = parent != null ? parent.getAnchor() : v;

			new FunctionBuilderDialog(
					refView.getContext(),
					refView,
					functionDescriptor,
					readyListener).show(); //.getDialog().show();

			if (parent != null) {
				parent.dismiss();
			}
		}
	}

}
