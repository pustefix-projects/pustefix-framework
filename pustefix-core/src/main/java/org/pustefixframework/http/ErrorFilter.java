package org.pustefixframework.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;

public class ErrorFilter implements Filter {

    private final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

    private static final String SESSION_ATTR_TRACEBACKLIST = ErrorFilter.class.getName() + ".LIST";

    private ExceptionProcessingConfiguration exceptionProcessingConfig;
    private Properties properties;
    private int maxTraceBackSize = 10;

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
        } finally {
            if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest req = (HttpServletRequest)request;
                HttpServletResponse res = (HttpServletResponse)response;
                if(maxTraceBackSize > 0) {
                    HttpSession session = req.getSession(false);
                    if(session != null) {
                        int status = res.getStatus();
                        if(status == 200 || status >= 400) {
                            String contentType = res.getContentType();
                            if(contentType != null) {
                                int ind = contentType.indexOf(';');
                                if(ind > 0) {
                                    contentType = contentType.substring(0, ind);
                                }
                            }
                            if(contentType == null || contentType.equals("text/html")) {
                                TraceBackList list = (TraceBackList)session.getAttribute(SESSION_ATTR_TRACEBACKLIST);
                                if(list == null) {
                                    list = new TraceBackList(maxTraceBackSize);
                                    session.setAttribute(SESSION_ATTR_TRACEBACKLIST, list);
                                }
                                list.addEntry(req, res);
                            }
                        }
                    }
                }
            }
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

    public void setMaxTraceBackSize(int maxTraceBackSize) {
        this.maxTraceBackSize = maxTraceBackSize;
    }

    public static TraceBackList getTraceBackList(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            return (TraceBackList)session.getAttribute(SESSION_ATTR_TRACEBACKLIST);
        }
        return null;
    }


    public class TraceBackList {

        private int maxEntries;
        private List<TraceBackEntry> entries = new ArrayList<>();
        private int count;

        TraceBackList(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        public synchronized void addEntry(HttpServletRequest req, HttpServletResponse res) {
            TraceBackEntry entry = new TraceBackEntry();
            entry.status = res.getStatus();
            entry.method = req.getMethod();
            entry.requestURI = req.getRequestURI();
            entry.count = ++count;
            entries.add(0, entry);
            if(entries.size() > maxEntries) {
                entries.remove(entries.size() - 1);
            }
        }

        public List<TraceBackEntry> getEntries() {
            return Collections.unmodifiableList(entries);
        }
    }

    public class TraceBackEntry {

        public int count;
        public int status;
        public String method;
        public String requestURI;
    }

}
