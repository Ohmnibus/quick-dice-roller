package ohm.library.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class TabBar extends LinearLayout {
	public interface TabListener {
		void onTabClicked(int i, View tab);
	}

	private List<View> tabs = new ArrayList<View>();
	private int selectedIndex;
	private TabListener tabListener;

//	public TabBar(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//	}

	public TabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TabBar(Context context) {
		super(context);
	}
	

	public void addTab(final View view) {

		final int position = tabs.size();
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tabListener != null) {
					tabListener.onTabClicked(position, view);
				}
			}
		});
		view.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
		view.setClickable(true);
		view.setFocusable(true);
		tabs.add(view);
		addView(view);
	}

	public void setTabListener(TabListener listener) {
		tabListener = listener;
	}

	private void unselectAll() {
		for (View tab : tabs) {
			tab.setSelected(false);
		}
	}

	public void selectTab(int i) {
		View v = tabs.get(i);
		unselectAll();
		v.setSelected(true);
		selectedIndex = i;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

}
