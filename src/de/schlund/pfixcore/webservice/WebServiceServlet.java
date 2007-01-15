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

package de.schlund.pfixcore.webservice;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis.AxisFault;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ConfigurationReader;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixcore.webservice.jsonws.JSONWSProcessor;
import de.schlund.pfixcore.webservice.jsonws.JSONWSStubGenerator;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * WebServiceServlet.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceServlet extends AxisServlet implements ServiceProcessor {

    private Logger LOG=Logger.getLogger(getClass().getName());
    private boolean DEBUG=LOG.isDebugEnabled();
    
    private static Object initLock=new Object();
    private ServiceRuntime runtime;
   
    private AdminWebapp adminWebapp;
   
    private ClassLoader currentLoader;
    
    private static ThreadLocal<Fault> currentFault=new ThreadLocal<Fault>();
    private static ThreadLocal<ServiceRequest> currentRequest=new ThreadLocal<ServiceRequest>();
    private static ThreadLocal<ServiceResponse> currentResponse=new ThreadLocal<ServiceResponse>();
    
    public static void setCurrentFault(Fault fault) {
        currentFault.set(fault);
    }
    
    public static Fault getCurrentFault() {
        return (Fault)currentFault.get();
    }
    
    public static void setCurrentRequest(ServiceRequest request) {
        currentRequest.set(request);
    }
    
    public static ServiceRequest getCurrentRequest() {
        return (ServiceRequest)currentRequest.get();
    }
    
    public static void setCurrentResponse(ServiceResponse response) {
        currentResponse.set(response);
    }
    
    public static ServiceResponse getCurrentResponse() {
        return (ServiceResponse)currentResponse.get();
    }
    
    public void init(ServletConfig config) throws ServletException {
    	AppLoader loader=AppLoader.getInstance();
    	if(loader.isEnabled()) {
    		ClassLoader newLoader=loader.getAppClassLoader();
    		//ClassUtils.setDefaultClassLoader(newLoader);
    		Thread.currentThread().setContextClassLoader(newLoader);
            currentLoader=newLoader;
    	}
        super.init(config);
        synchronized(initLock) {
        	runtime=(ServiceRuntime)getServletContext().getAttribute(ServiceRuntime.class.getName());
        	if(runtime==null) {
        		String servletProp=config.getInitParameter(Constants.PROP_SERVLET_FILE);
        		if(servletProp!=null) {
        			FileResource wsConfFile=ResourceUtil.getFileResourceFromDocroot(servletProp);
        			try {
        				Configuration srvConf=ConfigurationReader.read(wsConfFile);
        				runtime=new ServiceRuntime();
        				runtime.setConfiguration(srvConf);
        				runtime.setApplicationServiceRegistry(new ServiceRegistry(runtime.getConfiguration(),ServiceRegistry.RegistryType.APPLICATION));
        				runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_SOAP,this);
        				runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_JSONWS,new JSONWSProcessor());
                        runtime.addServiceStubGenerator(Constants.PROTOCOL_TYPE_JSONWS,new JSONWSStubGenerator());
        				getServletContext().setAttribute(ServiceRuntime.class.getName(),runtime);
                        adminWebapp=new AdminWebapp(runtime);
        			} catch(Exception x) {
        				LOG.error("Error while initializing ServiceRuntime",x);
        				throw new ServletException("Error while initializing ServiceRuntime",x);
        			}
        		} else LOG.error("No webservice configuration found!!!");
        	}
        }
    }
    
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
            ClassLoader newLoader=loader.getAppClassLoader();
            Thread.currentThread().setContextClassLoader(newLoader);
        
            synchronized(this) {
                if(newLoader!=currentLoader) {
                    if(DEBUG) LOG.debug("Reload Axis Engine.");
                    Thread.currentThread().setContextClassLoader(newLoader);
                    currentLoader=newLoader;
                    axisServer=null;
                    getServletContext().removeAttribute(ATTR_AXIS_ENGINE);
                    getServletContext().removeAttribute(getServletName() + ATTR_AXIS_ENGINE);
                    try {
                        init();
                    } catch(ServletException x) {
                        throw new RuntimeException("Error while reloading Axis",x);
                    }
                }
            }
        }
        
    	try {
    		runtime.process(req,res);
    	} catch(ServiceException x) {
    		throw new ServletException("Error while processing webservice request.",x);
    	}
    }
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        adminWebapp.doGet(req,res);
    }
    
    public void init(ServiceRuntime runtime) {}
    
    public void process(ServiceRequest serviceReq,ServiceResponse serviceRes,ServiceRuntime runtime,ServiceRegistry registry,ProcessingInfo procInfo) throws ServiceException {
 
        HttpServletRequest req=(HttpServletRequest)serviceReq.getUnderlyingRequest();
        HttpServletResponse res=(HttpServletResponse)serviceRes.getUnderlyingResponse();
        
        try {
        	setCurrentFault(null);
        	setCurrentRequest(serviceReq);
        	setCurrentResponse(serviceRes);
        	
          	if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null && req.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
        		if(LOG.isDebugEnabled()) LOG.debug("no SOAPAction header, but soapmessage parameter -> iframe method");
        		HttpServletResponse response=res;
        		String reqID=req.getParameter(Constants.PARAM_REQUEST_ID);
        		if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID parameter: "+reqID);
        		String insPI=req.getParameter("insertpi");
        		if(insPI!=null) response=new InsertPIResponseWrapper(res);
        		if(LOG.isDebugEnabled()) if(insPI!=null) LOG.debug("contains insertpi parameter");
        		super.doPost(new SOAPActionRequestWrapper(req),response);
        	} else if(req.getHeader(Constants.HEADER_SOAP_ACTION)!=null) {
        		if(LOG.isDebugEnabled()) LOG.debug("found SOAPAction header, but no soapmessage parameter -> xmlhttprequest version");
        		String reqID=req.getHeader(Constants.HEADER_REQUEST_ID);
        		if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID header: "+reqID);
        		if(reqID!=null) res.setHeader(Constants.HEADER_REQUEST_ID,reqID);
        		super.doPost(req,res);
        	} 
        } catch(IOException x) {
        	throw new ServiceException("IOException during service processing.",x);
        } catch(ServletException x) {
        	throw new ServiceException("ServletException during service processing.",x);
        } finally {
        	setCurrentFault(null);
        	setCurrentRequest(null);
        }
    	
    }
    
    protected void processAxisFault(AxisFault axisFault) {
        Fault fault=getCurrentFault();
        if(fault==null) {
        	ServiceRequest serviceReq=getCurrentRequest();
        	HttpServletRequest req=(HttpServletRequest)serviceReq.getUnderlyingRequest();
        	LOG.warn(dumpRequest(req,true));
        	Throwable t=axisFault.getCause();
            if(t!=null) LOG.warn(t,t);
        } else {
        	Throwable t=axisFault.getCause();
        	if(t!=null) LOG.error("Exception while processing request",t);
        	String serviceName=fault.getServiceName();
        	Configuration config=runtime.getConfiguration();
        	ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
        	FaultHandler faultHandler=serviceConfig.getFaultHandler();
        	if(faultHandler!=null) {
        		fault.setThrowable(t);
        		faultHandler.handleFault(fault);
        		axisFault.setFaultString(fault.getFaultString());
        	}
        }
        axisFault.removeFaultDetail(org.apache.axis.Constants.QNAME_FAULTDETAIL_STACKTRACE);
    }

    private String dumpRequest(HttpServletRequest srvReq,boolean showHeaders) {
		StringBuffer sb=new StringBuffer();
		sb.append(srvReq.getScheme());
		sb.append("://");
		sb.append(srvReq.getServerName());
		sb.append(":");
		sb.append(srvReq.getServerPort());
		sb.append(srvReq.getRequestURI());
		HttpSession session=srvReq.getSession(false);
		if(session!=null) {
			sb.append(Constants.SESSION_PREFIX);
			sb.append(session.getId());
		}
		String s=srvReq.getQueryString();
		if(s!=null&&!s.equals("")) {
			sb.append("?");
			sb.append(s);
		}
		sb.append("\n");
		if(showHeaders) {
			Enumeration headers=srvReq.getHeaderNames();
			while(headers.hasMoreElements()) {
				String header=(String)headers.nextElement();
				String value=srvReq.getHeader(header);
				sb.append(header+": "+value+"\n");
			}
		}
		return sb.toString();
	}    

}
