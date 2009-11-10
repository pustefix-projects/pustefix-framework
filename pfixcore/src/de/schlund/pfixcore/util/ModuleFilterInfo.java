package de.schlund.pfixcore.util;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.resources.ModuleFilter;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

public class ModuleFilterInfo {
    
    private static ModuleFilterInfo instance = new ModuleFilterInfo();
    
    private Map<String, ModuleFilter> applicationToFilter = new HashMap<String, ModuleFilter>();
    
    private ModuleFilterInfo() {
        readModuleFilters();
    }
    
    public static ModuleFilterInfo getInstance() {
        return instance;
    }
    
    private void readModuleFilters() {
        try {
            Document projectsXml = Xml.parse(XsltVersion.XSLT1, ResourceUtil.getFileResourceFromDocroot("servletconf/projects.xml"));
            NodeList projectNodes = projectsXml.getElementsByTagName("project");
            for(int i=0; i<projectNodes.getLength(); i++) {
                Element projectElem = (Element)projectNodes.item(i);
                String projectName = projectElem.getAttribute("name").trim();
                NodeList dynIncNodes = projectElem.getElementsByTagName("dynamic-includes");
                if(dynIncNodes.getLength() > 0) {
                    Element dynIncElem = (Element)dynIncNodes.item(0);
                    String filter = dynIncElem.getAttribute("filter").trim();
                    if(filter.length() > 0) {
                        addModuleFilter(projectName, filter);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Reading module filter infos from projects.xml failed", e);
        }
    }
    
    public void addModuleFilter(String application, String filterExpression) {
        applicationToFilter.put(application, new ModuleFilter(filterExpression));
    }
    
    public ModuleFilter getModuleFilter(String application) {
        return applicationToFilter.get(application);
    }
    
}
