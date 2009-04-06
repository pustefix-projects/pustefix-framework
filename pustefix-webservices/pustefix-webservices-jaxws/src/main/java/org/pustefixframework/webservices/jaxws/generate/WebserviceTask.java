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
package org.pustefixframework.webservices.jaxws.generate;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ConfigurationReader;
import org.pustefixframework.webservices.config.GlobalServiceConfig;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.pustefixframework.webservices.spring.WebServiceBeanConfigReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.tools.ws.ant.WsGen;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.FileUtils;

/**
 * Ant task which iterates over the webservice configurations of all projects
 * and creates WSDL descriptions and Javascript stubs for the contained SOAP
 * services. Additionally the endpoint configuration files for the JAXWS runtime
 * are created.
 * 
 * @author mleidig@schlund.de
 */
public class WebserviceTask extends MatchingTask {

    private File prjdir;
    private File builddir;
    private File tmpdir;
    private File webappdir;
    private File gendir;
    private Path classPath;
    private int portbase;
    
    private Set<File> wsgenDirs;

    /**
     * Iterate over projects and webservice configurations and generate WSDL
     * descriptions, Javascript stubs and endpoint configurations.
     */
    @Override
    public void execute() throws BuildException {
        try {
            GlobalConfigurator.setDocroot(prjdir.getAbsolutePath());
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
        try {
            DirectoryScanner scanner = getDirectoryScanner(prjdir);
            String[] files = scanner.getIncludedFiles();
            
            wsgenDirs = new HashSet<File>();
            
            for (String file : files) {
                File prjFile = new File(prjdir, file);
                File confDir = prjFile.getParentFile();
                String projectName = confDir.getParentFile().getName();
                Document doc = TaskUtils.loadDoc(prjFile);
                Element serviceElem = (Element)doc.getElementsByTagName("webservice-service").item(0);
                if(serviceElem != null) {
                    
                    Configuration srvConf = null;
                    Element configFileElem = (Element)serviceElem.getElementsByTagName("config-file").item(0);
                    if(configFileElem == null) throw new BuildException("The 'webservice-service' element requires "+
                            " a 'config-file' child element in file '"+prjFile.getAbsolutePath()+"'.");
                    String configFileUri = configFileElem.getTextContent().trim();
                    FileResource configFile = ResourceUtil.getFileResource(configFileUri);
                    
                    if (configFile.exists()) {
                        srvConf = ConfigurationReader.read(configFile);
                    } else {
                        srvConf = new Configuration(); 
                    }
                    
                    File springConfigFile = new File(prjFile.getParentFile(), "spring.xml");
                    FileResource springConfigRes = ResourceUtil.getFileResource(springConfigFile.toURI());
                    if(springConfigFile.exists()) {
                        List<ServiceConfig> serviceList = WebServiceBeanConfigReader.read(springConfigRes);
                        srvConf.addServiceConfigs(serviceList);
                    }
                    
                    if(srvConf.getServiceConfig().size()>0) {
                        
                        int wsdlCount = 0;
                        int stubCount = 0;
                        File tmpDir = getTmpDir(projectName);
                      
                        GlobalServiceConfig globConf = srvConf.getGlobalServiceConfig();
                        Configuration refSrvConf = null;
                        GlobalServiceConfig refGlobConf = null;
                        boolean globalConfChanged = false;
                        // read last built webservice configuration
                        FileResource refWsConfFile = ResourceUtil.getFileResource("file://" + tmpDir.getAbsolutePath() + "/" + "webservice.conf.ser");
                        if (refWsConfFile.exists()) {
                            try {
                                refSrvConf = ConfigurationReader.deserialize(refWsConfFile);
                                refGlobConf = refSrvConf.getGlobalServiceConfig();
                                if (!globConf.equals(refGlobConf)) globalConfChanged = true;
                            } catch (Exception x) {
                                log("Error deserializing old reference configuration", Project.MSG_VERBOSE);
                                log("Warning: Ignore old reference configuration because it can't be deserialized. "
                                        + "Services will be built from scratch.", Project.MSG_WARN);
                            }
                        }
                        // Setup WSDL repository
                        if (!webappdir.exists()) throw new BuildException("Web application directory of project '" + projectName + "' doesn't exist: " + webappdir.getAbsolutePath());
                        File wsdlDir = tmpDir;
                        if (globConf.getWSDLSupportEnabled()) {
                            String wsdlRepo = globConf.getWSDLRepository();
                            if (wsdlRepo.startsWith("/")) wsdlRepo.substring(1);
                            wsdlDir = new File(webappdir, wsdlRepo);
                            if (!wsdlDir.exists()) {
                                boolean ok = wsdlDir.mkdir();
                                if (!ok) throw new BuildException("Can't create WSDL directory " + wsdlDir.getAbsolutePath());
                            }
                        }
                        // Setup javascript stub repository
                        File stubDir = tmpDir;
                        if (globConf.getStubGenerationEnabled()) {
                            String stubRepo = globConf.getStubRepository();
                            if (stubRepo.startsWith("/")) stubRepo.substring(1);
                            stubDir = new File(webappdir, stubRepo);
                            if (!stubDir.exists()) {
                                boolean ok = stubDir.mkdir();
                                if (!ok) throw new BuildException("Can't create webservice stub directory " + stubDir.getAbsolutePath());
                            }
                        }
                        // Check if WEB-INF exists
                        File webInfDir = new File(webappdir, "WEB-INF");
                        if (!webInfDir.exists())
                            throw new BuildException("Web application WEB-INF subdirectory of project '" + projectName + "' doesn't exist");
                        
                        // Iterate over services
                        for (ServiceConfig conf : srvConf.getServiceConfig()) {
                            if (conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY) || conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_SOAP)) {
                                ServiceConfig refConf = null;
                                if (refSrvConf != null) refConf = refSrvConf.getServiceConfig(conf.getName());
                                File wsdlFile = new File(wsdlDir, conf.getName() + ".wsdl");
                                // Check if WSDL/Javascript has to be built
                                if (refConf == null || !wsdlFile.exists() || globalConfChanged || !conf.equals(refConf)
                                        || TaskUtils.checkInterfaceChange(conf.getInterfaceName(), builddir, wsdlFile)) {
                                    if(conf.getInterfaceName()!=null) checkInterface(conf.getInterfaceName());
                                    Class<?> implClass = Class.forName(conf.getImplementationName());
                                    WebService anno = implClass.getAnnotation(WebService.class);
                                    if(anno == null) {
                                        throw new BuildException("Missing @WebService annotation at service implementation "+
                                                "class '"+conf.getImplementationName()+"' of service '"+conf.getName()+"'.");
                                    }
                                    // Generate WSDL
                                    File wsgenDir=new File(tmpdir,"wsdl/"+conf.getName()+"/"+conf.getImplementationName());
                                    
                                    if(!wsgenDirs.contains(wsgenDir)) {
                                    
                                        if(!wsgenDir.exists()) wsgenDir.mkdirs();
                                        WsGen wsgen = new WsGen();
                                        wsgen.setProject(getProject());
                                        wsgen.setDynamicAttribute("keep", "true");
                                        if(!gendir.exists()) gendir.mkdirs();
                                        wsgen.setDynamicAttribute("sourcedestdir", gendir.getAbsolutePath());
                                        wsgen.setDynamicAttribute("genwsdl", "true");
                                        wsgen.setDynamicAttribute("destdir", builddir.getAbsolutePath());
                                        wsgen.setDynamicAttribute("resourcedestdir", wsgenDir.getAbsolutePath());
                                        wsgen.setDynamicAttribute("classpath", classPath.toString());
                                        wsgen.setDynamicAttribute("sei", conf.getImplementationName());
                                        String serviceName = "{" + TaskUtils.getTargetNamespace(implClass) + "}" + conf.getName();
                                        wsgen.setDynamicAttribute("servicename", serviceName);
                                        try {
                                            wsgen.execute();
                                        } catch(Exception x) {
                                            x.printStackTrace();
                                            throw x;
                                        }
                                        wsgenDirs.add(wsgenDir);
                                    } 
                                    FileUtils.copyFiles(wsgenDir, wsdlDir, ".*wsdl", ".*xsd");
                                    // Replace endpoint URL
                                    String srvName = "HOST";
                                    String srvPort = "";
                                    srvPort = ":" + (portbase + 80);
                                    String wsUrl = "http://" + srvName + srvPort + globConf.getRequestPath() + "/" + conf.getName();
                                    FileUtils.searchAndReplace(wsdlFile, "UTF-8", "REPLACE_WITH_ACTUAL_URL", wsUrl);
                                    wsdlCount++;
                                    // Generate javascript stubs
                                    if (globConf.getStubGenerationEnabled()) {
                                        File stubFile = new File(stubDir, conf.getName() + ".js");
                                        if (!stubFile.exists() || stubFile.lastModified() < wsdlFile.lastModified()) {
                                            Wsdl2Js task = new Wsdl2Js();
                                            task.setInputFile(wsdlFile);
                                            task.setOutputFile(stubFile);
                                            task.generate();
                                            stubCount++;
                                        }
                                    }
                                }
                            }
                        }
                        if (wsdlCount > 0) log("Generated " + wsdlCount + " WSDL file" + (wsdlCount == 1 ? "" : "s") + ".");
                        if (stubCount > 0) log("Generated " + stubCount + " Javascript stub file" + (stubCount == 1 ? "" : "s") + ".");
                       
                        // Store current webservice configuration file
                        ConfigurationReader.serialize(srvConf, refWsConfFile);
                    }
                }
            }
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }

    /**
     * Get or create temporary/webservice artifact directory for a specific
     * project.
     */
    private File getTmpDir(String project) throws BuildException {
        if (!tmpdir.exists()) {
            boolean ok = tmpdir.mkdir();
            if (!ok) throw new BuildException("Can't create temporary directory " + tmpdir.getAbsolutePath());
        }
        File dir = new File(tmpdir, project);
        if (!dir.exists()) {
            boolean ok = dir.mkdir();
            if (!ok) throw new BuildException("Can't create temporary directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Check if class is a legal webservice interface (being an interface and
     * having no overloaded methods)
     */
    private void checkInterface(String className) throws BuildException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!clazz.isInterface()) throw new BuildException("Web service interface class doesn't represent an interface type");
            Method[] methods = clazz.getDeclaredMethods();
            HashSet<String> names = new HashSet<String>();
            for (int i = 0; i < methods.length; i++) {
                String name = methods[i].getName();
                if (names.contains(name))
                    throw new BuildException("Web service interface class '" + className + "' contains " + "overloaded method '" + name
                            + "'. Method overloading isn't allowed in web service interface definitions, "
                            + "as future WSDL versions (1.2+) will no longer support operation overloading.");
                names.add(name);
            }
        } catch (ClassNotFoundException x) {
            throw new BuildException("Web service interface class " + className + " not found", x);
        }
    }

    // Ant-task properties:
    
    /**
     * Set project directory.
     */
    public void setPrjdir(File prjdir) {
        this.prjdir = prjdir;
    }

    /**
     * Set build directory.
     */
    public void setBuildDir(File builddir) {
        this.builddir = builddir;
    }
    
    public void setGenDir(File gendir) {
        this.gendir = gendir;
    }

    /**
     * Set directory for temporary/webservice build artifacts.
     */
    public void setTmpdir(File tmpdir) {
        this.tmpdir = tmpdir;
    }

    /**
     * Set webapp deployment directory.
     */
    public void setWebappdir(File webappdir) {
        this.webappdir = webappdir;
    }

    /**
     * Set reference to classpath containing application classes.
     */
    public void setClasspathRef(Reference ref) {
        classPath = new Path(getProject());
        classPath.createPath().setRefid(ref);
    }

    /**
     * Set the relative port for the Tomcat.
     */
    public void setPortbase(int portbase) {
        this.portbase = portbase;
    }

}
