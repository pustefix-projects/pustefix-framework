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
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.config.Configuration;
import org.pustefixframework.webservices.config.ConfigurationReader;
import org.pustefixframework.webservices.config.ServiceConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * This class generates annotated, JAXWS specific, webservice proxy classes from 
 * webservice stack independent webservice interface and implementation classes.
 * 
 * @author mleidig@schlund.de
 */
public class ProxyGenerator extends Task {
    
    private final static String VERSION = "2.0";
    
    private File prjfile;
    private File prjdir;
    private File builddir;
    private File gensrcdir;
    
    //Ant-task properties:
    
    /**
     * Set project configuration file.
     */
    public void setPrjfile(File prjfile) {
        this.prjfile=prjfile;
    }
    
    /**
     * Set project directory.
     */
    public void setPrjdir(File prjdir) {
        this.prjdir=prjdir;
    }
    
    /**
     * Set directory for generated sources.
     */
    public void setGensrcdir(File gensrcdir) {
        this.gensrcdir=gensrcdir;
    }
    
    /**
     * Set directory for built classes.
     */
    public void setBuilddir(File builddir) {
        this.builddir=builddir;
    }
    
    //---
    
    /**
     * Iterates over projects, reads webservice configuration files and
     * generates webservice proxy class for each SOAP service.
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
                    // read webservice configuration
                    Configuration srvConf = ConfigurationReader.read(wsConfFile);
                    int genCount = 0;
                    StringBuilder srcFiles = new StringBuilder();
                    for (ServiceConfig conf : srvConf.getServiceConfig()) {
                        if (conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY) || conf.getProtocolType().equals(Constants.PROTOCOL_TYPE_SOAP)) {
                            StringBuilder sb = new StringBuilder();
                            String implName = conf.getImplementationName();
                            String itfName = conf.getInterfaceName();
                            int ind = implName.lastIndexOf('.');
                            String pkgName = implName.substring(0, ind);
                            String simpleName = implName.substring(ind + 1);
                            ind = itfName.lastIndexOf('.');
                            Class<?> itfClass = Class.forName(itfName);
                            String proxyName = simpleName + "JAXWS";
                            String proxyFileName = pkgName.replaceAll("\\.", "/") + "/" + proxyName + ".java";
                            File proxyFile = new File(gensrcdir, proxyFileName);
                            if (!proxyFile.exists() || TaskUtils.checkInterfaceChange(itfName, builddir, proxyFile)) {
                                sb.append("package " + pkgName + ";\n");
                                sb.append("\n");
                                sb.append("import javax.jws.*;\n");
                                sb.append("import org.pustefixframework.webservices.jaxws.JAXWSContext;\n");
                                sb.append("import org.pustefixframework.webservices.jaxws.ProxyInvocationException;\n");
                                sb.append("\n");
                                sb.append("/**\n");
                                sb.append("  * Auto-generated Pustefix/JAXWS proxy\n");
                                sb.append("  *\n");
                                sb.append("  * Webservice interface: " + itfName + "\n");
                                sb.append("  * Webservice implementation: " + implName + "\n");
                                sb.append("  * Proxy version: " + VERSION + "\n");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                sb.append("  * Creation date: " + format.format(new Date()) + "\n");
                                sb.append("  */\n");
                                sb.append("@WebService(name=\"" + conf.getName() + "\")\n");
                                sb.append("public class " + proxyName + " {\n");
                                sb.append("\n");
                                sb.append("  private " + simpleName + " impl;\n");
                                sb.append("\n");
                                sb.append("  public " + proxyName + "() {\n");
                                sb.append("    impl = new " + simpleName + "();\n");
                                sb.append("  }\n");
                                Method[] meths = itfClass.getMethods();
                                for (Method meth : meths) {
                                    if(TaskUtils.hasInterfaceType(meth)) {
                                        log("Warning: Skip method '"+meth.getName()+"' because interface types aren't supported here by JAXWS.",Project.MSG_WARN);
                                        continue;
                                    }
                                    sb.append("\n");
                                    sb.append("  @WebMethod\n");
                                    sb.append("  public ");
                                    String methName = meth.getName();
                                    Type retType = meth.getGenericReturnType();
                                    sb.append(TaskUtils.getTypeString(retType) + " " + methName + "(");
                                    Type[] paramTypes = meth.getGenericParameterTypes();
                                    String callStr = "";
                                    for (int no = 0; no < paramTypes.length; no++) {
                                        sb.append(TaskUtils.getTypeString(paramTypes[no]) + " in" + no);
                                        callStr += "in" + no;
                                        if (no < paramTypes.length - 1) {
                                            sb.append(", ");
                                            callStr += ", ";
                                        }
                                    }
                                    sb.append(") ");
                                    /*
                                    Class<?>[] exTypes = meth.getExceptionTypes();
                                    if (exTypes.length > 0) {
                                        sb.append("throws ");
                                    }
                                    for (int exNo = 0; exNo < exTypes.length; exNo++) {
                                        sb.append(exTypes[exNo].getCanonicalName());
                                        if (exNo < exTypes.length - 1) sb.append(", ");
                                    }
                                    */
                                    sb.append(" {\n");
                                    sb.append("    JAXWSContext ctx=JAXWSContext.getCurrentContext();\n");
                                    sb.append("    ctx.startInvocation();\n");
                                    sb.append("    try {\n");
                                    String ret = "return ";
                                    if (retType == void.class) ret = "";
                                    sb.append("      " + ret + "impl." + methName + "(" + callStr + ");\n");
                                    sb.append("    } catch(Throwable t) {\n");
                                    sb.append("      ctx.setThrowable(t);\n");
                                    sb.append("      throw new ProxyInvocationException(t);\n");
                                    sb.append("    } finally {\n");
                                    sb.append("      ctx.endInvocation();\n");
                                    sb.append("    }\n");
                                    sb.append("  }\n");
                                }
                                sb.append("\n");
                                sb.append("}\n");
                                File parentDir = proxyFile.getParentFile();
                                if (!parentDir.exists()) parentDir.mkdirs();
                                FileOutputStream out = new FileOutputStream(proxyFile);
                                out.write(sb.toString().getBytes("ISO-8859-1"));
                                out.close();
                                log("Created JAXWS proxy: " + proxyFile.getAbsolutePath(), Project.MSG_VERBOSE);
                                srcFiles.append(" " + proxyFileName);
                                genCount++;
                            }
                        }
                    }
                    if (genCount > 0) {
                        log("Generated " + genCount + " proxy class" + (genCount == 1 ? "" : "es") + ".");
                        getProject().setProperty("webservice.proxysources", srcFiles.toString());
                    }
                }
            }
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }
    
}
