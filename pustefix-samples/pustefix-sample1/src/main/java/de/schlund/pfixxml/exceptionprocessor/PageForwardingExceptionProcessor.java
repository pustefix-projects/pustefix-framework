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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * This class is simple implementation of the
 * {@link ExceptionProcessor ExceptionProcessor}-interface. Look at the
 * {@link #process(Exception, ExceptionConfig, PfixServletRequest, HttpServletRequest, HttpServletResponse) process()}-method
 * for details.
 *
 * @author <a href="mailto:benjamin@schlund.de">Benjamin Reitzammer</a>
 * @version $Id$
 */
public class PageForwardingExceptionProcessor implements ExceptionProcessor {

    private final static Logger LOG = Logger.getLogger(PageForwardingExceptionProcessor.class);

    /**
     * The request gets forwarded to the page specified by the 'page'-attribute
     * of {@link ExceptionConfig ExceptionConfig}. Also, the 'forward'-attribute
     * of the exceptionConfig must be true, else this method will throw an exception.
     *
     * @param exception {@inheritDoc}
     * @param exConfig {@inheritDoc}
     * @param pfixReq {@inheritDoc}
     * @param servletContext {@inheritDoc}
     * @param req {@inheritDoc}
     * @param res {@inheritDoc}
     * @exception ServletException if the state of the <code>ExceptionConfig</code>-object
     * is not sufficient for the processing of the provided <code>exception</code>
     * @exception IOException if an IOException occurs while forwarding the request
     */
    public void processException(Throwable exception, ExceptionConfig exConfig,
                                 PfixServletRequest pfixReq, ServletContext context,
                                 HttpServletRequest req, HttpServletResponse res,
                                 Properties props)
                          throws IOException, ServletException {
        if ( !exConfig.getForward() || exConfig.getPage() == null )
            throw new ServletException("Wrong ExceptionConfig! 'forward' is false or 'page' is null : \n"+exConfig);

        String forwardPage = exConfig.getPage();
        pfixReq.setLastException(exception);
        if ( !forwardPage.startsWith("/") )
            forwardPage = "/"+forwardPage;

        LOG.info("Processing Exception of type: "+ exception.getClass());
        LOG.info("Trying to forward to page: "+forwardPage);

        RequestDispatcher dispatcher = context.getRequestDispatcher(forwardPage);
        if ( dispatcher == null ) {
            ServletContext context2 = context.getContext(forwardPage);
            if ( context2 != null )
                dispatcher = context2.getRequestDispatcher(forwardPage);
        }

        // if the asked for context is not the context of the current servlet,
        // and the environment is security conscious, context can be null
        // sorry, can't do anything about it -- the forward page is unreachable from here then
        if ( dispatcher == null )
            throw new ServletException("Can't forward to page "+forwardPage+"! Page is in an inaccessible ServletContext");

        dispatcher.forward(req, res);
    }
}