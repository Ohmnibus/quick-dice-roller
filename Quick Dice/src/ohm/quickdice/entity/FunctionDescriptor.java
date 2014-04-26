package ohm.quickdice.entity;

import android.content.Context;
import android.content.res.Resources;

public class FunctionDescriptor {
	
	public class ParamDescriptor {
		String label;
		String hint;
		
		public ParamDescriptor(String label, String hint) {
			this.label = label;
			this.hint = hint;
		}
		
		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		/**
		 * @return the hint
		 */
		public String getHint() {
			return hint;
		}
		/**
		 * @param hint the hint to set
		 */
		public void setHint(String hint) {
			this.hint = hint;
		}
	}
	
	String token;
	int resIndex;
	String name;
	String desc;
	String url;
	ParamDescriptor[] parameters;
	
	public FunctionDescriptor(String token, int resId, String name, String description, String onlineReference) {
		this.token = token;
		this.resIndex = resId;
		this.name = name;
		this.desc = description;
		this.url = onlineReference;
	}
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the resIndex
	 */
	public int getResId() {
		return resIndex;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @return the parameters
	 */
	public ParamDescriptor[] getParameters() {
		return parameters;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void addParameter(ParamDescriptor parameter) {
		if (this.parameters == null) {
			this.parameters = new ParamDescriptor[1];
		} else {
			ParamDescriptor[] newArray = new ParamDescriptor[this.parameters.length + 1];
			for (int i = 0; i<this.parameters.length; i++) {
				newArray[i] = this.parameters[i];
			}
			this.parameters = newArray;
		}
		this.parameters[this.parameters.length - 1] = parameter;
	}
	
	public static FunctionDescriptor initDescriptor(Context context, String token, int resId, int nameId, int descriptionId, int onlineReferenceId, int paramNamesId, int paramHintsId) {
		FunctionDescriptor retVal;
    	Resources res;
    	res = context.getResources();
    	
		retVal = new FunctionDescriptor(
				token,
				resId,
				res.getString(nameId),
				res.getString(descriptionId),
				res.getString(onlineReferenceId));
		
		String[] pLabels = res.getStringArray(paramNamesId);
		String[] pHints = res.getStringArray(paramHintsId);
		
		for (int i = 0; i < pLabels.length; i++) {
			retVal.addParameter(retVal.new ParamDescriptor(
					pLabels[i],
					pHints[i]));
		}
		
		return retVal;
	}
}
