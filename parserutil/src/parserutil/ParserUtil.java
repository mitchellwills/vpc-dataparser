package parserutil;

public class ParserUtil {

	public static String trimKey(String s){
		s = s.replace('\u00a0',' ').trim();
		if(s.endsWith(":"))
			return s.substring(0, s.length()-1);
		return s;
	}
	public static String trimValue(String s){
		return s.replace('\u00a0',' ').trim();
	}
}
