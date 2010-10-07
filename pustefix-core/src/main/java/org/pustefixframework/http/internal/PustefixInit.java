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
import java.util.Properties;

import javax.servlet.ServletContext;
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
import org.w3c.dom.Document;
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
public class PustefixInit {

    /**
     * 
     */
    private static final long serialVersionUID = 3072991705791635451L;

    // ~ Instance/static variables
    // ..................................................................

    private final static Logger LOG = Logger.getLogger(PustefixInit.class);
    
    private static String log4jconfig = "/WEB-INF/pfixlog.xml";

    private static long log4jmtime = -1;
    private static boolean warMode = false;
    
    private static boolean initDone;
    
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
    
    public static void init(ServletContext servletContext) throws PustefixCoreException {
        
        //avoid re-initializations, e.g. when ApplicationContext is refreshed
        if(initDone) return;
        
    	Properties properties = new Properties(System.getProperties());
        
    	File tempDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
    	if(tempDir != null && !tempDir.equals("")) {
    	    File cacheDir = new File(tempDir, "pustefix-jar-cache");
    	    JarFileCache.setCacheDir(cacheDir);
    	}
    	
    	// old webapps specify docroot -- true webapps don't
    	String docrootstr = servletContext.getInitParameter("pustefix.docroot");
    	if (docrootstr == null || docrootstr.equals("")) {
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
    		// this is for stuff that can't use the PathFactory. Should not be used
    		// when possible...
    		properties.setProperty("pustefix.docroot", docrootstr);
    	}
    
    	configureLogging(properties, servletContext);
    	LOG.debug(">>>> LOG4J Init OK <<<<");
    	
    	initDone = true;

    }

    private static void configureLogging(Properties properties, ServletContext servletContext) throws PustefixCoreException {
        
    	FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
    	
        if(!l4jfile.exists()) {
            ProxyLogUtil.getInstance().configureLog4jProxy();
            ProxyLogUtil.getInstance().setServletContext(servletContext);
        } else {
        	
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
            CustomizationHandler cushandler = new CustomizationHandler(dh);
            cushandler.setFallbackDocroot();
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
                            return new InputSource(ResourceUtil.getResource("module://pustefix-core/schema/log4j.dtd").getInputStream());
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

}
