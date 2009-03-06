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
package de.schlund.pfixcore.util;



import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TargetGeneratorTask extends MatchingTask {

    /** holds directory to perform generation in */
    private File dir;
    /** holds the file containing the log4j configuration */
    private File log4jconfig;

    public void execute() throws BuildException {
        
        TargetGenerator gen = null;

        // code has been taken over from de/schlund/pfixxml/targets/TargetGenerator#main(String[])
        
        File log4jconfigfile = getLog4jconfig();
        if (log4jconfigfile == null) {
            throw new BuildException("Need the log4jconfig attribute.");
        }
        DOMConfigurator.configure(log4jconfig.getPath());

        try {
            GlobalConfigurator.setDocroot(getDir().getPath());
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
        
        DirectoryScanner scanner = getDirectoryScanner(getDir());
        scanner.scan();
        String[] confignames = scanner.getIncludedFiles(); // **/depend.xml relative to getDir()

        if (confignames.length > 0) {
            try {
                for (int i = 0; i < confignames.length; i++) {
                    FileResource confile = ResourceUtil.getFileResourceFromDocroot(confignames[i]);
                    if (confile.exists() && confile.canRead() && confile.isFile()) {
                        try {
                            gen = createTargetGenerator(confile);
                            gen.setIsGetModTimeMaybeUpdateSkipped(false);
                            System.out.println("---------- Doing " + confignames[i] + "...");
                            gen.generateAll();
                            System.out.println("---------- ...done [" + confignames[i] + "]");

                            TargetGenerator.resetFactories();
                        } catch (Exception e) {
                            throw new BuildException(confile + ": " + e.getMessage(), e);
                        }
                    } else {
                        throw new BuildException("Couldn't read configfile '" + confignames[i] + "'");
                    }
                }
            } finally {
                log(TargetGenerator.getReportAsString(), Project.MSG_INFO);
                if(TargetGenerator.errorsReported()) throw new BuildException("TargetGenerator reported errors.");
            }
        } else {
            log("Need configfile to work on", Project.MSG_WARN);
        }
        
    }
    
    protected TargetGenerator createTargetGenerator(FileResource confilepath) {
        try {
            return new TargetGenerator(confilepath);
        } catch (Exception e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public File getLog4jconfig() {
        return log4jconfig;
    }

    public void setLog4jconfig(File log4jconfig) {
        this.log4jconfig = log4jconfig;
    }

}
