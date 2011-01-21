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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.pustefixframework.admin.mbeans.AdminBroadcaster;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.util.FrameworkInfo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.ModuleResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
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
    
    private static Logger LOG = Logger.getLogger(PustefixInternalsRequestHandler.class);
    
    private static final String STYLESHEET = "module://pustefix-core/xsl/pfxinternals.xsl";
      
    private String handlerURI ="/xml/pfxinternals";
    private ServletContext servletContext;
    private SessionAdmin sessionAdmin;
    private long startTime;
    private long reloadTimeout = 1000 * 10;
    
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
        
       try {
           String action = req.getParameter("action");
           if(action != null) {
               if(action.equals("reload")) {
                   if((System.currentTimeMillis() - startTime) > reloadTimeout) {
                       messageList.addMessage(Message.Level.INFO, "Scheduled webapp reload.");
                       serialize();
                       ObjectName mbeanName = new ObjectName(AdminBroadcaster.JMX_NAME);
                       MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                       if(server.isRegistered(mbeanName)) {
                           if(server != null) {
                               sessionAdmin.invalidateSessions();
                               File workDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
                               if(workDir != null) {
                                   String[] paramTypes = {"java.lang.String"};
                                   Object[] params = {workDir.getPath()};
                                   server.invoke(mbeanName, "reload", params, paramTypes);
                               } else {
                                   messageList.addMessage(Message.Level.WARN, "Missing servlet context attribute 'javax.servlet.context.tempdir'.");
                               }
                           }
                       } else {
                           messageList.addMessage(Message.Level.WARN, "Can't do reload because Admin mbean isn't registered.");
                       }
                   } else {
                       messageList.addMessage(Message.Level.WARN, "Skipped repeated webapp reload scheduling.");
                   }
                   res.sendRedirect(req.getContextPath()+"/xml/pfxinternals#messages");
                   return;
               } else if(action.equals("invalidate")) {
                   Set<String> sessionIds = sessionAdmin.getAllSessionIds();
                   Iterator<String> it = sessionIds.iterator();
                   while(it.hasNext()) {
                       String sessionId = it.next();
                       sessionAdmin.invalidateSession(sessionId);
                   }
                   messageList.addMessage(Message.Level.INFO, "Invalidated sessions.");
                   res.sendRedirect(req.getContextPath()+"/xml/pfxinternals#messages");
                   return;
               }
           }
            
           Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
           Element root = doc.createElement("pfxinternals");
           doc.appendChild(root);
           addFrameworkInfo(root);
           addEnvironmentInfo(root);
           addJVMInfo(root);
           addModuleInfo(root);
           messageList.toXML(root);
           doc = Xml.parse(XsltVersion.XSLT1, doc);
           Templates stvalue = Xslt.loadTemplates(XsltVersion.XSLT1, (ModuleResource)ResourceUtil.getResource(STYLESHEET));
           res.setContentType("text/html");
           Map<String, Object> params = new HashMap<String, Object>();
           params.put("__contextpath", req.getContextPath());
           Xslt.transform(doc, stvalue, params, new StreamResult(res.getOutputStream()));
        
       } catch(Exception x) {
           LOG.error(x);
           throw new ServletException("Error while creating info page", x);
       }
    }
    
    private void addFrameworkInfo(Element parent) {
        
        Element root = parent.getOwnerDocument().createElement("framework");
        parent.appendChild(root);
        root.setAttribute("version", FrameworkInfo.getVersion());
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
        
    }
    
    private void addJVMInfo(Element parent) {
        
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean(); 
        MemoryUsage mem = mbean.getHeapMemoryUsage();
        Element root = parent.getOwnerDocument().createElement("jvm");
        parent.appendChild(root);
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
    
    private void addModuleInfo(Element parent) {
        
        Element root = parent.getOwnerDocument().createElement("modules");
        parent.appendChild(root);
        Set<String> modules = ModuleInfo.getInstance().getModules();
        SortedSet<String> sortedModules = new TreeSet<String>();
        sortedModules.addAll(modules);
        for(String module: sortedModules) {
            Element elem = parent.getOwnerDocument().createElement("module");
            elem.setAttribute("name", module);
            root.appendChild(elem);
        }
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
