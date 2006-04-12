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

package de.schlund.util.configmigration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class MigrationTool {

    public static void main(String[] args) {
        // Use Saxon 6.5.x
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.icl.saxon.TransformerFactoryImpl");

        // Check whether current working path is a Pustefix environment
        File centralProjectsFile = new File(
                "projects/servletconf/projects.xml.in");
        File projectsDir = new File("./projects");
        if (!centralProjectsFile.exists()) {
            centralProjectsFile = new File(
                    "example/servletconf/projects.xml.in");
            projectsDir = new File("./example");
        }
        if (!centralProjectsFile.exists()) {
            System.err.println("Neither projects/servletconf/projects.xml.in"
                    + " nor example/servletconf/projects.xml.in"
                    + " could be found, exiting...");
            return;
        }

        // Talk to user
        System.out.print("WARNING this tool will overwrite existing files!\n");
        System.out
                .print("Make a backup of your environment before using this tool!\n\n");
        try {
            System.out.print("Process will start in 3");
            Thread.sleep(1000);
            System.out.print(", 2");
            Thread.sleep(1000);
            System.out.print(", 1");
            Thread.sleep(1000);
            System.out.print(", now!\n\n");
        } catch (InterruptedException e) {
            System.out.print("\nAborted!\n");
            return;
        }
        System.out.print("Creating file list...");

        // List of configuration files that have to be processed
        ArrayList<String> filenamesToProcess = new ArrayList<String>();
        ArrayList<String> filenamesThatFailed = new ArrayList<String>();

        File tempFile;

        // Add files, that will not be found automatically
        filenamesToProcess.add("servletconf/projects.xml.in");
        tempFile = new File(projectsDir, "common/conf/pfixlog.xml.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/pfixlog.xml.in");
        }
        tempFile = new File(projectsDir, "common/conf/pfixlog.xml.in.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/pfixlog.xml.in.in");
        }
        tempFile = new File(projectsDir, "common/conf/pustefix.prop.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/pustefix.prop.in");
        }
        tempFile = new File(projectsDir, "common/conf/factory.prop.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/factory.prop.in");
        }
        tempFile = new File(projectsDir, "common/conf/exceptionhandler.prop.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/exceptionhandler.prop.in");
        }
        tempFile = new File(projectsDir, "common/conf/apploader.prop.in");
        if (tempFile.exists()) {
            filenamesToProcess.add("common/conf/apploader.prop.in");
        }

        // Iterate over all project directories
        File[] projectDirs = projectsDir.listFiles();
        for (int i = 0; i < projectDirs.length; i++) {
            // Ignore special directories "core" and "servletconf", and
            // files that are no directories
            // Files in "common" directory have already been manually added
            String projectName = projectDirs[i].getName();
            if (!projectDirs[i].isDirectory() || projectDirs[i].isHidden()
                    || projectName.equals("servletconf")
                    || projectName.equals("core")
                    || projectName.equals("common")) {
                continue;
            }

            File confDir = new File(projectDirs[i], "conf");

            // If there is no "conf" subdirectory, this is not a project
            if (!confDir.exists()) {
                continue;
            }

            // Iterate over all files in directory
            File[] confFiles = confDir.listFiles();
            for (int j = 0; j < confFiles.length; j++) {
                String filename = confFiles[j].getName();
                if (filename.equals("depend.xml.in")
                        || filename.equals("webservice.conf.in")
                        || filename.equals("project.xml.in")
                        || filename.endsWith(".prop.in")) {
                    filenamesToProcess.add(projectName + "/conf/" + filename);
                }
            }
        }

        // Talk to user
        System.out.print(" DONE\n\n");

        // Process all files
        for (Iterator i = filenamesToProcess.iterator(); i.hasNext();) {
            String filename = (String) i.next();
            File sourceFile = new File(projectsDir, filename);

            // Talk to user
            System.out.print("Processing " + filename + "...");

            String targetFilename = null;
            boolean success = false;
            String errorString = "Unknown error";

            try {
                if (filename.endsWith("/depend.xml.in")) {
                    targetFilename = filename.substring(0,
                            filename.length() - 3);
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getDependTemplates());
                    if (sourceFile.delete())
                        success = true;
                } else if (filename.endsWith("/pfixlog.xml.in")
                        || filename.endsWith("/pfixlog.xml.in.in")) {
                    if (filename.endsWith("/pfixlog.xml.in")) {
                        targetFilename = filename.substring(0, filename
                                .length() - 3);
                    } else {
                        targetFilename = filename;
                    }
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getPfixlogTemplates());
                    if (!targetFilename.equals(filename)) {
                        if (sourceFile.delete())
                            success = true;
                    } else {
                        success = true;
                    }
                } else if (filename.endsWith("/webservice.conf.in")) {
                    targetFilename = filename.substring(0,
                            filename.length() - 3)
                            + ".xml";
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getWebserviceTemplates());
                    if (sourceFile.delete())
                        success = true;
                } else if (filename.endsWith("/project.xml.in")
                        || filename.endsWith("/projects.xml.in")) {
                    targetFilename = filename;
                    File targetFile = sourceFile;
                    transform(sourceFile, targetFile, getProjectTmeplates());
                    success = true;
                } else if (filename.equals("common/conf/pustefix.prop.in")
                        || filename.equals("common/conf/factory.prop.in")) {
                    targetFilename = filename.substring(0,
                            filename.length() - 8)
                            + ".xml";
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getPropertiesTemplates());
                    if (sourceFile.delete())
                        success = true;
                } else if (filename.endsWith(".prop.in")
                        && filename.startsWith("common/conf/")) {
                    targetFilename = filename.substring(0,
                            filename.length() - 8)
                            + ".xml";
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getPropertiesTemplates());
                    if (sourceFile.delete()) {
                        success = true;
                    }
                } else if (filename.endsWith(".prop.in")) {
                    targetFilename = filename.substring(0,
                            filename.length() - 8)
                            + ".conf.xml";
                    File targetFile = new File(projectsDir, targetFilename);
                    transform(sourceFile, targetFile, getPropertiesTemplates());
                    if (sourceFile.delete())
                        success = true;
                }
            } catch (Exception e) {
                errorString = "\"" + e.getMessage() + "\"";
                success = false;
            }
            if (success) {
                System.out.print(" OK\n");
                System.out.print("  ==> " + targetFilename + "\n");
            } else {
                System.out.print(" FAILED\n");
                System.out.print("  xx> " + errorString + "\n");
                filenamesThatFailed.add(filename);
            }
            i.remove();
        }

        // Talk to user
        System.out.print("\nProcessing completed.\n");
        if (filenamesThatFailed.size() == 0) {
            System.out.print("\nAll files have been successfully processed!\n");
        } else {
            System.out.print("\nThe following files have failed:\n");
            for (Iterator i = filenamesThatFailed.iterator(); i.hasNext();) {
                String filename = (String) i.next();
                System.out.print("  " + filename + "\n");
            }
        }
    }

    private static Templates getTemplates(String tmplName)
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        Source s = new StreamSource(MigrationTool.class.getClassLoader()
                .getResource("xsl/" + tmplName + ".xsl").toExternalForm());
        Templates tmpl = TransformerFactory.newInstance().newTemplates(s);
        return tmpl;
    }

    private static Templates getPropertiesTemplates()
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        return getTemplates("properties");
    }

    private static Templates getProjectTmeplates()
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        return getTemplates("project");
    }

    private static Templates getWebserviceTemplates()
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        return getTemplates("webservice");
    }

    private static Templates getPfixlogTemplates()
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        return getTemplates("pfixlog");
    }

    private static Templates getDependTemplates()
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError {
        return getTemplates("depend");
    }

    private static void transform(File sourceFile, File targetFile,
            Templates templates) throws TransformerException, IOException {
        Transformer tr = templates.newTransformer();
        Source source = new StreamSource(sourceFile);
        File tempFile = File.createTempFile("transform", ".xml");
        tempFile.deleteOnExit();
        Result result = new StreamResult(tempFile);
        tr.transform(source, result);
        copy(tempFile, targetFile);
    }

    private static void copy(File source, File target) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);
        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        in.close();
        out.close();
    }

}
