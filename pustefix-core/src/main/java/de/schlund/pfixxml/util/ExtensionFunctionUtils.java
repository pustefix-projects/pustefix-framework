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
