package view;

public class ConsoleView extends View{
	
	public void printMsg(String args) {
		System.out.println(OUTPUT_PREFIX+args);
	}
	
	public void printErr(String args) {
		System.out.println(OUTPUT_PREFIX+args);
	}
}
