 /*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.util;



import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.Path;
import java.io.File;
import java.util.Properties;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;

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

        PathFactory.getInstance().init(getDir().getPath());
        
        DirectoryScanner scanner = getDirectoryScanner(getDir());
        scanner.scan();
        String[] confignames = scanner.getIncludedFiles(); // **/depend.xml relative to getDir()

        if (confignames.length > 0) {
            try {
                for (int i = 0; i < confignames.length; i++) {
                    Path confilepath = PathFactory.getInstance().createPath(confignames[i]);
                    File confile = confilepath.resolve();
                    if (confile.exists() && confile.canRead() && confile.isFile()) {
                        try {
                            gen = createTargetGenerator(confilepath);
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
            }
        } else {
            log("Need configfile to work on", Project.MSG_WARN);
        }
        
    }
    
    protected TargetGenerator createTargetGenerator(Path confilepath) {
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
