package interpreter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import parser.ast.Closure;
import parser.ast.ListNode;
import parser.ast.Node;

public class ActivationRecord{

	private Map<String, Node> varTable;
	private ActivationRecord parentARI;
	
	public ActivationRecord(ActivationRecord parentARI) {
		varTable = new HashMap<String,Node>();	
		this.parentARI = parentARI;
	}
	
	public Node insertTable(Node keyNode, Node valueNode) {
		return varTable.put(keyNode.toString(), valueNode);
	}

	public Node lookUptable(Node keyNode) throws IllegalArgumentException {
		Node node = varTable.get(keyNode.toString());
		if(node == null) {
			if(parentARI != null)
				return parentARI.lookUptable(keyNode);
			else 
				throw new IllegalArgumentException(keyNode.toString());
		}
		return node;
	}
	
	public ActivationRecord getParentARI() {
		return parentARI;
	}
}
