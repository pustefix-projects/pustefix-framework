 /*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.util;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

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
        String log4jconfigstr = (log4jconfigfile == null) ? null : log4jconfigfile.toString();
        if (log4jconfigstr == null || log4jconfigstr.equals("")) {
            throw new BuildException("Need the log4jconfig attribute.");
        }
        DOMConfigurator.configure(log4jconfigstr);

        DirectoryScanner scanner = getDirectoryScanner(getDir());
        scanner.scan();
        String[] confignames = scanner.getIncludedFiles(); // **/depend.xml relative to getDir()

        if (confignames.length > 0) {
            try {
                for (int i = 0; i < confignames.length; i++) {
                    File confile = new File(getDir(), confignames[i]);
                    if (confile.exists() && confile.canRead() && confile.isFile()) {
                        try {
                            gen = createTargetGenerator(confile);
                            gen.setIsGetModTimeMaybeUpdateSkipped(false);
                            System.out.println("---------- Doing " + confignames[i] + "...");
                            gen.generateAll();
                            System.out.println("---------- ...done [" + confignames[i] + "]");

                            TargetGenerator.resetFactories();
                        } catch (Exception e) {
                            throw new BuildException("Oops! TargetGenerator exit!", e);
                        }
                    } else {
                        throw new BuildException("Couldn't read configfile '" + confignames[i] + "'");
                    }
                }
            } finally {
                log(TargetGenerator.getReportAsString(), Project.MSG_INFO);
            }
        } else {
            log("Need configfile to work on", Project.MSG_WARN);
        }
        
    }
    
    protected TargetGenerator createTargetGenerator(File confile) {
        TargetGenerator ret;        
        try {
            ret = new TargetGenerator(confile);
        } catch (Exception e) {
            throw new BuildException("Can not initialize TargetGenerator",e);
        }
        return ret;
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
