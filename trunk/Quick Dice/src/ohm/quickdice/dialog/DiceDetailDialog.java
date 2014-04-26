package ohm.quickdice.dialog;

import ohm.dexp.DExpression;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DiceDetailDialog extends AlertDialog implements DialogInterface.OnClickListener {

	DExpression expression;
		
	public DiceDetailDialog(Context context, DExpression expression) {
		super(context);
		this.expression = expression;
	}

	/* (non-Javadoc)
	 * @see android.app.AlertDialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		View mView = getLayoutInflater().inflate(R.layout.dice_detail_dialog, null);
		
		setView(mView);
		
		setTitle(expression.getName());
		
		setIcon(getDialogIcon());
		
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		
		super.onCreate(savedInstanceState);
		
		((TextView)findViewById(R.id.ddName)).setText(expression.getName());
		((TextView)findViewById(R.id.ddDescription)).setText(expression.getDescription());
		((TextView)findViewById(R.id.ddExpresson)).setText(expression.getExpression());
		try {
			long min = expression.getMinResult() / TokenBase.VALUES_PRECISION_FACTOR;
			long max = expression.getMaxResult() / TokenBase.VALUES_PRECISION_FACTOR;
			long range = max - min + 1;
//			((TextView)findViewById(R.id.ddMinResult)).setText(Long.toString(min));
//			((TextView)findViewById(R.id.ddMaxResult)).setText(Long.toString(max));
//			((TextView)findViewById(R.id.ddRange)).setText(Long.toString(range));
			((TextView)findViewById(R.id.ddRange)).setText(
					Long.toString(min) + " - " +
					Long.toString(max) + " (" +
					Long.toString(range) + ")");
		} catch (DException e) {
//			((TextView)findViewById(R.id.ddMinResult)).setText(R.string.lblCannotEvaluate);
//			((TextView)findViewById(R.id.ddMaxResult)).setText(R.string.lblCannotEvaluate);
			((TextView)findViewById(R.id.ddRange)).setText(R.string.lblCannotEvaluate);
		}
	}
	
	protected Drawable getDialogIcon() {
//		DisplayMetrics metrics = new DisplayMetrics();
//		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		Drawable diceIcon = QuickDiceApp.getInstance().getDiceIcon(expression.getResourceIndex());
//		return Graphic.resizeDrawable(diceIcon, 32, 32, metrics);
//		Graphic graphicManager = new Graphic(this.getContext().getResources());
//		Drawable diceIcon = QuickDiceApp.getInstance().getDiceIcon(expression.getResourceIndex());
//		return graphicManager.resizeDrawable(diceIcon, 32, 32);
		return QuickDiceApp.getInstance().getGraphic().getResizedDiceIcon(
				expression.getResourceIndex(), 32, 32);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}

}
