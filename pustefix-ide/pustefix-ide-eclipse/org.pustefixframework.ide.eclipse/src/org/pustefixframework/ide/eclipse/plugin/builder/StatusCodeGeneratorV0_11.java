package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;

public class StatusCodeGeneratorV0_11 implements StatusCodeGenerator {
	
	private static Logger LOG=Activator.getLogger();

	private Set<IFile> scodeFiles;
	
	public void incrementalBuild(Environment environment, IResourceDelta delta,IProgressMonitor monitor) throws CoreException {
		IProject project=delta.getResource().getProject();
		IResourceDelta prjDelta=delta.findMember(environment.getStatusCodeSourceDirForWebapp());
		boolean mustBuild=false;
		if(prjDelta!=null) {
			//IPath factoryPath=environment.getCommonConfigPath().append("factory.xml");
		    //TODO:
		    IPath factoryPath=null;
		    IResourceDelta factoryDelta=delta.findMember(factoryPath);
			if(factoryDelta!=null) {
				int kind=factoryDelta.getKind();
				if(kind==IResourceDelta.ADDED || kind==IResourceDelta.CHANGED) {
					IFile factoryFile=(IFile)factoryDelta.getResource();
					Set<IFile> files=readFactoryConfig(factoryFile);
					if(scodeFiles==null || !scodeFiles.equals(files)) mustBuild=true;
					scodeFiles=files;
				}
			}
			if(!mustBuild) {
				if(scodeFiles==null) {
					IFile configFile=project.getFile(factoryPath);
					if(configFile.exists()) scodeFiles=readFactoryConfig(configFile);
					else scodeFiles=new HashSet<IFile>();
				}
				for(IFile file:scodeFiles) {
					IResourceDelta fileDelta=delta.findMember(file.getProjectRelativePath());
					if(fileDelta!=null) {
						int kind=fileDelta.getKind();
						if(kind==IResourceDelta.ADDED || delta.getKind()==IResourceDelta.CHANGED
								|| kind==IResourceDelta.REMOVED) {
							mustBuild=true;
							break;
						}
					}
				}
			}
		}
		if(!mustBuild) {
			IPath targetPath=environment.getStatusCodeTargetDir().append("/de/schlund/util/statuscodes/StatusCodeLib.java");
			IResourceDelta targetDelta=delta.findMember(targetPath);
			if(targetDelta!=null && targetDelta.getKind()==IResourceDelta.REMOVED) mustBuild=true;
		}
		if(mustBuild) build(environment, project);
	}
		
	private boolean build(Environment environment, IProject project) {
		boolean old=isOldPustefix(project);
		if(LOG.isDebugEnabled()) LOG.debug("GENERATE SCODES");
		IPath targetPath=environment.getStatusCodeTargetDir().append("/de/schlund/util/statuscodes/StatusCodeLib.java");
		IFile targetFile=project.getFile(targetPath);
		generate(scodeFiles,targetFile,old);
		return true;
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
	
	/**
	private boolean isStatusCodeFile(IFile file) {
		if(scodeFiles==null) {
			IPath factoryPath=environment.getCommonConfigPath().append("factory.xml");
			IFile configFile=file.getProject().getFile(factoryPath);
			if(configFile.exists()) scodeFiles=readFactoryConfig(configFile);
			else scodeFiles=new HashSet<IFile>();
		}
		return scodeFiles.contains(file);
	}
	*/
	
	private Set<IFile> readFactoryConfig(IFile configFile) {
		Set<IFile> files=new HashSet<IFile>();
		try {
			InputStream in=configFile.getContents();
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc=db.parse(in);
			NodeList nl=doc.getElementsByTagName("prop");
			for(int i=0;i<nl.getLength();i++) {
				Element elem=(Element)nl.item(i);
				String name=elem.getAttribute("name");
				if(name!=null && name.startsWith("partindex.scodefile")) {
					String relPath=elem.getTextContent();
					if(relPath!=null) {
						relPath=relPath.trim();
						if(!relPath.equals("")) {
							IFile scodeFile=configFile.getProject().getFile("/projects/"+relPath);
							files.add(scodeFile);
						}
					}
				}
			}
		} catch(CoreException x) {
			LOG.error(x);
		} catch(Exception x) {
			LOG.error(x);
		}
		return files;
	}
	
	
	private boolean isOldPustefix(IProject project) {
		IJavaProject javaProject=JavaCore.create(project);
		List<URL> urlList=new ArrayList<URL>();
		try {
			IClasspathEntry[] entries=javaProject.getRawClasspath();
			for(IClasspathEntry entry:entries) {
				if(entry.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
					URI uri=project.getWorkspace().getRoot().getFile(entry.getPath()).getLocationURI();
					try {
						urlList.add(uri.toURL());
					} catch(MalformedURLException x) {
						LOG.error(x);
					}
				}
			}
		} catch(JavaModelException x) {
			LOG.error(x);
		}
		URI uri=project.getFolder("build").getLocationURI();
		uri=URI.create(uri.toString()+"/");
		try {
			urlList.add(uri.toURL());
			URL[] urls=new URL[urlList.size()];
			urlList.toArray(urls);
			URLClassLoader cl=new URLClassLoader(urls);
			Class.forName("de.schlund.pfixxml.resources.ResourceUtil",true,cl);
			return false;
		} catch(ClassNotFoundException x) {
		} catch(MalformedURLException x) {
			LOG.error(x);
		} 
		return true;
	}
	
	private void generate(Set<IFile> files,IFile targetFile,boolean old) {
		
		IFile lastFile=null;
		try {
			
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out, "ascii");
		createHeader(writer,old);
		
		
		for (IFile file: files) {
			lastFile=file;
			InputStream in=file.getContents();
			IPath relPath=file.getProjectRelativePath();
			relPath=relPath.removeFirstSegments(1);
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc   = db.parse(in);
			NodeList list  = doc.getElementsByTagName("part");
			for (int i = 0; i < list.getLength() ; i++) {
				Element node      = (Element) list.item(i);
				String  name      = node.getAttribute("name");
				String  classname = convertToFieldName(name);
				writer.write("  public static final StatusCode " + classname);
				if(old) writer.write(" = new StatusCode(\"" + name + "\", PathFactory.getInstance().createPath(\"" + relPath + "\"));\n");
				else writer.write(" = new StatusCode(\"" + name + "\", ResourceUtil.getFileResourceFromDocroot(\"" + relPath + "\"));\n");
			}
			ResourceUtils.deleteProblemMarkers(file);
		}
          
			writer.write("}\n");
			writer.flush();
			writer.close();
          
			if(targetFile.exists()) {
				targetFile.setContents(new ByteArrayInputStream(out.toByteArray()),true,false,null);
			} else {
				ResourceUtils.createParentFolder(targetFile);
				targetFile.create(new ByteArrayInputStream(out.toByteArray()),false,null);
			}
			
		} catch(Exception x) {
			Throwable cause=getOriginalCause(x);
			int line=1;
			if(cause instanceof SAXParseException) {
				SAXParseException sx=(SAXParseException)cause;
				line=sx.getLineNumber();
			}
			if(lastFile!=null) ResourceUtils.addProblemMarker(lastFile,cause.getMessage(),line);
		}
	}
	
	private static String convertToFieldName(String part) {
		return part.replace('.', '_').replace(':', '_').toUpperCase();
	}
	
    private void createHeader(Writer writer,boolean old) throws IOException {
        writer.write("/*\n");
        writer.write(" * This file is AUTOGENERATED. Do not change by hand.\n");
        writer.write(" */\n");
        writer.write("\n");
        writer.write("\n");
        writer.write("package de.schlund.util.statuscodes;\n");
        if(old) writer.write("import de.schlund.pfixxml.PathFactory;\n");
        else writer.write("import de.schlund.pfixxml.resources.ResourceUtil;\n");
        writer.write("import java.lang.reflect.Field;\n");
        writer.write("\n");
        writer.write("public class StatusCodeLib {\n");
        writer.write("    public static StatusCode getStatusCodeByName(String name) throws StatusCodeException {\n");
        writer.write("        return getStatusCodeByName(name, false);\n");
        writer.write("    }\n");        
        writer.write("\n");        
        writer.write("    public static StatusCode getStatusCodeByName(String name, boolean optional) throws StatusCodeException {\n");
        writer.write("        String     fieldname = StatusCode.convertToFieldName(name);\n");
        writer.write("        StatusCode scode     = null;\n");
        writer.write("        try {\n");
        writer.write("            Field field = StatusCodeLib.class.getField(fieldname);\n");
        writer.write("            scode = (StatusCode) field.get(null);\n");
        writer.write("        } catch (NoSuchFieldException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (SecurityException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (IllegalAccessException e) {\n");
        writer.write("            //\n");
        writer.write("        }\n");
        writer.write("        if (scode == null && optional == false) {\n");
        writer.write("            throw new StatusCodeException(\"StatusCode \" + name + \" is not defined.\");\n");
        writer.write("        }\n");
        writer.write("        return scode;\n");
        writer.write("    }\n");
    }
	
}
