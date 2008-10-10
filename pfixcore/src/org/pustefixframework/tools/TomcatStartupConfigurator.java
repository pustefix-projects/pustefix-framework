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
package org.pustefixframework.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author mleidig
 *
 */
public class TomcatStartupConfigurator {
    
    private static String COMMAND = "java -cp <classpath> "+TomcatStartupConfigurator.class.getName();
    private static String ARGUMENTS = "<path/to/propertyfile> <path/to/server.xml> <path/to/server-runtime.xml>";
    private static String USAGE = "Usage: "+ COMMAND + " "+ ARGUMENTS;
    
    public static void main(String[] args) {
        
        if(args.length!=3) {
            System.err.println("Illegal arguments!");
            System.err.println(USAGE);
            System.exit(1);
        }
     
        File runtimeFile = new File(args[2]);
        
        File propFile = new File(args[0]);
        if(!propFile.exists()) {
            if(runtimeFile.exists()) runtimeFile.delete();
            System.exit(0);
        }
        
        File confFile = new File(args[1]);
        if(!confFile.exists()) {
            System.err.println("Configuration file '"+args[1]+"' doesn't exist!");
            System.exit(1);
        }
     
        Properties props = new Properties();   
        try {
            props.load(new FileInputStream(propFile));
        } catch(IOException x) {
            throw new RuntimeException("Can't read properties",x);
        }
        
        Set<String> projects = new HashSet<String>();
        String projectProp = props.getProperty("projects");
        if(projectProp != null) {
            String[] projectList = projectProp.split("(\\s+)|(\\s*,\\s*)");
            for(String project:projectList) {
                if(project.length()>0) projects.add(project);
            }
        }
        if(projects.isEmpty()) {
            if(runtimeFile.exists()) runtimeFile.delete();
            System.exit(0);
        }
        
        Document doc = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = db.parse(confFile);
        } catch(Exception x) {
            throw new RuntimeException("Can't read original server.xml file.",x);
        }
        
        NodeList hostNodes = doc.getElementsByTagName("Host");
        List<Element> remElems = new ArrayList<Element>();
        for(int i=0; i<hostNodes.getLength(); i++) {
            Element hostElem = (Element)hostNodes.item(i);
            String hostName = hostElem.getAttribute("name");
            int ind = hostName.indexOf('.');
            if(ind>-1) hostName = hostName.substring(0,ind);
            if(!projects.contains(hostName)) {
                NodeList contextNodes = hostElem.getElementsByTagName("Context");
                boolean contains = false;
                for(int j=0; j<contextNodes.getLength() && !contains; j++) {
                    Element contextElem = (Element)contextNodes.item(j);
                    String docBase = contextElem.getAttribute("docBase");
                    ind = docBase.lastIndexOf('/');
                    if(ind>-1) docBase = docBase.substring(ind+1);
                    contains = projects.contains(docBase);
                }
                if(!contains) remElems.add(hostElem);
            }
        }
        for(Element remElem:remElems) {
        	Node parent = remElem.getParentNode();
        	Node comment = parent.getOwnerDocument().createComment(" host '"+remElem.getAttribute("name")+"' was temporarily removed ");
        	parent.replaceChild(comment, remElem);
        }
        
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Source src = new DOMSource(doc);
            Result res = new StreamResult(new FileOutputStream(runtimeFile));
            t.transform(src, res);
        } catch(Exception x) {
            throw new RuntimeException("Can't write transformed server.xml",x);
        }
        
    }

}
