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
package org.pustefixframework.webservices.jaxws;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;

import net.sf.cglib.proxy.Enhancer;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.InsertPIResponseWrapper;
import org.pustefixframework.webservices.ProcessingInfo;
import org.pustefixframework.webservices.SOAPActionRequestWrapper;
import org.pustefixframework.webservices.ServiceCallContext;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.pustefixframework.webservices.ServiceRuntime;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.GlobalServiceConfig;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.springframework.aop.framework.ProxyFactory;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;

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
      this.servletContext = servletContext;
    }
    
    private void createDelegate(ServiceRuntime runtime, ServiceRegistry registry) throws ServiceException {
        ServletAdapterList adapterList = new ServletAdapterList();
        Configuration conf = runtime.getConfiguration();
        
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain =new ArrayList<Handler>();
        handlerChain.add(new ErrorHandler()); 
        GlobalServiceConfig globConf = conf.getGlobalServiceConfig();
        if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
            handlerChain.add(new RecordingHandler());
        }
        
        for(ServiceConfig serviceConf:conf.getServiceConfig()) {
            String serviceName = serviceConf.getName();
           
            Object serviceObj = runtime.getAppServiceRegistry().getServiceObject(serviceName);
            ProxyFactory pf = new ProxyFactory(serviceObj);
            pf.setProxyTargetClass(true);
            pf.addAdvice(new TracingInterceptor());
            Object proxyObj = pf.getProxy();
           
            Invoker invoker = InstanceResolver.createSingleton(proxyObj).createInvoker();
            QName serviceQName = new QName(JAXWSUtils.getTargetNamespace(serviceObj.getClass()),serviceName);
            WSBinding binding = BindingImpl.create(BindingID.SOAP11_HTTP);
            binding.setHandlerChain(handlerChain);
            Class<?> serviceClass = serviceObj.getClass();
            if(Enhancer.isEnhanced(serviceClass)) serviceClass = serviceClass.getSuperclass();
            WSEndpoint<?> endpoint = WSEndpoint.create(serviceClass, false, invoker, serviceQName, null, null, binding, null, null, null, true);    
            String url = conf.getGlobalServiceConfig().getRequestPath()+"/"+serviceName;
            adapterList.createAdapter(serviceName, url, endpoint);     
        }
        delegate = new WSServletDelegate(adapterList, servletContext);
    }
    
    private void tweakLogging() {
        //make JAXWS less verbose (shouldn't be hard-coded here)
        java.util.logging.Logger.getLogger("com.sun.xml.ws.server.sei.EndpointMethodHandler").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("com.sun.xml.ws.transport.http.servlet").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server.http").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.servlet.http").setLevel(Level.SEVERE);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        init(servletContext);
    }
    
    public void process(ServiceRequest req, ServiceResponse res, ServiceRuntime runtime, ServiceRegistry registry, ProcessingInfo procInfo) throws ServiceException {
        
        synchronized(this) {
            if(delegate==null) {
                tweakLogging();
                createDelegate(runtime,registry);
            }
        }
        
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
 
    
    class TracingInterceptor implements MethodInterceptor {
        
        public Object invoke(MethodInvocation i) throws Throwable {
            JAXWSContext ctx=JAXWSContext.getCurrentContext();
            ctx.startInvocation();
            try {
                return i.proceed();
            } catch(Exception x) {
                ctx.setThrowable(x);
                throw new ProxyInvocationException(x);
            } finally {
                ctx.endInvocation();
                ServiceResponse res = ServiceCallContext.getCurrentContext().getServiceResponse();
                if(res.getUnderlyingResponse() instanceof HttpServletResponse) {
                    HttpServletResponse httpRes = (HttpServletResponse)res.getUnderlyingResponse();
                    Context context = ServiceCallContext.getCurrentContext().getContext();
                    if(context != null) {
                        List<Cookie> cookies = ((ContextImpl)context).getCookies();
                        for(Cookie cookie: cookies) {
                            httpRes.addCookie(cookie);
                        }
                    }
                }
            }
        }
    }  
    
}