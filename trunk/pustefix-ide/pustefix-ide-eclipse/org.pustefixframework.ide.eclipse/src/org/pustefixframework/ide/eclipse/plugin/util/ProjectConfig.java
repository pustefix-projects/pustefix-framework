package org.pustefixframework.ide.eclipse.plugin.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProjectConfig {
	
	private IFile configFile;
	private List<String> handlers;
	private Map<String,IPath> contextPropFiles;
	
	public ProjectConfig(IFile configFile) throws Exception {
		this.configFile=configFile;
		read();
	}
	
	private void read() throws Exception {
		handlers=new ArrayList<String>();
		contextPropFiles=new HashMap<String,IPath>();
		if(configFile.exists()) {
			Document doc=null;
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			InputStream in=configFile.getContents();
			doc=db.parse(in);
			NodeList nl=doc.getElementsByTagName("servlet");
			for(int i=0;i<nl.getLength();i++) {
				Element srvElem=(Element)nl.item(i);
				NodeList classNodes=srvElem.getElementsByTagName("class");
				if(classNodes!=null && classNodes.getLength()==1) {
					Element classElem=(Element)classNodes.item(0);
					String className=classElem.getTextContent();
					if(className!=null) {
						className=className.trim();
						if(className.equals("de.schlund.pfixxml.ContextXMLServer")||
								className.equals("de.schlund.pfixxml.ContextXMLServlet")) {
							String servletName=srvElem.getAttribute("name");
							handlers.add(servletName);
							NodeList propNodes=srvElem.getElementsByTagName("propfile");
							if(propNodes!=null && propNodes.getLength()==1) {
								Element propElem=(Element)propNodes.item(0);
								String propPath=propElem.getTextContent();
								if(propPath!=null) {
									propPath=propPath.trim();
									IPath path=new Path(propPath);
									contextPropFiles.put(servletName,path);
								}
							}
						}
					}
				}
			}
			nl = doc.getElementsByTagName("context-xml-service");
			if(nl.getLength()==1) {
				Element elem = (Element)nl.item(0);
				Element pathElem = DOMUtils.getChildElementByName(elem, "path");
				if(pathElem!=null) {
					String handler = pathElem.getTextContent().trim();
					handlers.add(handler);
					Element fileElem = DOMUtils.getChildElementByName(elem, "config-file");
					if(fileElem!=null) {
						String propPath=fileElem.getTextContent().trim();
						if(propPath.length()>0) {
							if(propPath.startsWith("pfixroot:")) {
								propPath=propPath.substring(9);
								IPath path=new Path(propPath);
								contextPropFiles.put(handler,path);
							}
						}
					}
				}
			}
			
			
		}
	}
	
	public List<String> getHandlers() {
		return handlers;
	}
	
	public IPath getPropFile(String servlet) {
		return contextPropFiles.get(servlet);
	}
	
}
