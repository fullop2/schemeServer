package view;

public abstract class View {

	protected static String OUTPUT_PREFIX = "¡¦ ";
	
	public abstract void printMsg(String args);	
	public abstract void printErr(String args);
}
