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
*
*/

package de.schlund.pfixxml.exceptionprocessor;

import de.schlund.pfixxml.PfixServletRequest;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Classes that implement this interface <b>must</b> provide a no-args constructor,
 * and the {@link #processException processException}-method should be thread-safe.
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
     */
    public void processException(Throwable exception,
                                 ExceptionConfig exConfig,
                                 PfixServletRequest pfixReq,
                                 ServletContext servletContext,
                                 HttpServletRequest req,
                                 HttpServletResponse res)
                          throws IOException,
                                 ServletException;

}