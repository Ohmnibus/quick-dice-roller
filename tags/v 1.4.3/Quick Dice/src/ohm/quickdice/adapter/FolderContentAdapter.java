package ohm.quickdice.adapter;

import java.util.List;

import ohm.quickdice.R;
import ohm.quickdice.entity.FolderItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FolderContentAdapter extends ArrayAdapter<FolderItem> {
	
	Context context;
	int rowResource;
	List<FolderItem> folderContent;
	
	public FolderContentAdapter(Context context, int rowResource, List<FolderItem> folderContent) {
		super(context, rowResource, folderContent);
		this.context = context;
		this.rowResource = rowResource;
		this.folderContent = folderContent;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public FolderItem getItem(int position) {
		return folderContent.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        FolderItem item;
        ImageView img;
        TextView txt;
        
        if (view == null) {
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        view = inflater.inflate(rowResource, null);
        }
        
        item = folderContent.get(position);
        
        img = (ImageView)view.findViewById(R.id.fpiImage);
        if (item.getType() == FolderItem.TYPE_FILE) {
        	if (item.getName().endsWith(".qdr.json")) {
        		img.setImageResource(R.drawable.ic_d6_blue);
        	} else {
        		img.setImageResource(R.drawable.ic_fs_document);
        	}
        } else {
        	img.setImageResource(R.drawable.ic_fs_folder);
        }
        
        txt = (TextView)view.findViewById(R.id.fpiName);
        txt.setText(item.getName());
        
        txt = (TextView)view.findViewById(R.id.fpiDescription);
        txt.setText(item.getDescription());

        return view;
     }
	
	
}
