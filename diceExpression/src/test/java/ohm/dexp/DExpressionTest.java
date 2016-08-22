package ohm.dexp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.regex.Pattern;

import ohm.dexp.DContext;
import ohm.dexp.DExpression;
import ohm.dexp.DResult;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.DParseException;
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
import ohm.dexp.function.TokenFunctionMax;
import ohm.dexp.function.TokenFunctionRandom;
import ohm.dexp.function.TokenFunctionRollAndKeep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DExpressionTest {

	@BeforeClass
	public static void Setup() {
		System.out.println("");
		System.out.println("Setup");

		initFunctions();
	}

//	@Test
//	public void testParse() throws Exception {
//
//	}

	@Test
	public void testParseUnaryOperators() {
		System.out.println("");
		System.out.println("testParseUnaryOperators");

		DContext dCtx = new DContext();

		testResult(".6*100", dCtx, 60);
		testResult("1.06*100", dCtx, 106);
		testResult("0.5555*10000", dCtx, 5550);
		testResult("1+2", dCtx, 3);
		testResult("1-2", dCtx, -1);
		testResult("+5", dCtx, 5);
		testResult("-5", dCtx, -5);
		testResult("3+(-5)", dCtx, 3+(-5));
		testResult("max(-3,-5)", dCtx, -3);

	}

	@Test
	public void testParseVariables() {
		System.out.println("");
		System.out.println("testParseVariables");

		DContext dCtx = new DContext();
		dCtx.setValue("Test", 0, 2, 1);
		testResult("Test+0", dCtx, 0, 0);
		testResult("test+0", dCtx, 0, 0);
		testResult("tesT+0", dCtx, 0, 0);
		testException("Tust+0", dCtx, UnknownVariable.class, 1);

		dCtx.setValue("A1", 0, 2, 1);
		testException("A1+0", dCtx, ExpectedEndOfStatement.class, 2);

		dCtx.setValue("AA1", 0, 2, 1);
		testResult("AA1+0", dCtx, 0, 0);

		dCtx.setValue("AA1A", 0, 2, 1);
		testResult("AA1A+0", dCtx, 0, 0);

//		System.out.println("A " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "A"));
//		System.out.println("A1 " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "A1"));
//		System.out.println("AA " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AA"));
//		System.out.println("AA1 " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AA1"));
//		System.out.println("AA1A " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AA1A"));
//		System.out.println("1AAA " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "1AAA"));
//		System.out.println("AAAAA " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AAAAA"));
//		System.out.println("AAAAAA " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AAAAAA"));
//		System.out.println("AaaA1 " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AAAA1"));
//		System.out.println("AB123 " + Pattern.matches("^[a-zA-Z]{2}[a-zA-Z0-9]{0,3}$", "AB123"));
	}

	@Test
	public void testParseFunctions() {
		System.out.println("");
		System.out.println("testParseFunctions");

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);

		testResult("3+rak(1d6,5,4)", dCtx, 7, 27);

	}

	@Test
	public void testParsePrecedence() {
		System.out.println("");
		System.out.println("testParsePrecedence");

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);

		testResult("1d6*2+6+4/2", dCtx, 10, 20);
		testResult("1+3*2d6", dCtx, 7, 37);
		testResult("1+1+3*2*3+1+2", dCtx, 1+1+3*2*3+1+2);
	}

	@Test
	public void testParseErrors() {
		System.out.println("");
		System.out.println("testParseErrors");

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);

		testException("+", dCtx, MissingOperand.class, 2);
		testException("*", dCtx, MissingOperand.class, 1);
		testException("3+", dCtx, MissingOperand.class, 3);
		testException("*5", dCtx, MissingOperand.class, 1);
		testException("(*5)", dCtx, MissingOperand.class, 2);
		testException("()", dCtx, MissingOperand.class, 2);
		testException("(", dCtx, UnbalancedBracket.class, 2);
		testException("3+5)", dCtx, UnbalancedBracket.class, 4);
		testException("(3+(5)))", dCtx, UnbalancedBracket.class, 8);
		testException("rak(1d6", dCtx, UnbalancedBracket.class, 8);
		testException("rak(1d6,", dCtx, UnbalancedBracket.class, 9);
		testException("rak(1d6)", dCtx, ExpectedParameter.class, 8);
		//TODO: Zero parameters functions are not supported
		//testException("rak()", dCtx, ExpectedParameter.class, 5);
		testException("rak(1d6,1,1,1d6)", dCtx, UnexpectedParameter.class, 12);
		testException("2+rUk(1d6,1,1,1d6)", dCtx, UnknownFunction.class, 3);
		testException("2+rUk", dCtx, UnknownVariable.class, 3);
		testException("2+5,6", dCtx, ExpectedEndOfStatement.class, 4);
		testException("2+(5,6)", dCtx, ExpectedEndOfStatement.class, 5);
		testException("2 5", dCtx, ExpectedEndOfStatement.class, 2);
		testException("rolls 3", dCtx, ExpectedEndOfStatement.class, 6);
		testException("", dCtx, NothingToEvaluate.class, 0);
		testException("#", dCtx, InvalidCharacter.class, 1);
		testException("2+3#", dCtx, InvalidCharacter.class, 4);
		testException("2+3.3.2", dCtx, InvalidCharacter.class, 6);
	}

	private void testResult(String formula, DContext dCtx, int fixedValue) {
		testResult(formula, dCtx, fixedValue, fixedValue);
	}

	private void testResult(String formula, DContext dCtx, int minValue, int maxValue) {
		DExpression dExp = new DExpression();
		dExp.setContext(dCtx);
		try {
			dExp.setExpression(formula);
			DResult res = dExp.getResult();

			printResult(dExp.getExpression(), res);

			assertEquals("Min value differ", minValue, res.getMinResult());
			if (minValue != maxValue) {
				assertEquals("Max value differ", maxValue, res.getMaxResult());
			}
		} catch (DException e) {
			e.printStackTrace();
			Assert.fail("Exception: " + e.getClass().getSimpleName());
		}
	}

	private void testException(String formula, DContext dCtx, Class exClass, int errorAt) {
		DExpression dExp = new DExpression();
		dExp.setContext(dCtx);
		try {
			dExp.setExpression(formula);
			printResult(dExp.getExpression(), dExp.getResult());

			Assert.fail("Exception not thrown.");
		} catch (DException ex) {
			if (! ex.getClass().equals(exClass)) {
				Assert.fail("Wrong exception: " + ex.getClass().getSimpleName());
			} else {
				if (ex instanceof DParseException) {
					assertEquals("Error position differ", errorAt, ((DParseException)ex).getFromChar());
				}
				printResult(dExp.getExpression(), ex);
			}
		}
	}

	private void printResult(String exp, DResult res) {
		System.out.print("\"" + exp + "\" -> ");
		System.out.print(res.getResult());
		System.out.print(" (");
		System.out.print(res.getMinResult());
		System.out.print("/");
		System.out.print(res.getMaxResult());
		System.out.print("); \"");
		System.out.print(res.getResultText());
		System.out.println("\"");
	}

	private void printResult(String exp, DException ex) {
		System.out.print("\"" + exp + "\" -> ");
		System.out.print(ex.getClass().getSimpleName());
		if (ex instanceof DParseException) {
			System.out.print("@");
			System.out.print(((DParseException)ex).getFromChar());
			System.out.print("-");
			System.out.print(((DParseException)ex).getToChar());
		}
		System.out.print(": ");
		System.out.print(ex.getMessage());
		System.out.println();
	}

	private static boolean initFnc = false;

	private static void initFunctions() {
		if (initFnc == false) {
			initFnc = true;
			addFunction("max", TokenFunctionMax.class);
			addFunction("rand", TokenFunctionRandom.class);
			addFunction("rak", TokenFunctionRollAndKeep.class);
		}
	}

	private static void addFunction(String token, Class<? extends TokenFunction> functionClass) {
		TokenFunction.addFunction(token, functionClass);

		//Used to get function IDS
		try {
			TokenFunction manager = functionClass.getConstructor().newInstance();
			System.out.println(token + ": " + manager.getType());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}