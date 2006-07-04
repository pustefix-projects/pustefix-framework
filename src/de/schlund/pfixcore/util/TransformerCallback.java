package de.schlund.pfixcore.util;

import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.Context;
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

    public static int isAccessible(Context context, String pagename) throws Exception {
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

    public static int isVisited(Context context, String pagename) throws Exception {
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
