package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class ServletConfigValidator {
	
    private Logger LOG = Activator.getLogger();
    
	Environment environment;
	ServletConfigVisitor visitor;
	IJavaProject javaProject;
	
	public ServletConfigValidator(Environment environment,IJavaProject javaProject) {
		this.environment=environment;
		this.javaProject=javaProject;
		visitor=new ServletConfigVisitor();
	}
	
	public void incrementalBuild(IResourceDelta delta,IProgressMonitor monitor) throws CoreException {
		IResourceDelta prjDelta=delta.findMember(new Path("src/main/webapp/WEB-INF"));
		if(prjDelta!=null) prjDelta.accept(visitor);
	}
	
	public void validate(IFile file) {
		ResourceUtils.deleteProblemMarkers(file, false);
		try {
			XMLReader reader=XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new MyContentHandler(file,javaProject));
			InputStream in=file.getContents();
			reader.parse(new InputSource(in));
		} catch(Exception x) {
		    LOG.error(x);
		}
	}
	
	public void undoValidate(IFile file) {
		ResourceUtils.deleteProblemMarkers(file, false);
	}
	
	class ServletConfigVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource=delta.getResource();
			if(resource instanceof IFile && resource.getName().endsWith(".conf.xml")) {
				IFile file=(IFile)resource;
				int kind=delta.getKind();
				if(kind==IResourceDelta.CHANGED||kind==IResourceDelta.ADDED) {
					validate(file);
				} else if(kind==IResourceDelta.REMOVED) {
					undoValidate(file);
				}
			}
			return true;
		}
	}
	
	class MyContentHandler extends DefaultHandler {
		
		Locator locator;
		IJavaProject javaProject;
		IFile xmlFile;
		
		MyContentHandler(IFile xmlFile,IJavaProject javaProject) {
			this.xmlFile=xmlFile;
			this.javaProject=javaProject;
		}
		
		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator=locator;
		}
		
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			String className=attributes.getValue("class");
			if(className!=null && !className.startsWith("script:")) {
				try {
					IType type=javaProject.findType(className);
					if(type==null) {
						ResourceUtils.addProblemMarker(xmlFile,"Class not found: "+className,locator.getLineNumber());
					}
				} catch(JavaModelException x) {
					LOG.error(x);
				}
			
			}
		}
		
	}
	

}
