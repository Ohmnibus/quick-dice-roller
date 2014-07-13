package ohm.quickdice.dialog;

import ohm.quickdice.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

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

		title = String.format(res.getString(R.string.msgAboutTitle),
				res.getString(R.string.app_name));

		body = String.format(res.getString(R.string.msgAboutBody),
				res.getString(R.string.app_name),
				version,
				res.getString(R.string.app_author));

		//new MarkupDialog(context, title, body, R.drawable.ic_launcher, null).show();
		MarkupDialog dlg = new MarkupDialog(context, title, body, R.drawable.ic_launcher, null);
		dlg.setButton(MarkupDialog.BUTTON_NEUTRAL, res.getString(R.string.msgWhatsNew), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MarkupDialog dlg = (MarkupDialog)dialog;
				ShowWhatsNew(dlg.getContext());
				dlg.onClick(dialog, which);
			}
		});
		dlg.show();
	}

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

		title = String.format(res.getString(R.string.msgWhatsNewTitle),
				res.getString(R.string.app_name),
				version);

		body = String.format(res.getString(R.string.msgWhatsNewBody),
				res.getString(R.string.app_name),
				version,
				res.getString(R.string.app_author));

		new MarkupDialog(context, title, body, -1, null).show();
	}
}
