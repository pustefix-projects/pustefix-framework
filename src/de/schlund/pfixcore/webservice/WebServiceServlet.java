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
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ConfigurationReader;
import de.schlund.pfixcore.webservice.jaxws.JAXWSProcessor;
import de.schlund.pfixcore.webservice.jsonqx.JSONQXProcessor;
import de.schlund.pfixcore.webservice.jsonws.JSONWSProcessor;
import de.schlund.pfixcore.webservice.jsonws.JSONWSStubGenerator;
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

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        synchronized (initLock) {
            runtime = (ServiceRuntime) getServletContext().getAttribute(ServiceRuntime.class.getName());
            if (runtime == null) {
                String servletProp = config.getInitParameter(Constants.PROP_SERVLET_FILE);
                if (servletProp != null) {
                    FileResource wsConfFile = ResourceUtil.getFileResourceFromDocroot(servletProp);
                    try {
                        Configuration srvConf = ConfigurationReader.read(wsConfFile);
                        if (srvConf.getGlobalServiceConfig().getContextName() == null && config.getInitParameter(Constants.PROP_CONTEXT_NAME) != null) {
                            srvConf.getGlobalServiceConfig().setContextName(config.getInitParameter(Constants.PROP_CONTEXT_NAME));
                        }
                        runtime = new ServiceRuntime();
                        runtime.setConfiguration(srvConf);
                        runtime.setApplicationServiceRegistry(new ServiceRegistry(runtime.getConfiguration(),
                                ServiceRegistry.RegistryType.APPLICATION));
                        runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_SOAP, new JAXWSProcessor(getServletContext()));
                        URL metaURL = srvConf.getGlobalServiceConfig().getDefaultBeanMetaDataURL();
                        runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_JSONWS, new JSONWSProcessor(metaURL));
                        runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_JSONQX, new JSONQXProcessor(metaURL));
                        runtime.addServiceStubGenerator(Constants.PROTOCOL_TYPE_JSONWS, new JSONWSStubGenerator());
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

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            runtime.process(req, res);
        } catch (Throwable t) {
            LOG.error("Error while processing webservice request", t);
            if (!res.isCommitted()) throw new ServletException("Error while processing webservice request.", t);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        adminWebapp.doGet(req, res);
    }
    
}