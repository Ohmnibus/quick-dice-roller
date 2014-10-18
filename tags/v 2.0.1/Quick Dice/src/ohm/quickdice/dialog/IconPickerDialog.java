package ohm.quickdice.dialog;

import ohm.quickdice.R;
import ohm.quickdice.activity.EditDiceActivity;
import ohm.quickdice.adapter.IconAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class IconPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

	public static final int ICON_UNDEFINED = -1;

	Context context;
	int titleId;
	int defaultIconId;
	OnIconPickedListener readyListener;

	GridView gridView;

	public interface OnIconPickedListener {
		public void onIconPicked(boolean confirmed, int iconId);
	}
	
	public IconPickerDialog(Context context, OnIconPickedListener readyListener) {
		this(context, ICON_UNDEFINED, readyListener);
	}

	public IconPickerDialog(Context context, int defaultIcon, OnIconPickedListener readyListener) {
		this(context, R.string.lblIconPicker, ICON_UNDEFINED, readyListener);
	}

	public IconPickerDialog(Context context, int titleId, int defaultIconId, OnIconPickedListener readyListener) {
		super(context);

		this.context = context;
		this.titleId = titleId;
		this.defaultIconId = defaultIconId;
		this.readyListener = readyListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = getLayoutInflater().inflate(R.layout.icon_picker_dialog, null);

		setView(mView);

		setTitle(this.titleId);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);

		super.onCreate(savedInstanceState);

		gridView = (GridView)findViewById(R.id.ipdIcons);
		gridView.setAdapter(new IconAdapter(context, defaultIconId));
		gridView.setOnItemClickListener(gridItemClickListener);

		getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		int iconId;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			//The dialog has been confirmed

			//iconId = gridView.getSelectedItemPosition();
			//if (iconId == GridView.INVALID_POSITION) {
			//	iconId = ICON_UNDEFINED;
			//}
			iconId = ((IconAdapter)gridView.getAdapter()).getSelected();
		} else {
			iconId = ICON_UNDEFINED;
		}
		if (readyListener != null) {
			readyListener.onIconPicked(which == DialogInterface.BUTTON_POSITIVE, iconId);
		}
		dismiss();
	}

	OnItemClickListener gridItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			IconAdapter myAdapter = (IconAdapter)parent.getAdapter();
			myAdapter.setSelected(position);
			myAdapter.notifyDataSetChanged();
		}
	};
}
