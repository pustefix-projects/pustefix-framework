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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Ant task copying the paths specified using the passthrough option
 * in a projects.xml file from a source to a destination directory.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class WebappCopyPassthroughHtdocsTask extends Task {
    private File srcdir;

    private File destdir;

    private File projectsxml;

    private String projectname;

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setProjectsxml(File projectsxml) {
        this.projectsxml = projectsxml;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public void execute() throws BuildException {
        checkParameters();

        String htdocspath = getHtdocs();
        File htdocs = new File(htdocspath);
        if (htdocs.exists() && htdocs.isDirectory()) {
            // Change srcdir temporarily
            File temp = srcdir;
            srcdir = htdocs;
            copyDirectory("/");
            srcdir = temp;
        }

        Set<String> paths = getPassthroughPaths();

        for (String path : paths) {
            System.out.println("Processing passthrough " + path);
            File pathelement = new File(srcdir, path);
            if (pathelement.exists() && !pathelement.isHidden()) {
                if (pathelement.isFile()) {
                    copyFile(path);
                } else if (pathelement.isDirectory()) {
                    copyDirectory(path);
                } else {
                    System.out.println("Ignoring " + path);
                }
            }
        }
    }

    private void checkParameters() throws BuildException {
        if (projectsxml == null) {
            throw new BuildException(
                    "Mandatory attribute \"projectsxml\" is not set!");
        }
        if (!projectsxml.exists() || !projectsxml.isFile()) {
            throw new BuildException("File " + projectsxml + " does not exist!");
        }
        if (srcdir == null) {
            throw new BuildException(
                    "Mandatory attribute \"srcdir\" is not set!");
        }
        if (!srcdir.exists() || !srcdir.isDirectory()) {
            throw new BuildException("Directory " + srcdir + " does not exist!");
        }
        if (destdir == null) {
            throw new BuildException(
                    "Mandatory attribute \"destdir\" is not set!");
        }
        if (!destdir.exists() || !destdir.isDirectory()) {
            throw new BuildException("Directory " + destdir
                    + " does not exist!");
        }
        if (projectname == null || projectname.equals("")) {
            throw new BuildException("Attribute \"projectname\" has to be set!");
        }
    }

    private Set<String> getPassthroughPaths() throws BuildException {
        HashSet<String> paths = new HashSet<String>();

        try {
            Document doc = Xml.parseMutable(projectsxml);
            List<Node> nodes = XPath
                    .select(
                            doc,
                            "/projects/project/passthrough/text()|/projects/common/apache/passthrough/text()");
            for (Node node : nodes) {
                if (node.getNodeValue().length() > 0)
                    paths.add(node.getNodeValue());
            }
        } catch (Exception e) {
            throw new BuildException("Cannot parse " + projectsxml, e);
        }

        return paths;
    }

    private String getHtdocs() throws BuildException {
        try {
            Document doc = Xml.parseMutable(projectsxml);
            Node node = XPath.selectNode(doc, "/projects/project[@name='"
                    + projectname + "']/documentroot/text()");
            if (node != null) {
                String val = node.getNodeValue();
                if (val != null && val.length() > 0) {
                    return val;
                } else {
                    return null;
                }
            }
            return null;

        } catch (Exception e) {
            throw new BuildException("Cannot parse " + projectsxml, e);
        }
    }

    private void copyFile(String path) {
        try {
            File srcfile = new File(srcdir, path);
            File destfile = new File(destdir, path);

            File parent = destfile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            FileInputStream fis = new FileInputStream(srcfile);
            FileOutputStream fos = new FileOutputStream(destfile);

            int bytes_read = 0;
            byte buffer[] = new byte[512];
            while ((bytes_read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes_read);
            }

            fis.close();
            fos.close();

        } catch (IOException e) {
            throw new BuildException("Error while copying file " + path);
        }
    }

    private void copyDirectory(String path) {
        File dir = new File(srcdir, path);

        String subpaths[] = dir.list();
        for (String subpath : subpaths) {
            String completepath = path + File.separator + subpath;
            File subfile = new File(srcdir, completepath);

            if (subfile.exists() && !subfile.isHidden()) {
                if (subfile.isFile()) {
                    copyFile(completepath);
                } else if (subfile.isDirectory()) {
                    copyDirectory(completepath);
                } else {
                    System.out.println("Ignoring " + completepath);
                }
            }
        }
    }
}