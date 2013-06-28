package de.schlund.pfixxml;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltContext;


public class RenderExtension {
    
    private final static Logger LOG = Logger.getLogger(RenderExtension.class);
    
    public static boolean render(TargetGenerator targetGenerator, String href, 
            String part, String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext) throws Exception {

        if(href.startsWith("/")) href = href.substring(1);
        
        String cacheKey = href + "#" + part + "#" + module + "#" + search;
        
        long t1 = System.currentTimeMillis();
            
        boolean isContextual = false;
        Templates style = renderContext.getTemplates(cacheKey);
        if(style == null) {
            Target target = targetGenerator.getRenderTarget(href, part, module, search, requestContext.getVariant());
            if(target == null) {
            	return false;
            } else {
            	style = (Templates)target.getValue();
            	Boolean value = (Boolean)target.getParams().get("render_contextual");
             	if(value != null && value) {
             		isContextual = true;
             	}
            	renderContext.setTemplates(cacheKey, style, isContextual);
            }
        } else {
        	isContextual = renderContext.isContextual(cacheKey);
        }
        
        long t2 = System.currentTimeMillis();
        
        if(isContextual) {
    		renderContext.pushContext(node);
    	} else {
    		renderContext.pushContext(node.getOwnerDocument());
    	}
        StringWriter writer;
        try {
        	
        	writer = new StringWriter();
        	Result res = new StreamResult(writer);
        	Xslt.transform(node.getOwnerDocument(), style, renderContext.getParameters(), res, "utf-8");
            
        } finally {
        	if(isContextual) {
        		renderContext.popContext();
        	}
        }
        	
        long t3 = System.currentTimeMillis();
            
        renderContext.profile(t2 - t1, t3 - t2);
        if(LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(cacheKey).append("|").append(t2 - t1)
                .append("|").append(t3 - t2);
            LOG.debug(sb.toString());
        }
            
        if(writer.getBuffer().length() > 0) {
            LOG.warn("Unexpected runtime include transformation output: " + writer.getBuffer().toString());
        }
        
        return true;

    }
	
    public static final String getSystemId(XsltContext context) {
        return context.getSystemId();
    }
    
}
