package ohm.quickdice.dialog;

import ohm.library.compat.CompatMisc;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;

public class MenuDialog extends AlertDialog implements DialogInterface.OnClickListener, MenuAdapter.OnItemClickListener {

	private Activity activity;
	private MenuAdapter adapter;

	public MenuDialog(Activity activity, Menu menu) {
		super(activity);
		this.activity = activity;
		//Adapter must be initialized now because menu
		//could be cleared by caller to avoid system context menu
		this.adapter = new MenuAdapter(activity, menu);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//TODO: Inflate view from resource so that can be easily styled
		//TODO: When creating such layout, add the divider as well, avoiding to do it here (see "Add divider").
		ListView root = new ListView(activity);
		root.setLayoutParams(new LayoutParams( //LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				CompatMisc.getInstance().LAYOUT_MATCH_PARENT,
				CompatMisc.getInstance().LAYOUT_MATCH_PARENT));
		root.setCacheColorHint(Color.TRANSPARENT);
		
		//Get the drawable for the divider.
		//Don't know any better way to do it.
		Drawable divider;
		TypedArray ta;
		ta = activity.getApplicationContext().obtainStyledAttributes(new int[] { android.R.attr.listDivider });
		divider = ta.getDrawable(0);
		ta.recycle();
		root.setDivider(divider);
		
		setView(root);
		
		setButton(BUTTON_POSITIVE, activity.getText(R.string.lblCancel), this);
		
		super.onCreate(savedInstanceState);
		
		View headerView = getHeaderView(getLayoutInflater(), root);
		if (headerView != null) {
			root.addHeaderView(headerView, null, false);
			
			//Add divider
			View div = new ImageView(activity);

			div.setLayoutParams(new android.widget.AbsListView.LayoutParams(
					root.getLayoutParams().width, // LayoutParams.FILL_PARENT,
					root.getDividerHeight()));
			
			ta = activity.getApplicationContext().obtainStyledAttributes(new int[] { android.R.attr.listDivider });
			divider = ta.getDrawable(0);
			ta.recycle();

			CompatMisc.getInstance().setBackgroundDrawable(div, divider);
			root.addHeaderView(div, null, false);
		} else {
			//Remove divider if list has been inflated
		}
		
		root.setAdapter(adapter);
		
		onPrepareOptionsMenu(adapter);
		//TODO: Handle the case onPrepareOptionsMenu return "false".
		//- If headerView is null, dismiss
		//- If headerView is not null, show with no menu options.
		
		adapter.pack();
		adapter.setOnItemClickListener(this);
	}
	
	/**
	 * Get the fixed view to appear at the top of the menu.<br />
	 * The view will be added as non clickable. To override this behavior
	 * manually add the view to {@code parent} specifying your parameters 
	 * and then return {@code null}.
	 * @param inflater Inflater that can be used to inflate the view.
	 * @param parent ListView on which the view will be added.
	 * @return View to be added to the ListView, or {@code null}.
	 */
	protected View getHeaderView(LayoutInflater inflater, ListView parent) {
		return null;
	}
	
	/**
	 * Prepare the menu to be displayed.<br />
	 * This is called right before the menu is shown. You can use this method 
	 * to efficiently enable/disable items or otherwise dynamically modify the contents.<br />
	 * @param adapter The adapter containing the menu to show.
	 * @return You must return true for the menu to be displayed; if you return false it will not be shown.
	 */
	protected boolean onPrepareOptionsMenu(MenuAdapter adapter) {
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}

	@Override
	public void onItemClick(MenuAdapter parent, View view, int row, int column, long id) {
		MenuItem selected = parent.getItem(row, column);
		activity.onContextItemSelected(selected);
		dismiss();
	}
}
