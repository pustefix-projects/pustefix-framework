package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;
import org.xml.sax.SAXParseException;

public class IWrapperGenerator {

	private static Logger LOG=Activator.getLogger();
	
	private Environment environment;
	private IResourceDeltaVisitor srcVisitor;
	private IResourceDeltaVisitor genSrcVisitor;
	private URL iwrapperTemplateURL;
	private Templates iwrapperTemplates;
	
	public IWrapperGenerator(Environment environment, URL iwrapperTemplateURL) {
		this.environment = environment;
		this.iwrapperTemplateURL = iwrapperTemplateURL;
		srcVisitor = new SourceVisitor();
		genSrcVisitor = new GeneratedSourceVisitor();
	}
	
	public void incrementalBuild(IResourceDelta delta,IProgressMonitor monitor) throws CoreException {
		IResourceDelta genSrcDelta=delta.findMember(environment.getIWrapperTargetDir());
		if(genSrcDelta!=null) genSrcDelta.accept(genSrcVisitor);
		IResourceDelta srcDelta=delta.findMember(environment.getIWrapperSourceDir());
		if(srcDelta!=null) srcDelta.accept(srcVisitor);
	}
	
	public void undoBuild(IFile file) {
		
		IPath filePath=file.getProjectRelativePath();
		int cnt=filePath.matchingFirstSegments(environment.getIWrapperSourceDir());
		IPath relPath=filePath.removeFirstSegments(cnt);
		relPath=relPath.removeFileExtension();
		relPath=relPath.addFileExtension("java");
		IPath targetPath=environment.getIWrapperTargetDir().append(relPath);
		
		ResourceUtils.deleteProblemMarkers(file);
		
		IFile targetFile=file.getProject().getFile(targetPath);
		if(LOG.isDebugEnabled()) LOG.debug("REMOVE IWRAPPER: "+targetFile.getProjectRelativePath());
		
		try {
			if(targetFile.exists()) targetFile.delete(true,null);
		} catch(CoreException x) {
			LOG.error(x);
		}
	}
	
	public void build(IFile file) {		
		
		IPath filePath=file.getProjectRelativePath();
		
		int cnt=filePath.matchingFirstSegments(environment.getIWrapperSourceDir());
		IPath relPath=filePath.removeFirstSegments(cnt);
		relPath=relPath.removeFileExtension();
		
		ResourceUtils.deleteProblemMarkers(file);
		
		String fullClassName=relPath.toString();
		fullClassName=fullClassName.replace(IPath.SEPARATOR,'.');
		int ind=fullClassName.lastIndexOf('.');
		if(ind==-1) {
			ResourceUtils.addProblemMarker(file,"IWrapper has to be part of a Java package",1);
			return;
		}
		String packageName=fullClassName.substring(0,ind);
		String className=fullClassName.substring(ind+1);
		
		relPath=relPath.addFileExtension("java");
		IPath targetPath=environment.getIWrapperTargetDir().append(relPath);
							
		IFile targetFile=file.getProject().getFile(targetPath);
			
		if(LOG.isDebugEnabled()) LOG.debug("GENERATE IWRAPPER: "+targetFile.getProjectRelativePath());
			
		Templates templates=getIWrapperXSL(file.getProject());
		if(templates!=null) {
			try {
				
				Transformer transformer=templates.newTransformer();
				
				
				transformer.setParameter("classname",className);
				transformer.setParameter("package",packageName);
				StreamSource source=new StreamSource(file.getContents());
				
				ByteArrayOutputStream out=new ByteArrayOutputStream();
				StreamResult result=new StreamResult(out);
				transformer.transform(source,result);
										
				if(targetFile.exists()) {
					targetFile.setContents(new ByteArrayInputStream(out.toByteArray()),true,false,null);
				} else {
					ResourceUtils.createParentFolder(targetFile);
					targetFile.create(new ByteArrayInputStream(out.toByteArray()),false,null);
				}
							
			} catch(CoreException x) {
				LOG.error(x);
			} catch(TransformerConfigurationException x) {
				LOG.error(x);
			} catch(TransformerException x) {
				Throwable cause=getOriginalCause(x);
				int line=1;
				if(cause instanceof SAXParseException) {
					SAXParseException sx=(SAXParseException)cause;
					line=sx.getLineNumber();
				}
				ResourceUtils.addProblemMarker(file,cause.getMessage(),line);
			}
		}
	}
	
	public void rebuild(IFile file) {
		IPath filePath=file.getProjectRelativePath();
		
		int cnt=filePath.matchingFirstSegments(environment.getIWrapperTargetDir());
		IPath relPath=filePath.removeFirstSegments(cnt);
		relPath=relPath.removeFileExtension();
		
		relPath=relPath.addFileExtension("iwrp");
		IPath srcPath=environment.getIWrapperSourceDir().append(relPath);
		
		IFile srcFile=file.getProject().getFile(srcPath);
		if(srcFile.exists()) {
			if(LOG.isDebugEnabled()) LOG.debug("REBUILD IWRAPPER: "+srcPath);
			build(srcFile);
		}
		
	}
	
	
	private Templates getIWrapperXSL(IProject project) {
		if(iwrapperTemplates==null) {
			try {
			    InputStream in = iwrapperTemplateURL.openStream();
	            TransformerFactory tf=TransformerFactory.newInstance();
				StreamSource source=new StreamSource(in);
				iwrapperTemplates=tf.newTemplates(source);
				in.close();
			} catch(Exception x) {
				LOG.error(x);
				ResourceUtils.addProblemMarker(project,x.getMessage(),1);
			}
		}
		return iwrapperTemplates;
	}
	
	
	private Throwable getOriginalCause(Throwable throwable) {
		while(throwable.getCause()!=null) throwable=throwable.getCause();
		try {
			Method method=throwable.getClass().getMethod("getException",new Class[0]);
			Exception embedded=(Exception)method.invoke(throwable,new Object[0]);
			if(embedded!=null) throwable=embedded;
		} catch(Exception ex) {}
		return throwable;
	}
	
	class SourceVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource=delta.getResource();
			if(resource instanceof IFile && resource.getName().endsWith(".iwrp")) {
				IFile file=(IFile)resource;
				int kind=delta.getKind();
				if(kind==IResourceDelta.ADDED) {
					build(file);
				} else if(kind==IResourceDelta.REMOVED) {
					undoBuild(file);
					//TODO: remove empty directories
				} else if(kind==IResourceDelta.CHANGED) {
					build(file);
				}
			}
			return true;
		}
	}
	
	class GeneratedSourceVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource=delta.getResource();
			if(resource instanceof IFile && resource.getName().endsWith(".java")) {
				IFile file=(IFile)resource;
				int kind=delta.getKind();
				if(kind==IResourceDelta.REMOVED) {
					rebuild(file);
				}
			}
			return true;
		}
	}
	
}
