/*
 * de.schlund.pfixcore.webservice.Java2WSDLTask
 */
package de.schlund.pfixcore.webservice;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Java2WSDLTask.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class Java2WSDLTask extends Task {
   
    private String msg;

    private File prjdir;
    private File prjfile;
    private File srcdir;
    
    // The method executing the task
    public void execute() throws BuildException {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.parse(prjfile);
            NodeList nl=doc.getElementsByTagName("project");
            for(int i=0;i<nl.getLength();i++) {
                Element elem=(Element)nl.item(i);
                String name=elem.getAttribute("name");
                File file=new File(prjdir,name+File.separator+"conf"+File.separator+"webservice.prop");
                if(file.exists()) {
                    System.out.println("***"+file.getAbsolutePath());
                    Properties props=new Properties();
                    props.load(new FileInputStream(file));
                    Iterator it=getPropertyKeys(props,"webservice\\.[^\\.]*\\.name");
                    while(it.hasNext()) {
                        String key=(String)it.next();
                        String wsName=props.getProperty(key);
                        String wsItf=props.getProperty("webservice."+wsName+".interface");
                        if(wsItf==null || wsItf.trim().equals("")) throw new BuildException(file.getAbsolutePath()+": Missing interface definition for web service '"+wsName+"'.");
                        String wsItfPath=wsItf.replace('.',File.separatorChar)+".java";
                        File wsItfFile=new File(srcdir,wsItfPath);
                        if(!wsItfFile.exists()) throw new BuildException(file.getAbsolutePath()+": Web service interface source '"+wsItfFile.getAbsolutePath()+"' doesn't exist.");
                        System.out.println(wsItfFile.getAbsolutePath());
                        
                    }
                }
                
            }
               
            
        } catch(Exception x) {
            throw new BuildException(x);
        }

        System.out.println(msg);
        }
    

        // The setter for the "message" attribute
        public void setMessage(String msg) {
            this.msg = msg;
        }
        
        public void setPrjdir(File prjdir) {
            this.prjdir=prjdir;
            System.out.println(prjdir.getAbsolutePath());
        }
        
        public void setPrjfile(File prjfile) {
            this.prjfile=prjfile;
            System.out.println(prjfile.getAbsolutePath());
        }
        
        public void setSrcdir(File srcdir) {
            this.srcdir=srcdir;
        }
        
        private BuildException createException(String prj,String ws,String msg) {
            return new BuildException(createError(prj,ws,msg));
        }
        
        private String createError(String prj,String ws,String msg) {
            return "Project '"+prj+"'/Web service '"+ws+"': "+msg;
        }
        
        private Iterator getPropertyKeys(Properties props,String regex) {
            Pattern pat=Pattern.compile(regex);
            ArrayList al=new ArrayList();
            Enumeration enum=props.propertyNames();
            while(enum.hasMoreElements()) {
                String key=(String)enum.nextElement();
                Matcher mat=pat.matcher(key);
                if(mat.matches()) al.add(key);
            }
            return al.iterator();
        }
    
}
