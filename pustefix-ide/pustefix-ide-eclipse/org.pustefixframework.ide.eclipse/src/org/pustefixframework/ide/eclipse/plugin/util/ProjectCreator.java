package org.pustefixframework.ide.eclipse.plugin.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProjectCreator {

	public static void createDirectories(IFolder projectRootDir,String projectName) throws CoreException {
		IFolder projectFolder=projectRootDir.getFolder(projectName);
		projectFolder.create(IResource.NONE,true,null);
		IFolder folder=projectFolder.getFolder("conf");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("htdocs");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("img");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("txt");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("txt/pages");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("xml");
		folder.create(IResource.NONE,true,null);
		folder=projectFolder.getFolder("xsl");
		folder.create(IResource.NONE,true,null);
	}
	
	private static void replaceTextContent(Document doc,String element,String text) {
		Element elem=(Element)doc.getElementsByTagName(element).item(0);
		elem.setTextContent(text);
	}
	
	private static void removeChildNodes(Element elem) {
		while(elem.hasChildNodes()) elem.removeChild(elem.getFirstChild());
	}
	
	public static void createConfiguration(IFolder projectRootDir,String projectName,String servletName,String defaultLang,String comment) throws CoreException {
		
		IFolder templateFolder=projectRootDir.getFolder("core/prjtemplates");
		IFolder projectDir=projectRootDir.getFolder(projectName);
		IFolder confFolder=projectDir.getFolder("conf");
		
		IFile prjTmpl=templateFolder.getFile("project.tmpl");
		IFile prjFile=confFolder.getFile("project.xml.in");
		InputStream in=prjTmpl.getContents();
		Document prjDoc=loadXML(in);
		
		if(prjDoc.getDocumentElement().getNodeName().equals("project")) {
			prjDoc.getDocumentElement().setAttribute("name",projectName);
			
			replaceTextContent(prjDoc,"comment",comment);
			replaceTextContent(prjDoc,"depend",projectName+"/conf/depend.xml");
			
			Element e=(Element)prjDoc.getElementsByTagName("servername").item(0);
			removeChildNodes(e);
			e.appendChild(prjDoc.createTextNode(projectName+"."));
			e.appendChild(prjDoc.createElement("cus:fqdn"));
			
			e=(Element)prjDoc.getElementsByTagName("serveralias").item(0);
			removeChildNodes(e);
			e.appendChild(prjDoc.createTextNode(projectName+"."));
			e.appendChild(prjDoc.createElement("cus:machine"));
			
			replaceTextContent(prjDoc,"defpath","/xml/"+servletName);
			replaceTextContent(prjDoc,"passthrough",projectName+"/img");
			replaceTextContent(prjDoc,"documentroot","<cus:docroot/>"+projectName+"/htdocs");
			
			e=(Element)prjDoc.getElementsByTagName("documentroot").item(0);
			removeChildNodes(e);
			e.appendChild(prjDoc.createElement("cus:docroot"));
			e.appendChild(prjDoc.createTextNode(projectName+"/htdocs"));
			
			Element srvElem=prjDoc.createElement("servlet");
			prjDoc.getDocumentElement().appendChild(srvElem);
			srvElem.setAttribute("name",servletName);
			srvElem.setAttribute("useineditor","true");
			srvElem.appendChild(prjDoc.createTextNode("\n"));
			Element actElem=prjDoc.createElement("active");
			srvElem.appendChild(actElem);
			actElem.setTextContent("true");
			srvElem.appendChild(prjDoc.createTextNode("\n"));
			Element classElem=prjDoc.createElement("class");
			srvElem.appendChild(classElem);
			
			String ctxServlet="de.schlund.pfixxml.ContextXMLServlet";
			NodeList nl=prjDoc.getElementsByTagName("class");
			for(int i=0;i<nl.getLength();i++) {
				Element elem=(Element)nl.item(i);
				String text=elem.getTextContent();
				if(text!=null && text.startsWith("de.schlund.pfixxml.ContextXML"))
					ctxServlet=elem.getTextContent().trim();
			}
			
			classElem.setTextContent(ctxServlet);
			srvElem.appendChild(prjDoc.createTextNode("\n"));
			Element propElem=prjDoc.createElement("propfile");
			srvElem.appendChild(propElem);
			propElem.setTextContent(projectName+"/conf/"+servletName+".conf.xml");	
		
		} else {
			
	        DOMUtils.replaceTextPlaceHolders(prjDoc.getDocumentElement(), "###PROJECTNAME###", projectName);
	        DOMUtils.replaceTextPlaceHolders(prjDoc.getDocumentElement(), "###PROJECTDESCRIPTION###", comment);
	        
		}
		
		saveXML(prjDoc,prjFile);
		
		
		IFile configTmpl=templateFolder.getFile("config.tmpl");
		in=configTmpl.getContents();
		Document configDoc=loadXML(in);
		IFile configFile = null;
		if(configDoc.getDocumentElement().getNodeName().equals("contextxmlserver")) {
			Element infoElem=(Element)configDoc.getElementsByTagName("servletinfo").item(0);
			infoElem.setAttribute("depend",projectName+"/conf/depend.xml");
			infoElem.setAttribute("name","pfixcore_project:"+projectName+"::servlet:"+servletName);
			configFile=confFolder.getFile(servletName+".conf.xml");
		} else {
			configDoc = DOMUtils.changeAttributes(configDoc, "flowstep", "name", "home", false);
			configDoc = DOMUtils.changeAttributes(configDoc, "pagerequest", "name", "home", false);
			configFile=confFolder.getFile("config.conf.xml");
			servletName = "config";
		}
		saveXML(configDoc,configFile);
		
		IFile dependTmpl=templateFolder.getFile("depend.tmpl");
		IFile dependFile=confFolder.getFile("depend.xml");
		in=dependTmpl.getContents();
		Document dependDoc=loadXML(in);
		Element makeElem=(Element)dependDoc.getElementsByTagName("make").item(0);
		makeElem.setAttribute("project",projectName);
		makeElem.setAttribute("lang",defaultLang);
		Element naviElem=(Element)dependDoc.getElementsByTagName("navigation").item(0);
		Element pageElem=dependDoc.createElement("page");
		naviElem.appendChild(pageElem);
		pageElem.setAttribute("handler","/xml/"+servletName);
		pageElem.setAttribute("name","home");
		Element stdPageElem=dependDoc.createElement("standardpage");
		makeElem.appendChild(stdPageElem);
		stdPageElem.setAttribute("name","home");
		stdPageElem.setAttribute("xml",projectName+"/xml/frame.xml");
		NodeList nl=dependDoc.getElementsByTagName("include");
		for(int i=0;i<nl.getLength();i++) {
			Element elem=(Element)nl.item(i);
			String val=elem.getAttribute("stylesheet");
			val=val.replaceFirst("myproject",projectName);
			elem.setAttribute("stylesheet",val);
		}
		saveXML(dependDoc,dependFile);
	
	}
	
	public static void createXMLPages(IFolder projectRootDir,String projectName) throws CoreException {
		
		IFolder templateFolder=projectRootDir.getFolder("core/prjtemplates");
		IFolder projectDir=projectRootDir.getFolder(projectName);
		IFolder pageFolder=projectDir.getFolder("txt/pages");
		IFolder xmlFolder=projectDir.getFolder("xml");
		IFolder xslFolder=projectDir.getFolder("xsl");
		
		IFile pageTmpl=templateFolder.getFile("page.tmpl");
		IFile pageFile=pageFolder.getFile("main_home.xml");
		InputStream in=pageTmpl.getContents();
		Document pageDoc=loadXML(in);
		Element themeElem=(Element)pageDoc.getElementsByTagName("theme").item(0);
		Element contentElem=pageDoc.createElement("h1");
		themeElem.appendChild(contentElem);
		contentElem.setTextContent("Home of project "+projectName);
		saveXML(pageDoc,pageFile);
		
		IFile frameTmpl=templateFolder.getFile("frame.tmpl");
		IFile frameFile=xmlFolder.getFile("frame.xml");
		frameTmpl.copy(frameFile.getFullPath(), true, null);
	
		IFile skinTmpl=templateFolder.getFile("skin.tmpl");
		IFile skinFile=xslFolder.getFile("skin.xsl");
		skinTmpl.copy(skinFile.getFullPath(), true, null);
		
		IFile metaTmpl=templateFolder.getFile("metatags.tmpl");
		IFile metaFile=xslFolder.getFile("metatags.xsl");
		metaTmpl.copy(metaFile.getFullPath(), true, null);
		
	}
	
	public static void createXMLPage(IFolder projectRootDir,String projectName,String pageName) throws CoreException {
		
		IFolder templateFolder=projectRootDir.getFolder("core/prjtemplates");
		IFolder projectDir=projectRootDir.getFolder(projectName);
		IFolder pageFolder=projectDir.getFolder("txt/pages");
	
		IFile pageTmpl=templateFolder.getFile("page.tmpl");
		IFile pageFile=pageFolder.getFile("main_"+pageName+".xml");
		InputStream in=pageTmpl.getContents();
		Document pageDoc=loadXML(in);
		Element themeElem=(Element)pageDoc.getElementsByTagName("theme").item(0);
		Element contentElem=pageDoc.createElement("h1");
		themeElem.appendChild(contentElem);
		contentElem.setTextContent("Page "+pageName);
		saveXML(pageDoc,pageFile);
		
	}
	
	private static Document loadXML(InputStream in) {
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db=dbf.newDocumentBuilder();
			return db.parse(in);
		} catch(Exception x) {
			throw new RuntimeException("Can't load XML",x);
		}
	}
	
	private static void saveXML(Document doc,IFile file) {
		TransformerFactory tf=TransformerFactory.newInstance();
		try {
			Transformer t=tf.newTransformer();
			Source src=new DOMSource(doc);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			Result res=new StreamResult(out);
			t.transform(src,res);						
			if(file.exists()) {
				file.setContents(new ByteArrayInputStream(out.toByteArray()),true,false,null);
			} else {
				ResourceUtils.createParentFolder(file);
				file.create(new ByteArrayInputStream(out.toByteArray()),false,null);
			}
		} catch(Exception x) {
			throw new RuntimeException("Can't save XML",x);
		}
	}

}
