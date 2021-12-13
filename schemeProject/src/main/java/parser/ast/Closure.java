package parser.ast;

import interpreter.ActivationRecord;

public class Closure implements Node {
	private ListNode arguments;
	private ListNode functionBody;
	private ActivationRecord parentARI;
	
	public Closure(ActivationRecord ARI, ListNode arguments, ListNode functionBody){
		this.parentARI = ARI;
		this.arguments = arguments;
		this.functionBody = functionBody;
	}
	public ActivationRecord getParentARI() {return parentARI;}
	public ListNode getArgs() {return arguments;}
	public ListNode getFuncBody() {return functionBody; }
	
	@Override
	public String toString() {
		return "#<Closure>";
	}
	
    @Override 
    public boolean equals(Object o) { 
	    if (this == o) 
	    	return true; 
	    if (!(o instanceof Closure)) 
	    	return false; 
	    Closure closure = (Closure) o; 
	    	return functionBody == closure.functionBody; 
    } 
}
