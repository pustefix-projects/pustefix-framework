package org.pustefixframework.maven.plugins.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pustefixframework.maven.plugins.launcher.BundleConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LaunchConfigurationBuilder {

	public void build(File runConfigurationFile, List<BundleConfig> bundles) {
		
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException x) {
			throw new RuntimeException("Error creating DOM.", x);
		}
		
		Element root = doc.createElement("launchConfiguration");
		root.setAttribute("type", "org.eclipse.pde.ui.EquinoxLauncher");
		doc.appendChild(root);
		
		addBooleanAttribute(root, "append.args", true);
		addBooleanAttribute(root, "automaticAdd", true);
		addBooleanAttribute(root, "automaticValidate", false);
		addStringAttribute(root, "bootstrap", "");
		addStringAttribute(root, "checked", "[NONE]");
		addBooleanAttribute(root, "clearConfig", false);
		addStringAttribute(root, "configLocation", runConfigurationFile.getAbsolutePath());
		addBooleanAttribute(root, "default_auto_start", true);
		addIntegerAttribute(root, "default_start_level", 4);
		//addStringAttribute(root, "deselected_workspace_plugins", "");
		addBooleanAttribute(root, "includeOptional", true);
		addStringAttribute(root, "org.eclipse.jdt.launching.PROGRAM_ARGUMENTS", 
			"-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl} -clean -console");
		addStringAttribute(root, "org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER", 
				"org.eclipse.pde.ui.workbenchClasspathProvider");
		addStringAttribute(root, "org.eclipse.jdt.launching.VM_ARGUMENTS",
			"-Declipse.ignoreApp=true -Dosgi.noShutdown=true -Dorg.osgi.service.http.port=8080");
		addStringAttribute(root, "pde.version", "3.3");
		addBooleanAttribute(root, "show_selected_only", false);
		
		boolean autoStart = true;
		int defaultStartLevel = 4;
		StringBuilder sb = new StringBuilder();
	   	Iterator<BundleConfig> it = bundles.iterator();
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		sb.append(bundle.getBundleSymbolicName());
    		sb.append("@");
    		if(bundle.getStartLevel() == defaultStartLevel) sb.append("default");
    		else sb.append(bundle.getStartLevel());
    		sb.append(":");
    		if((autoStart && bundle.doStart()) || (!autoStart && !bundle.doStart())) sb.append("default");
    		else if(bundle.doStart()) sb.append("true");
    		else sb.append("false");
    		if(it.hasNext()) sb.append(",");
		}
    	addStringAttribute(root, "target_bundles", sb.toString());
    	
    	addBooleanAttribute(root, "tracing", false);
    	addBooleanAttribute(root, "useDefaultConfigArea", true);
    	
    	Transformer transformer;
    	try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException x) {
			throw new RuntimeException("Error creating transformer", x);
		} catch (TransformerFactoryConfigurationError x) {
			throw new RuntimeException("Error creating transformer", x);
		}
		Source src = new DOMSource(doc);
		try {
			Result res = new StreamResult(new FileOutputStream(runConfigurationFile));
			transformer.transform(src, res);
    	
		} catch(Exception x) {
			throw new RuntimeException("Error writing run configuration to " + runConfigurationFile.getAbsolutePath(), x);
		}
		
	}

	private void addStringAttribute(Element parent, String key, String value) {
		Element elem = parent.getOwnerDocument().createElement("stringAttribute");
		elem.setAttribute("key", key);
		elem.setAttribute("value", value);
		parent.appendChild(elem);
	}

	private void addBooleanAttribute(Element parent, String key, boolean value) {
		Element elem = parent.getOwnerDocument().createElement("booleanAttribute");
		elem.setAttribute("key", key);
		elem.setAttribute("value", String.valueOf(value));
		parent.appendChild(elem);
	}

	private void addIntegerAttribute(Element parent, String key, int value) {
		Element elem = parent.getOwnerDocument().createElement("intAttribute");
		elem.setAttribute("key", key);
		elem.setAttribute("value", String.valueOf(value));
		parent.appendChild(elem);
	}

}
