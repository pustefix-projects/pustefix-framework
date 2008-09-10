package org.pustefixframework.util.firedebug;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private static final int maxIncludeNotFoundMatches = 10;
    
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
            javax.servlet.FilterChain chain, Context context)
            throws IOException, ServletException {        
        FireDebugServletResponseWrapper responseWrapper = new FireDebugServletResponseWrapper(response);
        
        ByteArrayOutputStream sysErrBuffer = new ByteArrayOutputStream();
        PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErrBuffer));
        chain.doFilter(request, responseWrapper);
        
        addFireDebugHeaders(response, getFireDebug(context), sysErrBuffer);
        response.getOutputStream().write(responseWrapper.getData());
        System.setErr(sysErr);
        System.err.write(sysErrBuffer.toByteArray());
    }

    private void addFireDebugHeaders(HttpServletResponse response, FireDebug fireDebug, OutputStream out) {
        appendSysoutMessagesToFireDebug(fireDebug, out);
        
        for (Entry<String, String> header : fireDebug.getHeaders().entrySet()) {
            response.setHeader(header.getKey(), header.getValue());
        }
        
        fireDebug.reset();
    }

    private void appendSysoutMessagesToFireDebug(FireDebug fireDebug, OutputStream out) {
        String regex = "[***] Include not found:.*\n.*Document = (.*).*\n.*Part = (.*) [***]";
        String input = out.toString();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        int matches = 0;
        while(matcher.find()) {
            if (matches < maxIncludeNotFoundMatches) {
                fireDebug.warn("Include not found: \n"
                    + "\tDocument = " + matcher.group(1) + "\n"
                    + "\tPart = " + matcher.group(2)
                );
            }
            matches++;
        }
        
        if (matches >= maxIncludeNotFoundMatches) {
            fireDebug.log("Es werden nur " + maxIncludeNotFoundMatches + " fehlende Includes von insgesamt " + matches + " angezeigt.");
        }
    }

    private FireDebug getFireDebug(Context context) {
        FireDebug fireDebug;
        
        if (context == null) {
            fireDebug = new FireDebugImpl();
            ((FireDebugImpl)fireDebug).setJSONSerializer(new PustefixJSONSerializer());
        } else {
            fireDebug = (FireDebug) context.getContextResourceManager().getResource(FireDebug.class);
        }
        
        return fireDebug;
    }

}