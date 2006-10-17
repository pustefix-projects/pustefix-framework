package de.schlund.pfixcore.util;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.RequestContextImpl;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixxml.SPDocument;

/**
 * Describe class TransformerCallback here.
 *
 *
 * Created: Tue Jul  4 14:45:43 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TransformerCallback {

    public static void setNoStore(SPDocument spdoc) {
        spdoc.setNostore(true);
    }

    public static int isAccessible(RequestContextImpl context, String pagename) throws Exception {
        ContextImpl pcontext = context.getParentContext();
        if (pcontext != null) {
            RequestContextImpl oldContext = pcontext.getRequestContextForCurrentThread();
            try {
                pcontext.setRequestContextForCurrentThread(context);
                if (pcontext.getContextConfig().getPageRequestConfig(pagename) != null) {
                    AccessibilityChecker check = (AccessibilityChecker) pcontext;
                    boolean retval;
                    if (pcontext.getContextConfig().isSynchronized()) {
                        synchronized(pcontext) {
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
            } finally {
                pcontext.setRequestContextForCurrentThread(oldContext);
            }

        } else {
            if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                if (check.isPageAccessible(pagename)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        }
    }

    public static int isVisited(RequestContextImpl context, String pagename) throws Exception {
        ContextImpl pcontext = context.getParentContext();
        if (pcontext != null) {
            RequestContextImpl oldContext = pcontext.getRequestContextForCurrentThread();
            try {
                pcontext.setRequestContextForCurrentThread(context);
                if (pcontext.getContextConfig().getPageRequestConfig(pagename) != null) {
                    AccessibilityChecker check = (AccessibilityChecker) pcontext;
                    boolean retval;
                    if (pcontext.getContextConfig().isSynchronized()) {
                        synchronized(pcontext) {
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
            } finally {
                pcontext.setRequestContextForCurrentThread(oldContext);
            }

        } else {
            if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                if (check.isPageAlreadyVisited(pagename)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        }
    }

}
