 /*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.ant;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
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
    /** holds File.class */
    private static final Class[] CLASSARR_FILE = { File.class };
    /** classname of TargetGenerator to instatiate */
    public static final String CLASSNAME_TARGETGENERATOR = "de.schlund.pfixxml.targets.TargetGenerator";
    /** classname of DOMConfigurator to instatiate */
    public static final String CLASSNAME_DOMCONFIGURATOR = "org.apache.log4j.xml.DOMConfigurator";
    
    /**
     * Code has been taken over from de/schlund/pfixxml/targets/TargetGenerator#main(String[]).
     * 
     * @see de.schlund.pfixxml.targets.TargetGenerator#main(String[])
     */
    public void execute() throws BuildException {
        
        File log4jconfigfile = getLog4jconfig();
        String log4jconfig = (log4jconfigfile == null) ? null : log4jconfigfile.toString();
        if (log4jconfig == null || log4jconfig.equals("")) {
            throw new BuildException("Need the log4jconfig attribute.");
        }
        
        // DOMConfigurator.configure(log4jconfig);

        
        
        DirectoryScanner scanner = getDirectoryScanner(getDir());
        scanner.scan();
        String[] confignames = scanner.getIncludedFiles(); // **/depend.xml relative to getDir()

        if (confignames.length <= 0) {
            log("Need configfiles (usually depend.xml) to work on", Project.MSG_WARN);
            return;
        }

        Class classGenerator = null;
        Constructor constructorGen = null;
        Method methodGenAll = null;
        Method methodStaticReport = null;
        
        try {
            classGenerator = Class.forName(CLASSNAME_TARGETGENERATOR);
            constructorGen = classGenerator.getConstructor(CLASSARR_FILE);
            methodGenAll = classGenerator.getMethod("generateAll", null);
            methodStaticReport = classGenerator.getMethod("getReportAsString", null);
        } catch (Exception e) {
            throw new BuildException("Could not initialize "+CLASSNAME_TARGETGENERATOR, e);
        }

        try {
            Object objectGenerator = null;
            File[] fileArr = new File[1];
            File confile = null;
            for (int i = 0; i < confignames.length; i++) {
                confile = new File(getDir(), confignames[i]);
                if (confile.exists() && confile.canRead() && confile.isFile()) {
                    fileArr[0] = confile;
                    try {
                        objectGenerator = constructorGen.newInstance(fileArr);
                    } catch(Exception e) {
                        // print out stacktrace, as it is not displayed
                        // when running without debug
                        e.printStackTrace();
                        throw new BuildException("Could not instantiate new TargetGenerator! Configfile '"+confile+"'. Run with ant -debug to see full stacktrace of Exception", e);
                    }
                    try {
                        System.out.println("---------- Doing " + confignames[i] + "...");
                        methodGenAll.invoke(objectGenerator, null);
                        System.out.println("---------- ...done [" + confignames[i] + "]");
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new BuildException("TargetGenerator Exception. Configfile '"+confile+"'. Run with ant -debug to see full stacktrace of Exception", e);
                    }
                } else {
                    throw new BuildException("Couldn't read configfile '" + confile + "'");
                }
            }
        } finally {
            String report = "No TargetGenerator report available.";
            try {
                report = (String) methodStaticReport.invoke(null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log(report, Project.MSG_INFO);
        }
            
//            try {
//                for (int i = 0; i < confignames.length; i++) {
//                    File confile = new File(getDir(), confignames[i]);
//                    if (confile.exists() && confile.canRead() && confile.isFile()) {
//                        try {
//                            // cleanup
//                            //gen = TargetGeneratorFactory.getInstance().createGenerator(confile.toString());
//                            gen = createTargetGenerator(confile);
//                            gen.setIsGetModTimeMaybeUpdateSkipped(false);
//                            System.out.println("---------- Doing " + confignames[i] + "...");
//                            gen.generateAll();
//                            System.out.println("---------- ...done [" + confignames[i] + "]");
//                        } catch (Exception e) {
//                            throw new BuildException("Oops! TargetGenerator exit! Run with ant -debug to see stacktrace of Exception", e);
//                        }
//                    } else {
//                        throw new BuildException("Couldn't read configfile '" + confignames[i] + "'");
//                    }
//                }
//            } finally {
//                log(TargetGenerator.getReportAsString(), Project.MSG_INFO);
//            }
        
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
