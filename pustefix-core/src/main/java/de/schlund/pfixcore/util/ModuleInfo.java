package de.schlund.pfixcore.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.exception.PustefixRuntimeException;

public class ModuleInfo {
    
	private static Logger LOG = Logger.getLogger(ModuleInfo.class);
	
    private static String MODULE_DESCRIPTOR_LOCATION = "META-INF/pustefix-module.xml";
    
    private static ModuleInfo instance = new ModuleInfo();
    
    private SortedMap<String,ModuleDescriptor> moduleDescMap = new TreeMap<String,ModuleDescriptor>();
    
    public static ModuleInfo getInstance() {
        return instance;
    }
    
    public ModuleInfo() {
        try {
            read();
            LOG.info(toString());
        } catch(Exception x) {
            throw new PustefixRuntimeException(x);
        }
    }
    
    public ModuleInfo(List<URL> urls){
        try {
            read(urls);
            LOG.info(toString());
        } catch(Exception x) {
            throw new PustefixRuntimeException(x);
        }
    }
    
    public void read() throws Exception {
        Enumeration<URL> urls = getClass().getClassLoader().getResources(MODULE_DESCRIPTOR_LOCATION);
        while(urls.hasMoreElements()) {
            URL url = urls.nextElement();
            read(url);
        }
    }
    
    public void read(List<URL> urls) throws Exception {
        for(URL url:urls) {
           read(url);
        }
    }
    
    private void read(URL url) throws Exception {
        ModuleDescriptor moduleDesc = ModuleDescriptor.read(url);
        if(moduleDescMap.containsKey(moduleDesc.getName())) {
            throw new Exception("Found multiple modules named '"+moduleDesc.getName()+"' ("+
                    moduleDesc.getURL() + " - " + moduleDescMap.get(moduleDesc.getName()).getURL() + ")");
        }
        moduleDescMap.put(moduleDesc.getName(), moduleDesc);
    }
    
    public ModuleDescriptor getModuleDescriptor(String moduleName) {
        return moduleDescMap.get(moduleName);
    }
    
    public Set<String> getModules() {
        return moduleDescMap.keySet();
    }
    
    public List<String> getOverridingModules(String moduleName, String resourcePath) {
        List<String> modules = new ArrayList<String>();
        
        getOverridingModules(moduleName, resourcePath, modules);
        return modules;
    }
    
    private void getOverridingModules(String moduleName, String resourcePath, List<String> modules) {
        for(ModuleDescriptor moduleDesc:moduleDescMap.values()) {
            if(moduleDesc.overridesResource(moduleName, resourcePath)) {
                if(!modules.contains(moduleDesc.getName())) {
                    modules.add(0, moduleDesc.getName());
                    getOverridingModules(moduleDesc.getName(), resourcePath, modules);
                }
            }
        }
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Module information - ");
    	int no = moduleDescMap.values().size();
    	sb.append("Detected " + no + " module" + (no==1?"":"s") + " [");
    	for(ModuleDescriptor moduleDesc:moduleDescMap.values()) {
    		sb.append(moduleDesc.getName() + " ");
    	}
    	if(no>0) sb.deleteCharAt(sb.length()-1);
    	sb.append("]");
    	return sb.toString();
    }

}
