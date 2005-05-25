package de.schlund.pfixxml.jmx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixxml.util.XPath;

public class ApplicationList implements Serializable {
    public static ApplicationList load(Document serverXml, boolean tomcat) throws TransformerException {
        ApplicationList result;
        List hosts;
        Iterator iter;
        Element host;
        String server;
        String docBase;
        String name;
        
        result = new ApplicationList();
        hosts = XPath.select(serverXml, "/Server/Service/Engine/Host");
        iter = hosts.iterator();
        while (iter.hasNext()) {
            host = (Element) iter.next();
            server = host.getAttribute("name");
            docBase = ((Attr) XPath.selectOne(host, "Context[@path='/xml']/@docBase")).getValue();
            name = docBase.substring(docBase.lastIndexOf('/') + 1); // ok for - 1
            result.add(Application.create(name, server, tomcat));
        }
        return result;
    }

    
    //--
    
    private final List apps;
    
    public ApplicationList() {
        apps = new ArrayList();
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
        Iterator iter;
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
    public List getApplications() {
        return apps;
    }
}
