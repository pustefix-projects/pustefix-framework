package org.pustefixframework.util.firedebug;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.AbstractContextServletFilter;

/**
 * FireDebugServletFilter
 * 
 * FireDebugServletFilter calls the ContextResource FireDebug and adds the
 * response headers needed for FirePHP to the current response.
 * 
 * @author Holger Rüprich
 */

public class FireDebugServletFilter extends AbstractContextServletFilter {
    
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
            javax.servlet.FilterChain chain, Context context)
            throws IOException, ServletException {        
        HttpServletResponse fireDebugResponse = new FireDebugServletResponseWrapper(response);
        chain.doFilter(request, fireDebugResponse); 
        
        if (context != null) {
            FireDebug fireDebug = (FireDebug) context.getContextResourceManager().getResource(FireDebug.class);
            
            for (Entry<String, String> header : fireDebug.getHeaders().entrySet()) {
                fireDebugResponse.setHeader(header.getKey(), header.getValue());
            }
            

            fireDebug.reset();
        }  
        
        response = fireDebugResponse;
    }

}