package ohm.quickdice.dialog;

import ohm.quickdice.R;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.WheelViewAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class ModifierBuilderDialog extends AlertDialog implements OnClickListener {
	
	public static final int POSITION_UNDEFINED = -1;
	
	int position;
	ReadyListener readyListener;
	
	WheelView signWheel;
	WheelView tensWheel;
	WheelView unitWheel;
	
	public interface ReadyListener {
        public void ready(boolean confirmed, int modifier, int position);
    }

	/**
	 * Initialize a builder with given parameters.
	 * @param context Context
	 * @param readyListener Callback listener.
	 */
	public ModifierBuilderDialog(Context context, ReadyListener readyListener) {
		this(context, POSITION_UNDEFINED, readyListener);
	}

	/**
	 * Initialize a builder with given parameters.
	 * @param context Context
	 * @param position Position on which the item should be added. This value is used by the caller.
	 * @param readyListener Callback listener.
	 */
	public ModifierBuilderDialog(Context context, int position, ReadyListener readyListener) {
		super(context);

		this.position = position;
		this.readyListener = readyListener;
	}

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        View mView = getLayoutInflater().inflate(R.layout.modifier_builder_dialog, null);
        
        setView(mView);
		
        setTitle(R.string.lblModifierBuilder);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
        setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
        
        super.onCreate(savedInstanceState);

        signWheel = initWheel(R.id.mbModSign, 0, new ArrayWheelAdapter<String>(getContext(), new String[] {"+", "-"}));
        tensWheel = initWheel(R.id.mbModTens, 0, new NumericWheelAdapter(getContext(), 0, 9));
        unitWheel = initWheel(R.id.mbModUnits, 0, new NumericWheelAdapter(getContext(), 0, 9));
		
        //modsWheel.setCurrentItem(50);
        
        getWindow().setLayout(
        		WindowManager.LayoutParams.WRAP_CONTENT,
        		WindowManager.LayoutParams.WRAP_CONTENT);
	}

    /**
     * Initializes wheel
     * @param id the wheel widget Id
     */
    private WheelView initWheel(int id, int label, WheelViewAdapter adapter) {
        WheelView wheel = getWheel(id);
        //wheel.setViewAdapter(new NumericWheelAdapter(getContext(), minValue, maxValue));
        wheel.setViewAdapter(adapter);
        if (this.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	wheel.setVisibleItems(3);
        } else {
        	wheel.setVisibleItems(5);
        }
        //wheel.setLabel(this.getContext().getString(label));
        wheel.setCurrentItem(0);
        
        return wheel;
    }

    /**
     * Returns wheel by Id
     * @param id the wheel Id
     * @return the wheel with passed Id
     */
    private WheelView getWheel(int id) {
    	return (WheelView) findViewById(id);
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		int modifier;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			//The dialog has been confirmed
			int sign = signWheel.getCurrentItem() == 0 ? 1 : -1;
			int tens = tensWheel.getCurrentItem();
			int unit = unitWheel.getCurrentItem();

			modifier = sign * ((tens * 10) + unit);
		} else {
			modifier = 0;
		}
		if (readyListener != null) {
			readyListener.ready(which == DialogInterface.BUTTON_POSITIVE, modifier, position);
		}
		dismiss();
	}

}
