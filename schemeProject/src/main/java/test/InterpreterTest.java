package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import interpreter.CuteInterpreter;
import parser.ast.ListNode;
import parser.ast.Node;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;
import parser.parse.ParserMain;

public class InterpreterTest {
	private static CuteInterpreter cuteInterpreter;
	
	
	@BeforeClass
	public static void setUp() {
		cuteInterpreter = new CuteInterpreter();
	}
	
	private String computeString(String command) {
		CuteParser cuteParser = new CuteParser(command+"\n");
		Node parseTree = cuteParser.parseExpr();
		Node resultNode = cuteInterpreter.runExpr(parseTree);
		NodePrinter nodePrinter = new NodePrinter(resultNode);
		return nodePrinter.printedData().trim();
	}
	
	@Test
	public void tokenTest() {	
		assertEquals("( )", computeString("( )"));	
		assertEquals("( )", computeString("' ( )"));
		assertEquals("2", computeString("2"));
		assertEquals("2345", computeString("' 2345"));
		
		assertEquals("abc", computeString("'abc"));
		assertEquals("( a b 2 )", computeString("' ( a b 2 )"));
		assertEquals("( ' b 2 )", computeString("' ( ' b 2 )"));
		assertEquals("( ' ( a ) ' b 2 )", computeString("' ( ' ( a ) ' b 2 )"));
	}
	
	
	@Test
	public void otherTest() {
		assertEquals("'", computeString("( car ( car ' ( ' ( 2 3 ) ) ) )"));
		assertEquals("' ( 2 3 )", computeString("( car ' ( ' ( 2 3 ) ) )"));
		assertEquals("25", computeString("( * 5 5 )"));
	}

	@Test
	public void reverseTest() {
		assertEquals("( c b a )", computeString("( cons ( car ( cdr ( cdr ' ( a b c ) ) ) ) ( cons ( car ( cdr ' ( a b c ) ) ) ( cons ( car ' ( a b c ) ) '( ) ) ) )"));
	}

	@Test
	public void cadrTest() {
		assertEquals("a", computeString("( car ( car ' ( ( a b ) c ) ) )"));
		assertEquals("( c )", computeString("( cdr ' ( ( a b ) c ) )"));
		assertEquals("( b c )", computeString("( cdr ' ( a b c ) )"));
		assertEquals("b", computeString("( car ( cdr ' ( a b c ) ) )"));
	}

	@Test
	public void quoteTest() {
		assertEquals("tiger", computeString("' tiger"));
		assertEquals("12345", computeString("' 12345"));
		assertEquals("( )", computeString("' ( )"));
		assertEquals("( PLUS 2 3 )", computeString("' ( + 2 3 )"));
		assertEquals("( 2 3 )", computeString("' ( 2 3 )"));
		assertEquals("( ( PLUS 2 3 ) )", computeString("' ( ( + 2 3 ) )"));
		assertEquals("( 2 3 )", computeString("( cdr ( car ' ( ( + 2 3 ) 3 4 ) ) )"));
	}

	@Test
	public void complexTest() {
		assertEquals("( PLUS 2 3 )", computeString("( car ' ( ( + 2 3 ) 3 4 ) )"));
		assertEquals("( 3 4 )", computeString("( cdr ' ( ( + 2 3 ) 3 4 ) )"));
		assertEquals("( 2 3 )", computeString("( cdr ( car ' ( ( + 2 3 ) 3 4 ) ) )"));
	}	

	@Test
	public void carTest() {	
		assertEquals("( )", computeString("( car ' ( ( ) 3 4 ) )"));	
		assertEquals("2", computeString("( car ( car ' ( ( 2 3 ) 4 ) ) )"));
		assertEquals("2", computeString("( car ' ( 2 3 4 ) )"));
		assertEquals("2", computeString("( car ' ( 2 3 ) )"));
		assertEquals("( 2 3 )", computeString("( car ' ( ( 2 3 ) ( 4 5 ) 6 ) )"));

	}

	@Test
	public void cdrTest() {
		assertEquals( "( )", computeString("( cdr ' ( 2 ) )"));
		assertEquals("( ( 4 5 ) 6 )", computeString("( cdr ' ( ( 2 3 ) ( 4 5 ) 6 ) )"));
		assertEquals("( 3 4 )", computeString("( cdr ' ( 2 3 4 ) )"));
		assertEquals("( ( 4 ) )", computeString("( cdr ' ( ( 3 ) ( 4 ) ) )"));
		assertEquals("( 4 )", computeString("( cdr ( cdr ' ( 2 3 4 ) ) )"));
	}

	@Test
	public void consTest() {
		assertEquals("( 2 )", computeString("( cons 2 ' ( ) )"));
		assertEquals("( 1 2 3 4 )", computeString("( cons 1 ' ( 2 3 4 ) )"));	
		assertEquals("( A S )", computeString("( cons 'A ( cons 'S ' ( ) ) )"));
		assertEquals("( A S D F )", computeString("( cons 'A ( cons 'S ( cons 'D ( cons ' F ' ( ) ) ) ) )"));
		assertEquals("( ( A S ) D F )", computeString("( cons ( cons 'A ( cons 'S '( ) ) ) ( cons 'D ( cons ' F ' ( ) ) ) )"));
		assertEquals("( 1 2 3 4 )", computeString("( cons 1 ( cons 2 ( cons 3 ( cons 4 ' ( ) ) ) ) )"));
		assertEquals("( ( 2 3 ) 4 5 6 )", computeString("( cons ' ( 2 3 ) ' ( 4 5 6 ) )"));
		assertEquals("( ( 2 3 ) 4 ( 5 6 ) )", computeString("( cons ' ( 2 3 ) ' ( 4 ( 5 6 ) ) )"));
	}

	@Test
	public void atomTest() {
		assertEquals("#T", computeString("( atom? ( car ' ( 1 2 ) ) )"));
		assertEquals("#T", computeString("( atom? ' a )"));
		assertEquals("#F", computeString("( atom? ' ( 1 2 ) )"));
		assertEquals("#T", computeString("( atom? ' ( ) )"));
	}

	@Test
	public void nullTest() {
		assertEquals("#F", computeString("( null? ' a )"));
		assertEquals("#F", computeString("( null? ' ( 1 2 ) )"));
		assertEquals("#T", computeString("( null? ' ( ) )"));
	}

	@Test
	public void eqTest() {
		
		assertEquals("#T", computeString("( eq? ( null? '( ) ) #T )"));
		assertEquals("#T", computeString("( eq? ( + 1 2 ) ( - 4 1 ) )"));
		assertEquals("#T", computeString("( eq? 123 123 )"));
		assertEquals("#T", computeString("( eq? ' a ' a )"));
		assertEquals("#F", computeString("( eq? ' a ' b )"));
		assertEquals("#F", computeString("( eq? ' ( a b ) ' ( a b ) )"));
	}

	@Test
	public void arithmeticTest() {
		assertEquals("3", computeString("( + 1 2 )"));
		assertEquals("5", computeString("( + ( + 1 2 ) 2 )"));
		assertEquals("-1", computeString("( - 1 2 )"));
		assertEquals("40", computeString("( * 8 5 )"));
		assertEquals("27", computeString("( + 7 ( * 5 4 ) )"));
		assertEquals("21", computeString("( / ( * 15 10 ) ( + 2 5 ) )"));
	}

	@Test
	public void arithmeticSeqTest() {
		assertEquals("10", computeString("( + 1 2 3 4 )"));
		assertEquals("8", computeString("( + ( + 1 2 3 ) 2 )"));
		assertEquals("26", computeString("( + ( * 1 2 3 4 ) 2 )"));
		assertEquals("-8", computeString("( - 1 2 3 4  )"));
		assertEquals("32", computeString("( * 2 ( * 2 2 ) 2 2 )"));
		assertEquals("1", computeString("( / 32 2 2 2 2 2 )"));
	}
	

	@Test
	public void compareTest() {
		assertEquals("#F", computeString("( > 1 5 )"));
		assertEquals("#T", computeString("( > ( + 9 3 ) 1 )"));
		assertEquals("#T", computeString("( not #F )"));
		assertEquals("#F", computeString("( not ( < 1 2 ) )"));
	}
	

	@Test
	public void compareSeqTest() {
		assertEquals("#F", computeString("( > 1 5 4 2 1 )"));
		assertEquals("#T", computeString("( > ( + 9 3 ) 5 4 3 2 1 )"));
		assertEquals("#T", computeString("( not #F )"));
		assertEquals("#F", computeString("( not ( < 1 2 ) )"));
	}

	@Test
	public void condTest() {
		assertEquals("1", computeString("( cond ( ( > 1 2 ) 0 ) ( #T 1 ) )"));
		assertEquals("3", computeString("( cond ( #F 0 ) ( #T 3 ) )"));
		assertEquals("-90", computeString("( cond " + 
				"   ( ( > 5 6 ) ( + 1 2 )            ) " + 
				"   ( ( < 3 2 ) ( - 6 10 )           ) " + 
				"   ( ( = 2 2 ) ( - 16 ( + 100 6 ) ) ) " + 
				") "));
	}
}
