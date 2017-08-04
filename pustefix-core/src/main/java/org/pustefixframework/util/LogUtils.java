package org.pustefixframework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
      
   private static Logger LOG = LoggerFactory.getLogger(LogUtils.class);

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

   public enum Level {

       TRACE,
       DEBUG,
       INFO,
       WARN,
       ERROR;

       public boolean isGreaterOrEqual(Level level) {
           return compareTo(level) >= 0;
       }

       public static Level toLevel(String level) {
           if(level != null) {
               try {
                   return Level.valueOf(level.toUpperCase());
               } catch(IllegalArgumentException x) {
                   LOG.warn("Log level not supported: " + level);
               }
           }
           return Level.DEBUG;
       }
   };

   public static Level getEffectiveLevel(Logger logger) {
       if(logger.isTraceEnabled()) {
           return Level.TRACE;
       } else if(logger.isDebugEnabled()) {
           return Level.DEBUG;
       } else if(logger.isInfoEnabled()) {
           return Level.INFO;
       } else if(logger.isWarnEnabled()) {
           return Level.WARN;
       }
       return Level.ERROR;
   }

   public static void log(Logger logger, Level level, String msg) {
       if(level == Level.ERROR) {
           logger.error(msg);
       } else if(level == Level.WARN) {
           logger.warn(msg);
       } else if(level == Level.INFO) {
           logger.info(msg);
       } else if(level == Level.DEBUG) {
           logger.debug(msg);
       } else if(level == Level.TRACE) {
           logger.trace(msg);
       }
   }

}
