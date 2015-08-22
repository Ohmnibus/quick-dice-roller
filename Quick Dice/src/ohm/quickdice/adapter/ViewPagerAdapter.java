package ohm.quickdice.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerAdapter extends PagerAdapter {

	View[] views;
	
	public ViewPagerAdapter(View[] views) {
		this.views = views;
	}

	@Override
	public int getCount() {
		return views.length;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = views[position];
		//container.addView(view);
		return view;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		//super.destroyItem(container, position, object);
		View view = views[position];
		container.removeView(view);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

}
