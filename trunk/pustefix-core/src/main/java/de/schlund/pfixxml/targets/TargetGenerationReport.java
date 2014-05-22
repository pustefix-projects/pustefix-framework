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
package de.schlund.pfixxml.targets;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TargetGenerationReport implements TargetGeneratorListener {
    
    private String LINE1 = "============================================================================================";
    private String LINE2 = "|----------------------------------------------------------------------------------";
    
    private Logger logger;
    
    private List<TargetGenerationException> errors = new ArrayList<TargetGenerationException>();
    
    private long start;
    private long end;
    
    private int startedTargets;
    private int finishedTargets;
    private int failedTargets;
    
    public TargetGenerationReport() {
    }
    
    public TargetGenerationReport(Logger logger) {
        this.logger = logger;
    }
    
    public void start(TargetGenerator targetGen) {
        start = System.currentTimeMillis();
        info("Start generating targets...");
    }
    
    public void end(TargetGenerator targetGen) {
        end = System.currentTimeMillis();
        info(LINE1);
        info("Generated " + finishedTargets + " targets in " + (end - start) + "ms");
        if(failedTargets > 0) {
            info("Failed to create " + failedTargets + " from " + startedTargets + " targets due to errors");
        }
        if (errors.isEmpty()) {
            info("No exceptions");
        } else {
            info("Exceptions:");
            Iterator<TargetGenerationException> it = errors.iterator();
            if(it.hasNext()) {
                info(LINE2);
            }
            while(it.hasNext()) {
                String errorMsg = it.next().toStringRepresentation();
                String[] errorLines = errorMsg.split("\n");
                for(String errorLine: errorLines) {
                    info(errorLine);
                }
                if(it.hasNext()) {
                    info(LINE2);
                }
            }
        }
        info(LINE1);
    }
    
    public boolean hasError() {
        return !errors.isEmpty();
    }
 
    public void error(Target target, TargetGenerationException exception) {
        failedTargets++;
        errors.add(exception);
        String path = target.getTargetGenerator().getDisccachedir().toURI().toString();
        error(">>>>> Error generating " + path + File.separator + target.getTargetKey() + " from " + 
                target.getXMLSource().getTargetKey() + " and " + target.getXSLSource().getTargetKey(), exception);
    }
    
    public void start(Target target) {
        startedTargets++;
    }
    
    public void end(Target target) {
        finishedTargets++;
        String path = target.getTargetGenerator().getDisccachedir().toURI().toString();
        debug(">>>>> Generated " + path + File.separator + target.getTargetKey() + " from " + 
                target.getXMLSource().getTargetKey() + " and " + target.getXSLSource().getTargetKey());
    }
    
    protected void debug(String msg) {
        logger.log(Level.FINE, msg);
    }
    
    protected void error(String msg, TargetGenerationException exception) {
        logger.log(Level.SEVERE, msg, exception);
    }
    
    protected void info(String msg) {
        logger.log(Level.INFO, msg);
    }
    
}