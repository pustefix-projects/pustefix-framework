package de.schlund.pfixcore.util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import de.schlund.pfixcore.util.GenerateSCodes.Result;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

public class GenerateSCodesTask extends MatchingTask {
    
    private String className;
    private File genDir;
    private File docrootDir;
    private File sourceFile;
    private String targetPath;
    private String property;
    private String module;
    
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
    
    public void setModule(String module) {
        this.module = module;
    }
    
    @Override
    public void execute() throws BuildException {
        
        //if(className == null) throw new BuildException("Mandatory 'class' attribute is missing.");
        if(genDir == null) throw new BuildException("Mandatory 'gendir' attribute is missing.");
        
        if(docrootDir == null) throw new BuildException("Mandatory 'docroot' attribute is missing.");
        if(GlobalConfig.getDocroot()==null) GlobalConfigurator.setDocroot(docrootDir.getAbsolutePath());
        
        DirectoryScanner scanner = getDirectoryScanner(docrootDir);
        String[] files = scanner.getIncludedFiles();
        try {
            Result result = GenerateSCodes.generateFromInfo(Arrays.asList(files), docrootDir.getAbsolutePath(), genDir, module);
            if(result.generatedClasses.size()>0) {
                log("Generated "+result.generatedClasses.size()+" statuscode class"+
                        (result.generatedClasses.size()>1?"es":""), Project.MSG_INFO);
            }
            if(property!=null) {
                StringBuilder sb=new StringBuilder();
                Iterator<String> it=result.allClasses.iterator();
                while(it.hasNext()) {
                    String filePath = it.next();
                    filePath = filePath.replace('.','/')+".java";
                    sb.append(filePath);
                    if(it.hasNext()) sb.append(" ");
                }
                String str = sb.toString().trim();
                if(str.length()>0) getProject().setProperty(property, str);
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
