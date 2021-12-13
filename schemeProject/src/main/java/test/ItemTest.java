package test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import interpreter.CuteInterpreter;
import parser.ast.Node;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;
import parser.parse.ParserMain;

public class ItemTest {
	
	CuteInterpreter cuteInterpreter;
	// Invisible Variable
	private static final String IllegalArgException = "java.lang.IllegalArgumentException"; 
	
	@Before
	public void initInterpreter() {
		cuteInterpreter = new CuteInterpreter();
	}
	
	private String computeString(String command) {
		try {
			if(command.equals("")) return "";
			CuteParser cuteParser = new CuteParser(command+"\n");
			Node parseTree = cuteParser.parseExpr();
			Node resultNode = cuteInterpreter.runExpr(parseTree);
			NodePrinter nodePrinter = new NodePrinter(resultNode);
			return nodePrinter.printedData().trim();
		} catch(Exception e) {
			return e.getClass().getName();
		}
	}
	
	/* Item 2 */
	@Test
	public void varDefineTest() {	
		assertEquals("", computeString("( define x 1 )"));	
		assertEquals("1", computeString("x"));
		assertEquals("", computeString("( define x ( + 1 2 ) )"));
		assertEquals("3", computeString("x"));
		assertEquals("", computeString("( define y ( + x 3 ) )"));
		assertEquals("6", computeString("y"));
	}
	
	/* Item 3 */
	@Test
	public void lambdaTest() {
		assertEquals("2", computeString("( ( lambda ( x ) ( + x 1 ) ) 1 )"));
		assertEquals("10", computeString("( ( lambda ( x ) ( + ( * x 3 ) 1 ) ) 3 )"));
		assertEquals("13", computeString("( ( lambda ( x z ) ( + ( * x z ) 1 ) ) 3 4 )"));
	}
	
	@Test
	public void funcDefineAndCallTest() {
		assertEquals("", computeString("( define func ( lambda ( x ) ( + x 1 ) ) )"));
		assertEquals("", computeString("( define func2 ( lambda ( x y ) ( * 2 x y ) ) )"));
		assertEquals("", computeString("( define func3 ( lambda ( x y ) ( * 2 y ( func x ) ) ) )"));
		assertEquals("16", computeString("( func2 4 2 )"));
		assertEquals("2", computeString("( func 1 )"));
		assertEquals("30", computeString("( func3 4 3 )"));
	}
	
	@Test
	public void localVarTest() {
		assertEquals("", computeString("( define func ( lambda ( x ) ( define y 3 ) ( + x y ) ) )"));
		assertEquals("6", computeString("( func 3 )"));
		assertEquals("#<Closure>", computeString("func"));
		assertEquals(IllegalArgException, computeString("y"));
		assertEquals(IllegalArgException, computeString("x"));
	}
	
	@Test
	public void nestedFuncTest() {
		assertEquals("", computeString("( define func ( lambda ( y ) ( define func2 ( lambda ( y ) ( * y 3 ) ) ) ( + 3 ( func2 y ) ) ) )"));
		assertEquals("12", computeString("( func 3 )"));
		assertEquals(IllegalArgException, computeString("y"));
		assertEquals(IllegalArgException, computeString("func2"));
	}
	
	@Test
	public void recursionFuncTest() {
		assertEquals("", computeString("( define lastitem ( lambda ( ls ) ( cond ( ( null? ( cdr ls ) ) ( car ls ) ) ( #T ( lastitem ( cdr ls ) ) ) ) ) )"));
		assertEquals("4", computeString("( lastitem ' ( 1 2 3 4 ) )"));
		assertEquals("asdf", computeString("( lastitem ' ( 1 2 3 asdf ) )"));
		assertEquals("( df )", computeString("( lastitem ' ( 1 2 as ( df ) ) )"));
		assertEquals("( a ( 4 ) )", computeString("( lastitem ' ( 1 2 ( a ( 4 ) ) ) )"));
		
		assertEquals(IllegalArgException, computeString("ls"));
	}
	
	
	@Test
	public void firstObjectFunctionTest() {
		assertEquals("", computeString("( define compose ( lambda ( func1 func2 x ) ( func1 ( func2 x ) ) ) )"));
		assertEquals("", computeString("( define f ( lambda ( x ) ( + x 1 ) ) )"));
		assertEquals("", computeString("( define g ( lambda ( x ) ( * x 2 ) ) )"));
		assertEquals("3", computeString("( compose f g 1 )"));
		
		assertEquals(IllegalArgException, computeString("x"));
	}
}
