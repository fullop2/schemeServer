package inputProcessor;

import java.util.Scanner;

public class ConsoleInput implements Input {
	private Scanner s = new Scanner(System.in); 
	private static String INPUT_PREFIX = "> ";
	
	private static void printPrefix() {
		System.out.print(INPUT_PREFIX);
	}
	@Override
	public String getInputData() {
		printPrefix();
		return s.nextLine() + '\n';
	}
}
