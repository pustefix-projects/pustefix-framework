package de.schlund.pfixxml;

import org.w3c.dom.Node;

import com.icl.saxon.Context;

import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon1;

public class RenderExtensionSaxon1 {
    
    public static void render(Context saxonContext, TargetGenerator targetGenerator, String href, 
            String part, String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext) throws Exception {

        try {
           
            RenderContextSaxon1 saxonRenderContext = (RenderContextSaxon1)renderContext;
            if(saxonRenderContext.getOutputter() == null) {
                saxonRenderContext.setOutputter(saxonContext.getOutputter());
            }
            
            RenderExtension.render(targetGenerator, href, part, module, search, 
                    node, requestContext, saxonRenderContext);
                
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
        
    public static void renderStart(Context saxonContext, RenderContextSaxon1 renderContext) throws Exception {     
        try {
            if(renderContext.getOutputter() != null) {
                saxonContext.getController().resetOutputDestination(renderContext.getOutputter());
            }
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static final String getSystemId(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return RenderExtension.getSystemId(xsltContext);
    }
    
}
