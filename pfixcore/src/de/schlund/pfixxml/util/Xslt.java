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
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.tinytree.TinyDocumentImpl;
import net.sf.saxon.xpath.XPathException;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import de.schlund.pfixxml.targets.Path;

public class Xslt {
    private static final Category CAT = Category.getInstance(Xslt.class.getName());

    // TransformerFactory.newInstance() does not work with ant, since the Factory
    // does not seem to pick the correct classloader with saxon in its classpath.
    // Simple instantiation or classloading works, since the current classloader is defined
    // by ant and therefore has saxon in its classpath.
    // TODO_AH check if object creation is really necessary here
    private static final TransformerFactoryImpl factory;
    private static final URIResolver stdResolver;
    
    static {
        factory = new TransformerFactoryImpl();
        stdResolver = factory.getURIResolver();
        factory.setErrorListener(new PFErrorListener());
    }

    public static SAXSource createSaxSource(InputSource input) {
        return new SAXSource(Xml.createXMLReader(), input);
    }
     
    //-- load documents
    
    /**
     * Convert the document implementation which is used for write-access 
     * by {@link SPDocument} to the document implementation which is used 
     * by the XSLTProcessor. Note: Currently we convert here from a mutable
     * DOM implementation to an immutable NodeInfo implementation(saxon).
     * @param doc the document as source for conversion(mostly a Node implementation
     * when using xerces)
     * @return a document as result of conversion(currently saxons TinyDocumentImpl)
     * @throws Exception on all errors 
     */
    public static Document xmlObjectFromDocument(Document doc) {
        return xmlObjectFromDocument(doc, null);
    }

    public static Document xmlObjectFromDocument(Document doc, String systemid) {
        if (doc instanceof TinyDocumentImpl) {
            return doc;
        } else {
            DOMSource domsrc = new DOMSource(doc);
            domsrc.setSystemId(systemid);
            try {
                return xmlObjectFromSource(domsrc);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Create a document from a sourcefile in the filesystem. 
     * @param path the path to the source file in the filesystem
     * @return the created document(currenly saxons TinyDocumentImpl)
     * @throws TransformerException on errors
     */
    public static Document xmlObjectFromDisc(File file) throws TransformerException {
        String path = file.getAbsolutePath();
        SAXSource src = Xslt.createSaxSource(new InputSource("file://" + path));
        return xmlObjectFromSource(src);
    }

    // public static Document xmlObjectFromString(String str) throws TransformerException {
    //     SAXSource src = Xslt.createSaxSource(new InputSource(new StringReader(str)));
    //     return xmlObjectFromSource(src);
    // }

    private static Document xmlObjectFromSource(Source input) throws  TransformerException, TransformerConfigurationException {
        try {
            Transformer trans  = factory.newTransformer();
            DOMResult   result = new DOMResult();
            trans.transform(input, result);
            return (TinyDocumentImpl) result.getNode();
        } catch (XPathException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerException in xmlObjectFromDisc!\n");
            sb.append("Path: ").append(input.getSystemId()).append("\n");
            sb.append("Message and Location: ").append(e.getMessage()).append("\n");
            Throwable cause = e.getException();
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            CAT.error(sb.toString());
            throw e;
        }
    }
    
    //-- load transformer
    
    public static synchronized Transformer loadTransformer(File file) throws TransformerConfigurationException {
        factory.setURIResolver(stdResolver);
        return factory.newTransformer(new StreamSource(file));
    }
    
    public static synchronized Transformer loadTransformer(File docroot, String path) throws TransformerConfigurationException {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("absolute path expected: " + path);
            // otherwise, I'd construct an wrong uri
        }
        Source src;
        src = Xslt.createSaxSource(new InputSource("file://" + path));
        factory.setURIResolver(new FileResolver(docroot));
        try {
            return factory.newTransformer(src);
        } catch (TransformerConfigurationException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerConfigurationException in xslObjectFromDisc!\n");
            sb.append("Path: ").append(path).append("\n");
            sb.append("Message and Location: ").append(e.getMessageAndLocation()).append("\n");
            Throwable cause = e.getException();
            if (cause == null) {
                cause = e.getCause();
            }
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            CAT.error(sb.toString());
            throw e;
        }
    }

    
    //-- apply transformation

    public static Document transform(Document doc, Transformer trafo) throws TransformerException {
        DOMSource src = new DOMSource(doc);
        DOMResult result = new DOMResult();
        trafo.transform(src, result);
        return (Document) result.getNode();
    }

    /**
     * Do a transformation with a given source document, a stylesheet, parameters for
     * the transformator and write the result to a given outputstream.
     * @param xmlobj the source document. Note: Currently an instance of saxons
     * TinyDocumentImpl must be passed.  
     * @param xslobj the stylesheet. Note: Currently an instance of saxons
     * PerparedStyleSheet must be passed.
     * @param params parameters for the transformator
     * @param out the outputstream where the result is written to
     * @throws exception on all errors
     */
    public static void transform(Document xml, Transformer trafo, Map params, OutputStream out) throws TransformerException {
        transform(xml, trafo, params, new StreamResult(out));
    }

    public static synchronized void transform(Document xml, Transformer trafo, Map params, Result result) throws TransformerException {
        long start = 0;
        if (params != null) {
            for (Iterator e = params.keySet().iterator(); e.hasNext();) {
                String name  = (String) e.next();
                String value = (String) params.get(name);
                if (name != null && value != null) {
                    trafo.setParameter(name, value);
                }
            }
        }
        if (CAT.isDebugEnabled())
            start = System.currentTimeMillis();
        // do the transformation
        trafo.transform((TinyDocumentImpl) xml, result);
        if (CAT.isDebugEnabled()) {
            long stop = System.currentTimeMillis();
            CAT.debug("      ===========> Transforming and serializing took " + (stop - start)
                      + " ms.");
        }
    }
    
    //--
    
    static class FileResolver implements URIResolver {
    	private static final String SEP = File.separator; 
        
        // always with tailing /
        private final File root;
        
        public FileResolver(File root) {
            this.root = root;
        }
        
        /**
         * Resolve file url relative to root. 
         * @param base ignored, always relative to root 
         * */
        public Source resolve(String href, String base) throws TransformerException {
            URI uri;
            String path;
            File file;
            
            try {
                uri = new URI(href);
            } catch (URISyntaxException e) {
            	return new StreamSource(href);
            }
            if (uri.getScheme() != null) {
                // we don't handle uris with an explicit scheme
            	return new StreamSource(href);
            }
            path = uri.getPath();
            try {
                file = Path.create(root, path).resolve();
            } catch (IllegalArgumentException e) {
                throw new TransformerException("cannot resolve " + href, e);
            }
            return new StreamSource(file);
        }
    }
    
    /**
     * Implementation of ErrorListener interface.
     */
    static class PFErrorListener implements ErrorListener {
        
        /**
         * @see javax.xml.transform.ErrorListener#warning(javax.xml.transform.TransformerException)
         */
        public void warning(TransformerException arg0) throws TransformerException {
            throw arg0;
        }
        
        /**
         * @see javax.xml.transform.ErrorListener#error(javax.xml.transform.TransformerException)
         */
        public void error(TransformerException arg0) throws TransformerException {
            throw arg0;
        }

        /**
         * @see javax.xml.transform.ErrorListener#fatalError(javax.xml.transform.TransformerException)
         */
        public void fatalError(TransformerException arg0) throws TransformerException {
            throw arg0;
        }
    }
    
}
