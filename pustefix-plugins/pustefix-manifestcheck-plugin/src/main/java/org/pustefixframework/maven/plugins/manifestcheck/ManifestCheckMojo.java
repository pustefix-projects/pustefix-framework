package org.pustefixframework.maven.plugins.manifestcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * This Mojo will print warnings about common MANIFEST.MF configuration
 * mistakes when using a "MANIFEST first" approach for bundle creation.
 *
 * Implemented checks:
 * - checkImportPackage (prints warnings when finding java import
 *   statements without corresponding Import-Package directive; prints
 *   warnings for superfluous Import-Package entries)
 *
 * @goal verify
 * @phase prepare-package
 */
public class ManifestCheckMojo extends AbstractMojo {

    private Manifest manifest = null;

    /**
     * @parameter default-value="${basedir}/src/main/resources/META-INF/MANIFEST.MF"
     * @required
     */
    private String manifestFile;

    /**
     * @parameter
     */
    private Set<File> srcDirs;


    public void execute() throws MojoExecutionException {
        if (!validateParams()) {
            return;
        }
        checkImportPackage();
    }


    private boolean validateParams() throws MojoExecutionException {
        // Validate parameter manifestFile
        File mfSrc = new File(manifestFile);
        if (!mfSrc.exists()) {
            log("warn", "No manifest present, using bundle plugin?");
            return false;
        }
        getLog().info("Checking manifest file '" + manifestFile + "'");
        try {
            manifest = new Manifest(new FileInputStream(mfSrc));
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read Manifest", e);
        }

        // Validate parameter srcDirs
        if (srcDirs == null) {
            srcDirs = new HashSet<File>();
            srcDirs.add(new File("src/main/java"));
            srcDirs.add(new File("target/generated-sources"));
        }
        Iterator<File> it = srcDirs.iterator();
        while (it.hasNext()) {
            File dir = it.next();
            if (!dir.isDirectory()) {
                log("warn", "SrcDir " + dir.getAbsolutePath() + " is not a directory");
                it.remove();
            }
        }

        return true;
    }

    private void checkImportPackage() throws MojoExecutionException {
        Set<String> imports = new TreeSet<String>();
        Set<String> packages = new TreeSet<String>();

        // We scan all src files
        for (File srcDir : srcDirs) {
            scanSrcFiles(srcDir, imports, packages);
        }

        // We compare with MANIFEST
        findImportsInManifest(imports);
    }


    private void scanSrcFiles(final File rootDir, final Set<String> imports, final Set<String> packages)
            throws MojoExecutionException {
        final List<File> srcFiles = new ArrayList<File>();
        addFilesRecursively(rootDir, srcFiles);
        for (File file : srcFiles) {
            grepImportsAndPackage(file, imports, packages);
        }

        // We compare import statements to our own package statements
        Iterator<String> it = imports.iterator();
        while (it.hasNext()) {
            String imprt = it.next();
            if (packages.contains(imprt)) {
                it.remove();
            }
        }
    }

    private void findImportsInManifest(final Set<String> javaImports) {
        // We collect all Import-Package entries
        Attributes attrs = manifest.getMainAttributes();
        String value = attrs.getValue("Import-Package");
        if (value == null) {
            log("warn", "No 'Import-Package' entry in manifest found");
            return;
        }
        Set<String> manifestImports = new TreeSet<String>();
        String[] importPackages = value.split(",");
        for (String importPackage : importPackages) {
            importPackage = importPackage.trim();
            int idx = importPackage.indexOf(";");
            if (idx > -1) {
                importPackage = importPackage.substring(0, idx);
            }
            manifestImports.add(importPackage);
        }

        for (String javaImport : javaImports) {
            if (!manifestImports.contains(javaImport)) {
                log("warn", "Manifest file does not contain Import-Package entry for '" + javaImport + "'");
            }
        }
        for (String mfImport : manifestImports) {
            if (!javaImports.contains(mfImport)) {
                log("warn", "Manifest file contains potentially superfluous Import-Package entry '" + mfImport + "'");
            }
        }
    }

    private void grepImportsAndPackage(final File file, final Set<String> imports, final Set<String> packages)
            throws MojoExecutionException {
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bfr.readLine()) != null) {
                line = line.replaceAll("\t", " ").trim();
                if (line.startsWith("import ")) {
                    line = line.replaceAll("import", "").replaceAll(";", "").replaceAll("\\s", "");
                    int idx = line.lastIndexOf(".");
                    line = line.substring(0, idx);
                    if (!line.startsWith("java.")) {
                        imports.add(line);
                    }
                } else if (line.startsWith("package ")) {
                    line = line.replaceAll("package", "").replaceAll(";", "").replaceAll("\\s", "");
                    packages.add(line);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read Java file", e);
        } finally {
            try {
                bfr.close();
            } catch (Exception e) {
                // Cannot handle;
            }
        }
    }

    private void addFilesRecursively(final File file, final List<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isFile() && child.getName().endsWith(".java")) {
                    all.add(child);
                }
                addFilesRecursively(child, all);
            }
        }
    }

    private void log(final String level, final String message) {
        Log log = getLog();
        try {
            Method meth = log.getClass().getDeclaredMethod(level, CharSequence.class);
            meth.invoke(log, "***** " + message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
