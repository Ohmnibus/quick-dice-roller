package ohm.library.compat;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Parcelable;

public abstract class CompatIntent {

	private static CompatIntent instance = null;
	
	private static CompatIntent getInstance() {
		if (instance == null) {
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.DONUT) {
				instance = new CompatIntentDonut();
			} else {
				instance = new CompatIntentEclaire();
			}
		}
		return instance;
	}

	/**
	 * Interface definition for a callback to be invoked when a {@link ResolveInfo} is evaluated.
	 * @author Ohmnibus
	 *
	 */
	public interface OnEvalResolveInfoListener {
		/**
		 * Callback to be invoked when a {@link ResolveInfo} is evaluated
		 * @param resolveInfo {@link ResolveInfo} to evaluate.
		 * @param target Reference {@link Intent} for this {@link ResolveInfo}.
		 * @return Should be {@code resolveInfo} if the evaluation is successful, 
		 * or {@code null} to discard this {@link ResolveInfo}.
		 */
		public ResolveInfo onEvalResolveInfo(ResolveInfo resolveInfo, Intent target);
	}
	
	/**
	 * Convenience function for creating a {@code ACTION_CHOOSER} Intent.
	 * @param target The Intent that the user will be selecting an activity to perform.
	 * @param title Optional title that will be displayed in the chooser.
	 * @param extra Optional Intent to add to the chooser.
	 * @return Return a new Intent object that you can hand to {@code Context.startActivity()} and related methods. 
	 */
	public static Intent createChooser(Context context, Intent target, CharSequence title, Intent extra, OnEvalResolveInfoListener listener) {
		return createChooser(context, target, title, new Intent[] {extra}, listener);
	}

	/**
	 * Convenience function for creating a {@code ACTION_CHOOSER} Intent.
	 * @param target The Intent that the user will be selecting an activity to perform.
	 * @param title Optional title that will be displayed in the chooser.
	 * @param extraList Optional list of intent to add to the chooser.
	 * @return Return a new Intent object that you can hand to {@code Context.startActivity()} and related methods.
	 */
	public static Intent createChooser(Context context, Intent target, CharSequence title, Intent[] extraList, OnEvalResolveInfoListener listener) {
		return getInstance().innerCreateChooser(context, target, title, extraList, listener);
	}
	
	protected abstract Intent innerCreateChooser(Context context, Intent target, CharSequence title, Intent[] extraList, OnEvalResolveInfoListener listener);
	
	//@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.DONUT)
	private static class CompatIntentDonut extends CompatIntent {

		@Override
		protected Intent innerCreateChooser(Context context, Intent target, CharSequence title, Intent[] extraList, OnEvalResolveInfoListener listener) {
			Intent chooser;
			//Intent.EXTRA_INITIAL_INTENTS does not work on Donut.
			if (extraList != null && extraList.length > 0) {
				//If extra intents were provided, choose the first.
				chooser = extraList[0];
			} else {
				//If extra intents were not provided, create a standard chooser.
				chooser = Intent.createChooser(target, title);
			}
			return chooser;
		}
		
	}
	
	//@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class CompatIntentEclaire extends CompatIntent {

		@Override
		protected Intent innerCreateChooser(Context context, Intent target, CharSequence title, Intent[] extraList, OnEvalResolveInfoListener listener) {

			List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(target, 0);
			
			List<Intent> chooserOptionList = new ArrayList<Intent>();
			
			for (ResolveInfo resolveInfo : resInfo) {
				if (listener != null) {
					resolveInfo = listener.onEvalResolveInfo(resolveInfo, target);
				}
				if (resolveInfo != null) {
					String name = resolveInfo.activityInfo.name;
					String packageName = resolveInfo.activityInfo.packageName;
	
					Intent chooserOption = new Intent(target);
					chooserOption.setPackage(packageName);
					chooserOption.setComponent(new ComponentName(packageName, name));
					
					chooserOptionList.add(chooserOption);
				} //else the item was filtered
			}
			
			if (extraList != null) {
				for (Intent extra : extraList) {
					chooserOptionList.add(extra);
				}
			}
			
			Intent retVal;
			if (chooserOptionList.size() == 0) {
				retVal = Intent.createChooser(target, title);
			} else if (chooserOptionList.size() == 1) {
				retVal = chooserOptionList.get(0);
			} else {
				retVal = Intent.createChooser(chooserOptionList.remove(0), title);
				retVal.putExtra(Intent.EXTRA_INITIAL_INTENTS, chooserOptionList.toArray(new Parcelable[]{}));
			}
			
			return retVal;
		}
		
	}
}
