package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Environment;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

public class StatusCodeBuilderDelegate implements BuilderDelegate {

    private static Logger LOG=Activator.getLogger();
    
    private final IProject project;
    private final Environment environment;
    private final StatusCodeGenerator scodeGenerator;
    
    private StatusCodeInfo statusCodeInfo;
    
    public StatusCodeBuilderDelegate(IProject project, Environment environment, StatusCodeGenerator scodeGenerator) {
        this.project = project;
        this.environment = environment;
        this.scodeGenerator = scodeGenerator;
    }
    
    public void clean(IProgressMonitor monitor) throws CoreException {
        IPath targetPath = environment.getStatusCodeTargetDir();
        IResource resource = project.findMember(targetPath);
        if(resource.exists() && resource.getType() == IResource.FOLDER) {
            ResourceUtils.removeDerivedResources(resource, monitor);
        }
        IPath srcPath = environment.getStatusCodeSourceDirForWebapp();
        resource = project.findMember(srcPath);
        if(resource.exists() && resource.getType() == IResource.FOLDER) {
            ResourceUtils.deleteProblemMarkers(resource, true);
        }
    }
    
    public void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        
        IProject project = delta.getResource().getProject();
        IResourceDelta prjDelta = delta.findMember(environment.getStatusCodeSourceDirForWebapp());
        
        if(prjDelta!=null) {
            
            System.out.println("dyntxt changed");
            Set<StatusCodeClass> scheduledClasses = new HashSet<StatusCodeClass>();
            //IPath path = environment.getStatusCodeSourceDirForWebapp().append("statuscodeinfo.xml");
            //statusCodeInfo = readStatusCodeInfo(project.getFile(path));
            
            IResourceDelta infoDelta = prjDelta.findMember(new Path("statuscodeinfo.xml"));
            if(infoDelta!=null) {
                System.out.println("statuscodeinfo changed");
                IResource infoRes = infoDelta.getResource();
                if(infoDelta.getKind()==IResourceDelta.REMOVED) {
                    if(statusCodeInfo != null) {
                        for(StatusCodeClass scClass:statusCodeInfo.getStatusCodeClasses()) {
                            String className = scClass.getClassName();
                            String relPath = className.replace('.',IPath.SEPARATOR)+".java";
                            IPath targetPath=environment.getStatusCodeTargetDir().append(relPath);
                            IFile scFile = project.getFile(targetPath);
                            if(scFile.exists()) scFile.delete(true,null);
                        }
                        statusCodeInfo = null;
                    }
                } else if(infoDelta.getKind()==IResourceDelta.ADDED) {
                    IFile file =(IFile)infoRes;
                    statusCodeInfo = readStatusCodeInfo(file);
                    for(StatusCodeClass scClass:statusCodeInfo.getStatusCodeClasses()) {
                        scheduledClasses.add(scClass);
                    }
                } else if(infoDelta.getKind()==IResourceDelta.CHANGED) {
                    IFile file =(IFile)infoRes;
                    StatusCodeInfo oldInfo = statusCodeInfo;
                    statusCodeInfo = readStatusCodeInfo(file);
                    if(oldInfo!=null) {
                        for(StatusCodeClass scClass:statusCodeInfo.getStatusCodeClasses()) {
                            if(!oldInfo.contains(scClass)) scheduledClasses.add(scClass);
                        }
                        for(StatusCodeClass scClass:oldInfo.getStatusCodeClasses()) {
                            if(!statusCodeInfo.containsClassName(scClass.getClassName())) {
                                String className = scClass.getClassName();
                                String relPath = className.replace('.',IPath.SEPARATOR)+".java";
                                IPath targetPath=environment.getStatusCodeTargetDir().append(relPath);
                                IFile scFile = project.getFile(targetPath);
                                if(scFile.exists()) scFile.delete(true,null);
                            }
                        }
                    } else {
                        for(StatusCodeClass scClass:statusCodeInfo.getStatusCodeClasses()) {
                            scheduledClasses.add(scClass);
                        }
                    }
                }
            }
            if(statusCodeInfo != null) {
                Iterator<StatusCodeClass> it = statusCodeInfo.getStatusCodeClasses().iterator();
                while(it.hasNext()) {
                    StatusCodeClass statusCodeClass = it.next();
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
            
            //TODO: check if generated StatusCode classes were removed and schedule them for rebuild
            
            for(StatusCodeClass scClass:scheduledClasses) {
                
                build(environment,project,scClass,monitor);
            }
                
        }
    }
    
    public void fullBuild(IProgressMonitor monitor) throws CoreException {
        IPath path = environment.getStatusCodeSourceDirForWebapp().append("statuscodeinfo.xml");
        statusCodeInfo = readStatusCodeInfo(project.getFile(path));
        for(StatusCodeClass scClass : statusCodeInfo.getStatusCodeClasses()) {
            build(environment,project,scClass,monitor);
        }
    }
    
    
    private boolean build(Environment environment, IProject project, StatusCodeClass statusCodeClass, IProgressMonitor monitor) {
        if(LOG.isDebugEnabled()) LOG.debug("GENERATE SCODES");
        System.out.println("BUILD: "+statusCodeClass.getClassName());
        generate(project, environment.getStatusCodeTargetDir(), null, statusCodeClass.getClassName(), statusCodeClass.getMessageFiles(), monitor);
        return true;
    }
    
    public void generate(IProject project, IPath targetDir, String module, String className, List<IFile> messageFiles, IProgressMonitor monitor) {
        
        IFile lastFile=null;
        try {
            
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, "ascii");

        scodeGenerator.createHeader(writer, className);
        List<String> docRelPaths = new ArrayList<String>();
        for (IFile input : messageFiles) {
            String path = scodeGenerator.getModulePath(input.getProjectRelativePath().removeFirstSegments(1).toString(),module);
            docRelPaths.add(path);
        }
        scodeGenerator.createResources(writer, docRelPaths);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (IFile input: messageFiles) {
            lastFile = input;
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(input.getContents());
            scodeGenerator.createStatusCodes(writer, doc, docRelPaths.indexOf(scodeGenerator.getModulePath(input.getProjectRelativePath().removeFirstSegments(1).toString(),module)));
            ResourceUtils.deleteProblemMarkers(input, false);
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
        
        String pathStr = className.replace('.','/')+".java";
        IPath targetPath=targetDir.append(pathStr);
        IFile targetFile=project.getFile(targetPath);
        if(targetFile.exists()) {
            targetFile.setContents(new ByteArrayInputStream(out.toByteArray()),true,false,null);
        } else {
            ResourceUtils.createParentFolder(targetFile, null);
            targetFile.create(new ByteArrayInputStream(out.toByteArray()),false,null);
        }
        targetFile.setDerived(true, monitor);
        
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
    
    private Throwable getOriginalCause(Throwable throwable) {
        while(throwable.getCause()!=null) throwable=throwable.getCause();
        try {
            Method method=throwable.getClass().getMethod("getException",new Class[0]);
            Exception embedded=(Exception)method.invoke(throwable,new Object[0]);
            if(embedded!=null) throwable=embedded;
        } catch(Exception ex) {}
        return throwable;
    }
    
    private StatusCodeInfo readStatusCodeInfo(IFile infoFile) {
        StatusCodeInfo scInfo = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream in = infoFile.getContents();
            Document doc = db.parse(in);
            scInfo = new StatusCodeInfo();
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
        
        private List<StatusCodeClass> statusCodeClasses;
        
        public StatusCodeInfo() {
            statusCodeClasses = new ArrayList<StatusCodeClass>();
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
