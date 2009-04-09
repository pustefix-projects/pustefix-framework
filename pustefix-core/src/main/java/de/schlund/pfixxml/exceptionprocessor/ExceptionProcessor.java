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

package de.schlund.pfixxml.exceptionprocessor;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * Every <code>ExceptionProcessor</code> that is configured to process exceptions
 * via the <code>processor</code>-attribute in the <code>exception</code>-tag of the
 * pustefix servlet-configuration file, must implement this interface.
 * <br />
 * Classes that implement this interface <b>must</b> provide a no-args constructor,
 * and the {@link #processException processException}-method should be thread-safe.
 * <br />
 * Look at the
 * {@link processException(Throwable,ExceptionConfig,PfixServletRequest,ServletContext,HttpServletRequest,HttpServletResponse res) processException}-method
 * for further details how to implement this interface.
 * 
 * If the implementation doesn't output a response, a ServletException is thrown.
 * 
 * @author <a href="mailto:benjamin@schlund.de">Benjamin Reitzammer</a>
 * @version $Id$
 */
public interface ExceptionProcessor {

    /**
     * If implementing classes forward the request, in which an exception occurred, they
     * should make sure, to save the occurred exception with
     * {@link de.schlund.pfixxml.PfixServletRequest#setLastException(Throwable) PfixServletRequest.setLastException()}.
     * This is needed, so that {@link de.schlund.pfixxml.ServletManager ServletManager}
     * can prevent infinite forwarding loops.
     *
     * @param exception is not allowed to be <code>null</code>
     * @param exConfig the {@link ExceptionConfig ExceptionConfig}-instance that
     * corresponds to the type of the <code>exception</code>-param that should
     * be handled by this <code>SimpleExceptionProcessor</code>-object
     * @param pfixReq the pustefix-specific request object of the current request, during
     * which the exception occurred
     * @param servletContext the {@link javax.servlet.ServletContext ServletContext}-instance
     * in which the request is answered
     * @param req the {@link javax.servlet.http.HttpServletRequest HttpServletRequest} of the current request
     * @param res the {@link javax.servlet.http.HttpServletResponse HttpServletResponse} of the current request
     * @param properties
     */
    public void processException(Throwable exception,
                                 ExceptionConfig exConfig,
                                 PfixServletRequest pfixReq,
                                 ServletContext servletContext,
                                 HttpServletRequest req,
                                 HttpServletResponse res, Properties properties)
                          throws IOException,
                                 ServletException;

}