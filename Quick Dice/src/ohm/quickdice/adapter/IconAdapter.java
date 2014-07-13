package ohm.quickdice.adapter;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class IconAdapter extends BaseAdapter {
	int selected;
	private Context ctx;
	private QuickDiceApp app;
	private DisplayMetrics metrics;
	private GridView.LayoutParams layoutParams;
	
	public IconAdapter(Context c, int selected) {
		ctx = c;
		app = (QuickDiceApp)ctx.getApplicationContext();
		metrics = new DisplayMetrics();
		((Activity)ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		layoutParams = new GridView.LayoutParams(
				(int)(64 * metrics.density),
				(int)(64 * metrics.density)); 
		this.selected = selected;
	}
	
	@Override
	public int getCount() {
		return app.getGraphic().getDiceIconCount();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ImageView img;
        
        if (convertView == null) {
        	img = new ImageView(ctx);
	        img.setLayoutParams(layoutParams);
	        img.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
        	img = (ImageView)convertView;
        }
        
        setImageResource(img, position);
        
        return img;
	}
	
	private void setImageResource(ImageView img, int position) {
        img.setImageDrawable(app.getGraphic().getDiceIcon(position));
//        Drawable bg = img.getBackground();
//        if (bg != null) {
//        	bg.setColorFilter(app.getDiceColor(position), Mode.SRC);
//        }else {
//			bg = ctx.getResources().getDrawable(R.drawable.gallery_item_border).mutate();
//			bg.setColorFilter(app.getDiceColor(position), Mode.SRC);
//			img.setBackgroundDrawable(bg);
//        }
        if (position == selected) {
        	img.setBackgroundResource(R.drawable.bg_last_result_focus);
        } else {
        	//img.setBackgroundDrawable(null);
        	img.setBackgroundResource(0);
        }
	}
	
	public void setSelected(int position) {
		selected = position;
	}
	
	public int getSelected() {
		return selected;
	}
}
