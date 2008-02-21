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
 * Does the actual logging work by selecting the right mean to log
 * through (log4j, commons-logging, jdk-logging, servlet context logging).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProxyLogUtil {
    private Class<?> clFactoryClass = null;

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
    
    private ThreadLocal<Object> contextLogTestFlag = new ThreadLocal<Object>();
    
    private boolean noContextLog = false;

    private static ProxyLogUtil instance = new ProxyLogUtil();

    /**
     * Returns the only instance of this class (singleton pattern)
     * 
     * @return Instance of this class
     */
    public static ProxyLogUtil getInstance() {
        return instance;
    }

    /**
     * Private constructor to force singleton pattern
     */
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
     * log4j nor commons-logging are available. However the servlet
     * context is only used for logging if it does not use
     * commons-logging or log4j (more precisely OUR instance of them)
     * to do the logging, as this would result in a logging loop.
     * 
     * @param context ServletContext to send log messages to
     */
    public void setServletContext(ServletContext context) {
        // Check whether we can log through servlet context without
        // creating a logging loop
        this.contextLogTestFlag.set(new Object());
        // Send test messsage
        context.log("Testing container logging...");
        this.contextLogTestFlag.set(null);
        // If flag is set, the test message was sent back to this
        // class and we should not use the context to avoid a
        // logging loop
        if (!noContextLog) {
            this.servletContext = context;
        }
    }

    /**
     * Returns the class loader throgh which the container version
     * of commons-logging can be loaded. If commons-logging is 
     * not present in the container, <code>null</code> is returned.
     * 
     * @return class loader another instance of commons-logging was
     *         loaded with or <code>null</code> if no such class 
     *         loader is available
     */
    private ClassLoader checkForCl() {
        // Get the classloader OUR version of commons-logging was
        // loaded with
        ClassLoader webappLoader = LogFactory.class.getClassLoader();
        // Get the parent class loader (should usually be the
        // class loader of the container)
        ClassLoader cl = webappLoader.getParent();
        while (cl != null) {
            // Iterate until a classloader supplying another
            // version of commons-logging has been found or
            // we have reached the root (e.g. bootstrap)
            // class loader
            Class<?> temp;
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

    /**
     * Returns the class loader throgh which the container version
     * of log4j can be loaded. If commons-logging is 
     * not present in the container, <code>null</code> is returned.
     * 
     * @return class loader another instance of log4j was
     *         loaded with or <code>null</code> if no such class 
     *         loader is available
     */
    private ClassLoader checkForLog4j() {
        // Look for log4j in container        
        ClassLoader webappLoader = Logger.class.getClassLoader();
        ClassLoader cl = webappLoader.getParent();
        while (cl != null) {
            Class<?> temp;
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
        // Register our special appender which will send all
        // log messages to this class
        proptemp.setProperty("log4j.appender.proxy", ProxyLogAppender.class.getName());
        PropertyConfigurator.configure(proptemp);
    }

    /**
     * Configure this class to log through log4j
     * 
     * @param classloader class loader to use for loading
     *        the instance of log4j which should be used to
     *        do the actual logging
     */
    private void configureForLog4j(ClassLoader classloader) {
        try {
            // Make sure all work inside parent log4j is done using its own
            // classloader (static initializations here)
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);

            Class<?> log4jLogger;
            Class<?> log4jPriority;
            try {
                log4jLogger = classloader.loadClass(Logger.class.getName());
                log4jPriority = classloader.loadClass(Priority.class.getName());
            } finally {
                // Set the old context classloader again
                Thread.currentThread().setContextClassLoader(ctxLoader);
            }

            // Use reflection to retrieve the various methods we
            // need to do the logging.
            // Remember: We have to do all stuff by reflection
            // because we are not using the log4j instance which was
            // loaded by OUR classloader, but the log4j instance
            // loaded by the container.
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

    /**
     * Configures this class for use with commons-logging.
     * 
     * @param classloader Class loader to load commons-logging trough
     */
    private void configureForCommonsLogging(ClassLoader classloader) {
        // This method works similar to configureForLog4j()
        try {
            // Make sure all work inside parent log4j is done using its own
            // classloader (static initializations here)
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classloader);

            Class<?> logClass;
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
        // Check for test flag set in setServletContext()
        if (this.contextLogTestFlag.get() != null) {
            // Test flag is set, so we got here when calling
            // ServletContext#log - so we should not use
            // the context for logging
            this.noContextLog = true;
            return;
        }
        
        if (haveLog4j) {
            // Prefer log4j
            doLogLog4j(name, level, msg);
        } else if (haveCl) {
            // Then use commons-logging
            doLogCl(name, level, msg);
        } else if (noContextLog) {
            // If neither log4j nor commons-logging are available,
            // and context cannot be used for logging, use JDK logging
            doLogJdk14(name, level, msg);
        } else {
            // Otherwise use servlet context for logging
            if (servletContext != null && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL)) {
                servletContext.log("[" + level + "] " + name + ": " + msg);
            }
        }
    }

    void doLogGeneric(String name, Level level, Object msg, Throwable ex) {
        // This method does nearly the same as the other 
        // doLogGeneric() method but takes an exception as
        // as an additional argument
        if (this.contextLogTestFlag.get() != null) {
            this.noContextLog = true;
            return;
        }
        
        if (haveLog4j) {
            doLogLog4j(name, level, msg, ex);
        } else if (haveCl) {
            doLogCl(name, level, msg, ex);
        } else if (noContextLog) {
            doLogJdk14(name, level, msg, ex);
        } else {
            if (servletContext != null && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL)) {
                servletContext.log("[" + level + "] " + name + ": " + msg, ex);
            }
        }
    }

    boolean doCheckEnabledGeneric(String name, Level level) {
        // Determine whether logging is enabled for
        // a specific level. Select the logging system
        // the same way doLogGeneric() does.
        if (haveLog4j) {
            return doCheckEnabledLog4j(name, level);
        } else if (haveCl) {
            return doCheckEnabledCl(name, level);
        } else if (noContextLog) {
            return doCheckEnabledJdk14(name, level);
        } else {
            if (level == Level.WARN || level == Level.ERROR || level == Level.FATAL) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    void doLogJdk14(String name, Level level, Object msg) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        if (level == Level.FATAL || level == Level.ERROR) {
            logger.log(java.util.logging.Level.SEVERE, msg.toString());
        } else if (level == Level.WARN) {
            logger.log(java.util.logging.Level.WARNING, msg.toString());
        } else if (level == Level.INFO) {
            logger.log(java.util.logging.Level.INFO, msg.toString());
        } else if (level == Level.DEBUG) {
            logger.log(java.util.logging.Level.FINE, msg.toString());
        } else if (level == Level.TRACE) {
            logger.log(java.util.logging.Level.FINER, msg.toString());
        }
    }
    
    void doLogJdk14(String name, Level level, Object msg, Throwable ex) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        if (level == Level.FATAL || level == Level.ERROR) {
            logger.log(java.util.logging.Level.SEVERE, msg.toString(), ex);
        } else if (level == Level.WARN) {
            logger.log(java.util.logging.Level.WARNING, msg.toString(), ex);
        } else if (level == Level.INFO) {
            logger.log(java.util.logging.Level.INFO, msg.toString(), ex);
        } else if (level == Level.DEBUG) {
            logger.log(java.util.logging.Level.FINE, msg.toString(), ex);
        } else if (level == Level.TRACE) {
            logger.log(java.util.logging.Level.FINER, msg.toString(), ex);
        }
    }
    
    boolean doCheckEnabledJdk14(String name, Level level) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        if (level == Level.FATAL || level == Level.ERROR) {
            return logger.isLoggable(java.util.logging.Level.SEVERE);
        } else if (level == Level.WARN) {
            return logger.isLoggable(java.util.logging.Level.WARNING);
        } else if (level == Level.INFO) {
            return logger.isLoggable(java.util.logging.Level.INFO);
        } else if (level == Level.DEBUG) {
            return logger.isLoggable(java.util.logging.Level.FINE);
        } else if (level == Level.TRACE) {
            return logger.isLoggable(java.util.logging.Level.FINER);
        } else {
            return false;
        }        
    }

    void doLogLog4j(String name, Level level, Object msg) {
        if (!haveLog4j) {
            // If log4j is not configured, we have to use
            // one the availabele logging systems
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
            // If log4j is not configured, we have to use
            // one the availabele logging systems
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
            // If log4j is not configured, we have to use
            // one the availabele logging systems
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
        // Return a logger for a specific name
        // Note: This has all to be done using the
        // right classloader or we will run in funny
        // problems...
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
        // Similar to doLogLog4j()
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
        // Similar to doLogLog4j()
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
        // Similar to doCheckEnabledLog4j()
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
        // Similar to getLog4jLogger()
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
        // This method is used by ProxyLogFactory in order do
        // determine wheter it shall create logger that log through
        // this class or use the default log4j loggers, which will
        // use our log4j instance for logging (this is the default).
        return haveCl;
    }

    /**
     * Enum class specifying all log levels
     * 
     * @author Sebastian Marsching <sebastian.marsching@1und1.de>
     */
    enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

}
