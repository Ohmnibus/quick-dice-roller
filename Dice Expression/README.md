Dice Expression
===============

This is the main library used to parse and evaluate expressions that represents dice rolls of any complexity.

In this library are defined all the available functions as extension of the `TokenFunction` class.

Add a new function
------------------

First, create your class:
```java
public class TokenFunctionAbs extends TokenFunction {
	@Override
	protected int initChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return 16;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
        //TODO
	}
```

Where:
- `initChildNumber` must return the number of mandatory parameters
- `getType` must return an unique value
- `getPriority` returns always 0 for functions
- `evaluateSelf` this is the method that evaluate all

A possible implementation of `evaluateSelf` to elaborate the absolute value is:

```java

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
        
        //Get the first (and only) parameter
        TokenBase child = getChild(1);
		
		//Evaluate the parameter using the supplied instance.
		//The instance contains informations like named values.
		child.evaluate(instance);
		
		//child.getRawResult() contains the "raw" result, that is the result
		//in fixed point notation.
		//VALUES_PRECISION_DIGITS is the number of digits after the fixed point (currently 3)
		//VALUES_PRECISION_FACTOR is used to obtain the integer result
		//intResult = rawResult / VALUES_PRECISION_FACTOR
		resultValue = Math.abs(child.getRawResult());

		//The maximum value of the parameter
		resultMaxValue = child.getMaxResult();
		//The minimum value of the parameter
		resultMinValue = child.getMinResult();
		
		if (resultMaxValue < 0 && resultMinValue < 0) {
			//Both negatives. Range change sign
			resultMaxValue = -resultMaxValue;
			resultMinValue = -resultMinValue;
		} else if (resultMaxValue < 0 || resultMinValue < 0) {
			//Only one negative. Range vary from 0 to higher absolute
			resultMaxValue = Math.abs(resultMaxValue);
			resultMinValue = Math.abs(resultMinValue);
			if (resultMaxValue < resultMinValue) {
				resultMaxValue = 0;
			} else {
				resultMinValue = 0;
			}
		}
		
		reorderMaxMinValues();

		//Check result length
		//getResultString contain the textual result
		String res = child.getResultString();
		//MAX_TOKEN_STRING_LENGTH is the maximum lenght allowed for the textual result 
		if (res.length() + 2 > MAX_TOKEN_STRING_LENGTH) {
			//Output will be too long, use short format
			resultString = CH_ABS_OP + SYM_TRUNK_PART_ELLIPSIS + SYM_TRUNK_PART_EQUAL + 
				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + CH_ABS_CL;
		} else {
			//Long format
			resultString = CH_ABS_OP + res + CH_ABS_CL;
		}

		//Done.
		//Essentially the result is stored in:
		//- resultValue
		//- resultString
		//- resultMaxValue
		//- resultMinValue
	}
```

More info
=========

More info at the [Quick Dice Roller project page][1].

[1]: https://github.com/Ohmnibus/quick-dice-roller