/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sun.management.ConnectorAddressLink;

/**
 * 
 * @author mleidig
 *
 */
public class WebappAdminClient {
    
    public static String USAGE_ARGS = "-p pid [-f path/to/server.xml] -c reload|refresh hostname|projectname ...";
    public static String USAGE = "Usage: java "+WebappAdminClient.class.getName()+" "+USAGE_ARGS;
    public static String DEFAULT_SERVERXML_PATH = "projects/servletconf/tomcat/conf/server.xml";
    public static String DEFAULT_COMMAND = "reload";
    
    private Map<String,String> hostNameToProjectName;
    private Map<String,String> projectNameToHostName;
    
    public WebappAdminClient(File serverXmlFile) {
        readServerXml(serverXmlFile);
    }
    
    public void exec(int pid, String command, List<String> args) throws WebappAdminClientException {
        
        String localUrl = null;
        try {
            localUrl=ConnectorAddressLink.importFrom(pid);
        } catch(IOException x) {
            throw new WebappAdminClientException("Can't find process with pid "+pid);
        }
        if(command.equals("reload")||command.equals("refresh")) {
            for(String arg: args) {
                try {
                    String projectName = resolveProjectName(arg);
                    if(projectName == null) throw new WebappAdminClientException("No valid projectname or hostname: "+arg);
                    JMXServiceURL url = new JMXServiceURL(localUrl); 
                    JMXConnector jmxc = JMXConnectorFactory.connect(url, null); 
                    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
                    ObjectName objectName = new ObjectName("Pustefix:type=WebappAdmin,project="+projectName);
                    if(mbsc.isRegistered(objectName)) {
                        mbsc.invoke(objectName,command,new Object[0],new String[0]);
                    } else {
                        throw new WebappAdminClientException("Can't find WebappAdmin MBean for project '"+projectName+"'.");
                    }
                } catch(WebappAdminClientException x) {
                    throw x;
                } catch(Exception x) {
                    throw new RuntimeException("Can't execute command '"+command+"'.",x);
                }
            }
        } else throw new WebappAdminClientException("Illegal command: "+command);
    }

    private String resolveProjectName(String name) {
        if(projectNameToHostName.containsKey(name)) return name;
        return hostNameToProjectName.get(name);
    }
    
    private void readServerXml(File file) {
        try {
            hostNameToProjectName = new HashMap<String,String>();
            projectNameToHostName = new HashMap<String,String>();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(file);
            NodeList hostNodes = doc.getElementsByTagName("Host");
            for(int i=0; i<hostNodes.getLength(); i++) {
                Element hostElem = (Element)hostNodes.item(i);
                String hostName = hostElem.getAttribute("name");
                NodeList contextNodes = hostElem.getElementsByTagName("Context");
                for(int j=0; j<contextNodes.getLength(); j++) {
                    Element contextElem = (Element)contextNodes.item(j);
                    String docBase = contextElem.getAttribute("docBase");
                    String projectName = docBase;
                    int ind = projectName.lastIndexOf('/');
                    if(ind>-1) projectName = projectName.substring(ind+1);
                    hostNameToProjectName.put(hostName,projectName);
                    projectNameToHostName.put(projectName,hostName);
                }
            }
        } catch(Exception x) {
            throw new RuntimeException("Can't read server.xml",x);
        }
    }

    public static void abort(String msg) {
        System.err.println(msg);
        System.err.println(USAGE);
        System.exit(1);
    }
    
    public static void main(String[] args) {
        List<String> names = new ArrayList<String>();
        String path = null;
        String command = null;
        int pid = -1;
        for(int i=0;i<args.length;i++) {
            if(args[i].equals("-f")) {
                i++;
                if(i==args.length) abort("Missing argument value: "+args[i-1]);
                path = args[i];
            } else if(args[i].equals("-c")) {
                i++;
                if(i==args.length) abort("Missing argument value: "+args[i-1]);
                command = args[i];
            } else if(args[i].equals("-p")) {
                i++;
                if(i==args.length) abort("Missing argument value: "+args[i-1]);
                pid = Integer.parseInt(args[i]);
            } else {
                names.add(args[i]);
            }
        }
        if(pid==-1) abort("Missing mandatory argument: -p");
        if(path==null) path = DEFAULT_SERVERXML_PATH;
        if(command==null) command = DEFAULT_COMMAND;
        File serverXmlFile = new File(path);
        WebappAdminClient client = new WebappAdminClient(serverXmlFile);
        try {
            client.exec(pid, command, names);
            System.exit(0);
        } catch(WebappAdminClientException x) {
            System.err.println(x.getMessage());
            System.exit(1);
        } catch(Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    class WebappAdminClientException extends Exception {
        
        private static final long serialVersionUID = 1986758512455077447L;

        public WebappAdminClientException(String msg) {
            super(msg);
        }
        
        public WebappAdminClientException(String msg, Throwable cause) {
            super(msg,cause);
        }
        
    }
    
}
