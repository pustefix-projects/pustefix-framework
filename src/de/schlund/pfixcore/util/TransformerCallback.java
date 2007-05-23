package de.schlund.pfixcore.util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.SPDocument;

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
        tokenName=tokenName.trim();
        if(tokenName.contains(":")) throw new IllegalArgumentException("Illegal token name: "+tokenName);
        String token=requestContext.getParentContext().getToken(tokenName);
        return token;
    } 
    
}
