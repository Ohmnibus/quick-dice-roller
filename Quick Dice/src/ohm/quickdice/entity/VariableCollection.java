package ohm.quickdice.entity;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.quickdice.control.SerializationManager;

public class VariableCollection implements BaseCollection<Variable> {
	
	private ArrayList<Variable> variableList;
	private DiceBag owner;
	private DContext context;
	
	/** If context is changed in it's structure */
	private boolean contextValid = false;
	/** If context is changed in it's values */
	private boolean contextUpToDate = false;

	protected VariableCollection(DiceBag owner) {
		this.owner = owner;
		this.variableList = new ArrayList<Variable>();
	}

	/**
	 * Add a new {@link Variable} as the last element.<br />
	 * @param newVariable {@link Variable} to add.
	 * @return Position at which the {@link Variable} is added.
	 */
	public int add(Variable newVariable) {
		return add(-1, newVariable);
	}
	
	/**
	 * Add a new {@link Variable} at the specified position.<br />
	 * If the position is outside the boundary of the {@link DiceBag}, the variable will be added at the end.
	 * @param position Position at which the variable will be added
	 * @param newVariable {@link Variable} to add.
	 * @return Position at which the {@link Variable} is added.
	 * @throws InvalidParameterException Thrown if the label of given variable already exists.
	 */
	public int add(int position, Variable newVariable) {
		int retVal = position;
		
		uniqueLabel(newVariable);
		
		if (position >= 0 && position < variableList.size()) {
			//Add at position
			variableList.add(position, newVariable);
		} else {
			//Add at the end
			variableList.add(newVariable);
			retVal = variableList.size() - 1;
		}
		newVariable.setParent(owner);
		setChanged();
		refreshId();

		return retVal;
	}

	/**
	 * Clone specified {@link Variable}.<br />
	 * The new cloned {@link Variable} will be added to the end of the collection.
	 * @param position Position of the source {@link Variable}.
	 */
	public void duplicate(int position) {
		try {
			Variable newVariable = SerializationManager.Variable(SerializationManager.Variable(get(position)));
			add(newVariable);
		} catch (IOException e) {
			//WTF?
			e.printStackTrace();
		}
	}
	
	/**
	 * Edit a specified variable ({@link Variable}).<br />
	 * This method will substitute the element at {@code position}
	 * with the given variable.<br />
	 * The behavior is pretty the same as calling {@code remove(position)}
	 * and then {@code add(position, newVariable)}.
	 * @param position Position of the variable to substitute.
	 * @param newVariable New {@code Variable}.
	 * @return A status indicating whether the change was made or not
	 */
	public boolean edit(int position, Variable newVariable) {
		boolean retVal = true;
		if (position >= 0 && position < variableList.size()) {
//			Variable oldVariable = variableList.set(position, newVariable);
//			oldVariable.setParent(null); //Avoid side effects
//			newVariable.setParent(owner);
//			uniqueLabel(newVariable);

			Variable oldVariable = variableList.get(position);
			if (! oldVariable.matchLabel(newVariable.getLabel())) {
				//Label has changed. Check for duplicate.
				uniqueLabel(newVariable);
			}
			variableList.set(position, newVariable);
			oldVariable.setParent(null); //Avoid side effects
			newVariable.setParent(owner);

			setChanged();
			refreshId();
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	/**
	 * Remove the variable at the given position.
	 * @param position Position of the variable to remove
	 * @return The {@link Variable} removed, or {@code null} if the change was not made
	 */
	public Variable remove(int position) {
		Variable retVal = null;
		if (position >= 0 && position < variableList.size()) {
			retVal = variableList.remove(position);
			retVal.setParent(null); //Avoid side effects
			setChanged();
			refreshId();
		}
		return retVal;
	}
	
	/**
	 * Move a variable inside current {@link DiceBag}.
	 * @param fromPosition Starting position of the variable to move
	 * @param toPosition Ending position of the variable to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromPosition, int toPosition) {
		int curDiceBagIndex = owner.getParent().getDiceBagCollection().getCurrentIndex();
		return move(curDiceBagIndex, fromPosition, curDiceBagIndex, toPosition);
	}
	
	/**
	 * Move a variable, even to a different {@link DiceBag}s.
	 * @param fromDiceBagIndex Index of the starting {@link DiceBag}
	 * @param fromPosition Starting position of the variable to move
	 * @param toDiceBagIndex Index of the destination {@link DiceBag}
	 * @param toPosition Ending position of the variable to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		boolean retVal = false;
		VariableCollection collectionFrom = null;
		VariableCollection collectionTo = null;
		
		if (fromDiceBagIndex == toDiceBagIndex) {
			if (toPosition > fromPosition) {
				toPosition--;
			}
		}
		if (fromDiceBagIndex != toDiceBagIndex || fromPosition != toPosition) {
			DiceBagCollection dBags = owner.getParent().getDiceBagCollection();
			if (fromDiceBagIndex >= 0 && fromDiceBagIndex < dBags.size()) {
				collectionFrom = dBags.get(fromDiceBagIndex).getVariables();
			}
			if (toDiceBagIndex >= 0 && toDiceBagIndex < dBags.size()) {
				collectionTo = dBags.get(toDiceBagIndex).getVariables();
			}
			if (collectionFrom != null && collectionTo != null) {
				if (fromPosition >= 0 && fromPosition < collectionFrom.size()
						&& toPosition >= 0 && toPosition <= collectionTo.size()) {
					
					Variable variable = collectionFrom.remove(fromPosition);
					collectionTo.add(toPosition, variable);
					
					collectionFrom.refreshId();
					if (fromDiceBagIndex != toDiceBagIndex)
						collectionTo.refreshId();
					
					setChanged();
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	public Variable get(int position) {
		return variableList.get(position);
	}
	
	@Override
	public int indexOf(Variable item) {
		return variableList.indexOf(item);
	}

	/**
	 * Return the variable corresponding to given label, or {@code null} id not found.
	 * @param label Variable's label to search.
	 * @return {@link Variable} corresponding to given label, or {@code null} id not found.
	 */
	public Variable getByLabel(String label) {
		Variable retVal = null;
		for (Variable var : variableList) {
			if (var.matchLabel(label)) {
				retVal = var;
				break;
			}
		}
		return retVal;
	}
	
	public int size() {
		return variableList.size();
	}

	public void clear() {
		for (Variable var : variableList) {
			var.setParent(null);
		}
		variableList.clear();
	}

	/**
	 * Set the identifier of each variable equal to it's index in the dice bag.
	 * @param variableList
	 */
	protected void refreshId() {
		for (int i = 0; i < variableList.size(); i++) {
			variableList.get(i).setID(i);
		}
	}
	
	/**
	 * Make sure that the label assigned to this variable is unique.
	 * @param variable
	 */
	private void uniqueLabel(Variable variable) {
		String label = uniqueLabel(variable.getLabel());
		if (! variable.matchLabel(label)) {
			//Label has changed
			variable.setLabel(label);
		}
	}

	/**
	 * Check if given label already exists in any variable of this collection.
	 * If exists, change the label in order to make it unique.
	 * @param label
	 * @return
	 */
	private String uniqueLabel(String label) {
		
		if (getByLabel(label) == null) { //Shortcut
			return label;
		}
				
		int iteration = 0;
		String workLabel;
		
		do {
			String suffix = intToChar(iteration);
			
			workLabel = label + suffix;
			
			if (workLabel.length() > 5) {
				workLabel = workLabel.substring(0, 5 - suffix.length()) + suffix;
			}
			
			iteration++;
		} while (getByLabel(workLabel) != null);
		
		return workLabel;
	}
	
	private String intToChar(int val) {
		String retVal = "";
		
		int workVal = val;
		do {
			char newChar = (char) (workVal % 26); //a-z: 26 chars
			workVal = (workVal - newChar) / 26;
			
			retVal = retVal + new String(new char[] { (char) ('A' + newChar) });
		} while (workVal > 0);
		
		return retVal;
	}
	
	/**
	 * Notify a change in the structure or content of any variable.
	 */
	protected void setChanged() {
		owner.setChanged();
		//changedContext();
		contextValid = false;
	}

	/**
	 * Notify a change in any value of any variable.
	 */
	protected void setValueChanged() {
		owner.setChanged();
		//changedCcontent();
		contextUpToDate = false;
	}

	@Override
	public Iterator<Variable> iterator() {
		return variableList.iterator();
	}
	
//	/**
//	 * Notify a change of the context.<br />
//	 * Such changes happen when variables are added, deleted, moved or edited.
//	 */
//	private void changedContext() {
//		contextValid = false;
//	}
	
//	/**
//	 * Notify a change of a value of a variable.<br />
//	 */
//	protected void changedContent() {
//		contextUpdated = false;
//	}
	
	protected DContext getContext() {
		if (! contextValid) {
			//Context is not valid.
			//This mean it must be recreated.
			context = new DContext();
			
			for (Variable var : variableList) {
				context.setValue(
						var.getCleanLabel(),
						var.getMinVal() * TokenBase.VALUES_PRECISION_FACTOR,
						var.getMaxVal() * TokenBase.VALUES_PRECISION_FACTOR,
						var.getCurVal() * TokenBase.VALUES_PRECISION_FACTOR);
			}
			
			contextValid = true;
			contextUpToDate = true;
		} else if (! contextUpToDate) {
			//Context is not updated.
			//It's values are changed.
			for (Variable var : variableList) {
				context.setValue(
						var.getCleanLabel(),
						var.getMinVal() * TokenBase.VALUES_PRECISION_FACTOR,
						var.getMaxVal() * TokenBase.VALUES_PRECISION_FACTOR,
						var.getCurVal() * TokenBase.VALUES_PRECISION_FACTOR);
			}
			contextUpToDate = true;
		}
		return context;
	}
}
