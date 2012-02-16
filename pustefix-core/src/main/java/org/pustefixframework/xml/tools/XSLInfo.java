package org.pustefixframework.xml.tools;

import java.util.ArrayList;
import java.util.List;

public class XSLInfo {

    private List<String> includes = new ArrayList<String>();
    private List<String> imports = new ArrayList<String>();
    private List<XSLTemplateInfo> templates = new ArrayList<XSLTemplateInfo>();
    private long lastMod;
    
    public List<XSLTemplateInfo> getTemplates() {
        return templates;
    }
    
    public void addTemplate(XSLTemplateInfo template) {
        templates.add(template);
    }
    
    public long getLastModified() {
        return lastMod;
    }
    
    public void setLastModified(long lastMod) {
        this.lastMod = lastMod;
    }
    
    public List<String> getIncludes() {
        return includes;
    }
    
    public void addInclude(String href) {
        includes.add(href);
    }
    
    public List<String> getImports() {
        return imports;
    }
    
    public void addImport(String href) {
        imports.add(href);
    }

}
