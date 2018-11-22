package org.pustefixframework.util.javascript;

import org.owasp.encoder.Encode;

@Deprecated
public class JSUtils {

	/**
	 * @deprecated Will be removed in Pustefix 1.0, use {@link org.owasp.encoder.Encode#forJavaScript()} instead.
	 */
	@Deprecated
	public static String escape(String input) {
	    return Encode.forJavaScript(input);
	}

}