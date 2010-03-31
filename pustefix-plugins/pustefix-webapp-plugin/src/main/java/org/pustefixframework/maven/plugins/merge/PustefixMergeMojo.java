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
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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


    public void execute() throws MojoExecutionException {
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
                    Merge merge = new Merge(srcFile, selection, destFile, false);
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

}
