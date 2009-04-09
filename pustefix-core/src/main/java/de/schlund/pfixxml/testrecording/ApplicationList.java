package de.schlund.pfixxml.testrecording;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.schlund.pfixxml.util.XPath;

public class ApplicationList implements Serializable {
    private static final Logger LOG = Logger.getLogger(ApplicationList.class);
    
    private static final long serialVersionUID = 4898818721473076365L;

    public static ApplicationList load(Document projectsXml, boolean tomcat, String sessionSuffix) throws TransformerException {
        ApplicationList result;
        Iterator<?> iter;
        Element project;
        String name;
        String server;
        String defpath;
        
        result = new ApplicationList();
        iter = XPath.select(projectsXml, "/TODO/project[normalize-space(./active/text())='true']").iterator();
        while (iter.hasNext()) {
            project = (Element) iter.next();
            name = project.getAttribute("name");
            defpath = getTextOpt(project, "defpath");
            if (defpath == null) {
                LOG.warn("application " + name + " has no defpath - ignored");
            } else {
                server = getText(project, "servername");
                result.add(new Application(name, server, tomcat, defpath, sessionSuffix));
            }
        }
        return result;
    }
    
    private static String getTextOpt(Node root, String path) throws DOMException, TransformerException {
        if (XPath.select(root, path).size() == 0) {
            return null;
        } else {
            return getText(root, path);
        }
    }

    private static String getText(Node root, String path) throws DOMException, TransformerException {
        return ((Text) XPath.selectOne(root, path + "/text()")).getNodeValue();        
    }
    
    //--
    
    private final List<Application> apps;
    
    public ApplicationList() {
        apps = new ArrayList<Application>();
    }
    
    public void add(Application app) {
        if (lookup(app.getName()) != null) {
            throw new IllegalArgumentException("duplicate application " + app.getName());
        }
        apps.add(app);
    }
    
    public int size() {
        return apps.size();
    }
    
    public Application get(String displayName) {
        Application result;
        
        result = lookup(displayName);
        if (result == null) {
            throw new IllegalArgumentException("unknown application: " + displayName);
        }
        return result;
    }
    
    public Application lookup(String displayName) {
        Iterator<Application> iter;
        Application app;
        
        iter = apps.iterator();
        while (iter.hasNext()) {
            app = (Application) iter.next();
            if (app.getName().equals(displayName)) {
                return app;
            }
        }
        return null;
    }
    
    // TODO: castor
    public List<Application> getApplications() {
        return apps;
    }
    
    @Override
    public String toString() {
        return "applications(" + apps.toString() + ")";
    }
}
