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
package org.pustefixframework.http.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.pustefixframework.admin.mbeans.Admin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.util.JarFileCache;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;

/**
 * This Servlet is just there to have it's init method called on startup of the
 * VM. It starts all VM-global factories by calling their 'init' method from the
 * {@link FactoryInit} interface. These factories are located by analyzing the
 * "servlet.propfile" parameter which points to a file where all factories are
 * listed.
 */
public class PustefixInit {

    // ~ Instance/static variables
    // ..................................................................

    private final static Logger LOG = Logger.getLogger(PustefixInit.class);
    
    private final static String log4jconfig = "/WEB-INF/pfixlog.xml";
    public final static String SERVLET_CONTEXT_ATTRIBUTE_NAME = "___PUSTEFIX_INIT___";

    private long log4jmtime = -1;
    private boolean initDone;
    
    public void tryReloadLog4j() {
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
                    Logger.getLogger(PustefixInit.class).error(
                            "Reloading log4j config failed!", e);
                } catch (SAXException e) {
                    Logger.getLogger(PustefixInit.class).error(
                            "Reloading log4j config failed!", e);
                } catch (IOException e) {
                    Logger.getLogger(PustefixInit.class).error(
                            "Reloading log4j config failed!", e);
                }
                log4jmtime = tmpmtime;
            }
        }
    }
    
    public PustefixInit(ServletContext servletContext) throws PustefixCoreException {
        this(servletContext, null);
    }
    
    public PustefixInit(ServletContext servletContext, String docrootstr) throws PustefixCoreException {
        
        //avoid re-initializations, e.g. when ApplicationContext is refreshed
        if(initDone) return;
        
    	Properties properties = new Properties(System.getProperties());
    	
    	try {
    	    final File cacheDir = PustefixTempDirs.getInstance(servletContext).createTempDir("pustefix-jar-cache-");
    	    JarFileCache.setCacheDir(cacheDir);
    	} catch(IOException x) {
    	    throw new RuntimeException("Error creating temporary directory for JAR caching", x);
    	}
    	
    	//override environment properties by according context init parameters
    	Enumeration<?> names = servletContext.getInitParameterNames();
    	while(names.hasMoreElements()) {
    	    String name = (String)names.nextElement();
    	    String value = servletContext.getInitParameter(name);
            if(value != null && !value.equals("")) {
                EnvironmentProperties.getProperties().put(name, value);
            }
    	}
    	
    	if(docrootstr == null) {
    	    docrootstr = servletContext.getRealPath("/");
    	    if (docrootstr == null) {
    	        GlobalConfigurator.setServletContext(servletContext);
    	    } else {
    	        if (!docrootstr.equals(GlobalConfig.getDocroot())) {
    	            GlobalConfigurator.setDocroot(docrootstr);
    	        }
    	    }
    	} else {
    	    GlobalConfigurator.setDocroot(docrootstr);
    	}
    	
    	configureLogging(properties, servletContext);
    	LOG.debug(">>>> LOG4J Init OK <<<<");

    	initAdminMBean();
    	
    	initDone = true;

    }

    private void configureLogging(Properties properties, ServletContext servletContext) throws PustefixCoreException {
        
    	FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
    	
    	try {
    		configureLog4j(l4jfile);
    	} catch (FileNotFoundException e) {
    		throw new PustefixCoreException(l4jfile + ": file for log4j configuration not found!", e);
    	} catch (SAXException e) {
    		throw new PustefixCoreException(l4jfile + ": error on parsing log4j configuration file", e);
    	} catch (IOException e) {
    		throw new PustefixCoreException(l4jfile + ": error on reading log4j configuration file!", e);
    	}

    }

    private void configureLog4j(FileResource configFile) throws SAXException, FileNotFoundException, IOException {
        log4jmtime = configFile.lastModified();
        Document confDoc = readLoggingConfig(configFile.getInputStream(), configFile.getURI().toString(), true);
        DOMConfigurator.configure(confDoc.getDocumentElement());
    }

    static Document readLoggingConfig(InputStream in, String systemID, boolean validating) 
            throws SAXException, FileNotFoundException, IOException {
        
        XMLReader xreader = XMLReaderFactory.createXMLReader();
        TransformerFactory tf = TransformerFactory.newInstance();
        SAXTransformerFactory stf = (SAXTransformerFactory) tf;
        TransformerHandler th;
        try {
            th = stf.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to configure TransformerFactory!", e);
        }
        DOMResult dr = new DOMResult();
        th.setResult(dr);
        DefaultHandler dh = new TransformerHandlerAdapter(th);
        CustomizationHandler cushandler = new CustomizationHandler(dh);
        cushandler.setFallbackDocroot();
        xreader.setContentHandler(cushandler);
        xreader.setDTDHandler(cushandler);
        xreader.setErrorHandler(cushandler);
        xreader.setEntityResolver(cushandler);
        InputSource source = new InputSource(in);
        source.setSystemId(systemID);
        xreader.parse(source);
        ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
        try {
            URIResolver resolver = new Resolver(tf.getURIResolver()); 
            tf.setURIResolver(resolver);
            Transformer transformer = tf.newTransformer(new StreamSource(PustefixInit.class.getResource("/pustefix/xsl/log4j.xsl").toString()));
            transformer.setURIResolver(resolver);
            transformer.transform(new DOMSource(dr.getNode()), new StreamResult(bufferStream));
        } catch (TransformerException e) {
            throw new SAXException(e);
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(validating);
        dbf.setNamespaceAware(true);
        Document confDoc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new EntityResolver() {
                
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.equals("http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd")) {
                        return new InputSource(getClass().getClassLoader().getResourceAsStream("PUSTEFIX-INF/schema/log4j.dtd"));
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
        return confDoc;
    }

    private static void initAdminMBean() {
        String mode = EnvironmentProperties.getProperties().getProperty("mode");
        if(!mode.equalsIgnoreCase("prod")) {
            try {
                String mletClass = "javax.management.loading.MLet";
                ObjectName mletName = new ObjectName(Admin.JMX_NAME + ",subtype=MLet");
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                if(!server.isRegistered(mletName)) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(null);
                        server.createMBean(mletClass, mletName);
                        LOG.debug("Created AdminMlet.");
                        Object mletParams[] = {PustefixInit.class.getProtectionDomain().getCodeSource().getLocation()};
                        String mletSignature[] = {"java.net.URL"};
                        server.invoke(mletName, "addURL", mletParams, mletSignature);
                        String mbeanClass = "org.pustefixframework.admin.mbeans.Admin";
                        ObjectName mbeanName = new ObjectName(Admin.JMX_NAME);
                        if(!server.isRegistered(mbeanName)) {
                            Object[] params = new Object[] {findFreePort()};
                            String[] signature = new String[] {"int"};
                            server.createMBean(mbeanClass, mbeanName, mletName, params, signature);
                            LOG.debug("Created Admin mbean.");
                        } else LOG.debug("Already found a registered Admin mbean.");
                    } finally {
                        Thread.currentThread().setContextClassLoader(cl);
                    }
                } else LOG.debug("Already found a registered AdminMLet.");
            } catch(Exception x) {
                LOG.error("Can't register Admin MBean", x);
            }
        }
    }
    
    private static int findFreePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch(IOException x) {
            throw new RuntimeException("Can't get free port", x);
        }
    }
    
   
    static class Resolver implements URIResolver {
        
        private URIResolver defaultResolver;
        
        public Resolver(URIResolver defaultResolver) {
            this.defaultResolver = defaultResolver;
        }

        public Source resolve(String href, String base) throws TransformerException {
            
            if(href.startsWith("module:")) {
                Resource res = ResourceUtil.getResource(href);
                try {
                    Document doc = readLoggingConfig(res.getInputStream(), res.toURI().toString(), false);
                    return new DOMSource(doc);
                } catch (Exception x) {
                    throw new TransformerException("Error resolving includes", x);
                }
                
            } else if(href.startsWith("classpath:")) {
                try {
                    String path = href.substring(10);
                    if(path.startsWith("/") && path.length() > 1) {
                    	path = path.substring(1);
                    }
                    Enumeration<URL> urls = PustefixInit.class.getClassLoader().getResources(path);
                    if(urls.hasMoreElements()) {
                        try {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            factory.setNamespaceAware(true);
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            Document includesDoc = builder.newDocument();
                            Element includesElem = includesDoc.createElement("includes");
                            includesDoc.appendChild(includesElem);
                            while(urls.hasMoreElements()) {
                                URL url = urls.nextElement();
                                Document doc = readLoggingConfig(url.openStream(), url.toString(), false);
                                Element root = doc.getDocumentElement();
                                if(root.hasChildNodes()) {
                                    NodeList nodes = root.getChildNodes();
                                    for(int i=0; i<nodes.getLength(); i++) {
                                        Node node = includesDoc.importNode(nodes.item(i), true);
                                        includesElem.appendChild(node);
                                    }
                                }
                            }
                            return new DOMSource(includesElem);
                        } catch(Exception x) {
                            throw new TransformerException("Error resolving includes", x);
                        }
                    }
                } catch (IOException e) {
                    throw new TransformerException("Error during resource resolving: " + href);
                }
            }
            String ref;
            if (href.contains(":")) {
                ref = href;
            } else {
                int idx = base.lastIndexOf('/');
                if (idx == -1) {
                    ref = href;
                } else {
                    ref = base.substring(0, idx) + "/" + href;
                }
            }
            return defaultResolver == null ? null : defaultResolver.resolve(ref, base);
        }
    }
}