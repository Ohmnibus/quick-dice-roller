package ohm.quickdice.dialog;

import java.lang.ref.WeakReference;

import ohm.library.compat.CompatMisc;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import ohm.quickdice.entity.Icon;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

public class MenuDialog extends AlertDialog implements DialogInterface.OnClickListener, MenuAdapter.OnItemClickListener, DialogInterface.OnDismissListener {

	private Activity activity;
	private MenuAdapter adapter;
	private OnDismissListener dismissListener = null;

	public MenuDialog(Activity activity, Menu menu) {
		super(activity);
		this.activity = activity;
		//Adapter must be initialized now because menu
		//could be cleared by caller to avoid system context menu
		this.adapter = new MenuAdapter(activity, menu);
		super.setOnDismissListener(this);
	}
	
	@Override
	public void setOnDismissListener(OnDismissListener listener) {
		//super.setOnDismissListener(listener);
		dismissListener = listener;
	}
	
//	protected Menu newMenuInstance(Activity activity) {
//		Menu retVal = null;
//		try {
//			Class<?> menuBuilderClass = Class.forName("com.android.internal.view.menu.MenuBuilder");
//
//			Constructor<?> constructor = menuBuilderClass.getDeclaredConstructor(Context.class);
//
//			retVal = (Menu) constructor.newInstance(activity);
//
//		} catch (Exception e) {e.printStackTrace();}
//
//		return retVal;
//	}
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View root = activity.getLayoutInflater().inflate(R.layout.dialog_menu, null);
		ListView list = (ListView)root.findViewById(R.id.lvMenu);
		
		setView(list);
		
		setButton(BUTTON_POSITIVE, activity.getText(R.string.lblDone), this);
		
		super.onCreate(savedInstanceState);
		
		View headerView = getHeaderView(getLayoutInflater(), list);
		if (headerView != null) {
			list.addHeaderView(headerView, null, false);
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				//Version prior to API19 need an explicit divider
				View div = new ImageView(activity);
	
				div.setLayoutParams(new android.widget.AbsListView.LayoutParams(
						list.getLayoutParams().width, // LayoutParams.FILL_PARENT,
						activity.getResources().getDimensionPixelSize(R.dimen.divider_thickness))); //list.getDividerHeight()
	
				CompatMisc.getInstance().setBackgroundDrawable(div, list.getDivider().getConstantState().newDrawable());
				list.addHeaderView(div, null, false);
			}
		}
		
		list.setAdapter(adapter);
		
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

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (dismissListener != null) {
			dismissListener.onDismiss(dialog);
		}
	}
	
	public void setIcon(Icon icon) {
		if (icon != null) {
//			setIcon(QuickDiceApp.getInstance().getBagManager().getIconDrawable(
//						icon.getId(), 32, 32));
			new AsyncResizer(this, icon.getId()).load();
		}
	}
	
	private static class AsyncResizer extends AsyncTask<Integer, Void, Drawable> {
		private final WeakReference<AlertDialog> viewReference;
		private final int iconId;

		public AsyncResizer(AlertDialog alertDialog, int iconId) {
			// Use a WeakReference to ensure the AlertDialog can be garbage collected
			this.viewReference = new WeakReference<AlertDialog>(alertDialog);
			this.iconId = iconId;
		}

		// Decode image in background.
		@Override
		protected Drawable doInBackground(Integer... params) {
			Drawable retVal = null;

			if (viewReference != null) {
				retVal = QuickDiceApp.getInstance().getBagManager().getIconDrawable(
						params[0],
						R.dimen.header_icon_size,
						R.dimen.header_icon_size);
			}
			
			return retVal;
		}

		// Once complete, see if ImageView is still around and set drawable.
		@Override
		protected void onPostExecute(Drawable drawable) {
			if (isCancelled()) {
				drawable = null;
			}
			if (viewReference != null && drawable != null) {
				final AlertDialog alertDialog = viewReference.get();
				alertDialog.setIcon(drawable);
			}
		}
		
		public void load() {
			execute(iconId);
		}
	}
}
