package org.pustefixframework.util.firedebug;

import java.util.Map.Entry;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;

/**
 * FireDebugEnabledState
 * 
 * FireDebugEnabledState calls the ContextResource FireDebug and adds the
 * response headers needed for FirePHP to the current response.
 * 
 * @author Holger Rüprich
 */

public class FireDebugEnabledState extends DefaultIWrapperState {

    public ResultDocument getDocument(Context context, PfixServletRequest request) throws Exception {
        ResultDocument result = super.getDocument(context, request);
        SPDocument spDocument = result.getSPDocument();
        FireDebug fireDebug = context.getContextResourceManager().getResource(org.pustefixframework.util.firedebug.FireDebug.class);
      
        for (Entry<String, String> header : fireDebug.getHeaders().entrySet()) {
            spDocument.addResponseHeader(header.getKey(), header.getValue());
        }
        
        fireDebug.reset();
        return result;
    }

}
