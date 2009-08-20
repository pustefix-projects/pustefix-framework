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

package org.pustefixframework.webservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.config.WebserviceConfiguration;
import org.pustefixframework.webservices.monitor.Monitor;
import org.pustefixframework.webservices.monitor.MonitorRecord;
import org.pustefixframework.webservices.spring.WebserviceRegistration;
import org.pustefixframework.webservices.utils.FileCache;
import org.pustefixframework.webservices.utils.FileCacheData;
import org.pustefixframework.webservices.utils.RecordingRequestWrapper;
import org.pustefixframework.webservices.utils.RecordingResponseWrapper;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.serverutil.SessionAdmin;

/**
 * @author mleidig@schlund.de
 */
public class ServiceRuntime {
	
    private final static Logger LOG=Logger.getLogger(ServiceRuntime.class);
    private final static Logger LOGGER_WSTRAIL=Logger.getLogger("LOGGER_WSTRAIL");
    
    private static ThreadLocal<ServiceCallContext> currentContext=new ThreadLocal<ServiceCallContext>();
	
    private WebserviceConfiguration configuration;	
    private Monitor monitor;
	
    private ProtocolProviderRegistry protocolProviderRegistry;

    private String defaultProtocol;
    
    private FileCache stubCache;
	
    private ServiceDescriptorCache srvDescCache;
    private ServiceRegistry serviceRegistry;
	
    private ServerContextImpl serverContext;
    private ContextImpl context;
    
    public ServiceRuntime() {
        srvDescCache=new ServiceDescriptorCache();
        stubCache=new FileCache(100);
    }	
	
    public ServiceDescriptorCache getServiceDescriptorCache() {
        return srvDescCache;
    }
    
    public WebserviceConfiguration getConfiguration() {
        return configuration;
    }
	
    public void setConfiguration(WebserviceConfiguration configuration) {
        this.configuration=configuration;
        if(configuration.getMonitoringEnabled()) {
            Monitor.Scope scope=Monitor.Scope.valueOf(configuration.getMonitoringScope().toUpperCase());
            monitor=new Monitor(scope,configuration.getMonitoringHistorySize());
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
	
    public Monitor getMonitor() {
        return monitor;
    }
    
    public void setProtocolProviderRegistry(ProtocolProviderRegistry protocolProviderRegistry) {
    	this.protocolProviderRegistry = protocolProviderRegistry;
    }
	
    public void process(HttpServletRequest req,HttpServletResponse res) throws ServiceException {
        
        String wsType=null;
        ServiceRequest serviceReq=new HttpServiceRequest(req);
        ServiceResponse serviceRes=new HttpServiceResponse(res);
        
        boolean doRecord = configuration.getMonitoringEnabled() || configuration.getLoggingEnabled();
        if(doRecord) {
            serviceReq=new RecordingRequestWrapper(serviceReq);
            serviceRes=new RecordingResponseWrapper(serviceRes);
        }
        
        try {	   
            
            ServiceCallContext callContext=new ServiceCallContext(this);
            callContext.setServiceRequest(serviceReq);
            callContext.setServiceResponse(serviceRes);
            setCurrentContext(callContext);
            
            String serviceName=serviceReq.getServiceName();
			
            wsType=req.getHeader(Constants.HEADER_WSTYPE);
            if(wsType==null) wsType=req.getParameter(Constants.PARAM_WSTYPE);
            if(wsType!=null) wsType=wsType.toUpperCase();
            
            HttpSession session = req.getSession(false);
            WebserviceRegistration registration = serviceRegistry.getWebserviceRegistration(serviceName);
            if(registration == null) throw new ServiceException("Service not found: " + serviceName);
           
            String sessionType = registration.getSessionType();
            if(sessionType == null) sessionType = configuration.getSessionType();
            if(sessionType.equals(Constants.SESSION_TYPE_SERVLET)) {
                if(session==null) throw new AuthenticationException("Authentication failed: No valid session.");
                boolean sslForce = false;
                if(registration.getSSLForce() != null) sslForce = registration.getSSLForce();
                else if(configuration.getSSLForce() != null) sslForce = configuration.getSSLForce();
                if(sslForce && !req.getScheme().equals("https")) 
                    throw new AuthenticationException("Authentication failed: SSL connection required");
                if(req.getScheme().equals("https")) {
                    Boolean secure=(Boolean)session.getAttribute(SessionAdmin.SESSION_IS_SECURE);
                    if(secure==null || !secure.booleanValue()) 
                        throw new AuthenticationException("Authentication failed: No secure session");
                }
                            
                //Find authconstraint using the following search order:
                //   - authconstraint referenced by webservice 
                //   - authconstraint referenced by webservice-global (implicit)
                //   - default authconstraint from context configuration
                AuthConstraint authConst = null;
                String authRef = registration.getAuthConstraint();
                if(authRef == null) authRef = configuration.getAuthConstraintRef();
                if(authRef != null) {
                    authConst = serverContext.getContextConfig().getAuthConstraint(authRef);
                    if(authConst == null) throw new ServiceException("AuthConstraint not found: "+authRef);
                }
                if(authConst == null) authConst = context.getContextConfig().getDefaultAuthConstraint();
                if(authConst != null) {
                    if(!authConst.isAuthorized(context)) 
                        throw new AuthenticationException("Authentication failed: AuthConstraint violated");
                }
                        
                try {
                    // Prepare context for current thread.
                    // Cleanup is performed in finally block.
                    context.setServerContext(serverContext);
                    context.prepareForRequest();                                                                                                                                  
                } catch(Exception x) {
                    throw new ServiceException("Preparing context failed",x);
                }
                callContext.setContext(context);
            }
            
          
            String protocolType = registration.getProtocol();
            if(protocolType == null) protocolType = getConfiguration().getProtocolType();
            
            if(wsType!=null) {
                if(!protocolType.equals(Constants.PROTOCOL_TYPE_ANY)&&!wsType.equals(protocolType))
                    throw new ServiceException("Service protocol '"+wsType+"' isn't supported.");
                else protocolType=wsType;
            }

            if(protocolType.equals(Constants.PROTOCOL_TYPE_ANY)) protocolType=defaultProtocol;
            ProtocolProvider protocolProvider = protocolProviderRegistry.getProtocolProvider(protocolType, null);
            if(protocolProvider == null) throw new ServiceException("No provider found for protocol: " + protocolType);
            ServiceProcessor processor = protocolProvider.getServiceProcessor();
            
            if(LOG.isDebugEnabled()) LOG.debug("Process webservice request: "+serviceName+" "+processor);

            ProcessingInfo procInfo=new ProcessingInfo(serviceName,null);
            procInfo.setStartTime(System.currentTimeMillis());
            procInfo.startProcessing();
                                         
            boolean synchronize = true;
            if(registration.getSynchronizeOnContext() != null) synchronize = registration.getSynchronizeOnContext();
            else if(configuration.getSynchronizeOnContext() != null) synchronize = configuration.getSynchronizeOnContext();
            
            if(context != null && synchronize) {
                synchronized(context) {
                    processor.process(serviceReq, serviceRes, this, serviceRegistry, procInfo);
                }
            } else {
                processor.process(serviceReq, serviceRes, this, serviceRegistry, procInfo);
            }
                                                                                                                                                        
            procInfo.endProcessing();
            
            if(session!=null) {
                StringBuilder line=new StringBuilder();
                line.append(session.getId()+"|");
                line.append(req.getRemoteAddr()+"|");
                line.append(req.getServerName()+"|");
                line.append(req.getRequestURI()+"|");
                line.append(serviceName+"|");
                line.append((procInfo.getMethod()==null?"-":procInfo.getMethod())+"|");
                line.append(procInfo.getProcessingTime()+"|");
                line.append((procInfo.getInvocationTime()==-1?"-":procInfo.getInvocationTime()));
                LOGGER_WSTRAIL.warn(line.toString());
            }

            if(doRecord) {
                RecordingRequestWrapper monitorReq=(RecordingRequestWrapper)serviceReq;
                RecordingResponseWrapper monitorRes=(RecordingResponseWrapper)serviceRes;
                String reqMsg=monitorReq.getRecordedMessage();
                String resMsg=monitorRes.getRecordedMessage();
                if(configuration.getMonitoringEnabled()) {
                    MonitorRecord monitorRecord=new MonitorRecord();
                    monitorRecord.setStartTime(procInfo.getStartTime());
                    monitorRecord.setProcessingTime(procInfo.getProcessingTime());
                    monitorRecord.setInvocationTime(procInfo.getInvocationTime());
                    monitorRecord.setProtocol(protocolType);
                    monitorRecord.setService(serviceName);
                    monitorRecord.setMethod(procInfo.getMethod());
                    monitorRecord.setRequestMessage(reqMsg);
                    monitorRecord.setResponseMessage(resMsg);
                    getMonitor().getMonitorHistory(req).addRecord(monitorRecord);
                }
                if(configuration.getLoggingEnabled()) {
                    StringBuffer sb=new StringBuffer();
                    sb.append("\nService: "+serviceName+"\n");
                    sb.append("Protocol: "+protocolType+"\n");
                    sb.append("Time: "+0+"\n");
                    sb.append("Request:\n");
                    sb.append(reqMsg==null?"":reqMsg);
                    sb.append("\nResponse:\n");
                    sb.append(resMsg);
                    sb.append("\n");
                    LOG.info(sb.toString());
                }
            }
        } catch(AuthenticationException x) {
            if(LOG.isDebugEnabled()) LOG.debug(x);
            ProtocolProvider protocolProvider = protocolProviderRegistry.getProtocolProvider(wsType, null);
            if(protocolProvider == null) throw new ServiceException("No provider found for protocol: " + wsType);
            ServiceProcessor processor = protocolProvider.getServiceProcessor();
            if(processor!=null) processor.processException(serviceReq,serviceRes,x);
            else throw x;
        } finally {
            setCurrentContext(null);
            if (context != null) {
                context.cleanupAfterRequest();
            }
        }
    }
    
    private static void setCurrentContext(ServiceCallContext callContext) {
        currentContext.set(callContext);
    }
    
    protected static ServiceCallContext getCurrentContext() {
        return currentContext.get();
    }
    
    public void getStub(HttpServletRequest req,HttpServletResponse res) throws ServiceException, IOException {
        
        long t1=System.currentTimeMillis();
        
        String nameParam=req.getParameter("name");
        if(nameParam==null) throw new ServiceException("Missing parameter: name");
        String typeParam=req.getParameter("type");
        if(typeParam==null) throw new ServiceException("Missing parameter: type");
        nameParam=nameParam.trim();
        String[] serviceNames=nameParam.split("\\s+");
        if(serviceNames.length==0) throw new ServiceException("No service name found");
        String serviceType=typeParam.toUpperCase();
        if(!serviceType.equals(Constants.PROTOCOL_TYPE_JSONWS)) throw new ServiceException("Protocol not supported: "+serviceType);
        
        WebserviceRegistration[] services = new WebserviceRegistration[serviceNames.length];
        for(int i=0;i<serviceNames.length;i++) {
            WebserviceRegistration service = serviceRegistry.getWebserviceRegistration(serviceNames[i]);
            if(service==null) throw new ServiceException("Service not found: "+serviceNames[i]);
            services[i]=service;
        }
        
        StringBuilder sb=new StringBuilder();
        for(String serviceName:serviceNames) {
            sb.append(serviceName);
            sb.append(" ");
        }
        String cacheKey=sb.toString();
                 
        FileCacheData data=stubCache.get(cacheKey);
        if(data==null) {
        	ProtocolProvider protocolProvider = protocolProviderRegistry.getProtocolProvider(serviceType, null);
            if(protocolProvider == null) throw new ServiceException("No provider found for protocol: " + serviceType);
            ServiceStubGenerator stubGen = protocolProvider.getServiceStubGenerator();
            if(stubGen!=null) {
                long tt1=System.currentTimeMillis();
                ByteArrayOutputStream bout=new ByteArrayOutputStream();
                for(int i=0;i<services.length;i++) {
                    stubGen.generateStub(services[i], configuration, bout);
                    if(i<services.length-1) bout.write("\n\n".getBytes());
                }
                byte[] bytes=bout.toByteArray();
                data=new FileCacheData(bytes);
                stubCache.put(cacheKey,data);
                long tt2=System.currentTimeMillis();
                if(LOG.isDebugEnabled()) 
                    LOG.debug("Generated stub for '"+cacheKey+"' (Time: "+(tt2-tt1)+"ms, Size: "+bytes.length+"b)");
            } else throw new ServiceException("No stub generator found for protocol: "+serviceType);
        }
        
        long t2=System.currentTimeMillis();
        if(LOG.isDebugEnabled()) LOG.debug("Retrieved stub for '"+cacheKey+"' (Time: "+(t2-t1)+"ms)");
        
        String etag=req.getHeader("If-None-Match");
        if(etag!=null && etag.equals(data.getMD5())) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            res.setContentType("text/plain");    
            res.setContentLength(data.getBytes().length);
            res.setHeader("ETag",data.getMD5());
            res.getOutputStream().write(data.getBytes());
            res.getOutputStream().close();
        }
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
    
    public void setServerContext(ServerContextImpl serverContext) {
        this.serverContext = serverContext;
    }
    
    public void setContext(ContextImpl context) {
        this.context = context;
    }

}
