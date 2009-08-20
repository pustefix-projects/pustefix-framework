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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.webservices.AdminWebapp;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRuntime;
import org.pustefixframework.webservices.config.WebserviceConfiguration;
import org.pustefixframework.webservices.osgi.WebserviceExtension;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;


/**
 * Webservice HTTP endpoint handling service requests (along with admin/tool stuff).
 * 
 * @author mleidig
 */
public class WebServiceHttpRequestHandler implements UriProvidingHttpRequestHandler, InitializingBean, ServletContextAware, ApplicationContextAware {

    private static final long serialVersionUID = -5686011510105975584L;

    private Logger LOG = Logger.getLogger(WebServiceHttpRequestHandler.class.getName());

    private ServiceRuntime runtime;

    private AdminWebapp adminWebapp;

    //TODO: inject configuration
    private WebserviceConfiguration configuration;
    
    private ServletContext servletContext;
    private String handlerURI;
    private ApplicationContext applicationContext;
    
    private static final String GENERATOR_IMPL_JSONWS="org.pustefixframework.webservices.jsonws.JSONWSStubGenerator";
    
    private WebserviceExtension rootExtension;
    
    public void afterPropertiesSet() throws Exception {
        LOG.info("Initialize ServiceRuntime ...");
        try {
            runtime.setConfiguration(configuration);
            runtime.setServiceRegistry(new ServiceRegistry());
            getServletContext().setAttribute(ServiceRuntime.class.getName(), runtime);
            adminWebapp = new AdminWebapp(runtime);
        } catch (Exception x) {
            LOG.error("Error while initializing ServiceRuntime", x);
            throw new ServletException("Error while initializing ServiceRuntime", x);
        }
       
        String[] extNames = applicationContext.getBeanNamesForType(WebserviceExtension.class);
        if(extNames.length > 1) throw new Exception("Found multiple root extensions");
        rootExtension = (WebserviceExtension)applicationContext.getBean(extNames[0]);
        
        LOG.info("Initialization of ServiceRuntime done.");
    }
    
    private void tryRefreshRegistry() {
    	if(rootExtension.getWebserviceRegistrations() != runtime.getServiceRegistry().getWebserviceRegistrations()) {
    		runtime.getServiceRegistry().setWebserviceRegistrations(rootExtension.getWebserviceRegistrations());
    	}
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        tryRefreshRegistry();
    	try {
            if(req.getMethod().equals("POST")) {
                runtime.process(req, res);
            } else if(req.getMethod().equals("GET")) {
                adminWebapp.doGet(req, res);
            } else throw new ServletException("Method "+req.getMethod()+" not supported!");
        } catch (Throwable t) {
            LOG.error("Error while processing webservice request", t);
            if (!res.isCommitted()) throw new ServletException("Error while processing webservice request.", t);
        }
    }

    public void setConfiguration(WebserviceConfiguration configuration) {
    	this.configuration = configuration;
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
        return new String[] { handlerURI };
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
