/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Generates a Pustefix webapp, but without the parts generated by Maven's war plugin. Executes in the generate-sources phase because 
 * some tests need files generated by this plugin.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class PustefixWebappMojo extends AbstractMojo {
    /**
     * Docroot of the application
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}"
     */
    private String docroot;

    /**
     * Where to unpack modules
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}/modules"
     */
    private String modulesdir;
    
    /**
     * Where to place apt-generated classes.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/apt"
     */
    private File aptdir;

    /**
     * @parameter expression="${machine}"
     */
    private String machine;

    /**
     * @parameter expression="${fqdn}"
     */
    private String fqdn;
    
    /**
     * @parameter expression="${makemode}"
     */
    private String makemode;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List<Artifact> pluginClasspath;

    
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        File basedir;
        
        if (makemode == null || makemode.length() == 0) {
            makemode = "test";
        }

        // because all executions operate on the same pfixcore classes:
        GlobalConfig.reset();
        
        GlobalConfigurator.setDocroot(docroot);
        new File(docroot, "WEB-INF").mkdirs();

        getLog().info("unpacked " + unpackModules() + " module(s)");
        try {
            buildtimeProps();
        } catch (IOException e) {
            throw new MojoExecutionException("error creating buildtime.props", e);
        }
        basedir = project.getBasedir();
        if (new Apt(basedir, aptdir, getLog()).execute(getPluginClasspath()) > 0) {
            project.addCompileSourceRoot(aptdir.getAbsolutePath());
        }
    }

    private String getPluginClasspath() {
        StringBuilder result;
        
        result = new StringBuilder();
        for (String path : pathStrings(pluginClasspath)) {
            if (result.length() > 0) {
                result.append(':');
            } 
            result.append(path);
        }
        return result.toString();
    }

    public void extractJar(String src, File dest) throws IOException {
         JarFile jar;
         JarEntry entry;
         File file;
         byte[] buffer = new byte[4096];
         
         getLog().info("extracting " + src + " to " + dest);
         jar = new JarFile(src);
         Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements()) {
             entry = entries.nextElement();
             file = new File(dest, entry.getName());
             if (entry.isDirectory()) {
                 if (!file.mkdirs()) {
                     throw new IOException(file + ": cannot create directory");
                 }
             } else {
                 copy(buffer, jar.getInputStream(entry), file);
             }
         }
         jar.close();
    }

    private void copy(byte[] buffer, InputStream src, File file) throws IOException {
         OutputStream out;
         int len;
         
         out = new FileOutputStream(file);
         while (true)  {
             len = src.read(buffer);
             if (len < 0) {
                 break;
             }
             out.write(buffer, 0, len);
         }
         out.close();
         src.close();
    }


    private void buildtimeProps() throws IOException {
        BuildTimeProperties.generate(getProperties(), makemode, getMachine(), getFqdn(), System.getProperty("user.name"));
    }
    
    private Properties getProperties() {
    	Properties orig;
    	Properties result;
    	String key;
    	
    	result = new Properties();
    	orig = project.getProperties();
        for (Map.Entry<Object, Object> entry: orig.entrySet()) {
        	key = (String) entry.getKey();
        	if (key.startsWith("pustefix.")) {
        		result.setProperty(key, (String) entry.getValue());
        	}
        }
    	return result;
    }

    private String getMachine() throws UnknownHostException {
        String str;
        int idx;
        
        if (machine != null && machine.length() > 0) {
            return machine;
        }
        str = System.getenv("MACHINE");
        if (str != null && str.length() > 0) {
            return str;
        }
        str = InetAddress.getLocalHost().getCanonicalHostName();
        idx = str.indexOf('.');
        return idx == -1 ? str : str.substring(0, idx);
    }

    private String getFqdn() throws UnknownHostException {
        String result;
        int idx;

        if (fqdn != null && fqdn.length() > 0) {
            return fqdn;
        }
        result = InetAddress.getLocalHost().getCanonicalHostName();
        idx = result.indexOf('.');
        result = idx == -1 ? "" : result.substring(idx);
        return getMachine() + result;
    }
    
    private static List<String> pathStrings(Collection<Artifact> artifacts) {
        List<String> lst;
        
        lst = new ArrayList<String>();
        if (artifacts != null) {
            for (Artifact a : artifacts) {
                lst.add(a.getFile().getPath());
            }
        }

        return lst;
    }
    
    //--
    
    public int unpackModules() throws MojoExecutionException {
        List<Artifact> artifacts;
        int count;
        File dir;
        
        if (modulesdir == null) {
            throw new MojoExecutionException("Mandatory attribute extractdir is not set!");
        }
        artifacts = project.getCompileArtifacts();
        count = 0;
        for (Artifact artifact : artifacts) {
            if ("jar".equals(artifact.getType())) {
                dir = processJar(artifact.getFile());
                if (dir != null) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /** @return unpacked directory */
    private File processJar(File jarFile) throws MojoExecutionException {
        JarFile jar;
        
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while reading JAR file " + jarFile, e);
        }
        ZipEntry dde = jar.getEntry("META-INF/pustefix-module.xml");
        if (dde == null) {
            return null;
        }
        InputStream dds;
        try {
            dds = jar.getInputStream(dde);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while reading deployment descriptor from module " + jarFile, e);
        }
        DeploymentDescriptor dd;
        try {
            dd = new DeploymentDescriptor(dds);
        } catch (TransformerException e) {
            throw new MojoExecutionException("Error while parsing deployment descriptor from module " + jarFile, e);
        }
        String moduleName = dd.getModuleName();
        for (DeploymentDescriptor.ResourceMapping rm : dd.getResourceMappings()) {
            String srcpath = rm.sourcePath;
            String targetpath = rm.targetPath;
            String searchpath = srcpath + "/";
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(searchpath)) {
                    String shortpath = entry.getName().substring(searchpath.length());
                    File targetfile;
                    if (targetpath.length() == 0) {
                        targetfile = new File(modulesdir, moduleName + "/" + shortpath);                        
                    } else {
                        targetfile = new File(modulesdir, moduleName + "/" + targetpath + "/" + shortpath);
                    }
                    if (entry.isDirectory()) {
                        targetfile.mkdirs();
                    } else {
                        try {
                            createFileFromStream(jar.getInputStream(entry), targetfile);
                        } catch (IOException e) {
                            throw new MojoExecutionException("Could not unpack file from JAR module to " + targetfile, e);
                        }
                    }
                }
            }
        }
        return new File(modulesdir, moduleName);
    }
    
    private void createFileFromStream(InputStream inputStream, File targetfile) throws IOException {
        FileOutputStream fos = new FileOutputStream(targetfile);
        int bytesread = 0;
        byte[] buf = new byte[1024];
        do {
            bytesread = inputStream.read(buf);
            if (bytesread > 0) {
                fos.write(buf, 0, bytesread);
            }
        } while (bytesread != -1);
        fos.close();
        inputStream.close();
    }

    private class DeploymentDescriptor {
        public class ResourceMapping {
            public String sourcePath;
            public String targetPath;
        }
        
        public final static String NS_MODULE = "http://pustefix.sourceforge.net/moduledescriptor200702";
        
        private String moduleName = "";

        private List<ResourceMapping> mappings;
        
        public DeploymentDescriptor(InputStream xmlStream) throws TransformerException {
            Document doc;
            doc = Xml.parse(XsltVersion.XSLT1, new StreamSource(xmlStream));
            Element root = doc.getDocumentElement();
            if (!root.getNamespaceURI().equals(NS_MODULE) || !root.getNodeName().equals("module-descriptor")) {
                throw new TransformerException("Descriptor has invalid format");
            }
            
            NodeList temp = root.getElementsByTagNameNS(NS_MODULE, "module-name");
            if (temp.getLength() != 1) {
                throw new TransformerException("Module name not set!");
            }
            Element nameElement = (Element) temp.item(0);
            temp = nameElement.getChildNodes();
            for (int i=0; i < temp.getLength(); i++) {
                if (temp.item(i).getNodeType() != Node.TEXT_NODE) {
                    throw new TransformerException("Found malformed module-name element!");
                }
                moduleName += temp.item(i).getNodeValue();
            }
            moduleName = moduleName.trim();
            
            temp = root.getElementsByTagNameNS(NS_MODULE, "resources");
            if (temp.getLength() > 1) {
                throw new TransformerException("Found more than one resources element!");
            }
            if (temp.getLength() == 0) {
                this.mappings = new ArrayList<ResourceMapping>();
                return;
            }
            temp = ((Element)temp.item(0)).getElementsByTagNameNS(NS_MODULE, "resource-mapping");
            ArrayList<ResourceMapping> mappings = new ArrayList<ResourceMapping>();
            for (int i=0; i<temp.getLength(); i++) {
                Element el = (Element) temp.item(i);
                String srcpath = el.getAttribute("srcpath");
                if (srcpath == null) {
                    throw new TransformerException("Mandatory attribute srcpath not set on resource-mapping attribute");
                }
                if (srcpath.startsWith("/")) {
                    srcpath = srcpath.substring(1);
                }
                if (srcpath.endsWith("/")) {
                    srcpath = srcpath.substring(0, srcpath.length()-1);
                }
                String targetpath = el.getAttribute("targetpath");
                if (targetpath == null) {
                    targetpath = "";
                }
                if (targetpath.startsWith("/")) {
                    targetpath = targetpath.substring(1);
                }
                if (targetpath.endsWith("/")) {
                    targetpath = targetpath.substring(0, targetpath.length()-1);
                }
                ResourceMapping rm = new ResourceMapping();
                rm.sourcePath = srcpath;
                rm.targetPath = targetpath;
                mappings.add(rm);
            }
            this.mappings = mappings;
        }
        
        public List<ResourceMapping> getResourceMappings() {
            return this.mappings;
        }
        
        public String getModuleName() {
            return this.moduleName;
        }
    }
}
