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
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.ModuleResource;
import de.schlund.pfixxml.resources.ResourceUtil;
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
    
    private Logger LOG = Logger.getLogger(PustefixInternalsRequestHandler.class);
    
    private static final String STYLESHEET = "module://pustefix-core/xsl/pfxinternals.xsl";
    
    private String handlerURI ="/xml/pfxinternals";
    private ServletContext servletContext;
   
    private MessageList messageList = new MessageList();
    
    public void setHandlerURI(String handlerURI) {
        this.handlerURI = handlerURI;
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public void afterPropertiesSet() throws Exception {
        deserialize();
        messageList.addMessage(Message.Level.INFO, new Date(), "Webapp started.");
    }
    
    public void destroy() throws Exception {
        messageList.addMessage(Message.Level.INFO, new Date(), "Webapp stopped.");
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
                    messageList.addMessage(Message.Level.INFO, new Date(), "Scheduled webapp reload.");
                    serialize();
                    Thread reloadThread = new ReloadThread(servletContext.getRealPath("/"));
                    reloadThread.start();
                    try { Thread.sleep(1000); } catch(InterruptedException x) {}
                    res.sendRedirect(req.getContextPath()+"/xml/develinfo#messages");
                    return;
                }
            }
            
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("develinfo");
        doc.appendChild(root);
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
    
    public String[] getRegisteredURIs() {
        return new String[] {handlerURI, handlerURI+"/**"};
    }
    
    private void serialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "pfx-develinfo.ser");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFile));
                out.writeObject(messageList);
                out.close();
            }
        } catch(IOException x) {
            LOG.warn("Error while serializing develinfo messages", x);
        }
    }
    
    private void deserialize() {
        try {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            if(tmpDir != null && tmpDir.exists()) {
                File dataFile = new File(tmpDir, "pfx-develinfo.ser");
                if(dataFile.exists()) {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFile));
                    messageList = (MessageList)in.readObject();      
                }
            }
        } catch(Exception x) {
            LOG.warn("Error while deserializing develinfo messages", x);
        }
    }
    
    
    private static class MessageList implements Serializable {
        
        private static final long serialVersionUID = 2988781346498479415L;
        
        int max = 10;
        List<Message> messages = new ArrayList<Message>();
        
        synchronized void addMessage(Message.Level level, Date date, String text) {
            messages.add(new Message(level, date, text));
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
        
        Message(Level level, Date date, String text) {
            this.level = level;
            this.date = date;
            this.text = text;
        }
        
        Level level;
        Date date;
        String text;
    
    }
    
    private static class ReloadThread extends Thread {
        
        private String realPath;
        
        ReloadThread(String realPath) {
            this.realPath = realPath;
        }
        
        public void run() {
            try {
                reload(realPath);
            } catch(Exception x) {
                x.printStackTrace();
            }
        }
        
        public synchronized void reload(String realPath)  {
            ObjectName objectName = getWebModuleObjectName(realPath);
            if(objectName==null) throw new PustefixRuntimeException("Can't reload webapp because "
                    +"no WebModule MBean could be found.");
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                mbeanServer.invoke(objectName,"reload",new Object[0],new String[0]);
            } catch(Exception x) {
                throw new PustefixRuntimeException("Can't reload webapp.",x);
            }
        }
        
        private ObjectName getWebModuleObjectName(String realPath) {
            try {
                Hashtable<String, String> props = new Hashtable<String,String>();
                props.put("J2EEApplication","none");
                props.put("J2EEServer","none");
                props.put("j2eeType","WebModule");
                props.put("name","//"+"*"+"/*");
                ObjectName objectNamePattern = new ObjectName("*",props);
                MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
                if(mbeanServer!=null) {
                    Set<ObjectName> objectNames = mbeanServer.queryNames(objectNamePattern, null);
                    for(ObjectName objectName:objectNames) {
                        String docBase =(String)mbeanServer.getAttribute(objectName,"docBase");
                        if(!docBase.endsWith("/")) docBase += "/";
                        if(docBase.equals(realPath)) return objectName;
                    }
                } 
                return null;
            } catch(Exception x) {
                throw new PustefixRuntimeException("Error while trying to find WebModule mbean name.",x);
            } 
        }
        
    }
    
}
