package de.schlund.pfixxml.util;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Category;

/*
 * Created on 05.07.2004
 */

/**
 * @author Niels Schelbach
 * 05.07.2004
 */
public class MD5Utils {
    
    private static Category logger = Category.getInstance(MD5Utils.class);
 
    
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_LATIN1 = "ISO-8859-1";
    
    /*
     * Convert an array of little-endian words to a hex string.
     */
    public static String byteToHex(byte[] raw)
    {
      String hex_tab = "0123456789abcdef";
      StringBuffer sb = new StringBuffer();
      for(int i=0;i<raw.length; i++)
      {
          byte b = raw[i];
          sb.append(hex_tab.charAt((b & 0xF0) >> 4));
          sb.append(hex_tab.charAt(b & 0xF));
      }
      return sb.toString();
    }
    
    public static String hex_md5(String message){ 
        return hex_md5(message,CHARSET_UTF8);
    }
    
    
    public static String hex_md5(String message, String charset){ 
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] raw = md.digest(new String(message).getBytes(charset));
            result = byteToHex(raw);
        }
        catch (NoSuchAlgorithmException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("No Such Algorithm",ex);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("Unsupported Charset",ex);
        }
        return result;
    }
    
}
