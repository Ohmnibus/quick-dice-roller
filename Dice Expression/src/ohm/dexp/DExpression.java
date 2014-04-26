package ohm.dexp;

import java.util.Locale;
import java.util.Stack;

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
 * Class to handle dice expressions again a given {@link DContext} and evaluate them
 * again a given {@link DInstance} of a {@link DContext}.
 * @author Ohmnibus
 *
 */
public class DExpression extends EntityBase {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 4278225516667385017L;
	
	private transient DContext context;
	private String exp;
	private transient TokenBase root;
	private transient boolean parsed;
	private transient boolean evaluatedOnce;
	private transient boolean evaluated;
	private transient int lastUsedInstanceID = -1;
	private transient long resultValue;
	private transient long resultMaxValue;
	private transient long resultMinValue;
	private transient String resultString;
	private transient DException error;
	//private int errNumber;
	//private int errFrom;
	//private int errTo;
	
	/**
	 * Create a new contextless empty expression.
	 */
	public DExpression() {
		reset();
		this.context = null;
	}
	
	/**
	 * Create a new empty expression.
	 * @param context Context to use to parse the expression.
	 */
	public DExpression(DContext context) {
		reset();
		this.context = context;
	}
	
	/**
	 * Create a new expression from an expression string.
	 * @param context Context to use to parse the expression.
	 * @param exp Expression string.
	 */
	public DExpression(DContext context, String exp) {
		reset();
		this.context = context;
		this.exp = exp;
	}
	
	/**
	 * Get the context associated to the expression evaluator.
	 * @return Context used.
	 */
	public DContext getContext() {
		return context;
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
	
//	/**
//	 * "Roll" the dice defined in the expression.<br />
//	 * This take no effect if the expression is deterministic (does not contain dices).
//	 */
//	public void roll() {
//		evaluated = false;
//	}
//	
//	/**
//	 * Get the expression result trunked to it's integer part.
//	 * @return The expression result.
//	 */
//	public long getResult() {
//		return getRawResult() / TokenBase.VALUES_PRECISION_FACTOR;
//	}
//	
//	/**
//	 * Get the raw expression result.<br />
//	 * This is a fixed point value with {@link TokenBase.VALUES_DECIMALS} decimal values
//	 * and need do be adjusted to obtain the real expression result.<br />
//	 * @return The raw expression result.
//	 */
//	public long getRawResult() {
//		evaluate();
//		return resultValue;
//	}
//	
//	/**
//	 * Get a string representing the expression with roll result in place of dices. 
//	 * @return String representation of the result.
//	 */
//	public String getResultString() {
//		evaluate();
//		return resultString;
//	}
	
	/**
	 * "Roll" the dice defined in the expression and return the result.<br />
	 * The result is always the same if the expression is deterministic (does not contain dices).
	 * @return Result of the expression evaluation.
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public DResult getResult() throws DException {
		evaluated = false;
		
		evaluate();
		
		return new DResult(resultValue, resultMaxValue, resultMinValue, resultString, this);
	}
	
	/**
	 * Get the expression maximum result.
	 * @return Expression maximum result in "raw" format (like {@link getRawResult}).
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public long getMaxResult() throws DException {
		//This is always a constant value, so just one evaluation is required.
		if (! evaluatedOnce) {
			evaluate();
		} else if (error != null) {
			throw error;
		}
		return resultMaxValue;
	}
	
	/**
	 * Get the expression minimum result.
	 * @return Expression minimum result in "raw" format (like {@link getRawResult}).
	 * @throws DException Thrown if an error occurred during parse or evaluation.
	 */
	public long getMinResult() throws DException {
		//This is always a constant value, so just one evaluation is required.
		if (! evaluatedOnce) {
			evaluate();
		} else if (error != null) {
			throw error;
		}
		return resultMinValue;
	}
	
//	/**
//	 * Get the parsing/evaluating error number, or {@link ERR_NONE} if no error where encountered.
//	 * @return Error number.
//	 */
//	public int getErrNumber() {
//		if (! evaluatedOnce) {
//			evaluate();
//		}
//		return errNumber;
//	}
//	
//	/**
//	 * Get the first character where error was encountered.
//	 * @return First character of error area.
//	 */
//	public int getErrFrom() {
//		if (! evaluatedOnce) {
//			evaluate();
//		}
//		return errFrom;
//	}
//	
//	/**
//	 * Get the last character where error was encountered.
//	 * @return Last character of error area.
//	 */
//	public int getErrTo() {
//		if (! evaluatedOnce) {
//			evaluate();
//		}
//		return errTo;
//	}
	
	// =============================
	// Private and protected methods
	// =============================
	
	/**
	 * Reset all content as the instance has been just created.
	 */
	protected void reset() {
		exp = "";
		root = null;
		parsed = false;
		evaluated = false;
		evaluatedOnce = false;
		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "";
		error = null;
		//errNumber = DResult.ERR_NONE;
		//errFrom = 0;
		//errTo = 0;
	}

//	protected void setError(int errorCode, int errorFrom, int errorTo) {
//		root = null;
//		parsed = true;
//		evaluated = true;
//		evaluatedOnce = true;
//		resultValue = 0;
//		resultMaxValue = 0;
//		resultMinValue = 0;
//		resultString = "Error";
//		errNumber = errorCode;
//		errFrom = errorFrom;
//		errTo = errorTo;
//	}

	protected void setError(DException ex) {
		root = null;
		parsed = true;
		evaluated = true;
		evaluatedOnce = true;
		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "Error";
		error = ex;
		//errNumber = errorCode;
		//errFrom = errorFrom;
		//errTo = errorTo;
	}
	
	protected void setResult(TokenBase rootToken) {
		//root = rootToken;
		//parsed = true;
		evaluated = true;
		evaluatedOnce = true;
		
		resultValue = rootToken.getRawResult();
		resultMaxValue = rootToken.getMaxResult();
		resultMinValue = rootToken.getMinResult();
		resultString = rootToken.getResultString();
		
		error = null;
		//errNumber = DResult.ERR_NONE;
		//errFrom = 0;
		//errTo = 0;
	}
	
	/**
	 * Parse and evaluate current expression.
	 * @return Error code.
	 */
	protected void evaluate() throws DException {
		DInstance instance;
		
		if (getContext() != null) {
			instance = getContext().getCurrentInstance();
		} else {
			instance = null;
		}
		
		if (lastUsedInstanceID != (instance == null ? Integer.MIN_VALUE : instance.getID())) {
			evaluated = false;
			evaluatedOnce = false;
			lastUsedInstanceID = (instance == null ? Integer.MIN_VALUE : instance.getID());
		}
		
		if (! evaluated) {
			try {
				parse();
				
				root.evaluate(instance);
				
				setResult(root);
			} catch (DException ex) {
				setError(ex);
				throw ex;
			}
		}
	}
	
	/**
	 * Parse current expression.
	 * @return Error code.
	 */
	protected void parse() throws DException {
		//int retVal;
		int iPos;		/* Expression index */

		GetTokenResult actToken;
		GetTokenResult nextToken;
		TokenBase tmpToken;
		int actTokenType;	/* Current token type (speed up a bit) */
		int lastTokenType;	/* Last token type */
		TokenBase tLastOp;	/* Last operator found */
		TokenBase tLastOpP; /* Last priority operator found */ //TODO: Investigare meglio
		TokenBase tFunc;	/* Last function found */
		Stack<TokenBase> parseStack;
		boolean isTerminal;	/* If last valid token is a terminal one */
		
		if (parsed) {
			//return errNumber;
			if (error != null) throw error;
			return; //Already parsed, no action needed.
		}

		//retVal = DResult.ERR_NONE;
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
				//actToken = nextToken; <- THIS IS BAD
				//actToken.error = nextToken.error;
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
							if (tLastOp==null) {
								root = tmpToken;
							} else {
								tLastOp.setRightChild(tmpToken);
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
									if (tmpToken instanceof TokenOperatorDice) {
										//tmpToken.setLeftChild(TokenValue.InitToken(1));
										root = tmpToken;
									} else if (tmpToken instanceof TokenOperatorAdd || tmpToken instanceof TokenOperatorSubtract) {
										//tmpToken.setLeftChild(TokenValue.InitToken(0));
										root = tmpToken;
									} else {
										//retVal = DResult.ERR_MISSING_OPERAND;
										//setError(retVal, actToken.begin, actToken.begin);
										throw new MissingOperand(actToken.begin);
									}
								} else {
									/* First operator */
									tmpToken.setLeftChild(root);
									root = tmpToken;
								}
								tLastOpP = tLastOp; //Same as "tLastOpP = null".
							} else {
								if (tmpToken.getPriority() > tLastOp.getPriority() || lastTokenType == TK_UOP) {
									tmpToken.setLeftChild(tLastOp.getRightChild());
									tLastOp.setRightChild(tmpToken);

									tLastOpP = tLastOp;
								} else {
									tmpToken.setLeftChild(tLastOp);
									if (tLastOpP==null) {
										root = tmpToken;
									} else {
										tLastOpP.setRightChild(tmpToken);
									}
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
								//retVal = DResult.ERR_UNBALANCED_PARENTHESYS;
								//setError(retVal, actToken.begin, actToken.begin);
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
									if (tFunc.nextChildNum()<=tFunc.getChildNumber()) {
										/* Error - too few parameters */
										//retVal = DResult.ERR_TOO_FEW_PARAMETERS;
										//setError(retVal, actToken.begin, actToken.begin);
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
								//retVal = DResult.ERR_EXPECTED_END_OF_STAT;
								//setError(retVal, actToken.begin, exp.length());
								throw new ExpectedEndOfStatement(actToken.begin);
							} else {
								tmpToken.setNextChild(root);
								if (tmpToken.nextChildNum()>tmpToken.getChildNumber()) {
									/* Error - too much parameters */
									//retVal = DResult.ERR_TOO_MANY_PARAMETERS;
									//setError(retVal, actToken.begin, actToken.begin);
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
									//retVal = DResult.ERR_UNKNOWN_FUNCTION;
									//setError(retVal, actToken.begin, actToken.end);
									throw new UnknownFunction(actToken.value, actToken.begin, actToken.end);
								}
							} else {
								//if (context.checkVariable(actToken.value)) {
								//	tmpToken = TokenValue.InitToken(actToken.value);
								tmpToken = TokenValue.InitToken(actToken.value, context);
								if (tmpToken != null) {
									if (tLastOp==null) {
										root = tmpToken;
									} else {
										tLastOp.setRightChild(tmpToken);
									}
									isTerminal = true;
								} else {
									//Error: variable is not defined
									//retVal = DResult.ERR_UNKNOWN_VARIABLE;
									//setError(retVal, actToken.begin, actToken.end);
									throw new UnknownVariable(actToken.value, actToken.begin, actToken.end);
								}
							}

					}
					lastTokenType=actToken.type;
				} else {
					/* Invalid token sequence */
					switch (lastTokenType) {
						case TK_VAL:
						case TK_PCL:
							//retVal = DResult.ERR_EXPECTED_END_OF_STAT;
							//setError(retVal, actToken.begin, exp.length());
							//break;
							throw new ExpectedEndOfStatement(actToken.begin);
						case TK_UOP:
						case TK_OP:
						case TK_POP:
						case TK_COM:
							//retVal = DResult.ERR_MISSING_OPERAND;
							//setError(retVal, actToken.begin, actToken.begin);
							//break;
							throw new MissingOperand(actToken.begin);
						default:
							//retVal = DResult.ERR_TOO_FEW_PARAMETERS;
							//setError(retVal, actToken.begin, actToken.begin);
							//break;
							throw new ExpectedParameter(actToken.begin);
					}
				}
			} else {
				/* can be an error or a parse end */
				//if (actToken.error != DResult.ERR_NONE) {
				//	/* Error from getToken */
				//	retVal = actToken.error;
				//	setError(retVal, actToken.begin, actToken.end);
				//} else {
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
				//}
			}
		} while (actToken.type!=TK_NULL /* && retVal==DResult.ERR_NONE */);
		
		parsed = true;

		//return retVal;		
	}

	/*
	 * Token riconosciuti:
	 * - Valore
	 * - Operatore
	 * - Variabile
	 * - Funzione/Variabile
	 * Pseoudo-token
	 * - Aperta Parentesi
	 * - Chiusa parentesi
	 * - Virgola (delimitatore di parametro)
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
	/** Pseudo-token: Parameter delimiter */
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
		//GetTokenResult retVal;
		String sExpr;
		int iCnt;	/* Counter and new token position */
		char actChar, nextChar;			/* Actual and next char */
		int actCharType, nextCharType;	/* Actual and next char type */
		int dotCount = 0; /* Used to check double dot in values */
		StringBuilder myValue = null; /* Used to optimize creation of values and names */
		
		//retVal = new GetTokenResult();
		//retVal.error = DResult.ERR_NONE;
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
						//retVal.error = DResult.ERR_INVALID_CHARACTER;
						//retVal.end = iCnt + 1;
						//break;
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
							//retVal.error = DResult.ERR_EXPECTED_END_OF_STAT;
							//retVal.end = iCnt + 1;
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
						/* Functions */
						
						//retVal.value = retVal.value + String.valueOf(actChar);
						if (myValue == null) {
							myValue = new StringBuilder();
						}
						myValue.append(actChar);
						
						if (nextCharType != CT_ALPHA) {
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
		
		//return retVal;
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
			//error = DResult.ERR_NONE;
			value = "";
			type = TK_NULL;
			begin = 0;
			end = 0;
		}

		/** Error code */
		//public int error = DResult.ERR_NONE;
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
