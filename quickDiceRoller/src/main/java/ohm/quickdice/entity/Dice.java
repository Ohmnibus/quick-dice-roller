package ohm.quickdice.entity;

import java.util.ArrayList;

import ohm.dexp.DExpression;
import ohm.dexp.DResult;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.DivisionByZero;

/**
 * Define a Dice.
 * @author Ohmnibus
 *
 */
public class Dice {

	private DExpression dExp = new DExpression();
	private int id;
	private String name;
	private String description;
	private int resIdx;
	protected DiceBag parent = null;
	
	public Dice() {
		super();
	}

	/**
	 * Set an unique identifier for this dice.
	 * @param id Unique identifier
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Get the unique identifier of this dice.
	 * @return Unique identifier
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Set the name (=label) of this dice.
	 * @param name Name of this dice.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name (=label) of this dice.
	 * @return Name of this dice.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the description of this dice.
	 * @param description Description of this dice.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description of this dice.
	 * @return Description of this dice.
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set the index of the graphic resource to be used for this dice.<br />
	 * This is NOT a reference to a resource.
	 * @param resourceIndex Resource index.
	 */
	public void setResourceIndex(int resourceIndex) {
		this.resIdx = resourceIndex;
	}

	/**
	 * Get the index of the resource to be used for this dice.<br />
	 * This is NOT a reference to a resource.
	 * @return Resource index.
	 */
	public int getResourceIndex() {
		return this.resIdx;
	}

	/**
	 * Set the expression that represent the roll to perform.
	 * @param exp Expression representing a roll, i.e. {@code 2d6+1d4+2}.
	 */
	public void setExpression(String exp) {
		dExp.setExpression(exp);
	}

	/**
	 * Get the expression that represent the roll to perform.
	 * @return Expression representing a roll, i.e. {@code 2d6+1d4+2}.
	 */
	public String getExpression() {
		return dExp.getExpression();
	}

	/**
	 * Perform a "Roll" according to the expression assigned to this dice and return the result.<br />
	 * The result is always the same if the expression is deterministic (does not contain dices).
	 * @return Result of the expression evaluation.
	 * @throws DException Thrown if an error occurred during the parsing of the expression or evaluating it.<br />
	 * The exception is non-deterministic for non-deterministic expression. In example, the expression
	 * {@code 1/(1d6-2)} can lead to a {@link DivisionByZero} exception if the outcome of the d6 is 2.
	 */
	public RollResult getNewResult() throws DException {
		setContext(parent);
		DResult dResult = dExp.getResult();
		return new RollResult(
			new String(getName()),
			new String(getDescription()),
			new String(dResult.getResultText()),
			dResult.getRawResult(),
			dResult.getMaxRawResult(),
			dResult.getMinRawResult(),
			getResourceIndex());
	}

	/**
	 * Return the maximum value obtained evaluating the expression assigned to this dice.<br />
	 * Once this value is evaluated, it cannot change.
	 * @return Maximum value for this expression.
	 * @throws DException see {@link #getNewResult()} for details.
	 */
	public long getMaxResult() throws DException {
		setContext(parent);
		return dExp.getMaxResult();
	}

	/**
	 * Return the minimum value obtained evaluating the expression assigned to this dice.<br />
	 * Once this value is evaluated, it cannot change.
	 * @return Minimum value for this expression.
	 * @throws DException see {@link #getNewResult()} for details.
	 */
	public long getMinResult() throws DException {
		setContext(parent);
		return dExp.getMinResult();
	}
	
	/**
	 * Return an array containing all the label of the variables required
	 * by this dice expression.
	 * @return Array of variable's labels
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public String[] getRequiredVariables() throws DException {
		setContext(parent);
		return dExp.getRequiredVariables();
	}
	
	/**
	 * Return an array containing all the label of the variables required
	 * by this dice expression but not found in current context.
	 * @return Array of variable's labels
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public String[] getUnavailableVariables() {
		return getUnavailableVariables(parent);
	}
	
	/**
	 * Return an array containing all the label of the variables required
	 * by this dice expression but not found in specified context.
	 * @param diceBag Dice bag representing the context to apply.
	 * @return Array of variable's labels
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public String[] getUnavailableVariables(DiceBag diceBag) {
		String[] retVal = new String[0];
		String[] labels;
		if (diceBag == null) {
			return retVal;
		}
		try {
			dExp.setContext(diceBag.getVariables().getContext());
			labels = dExp.getRequiredVariables();
		} catch (DException e) {
			labels = new String[0];
			e.printStackTrace();
		}
		ArrayList<String> notFound = new ArrayList<String>();
		for (String label : labels) {
			if (diceBag.getVariables().getByLabel(label) == null) {
				notFound.add(label);
			}
		}
		retVal = notFound.toArray(retVal);
		return retVal;
	}
	
	public DiceBag getParent() {
		return parent;
	}

	protected void setParent(DiceBag parent) {
		this.parent = parent;
	}

	public boolean isChanged() {
		return parent == null ? false : parent.isChanged();
	}

	protected void setChanged() {
		if (parent != null) {
			parent.setChanged();
		}
	}

	/**
	 * Set the context (variable names and values) relative to specified dice bag to this instance.
	 * @param diceBag {@link DiceBag} from which get the context.
	 */
	public void setContext(DiceBag diceBag) {
		if (diceBag != null) {
			dExp.setContext(diceBag.getVariables().getContext());
		}
	}
}
