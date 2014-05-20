package org.pustefixframework.pfxinternals;

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.pustefixframework.live.LiveResolver;
import org.w3c.dom.Element;

import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.FilterHelper;

public class ModulesCategory implements Category {

    private Logger LOG = Logger.getLogger(ModulesCategory.class);
    
    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        Element root = parent.getOwnerDocument().createElement("modules");
        parent.appendChild(root);
        Set<String> modules = ModuleInfo.getInstance().getModules();
        SortedSet<String> sortedModules = new TreeSet<String>();
        sortedModules.addAll(modules);
        
        ObjectName name;
        try {
            name = new ObjectName("Pustefix:type=LiveAgent");
        } catch(MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        boolean checkLiveClasses = mbeanServer.isRegistered(name);
        String[] signature = new String[] {"java.lang.String"};
        
        for(String module: sortedModules) {
            Element elem = parent.getOwnerDocument().createElement("module");
            elem.setAttribute("name", module);
            ModuleDescriptor desc = ModuleInfo.getInstance().getModuleDescriptor(module);
            try {
                URL url = LiveResolver.getInstance().resolveLiveModuleRoot(desc.getURL(), "/");
                if(url != null) elem.setAttribute("url", url.toString());
            } catch(Exception x) {
                LOG.warn("Error while live-resolving module", x);
            }
            root.appendChild(elem);
            try {
                String jarPath = desc.getURL().getPath();
                int ind = jarPath.lastIndexOf('!');
                if(ind > -1) jarPath = jarPath.substring(0, ind);
                if(checkLiveClasses) {
                    String result = (String)mbeanServer.invoke(name, "getLiveLocation", new Object[] {jarPath}, signature);
                    if(result != null) elem.setAttribute("classurl", result);
                }
            } catch(Exception x) {
                LOG.warn("Error while getting live location", x);
            }
        }
        
        Element defSearchElem = parent.getOwnerDocument().createElement("defaultsearch");
        root.appendChild(defSearchElem);
        List<String> defModules = ModuleInfo.getInstance().getDefaultSearchModules(null);
        for(String moduleName: defModules) {
            ModuleDescriptor desc = ModuleInfo.getInstance().getModuleDescriptor(moduleName);
            Dictionary<String,String> filterAttrs= desc.getDefaultSearchFilterAttributes();
            Element elem = parent.getOwnerDocument().createElement("module");
            defSearchElem.appendChild(elem);
            elem.setAttribute("name", desc.getName());
            String tenant = filterAttrs.get("tenant");
            String language = filterAttrs.get("lang");
            String filter = FilterHelper.getFilter(tenant, language);
            if(filter != null) {
                elem.setAttribute("filter", filter);
            }
            elem.setAttribute("priority", String.valueOf(desc.getDefaultSearchPriority()));
        }
    }
    
}
