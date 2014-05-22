package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.transform.Templates;

import org.w3c.dom.Node;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.util.SimpleCacheLRU;
import de.schlund.pfixxml.util.XsltVersion;

public abstract class RenderContext {
    
    private final static String SAXON1_IMPL = "de.schlund.pfixxml.RenderContextSaxon1";
    private final static String SAXON2_IMPL = "de.schlund.pfixxml.RenderContextSaxon2";
    
    private Map<String, Object> parameters;
    
    //Second-level template cache only available within the current transformation
    private SimpleCacheLRU<String, Templates> templatesCache = new SimpleCacheLRU<String, Templates>(10);
    private Map<String, Boolean> templateToContextual = new HashMap<String, Boolean>();
    
    private long templateCreationTime;
    private long transformationTime;
    
    private Stack<Node> contextNodes = new Stack<Node>();
    
    public RenderContext() {
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public Templates getTemplates(String templatesId) {
        return templatesCache.get(templatesId);
    }
    
    public void setTemplates(String templatesId, Templates templates, boolean contextual) {
        templatesCache.put(templatesId, templates);
        templateToContextual.put(templatesId, contextual);
    }
    
    public boolean isContextual(String templatesId) {
    	return templateToContextual.get(templatesId);
    }
    
    public void profile(long templateCreationTime, long transformationTime) {
        this.templateCreationTime += templateCreationTime;
        this.transformationTime += transformationTime;
    }
    
    public long getTemplateCreationTime() {
        return templateCreationTime;
    }
    
    public long getTransformationTime() {
        return transformationTime;
    }
    
    public void pushContext(Node node) {
    	contextNodes.push(node);
    }
    
    public Node popContext() {
    	return contextNodes.pop();
    }
    
    public Node getContextNode() {
    	if(contextNodes.empty()) return null;
    	return contextNodes.peek();
    }
    
    public static RenderContext create(XsltVersion xsltVersion) {
        String className;
        if(xsltVersion == XsltVersion.XSLT1) {
            className = SAXON1_IMPL;
        } else if(xsltVersion == XsltVersion.XSLT2){
            className = SAXON2_IMPL;
        } else throw new IllegalArgumentException("No RenderContext implementation available for " + xsltVersion);
        try {
            Class<?> clazz = Class.forName(className);
            return (RenderContext)clazz.newInstance();
        } catch(Exception x) {
            throw new PustefixRuntimeException("Can't create RenderContext", x);
        }
    }
    
}
