/*
 * de.schlund.pfixcore.webservice.Java2WSDLTask
 */
package de.schlund.pfixcore.webservice;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.*;

import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.schlund.pfixcore.webservice.config.*;
import de.schlund.pfixcore.webservice.generate.*;

/**
 * Java2WSDLTask.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class Java2WSDLTask extends Task {
   
    private String msg;

    private String fqdn;
    private File tmpdir;
    private File prjdir;
    private File prjfile;
    private File srcdir;
    private File webappsdir;
    
    
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
                String prjName=elem.getAttribute("name");
                
                File wsConfFile=new File(prjdir,prjName+File.separator+"conf"+File.separator+"webservice.prop");         
                if(wsConfFile.exists()) {
                
                    ConfigProperties cfgProps=new ConfigProperties(new File[] {wsConfFile});
                    ServiceConfiguration srvConf=new ServiceConfiguration(cfgProps);
                    ServiceGlobalConfig globConf=srvConf.getServiceGlobalConfig();
                    
                    Element srvElem=(Element)elem.getElementsByTagName("servername").item(0);
                    if(srvElem==null) throw new BuildException("Missing servername element in configuration of project '"+prjName+"'");
                    String srvName=null;
                    try {
                        srvName=getServerName(srvElem);
                    } catch(Exception x) {
                        throw new BuildException("Error while processing servername element from configuration of project '"+prjName+"'",x);
                    }
               
                    String wsUrl="http://"+srvName+globConf.getRequestPath();   
                    
                    File appDir=new File(webappsdir,prjName);
                    if(!appDir.exists()) throw new BuildException("Web application directory of project '"+prjName+"' doesn't exist");
                    
                    Iterator it=srvConf.getServiceConfig();
                    
                    while(it.hasNext()) {
                        
                        ServiceConfig conf=(ServiceConfig)it.next();
                        
                        String wsName=conf.getName();
                        String wsItf=conf.getInterfaceName();
                        String wsItfPkg=getPackageName(wsItf);
                        
                        String wsItfPath=wsItf.replace('.',File.separatorChar)+".java";
                        File wsItfFile=new File(srcdir,wsItfPath);
                        if(!wsItfFile.exists()) throw new BuildException("Web service interface source '"+wsItfFile.getAbsolutePath()+"' doesn't exist.");
               
                        File wsdlFile=new File(appDir,wsName+".wsdl");
                        if(!(wsdlFile.exists() && wsdlFile.lastModified()>wsItfFile.lastModified())) {
                                String wsNS=createShortNamespace(wsName);
                                //String wsNS=createLongNamespace(wsItf);
                                Java2Wsdl task=new Java2Wsdl();
                                task.setOutput(wsdlFile);
                                task.setClassName(wsItf);
                                task.setNamespace(wsNS);
                                task.setLocation(wsUrl+"/"+wsName);
                                //task.addNamespaceMapping("de.schlund.pfixcore.example.webservices",wsNS);
                                task.generate();
                        }
                        
                        File wsddFile=new File(appDir,wsName+".wsdd");
                        if(!(wsddFile.exists() && wsddFile.lastModified()>wsdlFile.lastModified())) {      
                            Wsdl2Java task=new Wsdl2Java();
                            task.setOutput(new File("/tmp/ws"));
                            task.setDeployScope("Application");
                            task.setServerSide(true);
                            task.setURL(wsdlFile.getAbsolutePath());
                            task.setPackageName(wsItfPkg);
                            task.generate();
                        }
                            
                            
                    
                            
                        }
                    
                }
                
            }
               
        
        } catch(Exception x) {
            throw new BuildException(x);
        }

        System.out.println(msg);
        }
    
        private String getPackageName(String className) {
            int ind=className.lastIndexOf('.');
            if(ind>0) return className.substring(0,ind);
            return "";
        }
    
        private String getServerName(Element srvElem) throws Exception {
            StringBuffer sb=new StringBuffer();
            NodeList nl=srvElem.getChildNodes();
            for(int i=0;i<nl.getLength();i++) {
                Node n=nl.item(i);
                if(n.getNodeType()==Node.TEXT_NODE) sb.append(n.getNodeValue().trim());
                else if(n.getNodeType()==Node.ELEMENT_NODE) {
                    if(n.getNodeName().equals("cus:fqdn")) sb.append(fqdn);
                    else throw new Exception("Unsupported XML element: "+n.getNodeName());
                } else throw new BuildException("Unsupported XML element: "+n.getNodeName());   
            }
            return sb.toString();
        }
    
        private String createShortNamespace(String id) {
            StringBuffer ns=new StringBuffer();
            ns.append("urn:");
            ns.append(id);
            return ns.toString();
        }
    
        private String createLongNamespace(String className) {
            StringBuffer ns=new StringBuffer();
            ns.append("urn:");
            StringTokenizer st=new StringTokenizer(className,".");
            String parts[]=new String[st.countTokens()];
            int i=0;
            while(st.hasMoreTokens()) {
                parts[i]=st.nextToken();
                i++;
            }
            for(i=parts.length-2;i>-1;i--) {
                if(i<parts.length-2) ns.append('.');
                ns.append(parts[i]);
            }
            return ns.toString();
        }
        
        private void initTmpDir() throws BuildException {
            if(tmpdir.exists()) {
                if(tmpdir.isDirectory()) log("Warning!!! Temporary directory "+tmpdir.getAbsolutePath()+" already exists and will get emptied.");
                else log("Warning!!! Temporary file "+tmpdir.getAbsolutePath()+" already exists and will be removed.");
                delete(tmpdir);
            }
            boolean ok=tmpdir.mkdir();
            if(!ok) throw new BuildException("Can't create temporary directory "+tmpdir.getAbsolutePath());
        }
    
        private void finalizeTmpDir() {
            if(tmpdir.exists()) {
                boolean ok=delete(tmpdir);
                if(!ok) throw new BuildException("Can't delete temporary directory "+tmpdir.getAbsolutePath());
            }
        }
        
        private boolean delete(File file) {
            if(file.isDirectory()) {
                File[] files=file.listFiles();
                for(int i=0;i<files.length;i++) {
                    delete(files[i]);
                }
            }
            return file.delete();
        }
        
    
        // The setter for the "message" attribute
        public void setMessage(String msg) {
            this.msg = msg;
        }
        
        public void setFqdn(String fqdn) {
            this.fqdn=fqdn;
        }
        
        public void setTmpdir(File tmpdir) {
            this.tmpdir=tmpdir;
            System.out.println(tmpdir.getAbsolutePath());
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
        
        public void setWebappsdir(File webappsdir) {
            this.webappsdir=webappsdir;
        }
        
        private BuildException createException(String prj,String ws,String msg) {
            return new BuildException(createError(prj,ws,msg));
        }
        
        private String createError(String prj,String ws,String msg) {
            return "Project '"+prj+"'/Web service '"+ws+"': "+msg;
        }
        
}
