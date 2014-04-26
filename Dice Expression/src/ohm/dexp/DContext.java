package ohm.dexp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;

/**
 * Represent a CONTEXT, which is composed by one or more Expressions and zero or more variables.<br />
 * All the expression defined for this context cannot contain other variables than those defined
 * for this context.<br />
 * During an expression definition, the expression must be checked again the parent context in order
 * to find undeclared variables. 
 * @author Ohmnibus
 *
 */
public class DContext extends EntityBase implements Serializable {

	/**
	 * Serial version UID used for serialization. 
	 */
	private static final long serialVersionUID = 8887117616411050029L;
	
	Hashtable<String, DVariable> variables;
	Hashtable<Integer, DExpression> expressions;
	Hashtable<Integer, DInstance> instances;
	DInstance currentInstance;

	public DContext() {
		variables = new Hashtable<String, DVariable>();
		expressions = new Hashtable<Integer, DExpression>();
		instances = new Hashtable<Integer, DInstance>();
	}
	
	/**
	 * Return the instance with the default values.
	 * @return Default instance.
	 */
	public DInstance getDefaultInstance() {
		return null;
	}
	
	/**
	 * Return the currently selected instance.
	 * @return Current instance.
	 */
	public DInstance getCurrentInstance() {
		return currentInstance;
	}
	
	/**
	 * Set currently selected instance providing instance identifier.
	 * @param id Instance identifier.
	 */
	public void setCurrentInstance(int id) {
		currentInstance = getInstance(id);
		if (currentInstance == null) {
			throw new IllegalArgumentException("Provided instance id is not valid.");
		}
	}
	
	/**
	 * Set currently selected instance.
	 * @param id Instance identifier.
	 */
	public void setCurrentInstance(DInstance instance) {
		if (instance.getContext() != this || getInstance(instance.getID()) == null) {
			throw new IllegalArgumentException("Provided instance is not valid.");
		}
		currentInstance = instance;
	}
	
	/* ***************************** */ 
	/* Variable definitions handling */ 
	/* ***************************** */ 
	
	/**
	 * Check if the given variable is defined for this {@link DContext}
	 * @param label Label of the variable to test.
	 * @return {@literal true} if the label is defined for this context; {@literal false} otherwise.
	 */
	public boolean checkVariable(String label) {
		return getVariableDefinition(label) != null;	
	}
	
	/**
	 * Return the definition of the given variable.
	 * @param label Label of the variable.
	 * @return Definition for the variable, or {@code null} if not exist.
	 */
	public DVariable getVariableDefinition(String label) {
		return variables.get(label);
	}
	
	/**
	 * Add a variable definition to the current context.
	 * @param label Label of the variable.
	 * @param definition Definition for the variable.
	 */
	public void setVariableDefinition(String label, DVariable definition) {
		definition.setLabel(label);
		setVariableDefinition(definition);
	}
	
	/**
	 * Add a variable definition to the current context.
	 * @param definition Definition for the variable.
	 */
	public void setVariableDefinition(DVariable definition) {
		variables.put(definition.getLabel(), definition);
	}

	/**
	 * Assign a definition to a variable.
	 * @param label Variable name.
	 * @param definition Definition for the variable.
	 */
	public void removeVariableDefinition(String label) {
		//Controlla che le espressioni non contengano la variabile. In caso positivo?
		//Rimuovi il valore dalle istanze
		//Rimuovi il valore dall'hashtable
	}

	
	/* ******************** */ 
	/* Expressions handling */ 
	/* ******************** */ 

	/**
	 * Search for the first available expression ID.
	 * @return First available expression ID.
	 */
	protected int getFreeExpressionID() {
		int expId;
		//Search for first free ID.
		//TODO: Ottimizzare
		expId = 0;
		while (expressions.containsKey(expId)) {
			expId ++;	
		}
		return expId;
	}

	/**
	 * Add a {@link DExpression} to the contexts expression collection.
	 * @param exp Expression to add.
	 * @return Expression ID.
	 */
	protected int addExpression(DExpression exp) {
		int expId;

		expId = getFreeExpressionID();
		exp.setID(expId);
		expressions.put(expId, exp);
		
		return expId;
	}
	
	/**
	 * Create a new {@link DExpression} valid for this context.
	 * @return New expression.
	 */
	public DExpression newExpression() {
		DExpression exp;

		exp = new DExpression(this);
		addExpression(exp);
		
		return exp;
	}
	
	/**
	 * Return the expression corresponding to the given identifier.
	 * @param id Expression identifier (related to the context).
	 * @return {@link DExpression} corresponding to the given id, or {@literal null} if not found.
	 */
	public DExpression getExpression(int id) {
		return expressions.get(id);
	}
	
	/**
	 * Return an array containing all the expression of this context.
	 * @return Array containing all the expression of this context.
	 */
	public Collection<DExpression> getAllExpression() {
		return expressions.values();
	}
	

	/* ******************* */ 
	/* Instances handling */ 
	/* ******************* */ 

	/**
	 * Search for the first available instance ID.
	 * @return First available instance ID.
	 */
	protected int getFreeInstanceID() {
		int id;
		//Search for first free ID.
		//TODO: Ottimizzare
		id = 0;
		while (instances.containsKey(id)) {
			id ++;	
		}
		return id;
	}

	/**
	 * Add a {@link DInstance} to the contexts instance collection.
	 * @param instance Instance to add.
	 * @return Instance ID.
	 */
	protected int addInstance(DInstance instance) {
		int id;

		id = getFreeInstanceID();
		instance.setID(id);
		instances.put(id, instance);
		
		return id;
	}
	
	/**
	 * Create a new {@link DInstance} valid for this context.
	 * @return New instance.
	 */
	public DInstance newInstance() {
		DInstance instance;

		instance = new DInstance(this);
		addInstance(instance);
		
		return instance;
	}
	
	/**
	 * Return the instance corresponding to the given identifier.
	 * @param id Instance identifier (related to the context).
	 * @return {@link DInstance} corresponding to the given id, or {@literal null} if not found.
	 */
	public DInstance getInstance(int id) {
		return instances.get(id);
	}
	
	/**
	 * Return an array containing all the expression of this context.
	 * @return Array containing all the expression of this context.
	 */
	public Collection<DInstance> getAllInstance() {
		return instances.values();
	}

}
