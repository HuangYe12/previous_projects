package util;

public class StringUtil {
	private static final String NEWLINE = System.getProperty("line.separator");
	private StringUtil(){	}
	
	public static String appendNewLine(String text){
		return text + NEWLINE;
	}

}
