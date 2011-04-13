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


public class RenderExtension {
    
    private final static Logger LOG = Logger.getLogger(RenderExtension.class);
    
    public static void render(TargetGenerator targetGenerator, String href, 
            String part, String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext) throws Exception {

        String renderKey = TargetGenerator.createRenderKey(href, part, module, search);
            
        long t1 = System.currentTimeMillis();
            
        Templates style = renderContext.getTemplates(renderKey);
        if(style == null) {
            Target target = targetGenerator.getTarget(renderKey);
            style = (Templates)target.getValue();
            renderContext.setTemplates(renderKey, style);
        }
            
        long t2 = System.currentTimeMillis();
            
        StringWriter writer = new StringWriter();
        Result res = new StreamResult(writer);
        Xslt.transform(node.getOwnerDocument(), style, renderContext.getParameters(), res, "utf-8");
            
        long t3 = System.currentTimeMillis();
            
        renderContext.profile(t2 - t1, t3 - t2);
        if(LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(renderKey).append("|").append(t2 - t1)
                .append("|").append(t3 - t2);
            LOG.debug(sb.toString());
        }
            
        if(writer.getBuffer().length() > 0) {
            LOG.warn("Unexpected runtime include transformation output: " + writer.getBuffer().toString());
        }

    }
	
}
