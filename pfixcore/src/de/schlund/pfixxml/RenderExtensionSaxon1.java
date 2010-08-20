package de.schlund.pfixxml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.icl.saxon.Context;

import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltVersion;

public class RenderExtensionSaxon1 extends RenderExtension {

    static ThreadLocal<Map<String, Templates>> templateCache = new ThreadLocal<Map<String, Templates>>();
    
    public static Node render(Context saxonContext, String targetGenerator, String href, String part, 
            String module, String search, Node node, RequestContextImpl requestContext, 
            RenderContext renderContext, boolean output) throws Exception {

        try {
        long t1 = System.currentTimeMillis();
        
        FileResource tgenPath = ResourceUtil.getFileResource(targetGenerator);
        TargetGenerator tgen = TargetGeneratorFactory.getInstance().createGenerator(tgenPath);
        
        Map<String, Object> subParams = new HashMap<String, Object>();
        Map<String, Object> parentParams = renderContext.getParameters();
        for(String name: parentParams.keySet()) subParams.put(name, parentParams.get(name));
        RenderContextSaxon1 subRenderContext = (RenderContextSaxon1)RenderContext.create(tgen.getXsltVersion());
        subRenderContext.setParent(renderContext);
        
        String componentKey = TargetGenerator.createComponentKey(href, part, module, search);
        
        String page = "__COMPONENT__";
        subParams.put("page", page);
        subParams.put("__rendercontext__", subRenderContext);
        subRenderContext.setParameters(Collections.unmodifiableMap(subParams));
        subRenderContext.setOutputter(saxonContext.getOutputter());
        
        long t2 = System.currentTimeMillis();
        
        Map<String,Templates> templateCacheMap = templateCache.get();
        if(templateCacheMap == null) {
            templateCacheMap = new HashMap<String,Templates>();
            templateCache.set(templateCacheMap);
        }
        Templates style = templateCacheMap.get(componentKey);
        if(style == null) {
            System.out.println("GGGGGGGGGGGGGGGGGG "+componentKey);
            Target target = tgen.getTarget(componentKey);
            style = (Templates)target.getValue();
            templateCacheMap.put(componentKey, style);
        }
        long t3 = System.currentTimeMillis();
        
        Result res;
        if(!output) res = new DOMResult();
        else res = new StreamResult(System.err);
        
        Xslt.transform(node.getOwnerDocument(), style, subParams, res, "utf-8");
        
        long t4 = System.currentTimeMillis();
        
        Document resultDoc = null;
        if(!output) {
            XsltVersion xsltVersion = Xml.getXsltVersion(node);
            resultDoc = Xml.parse(xsltVersion, (Document)((DOMResult)res).getNode());
        }
            
        long t5 = System.currentTimeMillis();
        
        System.out.println("*** RENDER " + page + " *** " + output);
        System.out.println("TOTAL: " + (t5-t1));
        System.out.println("TEMPLATE: " + (t3-t2));
        System.out.println("TRANSFORM: " + (t4-t3));
        System.out.println("TINYTREE: " + (t5-t4));
        
        return resultDoc;
        
        } catch(Exception x) {
            x.printStackTrace();
            throw x;
        }
    }
        
    public static void renderStart(Context saxonContext, RenderContextSaxon1 renderContext) throws TransformerException {
        if(renderContext.getParent() != null) {
            saxonContext.getController().resetOutputDestination(renderContext.getOutputter());
        }
    }
    
}
