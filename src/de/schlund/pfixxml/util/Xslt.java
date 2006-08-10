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
package de.schlund.pfixxml.util;

import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.tinytree.TinyDocumentImpl;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetImpl;

public class Xslt {
    private static final Logger LOG = Logger.getLogger(Xslt.class);

    private static final TransformerFactory ifactory = new TransformerFactoryImpl();

    private static final ThreadLocal<TransformerFactory> threadfactory = new ThreadLocal<TransformerFactory>();

    //-- load documents

    public synchronized static Transformer createIdentityTransformer() {
        try {
            return ifactory.newTransformer();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
    
    // pretty-print script by M. Kay, see 
    // http://www.cafeconleche.org/books/xmljava/chapters/ch17s02.html#d0e32721
    private static final String ID = "<?xml version='1.0'?>"
        + "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.1'>"
        + "  <xsl:output method='xml' indent='yes'/>"
        + "  <xsl:strip-space elements='*'/>"
        + "  <xsl:template match='/'>" + "    <xsl:copy-of select='.'/>"
        + "  </xsl:template>" + "</xsl:stylesheet>";
    
    private static final Templates PP_XSLT;
    
    static {
        Source src;
        TransformerFactory factory;
        
        src = new SAXSource(Xml.createXMLReader(), new InputSource(new StringReader(ID)));
        factory = new TransformerFactoryImpl();
        try {
            PP_XSLT = factory.newTemplates(src);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static synchronized Transformer createPrettyPrinter() {
        try {
            return PP_XSLT.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @deprecated Use {@link #loadTemplates(FileResource)} instead
     */
    public static Templates loadTemplates(Path path) throws TransformerConfigurationException {
        return loadTemplates(new InputSource("file://" + path.resolve().getAbsolutePath()), null);
    }

    public static Templates loadTemplates(FileResource path) throws TransformerConfigurationException {
        return loadTemplates(path, null);
    }

    public static Templates loadTemplates(FileResource path, TargetImpl parent) throws TransformerConfigurationException {
        InputSource input;
        try {
            input = new InputSource(path.toURL().toString());
        } catch (MalformedURLException e) {
            throw new TransformerConfigurationException("\"" + path.toString() + "\" does not respresent a valid file", e);
        }
        return loadTemplates(input, parent);
    }
    
    private static Templates loadTemplates(InputSource input, TargetImpl parent) throws TransformerConfigurationException {
        Source src = new SAXSource(Xml.createXMLReader(), input);
        TransformerFactory factory = threadfactory.get();

        if (factory == null) {
            // Create a new factory. As we have to use a new URIResolver
            // for each transformation, we cannot reuse the same factory
            // in other threads
            factory = new TransformerFactoryImpl();
            factory.setErrorListener(new PFErrorListener());
            threadfactory.set(factory);
        }

        factory.setURIResolver(new FileResolver(parent));

        try {
            Templates retval = factory.newTemplates(src);
            return retval;
        } catch (TransformerConfigurationException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerConfigurationException in doLoadTemplates!\n");
            sb.append("Path: ").append(input.getSystemId()).append("\n");
            sb.append("Message and Location: ").append(e.getMessageAndLocation()).append("\n");
            Throwable cause = e.getException();
            if (cause == null)
                cause = e.getCause();
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            LOG.error(sb.toString());
            throw e;
        }
    }
    
    //-- apply transformation
    
    public static void transform(Document xml, Templates templates, Map params, Result result) throws TransformerException {
        Transformer trafo = templates.newTransformer();
        long start = 0;
        if (params != null) {
            for (Iterator e = params.keySet().iterator(); e.hasNext();) {
                String name  = (String) e.next();
                Object value = params.get(name);
                if (name != null && value != null) {
                    trafo.setParameter(name, value);
                }
            }
        }
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();
        trafo.transform((TinyDocumentImpl) Xml.parse(xml), result);
        if (LOG.isDebugEnabled()) {
            long stop = System.currentTimeMillis();
            LOG.debug("      ===========> Transforming and serializing took " + (stop - start) + " ms.");
        }
    }

    //--

    static class FileResolver implements URIResolver {
        private static final String SEP = File.separator;
        
        private TargetImpl parent;

        public FileResolver(TargetImpl parent) {
            this.parent = parent;
        }

        /**
         * Resolve file url relative to root. Before searching the file system, check
         * if there is a XML Target defined and use this instead.
         * @param base ignored, always relative to root 
         * */
        public Source resolve(String href, String base) throws TransformerException {
            URI uri;
            String path;
            FileResource file;
            boolean forcePfixroot = false;
            
            try {
                uri = new URI(href);
            } catch (URISyntaxException e) {
                return new StreamSource(href);
            }
            if (uri.getScheme() != null && !uri.getScheme().equals("pfixroot")) {
                // we don't handle uris with an explicit scheme
                return new StreamSource(href);
            }
            path = uri.getPath();
            if (uri.getScheme() != null && uri.getScheme().equals("pfixroot")) {
                forcePfixroot = true;
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            
            if (parent != null) {
                Target target = parent.getTargetGenerator().getTarget(path);
                if (target == null) {
                    target = parent.getTargetGenerator().createXMLLeafTarget(path);
                }
                
                Document dom;
                try {
                    dom = (Document) target.getDOM();
                } catch (TargetGenerationException e) {
                    throw new TransformerException("Could not retrieve target '"
                                                   + target.getTargetKey() + "' included by stylesheet!", e);
                }
                
                // If Document object is null, the file could not be found or read
                // so return null to tell the parser the URI could not be resolved
                if (dom == null) {
                    return null;
                }
                
                Source source = new DOMSource(dom);
                
                // There is a bug in Saxon 6.5.3 which causes
                // a NullPointerException to be thrown, if systemId
                // is not set
                source.setSystemId(target.getTargetGenerator().getDisccachedir().toURI().toString() + "/" + path);
                
                // Register included stylesheet with target
                parent.getAuxDependencyManager().addDependencyTarget(target.getTargetKey());
                return source;
            }
            
            file = ResourceUtil.getFileResourceFromDocroot(path);
            try {
                Source source = new StreamSource(file.toURL().toString());
                return source;
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }

    /**
     * Implementation of ErrorListener interface.
     */
    static class PFErrorListener implements ErrorListener {
        public void warning(TransformerException arg) throws TransformerException {
            throw arg;
        }

        public void error(TransformerException arg) throws TransformerException {
            throw arg;
        }

        public void fatalError(TransformerException arg) throws TransformerException {
            throw arg;
        }
    }
}
