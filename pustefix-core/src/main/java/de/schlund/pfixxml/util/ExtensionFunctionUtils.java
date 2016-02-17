/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixxml.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pustefixframework.util.LogUtils;
import org.springframework.util.ClassUtils;

import de.schlund.pfixxml.RenderExtensionSaxon1;

/**
 * This class provides generic XSLT extension function support, 
 * which can be used by extension function implementors:
 * 
 * It supports thread local storage of extension function errors: 
 * due to an unfortunate implementation of Saxon's FunctionProxy 
 * only the exception message and no stacktrace or cause of an 
 * error, occurred within an extension function, is available in the 
 * resulting TransformerException. Using this class an extension
 * function can catch its exceptions and store it calling the method
 * setExtensionFunctionError. Later, during exception handling, the 
 * exception can be retrieved calling getExtensionFunctionError().
 * 
 * @author mleidig@schlund.de
 */
public class ExtensionFunctionUtils {

    private static ThreadLocal<Throwable> extFuncError=new ThreadLocal<>();
    private static ThreadLocal<Long> extFuncTime = new ThreadLocal<>();
    private static ThreadLocal<Map<Object, Object>> extFuncCache = new ThreadLocal<>();
    
    private static Logger LOG = Logger.getLogger(ExtensionFunctionUtils.class);
    
    public static void setExtensionFunctionError(Throwable t) {
        extFuncError.set(t);
    }
    
    public static Throwable getExtensionFunctionError() {
        return extFuncError.get();
    }
    
    public static Long getExtensionFunctionTime() {
        return extFuncTime.get();
    }
    
    public static void resetExtensionFunctionError() {
        extFuncError.set(null);
    }
    
    public static void resetExtensionFunctionTime() {
        extFuncTime.set(null);
    }
    
    public static Object invokeFunction(Method method, Object source, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        long t1 = 0, t2 = 0;
        if(LOG.isInfoEnabled()) {
            t1 = System.nanoTime();
        }
        try {
            return method.invoke(source, args);
        } finally {
            if(LOG.isInfoEnabled()) {
                t2 = System.nanoTime();
                Long totalTime = extFuncTime.get();
                if(totalTime == null) {
                    totalTime = new Long(t2 - t1);
                } else {
                    totalTime = totalTime + t2 - t1;
                }
                extFuncTime.set(totalTime);
            }
            if(LOG.isDebugEnabled()) {
                if(method.getDeclaringClass() != RenderExtensionSaxon1.class) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(method.getDeclaringClass().getName()).append(".");
                    sb.append(method.getName()).append("(");
                    for(int i=0; i<args.length; i++) {
                        if(args[i] == null) {
                            sb.append("null");
                        } else if(args[i].getClass() == String.class) {
                            sb.append("'").append(LogUtils.makeLogSafe(args[i].toString())).append("'");
                        } else if(ClassUtils.isPrimitiveOrWrapper(args[i].getClass())) {
                            sb.append(args[i].toString());
                        } else {
                            sb.append("<").append(args[i].getClass().getSimpleName()).append(">");
                        }
                        if(i<args.length-1) {
                            sb.append(",");
                        }
                    }
                    sb.append(") ");
                    String argStr = sb.toString();
                    if(argStr.length() > 130) {
                        argStr = argStr.substring(0, 127) + "...";
                    }
                    LOG.debug(String.format("%-130s %10.3fms", argStr, (float)(t2 - t1) / 1000000));
                }
            }
        }
    }

    public static Object getCacheValue(Object key) {
        Map<Object, Object> map = extFuncCache.get();
        if(map != null) {
            return map.get(key);
        }
        return null;
    }
    
    public static void setCacheValue(Object key, Object value) {
        Map<Object, Object> map = extFuncCache.get();
        if(map != null) {
            map.put(key, value);
        }
    }
    
    public static void initCache() {
        extFuncCache.set(new HashMap<Object, Object>());
    }
    
    public static void resetCache() {
        extFuncCache.set(null);
    }

}