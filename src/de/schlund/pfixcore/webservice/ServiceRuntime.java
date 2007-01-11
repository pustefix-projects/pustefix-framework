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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.monitor.Monitor;
import de.schlund.pfixcore.webservice.monitor.MonitorRecord;
import de.schlund.pfixcore.webservice.utils.RecordingRequestWrapper;
import de.schlund.pfixcore.webservice.utils.RecordingResponseWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.serverutil.SessionAdmin;

/**
 * @author mleidig@schlund.de
 */
public class ServiceRuntime {
	
	private static Logger LOG=Logger.getLogger(ServiceRuntime.class);
	
	private static ThreadLocal<ServiceCallContext> currentContext=new ThreadLocal<ServiceCallContext>();
	
	private Configuration configuration;	
	private Monitor monitor;
	
	private Map<String,ServiceProcessor> processors;
	private String defaultProtocol;
	
	private ServiceRegistry appServiceRegistry;
	
	public ServiceRuntime() {
		processors=new HashMap<String,ServiceProcessor>();
	}
	
	public void addServiceProcessor(String protocol,ServiceProcessor processor) {
		if(processors.isEmpty()) defaultProtocol=protocol;
		processors.put(protocol,processor);
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration=configuration;
		GlobalServiceConfig globConf=configuration.getGlobalServiceConfig();
        if(globConf.getMonitoringEnabled()) {
        	Monitor.Scope scope=Monitor.Scope.valueOf(globConf.getMonitoringScope().toUpperCase());
           	monitor=new Monitor(scope,globConf.getMonitoringHistorySize());
        }
	}
	
	public void setApplicationServiceRegistry(ServiceRegistry appServiceRegistry) {
	    this.appServiceRegistry=appServiceRegistry;
    }
	
	public Monitor getMonitor() {
		return monitor;
	}
	
	public void process(HttpServletRequest req,HttpServletResponse res) throws ServiceException {
		try {
			
			ServiceRequest serviceReq=new HttpServiceRequest(req);
			ServiceResponse serviceRes=new HttpServiceResponse(res);
			
			String serviceName=serviceReq.getServiceName();
			
            HttpSession session=req.getSession(false);
            
            ServiceRegistry serviceReg=null;
            ServiceConfig srvConf=appServiceRegistry.getServiceConfig(serviceName);
            if(srvConf!=null) serviceReg=appServiceRegistry;
            else {
                if(session!=null) {
                    serviceReg=(ServiceRegistry)session.getAttribute(ServiceRegistry.class.getName());
                    if(serviceReg==null) {
                        serviceReg=new ServiceRegistry(getConfiguration(),ServiceRegistry.RegistryType.SESSION);
                        session.setAttribute(ServiceRegistry.class.getName(),serviceReg);
                    }
                    srvConf=serviceReg.getServiceConfig(serviceName);
                } 
            }
            if(srvConf==null) throw new ServiceException("Service not found: "+serviceName);
    
			Context pfxSessionContext=null;
			ServiceCallContext callContext=null;
			
			if(srvConf.getContextName()!=null) {
				if(srvConf.getSessionType().equals(Constants.SESSION_TYPE_SERVLET)) {
					if(session==null) throw new ServiceException("Authentication failed: No valid session.");
					if(srvConf.getSSLForce() && !req.getScheme().equals("https")) 
						throw new ServiceException("Authentication failed: SSL connection required");
					if(req.getScheme().equals("https")) {
						Boolean secure=(Boolean)session.getAttribute(SessionAdmin.SESSION_IS_SECURE);
						if(secure==null || !secure.booleanValue()) 
							throw new ServiceException("Authentication failed: No secure session");
					}
					String contextName=srvConf.getContextName()+"__CONTEXT__";
					pfxSessionContext=(Context)session.getAttribute(contextName);
                    try {
                        if(pfxSessionContext.checkAuthorization(false)!=null) throw new ServiceException("Authorization failed");
                    } catch(Exception x) {
                        LOG.error(x,x);
                        throw new ServiceException("Authorization failed");
                    }
                    callContext=new ServiceCallContext(this);
                    callContext.setContext(pfxSessionContext);
                    setCurrentContext(callContext);
				}
            }
                  
        	String protocolType=srvConf.getProtocolType();
        	if(protocolType==null) protocolType=getConfiguration().getGlobalServiceConfig().getProtocolType();
			
			String wsType=req.getHeader(Constants.HEADER_WSTYPE);
			if(wsType==null) req.getParameter(Constants.PARAM_WSTYPE);
			if(wsType!=null) {
				wsType=wsType.toUpperCase();
				if(!protocolType.equals(Constants.PROTOCOL_TYPE_ANY)&&!wsType.equals(protocolType))
					throw new ServiceException("Service protocol '"+wsType+"' isn't supported.");
				else protocolType=wsType;
			}

			if(protocolType.equals(Constants.PROTOCOL_TYPE_ANY)) protocolType=defaultProtocol;
			ServiceProcessor processor=processors.get(protocolType);
			if(processor==null) throw new ServiceException("No ServiceProcessor found for protocol '"+protocolType+"'.");

			GlobalServiceConfig globConf=getConfiguration().getGlobalServiceConfig();
			boolean doRecord=globConf.getMonitoringEnabled()||globConf.getLoggingEnabled();
			long startTime=0;
			long endTime=0;
			if(doRecord) {
				startTime=System.currentTimeMillis();
				serviceReq=new RecordingRequestWrapper(serviceReq);
				serviceRes=new RecordingResponseWrapper(serviceRes);
			}
			
			
			
			if(LOG.isDebugEnabled()) LOG.debug("Process webservice request: "+serviceName+" "+processor);
			if(pfxSessionContext!=null&&srvConf.doSynchronizeOnContext()) {
    			synchronized(pfxSessionContext) {
    				processor.process(serviceReq,serviceRes,serviceReg);
    			}
			} else {
				processor.process(serviceReq,serviceRes,serviceReg);
			}		    		
			
			 if(doRecord) {
				 endTime=System.currentTimeMillis();
				 RecordingRequestWrapper monitorReq=(RecordingRequestWrapper)serviceReq;
				 RecordingResponseWrapper monitorRes=(RecordingResponseWrapper)serviceRes;
				 String reqMsg=monitorReq.getRecordedMessage();
				 String resMsg=monitorRes.getRecordedMessage();
				 if(globConf.getMonitoringEnabled()) {
					 MonitorRecord monitorRecord=new MonitorRecord();
					 monitorRecord.setStartTime(startTime);
					 monitorRecord.setEndTime(endTime);
					 monitorRecord.setProtocol(protocolType);
					 monitorRecord.setService(serviceName);
					 monitorRecord.setRequestMessage(reqMsg);
					 monitorRecord.setResponseMessage(resMsg);
					 getMonitor().getMonitorHistory(req).addRecord(monitorRecord);
				 }
				 if(globConf.getLoggingEnabled()) {
					 StringBuffer sb=new StringBuffer();
					 sb.append("\nService: "+serviceName+"\n");
					 sb.append("Protocol: "+protocolType+"\n");
					 sb.append("Time: "+(endTime-startTime)+"\n");
					 sb.append("Request:\n");
					 sb.append(reqMsg==null?"":reqMsg);
					 sb.append("\nResponse:\n");
					 sb.append(resMsg);
					 sb.append("\n");
					 LOG.info(sb.toString());
				 }
			 }
			
		} finally {
			setCurrentContext(null);
		}
	}
	
	private static void setCurrentContext(ServiceCallContext callContext) {
		currentContext.set(callContext);
	}
	
	protected static ServiceCallContext getCurrentContext() {
		return currentContext.get();
	}
	
	
	
}
