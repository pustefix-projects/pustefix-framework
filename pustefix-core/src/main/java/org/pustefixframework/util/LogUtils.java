package org.pustefixframework.util;

public class LogUtils {
      
   public static String makeLogSafe(String str) {
      if(str != null) {
         if(str.length() >= 100) {
            str = str.substring(0, 100);
         }
         str = str.replace('\n', '_').replace('\r', '_').replace('|', '_');
      }
      return str;
   }

}
