package de.schlund.pfixcore.util;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperInfo;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.config.IWrapperConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Describe class TransformerCallback here.
 * 
 * 
 * Created: Tue Jul 4 14:45:43 2006
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TransformerCallback {
    
    private static Logger LOG=Logger.getLogger(TransformerCallback.class);

    private static DocumentBuilderFactory  docBuilderFactory = DocumentBuilderFactory.newInstance();
    
    public static void setNoStore(SPDocument spdoc) {
        spdoc.setNostore(true);
    }

    public static int isAccessible(RequestContextImpl requestcontext, String pagename) throws Exception {
        try {
            ContextImpl context = requestcontext.getParentContext();
            if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                boolean retval;
                if (context.getContextConfig().isSynchronized()) {
                    synchronized (context) {
                        retval = check.isPageAccessible(pagename);
                    }
                } else {
                    retval = check.isPageAccessible(pagename);
                }
                if (retval) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static int isVisited(RequestContextImpl requestcontext, String pagename) throws Exception {
        try {
            ContextImpl context = requestcontext.getParentContext();
            if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                boolean retval;
                if (context.getContextConfig().isSynchronized()) {
                    synchronized (context) {
                        retval = check.isPageAlreadyVisited(pagename);
                    }
                } else {
                    retval = check.isPageAlreadyVisited(pagename);
                }
                if (retval) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static String getToken(RequestContextImpl requestContext, String tokenName) throws Exception {
        try {
            tokenName = tokenName.trim();
            if (tokenName.contains(":")) throw new IllegalArgumentException("Illegal token name: " + tokenName);
            String token = requestContext.getParentContext().getToken(tokenName);
            return token;
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static Node getIWrapperInfo(RequestContextImpl requestContext, Node docNode, String pageName, String prefix) {
        try {
            ContextImpl context = requestContext.getParentContext();
            XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
            if(pageName==null || pageName.equals("")) {
                PageRequest pg=requestContext.getCurrentPageRequest();
                if(pg!=null) pageName=pg.getName();
                else throw new IllegalArgumentException("Missing page name");
            }
            PageRequestConfig pageConfig = context.getContextConfig().getPageRequestConfig(pageName);
            if(pageConfig!=null) {
                Map<String, ? extends IWrapperConfig> iwrappers = pageConfig.getIWrappers();
                Class<? extends IWrapper> iwrpClass=null;
                IWrapperConfig iwrpConfig = iwrappers.get(prefix);
                if (iwrpConfig != null) {
                    iwrpClass=(Class<? extends IWrapper>)iwrpConfig.getWrapperClass();
                } else if(pageConfig.getAuthWrapperPrefix()!=null && pageConfig.getAuthWrapperPrefix().equals(prefix)) {
                    iwrpClass=(Class<? extends IWrapper>)pageConfig.getAuthWrapperClass();
                } else {
                    Map<String,Class<?>> auxWrappers=pageConfig.getAuxWrappers();
                    iwrpClass=(Class<? extends IWrapper>)auxWrappers.get(prefix);
                }
                if(iwrpClass!=null) return IWrapperInfo.getDocument(iwrpClass, xsltVersion);
            }
            return null;
        } catch(RuntimeException x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static Node getIWrappers(RequestContextImpl requestContext, Node docNode, String pageName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
            DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("iwrappers");
            doc.appendChild(root);
            if(pageName==null || pageName.equals("")) {
                PageRequest pg=requestContext.getCurrentPageRequest();
                if(pg!=null) pageName=pg.getName();
                else throw new IllegalArgumentException("Missing page name");
            }
            PageRequestConfig pageConfig = context.getContextConfig().getPageRequestConfig(pageName);
            if(pageConfig!=null) {
                if(pageConfig.getAuthWrapperPrefix()!=null) {
                    Element elem=doc.createElement("iwrapper");
                    elem.setAttribute("type","auth");
                    elem.setAttribute("prefix",pageConfig.getAuthWrapperPrefix());
                    root.appendChild(elem);
                }
                Map<String,Class<?>> auxWrappers=pageConfig.getAuxWrappers();
                for(String prefix:auxWrappers.keySet()) {
                    Element elem=doc.createElement("iwrapper");
                    elem.setAttribute("type","aux");
                    elem.setAttribute("prefix",prefix);
                    root.appendChild(elem);
                }
                Map<String, ? extends IWrapperConfig> iwrappers = pageConfig.getIWrappers();
                for(String prefix:iwrappers.keySet()) {
                    Element elem=doc.createElement("iwrapper");
                    elem.setAttribute("prefix",prefix);
                    root.appendChild(elem);
                }          
            }
            if (LOG.isDebugEnabled()) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                t.transform(new DOMSource(doc), new StreamResult(writer));
                LOG.debug(writer.toString());
            }
            Node iwrpDoc = Xml.parse(xsltVersion, doc);
            return iwrpDoc;
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
}
