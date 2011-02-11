package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;


public class StatusCodeGeneratorV0_13 implements StatusCodeGenerator {
	
	private static Logger LOG=Activator.getLogger();

	private Map<IFile,StatusCodeInfo> statusCodeInfos;
	
	public void incrementalBuild(Environment environment, IResourceDelta delta,IProgressMonitor monitor) throws CoreException {
		IProject project=delta.getResource().getProject();
		IResourceDelta prjDelta=delta.findMember(environment.getStatusCodeSourceDirForWebapp());
	
		Set<StatusCodeClass> scheduledClasses = new HashSet<StatusCodeClass>();
		
		if(prjDelta!=null) {
			boolean dyntxtChanged = false;
			IResourceDelta[] appDirDeltas = prjDelta.getAffectedChildren();
			for(IResourceDelta appDirDelta:appDirDeltas) {
				IResource appDirRes = appDirDelta.getResource();
				if(appDirRes.getType() == IResource.FOLDER) {
					IPath dynPath = new Path("dyntxt");
					IPath[] infoPaths = {new Path("conf/statuscodeinfo.xml"), new Path("dyntxt/statuscodeinfo.xml")};
					for(IPath infoPath:infoPaths) {
						IResourceDelta infoDelta = appDirDelta.findMember(infoPath);
						if(infoDelta!=null) {
							IResource infoRes = infoDelta.getResource();
							if(infoDelta.getKind()==IResourceDelta.REMOVED) {
								IFile file =(IFile)infoRes;
								if(statusCodeInfos==null) statusCodeInfos=readStatusCodeInfos(project);
								StatusCodeInfo statusCodeInfo = statusCodeInfos.get(file);
								if(statusCodeInfo!=null) {
									for(StatusCodeClass scClass:statusCodeInfo.getStatusCodeClasses()) {
										String className = scClass.getClassName();
										String relPath = className.replace('.',IPath.SEPARATOR)+".java";
										IPath targetPath=environment.getStatusCodeTargetDir().append(relPath);
										IFile scFile = project.getFile(targetPath);
										if(scFile.exists()) scFile.delete(true,null);
									}
									statusCodeInfos.remove(file);
								}
							} else if(infoDelta.getKind()==IResourceDelta.ADDED) {
	
								IFile file =(IFile)infoRes;
								if(statusCodeInfos==null) statusCodeInfos=readStatusCodeInfos(project);
								StatusCodeInfo scInfo = readStatusCodeInfo(file);
								statusCodeInfos.put(scInfo.getInfoFile(),scInfo);
								for(StatusCodeClass scClass:scInfo.getStatusCodeClasses()) {
									scheduledClasses.add(scClass);
								}
							} else if(infoDelta.getKind()==IResourceDelta.CHANGED) {
							
								IFile file =(IFile)infoRes;
								if(statusCodeInfos==null) statusCodeInfos=readStatusCodeInfos(project);
								StatusCodeInfo oldInfo = statusCodeInfos.get(file);
								StatusCodeInfo scInfo = readStatusCodeInfo(file);
								statusCodeInfos.put(scInfo.getInfoFile(),scInfo);
								if(oldInfo!=null) {
									for(StatusCodeClass scClass:scInfo.getStatusCodeClasses()) {
										if(!oldInfo.contains(scClass)) scheduledClasses.add(scClass);
									}
									for(StatusCodeClass scClass:oldInfo.getStatusCodeClasses()) {
										if(!scInfo.containsClassName(scClass.getClassName())) {
											String className = scClass.getClassName();
											String relPath = className.replace('.',IPath.SEPARATOR)+".java";
											IPath targetPath=environment.getStatusCodeTargetDir().append(relPath);
											IFile scFile = project.getFile(targetPath);
											
											if(scFile.exists()) scFile.delete(true,null);
										
										}
									}
								} else {
									for(StatusCodeClass scClass:scInfo.getStatusCodeClasses()) {
										scheduledClasses.add(scClass);
									}
								}
								
							}
						}
					}
					if(!dyntxtChanged) {
						IResourceDelta dyntxtDelta = appDirDelta.findMember(dynPath);
						if(dyntxtDelta!=null) dyntxtChanged=true;
					}
				}
			}
			if(dyntxtChanged) {		
				if(statusCodeInfos==null) statusCodeInfos = readStatusCodeInfos(project);
				for(StatusCodeInfo statusCodeInfo:statusCodeInfos.values()) {
					for(StatusCodeClass statusCodeClass:statusCodeInfo.getStatusCodeClasses()) {
						for(IFile messageFile:statusCodeClass.getMessageFiles()) {
							IResourceDelta fileDelta=delta.findMember(messageFile.getProjectRelativePath());
							if(fileDelta!=null) {
								int kind=fileDelta.getKind();
								if(kind==IResourceDelta.ADDED || delta.getKind()==IResourceDelta.CHANGED
										|| kind==IResourceDelta.REMOVED) {
									scheduledClasses.add(statusCodeClass);
								}
							}
						}
					}
				}
			}
			
			//TODO: check if generated StatusCode classes were removed and schedule them for rebuild
			
			for(StatusCodeClass scClass:scheduledClasses) {
				
				build(environment,project,scClass);
			}
				
		}
	}
	
	
	private boolean build(Environment environment, IProject project, StatusCodeClass statusCodeClass) {
		if(LOG.isDebugEnabled()) LOG.debug("GENERATE SCODES");
		generate(environment, project, statusCodeClass, null);
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
	
	
    public void generate(Environment environment, IProject project, StatusCodeClass statusCodeClass, String module) {
       
    	IFile lastFile=null;
    	try {
    		
    	ByteArrayOutputStream out=new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out, "ascii");

		String className = statusCodeClass.getClassName();
        createHeader(writer, className);
        List<String> docRelPaths = new ArrayList<String>();
        for (IFile input:statusCodeClass.getMessageFiles()) {
            String path = getModulePath(input.getProjectRelativePath().removeFirstSegments(1).toString(),module);
            docRelPaths.add(path);
        }
        createResources(writer, docRelPaths);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (IFile input: statusCodeClass.getMessageFiles()) {
        	lastFile = input;
        	DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(input.getContents());
            createStatusCodes(writer, doc, docRelPaths.indexOf(getModulePath(input.getProjectRelativePath().removeFirstSegments(1).toString(),module)));
            ResourceUtils.deleteProblemMarkers(input);
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
        
        String pathStr = className.replace('.','/')+".java";
        IPath targetPath=environment.getStatusCodeTargetDir().append(pathStr);
		IFile targetFile=project.getFile(targetPath);
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
	
    private String getModulePath(String relPath, String module) {
        if(module==null) return relPath;
        int ind = relPath.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Illegal file name: "+relPath);
        relPath = relPath.substring(0,ind)+"-merged"+relPath.substring(ind);
        String modulePath="";
        if(module.equals("pfixcore")) {
            if(!relPath.startsWith("core")) throw new RuntimeException("Illegal core file name: "+relPath);
            modulePath = "core-override/"+relPath.substring(5);
        } else {
            modulePath = "modules-override/"+module+"/"+relPath;
        }
        return modulePath;
    }
	
	private String convertToFieldName(String part) {
		return part.replace('.', '_').replace(':', '_').toUpperCase();
	}
	
    private void createResources(Writer writer, List<String> docRelPaths) throws IOException {
        writer.write("    public static final DocrootResource[] __RES = {\n");
        Iterator<String> it = docRelPaths.iterator();
        while(it.hasNext()) {
            writer.write("        ResourceUtil.getFileResourceFromDocroot(\""+it.next()+"\")");
            if(it.hasNext()) writer.write(",");
            writer.write("\n");
        }
        writer.write("    };\n\n");
    }
    
    private void createStatusCodes(Writer writer, Document doc, int resIndex) throws IOException {
        NodeList list  = doc.getElementsByTagName("part");
        for (int i = 0; i < list.getLength() ; i++) {
            Element node      = (Element) list.item(i);
            String  name      = node.getAttribute("name");
            String  classname = convertToFieldName(name);
            writer.write("    public static final StatusCode " + classname +
                    " = new StatusCode(\"" + name + "\", __RES["+resIndex+"]);\n");
        }
    }
	
    private void createHeader(Writer writer, String className) throws IOException {
        int ind = className.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Class name must contain package: "+className);
        String pkgName = className.substring(0,ind);
        String simpleName = className.substring(ind+1);
        writer.write("/*\n");
        writer.write(" * This file is AUTOGENERATED. Do not change by hand.\n");
        writer.write(" */\n");
        writer.write("\n");
        writer.write("\n");
        writer.write("package "+pkgName+";\n\n");
        writer.write("import de.schlund.pfixxml.resources.DocrootResource;\n");
        writer.write("import de.schlund.pfixxml.resources.ResourceUtil;\n");
        if(!pkgName.equals("de.schlund.util.statuscodes")) {
            writer.write("import de.schlund.util.statuscodes.StatusCode;\n");
            writer.write("import de.schlund.util.statuscodes.StatusCodeException;\n");
        }
        writer.write("import java.lang.reflect.Field;\n");
        writer.write("\n");
        writer.write("public class "+simpleName+" {\n\n");
        writer.write("    public static StatusCode getStatusCodeByName(String name) throws StatusCodeException {\n");
        writer.write("        return getStatusCodeByName(name, false);\n");
        writer.write("    }\n");        
        writer.write("\n");        
        writer.write("    public static StatusCode getStatusCodeByName(String name, boolean optional) throws StatusCodeException {\n");
        writer.write("        String     fieldname = StatusCode.convertToFieldName(name);\n");
        writer.write("        StatusCode scode     = null;\n");
        writer.write("        try {\n");
        writer.write("            Field field = "+simpleName+".class.getField(fieldname);\n");
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
        writer.write("    }\n\n");
    }

	
   
    
	private Map<IFile,StatusCodeInfo> readStatusCodeInfos(IProject project) {
		Map<IFile,StatusCodeInfo> infoMap = new HashMap<IFile,StatusCodeInfo>();
		IFolder projectsDir = project.getFolder("projects");
		try {
			IResource[] appDirs = projectsDir.members();
			for(IResource appDir:appDirs) {
				if(appDir.getType()==IResource.FOLDER) {
					IPath infoPath=new Path("conf/statuscodeinfo.xml");
					IResource infoRes = ((IFolder)appDir).findMember(infoPath);
					if(infoRes!=null && infoRes.getType()==IResource.FILE) {
						StatusCodeInfo info = readStatusCodeInfo((IFile)infoRes);
						if(info!=null) infoMap.put((IFile)infoRes,info);
					}
					infoPath=new Path("dyntxt/statuscodeinfo.xml");
					infoRes = ((IFolder)appDir).findMember(infoPath);
					if(infoRes!=null && infoRes.getType()==IResource.FILE) {
						StatusCodeInfo info = readStatusCodeInfo((IFile)infoRes);
						if(info!=null) infoMap.put((IFile)infoRes,info);
					}
				}
			}
		} catch(CoreException x) {
			LOG.error(x);
		}
		return infoMap;
	}
    
    private StatusCodeInfo readStatusCodeInfo(IFile infoFile) {
    	StatusCodeInfo scInfo = null;
    	try {
    		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream in = infoFile.getContents();
			Document doc = db.parse(in);
			scInfo = new StatusCodeInfo(infoFile);
			NodeList scNodes = doc.getElementsByTagName("statuscodes");
			if(scNodes!=null) {
				for(int i=0;i<scNodes.getLength();i++) {
					Element elem = (Element)scNodes.item(i);
					String className = elem.getAttribute("class");
					StatusCodeClass scClass = new StatusCodeClass(className);
					NodeList fileNodes = elem.getElementsByTagName("file");
					for(int j=0;j<fileNodes.getLength();j++) {
						Element fileElem = (Element)fileNodes.item(j);
						String relPath=fileElem.getTextContent();
						if(relPath!=null) {
							relPath=relPath.trim();
							if(!relPath.equals("")) {
								IFile messageFile=infoFile.getProject().getFile("/projects/"+relPath);
								if(!messageFile.exists()) {
									IPath path = infoFile.getProjectRelativePath().removeLastSegments(1).append(relPath);
									messageFile = infoFile.getProject().getFile(path);
									if(!messageFile.exists()) messageFile = null;
								}
								if(messageFile!=null) scClass.addMessageFile(messageFile);
							}
						}
					}
					scInfo.add(scClass);
				}
			}
			return scInfo;
		} catch(Exception x) {
			LOG.error("Error while reading statuscodeinfo.xml",x);
		}
    	return scInfo;
    }
    
    
    class StatusCodeInfo {
    	
    	private IFile infoFile;
    	private List<StatusCodeClass> statusCodeClasses;
    	
    	public StatusCodeInfo(IFile infoFile) {
    		this.infoFile = infoFile;
    		statusCodeClasses = new ArrayList<StatusCodeClass>();
    	}
    	
    	public IFile getInfoFile() {
    		return infoFile;
    	}
    	
    	public void add(StatusCodeClass statusCodeClass) {
    		statusCodeClasses.add(statusCodeClass);
    	}
    	
    	public List<StatusCodeClass> getStatusCodeClasses() {
    		return statusCodeClasses;
    	}
    	
    	public boolean contains(StatusCodeClass statusCodeClass) {
    		return statusCodeClasses.contains(statusCodeClass);
    	}
    	
    	public boolean containsClassName(String className) {
    		for(StatusCodeClass statusCodeClass:statusCodeClasses) {
    			if(statusCodeClass.getClassName().equals(className)) return true;
    		}
    		return false;
    	}
    	
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		for(StatusCodeClass statusCodeClass:statusCodeClasses) {
    			sb.append(statusCodeClass);
    			sb.append("\n");
    		}
    		if(sb.charAt(sb.length()-1)=='\n') sb.deleteCharAt(sb.length()-1);
    		return sb.toString();
    	}
    }
    
    class StatusCodeClass {
    	
    	private String className;
    	private List<IFile> messageFiles;
    	
    	public StatusCodeClass(String className) {
    		this.className = className;
    		messageFiles = new ArrayList<IFile>();
    	}
    	
    	public String getClassName() {
    		return className;
    	}
    	
    	public void addMessageFile(IFile messageFile) {
    		messageFiles.add(messageFile);
    	}
    	
    	public List<IFile> getMessageFiles() {
    		return messageFiles;
    	}
    	
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		sb.append(className);
    		sb.append("[");
    		for(IFile messageFile:messageFiles) {
    			sb.append(messageFile.getProjectRelativePath());
    			sb.append(",");
    		}
    		if(sb.charAt(sb.length()-1)==',') sb.deleteCharAt(sb.length()-1);
    		sb.append("]");
    		return sb.toString();
    	}
    	
    	@Override
    	public int hashCode() {
    		return className.hashCode();
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		if(obj instanceof StatusCodeClass) {
    			StatusCodeClass sc = (StatusCodeClass)obj;
    			if(className.equals(sc.getClassName()) && messageFiles.equals(sc.getMessageFiles())) return true;
    		}
    		return false;
    	}
    	
    }
    
}
