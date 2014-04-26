package ohm.quickdice.dialog;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.GraphicManager;
import ohm.quickdice.entity.RollResult;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RollDetailDialog extends AlertDialog implements DialogInterface.OnClickListener {

	RollResult[] result;
	GraphicManager graphicManager;
	
	public RollDetailDialog(Context context, RollResult[] result) {
		super(context);
		this.result = result;
		//this.graphicManager = new Graphic(context.getResources());
		this.graphicManager = QuickDiceApp.getInstance().getGraphic();
	}
	
	/* (non-Javadoc)
	 * @see android.app.AlertDialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		View mView = getLayoutInflater().inflate(R.layout.roll_detail_dialog, null);

		setView(mView);

		RollResult res = RollResult.mergeResultList(result);

		if (res == null) {
			res = new RollResult("", "", "", 0, 0, 0, RollResult.DEFAULT_RESULT_ICON);
		}
		setTitle(res.getName());
		
		setIcon(getDialogIcon(res));
		
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		
		super.onCreate(savedInstanceState);
		
		TextView txt;
		((TextView)findViewById(R.id.rdName)).setText(res.getName());
		((TextView)findViewById(R.id.rdDescription)).setText(res.getDescription());
		//((TextView)findViewById(R.id.rdExpression)).setText(res..getEDescription());
		((TextView)findViewById(R.id.rdResultText)).setText(res.getResultText());
		txt = (TextView)findViewById(R.id.rdResultValue);
		txt.setText(Long.toString(res.getResultValue()));

		Drawable resultIcon = graphicManager.resizeDrawable(res.getResultIconID(), 24, 24);
		txt.setCompoundDrawablesWithIntrinsicBounds(null, null, resultIcon, null);
		((TextView)findViewById(R.id.rdRange)).setText(
				Long.toString(res.getMinResultValue()) + " - " +
				Long.toString(res.getMaxResultValue()) + " (" +
				Long.toString(res.getMaxResultValue() - res.getMinResultValue() + 1) + ")");
	}
	
	protected Drawable getDialogIcon(RollResult res) {
//		Graphic graphicManager = new Graphic(this.getContext().getResources());
//		Drawable diceIcon = QuickDiceApp.getInstance().getDiceIcon(res.getResourceIndex());
//		return graphicManager.resizeDrawable(diceIcon, 32, 32);
		return graphicManager.getResizedDiceIcon(res.getResourceIndex(), 32, 32);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}
}
