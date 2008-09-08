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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
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
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetImpl;

public class Xslt {
    
    private static final Logger LOG = Logger.getLogger(Xslt.class);
    
    //-- load documents

    public synchronized static Transformer createIdentityTransformer(XsltVersion xsltVersion) {
        try {
            TransformerFactory trfFact=XsltProvider.getXsltSupport(xsltVersion).getSharedTransformerFactory();
            return trfFact.newTransformer();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static synchronized Transformer createPrettyPrinter(XsltVersion xsltVersion) {
        try {
            return XsltProvider.getXsltSupport(xsltVersion).getPrettyPrinterTemplates().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @deprecated Use {@link #loadTemplates(FileResource)} instead
     */
    @Deprecated
    public static Templates loadTemplates(Path path) throws TransformerConfigurationException {
        return loadTemplates(XsltVersion.XSLT1, new InputSource("file://" + path.resolve().getAbsolutePath()), null);
    }

    public static Templates loadTemplates(XsltVersion xsltVersion, FileResource path) throws TransformerConfigurationException {
        return loadTemplates(xsltVersion, path, null);
    }

    public static Templates loadTemplates(XsltVersion xsltVersion, FileResource path, TargetImpl parent) throws TransformerConfigurationException {
        InputSource input;
        try {
            input = new InputSource(path.toURL().toString());
        } catch (MalformedURLException e) {
            throw new TransformerConfigurationException("\"" + path.toString() + "\" does not respresent a valid file", e);
        }
        return loadTemplates(xsltVersion, input, parent);
    }
    
    private static Templates loadTemplates(XsltVersion xsltVersion, InputSource input, TargetImpl parent) throws TransformerConfigurationException {
        Source src = new SAXSource(Xml.createXMLReader(), input);
        TransformerFactory factory = XsltProvider.getXsltSupport(xsltVersion).getThreadTransformerFactory();
        factory.setErrorListener(new PFErrorListener());
        factory.setURIResolver(new FileResolver(parent,xsltVersion));
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
    public static void transform(Document xml, Templates templates, Map<String, Object> params, Result result) throws TransformerException {
        transform(xml, templates, params, result, null);
    }
    
    public static void transform(Document xml, Templates templates, Map<String, Object> params, Result result, String encoding) throws TransformerException {
        try {
            doTransform(xml,templates,params,result,encoding,false);
        } catch(UnsupportedOperationException x) {
            if(result instanceof StreamResult) {
                OutputStream out=((StreamResult)result).getOutputStream();
                if(out instanceof ByteArrayOutputStream) {
                    LOG.error("Try to transform again after UnsupportedOperationException",x);
                    ByteArrayOutputStream baos=(ByteArrayOutputStream)out;
                    baos.reset();
                    try {
                        doTransform(xml,templates,params,result,encoding,false);
                    } catch(UnsupportedOperationException ex) {
                        LOG.error("Try to transform and trace after UnsupportedOperationException",ex);
                        baos.reset();
                        doTransform(xml,templates,params,result,encoding,true);
                    }
                }
            }
        }
    }
    
    private static void doTransform(Document xml, Templates templates, Map<String, Object> params, Result result, String encoding, boolean trace) throws TransformerException {
        XsltVersion xsltVersion=getXsltVersion(templates);
        Transformer trafo = templates.newTransformer();
        if (encoding != null) {
            trafo.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        StringWriter traceWriter=null;
        if(trace) {
           traceWriter=new StringWriter();
           XsltProvider.getXsltSupport(xsltVersion).doTracing(trafo,traceWriter);
        }
        long start = 0;
        if (params != null) {
            for (Iterator<String> e = params.keySet().iterator(); e.hasNext();) {
                String name  = e.next();
                Object value = params.get(name);
                if (name != null && value != null) {
                    trafo.setParameter(name, value);
                }
            }
        }
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();
        try {
            ExtensionFunctionUtils.setExtensionFunctionError(null);
            trafo.transform(new DOMSource(Xml.parse(xsltVersion,xml)), result);
        } catch(TransformerException x) {
            Throwable t=ExtensionFunctionUtils.getExtensionFunctionError();
            if(t!=null) {
                ExtensionFunctionUtils.setExtensionFunctionError(null);
                throw new XsltExtensionFunctionException(t);
            }
            throw x;
        } finally {
           if(trace) {
              String traceStr=traceWriter.toString();
              int maxSize=10000;
              if(traceStr.length()>maxSize) {
                 traceStr=traceStr.substring(traceStr.length()-maxSize);
                 int ind=traceStr.indexOf('\n');
                 if(ind>-1) traceStr=traceStr.substring(ind); 
              }
              LOG.error("Last trace steps:\n"+traceStr);
           }
        }
        if (LOG.isDebugEnabled()) {
            long stop = System.currentTimeMillis();
            LOG.debug("      ===========> Transforming and serializing took " + (stop - start) + " ms.");
        }
    }
    
    //--

    private static XsltVersion getXsltVersion(Templates templates) {
        Iterator<Map.Entry<XsltVersion,XsltSupport>> it=XsltProvider.getXsltSupport().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<XsltVersion,XsltSupport> entry=it.next();
            if(entry.getValue().isInternalTemplate(templates)) return entry.getKey();
        }
        return null;
    }
    
    
    static class FileResolver implements URIResolver {
        private TargetImpl parent;
        private XsltVersion xsltVersion;

        public FileResolver(TargetImpl parent, XsltVersion xsltVersion) {
            this.parent = parent;
            this.xsltVersion = xsltVersion;
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
            //Rewrite include href to xslt version specific file, that's necessary cause
            //the XSLT1 and XSLT2 extension functions are incompatible and we want
            //to support using XSLT1 (Saxon 6.5.x) or XSLT2 (Saxon 8.x) without the
            //need to have both versions installed, thus the extension functions can't be
            //referenced within the same stylesheet and we rewrite to the according 
            //version specific stylesheet here
            if(href.equals("core/xsl/include.xsl")) {
                if(xsltVersion==XsltVersion.XSLT2) href="core/xsl/include_xslt2.xsl";
            }
            
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
            LOG.error("WARNING: "+arg.getMessage());
            throw arg;
        }

        public void error(TransformerException arg) throws TransformerException {
            LOG.error("ERROR: "+arg.getMessage());
            throw arg;
        }

        public void fatalError(TransformerException arg) throws TransformerException {
            LOG.error("FATAL ERROR: "+arg.getMessage());
            throw arg;
        }
    }
}
