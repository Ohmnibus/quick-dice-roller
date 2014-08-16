package ohm.dexp;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;

import ohm.dexp.DContext.DVariable;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.ExpectedEndOfStatement;
import ohm.dexp.exception.ExpectedParameter;
import ohm.dexp.exception.InvalidCharacter;
import ohm.dexp.exception.MissingOperand;
import ohm.dexp.exception.NothingToEvaluate;
import ohm.dexp.exception.UnbalancedBracket;
import ohm.dexp.exception.UnexpectedParameter;
import ohm.dexp.exception.UnknownFunction;
import ohm.dexp.exception.UnknownVariable;
import ohm.dexp.function.TokenFunction;

/**
 * Class to handle dice expressions and evaluate them against a given {@link DContext}.
 * @author Ohmnibus
 *
 */
public class DExpression {

	private static final String[] EMPTY_VAR_KEYS = new String[0];
	private String exp;
	private DContext ctx = null;
	private transient String[] varKeys; //Contain used variable names.
	private transient Hashtable<String, DVariable> varCache; //Contain used variables and their last value.
	private transient TokenBase root;
	private transient boolean parsed;
	private transient boolean evaluatedOnce;
	//private transient boolean evaluated;
	private transient long resultValue;
	private transient long resultMaxValue;
	private transient long resultMinValue;
	private transient String resultString;
	private transient DException error;
	
	/**
	 * Create a new empty expression.
	 */
	public DExpression() {
		this(null);
	}
	
	/**
	 * Create a new expression from an expression string.
	 * @param exp Expression string.
	 */
	public DExpression(String exp) {
		varCache = new Hashtable<String, DVariable>();
		reset();
		this.exp = exp;
	}
	
	/**
	 * Set a new expression string to the evaluator.
	 * @param exp Expression string.
	 */
	public void setExpression(String exp) {
		reset();
		this.exp = exp;
	}
	
	/**
	 * Get current expression string.
	 * @return Expression string.
	 */
	public String getExpression() {
		return exp;
	}
	
	/**
	 * Sets the context on which this expression will be evaluated.
	 * @param context Context to use on evaluating this expression.
	 */
	public void setContext(DContext context) {
		this.ctx = context;
	}
	
	/**
	 * Get the context used to evaluate this expression.
	 * @return Context.
	 */
	public DContext getContext() {
		return this.ctx;
	}

	
	/**
	 * Evaluate the expression and return a result.<br />
	 * If the expression contains indeterministic elements (dice and/or indeterministic
	 * functions), all these values are randomly evaluated to compute a new result.<br />
	 * If the expression is deterministic (does not contain dices) the result will be always 
	 * the same.
	 * @return Result of the expression evaluation.
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public DResult getResult() throws DException {
		//evaluated = false;
		
		evaluate();
		
		return new DResult(resultValue, resultMaxValue, resultMinValue, resultString, this);
	}
	
	/**
	 * Get the expression maximum result.<br />
	 * This value is evaluated only one time.
	 * @return Expression maximum result in "raw" format (like {@link getRawResult}).
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public long getMaxResult() throws DException {
		//This is always a constant value, so just one evaluation is required.
		//if (! evaluatedOnce) {
		if (! validBounds()) {
			evaluate();
		} else if (error != null) {
			throw error;
		}
		return resultMaxValue;
	}
	
	/**
	 * Get the expression minimum result.<br />
	 * This value is evaluated only one time.
	 * @return Expression minimum result in "raw" format (like {@link getRawResult}).
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public long getMinResult() throws DException {
		//This is always a constant value, so just one evaluation is required.
		//if (! evaluatedOnce) {
		if (! validBounds()) {
			evaluate();
		} else if (error != null) {
			throw error;
		}
		return resultMinValue;
	}
	
	/**
	 * Return an array containing all the label of the variables required
	 * by this dice expression.
	 * @return Array of variable's labels
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public String[] getRequiredVariables() throws DException {
		
		try {
			if (! parsed) {
				//A call to parse() should be enough, but better
				//perform all the checks made in evaluate()
				evaluate();
			} else if (error != null) {
				throw error;
			}
		} catch (UnknownVariable ex) {
			//Consume
		}

		return varKeys;
	}
	
	
	// =============================
	// Private and protected methods
	// =============================
	
	/**
	 * Tell if {@link #resultMinValue} and {@link #resultMaxValue} are valid.
	 * @return
	 */
	protected boolean validBounds() {
		boolean retVal = true;
		if (evaluatedOnce == false) {
			return false;
		}
//		if (ctx != null && varCache.size() > 0) { //If exp was not parsed "varCache" has no elements.
//			Enumeration<String> keys = varCache.keys();
//			while (keys.hasMoreElements()) {
//				String key = keys.nextElement();
//				if (ctx.checkName(key) == false || ! varCache.get(key).equals(ctx.getVariable(key))) {
//					//Note: if checkName was false we are going to get an UnknownFunction exception.
//					retVal = false;
//					break;
//				}
//			}
//		}
		if (ctx != null) {
			for (String key : varKeys) { //If exp was not parsed "varKeys" has no elements.
				if (ctx.checkName(key) == false || ! varCache.get(key).equals(ctx.getVariable(key))) {
					//Note: if checkName was false we are going to get an UnknownFunction exception.
					retVal = false;
					break;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Set values of variables to the ones contained in current {@link DContext}.
	 */
	protected void setVarCacheValues() {
//		if (ctx != null && varCache.size() > 0) {
//			Enumeration<String> keys = varCache.keys();
//			while (keys.hasMoreElements()) {
//				String key = keys.nextElement();
//				if (ctx.checkName(key)) {
//					varCache.put(key, new DVariable(ctx.getVariable(key)));
//				}
//			}
//		}
		if (ctx != null) {
			for (String key : varKeys) {
				if (ctx.checkName(key)) {
					varCache.put(key, new DVariable(ctx.getVariable(key)));
				}
			}
		}
	}
	
	/**
	 * Reset all content as the instance has been just created.
	 */
	protected void reset() {
		exp = "";
		root = null;
		parsed = false;
		//evaluated = false;
		evaluatedOnce = false;
		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "";
		varKeys = EMPTY_VAR_KEYS;
		varCache.clear();
		error = null;
	}

	protected void setError(DException ex) {
		root = null;
		parsed = true;
		//evaluated = true;
		evaluatedOnce = true;
		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "Error";
		
		setVarCacheValues();
		
		error = ex;
	}
	
	protected void setResult(TokenBase rootToken) {
		//evaluated = true;
		evaluatedOnce = true;
		
		resultValue = rootToken.getRawResult();
		resultMaxValue = rootToken.getMaxResult();
		resultMinValue = rootToken.getMinResult();
		resultString = rootToken.getResultString();
		
		setVarCacheValues();
		
		error = null;
	}
	
	/**
	 * Parse and evaluate current expression.
	 * @return Error code.
	 */
	protected void evaluate() throws DException {
		try {
			parse();
			
			root.evaluate(ctx);
			
			setResult(root);
		} catch (DException ex) {
			setError(ex);
			throw ex;
		}
	}
	
	/**
	 * Parse current expression.
	 * @return Error code.
	 */
	protected void parse() throws DException {
		int iPos;           /* Expression index */

		GetTokenResult actToken;
		GetTokenResult nextToken;
		TokenBase tmpToken;
		int actTokenType;   /* Current token type (speed up a bit) */
		int lastTokenType;  /* Last token type */
		TokenBase tLastOp;  /* Last operator found */
		TokenBase tLastOpP; /* Operator before unary operator */
		TokenBase tFunc;    /* Last function found */
		Stack<TokenBase> parseStack;
		boolean isTerminal;	/* If last valid token is a terminal one */
		
		if (parsed) {
			if (error != null) throw error;
			return; //Already parsed, no action needed.
		}

		varCache.clear();
		tLastOp = null;
		tLastOpP = null;
		tFunc = null;
		lastTokenType = TK_NULL;
		
		actToken = new GetTokenResult();
		actToken.end = 1;
		nextToken = new GetTokenResult();
		parseStack = new Stack<TokenBase>();
		isTerminal = false;
		do {
			iPos = actToken.end;
			
			if (nextToken.type != TK_NULL) {
				actToken.value = nextToken.value;
				actToken.type = nextToken.type;
				actToken.begin = nextToken.begin;
				actToken.end = nextToken.end;
				
				nextToken.reset();
			} else {
				getToken(this.exp, iPos, actToken);
			}
			
			if (actToken.type != TK_NULL /* && actToken.error == DResult.ERR_NONE */) {
				isTerminal = false;
				/* Check token validity */
				actTokenType=actToken.type;
				if (checkTokenOrder(lastTokenType, actTokenType)) {
					switch (actTokenType) {
						case TK_VAL:
							/* Add Number to tree */
							tmpToken = TokenValue.InitToken(TokenValue.ParseRawValue(actToken.value));
							if (tLastOp == null) {
								root = tmpToken;
							} else {
								tLastOp.setRightChild(tmpToken);
								if (tLastOpP != null) {
									/* Added child to unary operator. */
									/* Reset reference to last operator before unary operator */
									tLastOp = tLastOpP;
									tLastOpP = null;
								}
							}
							isTerminal = true;
							break;
						case TK_UOP:
						case TK_OP:
							/* Add operator to tree */
							tmpToken = TokenOperator.InitToken(actToken.value);
							if (tLastOp == null) {
								if (root == null) {
									/* Unary operator */
									if (tmpToken instanceof TokenOperatorDice
											|| tmpToken instanceof TokenOperatorAdd
											|| tmpToken instanceof TokenOperatorSubtract) {

										root = tmpToken;
									} else {
										throw new MissingOperand(actToken.begin);
									}
								} else {
									/* First operator */
									tmpToken.setLeftChild(root);
									root = tmpToken;
								}
								//tLastOpP = tLastOp; //Same as "tLastOpP = null".
								tLastOpP = null;
							} else {
								//if (tmpToken.getPriority() > tLastOp.getPriority() || lastTokenType == TK_UOP) {
								if (tmpToken.getPriority() > tLastOp.getPriority()) {
									tmpToken.setLeftChild(tLastOp.getRightChild());
									tLastOp.setRightChild(tmpToken);
									//tLastOpP = tLastOp;
									tLastOpP = null;
								} else if (lastTokenType == TK_OP || lastTokenType == TK_UOP) {
									/* Unary operator after another operator */
									tmpToken.setLeftChild(tLastOp.getRightChild()); //Always null!
									tLastOp.setRightChild(tmpToken);
									if (tLastOpP == null) {
										/* Set reference to last operator before unary operator */
										tLastOpP = tLastOp;
									}
								} else {
									tmpToken.setLeftChild(root);
									root = tmpToken;
									tLastOpP = null;
								}
							}
							tLastOp = tmpToken;
							break;
						case TK_POP:
							/* Process "(" token */
							parseStack.push(root);
							parseStack.push(tLastOp);
							parseStack.push(tLastOpP);
							parseStack.push(tFunc);
							
							root = null;
							tLastOp = null;
							tLastOpP = null;
							tFunc = null;
							break;
						case TK_PCL:
							/* Process ")" token */
							if (parseStack.isEmpty()) {
								/* Error - unbalanced parenthesis */
								throw new UnbalancedBracket(actToken.begin);
							} else {
								tmpToken = root;
								
								tFunc = parseStack.pop();
								tLastOpP = parseStack.pop();
								tLastOp = parseStack.pop();
								root = parseStack.pop();
								
								if (tFunc != null) {
									/* This parenthesis block enclose func. arg. list */
									/* Add last argument to function */
									/* Check for correct arguments number */
									/* Add function to tree */
									tFunc.setNextChild(tmpToken);
									if (tFunc.nextChildNum() <= tFunc.getChildNumber()) {
										/* Error - too few parameters */
										throw new ExpectedParameter(actToken.begin);
									} else {
										tmpToken = tFunc;	/* To not write another "if (tLastOp==null)..." */
										tFunc = null;
									}
								}

								//if (retVal == DResult.ERR_NONE) {
								if (tLastOp == null) {
									root = tmpToken;
								} else {
									tLastOp.setRightChild(tmpToken);
								}
								//}
								isTerminal = true;
							}
							break;
						case TK_COM:
							/* Process "," token */
							if (parseStack.isEmpty()) {
								tmpToken = null;
							} else {
								tmpToken = parseStack.peek();
							}
							if (tmpToken==null) {
								/* Error - expected eos */
								throw new ExpectedEndOfStatement(actToken.begin);
							} else {
								tmpToken.setNextChild(root);
								if (tmpToken.nextChildNum() > tmpToken.getMaxChildNumber()) {
									/* Error - too much parameters */
									throw new UnexpectedParameter(actToken.begin);
								} else {
									root=null;
									tLastOp=null;
									tLastOpP=null;
									tFunc=null;
								}
							}
							break;
						default:
							/* Add name to tree */
							
							getToken(this.exp, actToken.end, nextToken);
							
							if (nextToken.type == TK_POP) {
								//Next token is an open bracket: this is a function.
								tFunc = TokenFunction.InitToken(actToken.value);
								if (tFunc == null) {
									//Error: function is not recognized
									throw new UnknownFunction(actToken.value, actToken.begin, actToken.end);
								}
							} else {
								//tmpToken = TokenValue.InitToken(actToken.value, ctx);
								tmpToken = TokenValue.InitToken(actToken.value, actToken.begin);
								//if (tmpToken != null) {
								varCache.put(actToken.value, new DVariable(0, 0, 0));
								if (tLastOp==null) {
									root = tmpToken;
								} else {
									tLastOp.setRightChild(tmpToken);
								}
								isTerminal = true;
								//} else {
								//	//Error: variable is not defined
								//	throw new UnknownVariable(actToken.value, actToken.begin, actToken.end);
								//}
							}
					}
					lastTokenType=actToken.type;
				} else {
					/* Invalid token sequence */
					switch (lastTokenType) {
						case TK_VAL:
						case TK_PCL:
							throw new ExpectedEndOfStatement(actToken.begin);
						case TK_UOP:
						case TK_OP:
						case TK_POP:
						case TK_COM:
							throw new MissingOperand(actToken.begin);
						default:
							throw new ExpectedParameter(actToken.begin);
					}
				}
			} else {
				/* Check if state is valid */
				if (!parseStack.isEmpty()) {
					/* Stack not empty: unbalanced parenthesis */
					//retVal = DResult.ERR_UNBALANCED_PARENTHESYS;
					//setError(retVal, exp.length()+1, exp.length()+1);
					throw new UnbalancedBracket(exp.length()+1);
				} else if (root == null) {
					/* Root=null: Empty expression */
					//retVal = DResult.ERR_NOTHING_TO_EVALUATE;
					//setError(retVal, 0, 0);
					throw new NothingToEvaluate();
				} else if (!isTerminal) {
					/* bTerminal=false: Expected operand */
					//retVal = DResult.ERR_MISSING_OPERAND;
					//setError(retVal, exp.length()+1, exp.length()+1);
					throw new MissingOperand(exp.length()+1);
				}
			}
		} while (actToken.type!=TK_NULL /* && retVal==DResult.ERR_NONE */);

		//Get the array of required variables
		if (varCache.size() > 0) {
			varKeys = varCache.keySet().toArray(EMPTY_VAR_KEYS);
		} else {
			varKeys = EMPTY_VAR_KEYS;
		}

		parsed = true;
	}

	/*
	 * Valid Tokens:
	 * - Value
	 * - Operator
	 * - Variable
	 * - Function/Variable
	 * Pseudo-token
	 * - Open bracket
	 * - Closed bracket
	 * - Comma (parameter separator)
	 */
	/** Null token */
	private static final int TK_NULL = 0;
	/** Value */
	private static final int TK_VAL = 1;
	/** Unary Operator */
	private static final int TK_UOP = 2;
	/** Binary Operator */
	private static final int TK_OP = 3;
	/** Name (Variable or function) */
	private static final int TK_NAME = 4;
	/** Pseudo-token: Open bracket */
	private static final int TK_POP = 6;
	/** Pseudo-token: Closed bracket */
	private static final int TK_PCL = 7;
	/** Pseudo-token: Parameter separator */
	private static final int TK_COM = 8; /* Set TOKEN_NUMBER as the last value */

	
	private static final int TOKEN_NUMBER = TK_COM + 1;

	private static boolean[][] TokenOrder = null;

	/**
	 * Tell if the order of given token types is allowed.
	 * @param token1
	 * @param token2
	 * @return
	 */
	private boolean checkTokenOrder(int token1, int token2) {
		if (TokenOrder == null) {
			TokenOrder = new boolean[TOKEN_NUMBER][TOKEN_NUMBER];
			for (int i=0; i<TOKEN_NUMBER; i++) {
				for (int j=0; j<TOKEN_NUMBER; j++) {
					TokenOrder[i][j] = i == TK_NULL;
				}
			}

			//Cannot begin with closed bracket or separator
			TokenOrder[TK_NULL][TK_PCL] = false;
			TokenOrder[TK_NULL][TK_COM] = false;

			//After a value:
			//Unary Operator, Binary Operator, Closed bracket, Separator
			TokenOrder[TK_VAL][TK_UOP]=true;
			TokenOrder[TK_VAL][TK_OP]=true;
			TokenOrder[TK_VAL][TK_PCL]=true;
			TokenOrder[TK_VAL][TK_COM]=true;

			//After a name:
			//Value: Unary Operator, Binary Operator, Closed bracket, Separator
			//Function: Open bracket
			TokenOrder[TK_NAME][TK_UOP]=true;
			TokenOrder[TK_NAME][TK_OP]=true;
			TokenOrder[TK_NAME][TK_POP]=true;
			TokenOrder[TK_NAME][TK_PCL]=true;
			TokenOrder[TK_NAME][TK_COM]=true;

			//After a closed bracket:
			//Unary Operator, Binary Operator, Closed bracket, Separator
			TokenOrder[TK_PCL][TK_UOP]=true;
			TokenOrder[TK_PCL][TK_OP]=true;
			TokenOrder[TK_PCL][TK_PCL]=true;
			TokenOrder[TK_PCL][TK_COM]=true;

			//After a unary operator:
			//Unary Operator, Value, Name (Variable or function), Open Bracket
			TokenOrder[TK_UOP][TK_UOP]=true;
			TokenOrder[TK_UOP][TK_VAL]=true;
			TokenOrder[TK_UOP][TK_NAME]=true;
			TokenOrder[TK_UOP][TK_POP]=true;

			//After a binary operator:
			//Unary Operator, Value, Name (Variable or function), Open Bracket
			TokenOrder[TK_OP][TK_UOP]=true;
			TokenOrder[TK_OP][TK_VAL]=true;
			TokenOrder[TK_OP][TK_NAME]=true;
			TokenOrder[TK_OP][TK_POP]=true;

			//After a open bracket:
			//Unary Operator, Value, Name (Variable or function), Open Bracket
			TokenOrder[TK_POP][TK_UOP]=true;
			TokenOrder[TK_POP][TK_VAL]=true;
			TokenOrder[TK_POP][TK_NAME]=true;
			TokenOrder[TK_POP][TK_POP]=true;

			//After a separator:
			//Unary Operator, Value, Name (Variable or function), Open Bracket
			TokenOrder[TK_COM][TK_UOP]=true;
			TokenOrder[TK_COM][TK_VAL]=true;
			TokenOrder[TK_COM][TK_NAME]=true;
			TokenOrder[TK_COM][TK_POP]=true;
		}
		return TokenOrder[token1][token2];
	}
	
	/**
	 * Get next token and detect it's type.<br />
	 * Token properties are written to a parameter passed by reference to reduce the number
	 * of object instantiated.
	 * @param exp Expression to search in.
	 * @param index Starting character where search begin.
	 * @param retVal A {@link GetTokenResult} instance where token properties are written.
	 */
	private void getToken(String exp, int index, GetTokenResult retVal) throws DException {
		String sExpr;
		int iCnt;	/* Counter and new token position */
		char actChar, nextChar;			/* Actual and next char */
		int actCharType, nextCharType;	/* Actual and next char type */
		int dotCount = 0; /* Used to check double dot in values */
		StringBuilder myValue = null; /* Used to optimize creation of values and names */
		
		retVal.value = "";
		retVal.type = TK_NULL;
		retVal.begin = index;
		retVal.end = 0;
		
		iCnt = index;
		sExpr = exp.toLowerCase(Locale.getDefault());

		while (iCnt <= sExpr.length() && retVal.end == 0) {
			/* Get this char and next char */
			actChar = sExpr.charAt(iCnt-1);
			
			/* Check char type */
			actCharType = charType(actChar);

			/* If searching for new token, guess his type */
			if (retVal.type==TK_NULL) {
				switch (actCharType) {
					case CT_DIGIT:
						retVal.type=TK_VAL;
						//retVal.value="";
						break;
					case CT_DOT:
						retVal.type=TK_VAL;
						retVal.value="0";
						dotCount = 1;
						break;
					case CT_UOP:
						/* If is an operator, token is recognized ('cause are 1 char long) */
						retVal.type = TK_UOP;
						retVal.value = String.valueOf(actChar);
						retVal.end = iCnt + 1;
						break;
					case CT_OP:
						/* If is an operator, token is recognized ('cause are 1 char long) */
						retVal.type = TK_OP;
						retVal.value = String.valueOf(actChar);
						retVal.end = iCnt + 1;
						break;
					case CT_POP:
						/* If is a parenthesis, token is recognized ('cause are 1 char long) */
						retVal.type = TK_POP;
						retVal.value = "(";
						retVal.end = iCnt + 1;
						break;
					case CT_PCL:
						/* If is a parenthesis, token is recognized ('cause are 1 char long) */
						retVal.type = TK_PCL;
						retVal.value = ")";
						retVal.end = iCnt + 1;
						break;
					case CT_COM:
						/* If is a comma, token is recognized ('cause are 1 char long) */
						retVal.type = TK_COM;
						retVal.value = ",";
						retVal.end = iCnt + 1;
						break;
					case CT_ALPHA:
						/* Function or variable */
						retVal.type = TK_NAME;
						break;
					case CT_UNKNOWN:
						/* error: Invalid character */
						throw new InvalidCharacter(retVal.begin);
				}
			}
			
			/* From here i will compose number or functions */
			if (retVal.end == 0 && retVal.type != TK_NULL) {

				if (iCnt < sExpr.length()) {
					nextChar = sExpr.charAt(iCnt);
				} else {
					nextChar = 0;
				}
				nextCharType = charType(nextChar);

				switch (retVal.type) {
					case TK_VAL:
						/* Numbers */
						if (actCharType==CT_DOT && dotCount > 0) {
							/* error: double dot */
							throw new ExpectedEndOfStatement(retVal.begin);
						}

						//retVal.value = retVal.value + String.valueOf(actChar);
						if (myValue == null) {
							myValue = new StringBuilder();
						}
						myValue.append(actChar);
						
						if (nextCharType != CT_DIGIT && nextCharType != CT_DOT) {
							/* Number recognized */
							retVal.value = myValue.toString();
							retVal.end = iCnt + 1;
						}
						break;
					case TK_NAME:
						/* Functions and variables */
						if (myValue == null) {
							myValue = new StringBuilder();
						}
						myValue.append(actChar);

						//To prevent that "d6" could be recognized as a name,
						//they must be of 3 ALPHA followed by any combination of ALPHA or DIGIT

						//if (nextCharType != CT_ALPHA && nextCharType != CT_DIGIT) {
						if (nextCharType != CT_ALPHA && (myValue.length() < 3 || nextCharType != CT_DIGIT)) {
							/* Function recognized */
							retVal.value = myValue.toString();
							retVal.end = iCnt + 1;
						}
				}
			}
			
			iCnt++;
		}
		
		//Check for special case: "d" (or "w") operator
		if (retVal.value.equals("d") || retVal.value.equals("w")) {
			retVal.type = TK_UOP;
		}
		
		if (retVal.end == 0) {
			/* Expression parse completed, token not recognized */
			/* This condition should never happen */
			retVal.value = "";
			retVal.type = TK_NULL;
			retVal.begin = index;
			retVal.end = sExpr.length()+1;
		}
	}
	
	private static final int CT_NULL = 0;
	private static final int CT_DIGIT = 1;
	private static final int CT_UOP = 2;
	private static final int CT_OP = 3;
	private static final int CT_ALPHA = 4;
	private static final int CT_DOT = 5;
	private static final int CT_POP = 6;
	private static final int CT_PCL = 7;
	private static final int CT_COM = 8;
	private static final int CT_UNKNOWN = 9;

	/**
	 * Determine character type.
	 * @param ch Character to be checked.
	 * @return The character type.
	 */
	private static int charType(char ch) {
		int iRetVal = CT_UNKNOWN;
		switch (ch) {
			case '0': case '1':
			case '2': case '3':
			case '4': case '5':
			case '6': case '7':
			case '8': case '9':
				iRetVal = CT_DIGIT;
				break;
			case '+': case '-':
				iRetVal = CT_UOP;
				break;
			case '*': case '/':
				iRetVal = CT_OP;
				break;
			case '.':
				iRetVal = CT_DOT;
				break;
			case '(':
				iRetVal = CT_POP;
				break;
			case ')':
				iRetVal = CT_PCL;
				break;
			case ',':
				iRetVal = CT_COM;
				break;
			case ' ': case 0:
				iRetVal = CT_NULL;
				break;
			default:
				if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z'))
					iRetVal = CT_ALPHA;
		}
		
		return iRetVal;
	}
	
	/**
	 * Contain the result of a {@link getToken} call.
	 * @author Ohmnibus
	 *
	 */
	private class GetTokenResult {
		public GetTokenResult() {
			reset();
		}

		public void reset() {
			value = "";
			type = TK_NULL;
			begin = 0;
			end = 0;
		}

		/** Token raw name */
		public String value = "";
		/** Token type */
		public int type = TK_NULL;
		/** Token starting character relative to the original expression */
		public int begin = 0;
		/** Token ending character relative to the original expression */
		public int end = 0;
	}

}
