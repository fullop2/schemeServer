package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import parser.ast.*;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node() {};
	
	public CuteParser(File file) {
		try {
			tokens = Scanner.scan(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public CuteParser(String string) {
		tokens = Scanner.scan(string);
	}
	
	private Token getNextToken() {
		if (!tokens.hasNext())
			return null;
		return tokens.next();
	}

	public Node parseExpr() {
		Token t = null;
		try {
			t = getNextToken();
		}
		catch(Exception e) {
			return null;
		}
		if (t == null) {
			System.err.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();

		switch (tType) {
		case ID:
			return new IdNode(tLexeme);
		case INT:
			if (tLexeme == null)
				System.out.println("???");
			return new IntNode(tLexeme);
		// BinaryOpNode에 대하여 작성
		// +, -, /, *가 해당
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:
			return new BinaryOpNode(tType);

		// FunctionNode에 대하여 작성
		// 키워드가 FunctionNode에 해당
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:
			return new FunctionNode(tType);

		// BooleanNode에 대하여 작성
		case FALSE:
			return BooleanNode.FALSE_NODE;
		case TRUE:
			return BooleanNode.TRUE_NODE;

		// case L_PAREN일 경우와 case R_PAREN일 경우에 대해서 작성
		// L_PAREN일 경우 parseExprList()를 호출하여 처리
		case L_PAREN:
			return parseExprList();
		case R_PAREN:
			return END_OF_LIST;
		case APOSTROPHE:
			QuoteNode quoteNode = new QuoteNode();
			Node nextNode = parseExpr();
			if(nextNode instanceof ListNode) {
				((ListNode) nextNode).setQuotedIn();
			}
			else if(nextNode instanceof QuotableNode) {
				((QuotableNode) nextNode).setQuoted();
			}
			/* 
			 * case one node
			 * innerList = [ nextNode = a ListNode.ENDLIST ]
			 * 
			 * case List
			 * innerList = [ nextNode = [ 1 [2 ... [...] ] ] ListNode.ENDLIST ]
			 */
			ListNode innerList = ListNode.cons(nextNode, ListNode.ENDLIST);
			ListNode listNodeWithQuoteNode = ListNode.cons(quoteNode, innerList);
			return listNodeWithQuoteNode;
			
		default:
			// head의 next를 만들고 head를 반환하도록 작성
			System.out.println("Parsing Error!");
			return null;
		}

	}

	private ListNode parseExprList() {
		Node head = parseExpr();
		if (head == null) // if next token is RPAREN
			return null;
		if (head == END_OF_LIST)
			return ListNode.ENDLIST;		
		ListNode tail = parseExprList();
		if(tail == null)
			return null;
		return ListNode.cons(head, tail);
	}
}
