package interpreter;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.omg.CORBA.DynAnyPackage.TypeMismatch;

import inputProcessor.ConsoleInput;
import inputProcessor.Input;
import lexer.ScannerException;
import parser.ast.BinaryOpNode;
import parser.ast.BooleanNode;
import parser.ast.Closure;
import parser.ast.FunctionNode;
import parser.ast.IdNode;
import parser.ast.IntNode;
import parser.ast.ListNode;
import parser.ast.Node;
import parser.ast.QuoteNode;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;
import parser.parse.ParserMain;
import view.ConsoleView;
import view.View;

public class CuteInterpreter {
	
	private ActivationRecord ARI;
	private View view;
	private Input input;
	
	public CuteInterpreter() {
		ParserMain.class.getClassLoader();
		view = new ConsoleView();
		input = new ConsoleInput();
		ARI = new ActivationRecord(null);
	}
	
	public static void main(String[] args) {
		CuteInterpreter interpreter = new CuteInterpreter();
		interpreter.run();
	}
	
	public void run() {
		view.printMsg("");
		view.printMsg("Welcome to Scheme Console!");
		view.printMsg("Project From 2020 Spring Programming Language 101 Lecture.");
		view.printMsg("by 201602004 ParkTaehyun.");
		view.printMsg("");
		while(true) {
			try {
				String cmd = input.getInputData();
				if(cmd.equals("\n")) continue;
				CuteParser cuteParser = new CuteParser(cmd);
				Node parseTree = cuteParser.parseExpr();
				Node resultNode = runExpr(parseTree);
				NodePrinter nodePrinter = new NodePrinter(resultNode);
				String result = nodePrinter.printedData();
				if(!result.equals("")) view.printMsg(result);
			} catch(InputMismatchException e){ 
				view.printErr("Parameter Mismatch : " + e.getMessage());
			} catch(NoSuchElementException e){ 
				return;
			} catch(IllegalArgumentException e) {
				view.printErr("Illegal Token : " + e.getMessage());
			} catch(ScannerException e) {
				view.printErr("Illegal Token. token state transition failed" );
			} catch(Exception e) {
				view.printErr("Exception " + e.getMessage() );
			}
		}
	}
	
	public Node runExpr(Node rootExpr) {
		if(rootExpr == null)
			return null;
		if(rootExpr instanceof IdNode)
			if(((IdNode) rootExpr).isQuoted())
				return rootExpr;
			else
				return ARI.lookUptable(rootExpr);
		else if(rootExpr instanceof IntNode)
			return rootExpr;
		else if(rootExpr instanceof BooleanNode)
			return rootExpr;
		else if(rootExpr instanceof ListNode)
			return runList((ListNode)rootExpr);
		else
			throw new IllegalArgumentException(rootExpr.toString());
	}
	
	// functional car, cdr
	private ListNode listCar(ListNode listNode){
		return (ListNode)(listNode.car());
	}
	private ListNode cdr(ListNode listNode){
		return listNode.cdr();
	}
	private Node car(ListNode listNode){
		return listNode.car();
	}
	
	private int length(ListNode list) {
		if(list == ListNode.ENDLIST)
			return 0;
		return length(cdr(list)) + 1;
	}
	
	
	private Node runList(ListNode list) { 
		list = (ListNode)stripList(list); 
		
		if (list.equals(ListNode.EMPTYLIST) || list.equals(ListNode.ENDLIST)) {
			return ListNode.EMPTYLIST; 
		}
		else if (car(list) instanceof FunctionNode && !(list.isQuoted())) { 
			return runFunction((FunctionNode) list.car(), list.cdr());
		} 
		else if (car(list) instanceof BinaryOpNode && !(list.isQuoted())) { 
			return runBinary(list); 
		}
		/* Quoted List Process */
		Node nextNode = car(cdr(list));
		if(nextNode != null && (nextNode.equals(ListNode.ENDLIST) || nextNode.equals(ListNode.EMPTYLIST))) {
			return ListNode.EMPTYLIST;
		}
		else if (car(list) instanceof QuoteNode){
			return car(cdr(list));
		}
		else { // lambda
			return runLambda(list);
		}
	}
	
	private void updateARI(ListNode formalParams, ListNode actualParams) {
		while(car(formalParams) != null) {
			ARI.insertTable(car(formalParams), car(actualParams));
			formalParams = cdr(formalParams);
			actualParams = cdr(actualParams);
		}
	}
	
	private ListNode getArgs(ListNode list) {
		if(list.equals(ListNode.ENDLIST))
			return list;
		Node node = runExpr(car(list));
		
		return ListNode.cons(node, getArgs(cdr(list)));
	}
	
	private Node runLambda(ListNode list) {
		Closure lambdaExpression = null;
		
		if ( car(list) instanceof ListNode )
			lambdaExpression = (Closure)runList(listCar(list));
		else if ( car(list) instanceof IdNode ) { // is Closure
			lambdaExpression = (Closure)ARI.lookUptable(car(list));
		}
		else {
			throw new IllegalArgumentException(car(list).toString());
		}
			
		ListNode formalParams = lambdaExpression.getArgs();
		ListNode actualParams = getArgs(cdr(list));
		ListNode functionBody = lambdaExpression.getFuncBody();
		
		if(length(formalParams) > length(actualParams))
			throw new InputMismatchException(new NodePrinter(list).printedData());
		
		ActivationRecord newARI = new ActivationRecord(lambdaExpression.getParentARI());
		ActivationRecord dsrParentARI = ARI;
		ARI = newARI;
		
		updateARI(formalParams, actualParams);
		
		ListNode expr = listCar(functionBody);
		functionBody = cdr(functionBody);
		Node res = null;
		try {
			while(expr != ListNode.ENDLIST) {
				if((res = runList(expr)) != null) 
					break;
				expr = listCar(functionBody);
				functionBody = cdr(functionBody);
			}
		} catch(Exception e) {
			ARI = dsrParentARI;
			throw new IllegalArgumentException(new NodePrinter(((ListNode)expr)).printedData());
		}
		
		ARI = dsrParentARI;
		return res;
	}


	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.funcType) {
			case CAR: 
				return evalCar(operand);
			case CDR: 
				return evalCdr(operand);
			case CONS:
				return evalCons(operand);
			case NULL_Q:
				return evalNullQ(operand);
			case ATOM_Q:
				return evalAtomQ(operand);
			case EQ_Q:
				return evalEQQ(operand);
			case NOT:
				return evalNot(operand);
			case COND:
				return evalCond(operand);
			case DEFINE:
				return evalDefine(operand);
			case LAMBDA:
				return evalLambda(operand);
			default: 
				view.printErr("runFunction : undefined operator "+ operator);
				break;
			} 
		return null; 
	}
	
	private Node evalLambda(ListNode operand) {
		return new Closure(ARI, listCar(operand), cdr(operand) );
	}

	private Node evalDefine(ListNode operand)  throws NullPointerException, ClassCastException{
		Node carNode = car(operand);
		
		if(carNode instanceof IdNode) {
			Node cdrNode = runExpr(car(cdr(operand)));
			ARI.insertTable(carNode, cdrNode);
		}
		else
			throw new IllegalArgumentException("evalDefine : first argument is not IdNode");
		return null;
	}

	
	private Node evalCar(ListNode operand){
		Node res = car((ListNode)runExpr(car(operand)));
		if(res.equals(ListNode.ENDLIST))
			return ListNode.EMPTYLIST;
		return res;
	}
	
	private ListNode evalCdr(ListNode operand){
		ListNode res = cdr((ListNode)runExpr(car(operand)));
		if(res.equals(ListNode.ENDLIST))
			return ListNode.EMPTYLIST;
		return res;
	}
		
	private ListNode consRecursion(ListNode listNode) {
		Node nodeCar = listNode.car();
		if(nodeCar == null)
			return ListNode.ENDLIST;
		else if(nodeCar instanceof ListNode && !((ListNode)nodeCar).isQuoted())
			nodeCar = runExpr(nodeCar);
		return ListNode.cons(nodeCar, consRecursion(cdr(listNode)));
	}	
	
	private Node evalCons(ListNode operand) throws NullPointerException, ClassCastException{
		Node nodeCar = runExpr(car(operand));
		ListNode nodeCdr = (ListNode)runExpr(car(cdr(operand)));
		if(nodeCdr == ListNode.EMPTYLIST)
			nodeCdr = ListNode.ENDLIST;
		
		return ListNode.cons(nodeCar, consRecursion(nodeCdr) );
	}
	
	private Node evalNullQ(ListNode operand) throws NullPointerException{
		Node node = runExpr(car(operand));
		
		if(node.equals(ListNode.EMPTYLIST))
			return BooleanNode.TRUE_NODE;
		else
			return BooleanNode.FALSE_NODE;

	}

	private Node evalAtomQ(ListNode operand) throws NullPointerException, ClassCastException{
		Node node = runExpr(car(operand));
		
		if(node instanceof ListNode && !node.equals(ListNode.EMPTYLIST))
			return BooleanNode.FALSE_NODE;
		else
			return BooleanNode.TRUE_NODE;
	}	

	private Node evalEQQ(ListNode operand) throws NullPointerException{
		Node eval1 = runExpr(car(operand));
		Node eval2 = runExpr(car(cdr(operand)));
				
		if(eval1.equals(eval2))
			return BooleanNode.TRUE_NODE;
		else
			return BooleanNode.FALSE_NODE;
	}
	
	private Node evalNot(ListNode operand) throws NullPointerException {		
		Node node = runExpr(car(operand));
		if(!(node instanceof BooleanNode) || node.equals(BooleanNode.TRUE_NODE))
			return BooleanNode.FALSE_NODE;
		else 
			return BooleanNode.TRUE_NODE;
	}
	
	private Node evalCond(ListNode operand) throws NullPointerException{
		if(operand == ListNode.ENDLIST) {
			view.printErr("executeCond : No Matched condition");
			return null;
		}
		
		if(runExpr(car(listCar(operand))).equals(BooleanNode.TRUE_NODE))
			return runExpr(car(cdr(listCar(operand))));
		else
			return evalCond(cdr(operand));
		
	}
		
	private Node stripList(ListNode node) { 
		if (node.car() instanceof ListNode && node.cdr().car() == null) {
			Node listNode = node.car(); return listNode; 
		} 
		else {
			return node; 
		} 
	} 
	
	private Node runBinary(ListNode list) {
		BinaryOpNode operator = (BinaryOpNode) car(list);
		
		switch (operator.binType) {
			case PLUS: 
			case MINUS: 
			case TIMES: 
			case DIV:
				return evalArithmeticBinary(operator, (IntNode)runExpr(car(cdr(list))), cdr(cdr(list)));
			case LT:
			case GT:
			case EQ:
				return evalLogicBinary(operator, cdr(list));
			default: 
				return null; 
		}
	} 

	private BooleanNode evalLogicBinary(BinaryOpNode operator, ListNode list) {
		IntNode operand1 = null;
		IntNode operand2 = null;
		
		try {
			operand1 = (IntNode) runExpr(car(list));	
			
			if(cdr(list).equals(ListNode.ENDLIST)) {
				return BooleanNode.TRUE_NODE;
			}
			else {
				operand2 = (IntNode) runExpr(car(cdr(list)));
			}
		}
		catch(NullPointerException e) {
			view.printErr("runBinary : not available operand for binary operator");
		}
		catch(ClassCastException e) {
			view.printErr("runBinary : operands are not instance of IntNode");
		}
		boolean isTrue = false;
		switch (operator.binType) { 
			case LT:
				isTrue = (operand1.getValue() < operand2.getValue()) ;
				break;
			case GT:
				isTrue = (operand1.getValue() > operand2.getValue() ) ;
				break;
			case EQ:
				isTrue = (operand1.getValue().equals(operand2.getValue()) ) ;
				break;
		}
		
		if(isTrue)
			return evalLogicBinary(operator,cdr(list));
		else
			return BooleanNode.FALSE_NODE;
	}
	
	private IntNode evalArithmeticBinary(BinaryOpNode operator, IntNode accumulate, ListNode list) {	

		IntNode operand = null;	
		try {
			
			if(list.equals(ListNode.ENDLIST)) {
				return accumulate;
			}
			else {
				operand = (IntNode) runExpr(car(list));
			}
		}
		catch(NullPointerException e) {
			view.printErr("runBinary : not available operand for binary operator");
		}
		catch(ClassCastException e) {
			view.printErr("runBinary : operands are not instance of IntNode");
		}

		switch (operator.binType) {
			case PLUS: 
				accumulate = new IntNode(Integer.toString(accumulate.getValue() + operand.getValue() ));
				break;
			case MINUS: 
				accumulate = new IntNode(Integer.toString(accumulate.getValue() - operand.getValue() ));
				break;
			case TIMES: 
				accumulate = new IntNode(Integer.toString(accumulate.getValue() * operand.getValue() ));
				break;
			case DIV:
				accumulate = new IntNode(Integer.toString(accumulate.getValue() / operand.getValue() ));
				break;
		}
		return evalArithmeticBinary(operator,accumulate,cdr(list));
	}

}
