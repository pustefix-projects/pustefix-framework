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

package org.pustefixframework.webservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.GlobalServiceConfig;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.pustefixframework.webservices.monitor.Monitor;
import org.pustefixframework.webservices.monitor.MonitorRecord;
import org.pustefixframework.webservices.utils.FileCache;
import org.pustefixframework.webservices.utils.FileCacheData;
import org.pustefixframework.webservices.utils.RecordingRequestWrapper;
import org.pustefixframework.webservices.utils.RecordingResponseWrapper;
import org.springframework.aop.framework.Advised;

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
	
    private Configuration configuration;	
    private Monitor monitor;
	
    private Map<String,ServiceProcessor> processors;
    private Map<String,ServiceStubGenerator> generators;
    private String defaultProtocol;
    
    private FileCache stubCache;
	
    private ServiceDescriptorCache srvDescCache;
    private ServiceRegistry appServiceRegistry;
	
    private ServerContextImpl serverContext;
    private ContextImpl context;
    
    public ServiceRuntime() {
        srvDescCache=new ServiceDescriptorCache();
        processors=new HashMap<String,ServiceProcessor>();
        generators=new HashMap<String,ServiceStubGenerator>();
        stubCache=new FileCache(100);
    }	
	
    public ServiceDescriptorCache getServiceDescriptorCache() {
        return srvDescCache;
    }
    
	public void addServiceProcessor(String protocol,ServiceProcessor processor) {
		if(processors.isEmpty()) defaultProtocol=protocol;
		processors.put(protocol,processor);
	}
    
    public void addServiceStubGenerator(String protocol,ServiceStubGenerator generator) {
        generators.put(protocol,generator);
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
        
        String wsType=null;
        ServiceRequest serviceReq=new HttpServiceRequest(req);
        ServiceResponse serviceRes=new HttpServiceResponse(res);
        
        GlobalServiceConfig globConf=getConfiguration().getGlobalServiceConfig();
        boolean doRecord=globConf.getMonitoringEnabled()||globConf.getLoggingEnabled();
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
            
            HttpSession session=req.getSession(false);
            ServiceRegistry serviceReg=null;
            ServiceConfig srvConf=appServiceRegistry.getService(serviceName);
            if(srvConf!=null) serviceReg=appServiceRegistry;
            else {
                if(session!=null) {
                    serviceReg=(ServiceRegistry)session.getAttribute(ServiceRegistry.class.getName());
                    if(serviceReg==null) {
                        serviceReg=new ServiceRegistry(configuration,ServiceRegistry.RegistryType.SESSION);
                        session.setAttribute(ServiceRegistry.class.getName(),serviceReg);
                    }
                    srvConf=serviceReg.getService(serviceName);
                } 
            }
            if(srvConf==null) throw new ServiceException("Service not found: "+serviceName);
           
           
            if(srvConf.getSessionType().equals(Constants.SESSION_TYPE_SERVLET)) {
                if(session==null) throw new AuthenticationException("Authentication failed: No valid session.");
                if(srvConf.getSSLForce() && !req.getScheme().equals("https")) 
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
                String authRef=srvConf.getAuthConstraintRef();
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
            
          
            String protocolType=srvConf.getProtocolType();
            if(protocolType==null) protocolType=getConfiguration().getGlobalServiceConfig().getProtocolType();
            
            if(wsType!=null) {
                if(!protocolType.equals(Constants.PROTOCOL_TYPE_ANY)&&!wsType.equals(protocolType))
                    throw new ServiceException("Service protocol '"+wsType+"' isn't supported.");
                else protocolType=wsType;
            }

            if(protocolType.equals(Constants.PROTOCOL_TYPE_ANY)) protocolType=defaultProtocol;
            ServiceProcessor processor=processors.get(protocolType);
            if(processor==null) throw new ServiceException("No ServiceProcessor found for protocol '"+protocolType+"'.");
            
            if(LOG.isDebugEnabled()) LOG.debug("Process webservice request: "+serviceName+" "+processor);

            ProcessingInfo procInfo=new ProcessingInfo(serviceName,null);
            procInfo.setStartTime(System.currentTimeMillis());
            procInfo.startProcessing();
                                                                  
            if(context!=null&&srvConf.getSynchronizeOnContext()) {
                Advised proxy = (Advised)context;
                Object object = null;
                try {
                    object = proxy.getTargetSource().getTarget();
                } catch (Exception e) {
                    throw new RuntimeException("Can't get target object", e);
                }
                synchronized(object) {
                    processor.process(serviceReq,serviceRes,this,serviceReg,procInfo);
                }
            } else {
                processor.process(serviceReq,serviceRes,this,serviceReg,procInfo);
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
                if(globConf.getMonitoringEnabled()) {
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
                if(globConf.getLoggingEnabled()) {
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
            ServiceProcessor processor=processors.get(wsType);
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
        
        ServiceConfig[] services=new ServiceConfig[serviceNames.length];
        for(int i=0;i<serviceNames.length;i++) {
            HttpSession session=req.getSession(false);
            ServiceConfig service=appServiceRegistry.getService(serviceNames[i]);
            if(service==null) {
                if(session!=null) {
                    ServiceRegistry serviceReg=(ServiceRegistry)session.getAttribute(ServiceRegistry.class.getName());
                    if(serviceReg==null) {
                        serviceReg=new ServiceRegistry(configuration,ServiceRegistry.RegistryType.SESSION);
                        session.setAttribute(ServiceRegistry.class.getName(),serviceReg);
                    }
                    service=serviceReg.getService(serviceNames[i]);
                } 
            }
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
            ServiceStubGenerator stubGen=generators.get(serviceType);
            if(stubGen!=null) {
                long tt1=System.currentTimeMillis();
                ByteArrayOutputStream bout=new ByteArrayOutputStream();
                for(int i=0;i<services.length;i++) {
                    stubGen.generateStub(services[i],bout);
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

    public ServiceRegistry getAppServiceRegistry() {
        return appServiceRegistry;
    }
    
    public void setServerContext(ServerContextImpl serverContext) {
        this.serverContext = serverContext;
    }
    
    public void setContext(ContextImpl context) {
        this.context = context;
    }

}
