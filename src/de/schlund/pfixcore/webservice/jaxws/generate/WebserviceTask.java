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
package de.schlund.pfixcore.webservice.jaxws.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.tools.ws.ant.WsGen;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ConfigurationReader;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
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
public class WebserviceTask extends Task {

    private final static String XMLNS_JAXWS_RUNTIME = "http://java.sun.com/xml/ns/jax-ws/ri/runtime";
    private final static String XMLNS_JAVAEE = "http://java.sun.com/xml/ns/javaee";

    private File prjfile;
    private File prjdir;
    private File builddir;
    private File tmpdir;
    private File webappsdir;
    private Path classPath;
    private boolean standalone;
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
            wsgenDirs = new HashSet<File>();
            Document doc = TaskUtils.loadDoc(prjfile);
            NodeList nl = doc.getElementsByTagName("project");
            // iterate over projects
            for (int i = 0; i < nl.getLength(); i++) {
                Element elem = (Element) nl.item(i);
                String prjName = elem.getAttribute("name");
                FileResource wsConfFile = ResourceUtil.getFileResource("file://" + prjdir.getAbsolutePath() + "/" + prjName + "/" + "conf" + "/"
                        + "webservice.conf.xml");
                // go on processing if webservices found
                if (wsConfFile.exists()) {
                    int wsdlCount = 0;
                    int stubCount = 0;
                    File tmpDir = getTmpDir(prjName);
                    // read webservice configuration
                    Configuration srvConf = ConfigurationReader.read(wsConfFile);
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
                            log("Error deserializing old reference configuration", x, Project.MSG_VERBOSE);
                            log("Warning: Ignore old reference configuration because it can't be deserialized. "
                                    + "Services will be built from scratch.", Project.MSG_WARN);
                        }
                    }
                    // Setup WSDL repository
                    File appDir = new File(webappsdir, prjName);
                    if (!appDir.exists()) throw new BuildException("Web application directory of project '" + prjName + "' doesn't exist");
                    File wsdlDir = tmpDir;
                    if (globConf.getWSDLSupportEnabled()) {
                        String wsdlRepo = globConf.getWSDLRepository();
                        if (wsdlRepo.startsWith("/")) wsdlRepo.substring(1);
                        wsdlDir = new File(appDir, wsdlRepo);
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
                        stubDir = new File(appDir, stubRepo);
                        if (!stubDir.exists()) {
                            boolean ok = stubDir.mkdir();
                            if (!ok) throw new BuildException("Can't create webservice stub directory " + stubDir.getAbsolutePath());
                        }
                    }
                    // Check if WEB-INF exists
                    File webInfDir = new File(appDir, "WEB-INF");
                    if (!webInfDir.exists())
                        throw new BuildException("Web application WEB-INF subdirectory of project '" + prjName + "' doesn't exist");
                    // Setup endpoint document
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document endPointsDoc = db.newDocument();
                    Element endPointsElem = endPointsDoc.createElementNS(XMLNS_JAXWS_RUNTIME, "ws:endpoints");
                    endPointsElem.setAttribute("version", "2.0");
                    endPointsDoc.appendChild(endPointsElem);
                    boolean hasSOAPService=false;
                    // Iterate over services
                    for (ServiceConfig conf : srvConf.getServiceConfig()) {
                        if (conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY) || conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_SOAP)) {
                            hasSOAPService=true;
                            ServiceConfig refConf = null;
                            if (refSrvConf != null) refConf = refSrvConf.getServiceConfig(conf.getName());
                            File wsdlFile = new File(wsdlDir, conf.getName() + ".wsdl");
                            // Add endpoint configuration
                            Element endPointElem = endPointsDoc.createElementNS(XMLNS_JAXWS_RUNTIME, "ws:endpoint");
                            endPointsElem.appendChild(endPointElem);
                            endPointElem.setAttribute("name", conf.getName());
                            endPointElem.setAttribute("implementation", conf.getImplementationName() + "JAXWS");
                            endPointElem.setAttribute("url-pattern", globConf.getRequestPath() + "/" + conf.getName());
                            Element chainsElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler-chains");
                            Element chainElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler-chain");
                            chainsElem.appendChild(chainElem);
                            Element handlerElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler");
                            chainElem.appendChild(handlerElem);
                            Element handlerClassElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler-class");
                            handlerElem.appendChild(handlerClassElem);
                            handlerClassElem.setTextContent("de.schlund.pfixcore.webservice.jaxws.ErrorHandler");
                            if(globConf.getMonitoringEnabled()||globConf.getLoggingEnabled()) {
                                handlerElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler");
                                chainElem.appendChild(handlerElem);
                                handlerClassElem = endPointsDoc.createElementNS(XMLNS_JAVAEE, "ee:handler-class");
                                handlerElem.appendChild(handlerClassElem);
                                handlerClassElem.setTextContent("de.schlund.pfixcore.webservice.jaxws.RecordingHandler");
                            }
                            endPointElem.appendChild(chainsElem);
                            // Check if WSDL/Javascript has to be built
                            if (refConf == null || !wsdlFile.exists() || globalConfChanged || !conf.equals(refConf)
                                    || TaskUtils.checkInterfaceChange(conf.getInterfaceName(), builddir, wsdlFile)) {
                                checkInterface(conf.getInterfaceName());
                                Class<?> implClass = Class.forName(conf.getImplementationName());
                                // Generate WSDL
                                File wsgenDir=new File(tmpdir,"wsdl/"+conf.getName()+"/"+conf.getImplementationName());
                                if(!wsgenDirs.contains(wsgenDir)) {
                                    if(!wsgenDir.exists()) wsgenDir.mkdirs();
                                    WsGen wsgen = new WsGen();
                                    wsgen.setProject(getProject());
                                    wsgen.setDynamicAttribute("genwsdl", "true");
                                    wsgen.setDynamicAttribute("destdir", "build");
                                    wsgen.setDynamicAttribute("resourcedestdir", wsgenDir.getAbsolutePath());
                                    wsgen.setDynamicAttribute("classpath", classPath.toString());
                                    wsgen.setDynamicAttribute("sei", conf.getImplementationName() + "JAXWS");
                                    String serviceName = "{" + TaskUtils.getTargetNamespace(implClass) + "}" + conf.getName();
                                    wsgen.setDynamicAttribute("servicename", serviceName);
                                    wsgen.execute();
                                    wsgenDirs.add(wsgenDir);
                                } 
                                FileUtils.copyFiles(wsgenDir, wsdlDir, ".*wsdl", ".*xsd");
                                // Replace endpoint URL
                                Element srvElem = (Element) elem.getElementsByTagName("servername").item(0);
                                if (srvElem == null)
                                    throw new BuildException("Missing servername element in configuration of project '" + prjName + "'");
                                String srvName = srvElem.getTextContent().trim();
                                String srvPort = "";
                                if (standalone) srvPort = ":" + (portbase + 80);
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
                    // Generate JAXWS runtime endpoint configuration
                    File endPointsFile = new File(webInfDir, "sun-jaxws.xml");
                    if (hasSOAPService && (!endPointsFile.exists() || wsdlCount > 0)) {
                        Transformer t = TransformerFactory.newInstance().newTransformer();
                        t.setOutputProperty(OutputKeys.INDENT, "yes");
                        t.transform(new DOMSource(endPointsDoc), new StreamResult(new FileOutputStream(endPointsFile)));
                        log("Generated JAXWS runtime endpoint configuration.");
                    }
                    // Store current webservice configuration file
                    ConfigurationReader.serialize(srvConf, refWsConfFile);
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
     * Set project configuration file.
     */
    public void setPrjfile(File prjfile) {
        this.prjfile = prjfile;
    }

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

    /**
     * Set directory for temporary/webservice build artifacts.
     */
    public void setTmpdir(File tmpdir) {
        this.tmpdir = tmpdir;
    }

    /**
     * Set webapp deployment directory.
     */
    public void setWebappsdir(File webappsdir) {
        this.webappsdir = webappsdir;
    }

    /**
     * Set reference to classpath containing application classes.
     */
    public void setClasspathRef(Reference ref) {
        classPath = new Path(getProject());
        classPath.createPath().setRefid(ref);
    }

    /**
     * Set if it's build in Tomcat standalone mode.
     */
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    /**
     * Set the relative port for the Tomcat.
     */
    public void setPortbase(int portbase) {
        this.portbase = portbase;
    }

}
