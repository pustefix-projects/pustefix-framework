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

public class ServletConfig {

	private static Logger LOG=Activator.getLogger();
	
	private IFile configFile;
	private List<String> pageFlows;
	private List<String> pages;
	private Document doc;
	
	public ServletConfig(IFile configFile) throws Exception {
		this.configFile=configFile;
		read();
	}
	
	private void read() throws Exception {
		pageFlows=new ArrayList<String>();
		pages=new ArrayList<String>();
		if(configFile.exists()) {
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			InputStream in=configFile.getContents();
			doc=db.parse(in);
			NodeList nl=doc.getElementsByTagName("pageflow");
			for(int i=0;i<nl.getLength();i++) {
				Element flowElem=(Element)nl.item(i);
				pageFlows.add(flowElem.getAttribute("name"));
			}
			nl=doc.getElementsByTagName("pagerequest");
			for(int i=0;i<nl.getLength();i++) {
				Element pageElem=(Element)nl.item(i);
				pages.add(pageElem.getAttribute("name"));
			}
		}
		
	}
	
	public List<String> getPageFlows() {
		return pageFlows;
	}
	
	public List<String> getPages() {
		return pages;
	}
	
	public void addPage(String pageName,String state,String pageFlow) {
		Element pagePosElem=null;
		NodeList nl=doc.getElementsByTagName("pagerequest");
		if(nl.getLength()>0) {
			pagePosElem=(Element)nl.item(nl.getLength()-1);
		} else {
			nl=doc.getElementsByTagName("pageflow");
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
		Element pageElem=doc.createElement("pagerequest");
		pageElem.setAttribute("name",pageName);
		if(!(state.trim().equals("") || state.equals("de.schlund.pfixcore.workflow.app.DefaultIWrapperState"))) {
			Element stateElem=doc.createElement("state");
			stateElem.setAttribute("class",state);
		}
		Node afterPosNode=pagePosElem.getNextSibling();
		doc.getDocumentElement().insertBefore(pageElem,afterPosNode);
		
		if(pageFlow!=null && !pageFlow.trim().equals("")) {
			List<Element> list=DOMUtils.getElementsByTagAndAttr(doc.getDocumentElement(),"pageflow","name",pageFlow);
			if(!list.isEmpty()) {
				Element flowElem=list.get(0);
				Element stepElem=doc.createElement("flowstep");
				stepElem.setAttribute("name",pageName);
				flowElem.appendChild(stepElem);
			}
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
