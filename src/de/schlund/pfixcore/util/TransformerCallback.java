package de.schlund.pfixcore.util;

import java.util.Map;

import org.w3c.dom.Node;

import de.schlund.pfixcore.generator.IWrapperInfo;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.config.IWrapperConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
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

    public static void setNoStore(SPDocument spdoc) {
        spdoc.setNostore(true);
    }

    public static int isAccessible(RequestContextImpl requestcontext, String pagename) throws Exception {
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
    }

    public static int isVisited(RequestContextImpl requestcontext, String pagename) throws Exception {
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
    }
    
    public static String getToken(RequestContextImpl requestContext, String tokenName) throws Exception {
        tokenName = tokenName.trim();
        if (tokenName.contains(":")) throw new IllegalArgumentException("Illegal token name: " + tokenName);
        String token = requestContext.getParentContext().getToken(tokenName);
        return token;
    }
    
    public static Node getIWrapperInfo(RequestContextImpl requestContext, Node docNode, String pageName, String prefix) {
        ContextImpl context = requestContext.getParentContext();
        XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
        PageRequestConfig pageConfig = context.getContextConfig().getPageRequestConfig(pageName);
        Map<String, ? extends IWrapperConfig> iwrappers = pageConfig.getIWrappers();
        IWrapperConfig iwrpConfig = iwrappers.get(prefix);
        if (iwrpConfig != null) {
            return IWrapperInfo.getDocument(iwrpConfig, xsltVersion);
        } else
            return null;
            // throw new RuntimeException("IWrapper with prefix '" + prefix + "' on page '" + pageName + "' not found.");
    }
    
}
