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
package org.pustefixframework.webservices.fault;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;

public class ExceptionProcessorAdapter extends FaultHandler implements ApplicationContextAware {

    private static final long serialVersionUID = -7046404948962289457L;

    final static Logger LOG=Logger.getLogger(ExceptionProcessorAdapter.class);
    
    transient ApplicationContext appContext;
    
    @Override
    public void init() {   
    }
    
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }
    
    @Override
    public void handleFault(Fault fault) {
        if(isNotificationError(fault)) {
            if(!(fault.getThrowable() instanceof Exception)) {
                LOG.error("Throwable isn't Exception instance: "+fault.getThrowable().getClass().getName());
                return;
            }
            if(appContext != null) {
                Exception ex=(Exception)fault.getThrowable();
                ServiceRequest srvReq=fault.getRequest();
                ServiceResponse srvRes=fault.getResponse();
                if(srvReq.getUnderlyingRequest() instanceof HttpServletRequest) {
                    HttpServletRequest req=(HttpServletRequest)srvReq.getUnderlyingRequest();
                    HttpServletResponse res=(HttpServletResponse)srvRes.getUnderlyingResponse();
                    PfixServletRequest pfixReq=new PfixServletRequestImpl(req,new Properties());
                    HttpSession session=req.getSession(false);
                    if(session!=null) {
                        try {
                            res = new NoOpHttpServletResponse(res);
                            ExceptionProcessingConfiguration conf = (ExceptionProcessingConfiguration)appContext.getBean(ExceptionProcessingConfiguration.class.getName());
                            if(conf != null) {
                                ExceptionConfig exConf = conf.getExceptionConfigForThrowable(ex.getClass());
                                ExceptionProcessor exProc = exConf.getProcessor();
                                if(exProc != null) {
                                    Properties exProcProps = new Properties();
                                    exProc.processException(ex, exConf, pfixReq, session.getServletContext(), req, res, exProcProps);
                                } else {
                                    LOG.error("Can't get ExceptionProcessor for " + ex.getClass().getName());
                                }
                            } else {
                                LOG.error("Can't get ExceptionProcessingConfiguration for " + ex.getClass().getName());
                            }
                        } catch(Exception x) {
                            LOG.error("Can't process exception.",x);
                        }
                    }
                }
            }
        }
        if(isInternalServerError(fault)) fault.setThrowable(new InternalServerError());
    }
    
    public boolean isInternalServerError(Fault fault) {
        Throwable t=fault.getThrowable();
        if(t!=null && (t instanceof Error)) return true;
        return false;
    }
    
    public boolean isNotificationError(Fault fault) {
        return true;
    }
    
    private class NoOpHttpServletResponse extends HttpServletResponseWrapper {
    
        private ServletOutputStream out = new NoOpServletOutputStream();
        private PrintWriter writer = new PrintWriter(out);
        
        public NoOpHttpServletResponse(HttpServletResponse res) {
            super(res);
        }
        
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return out;
        }
        
        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }
        
        @Override
        public void addHeader(String name, String value) {}
        
        @Override
        public void setHeader(String name, String value) {}
        
        @Override
        public void setContentType(String type) {}
         
    }
    
    private class NoOpServletOutputStream extends ServletOutputStream {
        
        @Override
        public void write(int b) throws IOException {}
        
    }
    
}
