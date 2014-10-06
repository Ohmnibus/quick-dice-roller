package ohm.quickdice.adapter;

import java.util.ArrayList;

import ohm.quickdice.R;
import android.app.LauncherActivity.ListItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Adapter to handle menu in two columns list.
 * @author Ohmnibus
 *
 */
public class MenuAdapter extends BaseAdapter implements OnClickListener, OnLongClickListener {

	/**
	 * Identifies left column menu item.
	 */
	public static final int LEFT_ITEM = 0;
	/**
	 * Identifies right column menu item.
	 */
	public static final int RIGHT_ITEM = 1;

	private Menu mMenu;
	private LayoutInflater mInflater;
	private OnItemClickListener mItemClickListener = null;

	/**
	 * Handler for the menu item selection. 
	 * @author Ohmnibus
	 *
	 */
	public interface OnItemClickListener {
		/**
		 * This method will be invoked when a menu option is selected.
		 * @param parent Adapter containing the menu option.
		 * @param view The view that received the click.
		 * @param row The row of the item in the adapter.
		 * @param column The column of the item in the adapter.
		 * @param id The selected menu option id.
		 */
		void onItemClick(MenuAdapter parent, View view, int row, int column, long id);
	}
	
	/**
	 * This class hold a copy of the adapted menu.<br />
	 * This use a (very) simplified version of the {@link android.view.Menu} interface. 
	 * @author Ohmnibus
	 *
	 */
	protected class Menu {
		MenuItem[] items;
		
		/**
		 * Initialize this instance with the given menu.
		 * @param menu Menu to handle.
		 */
		public Menu (android.view.Menu menu) {
			if (menu != null) {
				items = new MenuItem[menu.size()];
			} else {
				items = new MenuItem[0];
			}
			for (int i = 0; i < items.length; i++) {
				items[i] = menu.getItem(i);
			}
		}
		
		/**
		 * Get the number of items in the menu.<br />
		 * Note that this will change any times items are added 
		 * or removed from the menu.
		 * @return The item count.
		 */
		public int size() {
			return items.length;
		}
		
		/**
		 * Return the menu item with a particular identifier.
		 * @param id The identifier to find.
		 * @return The menu item object, or null if there is no item with this identifier.
		 */
		public MenuItem findItem(int id) {
			for (int i = 0; i < items.length; i++) {
				if (items[i].getItemId() == id) {
					return items[i];
				}
			}
			return null;
		}
		
		/**
		 * Gets the menu item at the given index.
		 * @param index The index of the menu item to return.
		 * @return The menu item.
		 */
		public MenuItem getItem(int index) {
			return items[index];
		}

		/**
		 * Remove from the menu all the items marked as not visible.
		 */
		public void pack() {
			ArrayList<MenuItem> newList = new ArrayList<MenuItem>(items.length);
			for (int i = 0; i < items.length; i++) {
				if (items[i].isVisible()) {
					newList.add(items[i]);
				}
			}
			if (newList.size() != items.length) {
				items = new MenuItem[newList.size()];
				for (int i = 0; i < items.length; i++) {
					items[i] = newList.get(i);
				}
			}
		}
	}
	
	/**
	 * Define a reference for a menu item.
	 * @author Ohmnibus
	 *
	 */
	private class OptionReferences {
		public int row;
		public int column;
		public long id;

		public OptionReferences(int row, int column, long id) {
			this.row = row;
			this.column = column;
			this.id = id;
		}
	}

	/**
	 * Initialize the adapter.
	 * @param context Context to use as reference.
	 * @param menu Menu to be adapted. It will be immediately mapped to an
	 * internal object so that it can be safely cleared by the caller with
	 * {@code menu.clear()}
	 */
	public MenuAdapter(Context context, android.view.Menu menu) {
		this.mMenu = new Menu(menu);
		this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * Set the listener to handle a menu item selection.
	 * @param listener Menu item selection listener.
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		mItemClickListener = listener;
	}

	@Override
	public int getCount() {
		return (mMenu.size() + 1) / 2; //+1 to round up
	}

	/**
	 * Return the menu item with a particular identifier.
	 * @param id The identifier to find.
	 * @return The menu item object, or null if there is no item with this identifier.
	 */
	public MenuItem findItem(int id) {
		return mMenu.findItem(id);
	}

	@Override
	public MenuItem getItem(int position) {
		return mMenu.getItem(position * 2);
	}

	/**
	 * Return the item at given position.
	 * @param row Item row. This value is the same as the {@link ListItem}'s position.
	 * @param column Item column. Can be either {@link #LEFT_ITEM} or {@link #RIGHT_ITEM}.
	 * @return The item at given position, or {@code null} if not exists.
	 */
	public MenuItem getItem(int row, int column) {
		int position = (row * 2) + column;
		if (position >= mMenu.size()) {
			return null;
		}
		return mMenu.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mMenu.getItem(position * 2).getItemId();
	}

	/**
	 * Get the row id associated with the specified position in the list.
	 * @param row Item row. This value is the same as the {@link ListItem}'s position.
	 * @param column Item column. Can be either {@link #LEFT_ITEM} or {@link #RIGHT_ITEM}.
	 * @return The id of the item at the specified position.
	 */
	public long getItemId(int row, int column) {
		int position = (row * 2) + column;
		if (position >= mMenu.size()) {
			return 0;
		}
		return mMenu.getItem(position).getItemId();
	}

	/**
	 * Remove from the menu all the items marked invisible.<bt />
	 * A call to this method is required in order to avoid empty list elements.
	 */
	public void pack() {
		mMenu.pack();
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Get this row's options
		MenuItem[] item = new MenuItem[] {
			getItem(position, LEFT_ITEM),
			getItem(position, RIGHT_ITEM)
		};

		//Inflate the view
		View v = this.mInflater.inflate(R.layout.double_options_item, parent, false);

		//Get the view's elements
		TextView[] txt = new TextView[] {
				(TextView)v.findViewById(R.id.left_item_text),
				(TextView)v.findViewById(R.id.right_item_text)
		};

		//Set options to view's elements
		for (int i = 0; i < txt.length; i++) {
			if (item[i] != null) {
				txt[i].setText(item[i].getTitle());
				txt[i].setEnabled(item[i].isEnabled());
				txt[i].setVisibility(item[i].isVisible() ? View.VISIBLE : View.INVISIBLE);
				txt[i].setOnClickListener(this);
				txt[i].setOnLongClickListener(this);
				txt[i].setTag(new OptionReferences(position, i, item[i].getItemId()));
			} else {
				txt[i].setVisibility(View.INVISIBLE);
				txt[i].setClickable(false);
				txt[i].setOnClickListener(null);
				txt[i].setOnLongClickListener(null);
			}
		}

		return v;
	}

	@Override
	public void onClick(View v) {
		if (mItemClickListener != null) {
			OptionReferences ref = (OptionReferences)v.getTag();
			mItemClickListener.onItemClick(
					this,
					v,
					ref.row,
					ref.column,
					ref.id);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		OptionReferences ref = (OptionReferences)v.getTag();
		MenuItem item = getItem(ref.row, ref.column);
		Toast.makeText(
				this.mInflater.getContext(),
				item.getTitle(),
				Toast.LENGTH_LONG).show();
		return true;
	}
}