package org.pustefixframework.ide.eclipse.plugin.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;

public class DependConfig {

	private static Logger LOG=Activator.getLogger();
	
	private IFile configFile;
	private Document doc;
	private List<String> standardPages;
	
	public DependConfig(IFile configFile) throws Exception {
		this.configFile=configFile;	
		read();
	}
	
	private void read() throws Exception {
		standardPages=new ArrayList<String>();
		if(configFile.exists()) {
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			InputStream in=configFile.getContents();
			doc=db.parse(in);
			NodeList nl=doc.getElementsByTagName("standardpage");
			if(nl.getLength()>0) {
				for(int i=0;i<nl.getLength();i++) {
					Element elem=(Element)nl.item(i);
					standardPages.add(elem.getAttribute("name"));
				}
			}
		}
	}
	
	public List<String> getStandardPages() {
		return standardPages;
	}
	
	public void addStandardPage(String pageName,String xmlBase,String handler) {
		Element pagePosElem=null;
		NodeList nl=doc.getElementsByTagName("standardpage");
		if(nl.getLength()>0) {
			pagePosElem=(Element)nl.item(nl.getLength()-1);
		} else {
			nl=doc.getElementsByTagName("target");
			if(nl.getLength()>0) {
				pagePosElem=(Element)nl.item(nl.getLength()-1);
			} else {
				nl=doc.getDocumentElement().getChildNodes();
				if(nl.getLength()>0) {
					pagePosElem=(Element)nl.item(nl.getLength()-1);
				} else {
					LOG.info("Found no position to insert pagerequest.");
					return;
				}
			}
		}
		Element stdElem=doc.createElement("standardpage");
		stdElem.setAttribute("name",pageName);
		stdElem.setAttribute("xml",xmlBase);
		Node afterPosNode=pagePosElem.getNextSibling();
		doc.getDocumentElement().insertBefore(stdElem,afterPosNode);
		
		Element pageElem=doc.createElement("page");
		pageElem.setAttribute("name",pageName);
		pageElem.setAttribute("handler",handler);
		nl=doc.getElementsByTagName("navigation");
		if(nl.getLength()>0) {
			((Element)nl.item(0)).appendChild(pageElem);
		}
		
	}
	
	public void save() {
		
		TransformerFactory tf=TransformerFactory.newInstance();
		try {
			Transformer t=tf.newTransformer();
			Source src=new DOMSource(doc);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			Result res=new StreamResult(out);
			t.transform(src,res);						
			configFile.setContents(new ByteArrayInputStream(out.toByteArray()),true,false,null);
		} catch(Exception x) {
			throw new RuntimeException("Can't save XML",x);
		}
		
	}
	
	
}
