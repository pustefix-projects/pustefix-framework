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
package org.pustefixframework.webservices.jaxws;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.InsertPIResponseWrapper;
import org.pustefixframework.webservices.ProcessingInfo;
import org.pustefixframework.webservices.SOAPActionRequestWrapper;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.pustefixframework.webservices.ServiceRuntime;

/**
 * @author mleidig@schlund.de
 */
public class JAXWSProcessor implements ServiceProcessor {

    private static Logger LOG=Logger.getLogger(ServiceProcessor.class);
    
    private ServletContext servletContext;
    private WSServletDelegate delegate =null;
    
    private static ThreadLocal<JAXWSContext> currentJAXWSContext=new ThreadLocal<JAXWSContext>();
    
    public JAXWSProcessor() {
    }
    
    private void init(ServletContext servletContext) {
        delegate = (WSServletDelegate)servletContext.getAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        init(servletContext);
    }
    
    public void process(ServiceRequest req, ServiceResponse res, ServiceRuntime runtime, ServiceRegistry registry, ProcessingInfo procInfo) throws ServiceException {
           
        if(!(req.getUnderlyingRequest() instanceof HttpServletRequest)) throw new ServiceException("Service protocol not supported");
        HttpServletRequest httpReq=(HttpServletRequest)req.getUnderlyingRequest();
        HttpServletResponse httpRes=(HttpServletResponse)res.getUnderlyingResponse();
        
        try {
            JAXWSContext ctx=new JAXWSContext(procInfo);
            setCurrentContext(ctx);
            
            if(httpReq.getHeader(Constants.HEADER_SOAP_ACTION)==null && httpReq.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
                if(LOG.isDebugEnabled()) LOG.debug("no SOAPAction header, but soapmessage parameter -> iframe method");
                String reqID=httpReq.getParameter(Constants.PARAM_REQUEST_ID);
                if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID parameter: "+reqID);
                String insPI=httpReq.getParameter("insertpi");
                if(insPI!=null) httpRes=new InsertPIResponseWrapper(httpRes);
                if(LOG.isDebugEnabled()) if(insPI!=null) LOG.debug("contains insertpi parameter");
                httpReq=new SOAPActionRequestWrapper(httpReq);
            } else if(httpReq.getHeader(Constants.HEADER_SOAP_ACTION)!=null) {
                if(LOG.isDebugEnabled()) LOG.debug("found SOAPAction header, but no soapmessage parameter -> xmlhttprequest version");
                String reqID=httpReq.getHeader(Constants.HEADER_REQUEST_ID);
                if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID header: "+reqID);
                if(reqID!=null) httpRes.setHeader(Constants.HEADER_REQUEST_ID,reqID);
            }
            
            if (delegate != null) {
                delegate.doPost(httpReq,httpRes,servletContext);
            }
        } catch(ServletException x) {
            throw new ServiceException("Error processing webservice request",x);
        } catch(IOException x) {
            throw new ServiceException("Error processing webservice request",x);
        } finally {
            setCurrentContext(null);
        }
    }
    
    public void processException(ServiceRequest req, ServiceResponse res, Exception exception) throws ServiceException {
        try {
            if(res.getUnderlyingResponse() instanceof HttpServletResponse) {
                ((HttpServletResponse)res.getUnderlyingResponse()).setStatus(500);
            }
            res.setContentType("text/xml");
            res.setCharacterEncoding("utf-8");
            Writer out=res.getMessageWriter();
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            out.write("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
            out.write("<S:Body>");
            out.write("<S:Fault>");
            out.write("<faultcode>S:Server</faultcode>");
            out.write("<faultstring>");
            out.write(exception.getMessage());
            out.write("</faultstring>");
            out.write("<detail>");
            out.write("<ns:exception xmlns:ns=\"http://jax-ws.dev.java.net/\" class=\"");
            out.write(exception.getClass().getName());
            out.write("\"/>");
            out.write("</detail>");
            out.write("</S:Fault>");
            out.write("</S:Body>");
            out.write("</S:Envelope>");
        } catch(IOException x) {
            throw new ServiceException("IOException during service exception processing.",x);
        }
    }
    
    private static void setCurrentContext(JAXWSContext context) {
        currentJAXWSContext.set(context);
    }
    
    protected static JAXWSContext getCurrentContext() {
        return currentJAXWSContext.get();
    }
    
}