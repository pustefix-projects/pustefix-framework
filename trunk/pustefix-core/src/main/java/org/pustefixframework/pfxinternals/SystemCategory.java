package org.pustefixframework.pfxinternals;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class SystemCategory implements Category {

    private Logger LOG = Logger.getLogger(SystemCategory.class);
    
    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
       
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        long freeMem = 0;
        long totalMem = 0;
        long freeSwap = 0;
        long totalSwap = 0;
        try {
            ObjectName objName = new ObjectName("java.lang:type=OperatingSystem");
            freeMem = (Long)mbeanServer.getAttribute(objName, "FreePhysicalMemorySize");
            totalMem = (Long)mbeanServer.getAttribute(objName, "TotalPhysicalMemorySize");
            freeSwap = (Long)mbeanServer.getAttribute(objName, "FreeSwapSpaceSize");
            totalSwap = (Long)mbeanServer.getAttribute(objName, "TotalSwapSpaceSize");
        } catch(Exception x) {
            LOG.warn("No system memory information available", x);
        }
        Element root = parent.getOwnerDocument().createElement("system");
        parent.appendChild(root);
        Element elem = parent.getOwnerDocument().createElement("memory");
        root.appendChild(elem);
        elem.setAttribute("free", String.valueOf(freeMem));
        elem.setAttribute("total", String.valueOf(totalMem));
        elem = parent.getOwnerDocument().createElement("swap");
        root.appendChild(elem);
        elem.setAttribute("free", String.valueOf(freeSwap));
        elem.setAttribute("total", String.valueOf(totalSwap));
        long openDesc = 0;
        long maxDesc = 0;
        try {
            ObjectName objName = new ObjectName("java.lang:type=OperatingSystem");
            openDesc = (Long)mbeanServer.getAttribute(objName, "OpenFileDescriptorCount");
            maxDesc = (Long)mbeanServer.getAttribute(objName, "MaxFileDescriptorCount");
        } catch(Exception x) {
            LOG.warn("No file descriptor information available", x);
        }
        elem = parent.getOwnerDocument().createElement("filedescriptors");
        root.appendChild(elem);
        elem.setAttribute("open", String.valueOf(openDesc));
        elem.setAttribute("max", String.valueOf(maxDesc));
        int processors = 0;
        double load = 0;
        try {
            ObjectName objName = new ObjectName("java.lang:type=OperatingSystem");
            processors = (Integer)mbeanServer.getAttribute(objName, "AvailableProcessors");
            load = (Double)mbeanServer.getAttribute(objName, "SystemLoadAverage");
        } catch(Exception x) {
            LOG.warn("No CPU information available", x);
        }
        root.setAttribute("processors", String.valueOf(processors));
        root.setAttribute("load", String.valueOf(load));
        
    }
    
}
