
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import ohm.dexp.DContext;
import ohm.dexp.DExpression;
import ohm.dexp.DResult;
import ohm.dexp.exception.DException;
import ohm.dexp.function.TokenFunction;
import ohm.dexp.function.TokenFunctionMax;
import ohm.dexp.function.TokenFunctionRandom;
import ohm.dexp.function.TokenFunctionRollAndKeep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DExpressionTest {

	@Test
	public void UnaryTest() {
		initFunctions();

		DExpression dExp = new DExpression();
		dExp.setContext(new DContext());
		try {
			dExp.setExpression("1+2");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(3, dExp.getResult().getResult());

			dExp.setExpression("1-2");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(-1, dExp.getResult().getResult());

			dExp.setExpression("+5");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(5, dExp.getResult().getResult());

			dExp.setExpression("-5");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(-5, dExp.getResult().getResult());

			dExp.setExpression("3+(-5)");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(-2, dExp.getResult().getResult());

			dExp.setExpression("max(-3,-5)");
			printResult(dExp.getExpression(), dExp.getResult());
			assertEquals(-3, dExp.getResult().getResult());

		} catch (DException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void FunctionTest() {
		initFunctions();

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);
		DExpression dExp = new DExpression();
		dExp.setContext(dCtx);
		try {
			dExp.setExpression("3+rak(1d6,5,4)");
			printResult(dExp.getExpression(), dExp.getResult());

//			dExp.setExpression("teSt= 1d6");
//			printResult(dExp.getExpression(), dExp.getResult());
//
//			dExp.setExpression("teSt= 1d6:\"Test\"+6");
//			printResult(dExp.getExpression(), dExp.getResult());
		} catch (DException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void PrecedenceTest() {
		initFunctions();

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);
		DExpression dExp = new DExpression();
		dExp.setContext(dCtx);
		try {
			dExp.setExpression("1d6*2+6+4/2");
			printResult(dExp.getExpression(), dExp.getResult());
			dExp.setExpression("1+3*2d6");
			printResult(dExp.getExpression(), dExp.getResult());
			dExp.setExpression("1+1+3*2*3+1+2");
			printResult(dExp.getExpression(), dExp.getResult());
			Assert.assertEquals(1+1+3*2*3+1+2, dExp.getResult().getResult());
		} catch (DException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void ErrorTest() {
		initFunctions();

		DContext dCtx = new DContext();
		dCtx.setValue("rolls", 0, 1, 1);
		DExpression dExp = new DExpression();
		dExp.setContext(dCtx);
		try {
			dExp.setExpression("3+");
			printResult(dExp.getExpression(), dExp.getResult());

//			dExp.setExpression("teSt= 1d6");
//			printResult(dExp.getExpression(), dExp.getResult());
//
//			dExp.setExpression("teSt= 1d6:\"Test\"+6");
//			printResult(dExp.getExpression(), dExp.getResult());
			Assert.fail();
		} catch (DException e) {
			e.printStackTrace();
		}
	}

	private void printResult(String exp, DResult res) {
		System.out.print("\"" + exp + "\"->");
		System.out.print(res.getMinResult());
		System.out.print("<=");
		System.out.print(res.getResult());
		System.out.print("<=");
		System.out.print(res.getMaxResult());
		System.out.print(";\"");
		System.out.print(res.getResultText());
		System.out.println("\"");
	}

	private static boolean initFnc = false;

	private void initFunctions() {
		if (initFnc == false) {
			initFnc = true;
			addFunction("max", TokenFunctionMax.class);
			addFunction("rand", TokenFunctionRandom.class);
			addFunction("rak", TokenFunctionRollAndKeep.class);
		}
	}

	private void addFunction(String token, Class<? extends TokenFunction> functionClass) {
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