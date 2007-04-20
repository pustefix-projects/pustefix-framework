/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.webservice.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashSet;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDHandler;
import org.apache.axis.deployment.wsdd.WSDDRequestFlow;
import org.apache.axis.deployment.wsdd.WSDDResponseFlow;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ConfigurationReader;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * WebServiceTask.java
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig@schlund.de
 */
public class WebServiceTask extends Task {
   
    private String fqdn;
    private File tmpdir;
    private File prjdir;
    private File prjfile;
    private File srcdir;
    private File builddir;
    private File webappsdir;
    private File wsddSkel;
    
    private DocumentBuilder docBuilder;
    private Transformer trfSerializer;
    
    private boolean shortNamespaces=false;
    private String deployScope="Application";
    //SOAP encoding style: rpc|document
    private String encStyle="rpc";
    //SOAP encoding use: encoded|literal
    private String encUse="encoded";
    
    public void checkAttributes() throws BuildException {
    	if(srcdir==null) throw new BuildException("No source directory specified.");
        if(!(srcdir.exists()&&srcdir.isDirectory())) throw new BuildException("Source directory '"+srcdir+"' doesn't exist.");
        if(builddir==null) throw new BuildException("No build directory specified.");
        if(!(builddir.exists()&&builddir.isDirectory())) throw new BuildException("Build directory '"+builddir+"' doesn't exist.");
    }
    
    public void init() {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.ERROR);
        logger.addAppender(appender);
    }
    
    public void execute() throws BuildException {
        checkAttributes();
        if(!prjfile.exists()) throw new BuildException("Project configuration file "+prjfile.getAbsolutePath()+" doesn't exist");
        try {
            GlobalConfigurator.setDocroot(prjdir.getAbsolutePath());
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
        try {
            Document doc=loadDoc(prjfile);
            NodeList nl=doc.getElementsByTagName("project");
            //iterate over projects
            for(int i=0;i<nl.getLength();i++) {
                Element elem=(Element)nl.item(i);
                String prjName=elem.getAttribute("name");
                FileResource wsConfFile=ResourceUtil.getFileResource("file://"+prjdir.getAbsolutePath()+"/"+prjName+"/"+"conf"+"/"+"webservice.conf.xml");
                //go on processing if webservices found
                if(wsConfFile.exists()) {
                    if(!wsddSkel.exists()) throw new BuildException("Web service deployment descriptor skeleton "+
                            wsddSkel.getAbsolutePath()+" doesn't exist.");
                    File tmpDir=getTmpDir(prjName);
                    //read webservice configuration
                    Configuration srvConf=ConfigurationReader.read(wsConfFile);
                    GlobalServiceConfig globConf=srvConf.getGlobalServiceConfig();
                    
                    Configuration refSrvConf=null;
                    GlobalServiceConfig refGlobConf=null;
                    boolean globalConfChanged=false;
                    
                    //read last built webservice configuration
                    FileResource refWsConfFile=ResourceUtil.getFileResource("file://"+tmpDir.getAbsolutePath()+"/"+"webservice.conf.ser");
                    if(refWsConfFile.exists()) {
                        refSrvConf=ConfigurationReader.deserialize(refWsConfFile);
                    	refGlobConf=refSrvConf.getGlobalServiceConfig();
                    	if(!globConf.equals(refGlobConf)) globalConfChanged=true;
                    }
                    //Setup WSDL repository
                    File appDir=new File(webappsdir,prjName);
                    if(!appDir.exists()) throw new BuildException("Web application directory of project '"+prjName+"' doesn't exist");
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
                    //Setup javascript stub repository
                    File stubDir=tmpDir;
                    if(globConf.getStubGenerationEnabled()) {
                        String stubRepo=globConf.getStubRepository();
                        if(stubRepo.startsWith("/")) stubRepo.substring(1);
                        stubDir=new File(appDir,stubRepo);
                        if(!stubDir.exists()) {
                            boolean ok=stubDir.mkdir();
                            if(!ok) throw new BuildException("Can't create webservice stub directory "+stubDir.getAbsolutePath());
                        }
                    }
                    
                    File webInfDir=new File(appDir,"WEB-INF");
                    if(!webInfDir.exists()) throw new BuildException("Web application WEB-INF subdirectory of project '"+prjName+"' doesn't exist");
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
                    
                    WSDDRequestFlow reqFlow=new WSDDRequestFlow();
                    WSDDResponseFlow resFlow=new WSDDResponseFlow();
                    
                    WSDDHandler errorHandler=new WSDDHandler();
                    errorHandler.setType(new QName("ErrorHandler"));
                    reqFlow.addHandler(errorHandler);
                    
                    if(globConf.getLoggingEnabled() || globConf.getMonitoringEnabled()) {         
                    	WSDDHandler recHandler=new WSDDHandler();
                    	recHandler.setType(new QName("RecordingHandler"));
                    	reqFlow.addHandler(recHandler);
                    	resFlow.addHandler(recHandler);
                    }
                    	
                    Element srvElem=(Element)elem.getElementsByTagName("servername").item(0);
                    if(srvElem==null) throw new BuildException("Missing servername element in configuration of project '"+prjName+"'");
                    String srvName=getServerName(prjName,srvElem);
               
                    String wsUrl="http://"+srvName+globConf.getRequestPath();   
                    
                    int srvCnt=0;
                    int wsdlCnt=0;
                    int stubCnt=0;
                    int wsddCnt=0;
                    
                    for(ServiceConfig conf:srvConf.getServiceConfig()) {
                        
                        if(conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY)||
                                conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_SOAP)) {
                        
                            srvCnt++;
                            String wsName=conf.getName();
                            ServiceConfig refConf=null;
                            if(refSrvConf!=null) refConf=refSrvConf.getServiceConfig(wsName);
                            
                            String wsItf=conf.getInterfaceName();
                            String wsImpl=conf.getImplementationName();
                            //String wsItfPkg=getPackageName(wsItf);
                            
                            //Get service specific webservice scope
                            String wsDeployScope=deployScope;
                            if(conf.getScopeType()!=null) wsDeployScope=conf.getScopeType();
                            
                            //Get service specific message style, if not present take default
                            String wsEncStyle=encStyle;
                            if(conf.getEncodingStyle()!=null) wsEncStyle=conf.getEncodingStyle();
                            String wsEncUse=encUse;
                            if(conf.getEncodingUse()!=null) wsEncUse=conf.getEncodingUse();
                            
                            //Don't check for source files cause webservice classes can come from jar file
                            //String wsItfPath=wsItf.replace('.',File.separatorChar)+".java";
                            //File wsItfFile=new File(srcdir,wsItfPath);
                            //if(!wsItfFile.exists()) throw new BuildException("Web service interface source '"+wsItfFile.getAbsolutePath()+"' doesn't exist.");
        
                            File wsdlFile=new File(wsdlDir,wsName+".wsdl");
                            
                            //Generate WSDL
                            if(refConf==null || !wsdlFile.exists() || globalConfChanged || !conf.equals(refConf) || checkChanges(wsItf,wsdlFile)) { 
                                 
                                    checkInterface(wsItf);
                                
                                    wsdlCnt++;
                                    //String wsNS=null;
                                    //if(shortNamespaces) wsNS=createShortNamespace(wsName);
                                    //else wsNS=createLongNamespace(wsItf);
                                    Java2Wsdl task=new Java2Wsdl();
                                    task.setOutput(wsdlFile);
                                    task.setClassName(wsItf);
                                    //task.setNamespace(wsNS);
                                    task.setLocation(wsUrl+"/"+wsName);
                                    task.setImplClassName(conf.getImplementationName());
                                    //task.addNamespaceMapping("de.schlund.pfixcore.example.webservices",wsNS);
                                    task.setStyle(wsEncStyle);
                                    task.setUse(wsEncUse);
                                    task.generate();
                                    log("Created webservice definition file "+wsdlFile.getAbsolutePath(),Project.MSG_VERBOSE);
                            }                
                         
                            //Generate javascript stubs            
                            if(globConf.getStubGenerationEnabled()) {
                                File stubFile=new File(stubDir,wsName+".js");
                                if(!stubFile.exists() || stubFile.lastModified()<wsdlFile.lastModified()) {
                                    Wsdl2Js task=new Wsdl2Js();
                                    task.setInputFile(wsdlFile);
                                    task.setOutputFile(stubFile);
                                    task.generate();
                                    stubCnt++;
                                }
                            }
                            
                            //Generate WSDD
                            File wsddPath=null;
                            if(shortNamespaces) {
                                wsddPath=new File(tmpDir,wsName+"_pkg");
                            } else {
                                String wsddPathPart=getPackageName(wsItf).replace('.',File.separatorChar);
                                wsddPath=new File(tmpDir,wsddPathPart);
                            }
                            File wsddFile=new File(tmpDir,wsName+".wsdd");
                            if(!wsddFile.exists() || wsddFile.lastModified()<wsdlFile.lastModified()) {
                                
                                wsddCnt++;
                                Wsdl2Java task=new Wsdl2Java();
                                task.setOutput(tmpDir);
                                task.setDeployScope(wsDeployScope);
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
                                    
                                    wsddServices[j].setRequestFlow(reqFlow);
                                    wsddServices[j].setResponseFlow(resFlow);
                                    
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
                    }
                    
                    //Store changed server deployment descriptor
                    if(wsddChanged) {
                        serialize(srvWsdd.getDOMDocument(),srvWsddFile);
                        log("Created server deployment descriptor file "+srvWsddFile.getAbsolutePath(),Project.MSG_VERBOSE);
                    }
                            
                    //Store current webservice configuration file
                    ConfigurationReader.serialize(srvConf,refWsConfFile);
                            
                    if(wsdlCnt!=0) log("Generated "+wsdlCnt+"(of "+srvCnt+") WSDL file(s)");
                    if(wsddCnt!=0) log("Generated "+wsddCnt+"(of "+srvCnt+") WSDD file(s)");
                    if(stubCnt!=0) log("Generated "+stubCnt+"(of "+srvCnt+") WS stub file(s)");
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
    
        /**
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
        */
        
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
                HashSet<String> names=new HashSet<String>();
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
           
        private boolean checkChanges(String className,File wsdlFile) throws BuildException {
            try {
            	Class clazz=Class.forName(className);
            	if(!clazz.isInterface()) throw new BuildException("Web service interface class '"+className+"' doesn't define an interface type.");
                //Check if interface or dependant interfaces changed
                boolean changed=checkTypeChange(clazz,wsdlFile);
                if(changed) return true;
                //Check if method parameter or return type classes changed
                Method[] meths=clazz.getMethods();
                for(int i=0;i<meths.length;i++) {
                    Class ret=meths[i].getReturnType();
                    changed=checkTypeChange(ret,wsdlFile);
                    if(changed) return true;
                    Class[] pars=meths[i].getParameterTypes();
                    for(int j=0;j<pars.length;j++) {
                    	changed=checkTypeChange(pars[j],wsdlFile);
                    	if(changed) return true;
                    }
                }
                return false;
            } catch(ClassNotFoundException x) {
            	throw new BuildException("Web service interface class '"+className+"' not found.",x);
            }
        }
        
        private boolean checkTypeChange(Class clazz,File wsdlFile) {
        	if(!clazz.isPrimitive()) {
                ClassLoader cl=clazz.getClassLoader();
                if(cl instanceof AntClassLoader) {
                    if(clazz.isArray()) return checkTypeChange(getArrayType(clazz),wsdlFile);   
                    String path=clazz.getName().replace('.',File.separatorChar)+".class";
                    File file=new File(builddir,path);
                    long lastMod=Long.MAX_VALUE;
                    if(!file.exists()) {
                        URL url=cl.getResource(path);
                        if(url==null) throw new BuildException("Can't get URL for webservice class '"+clazz.getName()+"' from jar file.");
                        else {
                            try {
                                JarURLConnection con=(JarURLConnection)url.openConnection();
                                lastMod=con.getJarEntry().getTime();
                            } catch(IOException x) {
                                throw new BuildException("Can't get modification time for webservice class '"+clazz.getName()+"' from jar file.");
                            }
                        }
                    } else {
                        lastMod=file.lastModified();
                    }
                    if(wsdlFile.lastModified()<lastMod) return true;
                    if(clazz.isInterface()) {
                        Class[] itfs=clazz.getInterfaces();
                        for(int i=0;i<itfs.length;i++) {
                        	boolean changed=checkTypeChange(itfs[i],wsdlFile);
                        	if(changed) return true;
                        }
                    } else {
                        Class sup=clazz.getSuperclass();
                        boolean changed=checkTypeChange(sup,wsdlFile);
                        if(changed) return true;
                    }
                }
            }
            return false;
        }
        
        private Class getArrayType(Class clazz) {
        	if(clazz.isArray()) return getArrayType(clazz.getComponentType());
            else return clazz;
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
        
        public void setBuildDir(File builddir) {
        	this.builddir=builddir;
        }
        
        public void setWsddskel(File wsddSkel) {
            this.wsddSkel=wsddSkel;
        }
        
        public void setWebappsdir(File webappsdir) {
            this.webappsdir=webappsdir;
        }
        
}
