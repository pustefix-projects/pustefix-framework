package org.pustefixframework.util.javascript;

public class JSUtils {
	
	private static final String[] HEXTAB = new String[256];

	static {
		for(char ch=0; ch <= 0xFF; ch++) {
			if(ch > 0x2F && ch < 0x3A || ch > 0x40 && ch < 0x5B ||	ch > 0x60 && ch < 0x7B) {
				HEXTAB[ch] = null;
			} else {
				HEXTAB[ch] = "\\x" + (ch < 0x10 ? "0" : "") + Integer.toHexString(ch).toUpperCase();
			}
		}
		HEXTAB[','] = null;
		HEXTAB['.'] = null;
		HEXTAB['_'] = null;
	}
  
	public static String escape(String input) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<input.length(); i++) {
			char ch = input.charAt(i);
			if(ch <= 0xFF) {
				String hex = HEXTAB[ch];
				if(hex == null) {
					sb.append(ch);
				} else {
					sb.append(hex);
				}
			} else {
				String hex = Integer.toHexString(ch).toUpperCase();
				sb.append("\\u");
				for(int p=0; p < 4-hex.length(); p++) {
					sb.append("0");
				}
				sb.append(hex);
			}
		}
		return sb.toString();
	}

}