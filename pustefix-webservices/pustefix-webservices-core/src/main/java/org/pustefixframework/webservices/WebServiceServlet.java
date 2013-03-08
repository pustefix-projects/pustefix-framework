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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ConfigurationReader;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Webservice HTTP endpoint handling service requests (along with admin/tool stuff).
 * 
 * @author mleidig@schlund.de
 */
public class WebServiceServlet extends HttpServlet {

    private static final long serialVersionUID = -5686011510105975584L;

    private Logger LOG = Logger.getLogger(getClass().getName());

    private static Object initLock = new Object();
    private ServiceRuntime runtime;

    private AdminWebapp adminWebapp;

    private static final String PROCESSOR_IMPL_JAXWS="org.pustefixframework.webservices.jaxws.JAXWSProcessor";
    private static final String PROCESSOR_IMPL_JSONWS="org.pustefixframework.webservices.jsonws.JSONWSProcessor";
    
    private static final String GENERATOR_IMPL_JSONWS="org.pustefixframework.webservices.jsonws.JSONWSStubGenerator";
    
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
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        synchronized (initLock) {
            runtime = (ServiceRuntime) getServletContext().getAttribute(ServiceRuntime.class.getName());
            if (runtime == null) {
                String servletProp = config.getInitParameter(Constants.PROP_SERVLET_FILE);
                if (servletProp != null) {
                    FileResource wsConfFile = ResourceUtil.getFileResourceFromDocroot(servletProp);
                    try {
                        runtime = new ServiceRuntime();
                        Configuration srvConf = ConfigurationReader.read(wsConfFile);
                        runtime.setConfiguration(srvConf);
                        runtime.setApplicationServiceRegistry(new ServiceRegistry(runtime.getConfiguration(),
                                ServiceRegistry.RegistryType.APPLICATION));
                        //TODO: dynamic ServiceProcessor detection/registration
                        ServiceProcessor sp = findServiceProcessor(Constants.PROTOCOL_TYPE_SOAP);
                        if(sp!=null) {
                            Method meth = sp.getClass().getMethod("setServletContext", ServletContext.class);
                            meth.invoke(sp, getServletContext());
                            runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_SOAP, sp);
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
                } else LOG.error("No webservice configuration found!!!");
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            runtime.process(req, res);
        } catch (Throwable t) {
            LOG.error("Error while processing webservice request", t);
            if (!res.isCommitted()) throw new ServletException("Error while processing webservice request.", t);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        adminWebapp.doGet(req, res);
    }
    
}
