package de.schlund.pfixxml;

import java.util.Map;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.util.XsltVersion;

public abstract class RenderContext {
    
    private final static String SAXON1_IMPL = "de.schlund.pfixxml.RenderContextSaxon1";
    private final static String SAXON2_IMPL = "de.schlund.pfixxml.RenderContextSaxon2";
    
    private RenderContext parent;
    private Map<String, Object> parameters;
    
    public RenderContext() {
    }
    
    public void setParent(RenderContext parent) {
        this.parent = parent;
    }
    
    public RenderContext getParent() {
        return parent;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
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
