package org.pustefixframework.util.javascript.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.pustefixframework.util.javascript.Compressor;
import org.pustefixframework.util.javascript.CompressorException;

/**
 * Adapter to the YUI Javascript compressor, which can handle conflicting
 * Rhino versions in the classpath (problem with patched Rhino bundled with YUI).
 */
public class YUICompressorAdapter implements Compressor {
    
    private final static Logger LOG = Logger.getLogger(YUICompressorAdapter.class);
    
    private final static String COMPRESSOR_CLASS = "com.yahoo.platform.yui.compressor.JavaScriptCompressor";
    private final static String REPORTER_CLASS = "org.mozilla.javascript.ErrorReporter";
    
    private static boolean isAvailable;
    private static URL jarURL;
    
    private static Class<?> compressorClass;
    private static Class<?> reporterClass;
    
    static {
        //check if compressor is in classpath
        ClassLoader loader = YUICompressorAdapter.class.getClassLoader();
        URL url = loader.getResource("com/yahoo/platform/yui/compressor/JavaScriptCompressor.class");
        if(url != null) {
            LOG.info("Found YUI Javascript Compressor in classpath");
            isAvailable = true;
            if(url.getProtocol().equals("jar")) {
                //check if multiple rhino versions are in classpath
                try {
                    Enumeration<URL> urls = loader.getResources("org/mozilla/javascript/Parser.class");
                    if(urls.hasMoreElements()) {
                        urls.nextElement();
                        if(urls.hasMoreElements()) {
                            LOG.info("Found multiple Rhino versions in classpath");
                            String urlStr = url.toString();
                            int ind = urlStr.indexOf('!');
                            if(ind > -1 && urlStr.length() > ind + 1)  {
                                urlStr = urlStr.substring(0, ind+2);
                                jarURL = new URL(urlStr);
                                LOG.info("Load classes from " + jarURL.toString());
                            }
                        }
                    }
                } catch(IOException x) {
                    LOG.error("Error looking up Rhino versions", x);
                    isAvailable = false;
                }
            }
        }
        ClassLoader oldLoader = null;
        try {
            if(jarURL != null) {
                oldLoader = Thread.currentThread().getContextClassLoader();
                loader = new URLClassLoader(new URL[] {jarURL});
                Thread.currentThread().setContextClassLoader(loader);
            }       
            try {
                compressorClass = Class.forName(COMPRESSOR_CLASS, true, loader);
                reporterClass = Class.forName(REPORTER_CLASS, true, loader);
            } catch (ClassNotFoundException x) {
                LOG.error("Can't get compressor classes", x);
                isAvailable = false;
            }
        } finally {
            if(jarURL != null) Thread.currentThread().setContextClassLoader(oldLoader);
        }
        
    }
    
    public static boolean isAvailable() {
        return isAvailable;
    }

    public void compress(Reader reader, Writer writer) throws CompressorException {
            
        try {
            Object reporter = Proxy.newProxyInstance(reporterClass.getClassLoader(), new Class<?>[] {reporterClass}, new ErrorReporterProxy());
            Constructor<?> con = compressorClass.getConstructor(Reader.class, reporterClass);
            Object compressor = con.newInstance(reader, reporter);
            Method meth = compressorClass.getMethod("compress", Writer.class, int.class, boolean.class, boolean.class, boolean.class, boolean.class);
            meth.invoke(compressor, writer, 1000, true, false, false, false);
        } catch(InvocationTargetException x) {
            throw new CompressorException("Error during compression", x);
        } catch(Exception x) {
            throw new CompressorException("Can't invoke compressor", x);
            
        } 
                
    }
    
    class ErrorReporterProxy implements InvocationHandler {
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            StringBuilder sb = new StringBuilder();
            if(args!=null) {
                for(Object arg:args) {
                    sb.append(arg==null?"-":arg.toString()).append("|");
                }
            }
            String name = method.getName();
            if(name.equals("warning")) {
                LOG.warn(sb.toString());
            } else if(name.equals("error") || name.equals("runtimeError")) {
                LOG.error(sb.toString());
            }
            return null;
        }
        
    }

}
