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
package de.schlund.pfixxml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.xmlgenerator.targets.LeafTarget;
import org.pustefixframework.xmlgenerator.targets.Target;
import org.pustefixframework.xmlgenerator.targets.TargetGenerationException;
import org.pustefixframework.xmlgenerator.targets.TargetImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


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
    
    public static Templates loadTemplates(XsltVersion xsltVersion, InputStreamResource path) throws TransformerConfigurationException {
    	return loadTemplates(xsltVersion, path, null);
    }
    
    public static Templates loadTemplates(XsltVersion xsltVersion, InputStream in) throws TransformerConfigurationException {
    	return loadTemplates(xsltVersion, new InputSource(in), null);
    }
    
    public static Templates loadTemplates(XsltVersion xsltVersion, InputStreamResource res, TargetImpl parent) throws TransformerConfigurationException {
        InputSource input;
        try {
            input = new InputSource();
            input.setSystemId(res.getURI().toASCIIString());
            input.setByteStream(res.getInputStream());
        } catch (MalformedURLException e) {
            throw new TransformerConfigurationException("\"" + res.toString() + "\" does not respresent a valid file", e);
        } catch(IOException x) {
        	throw new TransformerConfigurationException("Can't read template resource: " + res.toString(), x);
        }
        return loadTemplates(xsltVersion, input, parent);
    }
    
    private static Templates loadTemplates(XsltVersion xsltVersion, InputSource input, TargetImpl parent) throws TransformerConfigurationException {
        try {
            return loadTemplates(xsltVersion, input, parent, false);
        } catch (TransformerConfigurationException e) {
            return loadTemplates(xsltVersion, input, parent, true);
        }
    }
    
    private static Templates loadTemplates(XsltVersion xsltVersion, InputSource input, TargetImpl parent, boolean debug) throws TransformerConfigurationException {
        Source src = new SAXSource(Xml.createXMLReader(), input);
        TransformerFactory factory = XsltProvider.getXsltSupport(xsltVersion).getThreadTransformerFactory();
        PFErrorListener errorListener = new PFErrorListener();
        factory.setErrorListener(errorListener);
        factory.setURIResolver(new ResourceResolver(parent,xsltVersion,debug));
        try {
            Templates retval = factory.newTemplates(src);
            return retval;
        } catch (TransformerConfigurationException e) {
        	e.printStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerConfigurationException in doLoadTemplates!\n");
            sb.append("Path: ").append(input.getSystemId()).append("\n");
            sb.append("Message and Location: ").append(e.getMessageAndLocation()).append("\n");
          
            
            List<TransformerException> errors = errorListener.getErrors();
            if(e.getException() == null && e.getCause() == null && errors.size() > 0) {
                if(e != errors.get(0)) {
                    TransformerException last = e;
                    final int maxDepth = 10;
                    for(int i=errors.size()-1; i>-1 && (errors.size()-i)<=maxDepth && last.getCause()==null; i--) {
                        if(last != errors.get(i)) last.initCause(errors.get(i));
                        last=errors.get(i);
                    }
                }
            }
            
            Throwable cause = e.getException();
            if (cause == null) cause = e.getCause();
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
            trafo.setErrorListener(new PFErrorListener());
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
    
    
    static class ResourceResolver implements URIResolver {
        
        private TargetImpl parent;
        private XsltVersion xsltVersion;
        private boolean debug;
        
        public ResourceResolver(TargetImpl parent, XsltVersion xsltVersion, boolean debug) {
            this.parent = parent;
            this.xsltVersion = xsltVersion;
            this.debug = debug;
        }

        /**
         * Resolve file url relative to root. Before searching the file system, check
         * if there is a XML Target defined and use this instead.
         * @param base ignored, always relative to root 
         * */
        public Source resolve(String href, String base) throws TransformerException {
            URI uri;
            String path;
            Resource resource;
            
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
            
            path = uri.getPath();
         
            if("bundle".equals(uri.getScheme())) {
                path = uri.toString();
            }
            
            if (parent != null) {
                Target target = parent.getTargetGenerator().getTarget(path);
                if (target == null) {
                	if(path.equals("")) throw new IllegalArgumentException("Empty path ["+ href + "| " + base +"]");
                    target = parent.getTargetGenerator().createXMLLeafTarget(path);
                }
                
                if(! ( debug && target instanceof LeafTarget)) {
                
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
                    source.setSystemId(target.getTargetGenerator().getDisccachedir().getURI().toString() + "/" + path);
                
                    // Register included stylesheet with target
                    parent.getAuxDependencyManager().addDependencyTarget(target.getTargetKey());
                    return source;
                }
            }
            
            ResourceLoader resourceLoader = parent.getTargetGenerator().getResourceLoader();
            resource = resourceLoader.getResource(uri);
            if(resource == null) {
                throw new TransformerException("Resource can't be found: " + uri.toString());
            }
            try {
            	Source source = new StreamSource(((InputStreamResource)resource).getInputStream(), path);
            	return source;
            } catch(IOException x) {
            	throw new TransformerException("Can't read resource: " + path);
            }
        }
    }

    /**
     * Implementation of ErrorListener interface.
     */
    static class PFErrorListener implements ErrorListener {
        
        private List<TransformerException> errors = new ArrayList<TransformerException>();
        
        public List<TransformerException> getErrors() {
            return errors;
        }
        
        public void warning(TransformerException arg) throws TransformerException {
            LOG.error("WARNING: "+arg.getMessageAndLocation());
        }

        public void error(TransformerException arg) throws TransformerException {
            LOG.error("ERROR: "+arg.getMessageAndLocation());
            errors.add(arg);
            throw arg;
        }

        public void fatalError(TransformerException arg) throws TransformerException {
            LOG.error("FATAL ERROR: "+arg.getMessageAndLocation());
            errors.add(arg);
            throw arg;
        }
    }
}
