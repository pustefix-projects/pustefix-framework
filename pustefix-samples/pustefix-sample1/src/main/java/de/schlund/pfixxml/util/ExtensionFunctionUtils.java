package de.schlund.pfixxml.util;

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

    private static ThreadLocal<Throwable> extFuncError=new ThreadLocal<Throwable>();
    
    public static void setExtensionFunctionError(Throwable t) {
        extFuncError.set(t);
    }
    
    public static Throwable getExtensionFunctionError() {
        return extFuncError.get();
    }
    
}
