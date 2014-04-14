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

package org.pustefixframework.webservices.spring;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.http.SessionUtils;
import org.pustefixframework.util.BytecodeAPIUtils;
import org.pustefixframework.webservices.AdminWebapp;
import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRuntime;
import org.pustefixframework.webservices.ServiceStubGenerator;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ConfigurationReader;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Webservice HTTP endpoint handling service requests (along with admin/tool stuff).
 * 
 * @author mleidig
 */
public class WebServiceHttpRequestHandler implements UriProvidingHttpRequestHandler, InitializingBean, ServletContextAware, ApplicationContextAware {

    private Logger LOG = Logger.getLogger(WebServiceHttpRequestHandler.class.getName());

    private ServiceRuntime runtime;

    private AdminWebapp adminWebapp;

    private Resource configFile;
    
    private ServletContext servletContext;
    private String handlerURI;
    private ApplicationContext applicationContext;
    
    private static final String PROCESSOR_IMPL_JAXWS="org.pustefixframework.webservices.jaxws.JAXWSProcessor";
    private static final String PROCESSOR_IMPL_JSONWS="org.pustefixframework.webservices.jsonws.JSONWSProcessor";
    
    private static final String GENERATOR_IMPL_JSONWS="org.pustefixframework.webservices.jsonws.JSONWSStubGenerator";
    private static final String GENERATOR_IMPL_JAXWS="org.pustefixframework.webservices.jaxws.JAXWSStubGenerator";
    
    //TODO: dynamic ServiceProcessor detection/registration
    private ServiceProcessor findServiceProcessor(String protocolType) throws ServletException {
        String procClass = null;
        if(protocolType.equals(Constants.PROTOCOL_TYPE_SOAP)) procClass = PROCESSOR_IMPL_JAXWS;
        else if(protocolType.equals(Constants.PROTOCOL_TYPE_JSONWS)) procClass = PROCESSOR_IMPL_JSONWS;
        try {
            Class<?> clazz = Class.forName(procClass);
            ServiceProcessor proc = (ServiceProcessor)clazz.newInstance();
            return proc;
        } catch(ClassNotFoundException x) {
            if(LOG.isDebugEnabled()) LOG.debug("ServiceProcessor '"+procClass+"' for protocol '"+
                protocolType+"' not found. Ignore and disable support for this protocol."); 
            return null;
        } catch(Exception x) {
            throw new ServletException("Can't instantiate ServiceProcessor: "+procClass, x);
        }
    }
    
    public void afterPropertiesSet() throws Exception {
        LOG.info("Initialize ServiceRuntime ...");
        try {
            Configuration srvConf = ConfigurationReader.read(configFile, applicationContext);
            //if (srvConf.getGlobalServiceConfig().getContextName() == null && config.getInitParameter(Constants.PROP_CONTEXT_NAME) != null) {
            //    srvConf.getGlobalServiceConfig().setContextName(config.getInitParameter(Constants.PROP_CONTEXT_NAME));
            //}
            runtime.setConfiguration(srvConf);
            runtime.setApplicationServiceRegistry(new ServiceRegistry(runtime.getConfiguration(),
                    ServiceRegistry.RegistryType.APPLICATION));
            //TODO: dynamic ServiceProcessor detection/registration
            ServiceProcessor sp = findServiceProcessor(Constants.PROTOCOL_TYPE_SOAP);
            if(sp!=null) {
                Method meth = sp.getClass().getMethod("setServletContext", ServletContext.class);
                meth.invoke(sp, getServletContext());
                runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_SOAP, sp);
                try {
                    Class<?> clazz = Class.forName(GENERATOR_IMPL_JAXWS);
                    ServiceStubGenerator gen = (ServiceStubGenerator)clazz.newInstance();
                    runtime.addServiceStubGenerator(Constants.PROTOCOL_TYPE_SOAP, gen);
                    LOG.info("Registered ServiceProcessor for "+Constants.PROTOCOL_TYPE_SOAP);
                } catch(Exception x) {
                    throw new ServletException("Can't instantiate ServiceStubGenerator: "+GENERATOR_IMPL_JSONWS,x);
                }
            }
            URL metaURL = srvConf.getGlobalServiceConfig().getDefaultBeanMetaDataURL();
            sp = findServiceProcessor(Constants.PROTOCOL_TYPE_JSONWS);
            if(sp!=null) {
                Method meth = sp.getClass().getMethod("setBeanMetaDataURL", URL.class);
                meth.invoke(sp, metaURL);
                runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_JSONWS, sp);
                try {
                    Class<?> clazz = Class.forName(GENERATOR_IMPL_JSONWS);
                    ServiceStubGenerator gen = (ServiceStubGenerator)clazz.newInstance();
                    runtime.addServiceStubGenerator(Constants.PROTOCOL_TYPE_JSONWS, gen);
                    LOG.info("Registered ServiceProcessor for "+Constants.PROTOCOL_TYPE_JSONWS);
                } catch(Exception x) {
                    throw new ServletException("Can't instantiate ServiceStubGenerator: "+GENERATOR_IMPL_JSONWS,x);
                }
            }
            getServletContext().setAttribute(ServiceRuntime.class.getName(), runtime);
            adminWebapp = new AdminWebapp(runtime);
        } catch (Exception x) {
            LOG.error("Error while initializing ServiceRuntime", x);
            throw new ServletException("Error while initializing ServiceRuntime", x);
        }
       
        initServices();
        
        LOG.info("Initialization of ServiceRuntime done.");
    }
    
    
    private void initServices() {
        LOG.info("Register Spring backed webservices ...");
        String[] names = applicationContext.getBeanNamesForType(WebServiceRegistration.class);
        for(String name:names) {
            WebServiceRegistration reg = (WebServiceRegistration)applicationContext.getBean(name);
            ServiceConfig serviceConfig = new ServiceConfig(runtime.getConfiguration().getGlobalServiceConfig());
            serviceConfig.setName(reg.getServiceName());
            serviceConfig.setScopeType(Constants.SERVICE_SCOPE_APPLICATION);
            serviceConfig.setSessionType(reg.getSessionType());
            serviceConfig.setAuthConstraintRef(reg.getAuthConstraint());
            String ref = reg.getTargetBeanName();
            serviceConfig.setInterfaceName(reg.getInterface());
            Object serviceObject = null;
            if(ref==null) {
                serviceObject = reg.getTarget();
            } else {
                serviceObject = applicationContext.getBean(ref);
            }
            Class<?> serviceObjectClass = serviceObject.getClass();
            if(BytecodeAPIUtils.isProxy(serviceObjectClass)) serviceObjectClass = serviceObjectClass.getSuperclass();
            serviceConfig.setImplementationName(serviceObjectClass.getName());
            serviceConfig.setProtocolType(reg.getProtocol());
            runtime.getConfiguration().addServiceConfig(serviceConfig);
            runtime.getAppServiceRegistry().register(reg.getServiceName(), serviceObject);
            LOG.info("Registered webservice "+reg.getServiceName());
        }
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            if(req.getMethod().equals("POST")) {
            	HttpSession session = req.getSession(false);
            	if(session != null) {
            		ReadWriteLock lock = (ReadWriteLock)session.getAttribute(SessionUtils.SESSION_ATTR_LOCK);
     				if(lock != null) {
     					Lock readLock = lock.readLock();
     					readLock.lock();
     					try {
     						runtime.process(req, res);
     						return;
     					} finally {
     						readLock.unlock();
     					}
     				}
            	}
                runtime.process(req, res);
            } else if(req.getMethod().equals("GET")) {
                adminWebapp.doGet(req, res);
            } else throw new ServletException("Method "+req.getMethod()+" not supported!");
        } catch (Exception x) {
            LOG.error("Error while processing webservice request", x);
            if (!res.isCommitted()) throw new ServletException("Error while processing webservice request.", x);
        }
    }

    public void setConfigFile(String path) {
        configFile = ResourceUtil.getResource(path);
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public ServletContext getServletContext() {
        return servletContext;
    }
    
    public void setHandlerURI(String uri) {
        this.handlerURI = uri;
    }
    
    public String[] getRegisteredURIs() {
        return new String[] { handlerURI, handlerURI + "/**" };
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public ServiceRuntime getServiceRuntime() {
        return runtime;
    }
    
    public void setServiceRuntime(ServiceRuntime runtime) {
        this.runtime = runtime;
    }
    
}
