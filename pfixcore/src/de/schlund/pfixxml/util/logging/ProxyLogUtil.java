/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.util.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility class used by ProxyLogObject and ProxyLogAppender.
 * Does the actual logging work by selecting and 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProxyLogUtil {
    private Class clFactoryClass = null;

    private Object clFactoryObject = null;

    private Method mClGetInstance;

    private ServletContext servletContext = null;

    private Map<String, Object> clLoggerCache = Collections.synchronizedMap(new HashMap<String, Object>());

    private Map<String, Object> l4jLoggerCache = Collections.synchronizedMap(new HashMap<String, Object>());

    // log4j reflection stuff
    private Method mLog4jTrace;

    private Method mLog4jTraceWithEx;

    private Method mLog4jIsTrace;

    private Method mLog4jDebug;

    private Method mLog4jDebugWithEx;

    private Method mLog4jIsDebug;

    private Method mLog4jInfo;

    private Method mLog4jInfoWithEx;

    private Method mLog4jIsInfo;

    private Method mLog4jWarn;

    private Method mLog4jWarnWithEx;

    private Method mLog4jError;

    private Method mLog4jErrorWithEx;

    private Method mLog4jFatal;

    private Method mLog4jFatalWithEx;

    private Method mLog4jGetLogger;

    private Method mLog4jIsEnabled;

    private Object log4jPriorityWarn;

    private Object log4jPriorityError;

    private Object log4jPriorityFatal;

    // commons-logging reflection stuff
    private Method mClTrace;

    private Method mClTraceWithEx;

    private Method mClIsTrace;

    private Method mClDebug;

    private Method mClDebugWithEx;

    private Method mClIsDebug;

    private Method mClInfo;

    private Method mClInfoWithEx;

    private Method mClIsInfo;

    private Method mClWarn;

    private Method mClWarnWithEx;

    private Method mClIsWarn;

    private Method mClError;

    private Method mClErrorWithEx;

    private Method mClIsError;

    private Method mClFatal;

    private Method mClFatalWithEx;

    private Method mClIsFatal;
    
    // Generic stuff
    
    private boolean haveCl = false;
    
    private boolean haveLog4j = false;

    private static ProxyLogUtil instance = new ProxyLogUtil();

    public static ProxyLogUtil getInstance() {
        return instance;
    }

    private ProxyLogUtil() {
        ClassLoader clLoader = checkForCl();
        ClassLoader log4jLoader = checkForLog4j();
        
        if (clLoader != null) {
            configureForCommonsLogging(clLoader);
        }
        
        if (log4jLoader != null) {
            configureForLog4j(log4jLoader);
        }
    }
    
    /**
     * Sets the servlet context to use for logging when neither
     * log4j nor commons-logging are available.
     * 
     * @param context ServletContext to send log messages to
     */
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }

    private ClassLoader checkForCl() {
        // Look for commons-logging in container
        ClassLoader webappLoader = LogFactory.class.getClassLoader();
        ClassLoader cl = webappLoader.getParent();
        while (cl != null) {
            Class temp;
            try {
                temp = cl.loadClass(LogFactory.class.getName());
            } catch (ClassNotFoundException e) {
                cl = cl.getParent();
                continue;
            }
            if (temp != LogFactory.class) {
                break;
            }
            cl = cl.getParent();
        }
        return cl;
    }

    private ClassLoader checkForLog4j() {
        // Look for log4j in container        
        ClassLoader webappLoader = Logger.class.getClassLoader();
        ClassLoader cl = webappLoader.getParent();
        while (cl != null) {
            Class temp;
            try {
                temp = cl.loadClass(Logger.class.getName());
            } catch (ClassNotFoundException e) {
                cl = cl.getParent();
                continue;
            }
            if (temp != Logger.class) {
                break;
            }
            cl = cl.getParent();
        }
        return cl;
    }
    
    /**
     * Configures log4j to log through this factory. Should only be used
     * if log4j is not configured by other code.
     */
    public void configureLog4jProxy() {
        // Configure log4j to log through this utility class
        Properties proptemp = new Properties();
        proptemp.setProperty("log4j.rootLogger", "ALL, proxy");
        proptemp.setProperty("log4j.appender.proxy", ProxyLogAppender.class.getName());
        PropertyConfigurator.configure(proptemp);
    }

    private void configureForLog4j(ClassLoader classloader) {
        try {
            // Make sure all work inside parent log4j is done using its own
            // classloader (static initializations here)
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);

            Class log4jLogger;
            Class log4jPriority;
            try {
                log4jLogger = classloader.loadClass(Logger.class.getName());
                log4jPriority = classloader.loadClass(Priority.class.getName());
            } finally {
                // Set the old context classloader again
                Thread.currentThread().setContextClassLoader(ctxLoader);
            }

            mLog4jGetLogger = log4jLogger.getMethod("getLogger", new Class[] { String.class });

            mLog4jDebug = log4jLogger.getMethod("debug", new Class[] { Object.class });
            mLog4jInfo = log4jLogger.getMethod("info", new Class[] { Object.class });
            mLog4jWarn = log4jLogger.getMethod("warn", new Class[] { Object.class });
            mLog4jError = log4jLogger.getMethod("error", new Class[] { Object.class });
            mLog4jFatal = log4jLogger.getMethod("fatal", new Class[] { Object.class });
            mLog4jDebugWithEx = log4jLogger.getMethod("debug", new Class[] { Object.class, Throwable.class });
            mLog4jInfoWithEx = log4jLogger.getMethod("info", new Class[] { Object.class, Throwable.class });
            mLog4jWarnWithEx = log4jLogger.getMethod("warn", new Class[] { Object.class, Throwable.class });
            mLog4jErrorWithEx = log4jLogger.getMethod("error", new Class[] { Object.class, Throwable.class });
            mLog4jFatalWithEx = log4jLogger.getMethod("fatal", new Class[] { Object.class, Throwable.class });
            mLog4jIsDebug = log4jLogger.getMethod("isDebugEnabled", new Class[0]);
            mLog4jIsInfo = log4jLogger.getMethod("isInfoEnabled", new Class[0]);

            // Log4j has no convenience methods for this priorities
            log4jPriorityWarn = log4jPriority.getField("WARN").get(null);
            log4jPriorityError = log4jPriority.getField("ERROR").get(null);
            log4jPriorityFatal = log4jPriority.getField("FATAL").get(null);
            mLog4jIsEnabled = log4jLogger.getMethod("isEnabledFor", new Class[] { log4jPriority });

            try {
                mLog4jTrace = log4jLogger.getMethod("trace", new Class[] { Object.class });
                mLog4jTraceWithEx = log4jLogger.getMethod("trace", new Class[] { Object.class, Throwable.class });
                mLog4jIsTrace = log4jLogger.getMethod("isTraceEnabled", new Class[0]);
            } catch (NoSuchMethodException e) {
                // trace() is not implemented in log4j <1.2.12
                mLog4jTrace = mLog4jDebug;
                mLog4jTraceWithEx = mLog4jDebugWithEx;
                mLog4jIsTrace = mLog4jIsDebug;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        haveLog4j = true;
    }

    private void configureForCommonsLogging(ClassLoader classloader) {
        try {
            // Make sure all work inside parent log4j is done using its own
            // classloader (static initializations here)
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);

            Class logClass;
            try {
                clFactoryClass = classloader.loadClass(LogFactory.class.getName());
                logClass = classloader.loadClass(Log.class.getName());
                Method clGetFactory = clFactoryClass.getMethod("getFactory", new Class[0]);
                clFactoryObject = clGetFactory.invoke(null, new Object[0]);
            } finally {
                // Set the old context classloader again
                Thread.currentThread().setContextClassLoader(ctxLoader);
            }

            mClGetInstance = clFactoryClass.getMethod("getInstance", new Class[] { String.class });
            mClTrace = logClass.getMethod("trace", new Class[] { Object.class });
            mClDebug = logClass.getMethod("debug", new Class[] { Object.class });
            mClInfo = logClass.getMethod("info", new Class[] { Object.class });
            mClWarn = logClass.getMethod("warn", new Class[] { Object.class });
            mClError = logClass.getMethod("error", new Class[] { Object.class });
            mClFatal = logClass.getMethod("fatal", new Class[] { Object.class });
            mClTraceWithEx = logClass.getMethod("trace", new Class[] { Object.class, Throwable.class });
            mClDebugWithEx = logClass.getMethod("debug", new Class[] { Object.class, Throwable.class });
            mClInfoWithEx = logClass.getMethod("info", new Class[] { Object.class, Throwable.class });
            mClWarnWithEx = logClass.getMethod("warn", new Class[] { Object.class, Throwable.class });
            mClErrorWithEx = logClass.getMethod("error", new Class[] { Object.class, Throwable.class });
            mClFatalWithEx = logClass.getMethod("fatal", new Class[] { Object.class, Throwable.class });
            mClIsTrace = logClass.getMethod("isTraceEnabled", new Class[0]);
            mClIsDebug = logClass.getMethod("isDebugEnabled", new Class[0]);
            mClIsInfo = logClass.getMethod("isInfoEnabled", new Class[0]);
            mClIsWarn = logClass.getMethod("isWarnEnabled", new Class[0]);
            mClIsError = logClass.getMethod("isErrorEnabled", new Class[0]);
            mClIsFatal = logClass.getMethod("isFatalEnabled", new Class[0]);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        haveCl = true;
    }

    void doLogGeneric(String name, Level level, Object msg) {
        if (haveLog4j) {
            doLogLog4j(name, level, msg);
        } else if (haveCl) {
            doLogCl(name, level, msg);
        } else {
            if (servletContext != null && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL)) {
                servletContext.log("[" + level + "] " + name + ": " + msg);
            }
        }
    }

    void doLogGeneric(String name, Level level, Object msg, Throwable ex) {
        if (haveLog4j) {
            doLogLog4j(name, level, msg, ex);
        } else if (haveCl) {
            doLogCl(name, level, msg, ex);
        } else {
            if (servletContext != null && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL)) {
                servletContext.log("[" + level + "] " + name + ": " + msg, ex);
            }
        }
    }

    boolean doCheckEnabledGeneric(String name, Level level) {
        if (haveLog4j) {
            return doCheckEnabledLog4j(name, level);
        } else if (haveCl) {
            return doCheckEnabledCl(name, level);
        } else {
            if (level == Level.WARN || level == Level.ERROR || level == Level.FATAL) {
                return true;
            } else {
                return false;
            }
        }
    }

    void doLogLog4j(String name, Level level, Object msg) {
        if (!haveLog4j) {
            doLogGeneric(name, level, msg);
            return;
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mLog4jTrace.getDeclaringClass().getClassLoader());

        Object logObj = getLog4jLogger(name);
        try {
            if (level == Level.TRACE) {
                mLog4jTrace.invoke(logObj, new Object[] { msg });
            } else if (level == Level.DEBUG) {
                mLog4jDebug.invoke(logObj, new Object[] { msg });
            } else if (level == Level.INFO) {
                mLog4jInfo.invoke(logObj, new Object[] { msg });
            } else if (level == Level.WARN) {
                mLog4jWarn.invoke(logObj, new Object[] { msg });
            } else if (level == Level.ERROR) {
                mLog4jError.invoke(logObj, new Object[] { msg });
            } else if (level == Level.FATAL) {
                mLog4jFatal.invoke(logObj, new Object[] { msg });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    void doLogLog4j(String name, Level level, Object msg, Throwable ex) {
        if (!haveLog4j) {
            doLogGeneric(name, level, msg, ex);
            return;
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mLog4jTraceWithEx.getDeclaringClass().getClassLoader());

        Object logObj = getLog4jLogger(name);
        try {
            if (level == Level.TRACE) {
                mLog4jTraceWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.DEBUG) {
                mLog4jDebugWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.INFO) {
                mLog4jInfoWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.WARN) {
                mLog4jWarnWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.ERROR) {
                mLog4jErrorWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.FATAL) {
                mLog4jFatalWithEx.invoke(logObj, new Object[] { msg, ex });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    boolean doCheckEnabledLog4j(String name, Level level) {
        if (!haveLog4j) {
            return doCheckEnabledGeneric(name, level);
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mLog4jIsTrace.getDeclaringClass().getClassLoader());

        Object retval = null;
        Object logObj = getLog4jLogger(name);
        try {
            if (level == Level.TRACE) {
                retval = mLog4jIsTrace.invoke(logObj, new Object[0]);
            } else if (level == Level.DEBUG) {
                retval = mLog4jIsDebug.invoke(logObj, new Object[0]);
            } else if (level == Level.INFO) {
                retval = mLog4jIsInfo.invoke(logObj, new Object[0]);
            } else if (level == Level.WARN) {
                retval = mLog4jIsEnabled.invoke(logObj, new Object[] { log4jPriorityWarn });
            } else if (level == Level.ERROR) {
                retval = mLog4jIsEnabled.invoke(logObj, new Object[] { log4jPriorityError });
            } else if (level == Level.FATAL) {
                retval = mLog4jIsEnabled.invoke(logObj, new Object[] { log4jPriorityFatal });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }

        return ((Boolean) retval).booleanValue();
    }

    private Object getLog4jLogger(String name) {
        Object logObj;
        logObj = l4jLoggerCache.get(name);
        if (logObj == null) {
            // Change and reset context class loader
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mLog4jGetLogger.getDeclaringClass().getClassLoader());

            try {
                logObj = mLog4jGetLogger.invoke(null, new Object[] { name });
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(ctxLoader);
            }
            l4jLoggerCache.put(name, logObj);
        }

        return logObj;
    }

    void doLogCl(String name, Level level, Object msg) {
        if (!haveCl) {
            doLogGeneric(name, level, msg);
            return;
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mClTrace.getDeclaringClass().getClassLoader());

        Object logObj = getClLogger(name);
        try {
            if (level == Level.TRACE) {
                mClTrace.invoke(logObj, new Object[] { msg });
            } else if (level == Level.DEBUG) {
                mClDebug.invoke(logObj, new Object[] { msg });
            } else if (level == Level.INFO) {
                mClInfo.invoke(logObj, new Object[] { msg });
            } else if (level == Level.WARN) {
                mClWarn.invoke(logObj, new Object[] { msg });
            } else if (level == Level.ERROR) {
                mClError.invoke(logObj, new Object[] { msg });
            } else if (level == Level.FATAL) {
                mClFatal.invoke(logObj, new Object[] { msg });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    void doLogCl(String name, Level level, Object msg, Throwable ex) {
        if (!haveCl) {
            doLogGeneric(name, level, msg, ex);
            return;
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mClTraceWithEx.getDeclaringClass().getClassLoader());

        Object logObj = getClLogger(name);
        try {
            if (level == Level.TRACE) {
                mClTraceWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.DEBUG) {
                mClDebugWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.INFO) {
                mClInfoWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.WARN) {
                mClWarnWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.ERROR) {
                mClErrorWithEx.invoke(logObj, new Object[] { msg, ex });
            } else if (level == Level.FATAL) {
                mClFatalWithEx.invoke(logObj, new Object[] { msg, ex });
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    boolean doCheckEnabledCl(String name, Level level) {
        if (!haveCl) {
            return doCheckEnabledGeneric(name, level);
        }
        
        // Change and reset context class loader
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mClIsTrace.getDeclaringClass().getClassLoader());

        Object retval = null;
        Object logObj = getClLogger(name);
        try {
            if (level == Level.TRACE) {
                retval = mClIsTrace.invoke(logObj, new Object[0]);
            } else if (level == Level.DEBUG) {
                retval = mClIsDebug.invoke(logObj, new Object[0]);
            } else if (level == Level.INFO) {
                retval = mClIsInfo.invoke(logObj, new Object[0]);
            } else if (level == Level.WARN) {
                retval = mClIsWarn.invoke(logObj, new Object[0]);
            } else if (level == Level.ERROR) {
                retval = mClIsError.invoke(logObj, new Object[0]);
            } else if (level == Level.FATAL) {
                retval = mClIsFatal.invoke(logObj, new Object[0]);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }

        return ((Boolean) retval).booleanValue();
    }

    private Object getClLogger(String name) {
        Object logObj;
        logObj = clLoggerCache.get(name);
        if (logObj == null) {
            // Change and reset context class loader
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(clFactoryObject.getClass().getClassLoader());

            try {
                logObj = mClGetInstance.invoke(clFactoryObject, new Object[] { name });
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(ctxLoader);
            }
            clLoggerCache.put(name, logObj);
        }

        return logObj;
    }
    
    boolean isConfiguredForCl() {
        return haveCl;
    }

    enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

}
