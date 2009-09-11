package org.pustefixframework.maven.plugins.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.pustefixframework.maven.plugins.launcher.BundleConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

public class TargetDefinitionBuilder {
   
    public void build(String targetName, File targetDefinitionFile, List<BundleConfig> bundles) throws MojoExecutionException {
    	
    	Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException("Can't create target definition document", e);
        }
        ProcessingInstruction pi = doc.createProcessingInstruction("pde", "version=\"3.5\"");
        doc.appendChild(pi);
        
    	Element targetElem = doc.createElement("target");
    	targetElem.setAttribute("name", targetName);
    	doc.appendChild(targetElem);
    	Element locationsElem = doc.createElement("locations");
    	targetElem.appendChild(locationsElem);
    	
    	Map<String, Element> pathToLocation = new HashMap<String, Element>();
    	
    	for(BundleConfig bundle:bundles) {
    		String path = bundle.getFile().getParentFile().getAbsolutePath();
    		Element locationElem = pathToLocation.get(path);
    		if(locationElem == null) {
    			locationElem = doc.createElement("location");
    			locationElem.setAttribute("path", path);
    			locationElem.setAttribute("type", "Directory");
    			locationsElem.appendChild(locationElem);
    			Element incElem = doc.createElement("includeBundles");
            	locationElem.appendChild(incElem);
    			pathToLocation.put(path, locationElem);
    		}
        	Element incElem = (Element)locationElem.getElementsByTagName("includeBundles").item(0);
        	Element pluginElem = doc.createElement("plugin");
        	pluginElem.setAttribute("id", bundle.getBundleSymbolicName());
        	incElem.appendChild(pluginElem);
    	}
    	
    	Element argsElem = doc.createElement("launcherArgs");
    	targetElem.appendChild(argsElem);
    	Element progElem = doc.createElement("programArgs");
    	progElem.setTextContent("-reloadworkaround_"+System.currentTimeMillis());
    	argsElem.appendChild(progElem);
    	
    	try {
        	FileOutputStream out = new FileOutputStream(targetDefinitionFile);
        	Transformer transformer = TransformerFactory.newInstance().newTransformer();
        	Source src = new DOMSource(doc);
        	Result res = new StreamResult(out);
        	transformer.transform(src, res);
        	out.close();
    	} catch(Exception x) {
    	    throw new MojoExecutionException("Can't write target definition file", x);
    	}
    
    }
    
}
