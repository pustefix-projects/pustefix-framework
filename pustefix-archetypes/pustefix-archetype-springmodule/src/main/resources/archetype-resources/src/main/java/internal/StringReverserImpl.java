#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.internal;

import org.apache.log4j.Logger;
import ${package}.StringReverser;

public class StringReverserImpl implements StringReverser {

	private static Logger logger = Logger.getLogger(StringReverserImpl.class);
	
	private String lastResult;
	
	public String reverse(String str) {
		logger.info("Receiveing string: " + str);
		char[] chars = str.toCharArray();
		StringBuffer buf = new StringBuffer();
		for (int i = chars.length - 1, x = 0; i >= 0; i--, x++) {
			buf.append(chars[i]);			
		}
		String reveresed = buf.toString();
		logger.info("Reterning string: " + reveresed);
		lastResult = reveresed;
		return reveresed;	
	}
	
	public String getLastResult() {
		System.out.println(this.getClass().getClassLoader().getClass().getName());
		System.out.println(this.getClass().getClassLoader());
		return lastResult;
	}
	
}
