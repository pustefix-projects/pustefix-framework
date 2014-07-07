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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.pfxinternals.Action;
import org.pustefixframework.pfxinternals.CacheCategory;
import org.pustefixframework.pfxinternals.Category;
import org.pustefixframework.pfxinternals.DownloadAction;
import org.pustefixframework.pfxinternals.DuplicatesAction;
import org.pustefixframework.pfxinternals.EnvironmentCategory;
import org.pustefixframework.pfxinternals.FrameworkCategory;
import org.pustefixframework.pfxinternals.IncludesCategory;
import org.pustefixframework.pfxinternals.InvalidateAction;
import org.pustefixframework.pfxinternals.JVMCategory;
import org.pustefixframework.pfxinternals.ModulesCategory;
import org.pustefixframework.pfxinternals.PageContext;
import org.pustefixframework.pfxinternals.ReloadAction;
import org.pustefixframework.pfxinternals.RetargetAction;
import org.pustefixframework.pfxinternals.SearchCategory;
import org.pustefixframework.pfxinternals.SendErrorAction;
import org.pustefixframework.pfxinternals.SystemCategory;
import org.pustefixframework.pfxinternals.TargetsCategory;
import org.pustefixframework.pfxinternals.ToolextAction;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class PustefixInternalsRequestHandler implements UriProvidingHttpRequestHandler, ServletContextAware, 
        ApplicationContextAware, PageContext {
    
    private final static Logger LOG = Logger.getLogger(PustefixInternalsRequestHandler.class);
    
    private final static String STYLESHEET = "module://pustefix-core/xsl/pfxinternals.xsl";
      
    private String handlerURI ="/pfxinternals";
    private ServletContext servletContext;

    private Map<String, Action> actions = new HashMap<String, Action>();
    private Map<String, Category> categories = new HashMap<String, Category>();
    private ApplicationContext applicationContext;
    private List<Message> messages = new ArrayList<Message>();
    
    public PustefixInternalsRequestHandler() {
        actions.put("duplicates", new DuplicatesAction());
        actions.put("download", new DownloadAction());
        actions.put("reload", new ReloadAction());
        actions.put("invalidate", new InvalidateAction());
        actions.put("toolext", new ToolextAction());
        actions.put("retarget", new RetargetAction());
        actions.put("senderror", new SendErrorAction());
        categories.put("framework", new FrameworkCategory());
        categories.put("environment", new EnvironmentCategory());
        categories.put("jvm", new JVMCategory());
        categories.put("system", new SystemCategory());
        categories.put("modules", new ModulesCategory());
        categories.put("cache", new CacheCategory());
        categories.put("targets", new TargetsCategory());
        categories.put("includes", new IncludesCategory());
        categories.put("search", new SearchCategory());
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void setHandlerURI(String handlerURI) {
        this.handlerURI = handlerURI;
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
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
               Action actionInstance = actions.get(action);
               if(actionInstance != null) {
                   actionInstance.execute(req, res, this);
                   return;
               }
           }
           
           Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
           Element root = doc.createElement("pfxinternals");
           doc.appendChild(root);
           if(category != null) {
               Category categoryInstance = categories.get(category);
               if(categoryInstance != null) {
                   categoryInstance.model(root, req, this);
               }
           }
           
           if(!messages.isEmpty()) {
               Element msgsElem = doc.createElement("messages");
               root.appendChild(msgsElem);
               for(Message message: messages) {
                   Element msgElem = doc.createElement("message");
                   msgsElem.appendChild(msgElem);
                   msgElem.setAttribute("level", message.level.toString().toLowerCase());
                   msgElem.setTextContent(message.msg);
               }
               messages.clear();
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
    
    @Override
    public void addMessage(PageContext.MessageLevel level, String msg) {
        messages.add(new Message(level, msg));
    }
    
    public String[] getRegisteredURIs() {
        return new String[] {handlerURI, handlerURI+"/**"};
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    
    private class Message {
        
        Message(MessageLevel level, String msg) {
            this.level = level;
            this.msg = msg;
        }
        
        String msg;
        MessageLevel level;
    }

}
