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
package org.pustefixframework.http.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.pustefixframework.config.generic.PropertyFileReader;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.FactoryInitException;
import de.schlund.pfixxml.FactoryInitUtil;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.SimpleResolver;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;
import de.schlund.pfixxml.util.logging.ProxyLogUtil;

/**
 * This Servlet is just there to have it's init method called on startup of the
 * VM. It starts all VM-global factories by calling their 'init' method from the
 * {@link FactoryInit} interface. These factories are located by analyzing the
 * "servlet.propfile" parameter which points to a file where all factories are
 * listed.
 */
public class FactoryInitWorker {

    /**
     * 
     */
    private static final long serialVersionUID = 3072991705791635451L;

    // ~ Instance/static variables
    // ..................................................................
    private final static String PROP_LOG4J = "pustefix.log4j.config";

    private final static String PROP_PREFER_CONTAINER_LOGGING = "pustefix.logging.prefercontainer";

    private final static Logger LOG = Logger.getLogger(FactoryInitWorker.class);

    private static boolean configured = false;
    
    private static FactoryInitException initException;

    private static String log4jconfig = null;

    private static long log4jmtime = -1;
    
    private boolean warMode = false;
    private boolean docrootSpecified = false;
    
    private final static Object initLock = new Object();
    private static boolean initRunning = false;

    public static void tryReloadLog4j() {
        if (log4jconfig != null) {
            FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
            long tmpmtime = l4jfile.lastModified();
            if (tmpmtime > log4jmtime) {
                LOG.error("\n\n################################\n"
                        + "#### Reloading log4j config ####\n"
                        + "################################\n");
                try {
                    configureLog4j(l4jfile);
                } catch (FileNotFoundException e) {
                    Logger.getLogger(FactoryInitWorker.class).error(
                            "Reloading log4j config failed!", e);
                } catch (SAXException e) {
                    Logger.getLogger(FactoryInitWorker.class).error(
                            "Reloading log4j config failed!", e);
                } catch (IOException e) {
                    Logger.getLogger(FactoryInitWorker.class).error(
                            "Reloading log4j config failed!", e);
                }
                log4jmtime = tmpmtime;
            }
        }
    }
    
    public static void init(ServletContext servletContext) throws ServletException {
        synchronized (initLock) {
            if (!initRunning) {
                initRunning = true;
                (new FactoryInitWorker()).doInit(servletContext);
            }
        }
    }
    
    /**
     * Initialize this servlet. Also call the 'init' method of all classes
     * listed in the configuration. These classes must implement the FactoryInit
     * interface.
     * 
     * @param config
     *            the servlet configuration
     * @see javax.servlet.Servlet#init(ServletConfig)
     * @throws ServletException
     *             on errors
     */
    @SuppressWarnings("deprecation")
    private void doInit(ServletContext servletContext) throws ServletException {
        
        try {
        
            Properties properties = new Properties(System.getProperties());
            
            // old webapps specify docroot -- true webapps don't
            String docrootstr = servletContext.getInitParameter("pustefix.docroot");
            if (docrootstr != null && !docrootstr.equals("")) {
                docrootSpecified = true;
            } else {
                docrootstr = servletContext.getRealPath("/");
                if (docrootstr == null) {
                    warMode = true;
                }
            }
    
            // Setup global configuration before doing anything else
            if (docrootstr != null) {
                if (!docrootstr.equals(GlobalConfig.getDocroot())) {
                    GlobalConfigurator.setDocroot(docrootstr);
                }
            }
            if (warMode) {
                GlobalConfigurator.setServletContext(servletContext);
            }
            
            if (docrootstr != null) {
                // For compatibility with old apps, initialize PathFactory
                de.schlund.pfixxml.PathFactory.getInstance().init(docrootstr);
            }
    
            String confname = "WEB-INF/factory.xml";
            if (confname != null) {
                FileResource confFile = ResourceUtil.getFileResourceFromDocroot(confname);
                try {
                    PropertyFileReader.read(confFile, properties);
                } catch (IOException e) {
                    throw new ServletException("*** [" + confname + "] Not found: "
                            + e.toString(), e);
                } catch (ParserException e) {
                    throw new ServletException("*** [" + confname + "] Parsing-error: "
                            + e.toString(), e);
                }
            } else {
                throw new ServletException(
                        "*** FATAL: Need the servlet.propfile property as init parameter! ***");
            }
            
            if (docrootstr != null) {
                // this is for stuff that can't use the PathFactory. Should not be used
                // when possible...
                properties.setProperty("pustefix.docroot", docrootstr);
            }
    
    
            configureLogging(properties, servletContext);
            LOG.debug(">>>> LOG4J Init OK <<<<");
        
            FactoryInitUtil.initialize(properties);
            
        } catch (FactoryInitException e) {
            initException = e;
            throw new ServletException(e.getCause().toString());
        } catch (ServletException e) {
            initException = new FactoryInitException("<init>", (e.getRootCause()==null?e:e.getRootCause()));
            throw e;
        } catch (RuntimeException e) {
            initException = new FactoryInitException("<init>", e);
            throw e;
        }
        LOG.debug("***** INIT of FactoryInitServlet done *****");

    }

    private void configureLogging(Properties properties, ServletContext servletContext) throws ServletException {
        String containerProp = properties.getProperty(PROP_PREFER_CONTAINER_LOGGING);
        if (warMode || (!docrootSpecified && (containerProp != null && containerProp.toLowerCase().equals("true")))) {
            ProxyLogUtil.getInstance().configureLog4jProxy();
            ProxyLogUtil.getInstance().setServletContext(servletContext);
        } else {
            log4jconfig = properties.getProperty(PROP_LOG4J);
            if (log4jconfig == null || log4jconfig.equals("")) {
                throw new ServletException("*** FATAL: Need the pustefix.log4j.config property in factory.xml! ***");
            }
            FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
            try {
                configureLog4j(l4jfile);
            } catch (FileNotFoundException e) {
                throw new ServletException(l4jfile + ": file for log4j configuration not found!", e);
            } catch (SAXException e) {
                throw new ServletException(l4jfile + ": error on parsing log4j configuration file", e);
            } catch (IOException e) {
                throw new ServletException(l4jfile + ": error on reading log4j configuration file!", e);
            }

        }
    }

    private static void configureLog4j(FileResource configFile) throws SAXException, FileNotFoundException, IOException {
        log4jmtime = configFile.lastModified();
        XMLReader xreader = XMLReaderFactory.createXMLReader();
        TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(
                        "Failed to configure TransformerFactory!", e);
            }
            DOMResult dr = new DOMResult();
            th.setResult(dr);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler cushandler = new CustomizationHandler(dh);
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);
            xreader.parse(new InputSource(configFile.getInputStream()));
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            try {
                Transformer t = SimpleResolver.configure(tf, "/pustefix/xsl/log4j.xsl");
                t.transform(new DOMSource(dr.getNode()), new StreamResult(bufferStream));
            } catch (TransformerException e) {
                throw new SAXException(e);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setNamespaceAware(true);
            Document confDoc;
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                db.setEntityResolver(new EntityResolver() {

                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        if (systemId.equals("http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd")) {
                            return new InputSource(ResourceUtil.getFileResourceFromDocroot("core/schema/log4j.dtd").getInputStream());
                        }
                        return null;
                    }
                    
                });
                db.setErrorHandler(new ErrorHandler() {

                    public void warning(SAXParseException exception) throws SAXException {
                        System.err.println("Warning while parsing log4j configuration: ");
                        exception.printStackTrace(System.err);
                    }

                    public void error(SAXParseException exception) throws SAXException {
                        System.err.println("Error while parsing log4j configuration: ");
                        exception.printStackTrace(System.err);
                    }

                    public void fatalError(SAXParseException exception) throws SAXException {
                        System.err.println("Fatal error while parsing log4j configuration: ");
                        exception.printStackTrace(System.err);                    }
                    
                });
                confDoc = db.parse(new ByteArrayInputStream(bufferStream.toByteArray()));
            } catch (SAXException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (ParserConfigurationException e) {
                String msg = "Error while trying to create DOM document";
                throw new RuntimeException(msg, e);
            }
            DOMConfigurator.configure(confDoc.getDocumentElement());
        } else {
            throw new RuntimeException(
                    "Could not get instance of SAXTransformerFactory!");
        }
    }

    public static boolean isConfigured() {
        return configured;
    }

    public static FactoryInitException getInitException() {
        return initException;
    }

}
