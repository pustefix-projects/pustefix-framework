package de.schlund.pfixxml;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Validation;

import org.w3c.dom.Node;

import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;

public class RenderExtensionSaxon2 {

    public static void render(XPathContext saxonContext, TargetGenerator targetGenerator, String href, 
            String part, String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext) throws Exception {

        try {
           
            RenderContextSaxon2 saxonRenderContext = (RenderContextSaxon2)renderContext;
            if(saxonRenderContext.getResult() == null) {
                saxonRenderContext.setResult(saxonContext.getController().getPrincipalResult());
                saxonRenderContext.setProperties(saxonContext.getController().getOutputProperties());
            }
           
            RenderExtension.render(targetGenerator, href, part, module, search,
                    node, requestContext, saxonRenderContext);
           
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
        
    public static void renderStart(XPathContext saxonContext, RenderContextSaxon2 renderContext) throws Exception {     
        try {
            if(renderContext.getResult() != null) {
                saxonContext.changeOutputDestination(renderContext.getProperties(), renderContext.getResult(), true, Configuration.XSLT, Validation.PRESERVE, null);
            }
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
}
