package de.schlund.pfixcore.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class GenerateSCodesTask extends MatchingTask {
    
    private String className;
    private File genDir;
    private File docrootDir;
    private File sourceFile;
    private String targetPath;
    private String property;
    
    public void setClass(String className) {
        this.className = className;
    }
    
    public void setGenDir(File genDir) {
        this.genDir = genDir;
    }
    
    public void setDocroot(File docrootDir) {
        this.docrootDir = docrootDir;
    }
    
    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
    
    public void setProperty(String property) {
        this.property = property;
    }
    
    @Override
    public void execute() throws BuildException {
        
        //if(className == null) throw new BuildException("Mandatory 'class' attribute is missing.");
        if(genDir == null) throw new BuildException("Mandatory 'gendir' attribute is missing.");
        
        if(docrootDir == null) throw new BuildException("Mandatory 'docroot' attribute is missing.");
        GlobalConfigurator.setDocroot(docrootDir.getAbsolutePath());
        
        DirectoryScanner scanner = getDirectoryScanner(docrootDir);
        String[] files = scanner.getIncludedFiles();
        List<DocrootResource> resList = new ArrayList<DocrootResource>();
        for (String file : files) {
            DocrootResource res = ResourceUtil.getFileResourceFromDocroot(file);
            resList.add(res);
        }
        try {
            List<String> genClasses = GenerateSCodes.generateFromInfo(resList, docrootDir.getAbsolutePath(), genDir);
            if(genClasses.size()>0) {
                log("Generated "+genClasses.size()+" statuscode class"+(genClasses.size()>1?"es":""), Project.MSG_INFO);
                if(property!=null) {
                    StringBuilder sb=new StringBuilder();
                    Iterator<String> it=genClasses.iterator();
                    while(it.hasNext()) {
                        String filePath = it.next();
                        filePath = filePath.replace('.','/')+".java";
                        sb.append(filePath);
                        if(it.hasNext()) sb.append(" ");
                    }
                    getProject().setProperty(property, sb.toString());
                }
            }
            
        } catch(Exception x) {
            throw new BuildException(x);
        }
        try {
           if(sourceFile != null) {
                if(targetPath == null) throw new BuildException("Attribute 'targetpath' is mandatory when used with 'sourcepath'.");
                GenerateSCodes.generate(sourceFile, genDir, className, targetPath);
            }
        } catch(Exception x) {
            throw new BuildException("Generating StatusCodeLib failed due to: "+x.getMessage(),x);
        }
    }
    
    

}
