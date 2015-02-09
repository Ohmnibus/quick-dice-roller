package ohm.quickdice.dialog;

import ohm.quickdice.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogHelper {

	public static void ShowAbout(Context context) {
		Resources res;
		String title;
		String body;
		String version;

		res = context.getResources();

		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "1.0.0";
		}

		title = res.getString(R.string.msgAboutTitle,
				res.getString(R.string.app_name));

		body = res.getString(R.string.msgAboutBody,
				res.getString(R.string.app_name),
				version,
				res.getString(R.string.app_author),
				res.getString(R.string.app_translators));

		MarkupDialog dlg = new MarkupDialog(context, title, body, R.drawable.ic_launcher, null);
		
		dlg.setButton(MarkupDialog.BUTTON_POSITIVE, res.getString(R.string.msgWhatsNew), onAboutClickListener);
		dlg.setButton(MarkupDialog.BUTTON_NEUTRAL, res.getString(R.string.msgLicenses), onAboutClickListener);
		
		dlg.show();

		addItem(
				R.drawable.ic_donate_small,
				R.string.lbl_donate_title_small,
				R.string.lbl_donate_url_small,
				dlg);
		
		addItem(
				R.drawable.ic_donate_medium,
				R.string.lbl_donate_title_medium,
				R.string.lbl_donate_url_medium,
				dlg);
		
		addItem(
				R.drawable.ic_donate_large,
				R.string.lbl_donate_title_large,
				R.string.lbl_donate_url_large,
				dlg);
	}
	
	protected static DialogInterface.OnClickListener onAboutClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			MarkupDialog dlg = (MarkupDialog)dialog;
			switch (which) {
				case MarkupDialog.BUTTON_POSITIVE:
					ShowWhatsNew(dlg.getContext());
					break;
				case MarkupDialog.BUTTON_NEUTRAL:
					ShowLicenses(dlg.getContext());
					break;
			}
			dlg.onClick(dialog, which);
		}
	};
	
	protected static void addItem(int iconResId, int titleResId, int urlResId, MarkupDialog dlg) {
		Context ctx = dlg.getContext();
		ViewGroup root = (ViewGroup)dlg.findViewById(R.id.grpContainer);
		View v = dlg.getLayoutInflater().inflate(R.layout.item_donate, root, false);
		
		ImageView img = (ImageView)v.findViewById(R.id.imgIcon);
		img.setImageResource(iconResId);
		img.setContentDescription(ctx.getText(titleResId));
		
		TextView txt = (TextView)v.findViewById(R.id.lblText);
		txt.setText(titleResId);
		
		View cmd = v.findViewById(R.id.grpButton);
		cmd.setTag(ctx.getString(urlResId));
		
		cmd.setOnClickListener(onDonateClickListener);
		
		root.addView(v);
	}
	
	protected static OnClickListener onDonateClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse((String) v.getTag()));
			v.getContext().startActivity(i);
		}
	};

	public static void ShowWhatsNew(Context context) {
		Resources res;
		String title;
		String body;
		String version;

		res = context.getResources();

		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "1.0.0";
		}

		title = res.getString(R.string.msgWhatsNewTitle,
				res.getString(R.string.app_name),
				version);

//		body = String.format(res.getString(R.string.msgWhatsNewBody),
//				res.getString(R.string.app_name),
//				version,
//				res.getString(R.string.app_author));

		body = res.getString(R.string.msgWhatsNewIntro) +
				res.getString(R.string.msgWhatsNewBody);
		
		new MarkupDialog(context, title, body, -1, null).show();
	}
	
	public static void ShowLicenses(Context context) {
		Resources res;
		String title;
		String body;

		res = context.getResources();
		
		String[] libUrls = res.getStringArray(R.array.lblLicenseLibURLs);
		String[] libNames = res.getStringArray(R.array.lblLicenseLibNames);
		String[] authors = res.getStringArray(R.array.lblLicenseLibAuthors);
		String[] licNames = res.getStringArray(R.array.lblLicenseNames);
		String[] licUrls = res.getStringArray(R.array.lblLicenseURLs);

		title = res.getString(R.string.msgLicenses);
		
		body = "";
		for (int i = 0; i < licUrls.length; i++) {
			body = body + res.getString(R.string.lblLicense,
					libUrls[i],
					libNames[i],
					authors[i],
					licUrls[i],
					licNames[i]);
		}

		new MarkupDialog(context, title, body, -1, null).show();
	}
}
