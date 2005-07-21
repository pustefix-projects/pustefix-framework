package de.schlund.pfixxml.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Created on 05.07.2004
 */

/**
 * @author Niels Schelbach
 * 05.07.2004
*/

public class MD5Utils {
    
    /*
     * Convert an array of little-endian words to a hex string.
     */
    public static String byteToHex(byte[] raw) {
        String       hex_tab = "0123456789abcdef";
        StringBuffer sb      = new StringBuffer();
        for(int i = 0; i < raw.length; i++) {
            byte b = raw[i];
            sb.append(hex_tab.charAt((b & 0xF0) >> 4));
            sb.append(hex_tab.charAt(b & 0xF));
        }
        return sb.toString();
    }
    
    public static String hex_md5(String message){ 
        String result = "";
        try {
            MessageDigest md  = MessageDigest.getInstance("MD5");
            byte[]        raw = md.digest(new String(message).getBytes());
            result            = byteToHex(raw);
        }
        catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
}
