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

package org.pustefixframework.maven.plugins.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import de.schlund.pfixxml.util.Xml;

/**
 * This mojo iterates over all statusmessage files included from a source directory and creates or merges changes into
 * an according merge file in the destination directory. Source and destination directory are used as base directories,
 * i.e. a statusmessage file in a source sub directory will produce a merge file in an according sub directory of the
 * destination directory. You additionally can define a filename suffix for this file, which is added after the name but
 * before the file extension.
 *
 * @goal merge-statusmessages
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class PustefixMergeMojo extends AbstractMojo {

    private static final String[] DEFAULT_INCLUDES =
            new String[]{"dyntxt/statusmessages.xml", "dyntxt/statusmessages-core.xml"};

    /**
     * Where modules have been unpacked
     *
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}/modules"
     */
    private String modulesdir;

    /**
     * Where modules will be merged
     *
     * @parameter default-value="${basedir}/src/main/webapp/modules-override"
     */
    private String modulesDestDirname;

    /**
     * Filename suffix for merged statusmessages file.
     *
     * @parameter default-value="-merged"
     */
    private String mergeSuffix;

    /**
     * XPath selecting the nodes to be merged.
     *
     * @parameter default-value="/include_parts/part/theme[@name='default']"
     */
    private String selection;

    /**
     * Files to be merged; default is "dyntxt/statusmessages.xml dyntxt/statusmessages-core.xml".
     *
     * @parameter
     */
    private String[] includes;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * @parameter
     */
    private boolean merge = true;
    

    public void execute() throws MojoExecutionException {
        
        if(merge) {
        
        File webappDir = new File(project.getBasedir(), "src/main/webapp");
        if(webappDir.exists()) {
        
            // Merge modules statuscodes
            File modulesSrcDir = new File(modulesdir);
            if (modulesSrcDir.exists() && modulesSrcDir.isDirectory()) {
                File[] subDirs = modulesSrcDir.listFiles();
                for (File moduleDir : subDirs) {
                    if (moduleDir.isDirectory()) {
                        process(moduleDir, modulesDestDirname + "/" + moduleDir.getName());
                    }
                }
            }
            // Merge statusmessages from modules which were not extracted
            processUnextractedModules();
        
        }
        
        }
    }    
    
    
    /**
     * Iterate over all included files and create according merge file or, if already existing, merge included file into
     * the merge file.
     */
    private void process(File srcDir, String destDirname) throws MojoExecutionException {

        File destDir = new File(destDirname);

        DirectoryScanner scanner = new DirectoryScanner();
        if (includes != null) {
            scanner.setIncludes(includes);
        } else {
            // Use default
            scanner.setIncludes(DEFAULT_INCLUDES);
        }
        scanner.setBasedir(srcDir);
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        for (String file : files) {
            File srcFile = new File(srcDir, file);
            File destFile = new File(destDir, file);
            if (mergeSuffix != null) {
                String name = destFile.getName();
                int ind = name.indexOf('.');
                if (ind > -1) {
                    name = name.substring(0, ind) + mergeSuffix + name.substring(ind);
                } else {
                    throw new MojoExecutionException("Expected file name containing file extension: "
                            + destFile.getAbsolutePath());
                }
                destFile = new File(destFile.getParentFile(), name);
            }

            if (!destFile.exists()) {
                try {
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    FileUtils.copyFile(srcFile, destFile);
                } catch (IOException x) {
                    throw new MojoExecutionException("Error copying statusmessages from '" + srcFile.getAbsolutePath()
                            + "' to '" + destFile.getAbsolutePath() + "'.", x);
                }
                try {
                    Document doc = Xml.parseMutable(destFile);
                    addComment(doc, file);
                    Xml.serialize(doc, destFile, true, true);
                } catch (Exception x) {
                    throw new MojoExecutionException("Error adding comment to statusmessages file '"
                            + destFile.getAbsolutePath() + "'.", x);
                }
                getLog().info("Created " + destFile + " from source file " + srcFile);
            } else {
                try {
                    InputSource src = new InputSource();
                    src.setSystemId(srcFile.toURI().toString());
                    src.setByteStream(new FileInputStream(srcFile));
                    Merge merge = new Merge(src, selection, destFile, false);
                    merge.run();
                } catch (Exception x) {
                    throw new MojoExecutionException("Merging to file " + destFile.getAbsolutePath() + " failed.", x);
                }
                getLog().info("Merged source file " + srcFile + " into " + destFile);
            }
        }
    }

    /**
     * Insert a comment at the beginning of the statusmessages document.
     */
    private void addComment(Document doc, String mergeSource) {
        Comment comment =
                doc.createComment("\nThis file contains merged statusmessages from '" + mergeSource + "'.\n"
                        + "You can modify this file to change the messages for exisiting statuscodes.\n"
                        + "Adding new statuscodes has to be done within the originating module.\n");
        Element element = doc.getDocumentElement();
        if (element.hasChildNodes()) {
            element.insertBefore(comment, element.getFirstChild());
            element.insertBefore(doc.createTextNode(" \n \n"), element.getFirstChild());
        } else {
            element.appendChild(doc.createTextNode("\n"));
            element.appendChild(comment);
        }
    }

    @SuppressWarnings("unchecked")
    public void processUnextractedModules() throws MojoExecutionException {
        List<Artifact> artifacts; artifacts = project.getCompileArtifacts();
        for (Artifact artifact : artifacts) {
            if ("jar".equals(artifact.getType())) {
                processUnextractedModule(artifact.getFile());
            }
        }
    }
    
    private void processUnextractedModule(File jarFile) throws MojoExecutionException {
        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while reading JAR file " + jarFile, e);
        }
        ZipEntry descEntry = jar.getEntry("META-INF/pustefix-module.xml");
        if(descEntry != null) {
            String moduleName;
            try {
                InputStream descIn = jar.getInputStream(descEntry);
                moduleName = getModuleName(descIn);
                descIn.close();
            } catch(IOException x) {
                throw new MojoExecutionException("Can't read module name from descriptor: " + jarFile.getAbsolutePath());
            }
            String[] modIncludes = DEFAULT_INCLUDES;
            if(includes != null) modIncludes = includes;
            File modulesSrcDir = new File(modulesdir);
            File destDir = new File(modulesDestDirname);
            for(String modInclude: modIncludes) {
                ZipEntry entry = jar.getEntry("PUSTEFIX-INF/"+modInclude);
                if(entry != null) {
                    File extracted = new File(modulesSrcDir, moduleName + "/" + modInclude);
                    String moduleURI = "module://" + moduleName + "/PUSTEFIX-INF/" + modInclude;
                    if(!extracted.exists()) {
                        File destFile = new File(destDir, moduleName + "/" + modInclude);
                        if (mergeSuffix != null) {
                            String name = destFile.getName();
                            int ind = name.indexOf('.');
                            if (ind > -1) {
                                name = name.substring(0, ind) + mergeSuffix + name.substring(ind);
                            } else {
                                throw new MojoExecutionException("Expected file name containing file extension: "
                                        + destFile.getAbsolutePath());
                            }
                            destFile = new File(destFile.getParentFile(), name);
                        }
                        if (!destFile.exists()) {
                            try {
                                if (!destFile.getParentFile().exists()) {
                                    destFile.getParentFile().mkdirs();
                                }
                                InputStream in = jar.getInputStream(entry);
                                FileOutputStream out = new FileOutputStream(destFile);
                                byte[] buffer = new byte[4096];
                                int no = 0;
                                try {
                                    while ((no = in.read(buffer)) != -1)
                                        out.write(buffer, 0, no);
                                } finally {
                                    in.close();
                                    out.close();
                                }
                            } catch (IOException x) {
                                throw new MojoExecutionException("Error copying statusmessages from '" + moduleURI
                                        + "' to '" + destFile.getAbsolutePath() + "'.", x);
                            }
                            try {
                                Document doc = Xml.parseMutable(destFile);
                                addComment(doc, moduleURI);
                                Xml.serialize(doc, destFile, true, true);
                            } catch (Exception x) {
                                throw new MojoExecutionException("Error adding comment to statusmessages file '"
                                        + destFile.getAbsolutePath() + "'.", x);
                            }
                            getLog().info("Created " + destFile + " from source file " + moduleURI);
                        } else {
                            try {
                                InputStream in = jar.getInputStream(entry);
                                InputSource src = new InputSource();
                                src.setSystemId(moduleURI);
                                src.setByteStream(in);
                                Merge merge = new Merge(src, selection, destFile, false);
                                merge.run();
                                in.close();
                            } catch (Exception x) {
                                throw new MojoExecutionException("Merging to file " + destFile.getAbsolutePath() + " failed.", x);
                            } 
                            getLog().info("Merged source file " + moduleURI + " into " + destFile);
                        }
                    }
                }
            }
        }  
    }
    
    private String getModuleName(InputStream in) throws MojoExecutionException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            XPath xpath = XPathFactory.newInstance().newXPath();
            return xpath.evaluate("/module-descriptor/module-name", doc);
        } catch(Exception x) {
            throw new MojoExecutionException("Error while reading module name from descriptor", x);
        }
    }
    
}
