/*
 * de.schlund.pfixcore.webservice.WebServiceTask
 */
package de.schlund.pfixcore.webservice.generate;

import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDHandler;
import org.apache.axis.deployment.wsdd.WSDDRequestFlow;
import org.apache.axis.deployment.wsdd.WSDDResponseFlow;
import org.apache.axis.deployment.wsdd.WSDDService;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import de.schlund.pfixcore.webservice.config.*;

/**
 * WebServiceTask.java
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class WebServiceTask extends Task {
   
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
    
    private boolean shortNamespaces=false;
    //SOAP encoding style: rpc|document
    private String encStyle="rpc";
    //SOAP encoding use: encoded|literal
    private String encUse="encoded";
    
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
                    File globPropsFile=new File(tmpDir,"global.props");
                    boolean propsChanged=false;
                    if(!globPropsFile.exists() || globPropsFile.lastModified()<wsConfFile.lastModified()) propsChanged=true;
                    
                    ConfigProperties cfgProps=new ConfigProperties(new File[] {wsConfFile});
                    Configuration srvConf=new Configuration(cfgProps);
                    GlobalServiceConfig globConf=srvConf.getGlobalServiceConfig();
                    
                    //Get default message style
                    encStyle=globConf.getEncodingStyle();
                    encUse=globConf.getEncodingUse();
                   
                    File appDir=new File(webappsdir,prjName);
                    if(!appDir.exists()) throw new BuildException("Web application directory of project '"+prjName+"' doesn't exist");
                    File webInfDir=new File(appDir,"WEB-INF");
                    if(!webInfDir.exists()) throw new BuildException("Web application WEB-INF subdirectory of project '"+prjName+"' doesn't exist");
                    
                    File wsdlDir=tmpDir;
                    if(globConf.getWSDLSupportEnabled()) {
                        String wsdlRepo=globConf.getWSDLRepository();
                        if(wsdlRepo.startsWith("/")) wsdlRepo.substring(1);
                        wsdlDir=new File(appDir,wsdlRepo);
                        if(!wsdlDir.exists()) {
                            boolean ok=wsdlDir.mkdir();
                            if(!ok) throw new BuildException("Can't create WSDL directory "+wsdlDir.getAbsolutePath());
                        }
                    }
                    
                    File srvWsddFile=new File(webInfDir,"server-config.wsdd");
                    boolean isNewWsdd=false;
                    boolean wsddChanged=false;
                    WSDDDocument srvWsdd=null;
                    if(!srvWsddFile.exists() || srvWsddFile.lastModified()<wsddSkel.lastModified()) {
                        Document wsddDoc=loadDoc(wsddSkel);
                        srvWsdd=new WSDDDocument(wsddDoc);
                        isNewWsdd=true;
                        wsddChanged=true;
                    } else {
                        Document wsddDoc=loadDoc(srvWsddFile);
                        srvWsdd=new WSDDDocument(wsddDoc);
                    }
                    
                    WSDDRequestFlow reqFlow=null;
                    WSDDResponseFlow resFlow=null;
                    if(globConf.getLoggingEnabled() || globConf.getMonitoringEnabled()) {
                        reqFlow=new WSDDRequestFlow();
                        resFlow=new WSDDResponseFlow();
                        if(globConf.getLoggingEnabled()) {
                            WSDDHandler loggingHandler=new WSDDHandler();
                            loggingHandler.setType(new QName("LoggingHandler"));
                            reqFlow.addHandler(loggingHandler);
                            resFlow.addHandler(loggingHandler);
                        }
                        if(globConf.getMonitoringEnabled()) {
                            WSDDHandler monitorHandler=new WSDDHandler();
                            monitorHandler.setType(new QName("MonitoringHandler"));
                            reqFlow.addHandler(monitorHandler);
                            resFlow.addHandler(monitorHandler);
                        }
                    }
                    
                    Element srvElem=(Element)elem.getElementsByTagName("servername").item(0);
                    if(srvElem==null) throw new BuildException("Missing servername element in configuration of project '"+prjName+"'");
                    String srvName=getServerName(prjName,srvElem);
               
                    String wsUrl="http://"+srvName+globConf.getRequestPath();   
                    
                    Iterator it=srvConf.getServiceConfig();
                    //iterate over services
                    int srvCnt=0;
                    int wsdlCnt=0;
                    int wsddCnt=0;
                    while(it.hasNext()) {
                        
                        srvCnt++;
                        ServiceConfig conf=(ServiceConfig)it.next();
                        String wsName=conf.getName();
                        String wsItf=conf.getInterfaceName();
                        String wsImpl=conf.getImplementationName();
                        String wsItfPkg=getPackageName(wsItf);
                        
                        //Get service specific message style, if not present take default
                        String wsEncStyle=encStyle;
                        if(conf.getEncodingStyle()!=null) wsEncStyle=conf.getEncodingStyle();
                        String wsEncUse=encUse;
                        if(conf.getEncodingUse()!=null) wsEncUse=conf.getEncodingUse();
                        
                        File confPropsFile=new File(tmpDir,wsName+".props");
                        String wsItfPath=wsItf.replace('.',File.separatorChar)+".java";
                        File wsItfFile=new File(srcdir,wsItfPath);
                        if(!wsItfFile.exists()) throw new BuildException("Web service interface source '"+wsItfFile.getAbsolutePath()+"' doesn't exist.");
                        File wsdlFile=new File(wsdlDir,wsName+".wsdl");
                        
                        if(!wsdlFile.exists() || wsdlFile.lastModified()<wsItfFile.lastModified() || !confPropsFile.exists() || 
                                propsChanged || conf.doesDiff(new ServiceConfig(new ConfigProperties(new File[] {confPropsFile}),wsName))) {
                            
                                checkInterface(wsItf);
                            
                                wsdlCnt++;
                                String wsNS=null;
                                if(shortNamespaces) wsNS=createShortNamespace(wsName);
                                else wsNS=createLongNamespace(wsItf);
                                Java2Wsdl task=new Java2Wsdl();
                                task.setOutput(wsdlFile);
                                task.setClassName(wsItf);
                                task.setNamespace(wsNS);
                                task.setLocation(wsUrl+"/"+wsName);
                                task.setImplClassName(conf.getImplementationName());
                                task.addNamespaceMapping("de.schlund.pfixcore.example.webservices",wsNS);
                                task.setStyle(wsEncStyle);
                                task.setUse(wsEncUse);
                                task.generate();
                                log("Created webservice definition file "+wsdlFile.getAbsolutePath(),Project.MSG_VERBOSE);
                                conf.saveProperties(new File(tmpDir,wsName+".props"));
                        }                
                     
                        File wsddPath=null;
                        if(shortNamespaces) {
                            wsddPath=new File(tmpDir,wsName+"_pkg");
                        } else {
                            String wsddPathPart=getPackageName(wsItf).replace('.',File.separatorChar);
                            wsddPath=new File(tmpDir,wsddPathPart);
                        }
                        File wsddFile=wsddFile=new File(tmpDir,wsName+".wsdd");
                        if(!wsddFile.exists() || wsddFile.lastModified()<wsdlFile.lastModified()) {
                            
                            wsddCnt++;
                            Wsdl2Java task=new Wsdl2Java();
                            task.setOutput(tmpDir);
                            task.setDeployScope("Application");
                            task.setServerSide(true);
                            task.setURL(wsdlFile.getAbsolutePath());
                            //task.setPackageName(wsItfPkg);
                            task.generate();
                            
                            File origWsddFile=new File(wsddPath,"deploy.wsdd");
                            if(!origWsddFile.exists()) throw new BuildException("Can't locate deployment descriptor file "+origWsddFile.getAbsolutePath());
                            origWsddFile.renameTo(wsddFile);
                            log("Created deployment descriptor file "+wsddFile.getAbsolutePath(),Project.MSG_VERBOSE);
                            
                            
                           
                            
                            Document wsddDoc=loadDoc(wsddFile);
                            WSDDDocument wsdd=new WSDDDocument(wsddDoc);
                            WSDDService[] wsddServices=wsdd.getDeployment().getServices();
                            for(int j=0;j<wsddServices.length;j++) {
                                //Change automatically generated name of implementation class to configured name
                                wsddServices[j].setParameter("className",wsImpl);
                                
                                if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
                                    wsddServices[j].setRequestFlow(reqFlow);
                                    wsddServices[j].setResponseFlow(resFlow);
                                }
                                
                                //Update server deployment descriptor
                                srvWsdd.getDeployment().deployService(wsddServices[j]);
                                wsddChanged=true;
                            }
                            serialize(wsdd.getDOMDocument(),wsddFile);
                              
                        } else if(isNewWsdd) {
                            
                            Document wsddDoc=loadDoc(wsddFile);
                            WSDDDocument wsdd=new WSDDDocument(wsddDoc);
                            WSDDService[] wsddServices=wsdd.getDeployment().getServices();
                            for(int j=0;j<wsddServices.length;j++) {
                                //Update server deployment descriptor
                                srvWsdd.getDeployment().deployService(wsddServices[j]);
                                wsddChanged=true;
                            }
                            
                        }
                        
                    }
                    
                   
                    
                    //Store changed server deployment descriptor
                    if(wsddChanged) {
                        serialize(srvWsdd.getDOMDocument(),srvWsddFile);
                        log("Created server deployment descriptor file "+srvWsddFile.getAbsolutePath(),Project.MSG_VERBOSE);
                    }
                    
                    //Store current global webservice properties
                    globConf.saveProperties(globPropsFile);
                    
                    if(wsdlCnt!=0 || wsddCnt!=0) log("Generated "+wsdlCnt+"(of "+srvCnt+") WSDL file(s) and "+wsddCnt+" (of "+srvCnt+") WSDD file(s)");
                    if(wsddChanged) log("Generated server WSDD file");
                    
                    
                }
                
            }
               
        
        } catch(Exception x) {
            throw new BuildException(x);
        }

        
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
        
       
        private void checkInterface(String className) throws BuildException {
            try {
                Class clazz=Class.forName(className);
                if(!clazz.isInterface()) throw new BuildException("Web service interface class doesn't represent an interface type");
                Method[] methods=clazz.getDeclaredMethods();
                HashSet names=new HashSet();
                for(int i=0;i<methods.length;i++) {
                    String name=methods[i].getName();
                    if(names.contains(name)) throw new BuildException("Web service interface class '"+className+"' contains "+
                            "overloaded method '"+name+"'. Method overloading isn't allowed in web service interface definitions, "+
                            "as future WSDL versions (1.2+) will no longer support operation overloading.");
                    names.add(name);
                }        
            } catch(ClassNotFoundException x) {
                throw new BuildException("Web service interface class "+className+" not found",x);
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
    
        public void setFqdn(String fqdn) {
            this.fqdn=fqdn;
        }
        
        public void setTmpdir(File tmpdir) {
            this.tmpdir=tmpdir;
        }
        
        public void setPrjdir(File prjdir) {
            this.prjdir=prjdir;
        }
        
        public void setPrjfile(File prjfile) {
            this.prjfile=prjfile;
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
        
}
