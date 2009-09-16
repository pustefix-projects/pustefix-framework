package org.pustefixframework.maven.plugins.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.pustefixframework.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adds linked resources to META-INF and PUSTEFIX-INF to Eclipse .project,
 * so that PDE is satisfied and detects bundle project
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectConfigBuilder {
	
	public void build(File baseDir) throws MojoExecutionException {
		DocumentBuilder docBuilder;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch(ParserConfigurationException x) {
			throw new MojoExecutionException("Can't get DOM factory", x);
		}
		File projectFile = new File(baseDir, ".project");
		if(projectFile.exists()) {
			Document doc;
			try {
				doc = docBuilder.parse(projectFile);
			} catch(Exception x) {
				throw new MojoExecutionException("Can't read .project file", x);
			}
			Element root = doc.getDocumentElement();
			if(root != null && root.getNodeName().equals("projectDescription")) {
				Element linkedRes = DOMUtils.getChildElementByTagName(root, "linkedResources");
				if(linkedRes == null) {
					linkedRes = doc.createElement("linkedResources");
					root.appendChild(linkedRes);
					DOMUtils.format(linkedRes, 2, 2);
				}
				List<Element> links = DOMUtils.getChildElementsByTagName(linkedRes, "link");
				Set<String> linkNames = new HashSet<String>();
				for(Element link: links) {
					Element name = DOMUtils.getChildElementByTagName(link, "name");
					if(name != null) linkNames.add(name.getTextContent().trim());
				}
				//we're using absolute paths here, because Eclipse 3.x doesn't support project relative links
				Element linkElem = null;
				if(!linkNames.contains("META-INF")) {
					linkElem = addLinkElement(linkedRes, "META-INF", 2, new File(baseDir, "target/classes/META-INF"));
					DOMUtils.format(linkElem, 4, 2);
				}
				if(!linkNames.contains("PUSTEFIX-INF")) {
					linkElem = addLinkElement(linkedRes, "PUSTEFIX-INF", 2, new File(baseDir, "src/main/resources/PUSTEFIX-INF"));
					DOMUtils.format(linkElem, 4, 2);
				}
			}
			Transformer transformer;
			try {
				transformer = TransformerFactory.newInstance().newTransformer();
			} catch(TransformerException x) {
				throw new MojoExecutionException("Can't get XSL Tranformer", x);
			}
			DOMSource src = new DOMSource(doc);
			StreamResult res;
			try {
				res = new StreamResult(new FileOutputStream(projectFile));
			} catch(IOException x) {
				throw new MojoExecutionException("Can't write .project file", x);
			}
			try {
				transformer.transform(src, res);
			} catch (TransformerException x) {
				throw new MojoExecutionException("Can't write .project file", x);
			}
		}
		
	}
	
	private Element addLinkElement(Element parent, String name, int type, File location) {
		Document doc = parent.getOwnerDocument();
		Element linkElem = doc.createElement("link");
		Element nameElem = doc.createElement("name");
		nameElem.setTextContent(name);
		linkElem.appendChild(nameElem);
		Element typeElem = doc.createElement("type");
		typeElem.setTextContent(String.valueOf(type));
		linkElem.appendChild(typeElem);
		Element locElem = doc.createElement("location");
		locElem.setTextContent(location.getAbsolutePath());
		linkElem.appendChild(locElem);
		parent.appendChild(linkElem);
		return linkElem;
	}
	
}
