package org.pustefixframework.pfxinternals;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

public class JVMCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean(); 
        MemoryUsage mem = mbean.getHeapMemoryUsage();
        Element root = parent.getOwnerDocument().createElement("jvm");
        parent.appendChild(root);
        root.setAttribute("version", System.getProperty("java.version"));
        root.setAttribute("home", System.getProperty("java.home"));
        
        RuntimeMXBean runtimeMBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMBean.getInputArguments();
        if(arguments != null) {
            Element argsElem = parent.getOwnerDocument().createElement("arguments");
            root.appendChild(argsElem);
            for(String argument: arguments) {
                Element argElem = parent.getOwnerDocument().createElement("argument");
                argsElem.appendChild(argElem);
                argElem.setTextContent(argument);
            }
        }
        
        Element elem = parent.getOwnerDocument().createElement("memory");
        root.appendChild(elem);
        elem.setAttribute("type", "heap");
        elem.setAttribute("used", String.valueOf(mem.getUsed()));
        elem.setAttribute("committed", String.valueOf(mem.getCommitted()));
        elem.setAttribute("max", String.valueOf(mem.getMax()));
        
        List<MemoryPoolMXBean> mxbeans = ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean mxbean:mxbeans) {
            if(mxbean.getName().equals("PS Perm Gen")) {
                elem = parent.getOwnerDocument().createElement("memory");
                mem = mxbean.getUsage();
                root.appendChild(elem);
                elem.setAttribute("type", "permgen");
                elem.setAttribute("used", String.valueOf(mem.getUsed()));
                elem.setAttribute("committed", String.valueOf(mem.getCommitted()));
                elem.setAttribute("max", String.valueOf(mem.getMax()));
            }
        }
        
        List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
        for(GarbageCollectorMXBean gcbean: gcbeans) {
            elem = parent.getOwnerDocument().createElement("gc");
            root.appendChild(elem);
            elem.setAttribute("name", gcbean.getName());
            elem.setAttribute("count", String.valueOf(gcbean.getCollectionCount()));
            elem.setAttribute("time", String.valueOf(gcbean.getCollectionTime()));
        }
    }
    
}
