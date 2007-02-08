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
 */

package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.util.Xml;

public class UnpackModuleTask extends MatchingTask {

    private File srcdir;
    private File extractdir;

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }
    
    public void setExtracttodir(File extractdir) {
        this.extractdir = extractdir;
    }
    
    @Override
    public void execute() throws BuildException {
        if (srcdir == null) {
            throw new BuildException("Mandatory attribute srcdir is not set!");
        }
        if (extractdir == null) {
            throw new BuildException("Mandatory attribute extractdir is not set!");
        }
        
        String[] filenames = getDirectoryScanner(srcdir).getIncludedFiles();
        for (String filename : filenames) {
            File file = new File(srcdir, filename);
            processJar(file);
        }
    }
    
    private void processJar(File jarFile) throws BuildException {
        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            throw new BuildException("Error while reading JAR file " + jarFile, e);
        }
        ZipEntry dde = jar.getEntry("META-INF/pustefix-module.xml");
        if (dde == null) {
            return;
        }
        InputStream dds;
        try {
            dds = jar.getInputStream(dde);
        } catch (IOException e) {
            throw new BuildException("Error while reading deployment descriptor from module " + jarFile, e);
        }
        DeploymentDescriptor dd;
        try {
            dd = new DeploymentDescriptor(dds);
        } catch (TransformerException e) {
            throw new BuildException("Error while parsing deployment descriptor from module " + jarFile, e);
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
                        targetfile = new File(this.extractdir, moduleName + "/" + shortpath);                        
                    } else {
                        targetfile = new File(this.extractdir, moduleName + "/" + targetpath + "/" + shortpath);
                    }
                    if (entry.isDirectory()) {
                        targetfile.mkdirs();
                    } else {
                        try {
                            createFileFromStream(jar.getInputStream(entry), targetfile);
                        } catch (IOException e) {
                            throw new BuildException("Could not unpack file from JAR module to " + targetfile, e);
                        }
                    }
                }
            }
        }
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
            doc = Xml.parse(new StreamSource(xmlStream));
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
