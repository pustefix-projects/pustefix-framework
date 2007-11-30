package de.schlund.pfixcore.editor2.frontend.handlers;

import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * Describe class LogoutState here.
 *
 *
 * Created: Tue Nov 15 11:27:26 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class LogoutState extends StateImpl {
    
    public ResultDocument getDocument(Context context, PfixServletRequest req) throws Exception {;
        
        if (!isDirectTrigger(context, req) || isSubmitAuthTrigger(context, req)) {
            // we are not direct triggered or we got authdata, don't do a logout,
        } else {
            EditorResourceLocator.getSessionResource(context).logout();
            EditorResourceLocator.getProjectsResource(context).reset();
        }

        return new ResultDocument();                
    }
}
