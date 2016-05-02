package org.pustefixframework.util;

public class LogUtils {
      
   public static String makeLogSafe(String str) {
	  if(str == null) {
	     str = "";
	  } else {
         if(str.length() >= 100) {
            str = str.substring(0, 100);
         }
         str = str.replace('\n', '_').replace('\r', '_').replace('|', '_');
      }
      return str;
   }

   public static String shortenClassName(Class<?> clazz, int maxLength) {
       String name = clazz.getSimpleName();
       Package pkg = clazz.getPackage();
       if(pkg == null) {
           return name;
       } else {
           String[] tokens = pkg.getName().split("\\.");
           boolean shortened = false;
           for(int i=tokens.length-1; i>-1; i--) {
               if(shortened || ( name.length() + tokens[i].length() + 1 + (i * 2) ) > maxLength) {
                   tokens[i] = tokens[i].substring(0, 1);
                   shortened = true;
               }
               name = tokens[i] + "." + name;
           }
       }
       return name;
   }

}
