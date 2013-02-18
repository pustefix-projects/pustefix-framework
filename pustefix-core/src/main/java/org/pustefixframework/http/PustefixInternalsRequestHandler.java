/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.pustefixframework.admin.mbeans.Admin;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.live.LiveResolver;
import org.pustefixframework.pfxinternals.search.FullTextSearch;
import org.pustefixframework.util.FrameworkInfo;
import org.pustefixframework.xml.tools.XSLInfo;
import org.pustefixframework.xml.tools.XSLInfoFactory;
import org.pustefixframework.xml.tools.XSLInfoParsingException;
import org.pustefixframework.xml.tools.XSLTemplateInfo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.FilterHelper;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.ModuleResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFile;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.AuxDependencyInclude;
import de.schlund.pfixxml.targets.AuxDependencyTarget;
import de.schlund.pfixxml.targets.LeafTarget;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetImpl;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.targets.cachestat.CacheStatistic;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Outputs some internal extra information for developers.
 * Can be used to trigger a webapp reload.
 * 
 * @author mleidig@schlund.de
 *
 */
public class PustefixInternalsRequestHandler implements UriProvidingHttpRequestHandler, ServletContextAware, InitializingBean, DisposableBean {
    
    private final static Logger LOG = Logger.getLogger(PustefixInternalsRequestHandler.class);
    
    private final static String STYLESHEET = "module://pustefix-core/xsl/pfxinternals.xsl";
      
    private String handlerURI ="/pfxinternals";
    private ServletContext servletContext;
    private SessionAdmin sessionAdmin;
    private CacheStatistic cacheStatistic;
    private TargetGenerator targetGenerator;
    private XSLInfoFactory xslInfoFactory = new XSLInfoFactory();
    private long startTime;
    private long reloadTimeout = 1000 * 5;
    
    private MessageList messageList = new MessageList();
    
    public void setHandlerURI(String handlerURI) {
        this.handlerURI = handlerURI;
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public void setSessionAdmin(SessionAdmin sessionAdmin) {
        this.sessionAdmin = sessionAdmin;
    }
    
    public void setCacheStatistic(CacheStatistic cacheStatistic) {
        this.cacheStatistic = cacheStatistic;
    }
    
    public void setTargetGenerator(TargetGenerator targetGenerator) {
        this.targetGenerator = targetGenerator;
    }
    
    public void afterPropertiesSet() throws Exception {
        deserialize();
        startTime = System.currentTimeMillis();
        messageList.addMessage(Message.Level.INFO, "Webapp started.");
    }
    
    public void destroy() throws Exception {
        messageList.addMessage(Message.Level.INFO, "Webapp stopped.");
        serialize();
    }
    
    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
       if(EnvironmentProperties.getProperties().get("mode").equals("prod")) {
           res.sendError(HttpServletResponse.SC_FORBIDDEN);
           return;
       }
       
       String category = null;
       String path = req.getPathInfo();
       String parentPath = "/pfxinternals/";
       int ind = path.indexOf(parentPath);
       path = path.substring(ind + parentPath.length());
       if(path.length() > 0) category = path;
       
       String action = req.getParameter("action");
       if(action == null && category == null) {
           res.sendRedirect(req.getContextPath() + handlerURI + "/framework");
           return;
       }
       
       try {
           if(action != null) {
               if(action.equals("reload")) {
                   if((System.currentTimeMillis() - startTime) > reloadTimeout) {
                       messageList.addMessage(Message.Level.INFO, "Scheduled webapp reload.");
                       serialize();
                       ObjectName mbeanName = new ObjectName(Admin.JMX_NAME);
                       MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                       if(server != null && server.isRegistered(mbeanName)) {
                           sessionAdmin.invalidateSessions();
                           File workDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
                           if(workDir != null) {
                               try {
                                   int port = (Integer)server.getAttribute(mbeanName, "Port");
                                   Socket sock = new Socket("localhost", port);
                                   OutputStream out = sock.getOutputStream();
                                   PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
                                   writer.println("reload");
                                   writer.println(workDir.getPath());
                                   writer.close();
                               } catch(Exception x) {
                                   messageList.addMessage(Message.Level.WARN, x.getMessage());
                               }
                           } else {
                               messageList.addMessage(Message.Level.WARN, "Missing servlet context attribute 'javax.servlet.context.tempdir'.");
                           }
                       } else {
                           messageList.addMessage(Message.Level.WARN, "Can't do reload because Admin mbean isn't available.");
                       }
                   } else {
                       messageList.addMessage(Message.Level.WARN, "Skipped repeated webapp reload scheduling.");
                   }
                   String page = req.getParameter("page");
                   if(page == null) {
                       res.sendRedirect(req.getContextPath() + handlerURI + "/messages");
                   } else {
                       sendReloadPage(req, res);
                   }
                   return;
               } else if(action.equals("invalidate")) {
                   String session = req.getParameter("session");
                   String page = req.getParameter("page");
                   if(session == null) {
                       sessionAdmin.invalidateSessions();
                       messageList.addMessage(Message.Level.INFO, "Invalidated sessions.");
                   } else {
                       sessionAdmin.invalidateSession(session);
                       messageList.addMessage(Message.Level.INFO, "Invalidated session.");
                   }
                   if(page == null) {
                       res.sendRedirect(req.getContextPath()+ handlerURI + "/messages");
                   } else {
                       String url = req.getRequestURI();
                       url = url.replace("pfxinternals", req.getParameter("page"));
                       res.sendRedirect(url.toString());
                   }
                   return;
               } else if(action.equals("download")) {
                   String resourceParam = req.getParameter("resource");
                   if(resourceParam != null) {
                	   if(resourceParam.startsWith("/")) {
                		   resourceParam = "file:" + resourceParam;
                	   }
                       Resource resource = ResourceUtil.getResource(resourceParam);
                       deliver(resource, res);
                       return;
                   } else {
                       res.sendError(HttpServletResponse.SC_NOT_FOUND, "Missing resource parameter");
                   }
               } else if(action.equals("toolext")) {
            	   boolean toolExtEnabled = targetGenerator.getToolingExtensions();
            	   targetGenerator.setToolingExtensions(!toolExtEnabled);
            	   targetGenerator.forceReinit();
            	   messageList.addMessage(Message.Level.INFO, (toolExtEnabled?"Disabled":"Enabled") + " TargetGenerator tooling extensions.");
            	   String referer = req.getHeader("Referer");
            	   if(referer != null && !referer.contains("pfxinternals")) {
            		   res.sendRedirect(referer);
            		   return;
            	   } else {
            		   res.sendRedirect(req.getContextPath()+ handlerURI + "/messages");
            		   return;
            	   }
               } else if(action.equals("retarget")) {
            	   targetGenerator.forceReinit();
            	   messageList.addMessage(Message.Level.INFO, "Reloaded TargetGenerator with cleared cache.");
            	   String referer = req.getHeader("Referer");
            	   if(referer != null && !referer.contains("pfxinternals")) {
            		   res.sendRedirect(referer);
            		   return;
            	   } else {
            		   res.sendRedirect(req.getContextPath()+ handlerURI + "/messages");
            		   return;
            	   }
               } else if(action.equals("senderror")) {
            	   String sc = req.getParameter("sc");
            	   String msg = req.getParameter("msg");
            	   if(msg == null) {
            		   res.sendError(Integer.parseInt(sc));
            	   } else {
            		   res.sendError(Integer.parseInt(sc), msg);
            	   }
            	   return;
               }
           }
           
           Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
           Element root = doc.createElement("pfxinternals");
           doc.appendChild(root);
           if(category != null) {
               if(category.equals("framework")) {
                   addFrameworkInfo(root);
               } else if(category.equals("environment")) {
                   addEnvironmentInfo(root);
               } else if(category.equals("jvm")) {
                   addJVMInfo(root);
               } else if(category.equals("system")) {
                   addSystemInfo(root);
               } else if(category.equals("targets")) {
                   addTargets(root, req);
               } else if(category.equals("search")) {
            	   Element searchElem = addSearch(root);
            	   if("search".equals(action)) {
            		   doSearch(searchElem, req);
            	   }
               } else if(category.equals("modules")) {
                   addModuleInfo(root);
               } else if(category.equals("cache")) {
                   addCacheStatistics(root);
               } else if(category.equals("messages")) {
                   messageList.toXML(root);
               } else {
                   
               }
           }
           doc = Xml.parse(XsltVersion.XSLT1, doc);
           Templates stvalue = Xslt.loadTemplates(XsltVersion.XSLT1, (ModuleResource)ResourceUtil.getResource(STYLESHEET));
           res.setContentType("text/html");
           Map<String, Object> params = new HashMap<String, Object>();
           params.put("__contextpath", req.getContextPath());
           if(category != null) params.put("category", category);
           Xslt.transform(doc, stvalue, params, new StreamResult(res.getOutputStream()));
        
       } catch(Exception x) {
           LOG.error(x);
           throw new ServletException("Error while creating info page", x);
       }
    }
    
    private void sendReloadPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter writer = res.getWriter();
        writer.println("<html>");
        writer.println("  <head>");
        writer.println("    <title>Pustefix internals - Reloading webapp</title>");
        String url = req.getRequestURI();
        url = url.replace("pfxinternals", req.getParameter("page"));
        writer.println("    <meta http-equiv=\"refresh\" content=\"1; URL=" + url + "\"></meta>");
        writer.println("    <style type=\"text/css\">");
        writer.println("      body {background: white; color: black;}");
        writer.println("      table {width: 100%; height: 100%;}");
        writer.println("      td {text-align: center; vertical-align: middle; font-size:150%; font-style:italic; font-family: serif;}");
        writer.println("      span {color:white;}");
        writer.println("    </style>");
        writer.println("    <script type=\"text/javascript\">");
        writer.println("      var no = -1;");
        writer.println("      function showProgress() {");
        writer.println("        no++;");
        writer.println("        if(no == 10) {");
        writer.println("          no = 0;");
        writer.println("          for(var i=0; i<10; i++) document.getElementById(i).style.color = \"white\";");
        writer.println("        }");
        writer.println("        document.getElementById(no).style.color = \"black\";");
        writer.println("      }");
        writer.println("      window.setInterval(\"showProgress()\", 500);");
        writer.println("    </script>");
        writer.println("  </head>");
        writer.print("<body><table><tr><td>");
        writer.print("Reloading webapp ");
        for(int i=0; i<10; i++) {
            writer.print("<span id=\"" + i + "\">.</span>");
        }
        writer.println("</td></tr></table></body></html>");
        writer.close();
    }
    
    private void addFrameworkInfo(Element parent) {
        
        Element root = parent.getOwnerDocument().createElement("framework");
        parent.appendChild(root);
        root.setAttribute("version", FrameworkInfo.getVersion());
        root.setAttribute("scmurl", FrameworkInfo.getSCMUrl());
    }
    
    private void addEnvironmentInfo(Element parent) {
        
        Element envElem = parent.getOwnerDocument().createElement("environment");
        parent.appendChild(envElem);
        Element propsElem = parent.getOwnerDocument().createElement("properties");
        envElem.appendChild(propsElem);
        Properties props = EnvironmentProperties.getProperties();
        Element elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "fqdn");
        elem.setTextContent(props.getProperty("fqdn"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "machine");
        elem.setTextContent(props.getProperty("machine"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "mode");
        elem.setTextContent(props.getProperty("mode"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "uid");
        elem.setTextContent(props.getProperty("uid"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "logroot");
        elem.setTextContent(props.getProperty("logroot"));
        propsElem.appendChild(elem);
        
        Element sysPropsElem = parent.getOwnerDocument().createElement("system-properties");
        parent.appendChild(sysPropsElem);
        RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean();
        Map<String, String> sysProps = mbean.getSystemProperties();
        for(String sysPropKey: sysProps.keySet()) {
            String sysPropVal = sysProps.get(sysPropKey);
            Element sysPropElem = parent.getOwnerDocument().createElement("property");
            sysPropElem.setAttribute("name", sysPropKey);
            sysPropElem.setTextContent(sysPropVal);
            sysPropsElem.appendChild(sysPropElem);
        }
        
    }
    
    private void addJVMInfo(Element parent) {
        
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
    
    private void addSystemInfo(Element parent) {
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
    
    private void addModuleInfo(Element parent) {
        
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
    
    private void addCacheStatistics(Element parent) {
        Document doc = cacheStatistic.getAsXML();
        Node imported = parent.getOwnerDocument().importNode(doc.getDocumentElement(), true);
        parent.appendChild(imported);
    }
    
    private void addTargets(Element parent, HttpServletRequest req) {
        Element targetsElem = parent.getOwnerDocument().createElement("targets");
        parent.appendChild(targetsElem);
        addTargetList(targetsElem);
        
        String targetKey = req.getParameter("target");
        if(targetKey == null) {
            targetKey = "metatags.xsl";
        }
        if(!targetGenerator.getAllTargets().containsKey(targetKey)) {
            if(targetGenerator.getAllTargets().size() > 0) {
                targetKey = targetGenerator.getAllTargets().firstKey();
            } else {
                targetKey = null;
            }
        }
        Target target = null;
        if(targetKey != null) {
            target = (TargetImpl)targetGenerator.getTarget(targetKey);
            try {
                target.getValue();
            } catch(TargetGenerationException x) {
                //ignore as we can still provide the static target information
            }
        }
        dumpTarget(target, targetsElem, new HashSet<String>(), true);
    }
    
    private void addTargetList(Element root) {
        Element targetsElem = root.getOwnerDocument().createElement("targetlist");
        root.appendChild(targetsElem);
        Map<String, Target> targets = targetGenerator.getAllTargets();
        Iterator<String> it = targets.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            Target target = targets.get(key);
            Element targetElem = root.getOwnerDocument().createElement("target");
            targetElem.setAttribute("key", target.getTargetKey());
            targetsElem.appendChild(targetElem);
        }
    }
    
    private void dumpTarget(Target target, Element root, Set<String> templates, boolean templateInfo) {
        
        Element targetElem = root.getOwnerDocument().createElement("target");
        root.appendChild(targetElem);
        targetElem.setAttribute("key", target.getTargetKey());
        targetElem.setAttribute("type", target.getType().getTag());
        if(target instanceof LeafTarget) {
            targetElem.setAttribute("resource", target.getTargetKey());
        } else {
            targetElem.setAttribute("resource", targetGenerator.getDisccachedir() + "/" + target.getTargetKey());
        }
        if(target instanceof VirtualTarget) {
            VirtualTarget virtual = (VirtualTarget)target;
            Target xmlTarget = virtual.getXMLSource();
            dumpTarget(xmlTarget, targetElem, templates, false);
            Target xslTarget = virtual.getXSLSource();
            dumpTarget(xslTarget, targetElem, templates, false);
        }
        
        if(target.getTargetKey().endsWith(".xsl") && templateInfo) {
            addTemplateInfo(target, targetElem, templates);
        }
        
        Element depsElem = root.getOwnerDocument().createElement("dependencies");
        targetElem.appendChild(depsElem);
        TreeSet<AuxDependency> deps = targetGenerator.getTargetDependencyRelation().getDependenciesForTarget(target);
        if(deps != null) {
        for(AuxDependency aux: deps) {
            if(aux instanceof AuxDependencyTarget) {
                AuxDependencyTarget auxDepTarget = (AuxDependencyTarget)aux;
                Target auxTarget = auxDepTarget.getTarget();
                dumpTarget(auxTarget, depsElem, templates, templateInfo);
            } else if(aux instanceof AuxDependencyInclude) {
                Element incElem = root.getOwnerDocument().createElement("include");
                depsElem.appendChild(incElem);
                AuxDependencyInclude auxDepInc = (AuxDependencyInclude)aux;
                incElem.setAttribute("path",auxDepInc.getPath().toString());
                incElem.setAttribute("part", auxDepInc.getPart());
                incElem.setAttribute("theme", auxDepInc.getTheme());
            } else if(aux instanceof AuxDependencyImage) {
                Element imgElem = root.getOwnerDocument().createElement("image");
                depsElem.appendChild(imgElem);
                AuxDependencyImage auxDepImg = (AuxDependencyImage)aux;
                imgElem.setAttribute("path",auxDepImg.getPath().toString());
            }  else if(aux instanceof AuxDependencyFile) {
                Element fileElem = root.getOwnerDocument().createElement("file");
                depsElem.appendChild(fileElem);
                AuxDependencyFile auxDepFile = (AuxDependencyFile)aux;
                fileElem.setAttribute("path", auxDepFile.getPath().toString());
            } 
        }
        } 
    }
    
    private void addTemplateInfo(Target target, Element root, Set<String> templates) {
        
        Resource res;
        if(target instanceof LeafTarget) {
            res = ResourceUtil.getResource(target.getTargetKey());
        } else {
            res = ResourceUtil.getFileResource(targetGenerator.getDisccachedir(), target.getTargetKey());
        }
        if(res.exists()) {
            root.setAttribute("url", res.toURI().toASCIIString());
            if(!templates.contains(target.getTargetKey())) {
                try {
                    XSLInfo info = xslInfoFactory.getXSLInfo(res);
                    Element templatesElem = root.getOwnerDocument().createElement("templates");
                    templatesElem.setAttribute("url", res.toURI().toASCIIString());
                    templatesElem.setAttribute("targetKey", target.getTargetKey());
                    root.getOwnerDocument().getDocumentElement().getElementsByTagName("targets").item(0).appendChild(templatesElem);
                    for(String include: info.getIncludes()) {
                        Element includeElem = root.getOwnerDocument().createElement("include");
                        templatesElem.appendChild(includeElem);
                        includeElem.setAttribute("href", include);
                        
                    }
                    for(String imp: info.getImports()) {
                        Element importElem = root.getOwnerDocument().createElement("import");
                        templatesElem.appendChild(importElem);
                        importElem.setAttribute("href", imp);
                    }
                    for(XSLTemplateInfo xi: info.getTemplates()) {
                        Element templateElem = root.getOwnerDocument().createElement("template");
                        templatesElem.appendChild(templateElem);
                        if(xi.getName() != null && !xi.getName().equals("")) templateElem.setAttribute("name", xi.getName());
                        if(xi.getMatch() != null && !xi.getMatch().equals("")) templateElem.setAttribute("match", xi.getMatch());
                    }
                } catch(XSLInfoParsingException x) {
                    x.printStackTrace();
                }
                templates.add(target.getTargetKey());
            }
            
        }
        
    }
    
    private void deliver(Resource res, HttpServletResponse response) throws IOException {
        if(!"prod".equals(EnvironmentProperties.getProperties().get("mode"))) {
            if(res.exists()) {
                OutputStream out = response.getOutputStream();
        
                String type = servletContext.getMimeType(res.getFilename());
                if (type == null) {
                    type = "application/octet-stream";
                }
                response.setContentType(type);
                response.setHeader("Content-Disposition", "inline;filename="+res.getFilename());
                
                long contentLength = res.length();
                if(contentLength > -1 && contentLength < Integer.MAX_VALUE) {
                    response.setContentLength((int)contentLength);
                }
                
                long lastModified = res.lastModified();
                if(lastModified > -1) {
                    response.setDateHeader("Last-Modified", lastModified);
                }
                
                InputStream in = res.getInputStream();
                byte[] buffer = new byte[4096];
                int no = 0;
                try {
                    while ((no = in.read(buffer)) != -1)
                        out.write(buffer, 0, no);
                } finally {
                    in.close();
                    out.close();
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, res.toString());
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, res.toString());
        }
        
    }
    
    private Element addSearch(Element parent) {
        
    	Element root = parent.getOwnerDocument().createElement("search");
    	parent.appendChild(root);
    
        Element modulesElem = parent.getOwnerDocument().createElement("modules");
        root.appendChild(modulesElem);
        Set<String> modules = ModuleInfo.getInstance().getModules();
        SortedSet<String> sortedModules = new TreeSet<String>();
        sortedModules.addAll(modules);

        for(String module: sortedModules) {
            Element elem = parent.getOwnerDocument().createElement("module");
            elem.setAttribute("name", module);
            modulesElem.appendChild(elem);
        }
        
        return root;
        
    }
    
    public void doSearch(Element root, HttpServletRequest req) {
    	
    	boolean paramErrors = false;
    	
    	//Read file pattern param
    	
    	String filePattern = req.getParameter("filepattern");
    	if(filePattern != null) {
    		root.setAttribute("filepattern", filePattern);
    	}
    		
    	String fileRegexPattern = "";
    	if(filePattern != null) {
    		String[] tmpFilePatterns = filePattern.split(",");
    		for(String tmpFilePattern: tmpFilePatterns) {
    			tmpFilePattern = tmpFilePattern.trim();
    			if(tmpFilePattern.length() > 0) {
    				tmpFilePattern = tmpFilePattern.replace(".", "\\.").replace("+", "\\+").replace("*", ".*").replace("?", ".");
    				fileRegexPattern = fileRegexPattern + (fileRegexPattern.length() == 0 ? "" : "|") + "(" + tmpFilePattern + ")";
    			}
    		}
    	}
    	if(fileRegexPattern.length() == 0) {
    		root.setAttribute("filepatternerror", "You have to enter one or more file name patterns");
    		paramErrors = true;
    	}
    	
    	Pattern fileRegexPatternComp = null;
    	try {
    		fileRegexPatternComp = Pattern.compile(fileRegexPattern, Pattern.CASE_INSENSITIVE);
    	} catch(PatternSyntaxException x) {
    		root.setAttribute("filepatternerror", x.getMessage());
    		paramErrors = true;
    	}
    	
    	//Read search text params
    	
    	String textPattern = req.getParameter("textpattern");
    	
    	boolean textPatternCase = false;
    	if("true".equals(req.getParameter("textpatterncase"))) {
    		textPatternCase = true;
    	}
    	
    	boolean textPatternRegex = false;
    	if("true".equals(req.getParameter("textpatternregex"))) {
    		textPatternRegex = true;
    	}
    	
    	if(textPattern != null) {
    		root.setAttribute("textpattern", textPattern);
    	}
    	root.setAttribute("textpatterncase", String.valueOf(textPatternCase));
    	root.setAttribute("textpatternregex", String.valueOf(textPatternRegex));
    	
    	Pattern textRegexPatternComp = null;
    	if(textPattern != null && textPattern.length() > 0) {
        	if(!textPatternRegex) {
        		textPattern = Pattern.quote(textPattern);
        	}
        	try {
        		if(textPatternCase) {
        			textRegexPatternComp = Pattern.compile(textPattern);
        		} else {
        			textRegexPatternComp = Pattern.compile(textPattern, Pattern.CASE_INSENSITIVE);
        		}
        	} catch(PatternSyntaxException x) {
        		root.setAttribute("textpatternerror", x.getMessage());
        		paramErrors = true;
        	}
    	}
    	
    	//Read search scope params
    	
    	boolean searchWebapp = false;
    	if("true".equals(req.getParameter("searchwebapp"))) {
    		searchWebapp = true;
    	}
    	boolean searchModules = false;
    	if("true".equals(req.getParameter("searchmodules"))) {
    		searchModules = true;
    	}
    	boolean searchClasspath = false;
    	if("true".equals(req.getParameter("searchclasspath"))) {
    		searchClasspath = true;
    	}
    	String searchModule = req.getParameter("searchmodule");
    	if("All modules".equals(searchModule)) {
    		searchModule = null;
    	}
    	
    	root.setAttribute("searchwebapp", String.valueOf(searchWebapp));
    	root.setAttribute("searchmodules", String.valueOf(searchModules));
    	root.setAttribute("searchclasspath", String.valueOf(searchClasspath));
    	if(searchModule != null) {
    		root.setAttribute("searchmodule", searchModule);
    	}
    	
    	
    	if(paramErrors) {
    		return;
    	}
    	
    	//Search
    	
    	FullTextSearch search = new FullTextSearch();
    	search.search(root, fileRegexPatternComp, textRegexPatternComp, searchWebapp, searchModules, searchModule, searchClasspath);
    	
    }
    
    
    public String[] getRegisteredURIs() {
        return new String[] {handlerURI, handlerURI+"/**"};
    }
    
    private void serialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "pfxinternals.ser");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFile));
                out.writeObject(messageList);
                out.close();
            }
        } catch(IOException x) {
            LOG.warn("Error while serializing pfxinternals messages", x);
        }
    }
    
    private void deserialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "pfxinternals.ser");
                if(dataFile.exists()) {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFile));
                    messageList = (MessageList)in.readObject();      
                }
            }
        } catch(Exception x) {
            LOG.warn("Error while deserializing pfxinternals messages", x);
        }
    }
    
    
    private static class MessageList implements Serializable {
        
        private static final long serialVersionUID = 2988781346498479415L;
        
        int max = 10;
        List<Message> messages = new ArrayList<Message>();
        
        synchronized void addMessage(Message.Level level, String text) {
            messages.add(new Message(level, text));
            if(messages.size() > max) messages.remove(0);
        }
        
        synchronized void toXML(Element parent) {
            Element root = parent.getOwnerDocument().createElement("messages");
            parent.appendChild(root);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for(Message msg: messages) {
                Element elem = parent.getOwnerDocument().createElement("message");
                elem.setAttribute("level", msg.level.name());
                elem.setAttribute("date", format.format(msg.date));
                elem.setTextContent(msg.text);
                root.appendChild(elem);
            }
        }
        
    }
    
    private static class Message implements Serializable {
        
        private static final long serialVersionUID = 4467711225014341882L;
        
        public enum Level { INFO, WARN, ERROR }
        
        Message(Level level, String text) {
            this.level = level;
            this.text = text;
            this.date = new Date();
        }
        
        Level level;
        Date date;
        String text;
    
    }
    
}
