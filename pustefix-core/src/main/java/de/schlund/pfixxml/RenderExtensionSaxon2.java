package de.schlund.pfixxml;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ParseOptions;

import org.w3c.dom.Node;

import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon2;

public class RenderExtensionSaxon2 {

    public static boolean render(XPathContext saxonContext, TargetGenerator targetGenerator, String href, 
            String part, String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext) throws Exception {

        try {
           
            RenderContextSaxon2 saxonRenderContext = (RenderContextSaxon2)renderContext;
            if(saxonRenderContext.getResult() == null) {
                saxonRenderContext.setResult(saxonContext.getController().getPrincipalResult());
                saxonRenderContext.setProperties(saxonContext.getController().getOutputProperties());
            }
           
            return RenderExtension.render(targetGenerator, href, part, module, search,
                    node, requestContext, saxonRenderContext);
           
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
        
    public static void renderStart(XPathContext saxonContext, RenderContextSaxon2 renderContext) throws Exception {     
        try {
            if(renderContext.getResult() != null) {
                saxonContext.changeOutputDestination(saxonContext.getReceiver(), new ParseOptions());
            }
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static String getSystemId(XPathContext context) {
        XsltContext xsltContext=new XsltContextSaxon2(context);
        return RenderExtension.getSystemId(xsltContext);
    }
    
}
