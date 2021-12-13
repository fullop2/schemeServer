package parser.parse;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import parser.ast.*;

public class NodePrinter {
    private final String OUTPUT_FILENAME = "output07.txt";
    private StringBuffer sb = new StringBuffer();
    private Node root;

    public NodePrinter(Node root) {
        this.root = root;
    }

    private void printList(ListNode listNode) {
        if (listNode == ListNode.EMPTYLIST) {
            sb.append("( )");
            return;
        }
        if (listNode == ListNode.ENDLIST) {
        	return;
        }       
        if (listNode.car() instanceof QuoteNode) {
        	printNode(listNode.car());  	
        	Node nextNode = listNode.cdr().car();
        	
        	if (nextNode instanceof ListNode)
        		printList((ListNode)nextNode);
        	else
        		printNode(nextNode);
        }
        else 
        {
        	ListNode nextPrintList = listNode;
            sb.append("( ");
            while(nextPrintList != ListNode.ENDLIST) {
            	printNode(nextPrintList.car());
            	nextPrintList = nextPrintList.cdr();
            }
            printNode(nextPrintList.car());	
            sb.append(") ");
        }
    }

    private void printNode(Node node) {
        if (node == null)
            return;

        if (node instanceof ListNode) {
            printList((ListNode)node);
        }
        else { // one node
            sb.append(node + " ");
        }
    }
    
    public void prettyPrint() {
        printNode(root);

        try (FileWriter fw = new FileWriter(OUTPUT_FILENAME);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void prettyPrintConsole() {
        printNode(root);
        System.out.println(sb.toString().trim());
    }
    
    public String printedData() {
        printNode(root);
        return sb.toString();
    }
}
