package ohm.quickdice.adapter;

import java.util.List;

import ohm.quickdice.R;
import ohm.quickdice.entity.MostRecentFile;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class MostRecentFilesAdapter extends ArrayAdapter<MostRecentFile> {

	Context context;
	int rowResource;
	List<MostRecentFile> mostRecentFile;

	java.text.DateFormat dateFormat;
	java.text.DateFormat timeFormat;

	public MostRecentFilesAdapter(Context context, int resource, List<MostRecentFile> objects) {
		super(context, resource, objects);

		this.context = context;
		this.rowResource = resource;
		this.mostRecentFile = objects;

		dateFormat = DateFormat.getMediumDateFormat(this.context);
		timeFormat = DateFormat.getTimeFormat(this.context);
	}

	@Override
	public MostRecentFile getItem(int position) {
		return mostRecentFile.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		MostRecentFile item;
		TextView txt;

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(rowResource, null);
		}

		item = mostRecentFile.get(position);

		txt = (TextView)view.findViewById(R.id.ieiName);
		txt.setText(item.getName());

		txt = (TextView)view.findViewById(R.id.ieiDescription);
		txt.setText(String.format(
				context.getString(R.string.lblItemMRU),
				item.getBagsNum(),
				item.getDiceNum(),
				item.getModsNum(),
				dateFormat.format(item.getLastUsed()),
				timeFormat.format(item.getLastUsed()),
				item.getVarsNum()));

		return view;
	}
}
