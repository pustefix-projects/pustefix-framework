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

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AptTask extends Task {
    private File lastAptRunFile = new File("target/.lastaptrun");
    private Path srcDir;
    private File destDir;
    private File preprocessDir;
    private String factory = "de.schlund.pfixcore.util.CommonAnnotationProcessorFactory";
    private Path classPath;
    private String encoding = "UTF-8";

    @Override
    public void execute() throws BuildException {
        long lastAptRun = lastAptRunFile.lastModified();
        List<File> modList = getModifiedFiles(lastAptRun);
        if (modList.size() > 0) {
            log("Processing " + modList.size() + " source file" + (modList.size() > 1 ? "s" : ""));
            Commandline cmd = new Commandline();
            cmd.setExecutable("apt");
            cmd.createArgument().setValue("-J-Xmx512m");
            cmd.createArgument().setValue("-classpath");
            classPath.addExisting(classPath.concatSystemClasspath());
            cmd.createArgument().setPath(classPath);
            cmd.createArgument().setValue("-sourcepath");
            cmd.createArgument().setPath(srcDir);
            cmd.createArgument().setValue("-nocompile");
            cmd.createArgument().setValue("-encoding");
            cmd.createArgument().setValue(encoding);
            cmd.createArgument().setValue("-factory");
            cmd.createArgument().setValue(factory);
            cmd.createArgument().setValue("-s");
            cmd.createArgument().setFile(preprocessDir);
            int firstFileIndex = cmd.size();
            for (File file : modList)
                cmd.createArgument().setValue(file.getAbsolutePath());
            log(cmd.toString(), Project.MSG_DEBUG);
            callApt(cmd.getCommandline(), firstFileIndex);
        }
        lastAptRunFile.delete();
        try {
            lastAptRunFile.createNewFile();
        } catch (IOException x) {
        }
    }

    private List<File> getModifiedFiles(long lastAptRun) throws BuildException {
        IWrapperFileScanner fileScanner = new IWrapperFileScanner();
        List<File> modList = new ArrayList<File>();
        String[] dirs = srcDir.list();
        for (int i = 0; i < dirs.length; i++) {
            File dir = getProject().resolveFile(dirs[i]);
            if (!dir.exists()) throw new BuildException("Source directory doesn't exist: " + dir.getAbsolutePath(), getLocation());
            List<File> newFiles = fileScanner.getChangedFiles(dir, destDir, lastAptRun);
            modList.addAll(newFiles);
        }
        if (fileScanner.getScanCount() > 0) System.out.println(fileScanner.printStatistics());
        return modList;
    }

    private void callApt(String[] args, int firstFileIndex) {
        File tmpFile = null;
        try {
            String[] commandArray = null;
            if (Commandline.toString(args).length() > 4096 && firstFileIndex >= 0) {
                PrintWriter out = null;
                try {
                    tmpFile = File.createTempFile("pfx-aptfiles-", ".tmp", null);
                    log("Commandline too long, using temporary file '" + tmpFile.getAbsolutePath() + "'.", Project.MSG_DEBUG);
                    tmpFile.deleteOnExit();
                    out = new PrintWriter(new FileWriter(tmpFile));
                    for (int i = firstFileIndex; i < args.length; i++) {
                        if (args[i].indexOf(" ") > -1) {
                            args[i] = args[i].replace(File.separatorChar, '/');
                            out.println("\"" + args[i] + "\"");
                        } else out.println(args[i]);
                    }
                    out.flush();
                    commandArray = new String[firstFileIndex + 1];
                    System.arraycopy(args, 0, commandArray, 0, firstFileIndex);
                    commandArray[firstFileIndex] = "@" + tmpFile;
                } catch (IOException x) {
                    throw new BuildException("Error creating temporary file", x, getLocation());
                } finally {
                    out.close();
                }
            } else {
                commandArray = args;
            }
            try {
                Execute exec = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
                exec.setAntRun(getProject());
                exec.setWorkingDirectory(getProject().getBaseDir());
                exec.setCommandline(commandArray);
                int ret = exec.execute();
                if (ret != 0) throw new BuildException("Error while executing apt (exit value: " + ret + ").");
            } catch (IOException x) {
                throw new BuildException("Error invoking apt", x, getLocation());
            }
        } finally {
            if (tmpFile != null && tmpFile.exists()) tmpFile.delete();
        }
    }

    public void setSrcdir(Path srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    public void setPreprocessdir(File preprocessDir) {
        this.preprocessDir = preprocessDir;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setClasspathRef(Reference ref) {
        classPath = new Path(getProject());
        classPath.createPath().setRefid(ref);
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
