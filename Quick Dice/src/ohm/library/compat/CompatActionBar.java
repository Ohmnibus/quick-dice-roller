package ohm.library.compat;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ohm.library.widget.TabBar;
import ohm.quickdice.R;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

public abstract class CompatActionBar {
	
	public interface TabListener {
		public void onTabSelected(int position, Tab tab);
	}
	
	public static class Tab {
		String text;
//		int position;
//		View view;
		TabListener tabListener;

		public String getText() {
			return text;
		}

		public Tab setText(String text) {
			this.text = text;
			return this;
		}

//		public int getPosition() {
//			return position;
//		}
//
//		protected void setPosition(int position) {
//			this.position = position;
//		}

//		protected View getView() {
//			return view;
//		}
//		
//		protected void setView(View view) {
//			this.view = view;
//		}

		public TabListener getTabListener() {
			return tabListener;
		}

		public Tab setTabListener(TabListener tabListener) {
			this.tabListener = tabListener;
			return this;
		}
	}
	
	public static CompatActionBar createInstance(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new CompatActionBarIceCreamSandwich(activity);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatActionBarHoneycomb(activity);
		} else {
			return new CompatActionBarEclair(activity);
		}
	}

	public CompatActionBar(Activity activity) {
		View tabBar = activity.findViewById(android.R.id.tabhost);
		if (tabBar != null) {
			tabBar.setVisibility(View.GONE);
		}
	}
	
	public abstract void setTitle(int titleId);
	
	public abstract void setTitle(CharSequence title);
	
	public abstract void setDisplayHomeAsUpEnabled(boolean showHomeAsUp);
	
	public abstract void setHomeButtonEnabled(boolean enabled);
	
	public abstract void setTabEnabled(boolean enabled);
	
	public abstract void addTab(Tab tab);
	
	public abstract void setSelectedNavigationItem(int position);
	
	protected void forceOverflowMenu(Context ctx) {
		try {
			ViewConfiguration config = ViewConfiguration.get(ctx);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			Log.w("forceOverflowMenu", "Cannot force Overflow Menu");
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class CompatActionBarEclair extends CompatActionBar implements TabBar.TabListener {

		Activity mActivity;
		TabBar mTabBar = null;
		ArrayList<Tab> mTabList = new ArrayList<Tab>();
		
		public CompatActionBarEclair(Activity activity) {
			super(activity);
			mActivity = activity;
		}

		@Override
		public void setTitle(int titleId) {
			mActivity.setTitle(titleId);
		}

		@Override
		public void setTitle(CharSequence title) {
			mActivity.setTitle(title);
		}

		@Override
		public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
			//NOOP
		}

		@Override
		public void setHomeButtonEnabled(boolean enabled) {
			//NOOP
		}

		@Override
		public void setTabEnabled(boolean enabled) {
			if (enabled) {
				mTabBar = (TabBar)mActivity.findViewById(android.R.id.tabhost);
				mTabBar.removeAllViews();
				mTabBar.setVisibility(View.VISIBLE);
				mTabBar.setTabListener(this);
				for (Tab tab : mTabList) {
					mTabBar.addTab(getNewTab(tab.getText()));
				}
			} else {
				if (mTabBar != null) {
					mTabBar.setVisibility(View.GONE);
					mTabBar = null;
				}
			}
		}

		@Override
		public void addTab(Tab tab) {
			mTabList.add(tab);
			if (mTabBar != null) {
				mTabBar.addTab(getNewTab(tab.getText()));
			}
		}

		private View getNewTab(String name) {
//			TextView tab;
//			
//			tab = new TextView(mActivity, null, android.R.attr.tabWidgetStyle);
//			tab.setText(name);
//			//tab.setBackgroundResource(android.R.drawable.list_selector_background);
//			//tab.setBackgroundResource(android.R.attr.tabWidgetStyle);
//			//tab.setBackgroundResource(android.R.drawable.tab_indicator);
//			tab.setBackgroundResource(R.drawable.bg_tab_selector);
//			//tab.setTextAppearance(mActivity, android.R.style.Widget_TextView_PopupMenu);
//			tab.setTextColor(android.R.attr.textColorPrimaryInverse);
//			tab.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
			
			LayoutInflater inflater = LayoutInflater.from(mActivity);
			View tab = inflater.inflate(R.layout.item_tab, null);
			
			((TextView)tab.findViewById(android.R.id.text1)).setText(name);
			
			return tab;
		}
		
		@Override
		public void setSelectedNavigationItem(int position) {
			if (mTabBar != null) {
				mTabBar.selectTab(position);
			}
		}

		/* **************************** */
		/* TabBar.TabListener Interface */
		/* **************************** */

		@Override
		public void onTabClicked(int i, View tab) {
			Tab myTab = mTabList.get(i);
			TabListener listener = myTab.getTabListener();
			if (listener != null) {
				listener.onTabSelected(i, myTab);
			}
		}
		
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class CompatActionBarHoneycomb extends CompatActionBar implements ActionBar.TabListener {
		
		ActionBar mActionBar;
		int mNavigationModeBackup;
		ArrayList<Tab> mTabList = new ArrayList<Tab>();
		
		public CompatActionBarHoneycomb(Activity activity) {
			super(activity);
			mActionBar = activity.getActionBar();
			mNavigationModeBackup = mActionBar.getNavigationMode();
			forceOverflowMenu(activity);
		}
		
		@Override
		public void setTitle(int titleId) {
			mActionBar.setTitle(titleId);
		}

		@Override
		public void setTitle(CharSequence title) {
			mActionBar.setTitle(title);

		}

		@Override
		public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
			mActionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
		}

		@Override
		public void setHomeButtonEnabled(boolean enabled) {
			//NOOP
		}

		@Override
		public void setTabEnabled(boolean enabled) {
			if (enabled) {
				mNavigationModeBackup = mActionBar.getNavigationMode();
				mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			} else {
				mActionBar.setNavigationMode(mNavigationModeBackup);
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void addTab(Tab tab) {
			mTabList.add(tab);
			ActionBar.Tab newTab = mActionBar.newTab();
			newTab.setText(tab.getText());
			newTab.setTabListener(this);
			mActionBar.addTab(newTab);
		}

		@Override
		public void setSelectedNavigationItem(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		/* ******************************* */
		/* ActionBar.TabListener Interface */
		/* ******************************* */
		@Override
		public void onTabReselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {
			//NOOP
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onTabSelected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {
			int pos = tab.getPosition();
			if (pos != android.app.ActionBar.Tab.INVALID_POSITION) {
				Tab myTab = mTabList.get(pos);
				TabListener listener = myTab.getTabListener();
				if (listener != null) {
					listener.onTabSelected(pos, myTab);
				}
			}
		}

		@Override
		public void onTabUnselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {
			//NOOP
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class CompatActionBarIceCreamSandwich extends CompatActionBarHoneycomb {

		public CompatActionBarIceCreamSandwich(Activity activity) {
			super(activity);
		}

		@Override
		public void setHomeButtonEnabled(boolean enabled) {
			mActionBar.setHomeButtonEnabled(enabled);
		}
	}
}
