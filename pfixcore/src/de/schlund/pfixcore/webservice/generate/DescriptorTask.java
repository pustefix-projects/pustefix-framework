/*
 * de.schlund.pfixcore.webservice.DescriptorTask
 */
package de.schlund.pfixcore.webservice.generate;

import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDService;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import de.schlund.pfixcore.webservice.config.*;

/**
 * DescriptorTask.java
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class DescriptorTask extends Task {
   
    private String msg;

    private String fqdn;
    private File tmpdir;
    private File prjdir;
    private File prjfile;
    private File srcdir;
    private File webappsdir;
    private File wsddSkel;
    
    private DocumentBuilder docBuilder;
    private Transformer trfSerializer;
   
    
    public void execute() throws BuildException {
        
        if(!prjfile.exists()) throw new BuildException("Project configuration file "+prjfile.getAbsolutePath()+" doesn't exist");
        
        if(!wsddSkel.exists()) throw new BuildException("Web service deployment descriptor skeleton"+
                wsddSkel.getAbsolutePath()+"doesn't exist.");
        
        try {

            Document doc=loadDoc(prjfile);
            NodeList nl=doc.getElementsByTagName("project");
              
            //iterate over projects
            for(int i=0;i<nl.getLength();i++) {
                
                Element elem=(Element)nl.item(i);
                String prjName=elem.getAttribute("name");
                File wsConfFile=new File(prjdir,prjName+File.separator+"conf"+File.separator+"webservice.prop");         
                
                //go on processing if webservices found
                if(wsConfFile.exists()) {
                
                    File tmpDir=getTmpDir(prjName);
                    
                    ConfigProperties cfgProps=new ConfigProperties(new File[] {wsConfFile});
                    ServiceConfiguration srvConf=new ServiceConfiguration(cfgProps);
                    ServiceGlobalConfig globConf=srvConf.getServiceGlobalConfig();
                    
                    Element srvElem=(Element)elem.getElementsByTagName("servername").item(0);
                    if(srvElem==null) throw new BuildException("Missing servername element in configuration of project '"+prjName+"'");
                    String srvName=getServerName(prjName,srvElem);
               
                    String wsUrl="http://"+srvName+globConf.getRequestPath();   
                    
                    File appDir=new File(webappsdir,prjName);
                    if(!appDir.exists()) throw new BuildException("Web application directory of project '"+prjName+"' doesn't exist");
                    
                    Iterator it=srvConf.getServiceConfig();
                    
                    while(it.hasNext()) {
                        
                        ServiceConfig conf=(ServiceConfig)it.next();
                        
                        String wsName=conf.getName();
                        String wsItf=conf.getInterfaceName();
                        String wsImpl=conf.getImplementationName();
                        String wsItfPkg=getPackageName(wsItf);
                        
                        String wsItfPath=wsItf.replace('.',File.separatorChar)+".java";
                        File wsItfFile=new File(srcdir,wsItfPath);
                        if(!wsItfFile.exists()) throw new BuildException("Web service interface source '"+wsItfFile.getAbsolutePath()+"' doesn't exist.");
               
                        File wsdlFile=new File(tmpDir,wsName+".wsdl");
                        if(!(wsdlFile.exists() && wsdlFile.lastModified()>=wsItfFile.lastModified())) {
                                String wsNS=createShortNamespace(wsName);
                                //String wsNS=createLongNamespace(wsItf);
                                Java2Wsdl task=new Java2Wsdl();
                                task.setOutput(wsdlFile);
                                task.setClassName(wsItf);
                                task.setNamespace(wsNS);
                                task.setLocation(wsUrl+"/"+wsName);
                                //task.addNamespaceMapping("de.schlund.pfixcore.example.webservices",wsNS);
                                task.generate();
                                System.out.println("Created: "+wsdlFile.getAbsolutePath());
                                conf.saveProperties(new File(tmpDir,wsName+".props"));
                        }
                        
                     
                        File srvWsddFile=new File(tmpDir,"server-config.wsdd");
                       
                        
                        
                        
                        System.out.println("###############:"+wsddSkel.getAbsolutePath());
                        
                        
                        String wsddPathPart=getPackageName(wsItf).replace('.',File.separatorChar);
                        File wsddPath=new File(tmpDir,wsddPathPart);
                        File wsddFile=new File(tmpDir,wsName+".wsdd");
                        if(!(wsddFile.exists() && wsddFile.lastModified()>=wsdlFile.lastModified())) {      
                            Wsdl2Java task=new Wsdl2Java();
                            task.setOutput(tmpDir);
                            task.setDeployScope("Application");
                            task.setServerSide(true);
                            task.setURL(wsdlFile.getAbsolutePath());
                            task.setPackageName(wsItfPkg);
                            task.generate();
                            
                            File origWsddFile=new File(wsddPath,"deploy.wsdd");
                            origWsddFile.renameTo(wsddFile);
                            System.out.println("Created: "+wsddFile.getAbsolutePath());
                            
                            //Change automatically generated name of implementation class to configured name
                            Document wsddDoc=loadDoc(wsddFile);
                            WSDDDocument wsdd=new WSDDDocument(wsddDoc);
                            WSDDService[] wsddServices=wsdd.getDeployment().getServices();
                            for(int j=0;j<wsddServices.length;j++) {
                                wsddServices[j].setParameter("className",wsImpl);
                            }
                            serialize(wsdd.getDOMDocument(),wsddFile);
                            
                            //Update server deployment descriptor
                            
                            
                        }
                  
                       
                        
                    }
                    
                   
                   
                    //Create server deployment descriptor
                    
                    //if(!(srvWsddFile.exists()) {
                       // File skelWsddFile=new File()
                    //}
                    //Document srvWsddDoc=db.parse();
                    
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
    
        private String getServerName(String prjName,Element srvElem) throws BuildException {
            try {
                StringBuffer sb=new StringBuffer();
                NodeList nl=srvElem.getChildNodes();
                for(int i=0;i<nl.getLength();i++) {
                    Node n=nl.item(i);
                    if(n.getNodeType()==Node.TEXT_NODE) sb.append(n.getNodeValue().trim());
                    else if(n.getNodeType()==Node.ELEMENT_NODE) {
                        if(n.getNodeName().equals("cus:fqdn")) sb.append(fqdn);
                        else throw new Exception("Unsupported XML element: "+n.getNodeName());
                    } else throw new Exception("Unsupported XML element: "+n.getNodeName());   
                }
                return sb.toString();
            } catch(Exception x) {
                throw new BuildException("Error while processing servername element from configuration of project '"+prjName+"'",x);
            }
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
            if(!tmpdir.exists()) {
                boolean ok=tmpdir.mkdir();
                if(!ok) throw new BuildException("Can't create temporary directory "+tmpdir.getAbsolutePath());
            }
        }
        
        private File getTmpDir(String project) throws BuildException {
            initTmpDir();
            File dir=new File(tmpdir,project);
            if(!dir.exists()) {
                boolean ok=dir.mkdir();
                if(!ok) throw new BuildException("Can't create temporary directory "+dir.getAbsolutePath());
            }
            return dir;
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
        
        private Document loadDoc(File file) throws BuildException {
            try { 
                if(docBuilder==null) {
                    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    dbf.setValidating(false);
                    docBuilder=dbf.newDocumentBuilder();
                }
                Document doc=docBuilder.parse(file);
                return doc;
            } catch(Exception x) {
                throw new BuildException("Can't load XML document from file "+file.getAbsolutePath(),x);
            }
        }
        
        private void serialize(Document doc,File file) throws BuildException {
            try {
                if(trfSerializer==null) {
                    TransformerFactory tf=TransformerFactory.newInstance();
                    trfSerializer=tf.newTransformer();
                }
                trfSerializer.transform(new DOMSource(doc),new StreamResult(new FileOutputStream(file)));
            } catch(Exception x) {
                throw new BuildException("Can't serialize XML document to file "+file.getAbsolutePath(),x);
            }
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
        
        public void setWsddskel(File wsddSkel) {
            this.wsddSkel=wsddSkel;
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
