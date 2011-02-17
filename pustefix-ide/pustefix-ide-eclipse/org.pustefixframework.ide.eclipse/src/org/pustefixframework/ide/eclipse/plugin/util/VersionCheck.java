package org.pustefixframework.ide.eclipse.plugin.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;

public class VersionCheck {
	
	private static Logger LOG=Activator.getLogger();
	
	public static PustefixVersion getPustefixVersion(IProject project) {
		
		//try to get version from pfixcore jar name in classpath
		IJavaProject javaProject=JavaCore.create(project);
		try {
			IClasspathEntry[] entries=javaProject.getRawClasspath();
			for(IClasspathEntry entry:entries) {
				//if(entry.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
					//TODO: try to get from MANIFEST.MF
					String name = entry.getPath().lastSegment();
					System.out.println(name);
					PustefixVersion pv = PustefixVersion.parseVersion(name);
					if(pv != null) return pv;
				//}
			}
		} catch(JavaModelException x) {
			//ignore and try fallback
		}
		
		//try to get version from pom.xml if it's pfixcore itself
		IFile file = project.getFile("pom.xml");
		if(file.exists()) {
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(file.getContents());
				Element prjElem = doc.getDocumentElement();
				Element artElem = DOMUtils.getChildElementByName(prjElem, "artifactId");
				if(artElem!=null) {
					String artifactId = artElem.getTextContent().trim();
					if(artifactId.equals("pfixcore")) {
						Element verElem = DOMUtils.getChildElementByName(prjElem, "version");
						if(verElem!=null) {
							String version = verElem.getTextContent().trim();
							return PustefixVersion.parseVersion(version);
						}
					}
				}
			} catch(Exception x) {
				//ignore and try fallback
			}
		}
			
		return null;
	}
	
}
