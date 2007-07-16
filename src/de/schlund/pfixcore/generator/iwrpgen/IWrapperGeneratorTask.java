package de.schlund.pfixcore.generator.iwrpgen;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class IWrapperGeneratorTask extends Task {

    File genSrcDir;
    String beans;
    
    @Override
    public void execute() throws BuildException {
        String[] beanList=beans.split("\\s+");
        for(String bean:beanList) {
            Class<?> clazz=null;
            try {
                clazz=Class.forName(bean);
            } catch(Exception x) {
                throw new BuildException("Can't get class: "+bean,x);
            }
            IWrapperRuntimeGenerator.generate(clazz, genSrcDir);
            System.out.println(bean);
        }
    }
    
    public void setBeans(String beans) {
        this.beans=beans;
    }
    
    public void setGenSrcDir(File genSrcDir) {
        this.genSrcDir=genSrcDir;
    }
}
