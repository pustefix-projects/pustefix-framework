package org.pustefixframework.http;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;

public class ErrorFilter implements Filter {

    private final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

    private ExceptionProcessingConfiguration exceptionProcessingConfig;
    private Properties properties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch(IOException | ServletException ex) {

            Throwable t = ex;
            if(t instanceof ServletException && t.getCause() != null) {
                t = t.getCause();
            }
            if((t instanceof IOException) &&
                    (t.getClass().getSimpleName().equals("ClientAbortException") ||
                     t.getClass().getName().equals("org.mortbay.jetty.EofException"))) {
                LOG.warn("Client aborted request.");
                request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_CLIENT_ABORTED, true);
                throw ex;
            } else {
                LOG.error("Exception in process", t);
                try {
                    ExceptionConfig exconf = exceptionProcessingConfig.getExceptionConfigForThrowable(t.getClass());
                    if(exconf != null && exconf.getProcessor()!= null) {
                        PfixServletRequest preq = (PfixServletRequest)request.getAttribute(PfixServletRequest.class.getName());
                        if(preq == null) {
                            preq = new PfixServletRequestImpl((HttpServletRequest)request, properties);
                        }
                        if ( preq.getLastException() == null ) {
                            ExceptionProcessor eproc = exconf.getProcessor();
                            eproc.processException(t, exconf, preq, request.getServletContext(),
                                    (HttpServletRequest)request, (HttpServletResponse)response, properties);
                        }
                        if(response.isCommitted()) {
                            return;
                        }
                    }
                } catch(IOException | ServletException x) {
                    LOG.error("Error while processing exception", x);
                }
            }
            throw ex;
        }

    }

    @Override
    public void destroy() {
    }

    public void setExceptionProcessingConfiguration(ExceptionProcessingConfiguration exceptionProcessingConfig) {
        this.exceptionProcessingConfig = exceptionProcessingConfig;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
