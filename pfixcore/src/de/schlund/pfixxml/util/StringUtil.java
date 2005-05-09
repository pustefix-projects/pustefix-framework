package de.schlund.pfixxml.util;

/**
 * Describe class StringUtil here.
 *
 *
 * Created: Tue Apr 19 11:37:01 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class StringUtil {

    public static String replaceAll(String input, String regexp, String replacement) {
        if (input == null) {
            return input;
        }
        return input.replaceAll(regexp, replacement);
    }

    public static String sign(String input, String secret) {
        String sign = MD5Utils.hex_md5(input + secret);
        return input + "SIGN=" + sign;
    }
    
}
